// com/inview/backend/service/EmailCodeService.java
package com.inview.backend.service;

import com.inview.backend.entity.EmailVerificationCode;
import com.inview.backend.repository.EmailVerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailCodeService {
    private final EmailVerificationCodeRepository repo;
    private final EmailService emailService;
    private final PasswordEncoder encoder; // SecurityConfig의 BCryptPasswordEncoder 빈 주입
    private final SecureRandom rnd = new SecureRandom();

    private String normalizeEmail(String e) {
        return e == null ? "" : e.trim().toLowerCase();
    }
    private String normalizeCode(String c) {
        return c == null ? "" : c.trim().replaceAll("\\s+", "");
    }

    private String generate6Digits() {
        int n = 100000 + rnd.nextInt(900000);
        return String.valueOf(n);
    }

    @Transactional
    public void sendCode(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        String code  = generate6Digits();
        String hash  = encoder.encode(code);
        Instant now  = Instant.now();

        EmailVerificationCode entity = EmailVerificationCode.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .codeHash(hash)
                .createdAt(now)
                .expiresAt(now.plusSeconds(10 * 60)) // 10분
                .attempts(0)
                .build();
        repo.save(entity);

        emailService.sendEmailCode(email, code);
    }

    @Transactional
    public boolean verifyCode(String rawEmail, String rawCode) {
        String email = normalizeEmail(rawEmail);
        String code  = normalizeCode(rawCode);

        Instant now = Instant.now();
        var opt = repo.findTopByEmailIgnoreCaseAndExpiresAtAfterOrderByCreatedAtDesc(email, now);
        if (opt.isEmpty()) return false;                 // 만료/선요청 없음

        EmailVerificationCode latest = opt.get();
        if (latest.getVerifiedAt() != null) return true; // 이미 검증됨
        if (latest.getAttempts() != null && latest.getAttempts() >= 10) return false; // 시도 제한

        latest.setAttempts((latest.getAttempts() == null ? 0 : latest.getAttempts()) + 1);

        boolean ok = encoder.matches(code, latest.getCodeHash());
        if (ok) latest.setVerifiedAt(now);

        repo.save(latest);
        return ok;
    }

    @Transactional(readOnly = true)
    public boolean isRecentlyVerified(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        return repo.findTopByEmailOrderByCreatedAtDesc(email)
                .map(v -> v.getVerifiedAt() != null && v.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }
}
