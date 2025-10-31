// filepath: src/main/java/vn/iotstar/controllers/vendor/DeleteVariantServlet.java
package vn.iotstar.controllers.vendor;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import vn.iotstar.services.ProductVariantService;

@WebServlet(urlPatterns = {"/vendor/product/variant/delete"})
public class DeleteVariantServlet extends HttpServlet {
    private final ProductVariantService variantService = new ProductVariantService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = Long.parseLong(req.getParameter("id"));
        Long pid = Long.parseLong(req.getParameter("productId"));
        variantService.deleteVariant(id);
        resp.sendRedirect(req.getContextPath() + "/vendor/product/variants?productId=" + pid);
    }
}
