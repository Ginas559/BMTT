// filepath: src/main/java/vn/iotstar/controllers/site/ShopCatalogServlet.java

package vn.iotstar.controllers.site;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import vn.iotstar.entities.Category;
import vn.iotstar.entities.Shop;
import vn.iotstar.services.ProductBrowseService;
import vn.iotstar.services.ShopService;

@WebServlet(urlPatterns = {"/vendor/*", "/vendor"})
public class ShopCatalogServlet extends HttpServlet {

    private ShopService shopService;
    private ProductBrowseService browseSvc;

    @Override
    public void init() throws ServletException {
        this.shopService = new ShopService();
        this.browseSvc = new ProductBrowseService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // --- Resolve shop by slug or ?shopId= ---
        String slug = extractSlug(req); // from /vendor/{slug}
        Long shopIdQuery = parseLong(req.getParameter("shopId"));
        Shop shop = shopService.findBySlugOrId(slug, shopIdQuery);

        if (shop == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Shop không tồn tại hoặc không khả dụng.");
            return;
        }
        Long shopId = shop.getShopId();

        // --- Nhận tham số filter giống /products ---
        int page = parseInt(req.getParameter("page"), 1);
        int size = parseInt(req.getParameter("size"), 12);
        String q = trimToNull(req.getParameter("q"));
        Long categoryId = parseLong(req.getParameter("catId"));
        String sort = trimToNull(req.getParameter("sort"));
        BigDecimal minPrice = parseBD(req.getParameter("minPrice"));
        BigDecimal maxPrice = parseBD(req.getParameter("maxPrice"));
        Integer minRating = parseIntObj(req.getParameter("minRating"));
        
        // ĐỌC CÁC THAM SỐ LỌC THUỘC TÍNH
        String brand  = trimToNull(req.getParameter("brand"));
        String gender = trimToNull(req.getParameter("gender"));
        String style  = trimToNull(req.getParameter("style"));
        String province= trimToNull(req.getParameter("province")); // NEW: Tham số tỉnh/thành

        // --- Gọi ProductBrowseService.page(...) có shopId để đồng bộ với /products ---
        var pageResult = browseSvc.page(
                q,                   // q
                categoryId,          // catId
                minPrice,            // minPrice
                maxPrice,            // maxPrice
                minRating,           // minRating
                shopId,              // shopId (lọc theo shop)
                brand, gender, style,     // filters
                province,            // NEW: Truyền province (đã được thêm vào ProductBrowseService)
                sort,                // sort
                page, size           // page, size
        );

        // --- Nạp categories + (tuỳ) danh sách shop cho datalist (để UI không trống) ---
        List<Category> categories = browseSvc.categories();
        var shops = browseSvc.shops(shop.getShopName());
        String shopQ = shop.getShopName();

        // --- Set attributes cho /WEB-INF/views/products/list.jsp ---
        req.setAttribute("pageTitle", shop.getShopName());
        req.setAttribute("page", pageResult);   // PageResult<ItemVM> có number/hasPrev/hasNext
        req.setAttribute("categories", categories);
        req.setAttribute("shops", shops);
        req.setAttribute("shopQ", shopQ);

        // giữ lại các param lọc để paginator không mất ngữ cảnh
        req.setAttribute("q", q);
        req.setAttribute("catId", categoryId);
        req.setAttribute("sort", sort);
        req.setAttribute("brand", brand);
        req.setAttribute("gender", gender);
        req.setAttribute("style", style);
        req.setAttribute("province", province);           // NEW: Giữ province
        req.setAttribute("minPrice", req.getParameter("minPrice"));
        req.setAttribute("maxPrice", req.getParameter("maxPrice"));
        req.setAttribute("minRating", req.getParameter("minRating"));

        // ngữ cảnh shop cho link/lọc
        req.setAttribute("shopId", shopId);
        req.setAttribute("shopSlug", slug);

        // --- Forward UI dùng chung ---
        req.getRequestDispatcher("/WEB-INF/views/products/list.jsp").forward(req, resp);
    }

    // =========================================================================
    // Private Utility Methods (Hàm tiện ích riêng tư)
    // =========================================================================

    private static String extractSlug(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || path.isBlank() || "/".equals(path)) return null;
        String s = path;
        if (s.startsWith("/")) s = s.substring(1);
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static int parseInt(String s, int defVal) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return defVal;
        }
    }

    private static Integer parseIntObj(String s) {
        try {
            return (s == null || s.isBlank()) ? null : Integer.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static Long parseLong(String s) {
        try {
            return (s == null || s.isBlank()) ? null : Long.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static BigDecimal parseBD(String s) {
        try {
            return (s == null || s.isBlank()) ? null : new BigDecimal(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}