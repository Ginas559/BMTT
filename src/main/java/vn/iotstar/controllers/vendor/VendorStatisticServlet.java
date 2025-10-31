// filepath: src/main/java/vn/iotstar/controllers/vendor/VendorStatisticServlet.java
package vn.iotstar.controllers.vendor;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import vn.iotstar.entities.Shop;
import vn.iotstar.services.StatisticService;
import vn.iotstar.utils.SessionUtil;

@WebServlet(urlPatterns = {"/vendor/statistics", "/vendor/statistics/view"})
public class VendorStatisticServlet extends HttpServlet {

    private final StatisticService statisticService = new StatisticService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String servletPath = req.getServletPath();
        if ("/vendor/statistics/view".equals(servletPath)) {
            req.setAttribute("pageTitle", "Thống kê");
            req.getRequestDispatcher("/WEB-INF/views/vendor/statistics.jsp").forward(req, resp);
            return;
        }

        resp.setContentType("application/json; charset=UTF-8");

        /*
         * Gợi nhớ về JWT: AuthFilter (JWT) đã gắn uid/role/shopId vào request nếu token hợp lệ.
         * 1) Ưu tiên dùng shopId từ JWT để tìm shop của vendor.
         * 2) Nếu không có JWT thì quay lại lối cũ dựa trên session (findShopByOwner).
         */
        Shop shop = null;
        Object jwtRoleObj = req.getAttribute("role");
        Object jwtShopIdObj = req.getAttribute("shopId");
        String jwtRole = (jwtRoleObj instanceof String) ? (String) jwtRoleObj : null;
        Long jwtShopId = (jwtShopIdObj instanceof Long) ? (Long) jwtShopIdObj : null;

        if ("VENDOR".equals(jwtRole) && jwtShopId != null) {
            shop = statisticService.findShopById(jwtShopId);
        } else {
            Long uid = SessionUtil.currentUserId(req);
            if (uid != null) {
                shop = statisticService.findShopByOwner(uid);
            }
        }

        if (shop == null) {
            resp.getWriter().write("{\"labels\":[],\"current\":[],\"previous\":[],\"values\":[],\"productRevenue\":[]}");
            return;
        }

        LocalDate from = parseDate(req.getParameter("from"));
        LocalDate to   = parseDate(req.getParameter("to"));
        Integer year   = parseInt(req.getParameter("year"));

        // Xác định khoảng thời gian cần thống kê
        LocalDateTime start = null, end = null, prevStart = null, prevEnd = null;
        boolean hasDateFilter = false;

        if (year != null) {
            hasDateFilter = true;
            start = LocalDate.of(year, 1, 1).atStartOfDay();
            end   = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
            prevStart = start.minusYears(1);
            prevEnd   = end.minusYears(1);
        } else if (from != null && to != null) {
            hasDateFilter = true;
            start = from.atStartOfDay();
            end   = to.plusDays(1).atStartOfDay().minusNanos(1);
            prevStart = start.minusYears(1);
            prevEnd   = end.minusYears(1);
        }

        Map<String, Object> series;
        List<Object[]> prodRows;

        if (hasDateFilter) {
            // Có lọc ngày/năm: lấy chuỗi dữ liệu theo khoảng thời gian chọn
            series = statisticService.buildMonthSeries(shop.getShopId(), start, end, prevStart, prevEnd);
            // Doanh thu theo sản phẩm trong khoảng lọc
            prodRows = statisticService.getProductRevenueBetween(shop.getShopId(), start, end);
        } else {
            // Không có lọc: mặc định theo năm hiện tại và sản phẩm trọn đời
            int currentYear = LocalDate.now().getYear();
            LocalDateTime defaultStart = LocalDate.of(currentYear, 1, 1).atStartOfDay();
            LocalDateTime defaultEnd   = LocalDate.of(currentYear, 12, 31).atTime(23, 59, 59);

            series = statisticService.buildMonthSeries(
                    shop.getShopId(),
                    defaultStart, defaultEnd,
                    defaultStart.minusYears(1), defaultEnd.minusYears(1)
            );

            prodRows = statisticService.getProductRevenueLifetime(shop.getShopId());
        }

        // Logging ngắn gọn để kiểm tra dữ liệu trả về từ service (dễ nhìn khi chạy dev)
        System.out.println("===== VendorStatisticServlet: product revenue rows =====");
        if (prodRows == null || prodRows.isEmpty()) {
            System.out.println("No product rows.");
        } else {
            for (Object[] row : prodRows) {
                System.out.printf("Product=%s | Total=%s%n", row[0], row[1]);
            }
        }
        System.out.println("========================================================");

        // Đóng gói dữ liệu sản phẩm thành JSON
        List<Map<String, Object>> productRevenue = new ArrayList<>();
        for (Object[] r : prodRows) {
            Map<String, Object> m = new HashMap<>();
            m.put("product", r[0] == null ? "(Không tên)" : r[0].toString());

            Object totalObj = r[1];
            BigDecimal total = (totalObj instanceof BigDecimal)
                    ? (BigDecimal) totalObj
                    : new BigDecimal(totalObj != null ? totalObj.toString() : "0");

            m.put("total", total);
            productRevenue.add(m);
        }
        series.put("productRevenue", productRevenue);

        // Gửi JSON
        resp.getWriter().write(gson.toJson(series));
    }

    private LocalDate parseDate(String s) {
        try { return (s == null || s.isBlank()) ? null : LocalDate.parse(s); }
        catch (Exception e) { return null; }
    }

    private Integer parseInt(String s) {
        try { return (s == null || s.isBlank()) ? null : Integer.valueOf(s); }
        catch (Exception e) { return null; }
    }
}
