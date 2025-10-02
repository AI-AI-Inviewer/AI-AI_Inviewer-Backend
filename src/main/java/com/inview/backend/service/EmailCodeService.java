// com/inview/backend/service/EmailCodeService.java
package com.inview.backend.service;

import com.inview.backend.entity.EmailVerificationCode;
import com.inview.backend.repository.EmailVerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom rnd = new SecureRandom();

    private String generate6Digits() {
        int n = 100000 + rnd.nextInt(900000);
        return String.valueOf(n);
    }

    @Transactional
    public void sendCode(String email) {
        String code = generate6Digits();
        String hash = encoder.encode(code);
        Instant now = Instant.now();

        EmailVerificationCode entity = EmailVerificationCode.builder()
                .id(UUID.randomUUID().toString())
                .email(email.toLowerCase())
                .codeHash(hash)
                .createdAt(now)
                .expiresAt(now.plusSeconds(10 * 60)) // 10분
                .attempts(0)
                .build();
        repo.save(entity);

        emailService.sendEmailCode(email, code);
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        Instant now = Instant.now();
        var opt = repo.findTopByEmailAndExpiresAtAfterOrderByCreatedAtDesc(email.toLowerCase(), now);
        if (opt.isEmpty()) return false;

        EmailVerificationCode latest = opt.get();
        if (latest.getVerifiedAt() != null) return true; // 이미 검증됨
        if (latest.getAttempts() >= 10) return false;    // 시도 제한

        latest.setAttempts(latest.getAttempts() + 1);
        boolean ok = encoder.matches(code, latest.getCodeHash());
        if (ok) latest.setVerifiedAt(now);
        repo.save(latest);
        return ok;
    }

    public boolean isRecentlyVerified(String email) {
        return repo.findTopByEmailOrderByCreatedAtDesc(email.toLowerCase())
                .map(v -> v.getVerifiedAt() != null && v.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }
}
