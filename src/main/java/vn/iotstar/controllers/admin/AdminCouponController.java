package vn.iotstar.controllers.admin;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.entities.Coupon;
import vn.iotstar.entities.Shop;
import vn.iotstar.services.admin.AdminCouponService;
import vn.iotstar.services.admin.AdminShopService;

@WebServlet(urlPatterns = {"/admin/coupons", "/admin/coupons/add", "/admin/coupons/edit", "/admin/coupons/delete"})
public class AdminCouponController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Giả định bạn đã khởi tạo các Service này (ví dụ: qua Dependency Injection hoặc khởi tạo thủ công)
    private AdminCouponService couponService = new AdminCouponService(); 
    private AdminShopService shopService = new AdminShopService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String url = request.getRequestURI();

        // 🟩 Trang danh sách Coupon (list.jsp)
        if (url.endsWith("/admin/coupons")) {
            handleCouponList(request, response);
            
        // 🟨 Trang thêm mới Coupon (add.jsp)
        } else if (url.endsWith("/admin/coupons/add")) {
//            handleCouponAddView(request, response);
            
        // 🟦 Trang chỉnh sửa Coupon (edit.jsp)
        } else if (url.endsWith("/admin/coupons/edit")) {
//             handleCouponEditView(request, response);
        }
        // ... Các xử lý GET khác (ví dụ: delete confirmation page nếu cần)
    }
    
    // --- PRIVATE HANDLERS ---
    
    /**
     * Xử lý hiển thị danh sách Coupon với tìm kiếm, lọc và phân trang.
     */
    private void handleCouponList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // --- 1. Lấy và Xử lý Tham số Tìm kiếm và Lọc ---
        
        String keyword = request.getParameter("keyword");
        
        // Lọc theo Shop
        String shopIdParam = request.getParameter("shopId");
        Shop shopFilter = null;
        
        if (shopIdParam != null && !shopIdParam.isEmpty()) {
            try {
                // Lấy đối tượng Shop dựa trên ID để truyền vào Service
                long shopId = Long.parseLong(shopIdParam);
                shopFilter = shopService.findById(shopId); 
            } catch (NumberFormatException e) {
                // Bỏ qua nếu ID không hợp lệ
            }
        }

        // --- 2. Xử lý Phân trang ---
        
        String pageParam = request.getParameter("page");
        int currentPage = 1;
        int pageSize = 10; // Kích thước trang mặc định

        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                currentPage = Integer.parseInt(pageParam);
            } catch (NumberFormatException e) {
                currentPage = 1;
            }
        }
        
        // --- 3. Gọi Service để lấy dữ liệu ---
        
        // Lấy danh sách Coupon (có tìm kiếm + lọc + phân trang)
        List<Coupon> coupons = couponService.searchCoupons(keyword, shopFilter, currentPage, pageSize);
        
        // Lấy tổng số Coupon (để tính tổng số trang)
        int totalCoupons = couponService.countCoupons(keyword, shopFilter);
        int totalPages = (int) Math.ceil((double) totalCoupons / pageSize);
        
        // Lấy danh sách tất cả Shops (để đổ vào bộ lọc trên JSP)
        List<Shop> allShops = shopService.findAllShopsValidate(); 
        
        // --- 4. Đặt thuộc tính và Chuyển hướng ---

        request.setAttribute("coupons", coupons);
        
        // Dữ liệu cho bộ lọc
        request.setAttribute("shops", allShops);
        request.setAttribute("keyword", keyword); // Giữ lại giá trị tìm kiếm
        
        // Dữ liệu phân trang
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);

        // Chuyển hướng đến trang JSP quản lý Coupon
        request.getRequestDispatcher("/WEB-INF/views/admin/coupons/list.jsp").forward(request, response);
    }
}
