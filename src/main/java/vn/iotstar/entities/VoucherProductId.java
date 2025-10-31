// filepath: src/main/java/vn/iotstar/entities/VoucherProductId.java

package vn.iotstar.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherProductId implements Serializable {

    // Khóa ngoại tham chiếu đến Voucher
    private Long voucherId;

    // Khóa ngoại tham chiếu đến Product
    private Long productId;
}