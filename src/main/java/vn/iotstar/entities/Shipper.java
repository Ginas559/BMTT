package vn.iotstar.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Shipper")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // thêm khóa chính giả vì bảng không có ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = true)
    private ShippingPartner shippingPartner;
    
    
    
    
}

