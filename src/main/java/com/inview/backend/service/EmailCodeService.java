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

@RequiredArgsConstructor
@Service
@Slf4j
public class EmailCodeService {
    private final EmailVerificationCodeRepository repo;
    private final EmailService emailService;
    private final PasswordEncoder encoder;          // SecurityConfig의 BCryptPasswordEncoder 빈 주입
    private final SecureRandom rnd = new SecureRandom();

    private String normalizeEmail(String e){
        return e == null ? "" : e.trim().toLowerCase();
    }
    private String normalizeCode(String c){
        // 붙여넣기 시 포함될 수 있는 공백/개행 싹 제거
        return c == null ? "" : c.replaceAll("\\s+","").trim();
    }
    private String generate6Digits() {
        // 선행 0 허용(메일 본문과 UI는 6자리 고정)
        return String.format("%06d", rnd.nextInt(1_000_000));
    }

    /** 인증 코드 발송 */
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
                .expiresAt(now.plusSeconds(10 * 60))   // 10분
                .attempts(0)
                .build();

        repo.save(entity);
        emailService.sendEmailCode(email, code);

        log.info("[EMAIL-CODE] sent to={}, expiresAt={}, code(6) masked=******", email, entity.getExpiresAt());
    }

    /** 인증 코드 검증 */
    @Transactional
    public boolean verifyCode(String rawEmail, String rawCode) {
        String email = normalizeEmail(rawEmail);
        String code  = normalizeCode(rawCode);

        var opt = repo.findTopByEmailIgnoreCaseAndExpiresAtAfterOrderByCreatedAtDesc(email, Instant.now());
        if (opt.isEmpty()) {
            log.warn("[EMAIL-CODE] no valid record. email={}", email);
            return false;
        }

        var latest = opt.get();
        if (latest.getVerifiedAt() != null) {
            log.info("[EMAIL-CODE] already verified. email={}", email);
            return true; // 이미 검증됨
        }

        if (latest.getAttempts() != null && latest.getAttempts() >= 10) {
            log.warn("[EMAIL-CODE] too many attempts. email={}", email);
            return false;
        }

        latest.bumpAttempts();
        boolean ok = encoder.matches(code, latest.getCodeHash());
        if (ok) latest.setVerifiedAt(Instant.now());
        repo.save(latest);

        log.info("[EMAIL-CODE] verify result={} email={} attempts={}", ok, email, latest.getAttempts());
        return ok;
    }

    /** 최근 발송분이 검증된 상태인지(만료 전) */
    @Transactional(readOnly = true)
    public boolean isRecentlyVerified(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        return repo.findTopByEmailOrderByCreatedAtDesc(email)
                .map(v -> v.getVerifiedAt() != null && v.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }
}
