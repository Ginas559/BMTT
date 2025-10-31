// filepath: src/main/java/vn/iotstar/controllers/auth/RegisterServlet.java
package vn.iotstar.controllers.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.iotstar.entities.User;
import vn.iotstar.services.AuthService;
import vn.iotstar.services.OtpService;

import java.io.IOException;

@WebServlet(urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private AuthService authService;
    private OtpService otpService;

    // Mã bí mật cho ADMIN/VENDOR/SHIPPER
    private static final String SECRET_CODE = "buithanhtung123";

    @Override
    public void init() throws ServletException {
        super.init();
        this.authService = new AuthService();
        this.otpService  = new OtpService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("pageTitle", "Đăng ký tài khoản");
        req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
    }

    private User.Role parseRole(String roleParam) {
        if (roleParam == null || roleParam.isBlank()) return User.Role.USER;
        String v = roleParam.trim().toUpperCase();
        // Lưu ý: enum trong User là { USER, ADMIN, VENDOR, ShIPPER } (ShIPPER viết hoa/lẫn thường)
        return switch (v) {
            case "ADMIN" -> User.Role.ADMIN;
            case "VENDOR" -> User.Role.VENDOR;
            case "SHIPPER" -> User.Role.SHIPPER; // tên enum đúng theo entity hiện có
            default -> User.Role.USER;
        };
    }

    private void requireSecretIfNeeded(User.Role role, String secret) {
        if (role == User.Role.USER) return; // USER không cần
        if (secret == null || !SECRET_CODE.equals(secret)) {
            throw new IllegalArgumentException("Mã bí mật không hợp lệ cho vai trò đã chọn.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String firstname = req.getParameter("firstname");
        String lastname  = req.getParameter("lastname");
        String email     = req.getParameter("email");
        String phone     = req.getParameter("phone");
        String password  = req.getParameter("password");
        String confirm   = req.getParameter("confirm");
        String roleParam = req.getParameter("role");
        String secret    = req.getParameter("secret");

        try {
            if (firstname == null || firstname.isBlank()
             || lastname == null  || lastname.isBlank()
             || email == null     || email.isBlank()
             || phone == null     || phone.isBlank()
             || password == null  || password.isBlank()
             || confirm == null   || confirm.isBlank()) {
                throw new IllegalArgumentException("Thiếu thông tin cần thiết.");
            }
            if (!password.equals(confirm)) {
                throw new IllegalArgumentException("Mật khẩu nhập lại không khớp.");
            }

            User.Role role = parseRole(roleParam);
            // ADMIN/VENDOR/SHIPPER bắt buộc mã bí mật
            requireSecretIfNeeded(role, secret);

            // Đăng ký theo role (đã set isEmailActive=false, băm mật khẩu)
            authService.registerNewUserWithRole(firstname, lastname, email, phone, password, role);

            // Gửi OTP kích hoạt
            boolean sent = otpService.sendRegisterOtp(email);

            // Flash message và điều hướng sang trang verify.jsp
            String msg = sent
                    ? "Mã OTP đã được gửi đến email của bạn."
                    : "Không gửi được email. Vui lòng thử 'Gửi lại OTP' hoặc kiểm tra cấu hình email.";
            req.getSession().setAttribute("flash", msg);
            resp.sendRedirect(req.getContextPath() + "/verify?email=" + email);

        } catch (Exception ex) {
            ex.printStackTrace();
            req.setAttribute("error", ex.getMessage() != null ? ex.getMessage() : "Có lỗi xảy ra, vui lòng thử lại.");
            req.setAttribute("pageTitle", "Đăng ký tài khoản");
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
        }
    }
}
