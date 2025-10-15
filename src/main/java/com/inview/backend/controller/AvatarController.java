package com.inview.backend.controller;

import com.inview.backend.service.HeygenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private final HeygenService heygenService;

    /** 프런트에서 사용하는 토큰 엔드포인트
     *  응답 예: { "token": "...", "expires_at": "..." }
     */
    @GetMapping(value = "/token", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> token() {
        return heygenService.createStreamingTokenRaw();
    }
}
