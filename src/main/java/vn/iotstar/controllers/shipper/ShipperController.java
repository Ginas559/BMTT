package vn.iotstar.controllers.shipper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.iotstar.entities.Order;
import vn.iotstar.entities.OrderItem;
import vn.iotstar.entities.Shipper;
import vn.iotstar.entities.ShipperPost;
import vn.iotstar.entities.User;
import vn.iotstar.services.admin.AdminOrderService;
import vn.iotstar.services.shipper.ShipperService;

import java.io.IOException;
import java.lang.module.ModuleDescriptor.Builder;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

// Import các Entity và Service tương ứng của bạn (ví dụ: Order, OrderService, UserService...)
// import vn.iotstar.entities.Order; 
// import vn.iotstar.services.OrderService; 

@WebServlet({ "/shipper/available-orders", "/shipper/my-orders", "/shipper/history", "/shipper/social",
		"/shipper/statistics/view", "/shipper/available-orders/accept", "/shipper/my-orders/complete",
		"/shipper/my-orders/return", "/shipper/social/add",
		// Thêm các đường dẫn detail
		"/shipper/available-orders/detail/*", "/shipper/my-orders/detail/*", "/shipper/history/detail/*" })
public class ShipperController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private ShipperService shipService = new ShipperService();
	// Giả định OrderService đã được khởi tạo
	private AdminOrderService orderService = new AdminOrderService();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Lấy đường dẫn URI
		String uri = request.getRequestURI();

		// Giả định: Kiểm tra quyền SHIPPER ở Filter/Interceptor hoặc ngay tại đây
		// if (!"SHIPPER".equals(request.getSession().getAttribute("role"))) {
		// response.sendRedirect(request.getContextPath() + "/login");
		// return;
		// }

		// --- 1. Trang Đơn Hàng Có Sẵn ---
		if (uri.endsWith("/shipper/available-orders")) {
			// Giả định: Gọi Service để lấy danh sách đơn hàng có sẵn
			List<Order> availableOrders = shipService.availableOrders(1, 10);
			request.setAttribute("availableOrders", availableOrders);

			request.setAttribute("pageTitle", "Đơn hàng có sẵn");
			request.getRequestDispatcher("/WEB-INF/views/shipper/available-orders.jsp").forward(request, response);
		}

		// --- 2. Trang Đơn Hàng Đã Nhận ---
		else if (uri.endsWith("/shipper/my-orders")) {
			// Giả định: Lấy userId từ session

			User shipper = (User) request.getSession().getAttribute("currentUser");
			List<Order> myOrders = shipService.myOrders(shipper, 1, 10);
			request.setAttribute("myOrders", myOrders);

			request.setAttribute("pageTitle", "Đơn hàng của tôi");
			request.getRequestDispatcher("/WEB-INF/views/shipper/my-orders.jsp").forward(request, response);
		}

		// --- 3. Trang Lịch Sử Giao Hàng ---
		else if (uri.endsWith("/shipper/history")) {
			User shipper = (User) request.getSession().getAttribute("currentUser");
			List<Order> historyOrders = shipService.history(shipper, 1, 10);
			request.setAttribute("historyOrders", historyOrders);

			request.setAttribute("pageTitle", "Lịch sử giao hàng");
			request.getRequestDispatcher("/WEB-INF/views/shipper/history.jsp").forward(request, response);
		}

		// --- 4. Trang Mạng Xã Hội Shipper ---
		// Trong ShipperSocialServlet.java (hoặc doPost/doGet tổng hợp của bạn)

		else if (uri.endsWith("/shipper/social")) {

			// --- CẤU HÌNH PHÂN TRANG ---
			final int PAGE_SIZE = 10; // Số bài viết mỗi trang
			int currentPage = 1;

			// 1. Lấy tham số 'page' từ request
			String pageParam = request.getParameter("page");
			if (pageParam != null && !pageParam.isEmpty()) {
				try {
					currentPage = Integer.parseInt(pageParam);
					if (currentPage < 1)
						currentPage = 1; // Đảm bảo trang không nhỏ hơn 1
				} catch (NumberFormatException e) {
					// Giữ nguyên currentPage = 1 nếu tham số không hợp lệ
				}
			}

			// 2. Lấy tổng số bài viết và tính toán tổng số trang
			try {
				// GIẢ ĐỊNH: Bạn cần có hàm này trong shipService
				long totalPosts = shipService.countShipperPosts();

				// Công thức tính tổng số trang: (Tổng số - 1) / Kích thước trang + 1
				int totalPages = (int) Math.ceil((double) totalPosts / PAGE_SIZE);

				if (currentPage > totalPages && totalPages > 0) {
					currentPage = totalPages; // Điều chỉnh nếu người dùng nhập trang quá lớn
				}
				if (totalPages == 0)
					totalPages = 1; // Trường hợp không có bài đăng

				// 3. Gọi Service để lấy bài viết của trang hiện tại
				List<ShipperPost> shipperPosts = shipService.shipperPosts(currentPage, PAGE_SIZE);

				// 4. Đặt tất cả dữ liệu phân trang vào Request
				request.setAttribute("posts", shipperPosts);
				request.setAttribute("currentPage", currentPage);
				request.setAttribute("totalPages", totalPages);
				request.setAttribute("totalPosts", totalPosts);

			} catch (Exception e) {
				// Xử lý lỗi nếu việc truy vấn DB thất bại
				request.setAttribute("errorMessage", "Không thể tải dữ liệu mạng xã hội.");
				e.printStackTrace();
			}

			request.setAttribute("pageTitle", "Mạng xã hội Shipper");
			request.getRequestDispatcher("/WEB-INF/views/shipper/social.jsp").forward(request, response);
		}

		// --- 5. Trang Thống Kê Cá Nhân ---
		else if (uri.endsWith("/shipper/statistics/view")) {
			// Giả định: Gọi Service để lấy dữ liệu thống kê
			User shipper = (User) request.getSession().getAttribute("currentUser");
			int successfulOrder = shipService.SuccessfulOrder(shipper);
			BigDecimal totalRevenue = shipService.TotalRevenue(shipper);
			double cancellationRate = 0.05;
			
			// ShipperStats stats = shipperService.getShipperStatistics(shipperId);
			// request.setAttribute("shipperStats", stats);

			// MOCK DATA
			request.setAttribute("totalDelivered", successfulOrder);
			request.setAttribute("totalRevenue", totalRevenue);
			request.setAttribute("cancellationRate", cancellationRate);
			
			request.setAttribute("pageTitle", "Thống kê hiệu suất");
			request.getRequestDispatcher("/WEB-INF/views/shipper/statistics.jsp").forward(request, response);
		}

		// --- 6. Trang Chi Tiết Đơn Hàng ---
		else if (uri.contains("/detail/")) {
			// Lấy OrderId từ URI. Ví dụ: /.../detail/123 -> id = 123
			String pathInfo = request.getPathInfo(); // Lấy phần sau /detail/
			if (pathInfo != null && pathInfo.length() > 1) {
				try {
					Long orderId = Long.parseLong(pathInfo.substring(1)); // Bỏ dấu '/' đầu tiên

					Order orderDetail = orderService.findById(orderId);
					List<OrderItem> items = orderService.getOrderItemsForDetail(orderId);
					if (orderDetail != null) {
						request.setAttribute("orderDetail", orderDetail);
						request.setAttribute("items", items);

						request.setAttribute("pageTitle", "Chi tiết đơn hàng #" + orderId);
						request.getRequestDispatcher("/WEB-INF/views/shipper/order-detail.jsp").forward(request,
								response);
					} else {
						// Xử lý không tìm thấy đơn hàng
						response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy đơn hàng.");
					}

				} catch (NumberFormatException e) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID đơn hàng không hợp lệ.");
				}
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Thiếu ID đơn hàng.");
			}
		}

		// Xử lý các đường dẫn không khớp
		else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Đảm bảo set encoding để xử lý tiếng Việt
		request.setCharacterEncoding("UTF-8");
		String uri = request.getRequestURI();
		HttpSession session = request.getSession();

		if (uri.endsWith("/shipper/social/add")) {

			// Lấy Shipper từ Session
			User shipper = (User) session.getAttribute("currentUser");

			// Lấy tham số từ Form
			String postTypeParam = request.getParameter("postType");
			String title = request.getParameter("title");
			String content = request.getParameter("content");

			// --- 1. KIỂM TRA TÍNH HỢP LỆ (Authentication & Data) ---
			if (shipper == null) {
				session.setAttribute("errorMessage", "Bạn cần đăng nhập để đăng bài.");
				response.sendRedirect(request.getContextPath() + "/login");
				return;
			}
			if (postTypeParam == null || title == null || content == null || title.isEmpty() || content.isEmpty()) {
				session.setAttribute("errorMessage", "Vui lòng điền đầy đủ tiêu đề, loại bài đăng và nội dung.");
				response.sendRedirect(request.getContextPath() + "/shipper/social");
				return;
			}

			ShipperPost.PostType type = null;
			try {
				// 2. CHUYỂN ĐỔI: Sửa lỗi kỹ thuật, chuyển String thành Enum
				type = ShipperPost.PostType.valueOf(postTypeParam.toUpperCase());

				// 3. TẠO ENTITY
				ShipperPost post = ShipperPost.builder().title(title).content(content).postType(type).shipper(shipper)
						.build();

				// 4. GỌI SERVICE
				// Giả định shipService.addPost(post) tồn tại
				shipService.addPost(post);

				// 5. THÀNH CÔNG: Đặt thông báo và chuyển hướng
				session.setAttribute("successMessage", "Bài đăng của bạn đã được chia sẻ thành công!");

			} catch (IllegalArgumentException e) {
				// Bắt lỗi nếu postTypeParam không khớp với bất kỳ Enum nào
				session.setAttribute("errorMessage", "Loại bài đăng không hợp lệ.");
			} catch (RuntimeException e) {
				// Bắt lỗi JPA/DB
				String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
				session.setAttribute("errorMessage", "Lỗi hệ thống khi đăng bài: " + errorMsg);
			}

			// 6. CHUYỂN HƯỚNG BẮT BUỘC
			response.sendRedirect(request.getContextPath() + "/shipper/social");
		} else {
			// Lấy Shipper ID và URL chuyển hướng mặc định
			User shipper = (User) session.getAttribute("currentUser");
			Long shipperId = (shipper != null) ? shipper.getId() : null;
			String redirectUrl = request.getContextPath() + "/shipper/dashboard";

			// 1. Kiểm tra User và OrderId chung
			String orderIdParam = request.getParameter("orderId");

			if (shipperId == null) {
				session.setAttribute("errorMessage", "Vui lòng đăng nhập với tài khoản Shipper.");
				response.sendRedirect(request.getContextPath() + "/login"); // Chuyển về trang login
				return;
			}
			if (orderIdParam == null || orderIdParam.isEmpty()) {
				session.setAttribute("errorMessage", "Không tìm thấy ID đơn hàng.");
				response.sendRedirect(redirectUrl);
				return;
			}

			Long orderId = null;
			try {
				orderId = Long.parseLong(orderIdParam);
				String successMessage = null;

				// =========================================================
				// A. NHẬN ĐƠN HÀNG (ACCEPT)
				// =========================================================
				if (uri.endsWith("/shipper/available-orders/accept")) {

					// Hàm đã có: assignOrderToShipper(orderId, shipperId)
					shipService.assignOrderToShipper(orderId, shipperId);

					successMessage = "✅ Đã nhận đơn hàng #" + orderId + ". Vui lòng giao hàng.";
					redirectUrl = request.getContextPath() + "/shipper/my-orders"; // Chuyển sang danh sách đơn đã nhận

				}

				// =========================================================
				// B. HOÀN THÀNH GIAO HÀNG (COMPLETE)
				// =========================================================
				else if (uri.endsWith("/shipper/my-orders/complete")) {

					// **Giả định hàm mới:** updateOrderStatus(orderId, shipperId, DELIVERED)
					shipService.completeOrder(orderId, shipperId);

					successMessage = "🎉 Giao hàng thành công cho đơn hàng #" + orderId + ".";
					redirectUrl = request.getContextPath() + "/shipper/my-orders"; // Quay lại danh sách đơn

				}

				// =========================================================
				// C. TRẢ LẠI/BÁO CÁO SỰ CỐ (RETURN)
				// =========================================================
				else if (uri.endsWith("/shipper/my-orders/return")) {

					
					shipService.dropOrder(orderId, shipperId);

					successMessage = "Đã báo cáo trả lại đơn hàng #" + orderId + ".";
					redirectUrl = request.getContextPath() + "/shipper/my-orders"; // Quay lại danh sách đơn

				}

				// 5. Xử lý thành công chung
				if (successMessage != null) {
					session.setAttribute("successMessage", successMessage);
				}
				response.sendRedirect(redirectUrl);

			} catch (NumberFormatException e) {
				session.setAttribute("errorMessage", "ID đơn hàng không đúng định dạng.");
				response.sendRedirect(redirectUrl);
			} catch (IllegalArgumentException | IllegalStateException e) {
				// Bắt lỗi nghiệp vụ: Đơn hàng không tồn tại, trạng thái không hợp lệ, v.v.
				session.setAttribute("errorMessage", "❌ Lỗi nghiệp vụ: " + e.getMessage());
				response.sendRedirect(redirectUrl);
			} catch (RuntimeException e) {
				// Bắt lỗi hệ thống (JPA/Transaction)
				String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
				session.setAttribute("errorMessage", "❌ Lỗi hệ thống: " + errorMsg);
				response.sendRedirect(redirectUrl);
			}
		}

	}
}
