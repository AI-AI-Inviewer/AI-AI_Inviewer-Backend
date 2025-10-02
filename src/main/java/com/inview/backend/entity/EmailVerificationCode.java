// com/inview/backend/entity/EmailVerificationCode.java
package com.inview.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "EMAIL_VERIFICATION_CODE")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailVerificationCode {

    @Id
    @Column(name = "ID", length = 36)
    private String id;

    @Column(name = "EMAIL", length = 320, nullable = false)
    private String email;

    @Column(name = "CODE_HASH", length = 120, nullable = false)
    private String codeHash;

    @Column(name = "EXPIRES_AT", nullable = false)
    private Instant expiresAt;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @Column(name = "VERIFIED_AT")
    private Instant verifiedAt;

    @Column(name = "ATTEMPTS", nullable = false)
    private Integer attempts;
}
