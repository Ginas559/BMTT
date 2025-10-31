// filepath: src/main/java/vn/iotstar/controllers/vendor/VendorOrderServlet.java
package vn.iotstar.controllers.vendor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.util.List;

import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Order;
import vn.iotstar.entities.Shop;
import vn.iotstar.entities.User;
import vn.iotstar.services.OrderService;
import vn.iotstar.services.StatisticService;
import vn.iotstar.utils.SessionUtil;

@WebServlet(urlPatterns = {"/vendor/orders", "/vendor/orders/*"})
public class VendorOrderServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();
    private final StatisticService helper = new StatisticService();

    // === utils ===
    private static Long parseLong(String s) {
        try { return (s == null || s.isBlank()) ? null : Long.valueOf(s); }
        catch (Exception e) { return null; }
    }
    private static int pi(String s, int d) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return d; }
    }
    private static String escape(String s) {
        return s == null ? "" : s
                .replace("&","&amp;").replace("<","&lt;")
                .replace(">","&gt;").replace("\"","&quot;")
                .replace("'","&#x27;"); // ĐÃ SỬA: Bỏ dấu nháy đơn thừa
    }
    
    // ép về Integer an toàn
    private static Integer toInt(Object o){
        if (o == null) return null;
        if (o instanceof Number) return ((Number)o).intValue();
        try { return Integer.valueOf(String.valueOf(o)); } catch (Exception e) { return null; }
    }
    // ép về BigDecimal an toàn
    private static java.math.BigDecimal toBD(Object o){
        if (o == null) return java.math.BigDecimal.ZERO;
        if (o instanceof java.math.BigDecimal) return (java.math.BigDecimal)o;
        try { return new java.math.BigDecimal(String.valueOf(o)); } catch (Exception e){ return java.math.BigDecimal.ZERO; }
    }

    // Ưu tiên JWT shop; fallback session (giữ y nguyên logic cũ)
    private Shop resolveShop(HttpServletRequest req) {
        Object jwtRoleObj = req.getAttribute("role");
        Object jwtShopIdObj = req.getAttribute("shopId");
        String jwtRole = (jwtRoleObj instanceof String) ? (String) jwtRoleObj : null;
        Long jwtShopId = (jwtShopIdObj instanceof Long) ? (Long) jwtShopIdObj : null;

        if ("VENDOR".equals(jwtRole) && jwtShopId != null) {
            return helper.findShopById(jwtShopId);
        }

        Long uid  = SessionUtil.currentUserId(req);
        String role = SessionUtil.currentRole(req);
        HttpSession ss = req.getSession(false);
        Long staffShopId = (ss != null && ss.getAttribute("staffShopId") != null)
                ? (Long) ss.getAttribute("staffShopId") : null;

        if ("VENDOR".equals(role)) return helper.findShopByOwner(uid);

        if ("USER".equals(role)) {
            Long sid = staffShopId;
            if (sid == null && ss != null) {
                Object cu = ss.getAttribute("currentUser");
                if (cu instanceof User u && u.getStaffShop() != null) {
                    sid = u.getStaffShop().getShopId();
                    ss.setAttribute("staffShopId", sid);
                }
            }
            return (sid != null) ? helper.findShopById(sid) : null;
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

        String path = req.getPathInfo();

        // ===== AJAX: /vendor/orders/detail?orderId=...  → trả HTML fragment cho modal =====
        if ("/detail".equals(path)) {
            Long oid = parseLong(req.getParameter("orderId"));
            if (oid == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing orderId");
                return;
            }

            // ràng buộc quyền xem đơn
            Order o = orderService.findByIdForShop(oid, shop.getShopId());
            if (o == null) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền xem đơn hàng này.");
                return;
            }

            EntityManager em = JPAConfig.getEntityManager();
            try {
                // SỬ DỤNG NATIVE QUERY
                String sql =
                    "SELECT oi.order_id, oi.order_item_id, p.product_id, p.product_name, p.description, " +
                    "       p.price, p.discount_price, p.status, p.stock AS product_stock, " +
                    "       sa.brand, sa.material, sa.gender, sa.style, " +
                    "       v.variant_id, v.size, v.color, v.stock AS variant_stock, v.image_url, v.price_adjustment, " +
                    "       oi.quantity, oi.price AS unit_price, (oi.quantity * oi.price) AS line_total " +
                    "FROM Order_Item oi " +
                    "JOIN Product p ON p.product_id = oi.product_id " +
                    "LEFT JOIN Shoe_Attribute sa ON sa.product_id = p.product_id " +
                    "OUTER APPLY (SELECT TOP 1 v.* FROM Product_Variant v " +
                    "             WHERE v.product_id = p.product_id " +
                    "             ORDER BY CASE WHEN v.image_url IS NOT NULL THEN 0 ELSE 1 END, v.stock DESC, v.variant_id) v " +
                    "WHERE oi.order_id = ? " +
                    "ORDER BY oi.order_item_id";

                // Sử dụng createNativeQuery
                @SuppressWarnings("unchecked")
                List<Object[]> rows = em.createNativeQuery(sql)
                        .setParameter(1, oid)
                        .getResultList();

                if (rows == null || rows.isEmpty()) {
                    resp.setStatus(500);
                    resp.setContentType("text/html;charset=UTF-8");
                    resp.getWriter().write(
                        "<div class='alert alert-danger'>Không tải được chi tiết đơn hàng hoặc đơn hàng không có sản phẩm. Vui lòng thử lại.</div>");
                    return;
                }

                // build HTML với các cột mới và chỉ mục đã sửa
                StringBuilder sb = new StringBuilder();
                java.math.BigDecimal grand = java.math.BigDecimal.ZERO; // Khởi tạo tổng tiền

                sb.append("<div class='table-responsive'><table class='table table-sm align-middle'>")
                  .append("<thead><tr>")
                  .append("<th>Sản phẩm</th><th>Brand</th><th>Material</th><th>Gender</th><th>Style</th>")
                  .append("<th>Màu</th><th>Size</th>")
                  .append("<th class='text-end'>Tồn (variant)</th>")
                  .append("<th class='text-end'>Tồn (product)</th>")
                  .append("<th class='text-end'>SL</th><th class='text-end'>Đơn giá</th><th class='text-end'>Thành tiền</th>")
                  .append("<th>Ảnh</th>")
                  .append("</tr></thead><tbody>");

                for (Object[] r : rows) {
                    // CÁC CHỈ SỐ CỘT ĐÃ ĐƯỢC CẬP NHẬT CHÍNH XÁC
                    String name     = String.valueOf(r[3]);
                    String brand    = r[9]  == null ? "" : String.valueOf(r[9]);
                    String material = r[10] == null ? "" : String.valueOf(r[10]);
                    String gender   = r[11] == null ? "" : String.valueOf(r[11]);
                    String style    = r[12] == null ? "" : String.valueOf(r[12]);
                    Object sizeObj  = r[14];
                    String color    = r[15] == null ? "" : String.valueOf(r[15]);

                    Integer vStock  = toInt(r[16]);          // variant_stock (index 16)
                    Integer pStock  = toInt(r[8]);           // product_stock (index 8)
                    int qty         = toInt(r[19]) == null ? 0 : toInt(r[19]); // oi.quantity (index 19)

                    java.math.BigDecimal unit = toBD(r[20]); // unit_price (index 20)
                    java.math.BigDecimal line = toBD(r[21]); // line_total (index 21)
                    
                    String img       = r[17] == null ? "" : String.valueOf(r[17]);

                    // Logic kiểm tra thiếu hàng và class CSS
                    boolean lack = (vStock != null && vStock < qty) || (pStock != null && pStock < qty);
                    String lackCls = lack ? "text-danger fw-semibold" : "";

                    sb.append("<tr>")
                      .append("<td>").append(escape(name)).append("</td>")
                      .append("<td>").append(escape(brand)).append("</td>")
                      .append("<td>").append(escape(material)).append("</td>")
                      .append("<td>").append(escape(gender)).append("</td>")
                      .append("<td>").append(escape(style)).append("</td>")
                      .append("<td>").append(escape(color)).append("</td>")
                      .append("<td>").append(sizeObj == null || "null".equals(sizeObj) ? "-" : sizeObj).append("</td>")
                      .append("<td class='text-end ").append(lackCls).append("'>").append(vStock == null ? "-" : vStock).append("</td>")
                      .append("<td class='text-end ").append(lackCls).append("'>").append(pStock == null ? "-" : pStock).append("</td>")
                      .append("<td class='text-end'>").append(qty).append("</td>")
                      .append("<td class='text-end'>").append(unit).append("</td>")
                      .append("<td class='text-end'>").append(line).append("</td>")
                      .append("<td>")
                        .append(img.isEmpty() || "null".equals(img) ? "" :
                            "<img src='" + escape(img) + "' style='width:56px;height:56px;object-fit:cover' onerror=\"this.style.display='none'\">")
                      .append("</td>")
                      .append("</tr>");

                    if (line != null) grand = grand.add(line);
                }
                
                // Hàng tổng cộng (Footer)
                sb.append("</tbody>")
                  .append("<tfoot><tr>")
                  .append("<th colspan='11' class='text-end'>Tổng cộng:</th>")
                  .append("<th class='text-end'>").append(grand).append("</th>")
                  .append("<th></th>")
                  .append("</tr></tfoot>")
                  .append("</table></div>");


                resp.setContentType("text/html;charset=UTF-8");
                resp.getWriter().write(sb.toString());
                return;
            } finally {
                em.close();
            }
        }

        // ===== Render list + phân trang (Giữ nguyên) =====
        String status = req.getParameter("status");
        String q = req.getParameter("q");
        int page = pi(req.getParameter("page"), 1);
        int size = pi(req.getParameter("size"), 20);

        OrderService.PageResult<Order> pr =
                orderService.findByShopPaged(shop.getShopId(), status, q, page, size);

        req.setAttribute("orders", pr.items);
        req.setAttribute("page", pr.page);
        req.setAttribute("size", pr.size);
        req.setAttribute("totalPages", pr.totalPages);
        req.setAttribute("totalItems", pr.totalItems);
        req.setAttribute("status", status);
        req.setAttribute("q", q);

        // flash
        HttpSession ss = req.getSession(false);
        if (ss != null) {
            String flashType = (String) ss.getAttribute("flashType");
            String flashMsg  = (String) ss.getAttribute("flashMsg");
            if (flashType != null && flashMsg != null) {
                req.setAttribute("flashType", flashType);
                req.setAttribute("flashMsg",  flashMsg);
                ss.removeAttribute("flashType");
                ss.removeAttribute("flashMsg");
            }
        }

        req.getRequestDispatcher("/WEB-INF/views/vendor/orders.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Shop shop = resolveShop(req);
        if (shop == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không xác định được shop.");
            return;
        }

        Long orderId = parseLong(req.getParameter("orderId"));
        if (orderId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu hoặc sai orderId");
            return;
        }
        String newStatus = req.getParameter("newStatus");

        Order o = orderService.findByIdForShop(orderId, shop.getShopId());
        if (o == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền cập nhật đơn này.");
            return;
        }

        HttpSession ss = req.getSession(true);

        // Kiểm kho khi chuyển CONFIRMED (logic cũ, chỉ dựa Product.stock)
        if ("CONFIRMED".equalsIgnoreCase(newStatus)) {
            EntityManager em = JPAConfig.getEntityManager();
            try {
                boolean outOfStock = false;

                // Fallback hai dạng bảng chi tiết
                List<Object[]> items = null;
                try {
                    items = em.createQuery(
                        "SELECT d.product.productId, d.quantity FROM OrderDetail d WHERE d.order.orderId=:oid",
                        Object[].class).setParameter("oid", orderId).getResultList();
                } catch (Exception ignore) {}
                if (items == null) {
                    try {
                        items = em.createQuery(
                            "SELECT i.product.productId, i.quantity FROM OrderItem i WHERE i.order.orderId=:oid",
                            Object[].class).setParameter("oid", orderId).getResultList();
                    } catch (Exception ignore) {}
                }
                if (items == null) {
                    items = em.createQuery(
                        "SELECT i.product.productId, i.qty FROM OrderItem i WHERE i.order.orderId=:oid",
                        Object[].class).setParameter("oid", orderId).getResultList();
                }

                for (Object[] r : items) {
                    Long pid = ((Number) r[0]).longValue();
                    int qty  = ((Number) r[1]).intValue();

                    // Vẫn đang kiểm tra stock của Product, không kiểm tra Variant
                    Integer stock = em.createQuery(
                        "SELECT p.stock FROM Product p WHERE p.productId=:pid", Integer.class)
                        .setParameter("pid", pid)
                        .getSingleResult();

                    if (stock == null || stock < qty) { outOfStock = true; break; }
                }

                if (outOfStock) {
                    ss.setAttribute("flashType", "warning");
                    ss.setAttribute("flashMsg",  "Không thể xác nhận: có sản phẩm hết hàng hoặc số lượng tồn không đủ!");
                    resp.sendRedirect(req.getContextPath() + "/vendor/orders?page=" + req.getParameter("page"));
                    return;
                }
            } finally {
                em.close();
            }
        }

        // Update status
        orderService.updateStatus(orderId, newStatus);
        ss.setAttribute("flashType", "success");
        ss.setAttribute("flashMsg",  "Cập nhật trạng thái đơn hàng thành công.");

        resp.sendRedirect(req.getContextPath() + "/vendor/orders?page=" + req.getParameter("page")
                + (req.getParameter("status")!=null ? "&status="+req.getParameter("status") : "")
                + (req.getParameter("q")!=null ? "&q="+req.getParameter("q") : ""));
    }
}