package com.inview.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HeyGen Streaming Avatar 토큰 발급 프록시
 */
@Service
@RequiredArgsConstructor
public class HeygenService {

    @Value("${heygen.api-key}")
    private String apiKey;

    // 변경 가능(기본값 유지)
    @Value("${heygen.base-url:https://api.heygen.com}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 토큰 원문 응답(안정 파싱: token이 data.token에 있든 top-level에 있든 둘 다 케어)
     */
    public Map<String, Object> createStreamingTokenRaw() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);           // HeyGen 문서 기준
        headers.setBearerAuth(apiKey);              // 혹시 모를 호환용 함께 전송

        HttpEntity<Map<String, Object>> req = new HttpEntity<>(Map.of(), headers);

        // HeyGen 문서에서 사용하는 엔드포인트 (하위 호환 위해 한 곳으로 통일)
        String url = baseUrl + "/v1/streaming.create_token";

        try {
            ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.POST, req, Map.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new IllegalStateException("HeyGen token API error: " + resp.getStatusCode());
            }

            Map<String, Object> body = resp.getBody();
            Object token = body.get("token");
            Object expires = body.get("expires_at");

            Object data = body.get("data");
            if (token == null && data instanceof Map) {
                token = ((Map<?, ?>) data).get("token");
                if (expires == null) expires = ((Map<?, ?>) data).get("expires_at");
            }

            if (token == null) {
                throw new IllegalStateException("HeyGen token missing in response");
            }

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("token", token.toString());
            if (expires != null) out.put("expires_at", String.valueOf(expires));
            return out;

        } catch (HttpStatusCodeException e) {
            throw new IllegalStateException("HeyGen token upstream error: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new IllegalStateException("HeyGen token internal error: " + e.getMessage(), e);
        }
    }

    /** 프런트가 기대하는 순수 토큰 문자열 */
    public String createStreamingToken() {
        return (String) createStreamingTokenRaw().get("token");
    }
}
