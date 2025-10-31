// filepath: src/main/java/vn/iotstar/controllers/vendor/VendorSaleServlet.java
package vn.iotstar.controllers.vendor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.persistence.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Product;
import vn.iotstar.entities.Shop;
import vn.iotstar.services.ProductSaleService;
import vn.iotstar.services.StatisticService;
import vn.iotstar.utils.SessionUtil;

@WebServlet(urlPatterns = {"/vendor/sales", "/vendor/sales/*"})
public class VendorSaleServlet extends HttpServlet {

    private final StatisticService helper = new StatisticService();
    private final ProductSaleService saleService = new vn.iotstar.services.ProductSaleService();


    /** Lấy shop theo user hiện tại (ưu tiên VENDOR) */
    private Shop resolveShop(HttpServletRequest req) {
        Long uid  = SessionUtil.currentUserId(req);
        String role = SessionUtil.currentRole(req);
        HttpSession ss = req.getSession(false);
        Long staffShopId = (ss != null && ss.getAttribute("staffShopId") != null)
                ? (Long) ss.getAttribute("staffShopId") : null;

        if ("VENDOR".equals(role)) {
            return helper.findShopByOwner(uid);
        }
        if ("STAFF".equals(role) && staffShopId != null) {
            return helper.findShopById(staffShopId);
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Shop shop = resolveShop(req);
        if (shop == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không xác định được shop.");
            return;
        }
        req.setAttribute("shop", shop);

        String path = req.getPathInfo();
        if (path == null || "/".equals(path) || "/new".equals(path)) {
            // Danh sách sản phẩm của shop (để chọn)
            EntityManager em = JPAConfig.getEntityManager();
            try {
                List<Product> products = em.createQuery(
                        "SELECT p FROM Product p WHERE p.shop.shopId = :sid ORDER BY p.productName",
                        Product.class)
                    .setParameter("sid", shop.getShopId())
                    .getResultList();
                req.setAttribute("products", products);

                // Nếu có ?productId=... từ trang list → pre-select
                Long prePid = parseLongOrNull(req.getParameter("productId"));
                req.setAttribute("productId", prePid);

            } finally {
                em.close();
            }
            req.getRequestDispatcher("/WEB-INF/views/vendor/sale-new.jsp").forward(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Shop shop = resolveShop(req);
        if (shop == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không xác định được shop.");
            return;
        }
        String path = req.getPathInfo();
        if (!"/create".equals(path)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Đọc input
        Long productId = parseLongOrNull(req.getParameter("productId"));
        BigDecimal percent = parseBig(req.getParameter("percent"));
        LocalDate start = parseDate(req.getParameter("startDate"));
        LocalDate end   = parseDate(req.getParameter("endDate"));

        List<String> errs = new ArrayList<>();
        if (productId == null) errs.add("Thiếu sản phẩm.");
        if (percent == null || percent.compareTo(BigDecimal.ZERO) <= 0 || percent.compareTo(new BigDecimal("90")) > 0)
            errs.add("Phần trăm giảm phải trong (0..90].");
        if (start == null || end == null) errs.add("Thiếu ngày bắt đầu/kết thúc.");
        if (start != null && end != null && end.isBefore(start)) errs.add("Ngày kết thúc phải ≥ ngày bắt đầu.");

        if (!errs.isEmpty()) {
            req.getSession(true).setAttribute("flashErrors", errs);
            resp.sendRedirect(req.getContextPath() + "/vendor/sales/new?productId=" + (productId != null ? productId : ""));
            return;
        }

        // Xác thực product thuộc shop
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Product p = em.find(Product.class, productId);
            if (p == null || p.getShop() == null || !Objects.equals(p.getShop().getShopId(), shop.getShopId())) {
                req.getSession(true).setAttribute("flashErrors", List.of("Sản phẩm không thuộc shop của bạn."));
                resp.sendRedirect(req.getContextPath() + "/vendor/sales/new");
                return;
            }

            tx.begin();
            // Tắt các sale ACTIVE đang trùng khoảng thời gian (đơn giản: deactivate hết sale ACTIVE của product)
            em.createNativeQuery("""
                UPDATE [Product_Sale]
                SET [status] = 'INACTIVE'
                WHERE product_id = :pid AND [status] = 'ACTIVE'
            """).setParameter("pid", productId).executeUpdate();

            // Thêm sale mới (type = PERCENT)
            LocalDateTime startAt = start.atStartOfDay();
            LocalDateTime endAt   = end.atTime(LocalTime.of(23, 59, 59));
            em.createNativeQuery("""
                INSERT INTO [Product_Sale] (shop_id, product_id, [type], percent_val, [amount], start_at, end_at, [status])
                VALUES (:sid, :pid, 'PERCENT', :pct, NULL, :s, :e, 'ACTIVE')
            """)
            .setParameter("sid", shop.getShopId())
            .setParameter("pid", productId)
            .setParameter("pct", percent)
            .setParameter("s", startAt)
            .setParameter("e", endAt)
            .executeUpdate();

            tx.commit();
            req.getSession(true).setAttribute("flash", "Đã tạo khuyến mãi cho sản phẩm #" + productId + " (" + percent + "%).");
        } catch (Exception ex) {
            if (tx.isActive()) tx.rollback();
            req.getSession(true).setAttribute("flashErrors", List.of("Lỗi tạo khuyến mãi: " + ex.getMessage()));
        } finally {
            em.close();
        }

        resp.sendRedirect(req.getContextPath() + "/vendor/sales/new?productId=" + productId);
    }

    private static Long parseLongOrNull(String s) {
        try { return (s == null || s.isBlank()) ? null : Long.valueOf(s); } catch (Exception e) { return null; }
    }
    private static BigDecimal parseBig(String s) {
        try { return (s == null || s.isBlank()) ? null : new BigDecimal(s.trim()); } catch (Exception e) { return null; }
    }
    private static LocalDate parseDate(String s) {
        try { return (s == null || s.isBlank()) ? null : LocalDate.parse(s.trim()); } catch (Exception e) { return null; }
    }
}
