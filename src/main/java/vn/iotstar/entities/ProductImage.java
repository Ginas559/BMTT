package vn.iotstar.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "[Product_Image]")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_ProductImage_Product")
    )
    private Product product;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
    
    @Builder.Default
    @Column(name = "is_thumbnail")
    private Boolean isThumbnail = false;
}

