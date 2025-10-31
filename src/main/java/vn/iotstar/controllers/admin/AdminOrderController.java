package vn.iotstar.controllers.admin;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.entities.Category;
import vn.iotstar.entities.Order;
import vn.iotstar.entities.OrderItem;
import vn.iotstar.entities.Order.OrderStatus;
import vn.iotstar.entities.Shop;
import vn.iotstar.entities.Shop.ShopStatus;
import vn.iotstar.services.admin.AdminCategoryService;
import vn.iotstar.services.admin.AdminOrderService;
import vn.iotstar.services.admin.AdminShopService;
import vn.iotstar.services.admin.UserService;

import java.io.IOException;
import java.util.List;

/**
 * Servlet Filter implementation class AdminOrderController
 */
@WebServlet(urlPatterns = { "/admin/orders", "/admin/orders/details"})
public class AdminOrderController extends HttpServlet {
       
	private static final long serialVersionUID = 1L;

	private AdminShopService shopService = new AdminShopService();
	private AdminCategoryService categoryService = new AdminCategoryService();
	private AdminOrderService orderService = new AdminOrderService();
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

	    String url = request.getRequestURI();

	    // 🟩 Trang danh sách Đơn hàng
	    if (url.endsWith("/admin/orders")) {

	        // --- 1. Lấy và Xử lý Tham số Tìm kiếm và Lọc ---
	        
	        // Lọc theo Trạng thái (OrderStatus)
	        String statusParam = request.getParameter("status");
	        OrderStatus status = null;
	        if (statusParam != null && !statusParam.isEmpty()) {
	            try {
	                // Giả định OrderStatus là một Enum
	                status = OrderStatus.valueOf(statusParam.toUpperCase());
	            } catch (IllegalArgumentException e) {
	                status = null; // Trạng thái không hợp lệ
	            }
	        }
	        
	        // Lọc theo Shop
	        String shopIdParam = request.getParameter("shopId");
	        Shop shopFilter = null;
	        if (shopIdParam != null && !shopIdParam.isEmpty()) {
	            try {
	                // Giả định shopService có method findById(long id)
	                shopFilter = shopService.findById(Long.parseLong(shopIdParam));
	            } catch (NumberFormatException | NullPointerException e) {
	                shopFilter = null; 
	            }
	        }
	        
	        // Lọc theo Category
	        String categoryIdParam = request.getParameter("categoryId");
	        Category categoryFilter = null;
	        if (categoryIdParam != null && !categoryIdParam.isEmpty()) {
	            try {
	                // Giả định categoryService có method findById(long id)
	                categoryFilter = categoryService.findById(Long.parseLong(categoryIdParam));
	            } catch (NumberFormatException | NullPointerException e) {
	                categoryFilter = null;
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

	        // Lấy danh sách đơn hàng (có lọc + phân trang)
	        // Giả định orderService có hàm searchOrders với signature tương tự yêu cầu
	        List<Order> orders = orderService.searchOrders(status, shopFilter, currentPage, pageSize);
	        
	        // Lấy tổng số đơn hàng (để tính tổng số trang)
	        int totalOrders = orderService.countOrders(status, shopFilter);
	        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);
	        
	        // Lấy danh sách Shops và Categories (để đổ vào bộ lọc trên JSP)
	        List<Shop> allShops = shopService.findAllShopsValidate(); // Lấy tất cả Shops
	        List<Category> allCategories = categoryService.findAllCategoriesValidate(); // Lấy tất cả Categories
	        
	        // --- 4. Đặt thuộc tính và Chuyển hướng ---

	        request.setAttribute("orders", orders);
	        
	        // Dữ liệu cho bộ lọc (dùng trong <select> và giữ lại giá trị đã chọn)
	        request.setAttribute("orderStatuses", OrderStatus.values()); // Giả định OrderStatus là một Enum
	        request.setAttribute("shops", allShops);
	        request.setAttribute("categories", allCategories);
	        
	        // Dữ liệu phân trang
	        request.setAttribute("currentPage", currentPage);
	        request.setAttribute("totalPages", totalPages);
	        request.setAttribute("pageSize", pageSize); // Có thể bỏ qua nếu giá trị cố định

	        // Chuyển hướng đến trang JSP quản lý đơn hàng
	        request.getRequestDispatcher("/WEB-INF/views/admin/orders/list.jsp").forward(request, response);
	    }
	    
	    // ... Xử lý các yêu cầu khác nếu có ...
	 // Giả định: Bạn đang ở trong Controller/Servlet hoặc Filter có quyền truy cập vào HttpServletRequest
	 // String url = request.getRequestURI() + "?" + request.getQueryString(); 
	 // HOẶC: String url = request.getRequestURI();

	 // 1. Kiểm tra URL (Chỉ kiểm tra đường dẫn cơ sở, tham số ID sẽ lấy riêng)
	 if (url.endsWith("/admin/orders/details")) {
	     
	     // 2. Lấy ID từ tham số query
	     String orderIdParam = request.getParameter("id");
	     
	     // 3. Kiểm tra xem ID có tồn tại không
	     if (orderIdParam != null && !orderIdParam.isEmpty()) {
	         try {
	             // Chuyển đổi ID sang kiểu Long
	             Long orderId = Long.parseLong(orderIdParam);
	             
	             // Khởi tạo OrderService (Giả định bạn có thể lấy nó từ IoC Container hoặc tự tạo)
	             
	             
	             // 4. Tìm đơn hàng
	             Order order = orderService.findById(orderId);
	             List<OrderItem> items = orderService.getOrderItemsForDetail(orderId);
	             request.setAttribute("items", items);
	             
	             // 5. Kiểm tra và chuyển hướng
	             if (order != null) {
	                 // Đặt đối tượng Order vào request để hiển thị trên JSP
	                 request.setAttribute("orderDetail", order);
	                 
	                 // Chuyển hướng đến trang chi tiết order
	                 request.getRequestDispatcher("/WEB-INF/views/admin/orders/order-detail.jsp").forward(request, response);
	                 
	             } else {
	                 // Xử lý trường hợp không tìm thấy đơn hàng (ví dụ: hiển thị trang lỗi 404)
	                 response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + orderId);
	             }
	             
	         } catch (NumberFormatException e) {
	             // Xử lý trường hợp ID không phải là số hợp lệ
	             response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID đơn hàng không hợp lệ.");
	         }
	     } else {
	         // Xử lý trường hợp URL là /admin/orders (không có ID), 
	         // có thể chuyển sang trang danh sách (list) nếu bạn chưa xử lý ở nơi khác.
	         // request.getRequestDispatcher("/WEB-INF/views/admin/orders/list.jsp").forward(request, response);
	     }
	 }
	}
}
