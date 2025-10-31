package vn.iotstar.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteItem {
    private Long productId;
    private String productName;
    private BigDecimal price;          // Giá gốc
    private BigDecimal discountPrice;  // Giá sau giảm (có thể null)
    private String imageUrl;           // URL ảnh thumbnail (có thể rỗng)
}
