// filepath: src/main/java/vn/iotstar/controllers/vendor/ShoeAttributeServlet.java
package vn.iotstar.controllers.vendor;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import vn.iotstar.entities.Product;
import vn.iotstar.entities.ShoeAttribute;
import vn.iotstar.services.ProductService;
import vn.iotstar.services.ShoeAttributeService;

@WebServlet(urlPatterns = {"/vendor/attribute"})
public class ShoeAttributeServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final ShoeAttributeService attrService = new ShoeAttributeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Long pid = Long.parseLong(req.getParameter("productId"));
        Product product = productService.findById(pid);
        ShoeAttribute attr = attrService.findByProduct(product);

        req.setAttribute("product", product);
        req.setAttribute("attr", attr);
        req.getRequestDispatcher("/WEB-INF/views/vendor/shoe-attribute.jsp")
            .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Long pid = Long.parseLong(req.getParameter("productId"));
        Product product = productService.findById(pid);

        ShoeAttribute attr = attrService.findByProduct(product);
        if (attr == null) attr = new ShoeAttribute();

        attr.setProduct(product);
        attr.setBrand(req.getParameter("brand"));
        attr.setMaterial(req.getParameter("material"));
        attr.setGender(req.getParameter("gender"));
        attr.setStyle(req.getParameter("style"));

        attrService.saveOrUpdate(attr);

        resp.sendRedirect(req.getContextPath() + "/vendor/attribute?productId=" + pid);
    }
}
