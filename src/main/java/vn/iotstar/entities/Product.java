package vn.iotstar.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "[Product]")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    // ✅ Shop liên kết (giữ nguyên để hiển thị shopName)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "shop_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_Product_Shop")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Shop shop;

    // ✅ Danh mục liên kết
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "category_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_Product_Category")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;

    // ✅ Lưu tên sản phẩm bằng NVARCHAR để không lỗi tiếng Việt
    @Column(name = "product_name", nullable = false, length = 200, columnDefinition = "NVARCHAR(200)")
    private String productName;

    // ✅ Mô tả dài (NVARCHAR(MAX))
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_price", precision = 18, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "stock", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer stock = 0;

    @Column(name = "rating_avg", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal ratingAvg = BigDecimal.valueOf(0.0);

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    

    public enum ProductStatus {
        ACTIVE,
        INACTIVE
    }
    
    @Builder.Default
    private Boolean isBanned = false;
}
