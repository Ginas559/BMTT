// filepath: src/main/java/vn/iotstar/controllers/auth/VerifyOtpServlet.java
package vn.iotstar.controllers.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.iotstar.services.OtpService;

import java.io.IOException;

@WebServlet(urlPatterns = {"/verify", "/otp/resend"})
public class VerifyOtpServlet extends HttpServlet {

    private OtpService otpService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.otpService = new OtpService();
    }

    private String resetFlag(String email) {
        return "RESET_READY:" + email;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/auth/verify.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();
        String purpose = req.getParameter("purpose"); // "register" | "reset"
        String email = req.getParameter("email");

        if ("/otp/resend".equals(path)) {
            boolean ok;
            if ("reset".equalsIgnoreCase(purpose)) {
                ok = otpService.resendResetOtp(email);
                req.getSession().setAttribute("flash", ok ? "Đã gửi lại OTP đặt lại mật khẩu." : "Không gửi được OTP. Vui lòng thử lại.");
                resp.sendRedirect(req.getContextPath() + "/verify?purpose=reset&email=" + email);
            } else {
                ok = otpService.resendRegisterOtp(email);
                req.getSession().setAttribute("flash", ok ? "Đã gửi lại OTP kích hoạt." : "Không gửi được OTP. Vui lòng thử lại.");
                resp.sendRedirect(req.getContextPath() + "/verify?email=" + email);
            }
            return;
        }

        // POST /verify
        String code = req.getParameter("code");

        if ("reset".equalsIgnoreCase(purpose)) {
            boolean ok = otpService.verifyResetOtp(email, code);
            if (ok) {
                req.getSession().setAttribute(resetFlag(email), Boolean.TRUE);
                req.getSession().setAttribute("flash", "Mã OTP hợp lệ. Vui lòng đặt mật khẩu mới.");
                resp.sendRedirect(req.getContextPath() + "/reset-password?email=" + email);
            } else {
                req.getSession().setAttribute("flash", "Mã OTP không đúng hoặc đã hết hạn.");
                resp.sendRedirect(req.getContextPath() + "/verify?purpose=reset&email=" + email);
            }
        } else {
            boolean ok = otpService.verifyActivateOtp(email, code);
            if (ok) {
                req.getSession().setAttribute("flash", "Kích hoạt tài khoản thành công. Vui lòng đăng nhập.");
                resp.sendRedirect(req.getContextPath() + "/login");
            } else {
                req.getSession().setAttribute("flash", "Mã OTP không đúng hoặc đã hết hạn.");
                resp.sendRedirect(req.getContextPath() + "/verify?email=" + email);
            }
        }
    }
}
