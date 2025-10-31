// filepath: src/main/java/vn/iotstar/entities/Voucher.java
package vn.iotstar.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "[Voucher]")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_id")
    private Long voucherId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "shop_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_Voucher_Shop")
    )
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private Shop shop;

    @Column(name = "code", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String code;

    // dùng để unique(case-insensitive)
    @Column(name = "code_upper", nullable = false, length = 50)
    private String codeUpper;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private VoucherType type; // PERCENT | AMOUNT

    // ⚠️ map sang percent_value để tránh keyword 'percent'
    @Column(name = "percent_value", precision = 5, scale = 2)
    private BigDecimal percent; // 1..100 (khi type = PERCENT)

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;  // >0 (khi type = AMOUNT)

    @Column(name = "min_order_amount", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // quan hệ N-N qua bảng nối
    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private Set<VoucherProduct> voucherProducts = new HashSet<>();

    public enum VoucherType { PERCENT, AMOUNT }
    public enum Status { ACTIVE, INACTIVE }

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (code != null) codeUpper = code.trim().toUpperCase();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
