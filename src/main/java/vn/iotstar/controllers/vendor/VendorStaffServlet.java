// src/main/java/vn/iotstar/controllers/vendor/VendorStaffServlet.java
package vn.iotstar.controllers.vendor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

import vn.iotstar.entities.Shop;
import vn.iotstar.entities.User;
import vn.iotstar.services.StaffService;
import vn.iotstar.services.StatisticService;
import vn.iotstar.utils.SessionUtil;

@WebServlet(urlPatterns = {
        "/vendor/staffs",        // GET: list, POST: add (fallback)
        "/vendor/staffs/add",    // POST: add by email
        "/vendor/staffs/delete"  // POST: delete by userId
})
public class VendorStaffServlet extends HttpServlet {

    private final StaffService staffService = new StaffService();
    private final StatisticService helper = new StatisticService(); // đã dùng trong các servlet vendor khác

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long uid = SessionUtil.currentUserId(req);
        if (uid == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Shop shop = helper.findShopByOwner(uid);
        if (shop == null) {
            req.getSession().setAttribute("error", "Bạn chưa có cửa hàng.");
            resp.sendRedirect(req.getContextPath() + "/vendor/dashboard");
            return;
        }

        List<User> staffs = staffService.listStaff(shop.getShopId());
        req.setAttribute("shop", shop);
        req.setAttribute("staffs", staffs);
        req.getRequestDispatcher("/WEB-INF/views/vendor/staffs.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        Long uid = SessionUtil.currentUserId(req);
        if (uid == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Shop shop = helper.findShopByOwner(uid);
        if (shop == null) {
            req.getSession().setAttribute("error", "Bạn chưa có cửa hàng.");
            resp.sendRedirect(req.getContextPath() + "/vendor/dashboard");
            return;
        }

        try {
            if ("/vendor/staffs/add".equals(servletPath) || "/vendor/staffs".equals(servletPath)) {
                String email = req.getParameter("email");
                staffService.addStaffByEmail(uid, shop.getShopId(), email);
                req.getSession().setAttribute("flash", "Đã thêm nhân viên.");
            } else if ("/vendor/staffs/delete".equals(servletPath)) {
                Long userId = Long.valueOf(req.getParameter("userId"));
                staffService.removeStaff(uid, shop.getShopId(), userId);
                req.getSession().setAttribute("flash", "Đã xóa nhân viên.");
            }
        } catch (Exception e) {
            req.getSession().setAttribute("error", e.getMessage() == null ? "Thao tác thất bại." : e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/vendor/staffs");
    }
}
