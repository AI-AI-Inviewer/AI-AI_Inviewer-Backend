package com.inview.backend.repository;

import com.inview.backend.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, String> {
    Optional<EmailVerificationCode> findTopByEmailIgnoreCaseOrderByCreatedAtDesc(String email);
}
