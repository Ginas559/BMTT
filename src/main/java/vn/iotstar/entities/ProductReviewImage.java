package vn.iotstar.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "[Product_Review_Image]")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // bigint identity
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "review_id",            // bigint NOT NULL
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_ProductReviewImage_ProductReview")
    )
    private ProductReview review;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
