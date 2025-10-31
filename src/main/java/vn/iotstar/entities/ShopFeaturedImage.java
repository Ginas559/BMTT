// src/main/java/vn/iotstar/entities/ShopFeaturedImage.java
package vn.iotstar.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shop_featured_images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShopFeaturedImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();
}
