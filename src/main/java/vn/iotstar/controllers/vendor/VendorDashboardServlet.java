// filepath: src/main/java/vn/iotstar/controllers/vendor/VendorDashboardServlet.java
package vn.iotstar.controllers.vendor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

import vn.iotstar.entities.Shop;
import vn.iotstar.entities.User;
import vn.iotstar.services.StatisticService;
import vn.iotstar.utils.SessionUtil;

@WebServlet(urlPatterns = {"/vendor/dashboard"})
public class VendorDashboardServlet extends HttpServlet {

    private final StatisticService statisticService = new StatisticService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        /*
         * Gợi nhớ về JWT:
         * - AuthFilter (JWT) đã gắn uid/role/shopId vào request nếu token hợp lệ.
         * - Ở đây ưu tiên dùng role/shopId từ JWT. Nếu không có, quay về cơ chế session như trước.
         */
        Shop shop = null;

        // Ưu tiên JWT
        Object jwtRoleObj = req.getAttribute("role");
        Object jwtShopIdObj = req.getAttribute("shopId");
        String jwtRole = (jwtRoleObj instanceof String) ? (String) jwtRoleObj : null;
        Long jwtShopId = (jwtShopIdObj instanceof Long) ? (Long) jwtShopIdObj : null;

        if ("VENDOR".equals(jwtRole) && jwtShopId != null) {
            shop = statisticService.findShopById(jwtShopId);
        } else {
            // Fallback session cho trường hợp chưa có JWT hoặc còn giữ cơ chế staff
            Long uid = SessionUtil.currentUserId(req);
            String role = SessionUtil.currentRole(req);
            HttpSession ss = req.getSession(false);
            Long staffShopId = (ss != null && ss.getAttribute("staffShopId") != null)
                    ? (Long) ss.getAttribute("staffShopId") : null;

            boolean isVendor = "VENDOR".equals(role);
            boolean isStaff = "USER".equals(role) && (staffShopId != null);

            if (uid == null || (!isVendor && !isStaff)) {
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            if (isVendor) {
                shop = statisticService.findShopByOwner(uid);
            } else {
                Long sid = staffShopId;
                if (sid == null && ss != null) {
                    Object cu = ss.getAttribute("currentUser");
                    if (cu instanceof User u && u.getStaffShop() != null) {
                        sid = u.getStaffShop().getShopId();
                        ss.setAttribute("staffShopId", sid);
                    }
                }
                if (sid != null) shop = statisticService.findShopById(sid);
            }
        }

        if (shop == null) {
            // Với vendor chưa có shop: điều hướng tạo shop; với staff: quay về trang chủ kèm thông báo
            if ("VENDOR".equals(jwtRole) || "VENDOR".equals(SessionUtil.currentRole(req))) {
                resp.sendRedirect(req.getContextPath() + "/vendor/shop/create");
            } else {
                req.getSession(true).setAttribute("error", "Bạn chưa có quyền vào khu quản lý shop.");
                resp.sendRedirect(req.getContextPath() + "/");
            }
            return;
        }

        // Lọc dữ liệu (tùy chọn)
        LocalDate from = parseDate(req.getParameter("from"));
        LocalDate to   = parseDate(req.getParameter("to"));
        Integer year   = parseInt(req.getParameter("year"));

        // Lấy dữ liệu tổng hợp cho dashboard
        Map<String, Object> stats      = statisticService.getDashboardData(shop.getShopId());
        var revenueRows   = statisticService.getRevenueByMonth(shop.getShopId(), year, from, to);
        var statusCounts  = statisticService.countOrdersByStatus(shop.getShopId(), from, to);
        var recentOrders  = statisticService.findRecentOrders(shop.getShopId(), 5);
        var mom           = statisticService.calcMonthOverMonthGrowth(shop.getShopId(), YearMonth.now());

        // Gắn dữ liệu cho JSP
        req.setAttribute("shop", shop);
        req.setAttribute("stats", stats);
        req.setAttribute("revenueRows", revenueRows);
        req.setAttribute("statusCounts", statusCounts);
        req.setAttribute("recentOrders", recentOrders);
        req.setAttribute("growthPercent", mom);
        req.setAttribute("filterFrom", from);
        req.setAttribute("filterTo", to);
        req.setAttribute("filterYear", year);

        req.getRequestDispatcher("/WEB-INF/views/vendor/dashboard.jsp").forward(req, resp);
    }

    private LocalDate parseDate(String s) {
        try {
            return (s == null || s.isBlank()) ? null : LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInt(String s) {
        try {
            return (s == null || s.isBlank()) ? null : Integer.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }
}
