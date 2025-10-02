package com.inview.backend.controller;

import com.inview.backend.config.JwtTokenProvider;
import com.inview.backend.entity.User;
import com.inview.backend.repository.UserRepository;
import com.inview.backend.service.EmailCodeService;
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
    private final EmailCodeService emailCodeService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User req) {
        // 이메일 중복
        if (userRepository.existsByUserEmail(req.getUserEmail())) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "이미 사용 중인 이메일입니다."));
        }
        // 이메일 인증 여부 확인
        if (!emailCodeService.isRecentlyVerified(req.getUserEmail())) {
            return ResponseEntity.status(403).body(Map.of("ok", false, "message", "이메일 인증이 필요합니다."));
        }
        User created = userService.register(req); // 내부에서 비번해시/중복체크 등
        // 선택: created.setEmailVerified('Y'); // User 엔티티에 필드가 있다면
        return ResponseEntity.ok(Map.of("ok", true, "userNum", created.getUserNum()));
    }
    /** 이메일 인증코드 발송 */
    @PostMapping("/email-code/send")
    public ResponseEntity<?> sendEmailCode(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "이메일이 필요합니다."));
        }
        if (userRepository.existsByUserEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "이미 가입된 이메일입니다."));
        }
        emailCodeService.sendCode(email);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /** 이메일 인증코드 검증 */
    @PostMapping("/email-code/verify")
    public ResponseEntity<?> verifyEmailCode(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String code = req.get("code");
        if (email == null || code == null || email.isBlank() || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "email과 code는 필수입니다."));
        }
        boolean ok = emailCodeService.verifyCode(email, code);
        if (!ok) return ResponseEntity.status(400).body(Map.of("ok", false, "message", "인증 코드가 유효하지 않습니다."));
        return ResponseEntity.ok(Map.of("ok", true));
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