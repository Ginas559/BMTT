package vn.iotstar.controllers.comment;

import vn.iotstar.services.CommentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * CommentServlet - xử lý:
 *  - GET  /comment/thread  : trả về JSON cây bình luận (threaded) cho 1 sản phẩm
 *  - POST /comment/add     : thêm bình luận cho sản phẩm (hỗ trợ reply qua parentId)
 *  - POST /comment/delete  : xoá 1 bình luận (ràng buộc đúng user & <24h)
 *  - POST /comment/reply   : thêm trả lời (reply) cho 1 bình luận (parent)
 */
@WebServlet(name = "CommentServlet", urlPatterns = {"/comment/*"})
public class CommentServlet extends HttpServlet {

    private static final DateTimeFormatter HUMAN_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /* ================== GET ================== */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo(); // "/thread"
        if (path == null) path = "";

        switch (path) {
            case "/thread":
                handleThread(req, resp);
                break;
            default:
                sendError(req, resp, HttpServletResponse.SC_NOT_FOUND,
                        "Unknown action: " + path, null);
        }
    }

    /* ================== POST ================== */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo(); // "/add" hoặc "/delete" hoặc "/reply"
        if (path == null) path = "";

        switch (path) {
            case "/add":
                handleAdd(req, resp);
                break;
            case "/delete":
                handleDelete(req, resp);
                break;
            case "/reply":
                handleReply(req, resp); // <-- mới thêm
                break;
            default:
                sendError(req, resp, HttpServletResponse.SC_NOT_FOUND,
                        "Unknown action: " + path, null);
        }
    }

    /* ================== /comment/thread (GET) ================== */
    private void handleThread(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        Long currentUserId = extractUserIdFromSession(session);
        Long productId = parseLongObj(req.getParameter("productId"));

        if (productId == null) {
            sendError(req, resp, HttpServletResponse.SC_BAD_REQUEST,
                    "productId is required", productId);
            return;
        }

        try {
            CommentService svc = new CommentService();
            List<CommentService.ThreadItem> items = svc.listThread(productId, currentUserId);

            // JSON build
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            StringBuilder sb = new StringBuilder(2048);
            sb.append("{\"ok\":true,\"items\":[");
            for (int i = 0; i < items.size(); i++) {
                var it = items.get(i);
                if (i > 0) sb.append(',');
                sb.append('{')
                  .append("\"commentId\":").append(it.getCommentId() == null ? "null" : it.getCommentId()).append(',')
                  .append("\"parentId\":").append(it.getParentId() == null ? "null" : it.getParentId()).append(',')
                  .append("\"userId\":").append(it.getUserId() == null ? "null" : it.getUserId()).append(',')
                  .append("\"userName\":\"").append(esc(it.getUserName())).append("\",")
                  .append("\"content\":\"").append(esc(it.getContent())).append("\",")
                  .append("\"createdAt\":\"").append(it.getCreatedAt() == null ? "" : esc(df.format(it.getCreatedAt()))).append("\",")
                  .append("\"depth\":").append(it.getDepth()).append(',')
                  .append("\"canDelete\":").append(it.isCanDelete() ? "true" : "false")
                  .append('}');
            }
            sb.append("]}");

            json(resp, 200, sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            json(resp, 500, "{\"ok\":false,\"error\":\"server_error\"}");
        }
    }

    /* ================== /comment/add (POST) ================== */
    private void handleAdd(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        Long userId = extractUserIdFromSession(session);
        Long productId = parseLongObj(req.getParameter("productId"));
        Long parentId = parseLongObj(req.getParameter("parentId")); // <-- hỗ trợ reply
        String ctx = req.getContextPath();

        if (userId == null) {
            if (wantsJson(req)) {
                json(resp, 401, "{\"ok\":false,\"error\":\"unauthenticated\"}");
            } else {
                resp.sendRedirect(ctx + "/login");
            }
            return;
        }
        if (productId == null) {
            sendError(req, resp, HttpServletResponse.SC_BAD_REQUEST,
                    "productId is required", productId);
            return;
        }

        String content = req.getParameter("content");
        content = (content == null) ? "" : content.trim();
        if (content.length() > 500) content = content.substring(0, 500);
        if (content.isBlank()) {
            if (wantsJson(req)) {
                json(resp, 400, "{\"ok\":false,\"error\":\"empty_content\"}");
            } else {
                resp.sendRedirect(ctx + "/product/" + productId + "?cm=empty");
            }
            return;
        }

        try {
            CommentService svc = new CommentService();

            if (parentId != null) {
                // Thêm reply, có kiểm tra parent thuộc đúng product ở service
                int affected = svc.addReply(userId, productId, parentId, content);
                if (affected <= 0) {
                    if (wantsJson(req)) {
                        json(resp, 400, "{\"ok\":false,\"error\":\"invalid_parent\"}");
                    } else {
                        resp.sendRedirect(ctx + "/product/" + productId + "?cm=parent_invalid");
                    }
                    return;
                }
            } else {
                // Thêm comment gốc (giữ nguyên hành vi cũ)
                svc.add(userId, productId, content);
            }

            if (wantsJson(req)) {
                String userName = extractUserNameFromSession(session);
                if (userName == null || userName.isBlank()) userName = "Bạn";
                String createdAt = LocalDateTime.now().format(HUMAN_FMT);

                String body = "{"
                        + "\"ok\":true,"
                        + "\"userName\":\"" + esc(userName) + "\","
                        + "\"createdAt\":\"" + esc(createdAt) + "\","
                        + "\"content\":\"" + esc(content) + "\","
                        + "\"parentId\":" + (parentId == null ? "null" : parentId)
                        + "}";
                json(resp, 200, body);
            } else {
                resp.sendRedirect(ctx + "/product/" + productId + "?cm=ok");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (wantsJson(req)) {
                json(resp, 500, "{\"ok\":false,\"error\":\"server_error\"}");
            } else {
                resp.sendRedirect(ctx + "/product/" + productId + "?cm=err");
            }
        }
    }

    /* ================== /comment/delete (POST) ================== */
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        Long userId = extractUserIdFromSession(session);
        Long commentId = parseLongObj(req.getParameter("commentId"));
        Long productId = parseLongObj(req.getParameter("productId"));
        String ctx = req.getContextPath();

        if (userId == null) {
            if (wantsJson(req)) {
                json(resp, 401, "{\"ok\":false,\"error\":\"unauthenticated\"}");
            } else {
                resp.sendRedirect(ctx + "/login");
            }
            return;
        }
        if (commentId == null) {
            sendError(req, resp, HttpServletResponse.SC_BAD_REQUEST,
                    "commentId is required", productId);
            return;
        }

        try {
            CommentService svc = new CommentService();
            int affected = svc.deleteByIdWithin24h(commentId, userId);

            if (wantsJson(req)) {
                if (affected > 0) {
                    json(resp, 200, "{\"ok\":true}");
                } else {
                    // Có thể là hết 24h, không phải chủ sở hữu, hoặc comment đã có reply
                    json(resp, 403, "{\"ok\":false,\"error\":\"too_late\"}");
                }
            } else {
                String suffix = (affected > 0) ? "?cm_del=ok" : "?cm_del=too_late";
                String back = (productId != null)
                        ? (ctx + "/product/" + productId + suffix)
                        : (ctx + "/products" + suffix);
                resp.sendRedirect(back);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (wantsJson(req)) {
                json(resp, 500, "{\"ok\":false,\"error\":\"server_error\"}");
            } else {
                String back = (productId != null)
                        ? (ctx + "/product/" + productId + "?cm_del=err")
                        : (ctx + "/products?cm_del=err");
                resp.sendRedirect(back);
            }
        }
    }

    /* ================== /comment/reply (POST) ================== */
    private void handleReply(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        Long userId = extractUserIdFromSession(session);
        // UI gửi parentCommentId; hỗ trợ cả parentId để tương thích
        Long parentId = parseLongObj(req.getParameter("parentCommentId"));
        if (parentId == null) parentId = parseLongObj(req.getParameter("parentId"));
        Long productId = parseLongObj(req.getParameter("productId"));
        String ctx = req.getContextPath();

        if (userId == null) {
            if (wantsJson(req)) {
                json(resp, 401, "{\"ok\":false,\"error\":\"unauthenticated\"}");
            } else {
                resp.sendRedirect(ctx + "/login");
            }
            return;
        }
        if (productId == null || parentId == null) {
            sendError(req, resp, HttpServletResponse.SC_BAD_REQUEST,
                    "productId/parentId is required", productId);
            return;
        }

        String content = req.getParameter("content");
        content = (content == null) ? "" : content.trim();
        if (content.length() > 500) content = content.substring(0, 500);
        if (content.isBlank()) {
            if (wantsJson(req)) {
                json(resp, 400, "{\"ok\":false,\"error\":\"empty_content\"}");
            } else {
                resp.sendRedirect(ctx + "/product/" + productId + "?cm_rep=empty");
            }
            return;
        }

        try {
            CommentService svc = new CommentService();
            int affected = svc.addReply(userId, productId, parentId, content);
            if (affected <= 0) {
                if (wantsJson(req)) {
                    json(resp, 400, "{\"ok\":false,\"error\":\"invalid_parent\"}");
                } else {
                    resp.sendRedirect(ctx + "/product/" + productId + "?cm_rep=parent_invalid");
                }
                return;
            }

            if (wantsJson(req)) {
                String userName = extractUserNameFromSession(session);
                if (userName == null || userName.isBlank()) userName = "Bạn";
                String createdAt = LocalDateTime.now().format(HUMAN_FMT);

                String body = "{"
                        + "\"ok\":true,"
                        + "\"userName\":\"" + esc(userName) + "\","
                        + "\"createdAt\":\"" + esc(createdAt) + "\","
                        + "\"content\":\"" + esc(content) + "\","
                        + "\"parentId\":" + parentId
                        + "}";
                json(resp, 200, body);
            } else {
                resp.sendRedirect(ctx + "/product/" + productId + "?cm_rep=ok");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (wantsJson(req)) {
                json(resp, 500, "{\"ok\":false,\"error\":\"server_error\"}");
            } else {
                resp.sendRedirect(ctx + "/product/" + productId + "?cm_rep=err");
            }
        }
    }

    /* ================== Helpers ================== */

    private static boolean wantsJson(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        String xhr    = req.getHeader("X-Requested-With");
        return (accept != null && accept.toLowerCase().contains("application/json"))
                || (xhr != null && "xmlhttprequest".equalsIgnoreCase(xhr));
    }

    private static void json(HttpServletResponse resp, int status, String body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.write(body);
        }
    }

    private void sendError(HttpServletRequest req, HttpServletResponse resp,
                           int status, String message, Long productId) throws IOException {
        if (wantsJson(req)) {
            json(resp, status, "{\"ok\":false,\"error\":\"" + esc(message) + "\"}");
        } else {
            if (productId != null) {
                resp.sendRedirect(req.getContextPath() + "/product/" + productId + "?cm=err");
            } else {
                resp.sendError(status, message);
            }
        }
    }

    /* ==== session & parsing ==== */

    private static Long parseLongObj(String s) {
        try { return (s == null || s.isBlank()) ? null : Long.valueOf(s); }
        catch (Exception e) { return null; }
    }

    private static Long extractUserIdFromSession(HttpSession session) {
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
        return null;
    }

    private static String extractUserNameFromSession(HttpSession session) {
        if (session == null) return null;
        Object uname = safeGet(session, "currentUserName");
        if (uname instanceof String && !((String) uname).isBlank()) return (String) uname;
        Object uobj = null;
        String[] keys = new String[]{"currentUser","loginUser","user","account","customer","authUser"};
        for (String k : keys) {
            uobj = safeGet(session, k);
            if (uobj != null) break;
        }
        if (uobj != null) {
            try {
                Object fn = uobj.getClass().getMethod("getFullname").invoke(uobj);
                if (fn instanceof String && !((String) fn).isBlank()) return (String) fn;
            } catch (Exception ignore) {}
            try {
                Object f = uobj.getClass().getMethod("getFirstname").invoke(uobj);
                Object l = uobj.getClass().getMethod("getLastname").invoke(uobj);
                String s1 = (f instanceof String) ? (String) f : "";
                String s2 = (l instanceof String) ? (String) l : "";
                String both = (s1 + " " + s2).trim();
                if (!both.isBlank()) return both;
            } catch (Exception ignore) {}
            try {
                Object name = uobj.getClass().getMethod("getName").invoke(uobj);
                if (name instanceof String && !((String) name).isBlank()) return (String) name;
            } catch (Exception ignore) {}
        }
        return null;
    }

    private static Object safeGet(HttpSession s, String key){
        try { return s.getAttribute(key); } catch (Exception e){ return null; }
    }
    private static Long castToLong(Object v){
        try {
            if (v instanceof Long) return (Long) v;
            if (v instanceof Integer) return ((Integer) v).longValue();
        } catch (Exception ignore){}
        return null;
    }
    private static Long reflectId(Object obj){
        if (obj == null) return null;
        try {
            var m = obj.getClass().getMethod("getUserId");
            Object r = m.invoke(obj);
            Long id = castToLong(r);
            if (id != null) return id;
        } catch (NoSuchMethodException ignore) {
            try {
                var m2 = obj.getClass().getMethod("getId");
                Object r2 = m2.invoke(obj);
                Long id2 = castToLong(r2);
                if (id2 != null) return id2;
            } catch (Exception ignore2) {}
        } catch (Exception ignore) {}
        return null;
    }

    private static String esc(String s){
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"")
                .replace("\n","\\n").replace("\r","\\r").replace("\t","\\t");
    }
}
