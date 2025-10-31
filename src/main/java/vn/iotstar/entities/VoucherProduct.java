// ==============================
// src/main/java/vn/iotstar/entities/VoucherProduct.java
// ==============================

package vn.iotstar.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "[Voucher_Product]")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherProduct {

    @EmbeddedId
    private VoucherProductId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("voucherId")
    @JoinColumn(
        name = "voucher_id", 
        nullable = false, 
        foreignKey = @ForeignKey(name = "FK_VP_Voucher")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(
        name = "product_id", 
        nullable = false, 
        foreignKey = @ForeignKey(name = "FK_VP_Product")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;
}