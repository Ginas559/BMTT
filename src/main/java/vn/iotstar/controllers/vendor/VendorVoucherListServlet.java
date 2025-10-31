// filepath: src/main/java/vn/iotstar/controllers/vendor/VendorVoucherListServlet.java
package vn.iotstar.controllers.vendor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

import vn.iotstar.entities.Shop;
import vn.iotstar.entities.User;
import vn.iotstar.services.VoucherService;
import vn.iotstar.services.VoucherService.PageResult;
import vn.iotstar.entities.Voucher;
import vn.iotstar.services.StatisticService;
import vn.iotstar.utils.SessionUtil;

@WebServlet(urlPatterns = {"/vendor/vouchers"})
public class VendorVoucherListServlet extends HttpServlet {

    private final VoucherService voucherService = new VoucherService();
    private final StatisticService helper = new StatisticService();

    private Shop resolveShop(HttpServletRequest req) {
        final Long uid = SessionUtil.currentUserId(req);
        final String role = SessionUtil.currentRole(req);
        final HttpSession session = req.getSession(false);

        // 1) Vendor: shop do chính vendor sở hữu
        if (role != null && role.equalsIgnoreCase("VENDOR")) {
            return helper.findShopByOwner(uid);
        }

        // 2) Staff/User: ưu tiên staffShopId trong session
        Long staffShopId = null;
        if (session != null) {
            Object v = session.getAttribute("staffShopId");
            if (v instanceof Long) staffShopId = (Long) v;
        }
        if (staffShopId != null) {
            return helper.findShopById(staffShopId);
        }

        // 3) Thử lấy từ currentUser rồi cache
        if (session != null) {
            Object cu = session.getAttribute("currentUser");
            if (cu instanceof User u && u.getStaffShop() != null) {
                session.setAttribute("staffShopId", u.getStaffShop().getShopId());
                return u.getStaffShop();
            }
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

        int page = parseInt(req.getParameter("page"), 1);
        int size = parseInt(req.getParameter("size"), 10);
        String q       = trim(req.getParameter("q"));
        String type    = trim(req.getParameter("type"));   // PERCENT | AMOUNT | rỗng
        String status  = trim(req.getParameter("status")); // ACTIVE | INACTIVE | rỗng

        // GỌI ĐÚNG CHỮ KÝ 6 THAM SỐ
        PageResult<Voucher> pr = voucherService.findByShopPaged(
                shop.getShopId(), page, size, q, type, status
        );

        req.setAttribute("vouchers", pr.items);
        req.setAttribute("page", pr.page);
        req.setAttribute("size", pr.size);
        req.setAttribute("totalPages", pr.totalPages);
        req.setAttribute("q", q);
        req.setAttribute("type", type);
        req.setAttribute("status", status);

        req.getRequestDispatcher("/WEB-INF/views/vendor/vouchers.jsp").forward(req, resp);
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
    private static String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
