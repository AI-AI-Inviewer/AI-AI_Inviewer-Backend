package com.inview.backend.controller;

import com.inview.backend.config.JwtTokenProvider;
import com.inview.backend.entity.User;
import com.inview.backend.repository.UserRepository;
import com.inview.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
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

    /** 로그인: 아이디/비번 검증 → JWT 발급 → 환경별 쿠키 옵션으로 Set-Cookie + JSON 반환 */
    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> login(@RequestBody Map<String, String> req, HttpServletRequest request) {
        String userId = req.get("userId");
        String password = req.getOrDefault("password", req.get("userPassword"));

        if (userId == null || userId.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "userId와 password는 필수입니다."));
        }

        User user = userService.authenticate(userId, password);
        String jwt = jwtTokenProvider.createToken(user.getUserId());

        boolean isProd = request.getServerName() != null &&
                request.getServerName().endsWith("aiinviewer.co.kr");

        ResponseCookie.ResponseCookieBuilder cb = ResponseCookie.from("ACCESS_TOKEN", jwt)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(7));

        if (isProd) {
            cb.secure(true).sameSite("None").domain(".aiinviewer.co.kr");
        } else {
            // 로컬 개발환경: http라 secure=false, domain 미지정(호스트 한정 쿠키), sameSite=Lax
            cb.secure(false).sameSite("Lax");
        }

        ResponseCookie jwtCookie = cb.build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(Map.of(
                        "ok", true,
                        "token", jwt,                 // 선택: 필요 없으면 제거 가능
                        "userId", user.getUserId(),
                        "userName", user.getUserName()
                ));
    }

    /** 로그아웃: 쿠키 제거 */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        boolean isProd = request.getServerName() != null &&
                request.getServerName().endsWith("aiinviewer.co.kr");

        ResponseCookie.ResponseCookieBuilder cb = ResponseCookie.from("ACCESS_TOKEN", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0);

        if (isProd) {
            cb.secure(true).sameSite("None").domain(".aiinviewer.co.kr");
        } else {
            cb.secure(false).sameSite("Lax");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cb.build().toString())
                .body(Map.of("ok", true));
    }

    /** 인증 확인 */
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
