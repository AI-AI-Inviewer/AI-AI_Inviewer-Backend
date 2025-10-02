// com/inview/backend/entity/EmailVerificationCode.java
package com.inview.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "email_verification_code")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EmailVerificationCode {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(length = 254, nullable = false)
    private String email;               // 항상 소문자로 저장

    @Column(name = "code_hash", length = 100, nullable = false)
    private String codeHash;            // Bcrypt(60) 충분히 커야 함

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column
    private Integer attempts;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    public void bumpAttempts() {
        this.attempts = (this.attempts == null ? 0 : this.attempts) + 1;
    }
}
