package vn.iotstar.controllers.vendor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

import vn.iotstar.entities.Shop;
import vn.iotstar.entities.User;
import vn.iotstar.services.StatisticService;

@WebServlet(urlPatterns = {"/chat"})
public class VendorChatServlet extends HttpServlet {

    private final StatisticService stats = new StatisticService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String shopIdStr = req.getParameter("shopId");
        if (shopIdStr == null || shopIdStr.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu shopId");
            return;
        }

        Long shopId = Long.valueOf(shopIdStr);
        Shop shop = stats.findShopById(shopId);
        if (shop == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Shop không tồn tại");
            return;
        }

        HttpSession ss = req.getSession(false);
        if (ss == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = (String) ss.getAttribute("role");
        Long uid = (Long) ss.getAttribute("userId");
        Long staffShopId = (Long) ss.getAttribute("staffShopId");

        boolean isVendorOwner = "VENDOR".equals(role)
                && shop.getVendor() != null
                && uid != null
                && shop.getVendor().getId().equals(uid);

        boolean isStaffOfThisShop = "USER".equals(role)
                && staffShopId != null
                && staffShopId.equals(shopId);

        if (!(isVendorOwner || isStaffOfThisShop)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập phòng chat của shop này.");
            return;
        }

        req.setAttribute("shop", shop);
        req.getRequestDispatcher("/WEB-INF/views/vendor/shop-chat.jsp")
                .forward(req, resp);
    }
}
