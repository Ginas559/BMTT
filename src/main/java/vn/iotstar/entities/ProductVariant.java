// filepath: src/main/java/vn/iotstar/entities/ProductVariant.java
package vn.iotstar.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Product_Variant")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "size", length = 10, nullable = false)
    private String size;

    @Column(name = "color", length = 50, nullable = false)
    private String color;

    @Column(name = "stock")
    private Integer stock = 0;

    @Column(name = "sku", length = 50)
    private String sku; // optional: mã nội bộ, nếu cần

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "price_adjustment", precision = 10, scale = 2)
    private BigDecimal priceAdjustment; // nếu size/màu có giá khác
}
