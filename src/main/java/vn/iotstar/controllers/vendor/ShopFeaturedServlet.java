// src/main/java/vn/iotstar/controllers/vendor/ShopFeaturedServlet.java
package vn.iotstar.controllers.vendor;

import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import java.util.*;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import vn.iotstar.configs.CloudinaryConfig;
import vn.iotstar.services.ShopFeaturedService;
import vn.iotstar.services.StatisticService;
import vn.iotstar.entities.Shop;
import vn.iotstar.utils.SessionUtil;

@WebServlet(urlPatterns = {"/vendor/shop/featured", "/vendor/shop/featured/delete"})
@MultipartConfig(maxFileSize = 10 * 1024 * 1024)
public class ShopFeaturedServlet extends HttpServlet {
    private final ShopFeaturedService service = new ShopFeaturedService();
    private final StatisticService helper = new StatisticService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long uid = SessionUtil.currentUserId(req);
        Shop shop = helper.findShopByOwner(uid);
        if (shop == null) {
            resp.sendRedirect(req.getContextPath() + "/vendor/dashboard");
            return;
        }

        // Upload nhiều ảnh
        for (Part part : req.getParts()) {
            if ("featured[]".equals(part.getName()) && part.getSize() > 0) {
                String type = part.getContentType();
                if (type == null || !type.startsWith("image/")) continue;

                Cloudinary cloud = CloudinaryConfig.getCloudinary();
                try (InputStream is = part.getInputStream()) {
                    byte[] data = is.readAllBytes();
                    Map<?, ?> res = cloud.uploader().upload(data,
                            ObjectUtils.asMap("folder", "shoes-shop/products"));
                    String url = String.valueOf(res.get("secure_url"));
                    service.addImage(shop.getShopId(), url);
                }
            }
        }
        resp.sendRedirect(req.getContextPath() + "/vendor/shop");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long uid = SessionUtil.currentUserId(req);
        Shop shop = helper.findShopByOwner(uid);
        if (shop == null) {
            resp.sendRedirect(req.getContextPath() + "/vendor/dashboard");
            return;
        }

        Long id = Long.valueOf(req.getParameter("id"));
        service.delete(id, shop.getShopId());
        resp.sendRedirect(req.getContextPath() + "/vendor/shop");
    }
}
