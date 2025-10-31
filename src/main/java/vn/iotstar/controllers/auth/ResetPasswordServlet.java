// filepath: src/main/java/vn/iotstar/controllers/auth/ResetPasswordServlet.java
package vn.iotstar.controllers.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.iotstar.entities.User;
import vn.iotstar.repositories.UserRepository;
import vn.iotstar.repositories.UserOtpRepository;
import vn.iotstar.entities.UserOtp;
import vn.iotstar.utils.PasswordUtil;

import java.io.IOException;

@WebServlet(urlPatterns = {"/reset-password"})
public class ResetPasswordServlet extends HttpServlet {

    private UserRepository userRepo;
    private UserOtpRepository otpRepo;

    @Override
    public void init() throws ServletException {
        super.init();
        this.userRepo = new UserRepository();
        this.otpRepo = new UserOtpRepository();
    }

    private String flagKey(String email) {
        return "RESET_READY:" + email;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        if (email == null || req.getSession().getAttribute(flagKey(email)) == null) {
            req.getSession().setAttribute("flash", "Phiên đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            resp.sendRedirect(req.getContextPath() + "/forgot-password");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirm  = req.getParameter("confirm");

        if (email == null || req.getSession().getAttribute(flagKey(email)) == null) {
            req.getSession().setAttribute("flash", "Phiên đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            resp.sendRedirect(req.getContextPath() + "/forgot-password");
            return;
        }

        if (password == null || confirm == null || !password.equals(confirm) || password.length() < 6) {
            req.setAttribute("error", "Mật khẩu mới không hợp lệ (tối thiểu 6 ký tự) hoặc không khớp.");
            req.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp").forward(req, resp);
            return;
        }

        User user = userRepo.findByEmail(email);
        if (user == null) {
            req.getSession().removeAttribute(flagKey(email));
            req.getSession().setAttribute("flash", "Tài khoản không tồn tại.");
            resp.sendRedirect(req.getContextPath() + "/forgot-password");
            return;
        }

        // Hash mật khẩu mới
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashWithBCrypt(salt + password);
        user.setSalt(salt);
        user.setHashedPassword(hash);
        userRepo.update(user);

        // Dọn toàn bộ OTP RESET còn lại (nếu có)
        otpRepo.deleteByUserAndPurpose(user, UserOtp.Purpose.RESET);

        // Xoá session flag
        req.getSession().removeAttribute(flagKey(email));

        req.getSession().setAttribute("flash", "Đặt lại mật khẩu thành công. Vui lòng đăng nhập.");
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
