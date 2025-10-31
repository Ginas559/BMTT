// filepath: src/main/java/vn/iotstar/entities/ShoeAttribute.java
package vn.iotstar.entities;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Shoe_Attribute")
public class ShoeAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attr_id")
    private Long attrId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "gender", length = 10)
    private String gender; // Nam / Nữ / Unisex

    @Column(name = "material", length = 100)
    private String material; // Da, vải, PU,...

    @Column(name = "brand", length = 100)
    private String brand; // Nike, Adidas,...

    @Column(name = "style", length = 50)
    private String style; // Sneaker, Boot, Thể thao,...
}
