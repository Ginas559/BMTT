// filepath: src/main/java/vn/iotstar/controllers/vendor/VendorProductServlet.java
package vn.iotstar.controllers.vendor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import vn.iotstar.configs.CloudinaryConfig;
import vn.iotstar.entities.Product;
import vn.iotstar.entities.Shop;
import vn.iotstar.entities.User;
import vn.iotstar.services.CategoryService;
import vn.iotstar.services.ProductImageService;
import vn.iotstar.services.ProductSaleService;
import vn.iotstar.services.ProductService;
import vn.iotstar.services.StatisticService;
import vn.iotstar.utils.SessionUtil;
import vn.iotstar.services.ProductVariantService;

@WebServlet(urlPatterns = {"/vendor/products", "/vendor/products/*"})
@MultipartConfig(maxFileSize = 10 * 1024 * 1024)
public class VendorProductServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    private final ProductImageService productImageService = new ProductImageService();
    private final StatisticService helper = new StatisticService();
    private final ProductVariantService variantService = new ProductVariantService();
    private final ProductSaleService saleService = new vn.iotstar.services.ProductSaleService();

    
    /**
     * Lưu ý JWT:
     * - AuthFilter (JWT) đã gắn sẵn 3 thuộc tính vào request nếu token hợp lệ: uid, role, shopId.
     * - Ở đây ưu tiên dùng role/shopId từ JWT để xác định shop. Nếu không có JWT thì fallback session như cũ.
     */
    private Shop resolveShop(HttpServletRequest req) {
        // Ưu tiên JWT: lấy từ request attributes do AuthFilter gắn vào
        Object jwtRoleObj = req.getAttribute("role");
        Object jwtShopIdObj = req.getAttribute("shopId");
        String jwtRole = (jwtRoleObj instanceof String) ? (String) jwtRoleObj : null;
        Long jwtShopId = (jwtShopIdObj instanceof Long) ? (Long) jwtShopIdObj : null;

        // Trường hợp có JWT và là vendor: dùng shopId từ token để tìm đúng shop
        if ("VENDOR".equals(jwtRole) && jwtShopId != null) {
            return helper.findShopById(jwtShopId);
        }

        // Fallback session như logic cũ
        Long uid  = SessionUtil.currentUserId(req);
        String role = SessionUtil.currentRole(req);
        HttpSession ss = req.getSession(false);
        Long staffShopId = (ss != null && ss.getAttribute("staffShopId") != null)
                ? (Long) ss.getAttribute("staffShopId") : null;

        if ("VENDOR".equals(role)) {
            // Trường hợp này dành cho lối cũ dùng session
            return helper.findShopByOwner(uid);
        }
        // Hỗ trợ cả USER hoặc STAFF qua session (khi chưa dùng claim staff trong JWT)
        if ("USER".equals(role) || "STAFF".equals(role)) {
            Long sid = staffShopId;
            if (sid == null && ss != null) {
                Object cu = ss.getAttribute("currentUser");
                if (cu instanceof User u && u.getStaffShop() != null) {
                    sid = u.getStaffShop().getShopId();
                    ss.setAttribute("staffShopId", sid);
                }
            }
            return (sid != null) ? helper.findShopById(sid) : null;
        }
        return null;
    }

    private boolean ensureOwnedOr403(Shop shop, Product p, HttpServletResponse resp) throws IOException {
        if (p == null || shop == null || p.getShop() == null
                || !Objects.equals(p.getShop().getShopId(), shop.getShopId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền thao tác sản phẩm này.");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        Shop shop = resolveShop(req);
        if (shop == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không xác định được shop.");
            return;
        }
        req.setAttribute("shop", shop);

        // Flash errors (nếu có)
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("flashErrors") != null) {
            @SuppressWarnings("unchecked")
            List<String> errs = (List<String>) session.getAttribute("flashErrors");
            req.setAttribute("errors", errs);
            session.removeAttribute("flashErrors");
        }

        if (path == null || "/".equals(path)) {
            int page = parseIntOrDefault(req.getParameter("page"), 1);
            int size = parseIntOrDefault(req.getParameter("size"), 10);
            String q = trimToNull(req.getParameter("q"));
            Long categoryId = parseLongOrNull(req.getParameter("categoryId"));
            Product.ProductStatus status = parseStatusOrNull(req.getParameter("status"));

            // Page sản phẩm như cũ
            ProductService.PageResult<Product> pr = productService.findByShopPaged(
                    shop.getShopId(), page, size, q, categoryId, status);

            req.setAttribute("products", pr.items);
            req.setAttribute("totalPages", pr.totalPages);
            req.setAttribute("page", pr.page);
            req.setAttribute("size", pr.size);
            req.setAttribute("q", q);
            req.setAttribute("categoryId", categoryId);
            req.setAttribute("status", status != null ? status.name() : "");
            req.setAttribute("categories", categoryService.findAll());

            // Ảnh thumbnail
            Map<Long, String> thumbnails = new HashMap<>();
            for (Product p : pr.items) {
                thumbnails.put(p.getProductId(), productImageService.getThumbnailUrl(p.getProductId()));
            }
            req.setAttribute("thumbnails", thumbnails);

            // Tổng tồn (biến thể) + fallback product.stock
            Map<Long, Integer> stocks = new HashMap<>();
            for (Product p : pr.items) {
                int sum = variantService.sumStockByProductId(p.getProductId());
                if (sum == 0 && p.getStock() != null) sum = p.getStock();
                stocks.put(p.getProductId(), sum);
            }
            req.setAttribute("stocks", stocks);

            // ====== NEW: Map khuyến mãi đang hiệu lực + giá đã giảm ======
            // Thu thập productIds trong trang hiện tại
            List<Long> pids = new ArrayList<>();
            for (Product p : pr.items) pids.add(p.getProductId());

            // Gọi service khuyến mãi (bạn đã tạo ở bước 1)
            vn.iotstar.services.ProductSaleService saleService =
                    new vn.iotstar.services.ProductSaleService();

            // Lấy sale đang ACTIVE theo danh sách productIds
            Map<Long, vn.iotstar.services.ProductSaleService.SaleVM> saleMap =
                    saleService.activeByProductIds(pids);

            // Chuẩn bị các map đẩy sang JSP
            Map<Long, java.math.BigDecimal> salePercent = new HashMap<>();
            Map<Long, java.time.LocalDate>  saleEndDate = new HashMap<>();
            Map<Long, java.math.BigDecimal> discountedPrice = new HashMap<>();

            for (Product p : pr.items) {
                var s = saleMap.get(p.getProductId());
                if (s != null && p.getPrice() != null) {
                    salePercent.put(p.getProductId(), s.percent);   // ví dụ 10.00 nghĩa là -10%
                    saleEndDate.put(p.getProductId(), s.endDate);   // ngày kết thúc
                    // Tính giá đã giảm: price * (100 - percent) / 100
                    discountedPrice.put(p.getProductId(), saleService.discounted(p.getPrice(), s.percent));
                }
            }

            // Đẩy sang JSP (để hiển thị badge “-10%” và giá gạch/giá sau giảm)
            req.setAttribute("salePercent", salePercent);
            req.setAttribute("saleEndDate", saleEndDate);
            req.setAttribute("discountedPrice", discountedPrice);
            // ====== /NEW ======

            req.getRequestDispatcher("/WEB-INF/views/vendor/products.jsp").forward(req, resp);
            return;
        }

        if ("/edit".equals(path)) {
            Long id = parseLongOrNull(req.getParameter("id"));
            if (id == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu id");
                return;
            }
            Product p = productService.findById(id);
            if (!ensureOwnedOr403(shop, p, resp)) return;

            req.setAttribute("p", p);
            req.setAttribute("categories", categoryService.findAll());
            req.setAttribute("thumbEditing", productImageService.getThumbnailUrl(id));
            req.getRequestDispatcher("/WEB-INF/views/vendor/products.jsp").forward(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        Shop shop = resolveShop(req);
        if (shop == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không xác định được shop.");
            return;
        }

        if ("/add".equals(path)) {
            List<String> errors = new ArrayList<>();
            
            // --- BẮT ĐẦU PHẦN CHỈNH SỬA (BỎ KIỂM TRA TỒN KHO) ---
            String name = req.getParameter("name");
            String priceStr = req.getParameter("price");
            // String stockStr = req.getParameter("stock"); // <- KHÔNG dùng để validate nữa
            String catStr = req.getParameter("categoryId");

            if (name == null || name.trim().length() < 3) errors.add("Tên tối thiểu 3 ký tự.");
            try { new BigDecimal(priceStr); } catch (Exception e) { errors.add("Giá không hợp lệ."); }
            // try { Integer.parseInt(stockStr); } catch (Exception e) { errors.add("Tồn không hợp lệ."); } // <- bỏ
            try { Long.valueOf(catStr); } catch (Exception e) { errors.add("Danh mục không hợp lệ."); }
            // --- KẾT THÚC PHẦN CHỈNH SỬA ---

            Part part = req.getPart("image");
            String fileErr = validateImagePart(part);
            if (fileErr != null) errors.add(fileErr);

            if (!errors.isEmpty()) {
                flashAndBack(req, resp, errors);
                return;
            }

            Long productId;
            try {
                productId = productService.addForShop(req, shop.getShopId());
            } catch (RuntimeException ex) {
                errors.add(ex.getMessage());
                flashAndBack(req, resp, errors);
                return;
            }

            if (part != null && part.getSize() > 0) {
                String url = uploadToCloudinary(part);
                productImageService.addImage(productId, url, true);
            }
            resp.sendRedirect(req.getContextPath() + "/vendor/products");
            return;
        }

        if ("/update".equals(path)) {
            List<String> errors = new ArrayList<>();

            Long productId = parseLongOrNull(req.getParameter("productId"));
            if (productId == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu productId");
                return;
            }
            Product p = productService.findById(productId);
            if (!ensureOwnedOr403(shop, p, resp)) return;

            // Vẫn giữ validation cho stock ở nhánh /update (vì form có thể vẫn gửi lên)
            String name = req.getParameter("name");
            String priceStr = req.getParameter("price");
            String stockStr = req.getParameter("stock"); 
            if (name == null || name.trim().length() < 3) errors.add("Tên tối thiểu 3 ký tự.");
            try { new BigDecimal(priceStr); } catch (Exception e) { errors.add("Giá không hợp lệ."); }
            try { Integer.parseInt(stockStr); } catch (Exception e) { errors.add("Tồn không hợp lệ."); }

            Part part = req.getPart("image");
            if (part != null && part.getSize() > 0) {
                String fileErr = validateImagePart(part);
                if (fileErr != null) errors.add(fileErr);
            }

            if (!errors.isEmpty()) {
                flashAndBack(req, resp, errors);
                return;
            }

            try {
                productService.updateForShop(req, shop.getShopId());
            } catch (RuntimeException ex) {
                errors.add(ex.getMessage());
                flashAndBack(req, resp, errors);
                return;
            }

            if (part != null && part.getSize() > 0) {
                String url = uploadToCloudinary(part);
                productImageService.clearThumbnail(productId);
                productImageService.addImage(productId, url, true);
            }
            resp.sendRedirect(req.getContextPath() + "/vendor/products");
            return;
        }

        if ("/toggle".equals(path)) {
            Long id = parseLongOrNull(req.getParameter("id"));
            if (id == null) {
                writeJson(resp, 400, Map.of("ok", false, "message", "Thiếu id"));
                return;
            }
            try {
                Product.ProductStatus st = productService.toggleStatus(id, shop.getShopId());
                writeJson(resp, 200, Map.of("ok", true, "status", st.name()));
            } catch (SecurityException se) {
                writeJson(resp, 403, Map.of("ok", false, "message", "Không có quyền"));
            } catch (RuntimeException re) {
                writeJson(resp, 400, Map.of("ok", false, "message", re.getMessage()));
            }
            return;
        }

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
    private static int parseIntOrDefault(String s, int d) {
        try { return Integer.parseInt(s); } catch (Exception e) { return d; }
    }
    private static Long parseLongOrNull(String s) {
        try { return (s == null || s.isBlank()) ? null : Long.valueOf(s); } catch (Exception e) { return null; }
    }
    private static Product.ProductStatus parseStatusOrNull(String s) {
        try { return (s == null || s.isBlank()) ? null : Product.ProductStatus.valueOf(s); }
        catch (Exception e) { return null; }
    }

    private void flashAndBack(HttpServletRequest req, HttpServletResponse resp, List<String> errors) throws IOException {
        req.getSession(true).setAttribute("flashErrors", errors);
        resp.sendRedirect(req.getContextPath() + "/vendor/products");
    }

    private String validateImagePart(Part part) {
        if (part == null || part.getSize() == 0) return null;
        long size = part.getSize();
        if (size > (2L * 1024 * 1024)) return "Ảnh tối đa 2MB.";
        String fn = part.getSubmittedFileName();
        if (fn == null) return "Tệp ảnh không hợp lệ.";
        String lower = fn.toLowerCase();
        if (!(lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp"))) {
            return "Ảnh phải là jpg/jpeg/png/webp.";
        }
        String ct = part.getContentType();
        if (ct == null || !ct.startsWith("image/")) return "Content-Type ảnh không hợp lệ.";
        return null;
    }

    private String uploadToCloudinary(Part part) throws IOException {
        Cloudinary cloud = CloudinaryConfig.getCloudinary();
        try (InputStream is = part.getInputStream()) {
            byte[] data = is.readAllBytes();
            Map<?, ?> res = cloud.uploader().upload(
                data,
                ObjectUtils.asMap(
                    "folder", "shoes-shop/products",
                    "resource_type", "image"
                )
            );
            return String.valueOf(res.get("secure_url"));
        }
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
