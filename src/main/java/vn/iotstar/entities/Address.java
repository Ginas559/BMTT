package vn.iotstar.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "[Address]")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_Address_User")
    )
    private User user;

    @Nationalized
    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Nationalized
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    // ✅ Đặt kiểu NVARCHAR để hiển thị tiếng Việt đúng
    @Nationalized
    @Column(name = "address_detail", nullable = false, length = 255)
    private String addressDetail;

    @Builder.Default
    @Column(name = "is_default")
    private Boolean isDefault = false;
}
