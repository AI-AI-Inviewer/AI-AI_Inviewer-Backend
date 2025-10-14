package com.inview.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Pattern;
import java.util.*;

/**
 * OpenAI 연동 서비스 (채팅/음성/STT/최종평가)
 * - 최종평가: 보수적(엄격) 집계 + 휴리스틱 페널티 적용
 */
@Service
public class OpenAiService {

    @Value("${openai.api-key}")
    private String apiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_STT_URL  = "https://api.openai.com/v1/audio/transcriptions";

    private final ObjectMapper mapper = new ObjectMapper();

    // ---------- 유틸 ----------
    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
    private static int floorMul(int v, double f) { return (int)Math.floor(v * f); }

    private static class Metrics {
        int userTurns;
        int userWords;
        double avgWords;
        boolean noExperience;     // "아직 … 못했", "경험 없음" 등
        boolean mentionsMovie;    // "영화" 언급
        boolean mentionsProject;  // "프로젝트/실습/CTF/포트폴리오/깃허브" 등
    }

    private static Metrics analyzeTranscript(String transcript) {
        Metrics m = new Metrics();
        if (transcript == null) return m;
        String[] lines = transcript.split("\\R");
        Pattern noExp = Pattern.compile("(아직.*(시작|일).*(못했|못하)|실무.*없|경험.*없)");
        Pattern proj  = Pattern.compile("(프로젝트|과제|실습|CTF|캡스톤|포트폴리오|깃허브|리포지토리|POC)", Pattern.CASE_INSENSITIVE);

        for (String line : lines) {
            if (line.startsWith("[후보자]")) {
                String txt = line.replaceFirst("^\\[후보자\\]\\s*", "").trim();
                if (!txt.isEmpty()) {
                    m.userTurns++;
                    m.userWords += txt.split("\\s+").length;
                    if (noExp.matcher(txt).find()) m.noExperience = true;
                    if (txt.contains("영화")) m.mentionsMovie = true;
                    if (proj.matcher(txt).find()) m.mentionsProject = true;
                }
            }
        }
        m.avgWords = m.userTurns > 0 ? (double)m.userWords / m.userTurns : 0.0;
        return m;
    }

    // ---------- Chat ----------
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

    // ---------- STT ----------
    public String transcribe(byte[] audioBytes, String filename, String mimeType, String language) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(apiKey);

        ByteArrayResource audioResource = new ByteArrayResource(audioBytes) {
            @Override public String getFilename() { return filename; }
        };
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(
                mimeType != null ? mimeType : MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE));

        HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(audioResource, fileHeaders);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", filePart);
        body.add("model", "gpt-4o-transcribe"); // (안되면 whisper-1)
        if (language != null && !language.isBlank()) body.add("language", language);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<java.util.Map> response =
                restTemplate.exchange(OPENAI_STT_URL, HttpMethod.POST, requestEntity, java.util.Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Object text = response.getBody().get("text");
            if (text instanceof String) return (String) text;
        }
        throw new RuntimeException("STT 실패: " + response.getStatusCode());
    }

    // ---------- 최종 평가(엄격 모드) ----------
    public Map<String, Object> evaluateFinal(String company, String resumeSummary, String transcript, int rounds) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 모델 지시: 총점은 합계로 산출(0~100), 과장 금지, JSON만
        String systemPrompt =
                "당신은 '" + company + "' 회사의 면접 최종 평가자입니다. " +
                        "대화록을 근거로 4개 항목(직무적합성/전문성/인성 및 태도/공직윤리)을 각각 0~25 정수로 채점하고, " +
                        "총점은 네 항목 합계(0~100)로 계산하세요. " +
                        "지원자의 답변이 짧거나 실무 경험이 부족하면 낮은 점수를 주세요. 과장 금지. " +
                        "반드시 JSON만 출력하세요(설명 금지). 각 점수는 반드시 근거 인용(evidence) 배열에 짧게 괄호 인용을 남기세요.";

        // JSON Schema (strict)
        Map<String, Object> schema = Map.of(
                "type","object",
                "additionalProperties", false,
                "properties", Map.of(
                        "score", Map.of(
                                "type","object",
                                "additionalProperties", false,
                                "properties", Map.of(
                                        "직무적합성", Map.of("type","integer","minimum",0,"maximum",25),
                                        "전문성",     Map.of("type","integer","minimum",0,"maximum",25),
                                        "인성및태도", Map.of("type","integer","minimum",0,"maximum",25),
                                        "공직윤리",   Map.of("type","integer","minimum",0,"maximum",25),
                                        "총점",       Map.of("type","integer","minimum",0,"maximum",100)
                                ),
                                "required", List.of("직무적합성","전문성","인성및태도","공직윤리","총점")
                        ),
                        "recommendation", Map.of("type","string","enum", List.of("합격","보류","불합격")),
                        "summary", Map.of("type","string"),
                        "evidence", Map.of("type","array","items", Map.of("type","string"), "minItems", 3, "maxItems", 8)
                ),
                "required", List.of("score","recommendation","summary","evidence")
        );
        Map<String, Object> responseFormat = Map.of(
                "type","json_schema",
                "json_schema", Map.of("name","InterviewFinalScore","strict", true, "schema", schema)
        );

        Map<String, Object> messages = Map.of(
                "messages", List.of(
                        Map.of("role","system","content", systemPrompt),
                        Map.of("role","user","content",
                                "[자기소개서 요약]\n" + (resumeSummary==null ? "" : resumeSummary) +
                                        "\n\n[대화록]\n" + (transcript==null ? "" : transcript))
                )
        );

        // 대화 메트릭 분석(엄격 페널티용)
        Metrics m = analyzeTranscript(transcript);

        // 라운드별 호출 (seed 고정 + temperature 낮게)
        List<Map<String,Object>> results = new ArrayList<>();
        for (int i = 0; i < Math.max(1, rounds); i++) {
            Map<String, Object> reqBody = new HashMap<>();
            reqBody.putAll(messages);
            reqBody.put("model", "gpt-4o-mini");
            reqBody.put("temperature", 0.2);
            reqBody.put("seed", 42 + i);
            reqBody.put("response_format", responseFormat);

            HttpEntity<Map<String,Object>> request = new HttpEntity<>(reqBody, headers);
            ResponseEntity<Map> resp = restTemplate.postForEntity(OPENAI_API_URL, request, Map.class);

            // --- 안전 추출 ---
            Map body = resp.getBody();
            if (body == null) throw new RuntimeException("OpenAI 응답 본문이 비어있습니다.");
            List choices = (List) body.get("choices");
            if (choices == null || choices.isEmpty()) throw new RuntimeException("OpenAI choices가 비어있습니다.");
            Map first = (Map) choices.get(0);
            Map message = (Map) first.get("message");

            Map<String,Object> jo;
            Object parsed = message.get("parsed");
            if (parsed instanceof Map) {
                //noinspection unchecked
                jo = (Map<String, Object>) parsed;
            } else {
                Object contentObj = message.get("content");
                String contentStr = String.valueOf(contentObj).trim();
                // 코드펜스 제거 ```json ... ```
                if (contentStr.startsWith("```")) {
                    contentStr = contentStr.replaceFirst("^```(?:json)?\\s*", "");
                    contentStr = contentStr.replaceFirst("\\s*```\\s*$", "");
                }
                try {
                    //noinspection unchecked
                    jo = mapper.readValue(contentStr, Map.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("OpenAI JSON 파싱 실패: " + e.getOriginalMessage() +
                            " / content=" + contentStr, e);
                }
            }
            results.add(jo);
        }

        if (results.isEmpty()) {
            throw new RuntimeException("평가 결과를 생성하지 못했습니다.");
        }

        // 보수적 집계: 라운드별 점수의 최솟값(min)
        int[] s = new int[4];
        String[] keys = {"직무적합성","전문성","인성및태도","공직윤리"};
        for (int ki = 0; ki < keys.length; ki++) {
            String k = keys[ki];
            List<Integer> arr = new ArrayList<>();
            for (Map<String,Object> r: results) {
                Map<String,Object> sc = (Map<String,Object>) r.get("score");
                arr.add(((Number) sc.get(k)).intValue());
            }
            int minScore = arr.stream().mapToInt(Integer::intValue).min().orElse(0);
            s[ki] = clamp(minScore, 0, 25);
        }

        // ---- 엄격 페널티 규칙 ----
        // 0) 극히 짧은 대화(턴<5) → 일괄 40% 감점
        if (m.userTurns < 5) {
            for (int i = 0; i < 4; i++) s[i] = floorMul(s[i], 0.6);
        }
        // 1) 평균 단어 수가 12 미만 → 추가 15% 감점
        if (m.avgWords < 12.0) {
            for (int i = 0; i < 4; i++) s[i] = floorMul(s[i], 0.85);
        }
        // 2) 실무경험 없음 선언 → 직무적합성 ≤10, 전문성 ≤8
        if (m.noExperience) {
            s[0] = clamp(s[0], 0, 10); // 직무적합성
            s[1] = clamp(s[1], 0, 8);  // 전문성
        }
        // 3) 영화만 언급(프로젝트/실습 언급 없음) → 직무적합성 ≤8
        if (m.mentionsMovie && !m.mentionsProject) {
            s[0] = clamp(s[0], 0, 8);
        }

        // 클램프(0~25)
        for (int i = 0; i < 4; i++) s[i] = clamp(s[i], 0, 25);

        // 총점 = 합계(보정 없음)
        int total = s[0] + s[1] + s[2] + s[3];

        // 텍스트 필드는 첫 결과 사용(원하면 합성 가능)
        Map<String,Object> pick = results.get(0);

        Map<String,Object> out = new LinkedHashMap<>();
        out.put("score", Map.of(
                "직무적합성", s[0],
                "전문성",     s[1],
                "인성및태도", s[2],
                "공직윤리",   s[3],
                "총점",       total
        ));
        out.put("recommendation", pick.get("recommendation"));
        out.put("summary", pick.get("summary"));
        out.put("evidence", pick.get("evidence"));

        return out;
    }
}
