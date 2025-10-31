// filepath: src/main/java/vn/iotstar/controllers/publics/PublicChatServlet.java

package vn.iotstar.controllers.publics;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import vn.iotstar.entities.Shop;
import vn.iotstar.services.StatisticService;

/**
 * Phòng chat công khai cho mọi người. Không ràng buộc quyền.
 */
@WebServlet(urlPatterns = {"/chat/public"})
public class PublicChatServlet extends HttpServlet {

    private final StatisticService stats = new StatisticService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String shopIdStr = req.getParameter("shopId");
        if (shopIdStr == null || shopIdStr.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu shopId");
            return;
        }

        Long shopId;
        try {
            shopId = Long.valueOf(shopIdStr);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "shopId không hợp lệ");
            return;
        }

        Shop shop = stats.findShopById(shopId);
        if (shop == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Shop không tồn tại");
            return;
        }

        req.setAttribute("shop", shop);
        req.getRequestDispatcher("/WEB-INF/views/public/shop-chat-public.jsp").forward(req, resp);
    }
}