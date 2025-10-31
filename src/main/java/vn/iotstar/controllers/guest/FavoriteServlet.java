package vn.iotstar.controllers.guest;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;

import vn.iotstar.dto.FavoriteItem;
import vn.iotstar.services.FavoriteService;

public class FavoriteServlet extends HttpServlet {

    private final FavoriteService favoriteService = new FavoriteService();

    // ===== Utils: write JSON with explicit Content-Length =====
    private void writeJson(HttpServletResponse resp, int status, String json) throws IOException {
        if (json == null) json = "";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        resp.setStatus(status);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setContentLength(bytes.length);
        try (OutputStream os = resp.getOutputStream()) {
            os.write(bytes);
            os.flush();
        }
    }

    private boolean isToggleEndpoint(HttpServletRequest req) {
        // chấp nhận cả 2 kiểu mapping
        String sp = req.getServletPath();   // có thể là "/favorite/toggle" hoặc "/favorite"
        String pi = req.getPathInfo();      // có thể là null hoặc "/toggle"
        String uri = req.getRequestURI();   // .../favorite/toggle
        return "/favorite/toggle".equals(sp)
                || ("/favorite".equals(sp) && "/toggle".equals(pi))
                || (uri != null && uri.endsWith("/favorite/toggle"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isToggleEndpoint(req)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Long userId = extractUserIdFromSession(req.getSession(false));
        if (userId == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"ok\":false,\"message\":\"Bạn cần đăng nhập để sử dụng Yêu thích\"}");
            return;
        }

        Long productId = parseLongObj(req.getParameter("productId"));
        if (productId == null) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "{\"ok\":false,\"message\":\"Thiếu hoặc sai productId\"}");
            return;
        }

        try {
            boolean favNow = favoriteService.toggle(userId, productId);
            long count = favoriteService.countByProduct(productId);
            String json = String.format("{\"ok\":true,\"fav\":%s,\"count\":%d}",
                    favNow ? "true" : "false", count);
            writeJson(resp, HttpServletResponse.SC_OK, json);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null) msg = "Unknown";
            msg = msg.replace("\"", "\\\"");
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "{\"ok\":false,\"message\":\"Lỗi hệ thống: " + msg + "\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Kỳ vọng map: /favorites  (GET)
        String sp = req.getServletPath();
        if (!"/favorites".equals(sp)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Long userId = extractUserIdFromSession(req.getSession(false));
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        List<FavoriteItem> list = favoriteService.listByUser(userId);
        req.setAttribute("favorites", list);
        req.setAttribute("pageTitle", "Yêu thích");
        req.getRequestDispatcher("/WEB-INF/views/products/favorites.jsp").forward(req, resp);
    }

    // ================= Helpers =================

    private Long parseLongObj(String s) {
        try { return (s == null || s.isBlank()) ? null : Long.valueOf(s); }
        catch (Exception e) { return null; }
    }

    private Long extractUserIdFromSession(HttpSession session) {
        if (session == null) return null;

        Object direct = safeGet(session, "userId");
        Long id = castToLong(direct);
        if (id != null) return id;

        String[] keys = new String[]{"currentUser","loginUser","user","account","customer","authUser"};
        for (String k : keys) {
            Object v = safeGet(session, k);
            id = reflectId(v);
            if (id != null) return id;
        }

        try {
            Enumeration<String> names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                Object v = session.getAttribute(name);
                id = castToLong(v);
                if (id != null) return id;

                id = reflectId(v);
                if (id != null) return id;
            }
        } catch (Exception ignore) {}
        return null;
    }

    private Object safeGet(HttpSession s, String key) {
        try { return s.getAttribute(key); } catch (Exception e) { return null; }
    }

    private Long castToLong(Object v) {
        try {
            if (v instanceof Long) return (Long) v;
            if (v instanceof Integer) return ((Integer) v).longValue();
        } catch (Exception ignore) {}
        return null;
    }

    private Long reflectId(Object obj) {
        if (obj == null) return null;
        try {
            Method m = obj.getClass().getMethod("getUserId");
            Object r = m.invoke(obj);
            Long id = castToLong(r);
            if (id != null) return id;
        } catch (NoSuchMethodException ignore) {
            try {
                Method m2 = obj.getClass().getMethod("getId");
                Object r2 = m2.invoke(obj);
                Long id2 = castToLong(r2);
                if (id2 != null) return id2;
            } catch (Exception ignore2) {}
        } catch (Exception ignore) {}
        return null;
    }
}
