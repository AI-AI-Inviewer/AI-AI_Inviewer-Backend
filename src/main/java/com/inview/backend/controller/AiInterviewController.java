package com.inview.backend.controller;

import com.inview.backend.dto.TtsRequest;
import com.inview.backend.service.ElevenLabsService;
import com.inview.backend.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class AiInterviewController {

    private final OpenAiService openAiService;
    private final ElevenLabsService elevenLabsService;

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, List<Map<String, String>>> body) {
        List<Map<String, String>> messages = body.get("messages");
        String reply = openAiService.chatWithOpenAi(messages);
        Map<String, String> result = new HashMap<>();
        result.put("reply", reply);
        return result;
    }

    @PostMapping(value = "/stt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> stt(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "language", required = false, defaultValue = "ko") String language
    ) throws Exception {
        byte[] bytes = file.getBytes();
        String filename = (file.getOriginalFilename() != null) ? file.getOriginalFilename() : "audio.webm";
        String text = openAiService.transcribe(bytes, filename, file.getContentType(), language);
        Map<String, String> result = new HashMap<>();
        result.put("text", text);
        return result;
    }

    /** ElevenLabs TTS */
    @PostMapping(value = "/tts", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> tts(@RequestBody TtsRequest req) {
        if (!StringUtils.hasText(req.getText())) return ResponseEntity.badRequest().build();
        if (!StringUtils.hasText(req.getVoiceId())) return ResponseEntity.badRequest().build();
        if (!StringUtils.hasText(req.getModelId())) return ResponseEntity.badRequest().build();
        if (!StringUtils.hasText(req.getOutputFormat())) req.setOutputFormat("mp3_44100_128");

        try {
            byte[] audio = elevenLabsService.tts(
                    req.getText(),
                    req.getVoiceId(),
                    req.getModelId(),
                    req.getOutputFormat()
            );

            MediaType contentType = elevenLabsService.resolveContentType(req.getOutputFormat());
            String ext = mapExtFromContentType(contentType);

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"tts." + ext + "\"")
                    .cacheControl(CacheControl.noStore())
                    .body(audio);

        } catch (IllegalStateException e) {
            String msg = e.getMessage() == null ? "TTS upstream error" : e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(msg.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            String msg = "TTS internal error: " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(msg.getBytes(StandardCharsets.UTF_8));
        }
    }
    @PostMapping(value = "/eval", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> eval(@RequestBody Map<String, Object> body) {
        String company = (String) body.getOrDefault("company", "미지정");
        String resumeSummary = (String) body.getOrDefault("resumeSummary", "");
        String transcript = (String) body.getOrDefault("transcript", "");
        // 3회 평가 → 중앙값/평균 집계
        return openAiService.evaluateFinal(company, resumeSummary, transcript, 3);
    }


    /** 보이스 목록 */
    @GetMapping("/voices")
    public ResponseEntity<String> voices() {
        return ResponseEntity.ok(elevenLabsService.listVoicesRaw());
    }

    /** /v1/user */
    @GetMapping("/elevenlabs/whoami")
    public ResponseEntity<String> whoAmI() {
        String body = elevenLabsService.whoAmI();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    /** curl 재현 테스트용 */
    @GetMapping("/elevenlabs/tts-test")
    public ResponseEntity<byte[]> ttsFixedTest() {
        String text = "백엔드 경유 테스트";
        String voiceId = "21m00Tcm4TlvDq8ikWAM";
        String modelId = "eleven_multilingual_v2";
        String outputFormat = "mp3_44100_128";

        try {
            byte[] audio = elevenLabsService.tts(text, voiceId, modelId, outputFormat);

            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("audio/mpeg"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=tts.mp3")
                    .cacheControl(CacheControl.noStore())
                    .body(audio);

        } catch (IllegalStateException e) {
            String msg = e.getMessage() == null ? "TTS upstream error" : e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(msg.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            String msg = "TTS internal error: " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(msg.getBytes(StandardCharsets.UTF_8));
        }
    }

    // ===== helpers =====
    private String mapExtFromContentType(MediaType ct) {
        if (ct == null) return "bin";
        if (MediaType.valueOf("audio/mpeg").includes(ct)) return "mp3";
        if (MediaType.valueOf("audio/wav").includes(ct)) return "wav";
        if (MediaType.valueOf("audio/ogg").includes(ct)) return "ogg";
        if (MediaType.valueOf("audio/webm").includes(ct)) return "webm";
        return "bin";
    }
}