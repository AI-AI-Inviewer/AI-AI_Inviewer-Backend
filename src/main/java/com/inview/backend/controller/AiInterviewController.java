package com.inview.backend.controller;

import com.inview.backend.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class AiInterviewController {

    private final OpenAiService openAiService;

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, List<Map<String, String>>> body) {
        List<Map<String, String>> messages = body.get("messages");
        String reply = openAiService.chatWithOpenAi(messages);
        Map<String, String> result = new HashMap<>();
        result.put("reply", reply);
        return result;
    }
}
