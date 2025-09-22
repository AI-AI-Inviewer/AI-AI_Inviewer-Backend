package com.inview.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElevenLabsService {


    @Value("${elevenlabs.base-url:https://api.elevenlabs.io}")   // ← 기본값 교체
    private String baseUrl;

    @Value("${elevenlabs.api-key:}")
    private String rawApiKey;

    @Value("${elevenlabs.project-id:}")
    private String rawProjectId;

    @Value("${elevenlabs.model-id:eleven_multilingual_v2}")
    private String defaultModelId;

    @Value("${elevenlabs.default-voice-id:21m00Tcm4TlvDq8ikWAM}")
    private String defaultVoiceId;

    @Value("${elevenlabs.output-format:mp3_44100_128}")
    private String defaultOutputFormat;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    void printConfiguredKeyOnce() {
        log.info("[11Labs] API key configured: {}", mask(rawApiKey));
        log.info("[11Labs] Project ID configured: {}", mask(rawProjectId));
        log.info("[11Labs] Base URL: {}", baseUrl);
    }

    @PostConstruct
    void tuneRestTemplate() {
        restTemplate.setRequestFactory(
                new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory())
        );
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse resp) throws IOException {
                return false;
            }
        });

        restTemplate.getInterceptors().add((req, body, exec) -> {
            log.info("[11Labs][REQ] {} {}", req.getMethod(), req.getURI());

            HttpHeaders masked = new HttpHeaders();
            masked.putAll(req.getHeaders());
            maskHeader(masked, "xi-api-key");
            maskHeader(masked, "xi-project-id");
            log.info("[11Labs][REQ] headers={}", masked);

            if (body != null && body.length > 0) {
                log.info("[11Labs][REQ] body={}", new String(body, StandardCharsets.UTF_8));
            }

            var resp = exec.execute(req, body);
            log.info("[11Labs][RES] status={} headers={}", resp.getStatusCode(), resp.getHeaders());
            return resp;
        });
    }

    // -------------------------
    // Public API
    // -------------------------

    /** 최소 페이로드 TTS */

    public byte[] tts(String text, String voiceId, String modelId, String outputFormat) {

        final String apiKey = requireApiKey();
        final String projectId = optionalProjectId(); // ← 추가

        HttpHeaders headers = new HttpHeaders();
        headers.set("xi-api-key", apiKey);
        if (!projectId.isBlank()) {                    // ← 프로젝트 헤더 추가
            headers.set("xi-project-id", projectId);
        }
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("text", text);
        body.put("model_id", nullOr(modelId, defaultModelId)); // eleven_flash_v2_5 추천

        String url = String.format(
                "%s/v1/text-to-speech/%s?output_format=%s",
                baseUrl,
                nullOr(voiceId, defaultVoiceId),
                nullOr(outputFormat, defaultOutputFormat)
        );

        return exchangeForAudio(url, headers, body);
    }


    /** 보이스 목록 */
    public String listVoicesRaw() {
        final String apiKey = requireApiKey();
        final String projectId = optionalProjectId();

        HttpHeaders headers = new HttpHeaders();
        headers.set("xi-api-key", apiKey);
        if (!projectId.isBlank()) {
            headers.set("xi-project-id", projectId);
        }

        ResponseEntity<String> resp = restTemplate.exchange(
                baseUrl + "/v1/voices",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        return resp.getBody();
    }

    /** /v1/user */
    public String whoAmI() {
        final String apiKey = requireApiKey();
        final String projectId = optionalProjectId();

        HttpHeaders headers = new HttpHeaders();
        headers.set("xi-api-key", apiKey);
        if (!projectId.isBlank()) {
            headers.set("xi-project-id", projectId);
        }
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<String> resp = restTemplate.exchange(
                baseUrl + "/v1/user",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        log.info("[11Labs] /v1/user status={}, body={}", resp.getStatusCode(), resp.getBody());
        return resp.getBody();
    }

    // -------------------------
    // Private helpers
    // -------------------------

    private byte[] exchangeForAudio(String url, HttpHeaders headers, Map<String, Object> body) {
        try {
            String json = objectMapper.writeValueAsString(body);

            return restTemplate.execute(
                    url,
                    HttpMethod.POST,
                    req -> {
                        req.getHeaders().putAll(headers);
                        req.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        req.getBody().write(json.getBytes(StandardCharsets.UTF_8));
                    },
                    resp -> {
                        HttpStatusCode status = resp.getStatusCode();
                        byte[] bytes = resp.getBody() != null
                                ? resp.getBody().readAllBytes()
                                : new byte[0];
                        log.info("[11Labs] status={} len={}", status, bytes.length);

                        if (status.is2xxSuccessful() && bytes.length > 0) return bytes;

                        String err = new String(bytes, StandardCharsets.UTF_8);
                        log.error("[11Labs] Non-2xx from ElevenLabs. status={} body={}", status, err);
                        throw new IllegalStateException("Unexpected status from ElevenLabs: " + status);
                    }
            );

        } catch (Exception e) {
            log.error("TTS error", e);
            throw new RuntimeException(e);
        }
    }

    private String requireApiKey() {
        String apiKey = rawApiKey == null ? "" : rawApiKey.trim();
        if (apiKey.isEmpty()) throw new IllegalStateException("ELEVENLABS_API_KEY is empty");
        return apiKey;
    }

    private String optionalProjectId() {
        return rawProjectId == null ? "" : rawProjectId.trim();
    }

    private static <T> T nullOr(T v, T fallback) {
        return v != null ? v : fallback;
    }

    private static String mask(String v) {
        if (v == null || v.isBlank()) return "(empty)";
        String k = v.trim();
        int keepHead = Math.min(6, k.length());
        int keepTail = Math.min(4, Math.max(0, k.length() - keepHead));
        String head = k.substring(0, keepHead);
        String tail = keepTail > 0 ? k.substring(k.length() - keepTail) : "";
        return head + "…" + tail;
    }

    private static void maskHeader(HttpHeaders headers, String key) {
        List<String> vals = headers.get(key);
        if (vals != null && !vals.isEmpty()) {
            headers.put(key, List.of(mask(vals.get(0))));
        }
    }

    /** output_format -> Content-Type */
    public MediaType resolveContentType(String outputFormat) {
        String f = (outputFormat == null ? "" : outputFormat).toLowerCase();
        if (f.startsWith("mp3"))  return MediaType.valueOf("audio/mpeg");
        if (f.startsWith("wav"))  return MediaType.valueOf("audio/wav");
        if (f.startsWith("ogg"))  return MediaType.valueOf("audio/ogg");
        if (f.startsWith("webm")) return MediaType.valueOf("audio/webm");
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
