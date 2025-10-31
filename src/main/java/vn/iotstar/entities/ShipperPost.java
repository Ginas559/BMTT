package vn.iotstar.entities;

import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Data;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "Shipper_Post")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipperPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    // Người đăng bài (Shipper/User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id", nullable = false)
    private User shipper; // Giả định Shipper là một loại User

    // Tiêu đề/Loại bài đăng (Ví dụ: "Cảnh báo kẹt xe Quận 1", "Kinh nghiệm giao hàng")
    @Column(name = "title", nullable = false, length = 255, columnDefinition = "NVARCHAR(255)")
    private String title;

    // Nội dung chi tiết của bài đăng
    @Column(name = "content", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
 // Loại bài đăng (Ví dụ: Cảnh báo, Kinh nghiệm, Hỏi đáp) - Tùy chọn
    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", length = 50)
    private PostType postType;
    
    public enum PostType {
        TRAFFIC_ALERT, // Cảnh báo giao thông
        EXPERIENCE,    // Kinh nghiệm
        DISCUSSION     // Thảo luận chung
    }
    
 // Trong ShipperPost (hoặc ShipperPostDTO)
    @Transient // Đảm bảo trường này không được lưu vào DB
    private String formattedTime; 

    // Thêm getter cho trường này
    public String getFormattedTime() {
        // Sử dụng Java Time API để định dạng
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        return this.createdAt.format(formatter);
    }

}
