package vn.iotstar.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "[Category]")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;
    
//    @Column(nullable = true)
//    private String image;

    // ✅ Dùng NVARCHAR để lưu tiếng Việt chuẩn
    @Column(name = "category_name", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String categoryName;

    @Column(name = "description", length = 500, columnDefinition = "NVARCHAR(500)")
    private String description;
    
    @Builder.Default
    private Boolean isBanned = false;
}
