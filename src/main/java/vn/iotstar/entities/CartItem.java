package vn.iotstar.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Cart_Item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "cart_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_CartItem_Cart")
    )
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_CartItem_Product")
    )
    private Product product;

    @Column(nullable = false)
    private Integer quantity;
}

