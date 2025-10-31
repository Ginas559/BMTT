// filepath: src/main/java/vn/iotstar/controllers/vendor/VendorVoucherFormServlet.java
package vn.iotstar.controllers.vendor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import vn.iotstar.entities.Product;
import vn.iotstar.entities.Shop;
import vn.iotstar.entities.User;
import vn.iotstar.entities.Voucher;
import vn.iotstar.entities.Voucher.VoucherType;
import vn.iotstar.services.ProductService;
import vn.iotstar.services.VoucherService;
import vn.iotstar.services.StatisticService;
import vn.iotstar.utils.SessionUtil;

@WebServlet(urlPatterns = {
        "/vendor/vouchers/form",   // GET: tạo/sửa
        "/vendor/vouchers/new",    // GET: alias tạo mới
        "/vendor/vouchers/save"    // POST: lưu (create/update)
})
public class VendorVoucherFormServlet extends HttpServlet {

    private final VoucherService voucherService = new VoucherService();
    private final ProductService productService = new ProductService();
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

    	//test
    	System.out.println("===== [DEBUG - Voucher Edit] =====");
    	System.out.println("User ID: " + SessionUtil.currentUserId(req));
    	System.out.println("Role: " + SessionUtil.currentRole(req));
    	System.out.println("Session staffShopId: " + req.getSession().getAttribute("staffShopId"));
    	//end test
    	
        Shop shop = resolveShop(req);
        
        //test
        if (shop == null) {
            System.out.println("Resolved shop = NULL ❌");
        } else {
            System.out.println("Resolved shopId = " + shop.getShopId() + " ✅");
        }
		//end test
        
        if (shop == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không xác định được shop.");
            return;
        }
        req.setAttribute("shop", shop);

        // Hỗ trợ cả /form và /new
        String servletPath = req.getServletPath();
        boolean isNew = "/vendor/vouchers/new".equals(servletPath);

        Long id = isNew ? null : parseLong(req.getParameter("id"));
        Voucher v = (id != null) ? voucherService.findByIdForShop(id, shop.getShopId()) : null;
        if (id != null && v == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền xem voucher này.");
            return;
        }
        req.setAttribute("v", v);

        // Danh sách sản phẩm của shop để multi-select (chỉ khi PERCENT)
        req.setAttribute("products", productService.getByShopId(shop.getShopId()));

        if (v != null && v.getType() == VoucherType.PERCENT) {
            Set<Long> selectedIds = v.getVoucherProducts()
                    .stream().map(vp -> vp.getProduct().getProductId())
                    .collect(Collectors.toSet());
            req.setAttribute("selectedProductIds", selectedIds);
        }

        // Flash errors từ session (nếu có)
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("flashErrors") != null) {
            @SuppressWarnings("unchecked")
            List<String> errs = (List<String>) session.getAttribute("flashErrors");
            req.setAttribute("errors", errs);
            session.removeAttribute("flashErrors");

            copyIf(session, req, "v_id");
            copyIf(session, req, "v_code");
            copyIf(session, req, "v_type");
            copyIf(session, req, "v_percent");
            copyIf(session, req, "v_amount");
            copyIf(session, req, "v_minOrder");
            copyIf(session, req, "v_startAt");
            copyIf(session, req, "v_endAt");
            Object sel = session.getAttribute("selectedProductIds");
            if (sel instanceof Set<?>) req.setAttribute("selectedProductIds", sel);
            session.removeAttribute("selectedProductIds");
        }

        req.getRequestDispatcher("/WEB-INF/views/vendor/voucher-form.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();
        if (!"/vendor/vouchers/save".equals(path)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Shop shop = resolveShop(req);
        if (shop == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không xác định được shop.");
            return;
        }

        Long id = parseLong(req.getParameter("id"));
        try {
            if (id == null) {
                voucherService.createForShop(req, shop.getShopId());
            } else {
                Voucher v = voucherService.findByIdForShop(id, shop.getShopId());
                if (v == null) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền cập nhật voucher này.");
                    return;
                }
                voucherService.updateForShop(req, shop.getShopId());
            }
            resp.sendRedirect(req.getContextPath() + "/vendor/vouchers");
        } catch (RuntimeException ex) {
            HttpSession session = req.getSession(true);
            session.setAttribute("flashErrors", List.of(ex.getMessage()));

            session.setAttribute("v_id", req.getParameter("id"));
            session.setAttribute("v_code", req.getParameter("code"));
            session.setAttribute("v_type", req.getParameter("type"));
            session.setAttribute("v_percent", req.getParameter("percent"));
            session.setAttribute("v_amount", req.getParameter("amount"));
            session.setAttribute("v_minOrder", req.getParameter("minOrderAmount"));
            session.setAttribute("v_startAt", req.getParameter("startAt"));
            session.setAttribute("v_endAt", req.getParameter("endAt"));

            String[] productIds = req.getParameterValues("productIds");
            if (productIds != null) {
                Set<Long> sel = new HashSet<>();
                for (String s : productIds) {
                    try { sel.add(Long.valueOf(s)); } catch (Exception ignore) {}
                }
                session.setAttribute("selectedProductIds", sel);
            }

            String back = req.getContextPath() + "/vendor/vouchers/form";
            if (id != null) back += "?id=" + id;
            
            
            resp.sendRedirect(back);
        }
    }

    private static Long parseLong(String s) {
        try { return (s == null || s.isBlank()) ? null : Long.valueOf(s.trim()); }
        catch (Exception e) { return null; }
    }
    private static void copyIf(HttpSession ss, HttpServletRequest req, String name) {
        Object v = ss.getAttribute(name);
        if (v != null) { req.setAttribute(name, v); ss.removeAttribute(name); }
    }
}
