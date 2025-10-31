// filepath: src/main/java/vn/iotstar/utils/AuthFilter.java
package vn.iotstar.utils;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Set;

/**
 * Bộ lọc quyền truy cập cho khu vực /vendor/*
 *
 * Mục tiêu:
 * 1) Ưu tiên xác thực bằng JWT: đọc token từ Authorization: Bearer ... hoặc cookie "access_token".
 *    - Nếu token hợp lệ: gắn uid/role/shopId vào request attributes để các servlet phía sau dùng.
 * 2) Fallback bằng session: giữ cơ chế cũ (role từ session và staffShopId) nếu không có JWT.
 * 3) Quy tắc quyền:
 *    - VENDOR: được vào toàn bộ /vendor/*
 *    - STAFF (USER có staffShopId): chỉ được vào các path whitelist (dashboard, products, orders)
 *    - Các trường hợp khác: chuyển về /login hoặc trả 403 tùy tình huống
 */
@WebFilter(urlPatterns = {"/vendor/*"})
public class AuthFilter implements Filter {

    // Danh sách đường dẫn staff được phép truy cập khi fallback session
    private static final Set<String> STAFF_ALLOWED = Set.of(
            "/vendor/dashboard",
            "/vendor/products",
            "/vendor/orders"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Mặc định chưa xác thực
        Long jwtUid = null;
        String jwtRole = null;
        Long jwtShopId = null;

        // 1) Ưu tiên JWT: nếu có token thì xác minh
        String token = readBearerToken(req);
        if (token == null) {
            token = readCookieToken(req, "access_token");
        }

        boolean jwtOk = false;
        if (token != null && !token.isBlank()) {
            try {
                DecodedJWT jwt = JwtUtil.verify(token);
                jwtUid = JwtUtil.getUserId(jwt);
                jwtRole = JwtUtil.getRole(jwt);
                jwtShopId = JwtUtil.getShopId(jwt);

                // Gắn vào request để các servlet vendor dùng trực tiếp
                req.setAttribute("uid", jwtUid);
                req.setAttribute("role", jwtRole);
                req.setAttribute("shopId", jwtShopId);

                jwtOk = true;
            } catch (Exception ex) {
                // Token có nhưng không hợp lệ → không dùng JWT, chuyển xuống fallback session
                jwtOk = false;
            }
        }

        // 2) Nếu không dùng được JWT, fallback session y như cơ chế cũ
        if (!jwtOk) {
            HttpSession ss = req.getSession(false);
            String role = (ss != null) ? (String) ss.getAttribute("role") : null;
            Object staffShopId = (ss != null) ? ss.getAttribute("staffShopId") : null;

            // Vendor theo session: cho qua toàn bộ /vendor/*
            if ("VENDOR".equals(role)) {
                // Không có JWT nên không thể gắn uid/shopId, phần này chỉ giữ hành vi cũ
                chain.doFilter(request, response);
                return;
            }

            // Staff theo session: chỉ whitelist
            if ("USER".equals(role) && staffShopId != null) {
                String sp = req.getServletPath(); // ví dụ: /vendor/products
                if (isStaffAllowed(sp)) {
                    chain.doFilter(request, response);
                    return;
                }
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Staff không có quyền truy cập trang này.");
                return;
            }

            // Không có JWT và cũng không có session hợp lệ → yêu cầu đăng nhập
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 3) JWT hợp lệ: áp quyền theo vai trò từ token
        // VENDOR: cho qua toàn bộ /vendor/*
        if ("VENDOR".equals(jwtRole)) {
            chain.doFilter(request, response);
            return;
        }

        // STAFF qua JWT (nếu về sau thêm claim staff): hiện tại chưa có claim staff riêng
        // Nếu muốn hỗ trợ staff qua JWT, có thể bổ sung thêm claim và whitelist tương tự như session.

        // JWT hợp lệ nhưng không phải VENDOR → không được vào /vendor/*
        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập khu vực này.");
    }

    private boolean isStaffAllowed(String servletPath) {
        if (servletPath == null) return false;
        for (String allowed : STAFF_ALLOWED) {
            if (servletPath.startsWith(allowed)) return true;
        }
        return false;
    }

    private String readBearerToken(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        if (h == null) return null;
        if (!h.startsWith("Bearer ")) return null;
        return h.substring(7).trim();
    }

    private String readCookieToken(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
