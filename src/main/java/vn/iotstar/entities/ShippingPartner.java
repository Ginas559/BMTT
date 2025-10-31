package vn.iotstar.entities;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Shipping_Partner")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "partner_id")
    private Long partnerId;

    @Column(name = "partner_name", nullable = false, length = 100)
    private String partnerName;

    @Column(name = "shipping_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingFee;
}
