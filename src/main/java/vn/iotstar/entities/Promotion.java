package vn.iotstar.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import java.time.format.DateTimeFormatter; // Phải import

@Entity
@Table(name = "Promotion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Long promotionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "apply_to", length = 20)
    private String applyTo; // 'PRODUCT' hoặc 'SHIPPING'
    
    private PromotionStatus status = PromotionStatus.ACTIVE;
    
    public enum PromotionStatus{ACTIVE, INACTIVE};
    
    @Transient // Không map vào database
    public String getFormattedStartDate() {
        if (this.startDate != null) {
            return this.startDate.format(DateTimeFormatter.ISO_DATE); // Định dạng yyyy-MM-dd
        }
        return "";
    }

    @Transient // Không map vào database
    public String getFormattedEndDate() {
        if (this.endDate != null) {
            return this.endDate.format(DateTimeFormatter.ISO_DATE); // Định dạng yyyy-MM-dd
        }
        return "";
    }
}

