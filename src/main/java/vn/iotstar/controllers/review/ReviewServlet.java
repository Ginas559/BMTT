package vn.iotstar.controllers.review;

import vn.iotstar.services.ReviewService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ReviewServlet
 *  - POST /review/save
 *  - POST /review/delete
 *  - GET  /review/list    (lazy-load danh sách)
 *  - GET  /review/stats   (avg, count)
 *  - GET  /review/mine    (đánh giá của chính user nếu có)
 */
@MultipartConfig(
        fileSizeThreshold = 0,                   // write to disk immediately
        maxFileSize = 60L * 1024 * 1024,        // per-file cap (>=50MB video)
        maxRequestSize = 80L * 1024 * 1024      // total cap
)
@WebServlet(name = "ReviewServlet", urlPatterns = {"/review/*"})
public class ReviewServlet extends HttpServlet {

    /* ================== GET endpoints ================== */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";
        switch (path) {
            case "/list":  handleList(req, resp);  break;
            case "/stats": handleStats(req, resp); break;
            case "/mine":  handleMine(req, resp);  break;
            default: resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * GET /review/list?productId=...&limit=...
     * Public, trả JSON: { ok, items: [...] }
     */
    private void handleList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long productId = parseLongObj(req.getParameter("productId"));
        if (productId == null) {
            json(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"ok\":false,\"error\":\"bad_request\"}");
            return;
        }
        Integer limit = parseIntObj(req.getParameter("limit"));
        if (limit == null || limit <= 0 || limit > 1000) limit = 100;

        try {
            ReviewService svc = new ReviewService();

            // Thử gọi nhiều tên hàm phổ biến trong ReviewService (Long[, Integer])
            List<?> raw = tryFetchList(svc, productId, limit);

            // Build JSON an toàn từ bất kỳ kiểu phần tử nào
            StringBuilder sb = new StringBuilder(512);
            sb.append("{\"ok\":true,\"items\":[");
            boolean first = true;
            for (Object it : raw) {
                if (!first) sb.append(',');
                first = false;
                sb.append(buildItemJson(
                        getStringAny(it,"getUserName","getUsername","getName","getUserFullName","getFullName","getUser"),
                        getStringAny(it,"getCreatedAt","getCreateAt","getCreateDate","getCreatedDate","getCreatedTime","getDate","getTime"),
                        getIntAny(it, 0, "getRating","getStars","getStar"),
                        getStringAny(it,"getCommentText","getComment","getContent","getReviewText","getText"),
                        getStringAny(it,"getImageUrl","getImage","getPhotoUrl"),
                        getStringAny(it,"getVideoUrl","getVideo")
                ));
            }
            sb.append("]}");
            json(resp, HttpServletResponse.SC_OK, sb.toString());

        } catch (Exception e) {
            // Không để 500 làm vỡ UI; log và trả items rỗng để giao diện vẫn hoạt động
            e.printStackTrace();
            json(resp, HttpServletResponse.SC_OK, "{\"ok\":true,\"items\":[]}");
        }
    }

    /** GET /review/stats?productId=... -> {ok, avg, count} */
    private void handleStats(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long productId = parseLongObj(req.getParameter("productId"));
        if (productId == null) {
            json(resp, 400, "{\"ok\":false,\"error\":\"bad_request\"}");
            return;
        }
        try {
            ReviewService svc = new ReviewService();
            ReviewService.Stats s = nullSafeStats(svc, productId);
            String body = "{\"ok\":true,\"avg\":" + formatDouble(s.getAvg()) + ",\"count\":" + s.getCount() + "}";
            json(resp, 200, body);
        } catch (Exception e) {
            e.printStackTrace();
            // Fail-soft để UI vẫn hiển thị: avg=0, count=0
            json(resp, 200, "{\"ok\":true,\"avg\":0.00,\"count\":0}");
        }
    }

    /** GET /review/mine?productId=... -> {ok, userReview:{...}|null} */
    private void handleMine(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long productId = parseLongObj(req.getParameter("productId"));
        if (productId == null) {
            json(resp, 400, "{\"ok\":false,\"error\":\"bad_request\"}");
            return;
        }
        try {
            HttpSession session = req.getSession(false);
            Long userId = extractUserIdFromSession(session);
            if (userId == null) { // chưa đăng nhập
                json(resp, 200, "{\"ok\":true,\"userReview\":null}");
                return;
            }
            ReviewService svc = new ReviewService();
            ReviewService.ReviewItem me = nullSafeMyReview(svc, productId, userId);
            if (me == null) {
                json(resp, 200, "{\"ok\":true,\"userReview\":null}");
            } else {
                String body = "{\"ok\":true,\"userReview\":" + buildItemJson(
                        me.getUserName(), toIso(me.getCreatedAt()), me.getRating(),
                        me.getCommentText(), me.getImageUrl(), me.getVideoUrl()
                ) + "}";
                json(resp, 200, body);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fail-soft
            json(resp, 200, "{\"ok\":true,\"userReview\":null}");
        }
    }

    /* Gọi list qua reflection với nhiều tên hàm/kiểu tham số */
    @SuppressWarnings({"rawtypes","unchecked"})
    private List<?> tryFetchList(ReviewService svc, Long productId, Integer limit) {
        String[] names = new String[]{
            "list", "listByProduct", "findByProduct", "getByProduct",
            "getByProductId", "findAllByProductId", "getReviews", "reviews",
            "findAllByProduct", "getAllByProductId",
            "listPublic", "publicList", "listVisible", "visibleList",
            "listApproved", "approvedList", "findPublic", "getPublic",
            "findApproved", "getApproved", "findAllVisible", "findAllApproved",
            "listAll", "findAll", "getAll"
        };
        Class<?> cls = svc.getClass();

        // Ưu tiên 2 tham số
        for (String n : names) {
            Method m = findPublic(cls, n, Long.class, Integer.class);
            if (m == null) m = findPublic(cls, n, long.class, int.class);
            if (m == null) m = findPublic(cls, n, Integer.class, Integer.class);
            if (m == null) m = findPublic(cls, n, int.class, int.class);
            if (m != null) {
                try {
                    Object arg0 = (m.getParameterTypes()[0] == int.class || m.getParameterTypes()[0] == Integer.class)
                            ? (productId == null ? 0 : productId.intValue()) : productId;
                    Object arg1 = (m.getParameterTypes()[1] == int.class || m.getParameterTypes()[1] == Integer.class)
                            ? (limit == null ? 0 : limit.intValue()) : limit;
                    Object r = m.invoke(svc, arg0, arg1);
                    if (r instanceof List) return (List) r;
                } catch (Throwable ignore) {}
            }
        }

        // 1 tham số
        for (String n : names) {
            Method m = findPublic(cls, n, Long.class);
            if (m == null) m = findPublic(cls, n, long.class);
            if (m == null) m = findPublic(cls, n, Integer.class);
            if (m == null) m = findPublic(cls, n, int.class);
            if (m != null) {
                try {
                    Object arg = (m.getParameterTypes()[0] == int.class || m.getParameterTypes()[0] == Integer.class)
                            ? (productId == null ? 0 : productId.intValue()) : productId;
                    Object r = m.invoke(svc, arg);
                    if (r instanceof List) return (List) r;
                } catch (Throwable ignore) {}
            }
        }

        return Collections.emptyList();
    }

    private static Method findPublic(Class<?> cls, String name, Class<?>... params) {
        try { return cls.getMethod(name, params); }
        catch (NoSuchMethodException e) { return null; }
    }

    /* ================== POST endpoints ================== */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo(); // "/save" hoặc "/delete"
        if (path == null) path = "";
        switch (path) {
            case "/save":   handleSave(req, resp); break;
            case "/delete": handleDelete(req, resp); break;
            default:
                sendError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Unknown action: " + path, null);
        }
    }

    private void handleSave(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        HttpSession session = req.getSession(false);
        Long userId = extractUserIdFromSession(session);
        String ctx = req.getContextPath();

        if (userId == null) {
            if (wantsJson(req)) json(resp, 401, "{\"ok\":false,\"error\":\"unauthenticated\"}");
            else resp.sendRedirect(ctx + "/login");
            return;
        }

        String contentType = req.getContentType();
        boolean isJsonBody = contentType != null && contentType.toLowerCase(Locale.ROOT).contains("application/json");
        boolean isMultipart = contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/");

        String productIdRaw = req.getParameter("productId");
        String ratingRaw    = req.getParameter("rating");
        String comment      = safe(req.getParameter("comment"));
        String imageUrl     = trimToNull(req.getParameter("imageUrl")); // fallback (form cũ)
        String videoUrl     = trimToNull(req.getParameter("videoUrl")); // fallback (form cũ)

        if (isJsonBody) {
            String body = readBody(req);
            if (isBlank(productIdRaw)) productIdRaw = jsonGetString(body, "productId");
            if (isBlank(ratingRaw))    ratingRaw    = jsonGetString(body, "rating");
            if (comment == null)       comment      = jsonGetString(body, "comment");
            if (imageUrl == null)      imageUrl     = jsonGetString(body, "imageUrl");
            if (videoUrl == null)      videoUrl     = jsonGetString(body, "videoUrl");
        }

        Long productId = parseLongObj(productIdRaw);
        Integer rating = parseIntObj(ratingRaw);

        if (productId == null) {
            sendError(req, resp, 400, "productId is required", null);
            return;
        }
        if (rating == null || rating < 1 || rating > 5) {
            if (wantsJson(req)) json(resp, 400, "{\"ok\":false,\"error\":\"invalid_rating\"}");
            else resp.sendRedirect(ctx + "/product/" + productId + "?rv=invalid_rating");
            return;
        }
        if (comment != null && comment.length() > 2000) comment = comment.substring(0, 2000);

        boolean can;
        try { can = new ReviewService().canReview(productId, userId); }
        catch (Exception e) { can = false; }

        if (!can) {
            if (wantsJson(req)) json(resp, 403, "{\"ok\":false,\"error\":\"forbidden\"}");
            else resp.sendRedirect(ctx + "/product/" + productId + "?rv=forbidden");
            return;
        }

        try {
            ReviewService svc = new ReviewService();

            if (isMultipart) {
                // ===== NEW: xử lý upload =====
                List<String> imageUrls = new ArrayList<>();
                String videoUploadedUrl = null;

                // Ràng buộc
                final int MAX_IMAGES = 6;
                final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;   // 5MB
                final long MAX_VIDEO_SIZE = 50L * 1024 * 1024;  // 50MB

                // Lấy parts
                Collection<Part> parts = req.getParts();
                for (Part p : parts) {
                    String name = p.getName();
                    if (p.getSize() <= 0) continue;

                    if ("images".equals(name)) {
                        // kiểm tra MIME
                        String ct = safe(p.getContentType());
                        if (ct == null || !(ct.startsWith("image/"))) continue;
                        if (p.getSize() > MAX_IMAGE_SIZE) {
                            // bỏ qua ảnh quá nặng
                            continue;
                        }
                        String url = uploadPart(p, false, req);
                        if (url != null) imageUrls.add(url);
                        if (imageUrls.size() >= MAX_IMAGES) break;
                    }
                }
                // video (chỉ 1)
                Part videoPart = null;
                try { videoPart = req.getPart("video"); } catch (Exception ignore) {}
                if (videoPart != null && videoPart.getSize() > 0) {
                    String vct = safe(videoPart.getContentType());
                    if (vct != null && vct.startsWith("video/") && videoPart.getSize() <= MAX_VIDEO_SIZE) {
                        videoUploadedUrl = uploadPart(videoPart, true, req);
                    }
                }

                // Gọi overload (nhiều ảnh)
                svc.saveOrUpdate(userId, productId, rating, comment, imageUrls, videoUploadedUrl);

                // Trả về JSON/redirect tương tự luồng cũ
                if (wantsJson(req)) {
                    ReviewService.Stats st = nullSafeStats(svc, productId);
                    ReviewService.ReviewItem me = nullSafeMyReview(svc, productId, userId);

                    String uName = (me != null && notBlank(me.getUserName()))
                            ? me.getUserName()
                            : getSessionUserName(session);
                    int rateVal = (me != null && me.getRating() > 0) ? me.getRating() : rating;
                    String cmt  = (me != null && notBlank(me.getCommentText()))
                            ? me.getCommentText()
                            : nullToEmpty(comment);
                    String img  = (me != null && notBlank(me.getImageUrl())) ? me.getImageUrl()
                            : (!imageUrls.isEmpty() ? imageUrls.get(0) : "");
                    String vid  = (me != null && notBlank(me.getVideoUrl())) ? me.getVideoUrl()
                            : nullToEmpty(videoUploadedUrl);

                    String body =
                            "{"
                          + "\"ok\":true,"
                          + "\"stats\":{"
                              + "\"avg\":" + formatDouble(st.getAvg()) + ","
                              + "\"count\":" + st.getCount()
                          + "},"
                          + "\"userReview\":" + buildItemJson(uName, toIso(LocalDateTime.now()), rateVal, cmt, img, vid)
                          + "}";
                    json(resp, 200, body);
                } else {
                    resp.sendRedirect(ctx + "/product/" + productId + "?rv=ok");
                }
            } else {
                // ===== Luồng cũ: form thường hoặc JSON với URL sẵn =====
                svc.saveOrUpdate(userId, productId, rating, comment, imageUrl, videoUrl);

                if (wantsJson(req)) {
                    ReviewService.Stats st = nullSafeStats(svc, productId);
                    ReviewService.ReviewItem me = nullSafeMyReview(svc, productId, userId);

                    // Fallback dữ liệu trả về để UI luôn có sao + nội dung ngay lập tức
                    String uName = (me != null && notBlank(me.getUserName()))
                            ? me.getUserName()
                            : getSessionUserName(session);
                    int rateVal = (me != null && me.getRating() > 0) ? me.getRating() : rating;
                    String cmt  = (me != null && notBlank(me.getCommentText()))
                            ? me.getCommentText()
                            : nullToEmpty(comment);
                    String img  = (me != null && notBlank(me.getImageUrl())) ? me.getImageUrl() : nullToEmpty(imageUrl);
                    String vid  = (me != null && notBlank(me.getVideoUrl())) ? me.getVideoUrl() : nullToEmpty(videoUrl);

                    String body =
                            "{"
                          + "\"ok\":true,"
                          + "\"stats\":{"
                              + "\"avg\":" + formatDouble(st.getAvg()) + ","
                              + "\"count\":" + st.getCount()
                          + "},"
                          + "\"userReview\":" + buildItemJson(uName, toIso(LocalDateTime.now()), rateVal, cmt, img, vid)
                          + "}";
                    json(resp, 200, body);
                } else {
                    resp.sendRedirect(ctx + "/product/" + productId + "?rv=ok");
                }
            }
        } catch (ReviewService.TooLateException te) {
            if (wantsJson(req)) json(resp, 403, "{\"ok\":false,\"error\":\"too_late\"}");
            else resp.sendRedirect(ctx + "/product/" + productId + "?rv=too_late");
        } catch (IllegalStateException ise) {
            // Ví dụ: "not_delivered" từ service
            String code = "server_error";
            if ("not_delivered".equalsIgnoreCase(String.valueOf(ise.getMessage()))) code = "forbidden";
            if (wantsJson(req)) json(resp, 403, "{\"ok\":false,\"error\":\"" + code + "\"}");
            else resp.sendRedirect(ctx + "/product/" + productId + "?rv=" + code);
        } catch (Exception e) {
            e.printStackTrace();
            if (wantsJson(req)) json(resp, 500, "{\"ok\":false,\"error\":\"server_error\"}");
            else resp.sendRedirect(ctx + "/product/" + productId + "?rv=err");
        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        Long userId = extractUserIdFromSession(session);
        String ctx = req.getContextPath();

        if (userId == null) {
            if (wantsJson(req)) json(resp, 401, "{\"ok\":false,\"error\":\"unauthenticated\"}");
            else resp.sendRedirect(ctx + "/login");
            return;
        }

        String contentType = req.getContentType();
        boolean isJsonBody = contentType != null && contentType.toLowerCase().contains("application/json");

        String productIdRaw = req.getParameter("productId");
        if (isJsonBody && isBlank(productIdRaw)) productIdRaw = jsonGetString(readBody(req), "productId");

        Long productId = parseLongObj(productIdRaw);
        if (productId == null) {
            sendError(req, resp, 400, "productId is required", null);
            return;
        }

        try {
            ReviewService svc = new ReviewService();
            svc.deleteByUser(userId, productId);

            if (wantsJson(req)) {
                ReviewService.Stats st = nullSafeStats(svc, productId);
                String body =
                        "{"
                      + "\"ok\":true,"
                      + "\"stats\":{"
                          + "\"avg\":" + formatDouble(st.getAvg()) + ","
                          + "\"count\":" + st.getCount()
                      + "}"
                      + "}";
                json(resp, 200, body);
            } else {
                resp.sendRedirect(ctx + "/product/" + productId + "?rv_del=ok");
            }
        } catch (ReviewService.TooLateException te) {
            if (wantsJson(req)) json(resp, 403, "{\"ok\":false,\"error\":\"too_late\"}");
            else resp.sendRedirect(ctx + "/product/" + productId + "?rv_del=too_late");
        } catch (Exception e) {
            e.printStackTrace();
            if (wantsJson(req)) json(resp, 500, "{\"ok\":false,\"error\":\"server_error\"}");
            else resp.sendRedirect(ctx + "/product/" + productId + "?rv_del=err");
        }
    }

    /* ================== Helpers ================== */

    private static boolean wantsJson(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        String xhr    = req.getHeader("X-Requested-With");
        String ct     = req.getContentType();
        return (accept != null && accept.toLowerCase().contains("application/json"))
            || (xhr != null && "xmlhttprequest".equalsIgnoreCase(xhr))
            || (ct != null && ct.toLowerCase().contains("application/json"));
    }

    private static String ensureUploadsDir(HttpServletRequest req) {
        String root = req.getServletContext().getRealPath("/");
        if (root == null) root = System.getProperty("java.io.tmpdir");
        File dir = new File(root, "uploads");
        if (!dir.exists()) dir.mkdirs();
        return dir.getAbsolutePath();
    }

    /** Upload 1 Part. Thử Cloudinary trước; nếu không có thư viện/config thì lưu local /uploads. */
    private String uploadPart(Part part, boolean isVideo, HttpServletRequest req) {
        // 1) Thử Cloudinary qua reflection (để không bắt buộc dependency tại compile-time)
        try {
            Class<?> cloudinaryCls = Class.forName("com.cloudinary.Cloudinary");
            Class<?> utilsCls = Class.forName("com.cloudinary.utils.ObjectUtils");
            // Lấy cấu hình từ env/system properties: CLOUDINARY_URL hoặc map {cloud_name, api_key, api_secret}
            String url = System.getenv("CLOUDINARY_URL");
            Object cloud;
            if (url != null && !url.isBlank()) {
                cloud = cloudinaryCls.getConstructor(String.class).newInstance(url);
            } else {
                Map<?,?> cfg = (Map<?,?>) utilsCls.getMethod("asMap", Object[].class)
                        .invoke(null, (Object) new Object[]{
                                "cloud_name", System.getenv("CLOUDINARY_CLOUD_NAME"),
                                "api_key",    System.getenv("CLOUDINARY_API_KEY"),
                                "api_secret", System.getenv("CLOUDINARY_API_SECRET")
                        });
                cloud = cloudinaryCls.getConstructor(Map.class).newInstance(cfg);
            }
            // Tạo file tạm để upload
            File tmp = File.createTempFile("rv_", isVideo ? ".mp4" : ".bin");
            try (InputStream in = part.getInputStream(); FileOutputStream out = new FileOutputStream(tmp)) {
                in.transferTo(out);
            }
            Object uploader = cloudinaryCls.getMethod("uploader").invoke(cloud);
            Map<?,?> opt = (Map<?,?>) utilsCls.getMethod("emptyMap").invoke(null);
            if (isVideo) {
                // resource_type=video
                opt = (Map<?,?>) utilsCls.getMethod("asMap", Object[].class)
                        .invoke(null, (Object) new Object[]{"resource_type", "video"});
            }
            @SuppressWarnings("unchecked")
            Map<String,Object> res = (Map<String,Object>) uploader.getClass()
                    .getMethod("upload", Object.class, Map.class)
                    .invoke(uploader, tmp, opt);
            Object secure = res.get("secure_url");
            if (secure != null) return String.valueOf(secure);
        } catch (Throwable ignore) {
            // bỏ qua để fallback local
        }

        // 2) Fallback: lưu local /uploads
        try {
            String uploads = ensureUploadsDir(req);
            String original = getSubmittedFileName(part);
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            } else if (isVideo) {
                ext = ".mp4";
            }
            String fname = "rv_" + System.currentTimeMillis() + "_" + (int)(Math.random()*1_000_000) + ext;
            File f = new File(uploads, fname);
            try (InputStream in = part.getInputStream(); FileOutputStream out = new FileOutputStream(f)) {
                in.transferTo(out);
            }
            // Trả URL tương đối để frontend hiển thị (cần server static map thư mục /uploads)
            String encoded = URLEncoder.encode(fname, StandardCharsets.UTF_8);
            return req.getContextPath() + "/uploads/" + encoded;
        } catch (Exception e) {
            return null;
        }
    }

    private static String getSubmittedFileName(Part part) {
        String cd = part.getHeader("content-disposition");
        if (cd == null) return null;
        // form-data; name="images"; filename="xxx.png"
        for (String c : cd.split(";")) {
            String t = c.trim().toLowerCase(Locale.ROOT);
            if (t.startsWith("filename=")) {
                String fn = c.substring(c.indexOf('=') + 1).trim().replace("\"", "");
                return fn;
            }
        }
        return null;
    }

    private static void json(HttpServletResponse resp, int status, String body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) { out.write(body); }
    }

    private void sendError(HttpServletRequest req, HttpServletResponse resp,
                           int status, String message, Long productId) throws IOException {
        if (wantsJson(req)) {
            json(resp, status, "{\"ok\":false,\"error\":\"" + esc(message) + "\"}");
        } else {
            if (productId != null) resp.sendRedirect(req.getContextPath() + "/product/" + productId + "?rv=err");
            else resp.sendError(status, message);
        }
    }

    private static Long parseLongObj(String s) {
        try { return (s == null || s.isBlank()) ? null : Long.valueOf(s.trim()); }
        catch (Exception e) { return null; }
    }
    private static Integer parseIntObj(String s) {
        try { return (s == null || s.isBlank()) ? null : Integer.valueOf(s.trim()); }
        catch (Exception e) { return null; }
    }
    private static String safe(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
    private static String trimToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
    private static String nullToEmpty(String s) { return s == null ? "" : s; }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static boolean notBlank(String s){ return s != null && !s.trim().isEmpty(); }

    private static String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line; var reader = req.getReader();
        while ((line = reader.readLine()) != null) sb.append(line);
        return sb.toString();
    }
    private static String jsonGetString(String json, String key) {
        if (json == null || key == null) return null;
        String p1 = "\""+key+"\"\\s*:\\s*\"([^\"]*)\"";
        String p2 = "\""+key+"\"\\s*:\\s*([-]?[0-9]+)";
        java.util.regex.Matcher m1 = java.util.regex.Pattern.compile(p1).matcher(json);
        if (m1.find()) return m1.group(1);
        java.util.regex.Matcher m2 = java.util.regex.Pattern.compile(p2).matcher(json);
        if (m2.find()) return m2.group(1);
        return null;
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

    /** NEW: lấy tên hiển thị từ session để fallback khi trả userReview */
    private static String getSessionUserName(HttpSession session){
        if (session == null) return "Bạn";
        String[] directKeys = new String[]{"displayName","userName","username","fullName","name","email"};
        for (String k : directKeys){
            try {
                Object v = session.getAttribute(k);
                if (v instanceof String && notBlank((String) v)) return (String) v;
            } catch (Exception ignore){}
        }
        String[] objKeys = new String[]{"currentUser","loginUser","user","account","customer","authUser"};
        for (String k : objKeys){
            try{
                Object u = session.getAttribute(k);
                if (u == null) continue;
                String s = getStringAny(u, "getFullName","getName","getUsername","getUserName","getEmail");
                if (notBlank(s)) return s;
            }catch(Exception ignore){}
        }
        return "Bạn";
    }

    private static Object safeGet(HttpSession s, String key){ try { return s.getAttribute(key); } catch (Exception e){ return null; } }
    private static Long castToLong(Object v){
        try {
            if (v == null) return null;
            if (v instanceof Long) return (Long) v;
            if (v instanceof Integer) return ((Integer) v).longValue();
            if (v instanceof String) {
                String s = ((String) v).trim();
                if (s.isEmpty()) return null;
                return Long.parseLong(s);
            }
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
    private static String formatDouble(double v) {
        return String.format(java.util.Locale.US, "%.2f", v);
    }
    private static String toJson(String s) { return (s == null) ? "null" : "\"" + esc(s) + "\""; }

    private static ReviewService.Stats nullSafeStats(ReviewService svc, Long productId) {
        try { ReviewService.Stats s = svc.stats(productId); if (s != null) return s; }
        catch (Exception ignore) {}
        return new ReviewService.Stats(0.0, 0L);
    }
    private static ReviewService.ReviewItem nullSafeMyReview(ReviewService svc, Long productId, Long userId) {
        try { return svc.findByUser(productId, userId); }
        catch (Exception ignore) { return null; }
    }

    /* ==== Helpers lấy field bằng reflection linh hoạt ==== */
    private static String getStringAny(Object bean, String... getters) {
        if (bean == null) return null;
        // Nếu getter là "getUser" → thử lấy tên từ user nested
        for (String g : getters) {
            if ("getUser".equals(g)) {
                try {
                    Method m = bean.getClass().getMethod("getUser");
                    Object user = m.invoke(bean);
                    if (user != null) {
                        String s = getStringAny(user, "getFullName","getName","getUsername","getUserName","getEmail");
                        if (s != null && !s.isEmpty()) return s;
                    }
                } catch (Exception ignore) {}
                continue;
            }
            try {
                Method m = bean.getClass().getMethod(g);
                Object r = m.invoke(bean);
                if (r == null) continue;
                String s = String.valueOf(r);
                if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) return s;
            } catch (Exception ignore) {}
        }
        return null;
    }
    private static int getIntAny(Object bean, int def, String... getters) {
        if (bean == null) return def;
        for (String g : getters) {
            try {
                Method m = bean.getClass().getMethod(g);
                Object r = m.invoke(bean);
                if (r == null) continue;
                if (r instanceof Number) return ((Number) r).intValue();
                try { return Integer.parseInt(String.valueOf(r)); } catch (Exception ignore) {}
            } catch (Exception ignore) {}
        }
        return def;
    }

    /* ===== JSON builders ===== */
    private static String buildItemJson(String userName, String createdAt, int rating,
                                        String commentText, String imageUrl, String videoUrl) {
        return "{"
                + "\"userName\":"   + toJson(userName) + ","
                + "\"createdAt\":"  + toJson(createdAt) + ","
                + "\"rating\":"     + rating + ","
                + "\"commentText\":"+ toJson(commentText) + ","
                + "\"imageUrl\":"   + toJson(imageUrl) + ","
                + "\"videoUrl\":"   + toJson(videoUrl)
                + "}";
    }
    private static String toIso(LocalDateTime t) {
        if (t == null) return null;
        return t.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
