package com.inview.backend.controller;

import com.inview.backend.config.JwtTokenProvider;
import com.inview.backend.entity.User;
import com.inview.backend.repository.UserRepository;
import com.inview.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User req) {
        User created = userService.register(req);
        return ResponseEntity.ok(Map.of("userNum", created.getUserNum()));
    }

    // 프런트가 { userId, userPassword }로 보내도 동작하게 하고,
    // 응답은 '토큰 문자열'을 그대로 반환 (response.data 에 바로 저장됨)
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> login(@RequestBody Map<String, String> req) {
        String userId = req.get("userId");

        // password 또는 userPassword 둘 다 허용
        String password = req.get("password");
        if (password == null) password = req.get("userPassword");

        if (userId == null || userId.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("userId와 password는 필수입니다.");
        }

        User user = userService.authenticate(userId, password);
        String token = jwtTokenProvider.createToken(user.getUserId());

        // 순수 문자열로 반환 → 프론트의 response.data 가 바로 토큰이 됨
        return ResponseEntity.ok(token);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).build();
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(Map.of(
                "userId", user.getUserId(),
                "userEmail", user.getUserEmail(),
                "userName", user.getUserName(),
                "userNickname", user.getUserNickname(),
                "userNum", user.getUserNum()
        ));
    }

    @GetMapping("/check-id")
    public ResponseEntity<Map<String, Boolean>> checkUserId(@RequestParam String userId) {
        boolean available = !userRepository.existsByUserId(userId);
        return ResponseEntity.ok(Map.of("available", available));
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestParam String nickname) {
        boolean available = !userRepository.existsByUserNickname(nickname);
        return ResponseEntity.ok(Map.of("available", available));
    }
}
