// filepath: src/main/java/vn/iotstar/controllers/auth/LogoutServlet.java
package vn.iotstar.controllers.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Hủy session cũ (nếu đang dùng cho staff hoặc phần chưa JWT)
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();

        // JWT: xóa cookie chứa token để kết thúc phiên đăng nhập
        Cookie cookie = new Cookie("access_token", "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // hết hạn ngay lập tức
        resp.addCookie(cookie);
        resp.addHeader("Set-Cookie", "access_token=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");

        // Sau khi logout, quay lại trang đăng nhập
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    // Cho phép GET để tiện thao tác và test
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }
}
