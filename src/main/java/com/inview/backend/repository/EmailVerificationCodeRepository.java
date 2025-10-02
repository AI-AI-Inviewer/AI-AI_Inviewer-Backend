package com.inview.backend.repository;

import com.inview.backend.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, String> {
    Optional<EmailVerificationCode> findTopByEmailOrderByCreatedAtDesc(String email);
    Optional<EmailVerificationCode> findTopByEmailAndExpiresAtAfterOrderByCreatedAtDesc(String email, Instant now);
}
