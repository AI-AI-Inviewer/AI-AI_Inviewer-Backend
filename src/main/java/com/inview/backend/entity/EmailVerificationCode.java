// com/inview/backend/entity/EmailVerificationCode.java
package com.inview.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification_code")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EmailVerificationCode {
    @Id
    @Column(name="ID", length=36, nullable=false)
    private String id;

    @Column(name="EMAIL", length=254, nullable=false)
    private String email;

    @Column(name="CODE_HASH", length=100, nullable=false)
    private String codeHash;

    @Column(name="ATTEMPTS", nullable=false)
    private Integer attempts;

    @Column(name="CREATED_AT", nullable=false)
    private LocalDateTime createdAt;   // ✅

    @Column(name="EXPIRES_AT", nullable=false)
    private LocalDateTime expiresAt;   // ✅

    @Column(name="VERIFIED_AT")
    private LocalDateTime verifiedAt;  // ✅

    // getters/setters ...
}
