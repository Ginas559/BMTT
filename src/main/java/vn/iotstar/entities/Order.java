package vn.iotstar.entities;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "[Order]")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    // Người đặt hàng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_Order_User")
    )
    private User user;

    // Cửa hàng nhận đơn
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "shop_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_Order_Shop")
    )
    private Shop shop;

    // Địa chỉ giao hàng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "address_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_Order_Address")
    )
    private Address address;

    // Người giao hàng (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "shipper_id",
        foreignKey = @ForeignKey(name = "FK_Order_Shipper")
    )
    private User shipper;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private OrderStatus status = OrderStatus.NEW;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // -------------------------------
    // ENUM cho Payment Method & Status
    // -------------------------------

    public enum PaymentMethod {
        COD,
        VNPAY,
        MOMO
    }

    public enum OrderStatus {
        NEW,
        CONFIRMED,
        SHIPPING,
        DELIVERED,
        CANCELED,
        RETURNED
    }
}

