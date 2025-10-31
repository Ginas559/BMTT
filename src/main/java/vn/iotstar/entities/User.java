// filepath: src/main/java/vn/iotstar/entities/User.java
package vn.iotstar.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * User entity (18+ fields) cho AUTH + SHOP:
 * - Unique: email, phone.
 * - slug và id_card KHÔNG unique.
 * - Tự gen slug + timestamps bằng @PrePersist/@PreUpdate.
 * - BỔ SUNG: staffShop (mỗi user chỉ làm cho 1 shop) để phục vụ UC_020.
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email",  columnNames = "email"),
        @UniqueConstraint(name = "uk_users_phone",  columnNames = "phone")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 32)
    @Column(nullable = false, length = 32, columnDefinition = "nvarchar(50)")
    private String firstname;

    @NotBlank @Size(max = 32)
    @Column(nullable = false, length = 32, columnDefinition = "nvarchar(50)")
    private String lastname;

    // KHÔNG unique nữa
    @NotBlank
    @Size(max = 70)
    @Column(nullable = false, length = 70)
    private String slug;

    // KHÔNG unique nữa (allow duplicates / nullable)
    @Size(max = 20)
    @Column(name = "id_card", length = 20)
    private String idCard;

    @Email @NotBlank @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String email;

    @NotBlank @Size(max = 15)
    @Column(nullable = false, length = 15)
    private String phone;

    @Column(nullable = false, columnDefinition = "bit default 0")
    private Boolean isEmailActive = false;

    @Column(nullable = false, columnDefinition = "bit default 0")
    private Boolean isPhoneActive = false;

    @NotBlank
    @Column(nullable = false)
    private String salt;

    @NotBlank
    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    public enum Role { USER, ADMIN, VENDOR, SHIPPER }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, columnDefinition = "varchar(10) default 'USER'")
    private Role role = Role.USER;

    @Size(max = 2000)
    @Column(length = 2000)
    private String addresses = "[]";

    @Size(max = 255)
    @Column(length = 255)
    private String avatar;

    @Size(max = 255)
    @Column(length = 255)
    private String cover;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer point = 0;

    @Column(name = "e_wallet", precision = 18, scale = 2, nullable = false, columnDefinition = "decimal(18,2) default 0")
    private BigDecimal eWallet = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private Boolean isBanned = false;

    // ================== BỔ SUNG CHO UC_020: STAFF CỦA SHOP ==================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_shop_id",
            foreignKey = @ForeignKey(name = "fk_users_staff_shop"))
    private Shop staffShop; // null = không là staff shop nào; != null = staff của shop đó
    // =======================================================================

    @PrePersist
    private void prePersist() {
        if (slug == null || slug.isBlank()) {
            this.slug = toSlug(firstname + " " + lastname);
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (email != null) this.email = email.trim().toLowerCase(Locale.ROOT);
        if (phone != null) this.phone = phone.trim();
    }

    @PreUpdate
    private void preUpdate() {
        if (slug == null || slug.isBlank()) {
            this.slug = toSlug(firstname + " " + lastname);
        }
        this.updatedAt = LocalDateTime.now();
        if (email != null) this.email = email.trim().toLowerCase(Locale.ROOT);
        if (phone != null) this.phone = phone.trim();
    }

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    private static String toSlug(String input) {
        if (input == null) return "";
        String noWhite = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(noWhite, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized.replaceAll("\\p{M}", "")).replaceAll("");
        slug = slug.replaceAll("[-]{2,}", "-");
        return slug.toLowerCase(Locale.ROOT);
    }

    // ===== tiện dùng cho JSP/Service =====
    @Transient
    public String getFullname() {
        String f = firstname == null ? "" : firstname.trim();
        String l = lastname == null ? "" : lastname.trim();
        return (f + " " + l).trim();
    }

    @Transient
    public boolean isStaff() { return staffShop != null; }
}
