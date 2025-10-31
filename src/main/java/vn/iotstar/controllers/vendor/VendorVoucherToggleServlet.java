// filepath: src/main/java/vn/iotstar/controllers/vendor/VendorVoucherToggleServlet.java
package vn.iotstar.controllers.vendor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

import vn.iotstar.entities.Shop;
import vn.iotstar.entities.User;
import vn.iotstar.entities.Voucher;
import vn.iotstar.services.VoucherService;
import vn.iotstar.services.StatisticService;
import vn.iotstar.utils.SessionUtil;

@WebServlet(urlPatterns = {"/vendor/vouchers/toggle"})
public class VendorVoucherToggleServlet extends HttpServlet {

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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        Shop shop = resolveShop(req);
        if (shop == null) {
            writeJson(resp, 403, Map.of("ok", false, "message", "Không xác định được shop."));
            return;
        }

        Long id = parseLong(req.getParameter("id"));
        if (id == null) {
            writeJson(resp, 400, Map.of("ok", false, "message", "Thiếu id"));
            return;
        }

        try {
            // ✅ Dùng Voucher.Status, KHÔNG dùng VoucherStatus
            Voucher.Status st = voucherService.toggleStatus(id, shop.getShopId());
            writeJson(resp, 200, Map.of("ok", true, "status", st.name()));
        } catch (SecurityException se) {
            writeJson(resp, 403, Map.of("ok", false, "message", "Không có quyền"));
        } catch (RuntimeException re) {
            writeJson(resp, 400, Map.of("ok", false, "message", re.getMessage()));
        }
    }

    private Long parseLong(String s) {
        try { return (s == null || s.isBlank()) ? null : Long.valueOf(s.trim()); }
        catch (Exception e) { return null; }
    }

    private void writeJson(HttpServletResponse resp, int status, Map<String, ?> body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, ?> e : body.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(e.getKey()).append('"').append(':');
            Object v = e.getValue();
            if (v == null) sb.append("null");
            else if (v instanceof Number || v instanceof Boolean) sb.append(v.toString());
            else sb.append('"').append(escapeJson(v.toString())).append('"');
        }
        sb.append('}');
        resp.getWriter().write(sb.toString());
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
