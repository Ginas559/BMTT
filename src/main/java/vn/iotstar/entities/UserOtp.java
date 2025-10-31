// filepath: src/main/java/vn/iotstar/entities/UserOtp.java
package vn.iotstar.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_otp",
       indexes = {
         @Index(name = "idx_userotp_user", columnList = "user_id"),
         @Index(name = "idx_userotp_purpose", columnList = "purpose"),
         @Index(name = "idx_userotp_exp", columnList = "expires_at")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Chủ sở hữu mã OTP
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_userotp_user"))
    private User user;

    // Mục đích của mã OTP
    public enum Purpose { REGISTER, RESET }
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 20, nullable = false)
    private Purpose purpose;

    // Mã OTP 6 ký tự số
    @Column(name = "code", length = 10, nullable = false)
    private String code;

    // Số lần nhập sai (để throttle)
    @Builder.Default
    @Column(name = "attempts", nullable = false, columnDefinition = "int default 0")
    private Integer attempts = 0;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (attempts == null) attempts = 0;
    }
}
