// filepath: src/main/java/vn/iotstar/controllers/vendor/ProductVariantServlet.java
package vn.iotstar.controllers.vendor;

import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import jakarta.persistence.EntityManager;
import vn.iotstar.configs.JPAConfig;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import vn.iotstar.configs.CloudinaryConfig;   // ✅ dùng chung config như trang sản phẩm
import vn.iotstar.entities.Product;
import vn.iotstar.entities.ProductVariant;
import vn.iotstar.services.ProductService;
import vn.iotstar.services.ProductVariantService;

@WebServlet(urlPatterns = {"/vendor/product/variants"})
@MultipartConfig(maxFileSize = 2 * 1024 * 1024) // 2MB
public class ProductVariantServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final ProductVariantService variantService = new ProductVariantService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long pid = Long.parseLong(req.getParameter("productId"));
        Product product = productService.findById(pid);
        req.setAttribute("product", product);
        req.setAttribute("variants", variantService.listByProduct(product));
        req.getRequestDispatcher("/WEB-INF/views/vendor/product-variant.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Long pid = Long.parseLong(req.getParameter("productId"));
        Product product = productService.findById(pid);

        ProductVariant v = new ProductVariant();
        v.setProduct(product);
        v.setSize(req.getParameter("size"));
        v.setColor(req.getParameter("color"));

        String stockStr = req.getParameter("stock");
        v.setStock((stockStr == null || stockStr.isBlank()) ? 0 : Integer.parseInt(stockStr));

        // ✅ Upload ảnh giống cách bạn làm ở VendorProductServlet (commit a589a83)
        Part part = req.getPart("variantImage");
        if (part != null && part.getSize() > 0) {
            v.setImageUrl(uploadToCloudinary(part, "shoes-shop/variants/" + pid));
        }

        variantService.addVariant(v);
        resp.sendRedirect(req.getContextPath() + "/vendor/product/variants?productId=" + pid);
    }

    /** Upload theo pattern: CloudinaryConfig.getCloudinary() + đọc byte[] trước khi upload. */
    private String uploadToCloudinary(Part part, String folder) throws IOException {
        Cloudinary cloud = CloudinaryConfig.getCloudinary();
        try (InputStream is = part.getInputStream()) {
            byte[] data = is.readAllBytes(); // fix lỗi stream như commit trước
            @SuppressWarnings("rawtypes")
            Map res = cloud.uploader().upload(data, ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image"
            ));
            return String.valueOf(res.get("secure_url"));
        }
    }
    
    /** Tổng tồn của mọi biến thể thuộc productId (nếu chưa có biến thể => 0). */
    public int sumStockByProductId(Long productId) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            Integer total = em.createQuery(
                    "SELECT COALESCE(SUM(v.stock),0) FROM ProductVariant v WHERE v.product.productId = :pid",
                    Integer.class)
                .setParameter("pid", productId)
                .getSingleResult();
            return (total != null) ? total : 0;
        } finally {
            em.close();
        }
    }
}
