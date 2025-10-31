// src/main/java/vn/iotstar/controllers/site/VendorDirectoryServlet.java
package vn.iotstar.controllers.site;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;
import java.util.List;

import vn.iotstar.entities.Shop;
import vn.iotstar.services.ShopService;

@WebServlet(urlPatterns = {"/vendors"})
public class VendorDirectoryServlet extends HttpServlet {

    private ShopService shopService;

    @Override
    public void init() throws ServletException {
        this.shopService = new ShopService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Params: page,size,q
        int page = parseInt(req.getParameter("page"), 1);
        int size = parseInt(req.getParameter("size"), 12);
        String q = trimToNull(req.getParameter("q"));

        // Data
        List<Shop> shops = shopService.listPublicShops(page, size, q);
        long total = shopService.countPublicShops(q);
        int totalPages = (int) Math.max(1, (total + size - 1) / size);

        // Attr
        req.setAttribute("shops", shops);
        req.setAttribute("q", q);
        req.setAttribute("page_number", page);
        req.setAttribute("page_size", size);
        req.setAttribute("total_pages", totalPages);
        req.setAttribute("total_items", total);

        // Forward view
        req.getRequestDispatcher("/WEB-INF/views/public/vendors.jsp").forward(req, resp);
    }

    private static int parseInt(String s, int defVal) {
        try { return Integer.parseInt(s); } catch (Exception e) { return defVal; }
    }
    private static String trimToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
}
