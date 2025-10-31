package vn.iotstar.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "[User_Verification]")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long verificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "FK_UserVerification_User"))
    private User user;

    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;

    @Column(name = "expire_time", nullable = false)
    private LocalDateTime expireTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private VerificationType type;

    public enum VerificationType {
        REGISTER,
        FORGOT_PASSWORD
    }
}

