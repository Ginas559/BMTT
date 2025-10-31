// filepath: src/main/java/vn/iotstar/controllers/auth/ForgotPasswordServlet.java
package vn.iotstar.controllers.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.iotstar.services.OtpService;

import java.io.IOException;

@WebServlet(urlPatterns = {"/forgot-password"})
public class ForgotPasswordServlet extends HttpServlet {

    private OtpService otpService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.otpService = new OtpService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/auth/forgot.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = req.getParameter("email");
        // Luôn trả lời “đã gửi” để tránh lộ thông tin tài khoản tồn tại hay không
        try {
            otpService.sendResetOtp(email);
        } catch (Exception ignore) { /* không lộ thông tin */ }

        req.getSession().setAttribute("flash", "Nếu email tồn tại, chúng tôi đã gửi OTP đặt lại mật khẩu.");
        resp.sendRedirect(req.getContextPath() + "/verify?purpose=reset&email=" + (email == null ? "" : email));
    }
}
