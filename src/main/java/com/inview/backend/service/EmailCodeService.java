// com/inview/backend/service/EmailCodeService.java
package com.inview.backend.service;

import com.inview.backend.entity.EmailVerificationCode;
import com.inview.backend.repository.EmailVerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCodeService {

    private final EmailVerificationCodeRepository repo;
    private final EmailService emailService;
    private final PasswordEncoder encoder;
    private final SecureRandom rnd = new SecureRandom();

    private String normalizeEmail(String e) { return e == null ? "" : e.trim().toLowerCase(); }
    private String normalizeCode(String c)  { return c == null ? "" : c.trim().replaceAll("\\s+", ""); }

    private String generate6Digits() {
        int n = 100000 + rnd.nextInt(900000);
        return String.valueOf(n);
    }

    @Transactional
    public void sendCode(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        String code = generate6Digits();
        String hash = encoder.encode(code);
        Instant now = Instant.now();

        EmailVerificationCode entity = EmailVerificationCode.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .codeHash(hash)
                .attempts(0)
                .createdAt(now)
                .expiresAt(now.plusSeconds(10 * 60)) // 10분
                .build();

        repo.save(entity);

        log.info("[EMAIL-CODE] sent to={}, expiresAt={}, code(6) masked=******",
                email, entity.getExpiresAt());
        emailService.sendEmailCode(email, code);
    }

    @Transactional
    public boolean verifyCode(String rawEmail, String rawCode) {
        String email = normalizeEmail(rawEmail);
        String code  = normalizeCode(rawCode);

        var opt = repo.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(email);
        if (opt.isEmpty()) {
            log.warn("[EMAIL-CODE] no record. email={}", email);
            return false;
        }

        var latest = opt.get();

        // 만료 체크 (DB 비교 대신 자바에서)
        if (latest.getExpiresAt() != null && latest.getExpiresAt().isBefore(Instant.now())) {
            log.warn("[EMAIL-CODE] expired. email={}, createdAt={}, expiresAt={}",
                    email, latest.getCreatedAt(), latest.getExpiresAt());
            return false;
        }

        // 이미 인증된 최신 코드면 true
        if (latest.getVerifiedAt() != null) {
            log.info("[EMAIL-CODE] already verified. email={}, verifiedAt={}",
                    email, latest.getVerifiedAt());
            return true;
        }

        // 시도 제한
        Integer attempts = latest.getAttempts() == null ? 0 : latest.getAttempts();
        if (attempts >= 10) {
            log.warn("[EMAIL-CODE] too many attempts. email={}, attempts={}", email, attempts);
            return false;
        }

        latest.setAttempts(attempts + 1);
        boolean ok = encoder.matches(code, latest.getCodeHash());
        if (ok) {
            latest.setVerifiedAt(Instant.now());
            log.info("[EMAIL-CODE] verify OK. email={}, attempts={}", email, latest.getAttempts());
        } else {
            log.warn("[EMAIL-CODE] verify FAIL. email={}, attempts={}", email, latest.getAttempts());
        }
        repo.save(latest);
        return ok;
    }

    @Transactional(readOnly = true)
    public boolean isRecentlyVerified(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        return repo.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(email)
                .map(v -> v.getVerifiedAt() != null && v.getExpiresAt() != null
                        && v.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }
}
