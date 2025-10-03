// com/inview/backend/service/EmailService.java  (클래스명은 EmailCodeService로 유지)
package com.inview.backend.service;

import com.inview.backend.entity.EmailVerificationCode;
import com.inview.backend.repository.EmailVerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailCodeService {

    private final EmailVerificationCodeRepository repo;
    private final PasswordEncoder passwordEncoder;  // ✅ SecurityConfig의 BCryptPasswordEncoder 주입

    private static final SecureRandom RNG = new SecureRandom();

    public void sendCode(String email) {
        String normEmail = email.trim().toLowerCase();
        String code = make6digits();               // ✅ 6자리 코드 생성
        String codeHash = hash(code);              // ✅ 해시 저장

        var now = LocalDateTime.now();
        var evc = new EmailVerificationCode();
        evc.setId(UUID.randomUUID().toString());
        evc.setEmail(normEmail);
        evc.setCodeHash(codeHash);
        evc.setAttempts(0);
        evc.setCreatedAt(now);
        evc.setExpiresAt(now.plusMinutes(10));

        repo.save(evc);

        // TODO: 실제 메일 전송 로직 (메일 본문에 code 삽입)
        // mailService.send(normEmail, "인증 코드", "인증코드: " + code);

        log.info("[EMAIL-CODE] sent to={}, expiresAt={}, code(6) masked={}",
                normEmail, evc.getExpiresAt(), mask(code));
    }

    public boolean verifyCode(String email, String code) {
        String normEmail = email.trim().toLowerCase();
        var now = LocalDateTime.now();

        var latest = repo.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(normEmail).orElse(null);
        if (latest == null) return false;
        if (latest.getVerifiedAt() != null) return true;                // 이미 인증됨
        if (now.isAfter(latest.getExpiresAt())) return false;           // 만료

        boolean ok = matches(code, latest.getCodeHash());
        if (!ok) {
            // 실패 횟수 증가(널 안전)
            Integer attempts = latest.getAttempts() == null ? 0 : latest.getAttempts();
            latest.setAttempts(attempts + 1);
            repo.save(latest);
            return false;
        }

        latest.setVerifiedAt(now);
        repo.save(latest);
        return true;
    }

    public boolean isRecentlyVerified(String email) {
        String normEmail = email.trim().toLowerCase();
        var latest = repo.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(normEmail).orElse(null);
        return latest != null
                && latest.getVerifiedAt() != null
                && latest.getVerifiedAt().isAfter(LocalDateTime.now().minusHours(24));
    }

    /* ---------- Helpers ---------- */

    // 6자리 숫자코드 생성(000000 ~ 999999)
    private String make6digits() {
        int n = RNG.nextInt(1_000_000);
        return String.format("%06d", n);
    }

    // 해시 및 검증(BCrypt 등)
    private String hash(String raw) {
        return passwordEncoder.encode(raw);
    }

    private boolean matches(String raw, String hashed) {
        return passwordEncoder.matches(raw, hashed);
    }

    private String mask(String code) {
        return "******";
    }
}
