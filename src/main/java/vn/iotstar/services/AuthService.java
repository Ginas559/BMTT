// filepath: src/main/java/vn/iotstar/services/AuthService.java
package vn.iotstar.services;

import vn.iotstar.entities.User;
import vn.iotstar.entities.Shop;
import vn.iotstar.repositories.UserRepository;
import vn.iotstar.utils.PasswordUtil;
import vn.iotstar.utils.JwtUtil;

/**
 * Gợi nhớ:
 * - Các hàm đăng ký/đăng nhập giữ nguyên như cũ để không ảnh hưởng nơi khác.
 * - Bổ sung issueAccessToken(User) để phát JWT sau khi login.
 *   + Token chứa: uid, role, shopId (nếu là vendor).
 *   + TTL mặc định 30 phút; có thể chỉnh khi cần.
 */
public class AuthService {

    private final UserRepository userRepo = new UserRepository();
    // Dùng lại StatisticService để tra cứu shop theo owner khi cần nhúng shopId vào token
    private final StatisticService statisticService = new StatisticService();

    /** Đăng ký mặc định (giữ nguyên method cũ để không phá code nơi khác) */
    public User registerNewUser(String firstname, String lastname, String email, String phone, String rawPassword) {
        return registerNewUserWithRole(firstname, lastname, email, phone, rawPassword, User.Role.USER);
    }

    /** Đăng ký theo role (mới thêm) + set INACTIVE + băm mật khẩu (salt + BCrypt) */
    public User registerNewUserWithRole(String firstname, String lastname, String email, String phone,
                                        String rawPassword, User.Role role) {
        if (userRepo.existsByEmail(email)) {
            throw new IllegalStateException("Email đã tồn tại.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có tối thiểu 6 ký tự.");
        }

        // Tạo salt + hash
        String salt = PasswordUtil.generateSalt();
        String hashed = PasswordUtil.hashWithBCrypt(salt + rawPassword);

        User u = new User();
        u.setFirstname(firstname);
        u.setLastname(lastname);
        u.setEmail(email);
        u.setPhone(phone);
        u.setSalt(salt);
        u.setHashedPassword(hashed);

        // set trạng thái & vai trò
        u.setIsEmailActive(false);
        u.setRole(role);

        // các field khác (nếu entity có mặc định thì giữ nguyên)

        userRepo.save(u);
        return u;
    }

    /** Đăng nhập bằng email + mật khẩu (giữ nguyên logic hiện tại) */
    public User login(String email, String rawPassword) {
        User u = userRepo.findByEmail(email);
        if (u == null) throw new IllegalStateException("Email không tồn tại.");
        if (u.getIsEmailActive() == null || !u.getIsEmailActive()) {
            throw new IllegalStateException("Tài khoản chưa kích hoạt email.");
        }
        boolean ok = PasswordUtil.matches(u.getSalt() + rawPassword, u.getHashedPassword());
        if (!ok) throw new IllegalStateException("Mật khẩu không đúng.");
        return u;
    }

    /**
     * JWT: phát access token cho user đã xác thực.
     * - role lấy từ u.getRole()
     * - với vendor: tra shop theo ownerId để gắn shopId vào claim, giúp filter/servlet kiểm soát truy cập theo shop.
     * - TTL mặc định: 30 phút.
     */
    public String issueAccessToken(User u) {
        long ttlMillis = 30 * 60 * 1000L; // 30 phút
        String role = (u.getRole() == null) ? "" : u.getRole().name();

        Long shopId = null;
        if ("VENDOR".equals(role)) {
            Shop s = statisticService.findShopByOwner(u.getId());
            if (s != null) shopId = s.getShopId();
        }

        return JwtUtil.generateAccessToken(u.getId(), role, shopId, ttlMillis);
    }
}
