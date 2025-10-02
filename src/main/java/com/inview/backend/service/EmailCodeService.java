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
@RequiredArgsConstructor
@Service
public class EmailCodeService {

    private final EmailVerificationCodeRepository repo;
    private final EmailService emailService;
    private final PasswordEncoder encoder;     // SecurityConfig의 BCryptPasswordEncoder 빈 사용
    private final SecureRandom rnd = new SecureRandom();

    private static String normalizeEmail(String e) {
        return e == null ? "" : e.trim().toLowerCase();
    }
    private static String normalizeCode(String c) {
        return c == null ? "" : c.replaceAll("\\s+", "").trim();
    }

    private String generate6Digits() {
        return String.valueOf(100000 + rnd.nextInt(900000));
    }

    @Transactional
    public void sendCode(String rawEmail) {
        final String email = normalizeEmail(rawEmail);
        final String code  = generate6Digits();
        final String hash  = encoder.encode(code);
        final Instant now  = Instant.now();

        EmailVerificationCode entity = EmailVerificationCode.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .codeHash(hash)
                .createdAt(now)
                .expiresAt(now.plusSeconds(10 * 60)) // 10분 유효
                .attempts(0)
                .build();
        repo.save(entity);

        // 메일 발송(본문에는 원본 6자리)
        emailService.sendEmailCode(email, code);
        log.info("[EMAIL-CODE] sent to={}, expiresAt={}, code(6) masked=******", email, entity.getExpiresAt());
    }

    @Transactional
    public boolean verifyCode(String rawEmail, String rawCode) {
        final String email = normalizeEmail(rawEmail);
        final String code  = normalizeCode(rawCode);
        final Instant now  = Instant.now();

        var opt = repo.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(email);
        if (opt.isEmpty()) {
            log.warn("[EMAIL-CODE] no record for email={}", email);
            return false;
        }

        EmailVerificationCode latest = opt.get();

        // DB에서가 아니라 자바에서 만료 판단(타임존/드라이버 이슈 회피)
        if (latest.getExpiresAt() == null || !latest.getExpiresAt().isAfter(now)) {
            log.warn("[EMAIL-CODE] expired. email={}, createdAt={}, expiresAt={}, now={}",
                    email, latest.getCreatedAt(), latest.getExpiresAt(), now);
            return false;
        }

        // 이미 인증된 코드면 OK
        if (latest.getVerifiedAt() != null) {
            log.info("[EMAIL-CODE] already verified. email={}", email);
            return true;
        }

        // 시도 제한
        Integer attempts = latest.getAttempts() == null ? 0 : latest.getAttempts();
        if (attempts >= 10) {
            log.warn("[EMAIL-CODE] attempts exceeded. email={}", email);
            return false;
        }

        latest.setAttempts(attempts + 1);

        boolean ok = encoder.matches(code, latest.getCodeHash());
        if (ok) {
            latest.setVerifiedAt(now);
            log.info("[EMAIL-CODE] verified OK. email={}", email);
        } else {
            log.warn("[EMAIL-CODE] wrong code. email={}", email);
        }

        repo.save(latest);
        return ok;
    }

    @Transactional(readOnly = true)
    public boolean isRecentlyVerified(String rawEmail) {
        final String email = normalizeEmail(rawEmail);
        final Instant now  = Instant.now();

        return repo.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(email)
                .map(v -> v.getVerifiedAt() != null && v.getExpiresAt() != null && v.getExpiresAt().isAfter(now))
                .orElse(false);
    }
}
