package vn.iotstar.controllers.guest;

import vn.iotstar.services.ViewedService;
import vn.iotstar.services.ProductBrowseService;
import vn.iotstar.entities.Product;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.lang.reflect.Method;

public class RecentViewedServlet extends HttpServlet {

    private final ViewedService viewedSvc = new ViewedService();
    private final ProductBrowseService browseSvc = new ProductBrowseService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Long userId = extractUserIdFromSession(req.getSession(false));

        if (userId == null) {
            // Chưa đăng nhập: điều hướng về trang sản phẩm
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        // Lấy TOÀN BỘ lịch sử đã xem (limit = 0 => no-limit)
        List<Product> recent = viewedSvc.recentByUser(userId, 0);

        // Giữ attribute cũ cho an toàn (nếu JSP hiện tại đang dùng)
        req.setAttribute("recentProducts", recent);

        // Build ViewModel có coverUrl để JSP hiển thị ảnh thật
        List<RecentItemVM> items = new ArrayList<>();
        if (recent != null) {
            for (Product p : recent) {
                if (p == null) continue;
                String cover = resolveThumbnailUrl(p);
                items.add(new RecentItemVM(
                        p.getProductId(),
                        safe(p.getProductName()),
                        cover,
                        p.getPrice(),
                        p.getDiscountPrice()
                ));
            }
        }
        req.setAttribute("recentItems", items);
        req.setAttribute("pageTitle", "Đã xem gần đây");

        // Forward tới JSP hiển thị
        req.getRequestDispatcher("/WEB-INF/views/products/recent.jsp").forward(req, resp);
    }

    /** Lấy URL thumbnail: ưu tiên isThumbnail=true, fallback phần tử đầu; sửa "/assset/" -> "/assets/". */
    private String resolveThumbnailUrl(Product pr) {
        try {
            List<?> imgs = browseSvc.imagesOf(pr);
            if (imgs == null || imgs.isEmpty()) return "";

            Object prefer = null;
            for (Object o : imgs) {
                if (o == null) continue;
                // Trường hợp List<String>
                if (o instanceof String) {
                    if (prefer == null) prefer = o; // tạm giữ; sẽ break ở cuối vòng nếu không có isThumbnail
                } else {
                    // Thử getIsThumbnail()
                    try {
                        Method mIsThumb = o.getClass().getMethod("getIsThumbnail");
                        Object v = mIsThumb.invoke(o);
                        if (v instanceof Boolean && (Boolean) v) {
                            prefer = o;
                            break;
                        }
                    } catch (NoSuchMethodException ignore) {
                        // không có cờ thumbnail
                    } catch (Exception ignore) {}
                    if (prefer == null) prefer = o; // giữ first non-null
                }
            }

            String raw;
            if (prefer instanceof String) {
                raw = (String) prefer;
            } else {
                try {
                    Method mUrl = prefer.getClass().getMethod("getImageUrl");
                    Object url = mUrl.invoke(prefer);
                    raw = (url != null) ? url.toString() : "";
                } catch (Exception e) {
                    Object first = imgs.get(0);
                    raw = (first instanceof String) ? (String) first : "";
                }
            }
            if (raw == null) raw = "";
            return raw.replace("/assset/", "/assets/");
        } catch (Exception e) {
            return "";
        }
    }

    private String safe(String s){ return (s == null) ? "" : s; }

    // ==== Helpers: giống ProductBrowseServlet ====
    private Long extractUserIdFromSession(HttpSession session){
        if (session == null) return null;

        // 1) userId trực tiếp
        Object direct = safeGet(session, "userId");
        Long id = castToLong(direct);
        if (id != null) return id;

        // 2) các object user phổ biến
        String[] keys = new String[]{"currentUser","loginUser","user","account","customer","authUser"};
        for (String k : keys) {
            Object v = safeGet(session, k);
            id = reflectId(v);
            if (id != null) return id;
        }

        // 3) fallback: duyệt tất cả attributes
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

    private Object safeGet(HttpSession s, String key){
        try { return s.getAttribute(key); } catch (Exception e){ return null; }
    }
    private Long castToLong(Object v){
        try {
            if (v instanceof Long) return (Long) v;
            if (v instanceof Integer) return ((Integer) v).longValue();
        } catch (Exception ignore){}
        return null;
    }
    private Long reflectId(Object obj){
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

    /** ViewModel cho trang "Xem tất cả" */
    public static class RecentItemVM {
        private final Long productId;
        private final String productName;
        private final String coverUrl;
        private final java.math.BigDecimal price;
        private final java.math.BigDecimal discountPrice;

        public RecentItemVM(Long productId, String productName, String coverUrl,
                            java.math.BigDecimal price, java.math.BigDecimal discountPrice) {
            this.productId = productId;
            this.productName = productName == null ? "" : productName;
            this.coverUrl = coverUrl == null ? "" : coverUrl;
            this.price = price;
            this.discountPrice = discountPrice;
        }

        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getCoverUrl() { return coverUrl; }
        public java.math.BigDecimal getPrice() { return price; }
        public java.math.BigDecimal getDiscountPrice() { return discountPrice; }
    }
}
