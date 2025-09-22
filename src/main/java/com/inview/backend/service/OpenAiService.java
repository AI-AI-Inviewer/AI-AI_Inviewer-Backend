package com.inview.backend.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    @Value("${openai.api-key}")
    private String apiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_STT_URL  = "https://api.openai.com/v1/audio/transcriptions";

    public String chatWithOpenAi(List<Map<String, String>> messages) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", messages,
                "temperature", 0.7
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_API_URL, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List choices = (List) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map choice = (Map) choices.get(0);
                Map message = (Map) choice.get("message");
                return (String) message.get("content");
            }
        }
        return "OpenAI 응답 실패";
    }

    public String transcribe(byte[] audioBytes, String filename, String mimeType, String language) {
        RestTemplate restTemplate = new RestTemplate();

        // multipart/form-data 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(apiKey);

        // 파일 파트
        ByteArrayResource audioResource = new ByteArrayResource(audioBytes) {
            @Override public String getFilename() { return filename; }
        };
        HttpHeaders fileHeaders = new HttpHeaders();
        // 가능하면 클라이언트가 보낸 mimeType(webm/ogg/wav/mp3 등)을 넣어줌
        fileHeaders.setContentType(MediaType.parseMediaType(
                mimeType != null ? mimeType : MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE));

        HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(audioResource, fileHeaders);

        // 폼 데이터
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", filePart);
        body.add("model", "gpt-4o-transcribe"); // 최신 고정밀. 안되면 whisper-1 사용
        if (language != null && !language.isBlank()) body.add("language", language); // 예: "ko"
        // body.add("response_format", "json"); // 기본 json
        // body.add("temperature", "0"); // 필요 시

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<java.util.Map> response =
                restTemplate.exchange(OPENAI_STT_URL, HttpMethod.POST, requestEntity, java.util.Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Object text = response.getBody().get("text");
            if (text instanceof String) return (String) text;
        }
        throw new RuntimeException("STT 실패: " + response.getStatusCode());
    }
}
