package vn.iotstar.controllers.admin;

import jakarta.persistence.Convert;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.entities.Promotion; // Import Promotion Entity
import vn.iotstar.entities.Promotion.PromotionStatus; // Import PromotionStatus Enum
import vn.iotstar.entities.Shop;
import vn.iotstar.services.admin.AdminPromotionService; // Giả định Service mới
import vn.iotstar.services.admin.AdminShopService;

import java.io.IOException;
import java.math.BigDecimal; // Cần thiết cho discountPercent
import java.time.LocalDate; // Cần thiết cho startDate và endDate
import java.util.List;

// Điều chỉnh URL mapping
@WebServlet(urlPatterns = { "/admin/promotions", "/admin/promotions/add", "/admin/promotions/edit",
		"/admin/promotions/toggle-status/*" })
public class AdminPromotionController extends HttpServlet {
	private static final long serialVersionUID = 1L;

    // Thay đổi Service
	private AdminPromotionService adminPromotionService = new AdminPromotionService(); 
	
	private AdminShopService shopService = new AdminShopService();

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String url = request.getRequestURI();

		// 🟩 Trang danh sách Promotions
		if (url.endsWith("/admin/promotions")) {
			// Lấy tham số từ query string
			String keyword = request.getParameter("keyword");
			String statusParam = request.getParameter("status"); // Thay đổi từ "banned" sang "status"
			String pageParam = request.getParameter("page");

			PromotionStatus status = null;
            // Chuyển đổi String statusParam sang PromotionStatus Enum
			if (statusParam != null && !statusParam.isEmpty()) {
				try {
					status = PromotionStatus.valueOf(statusParam.toUpperCase());
				} catch (IllegalArgumentException e) {
					// Bỏ qua hoặc xử lý lỗi nếu giá trị không hợp lệ
				}
			}

			int currentPage = 1;
			int pageSize = 10;
			if (pageParam != null && !pageParam.isEmpty()) {
				try {
					currentPage = Integer.parseInt(pageParam);
				} catch (NumberFormatException e) {
					currentPage = 1;
				}
			}

			// Lấy danh sách từ service (tìm kiếm, lọc, phân trang)
            // Thay đổi hàm và kiểu dữ liệu
			List<Promotion> promotions = adminPromotionService.searchPromotions(keyword, status, currentPage, pageSize);
			int totalPromotions = adminPromotionService.countPromotions(keyword, status);
			int totalPages = (int) Math.ceil((double) totalPromotions / pageSize);

			// Đưa dữ liệu sang JSP
			request.setAttribute("promotions", promotions); // Thay đổi tên list
			request.setAttribute("keyword", keyword);
			request.setAttribute("status", statusParam); // Giữ lại String để tiện cho việc hiển thị trên form lọc
			request.setAttribute("currentPage", currentPage);
			request.setAttribute("totalPages", totalPages);
			request.setAttribute("pageSize", pageSize);
            request.setAttribute("allStatuses", PromotionStatus.values()); // Để hiển thị các lựa chọn lọc

			// Điều chỉnh đường dẫn JSP
			request.getRequestDispatcher("/WEB-INF/views/admin/promotions/list.jsp").forward(request, response);

			// 🟨 Trang thêm mới
		} else if (url.endsWith("/add")) {
			
			List<Shop> shops = shopService.findAllShopsValidate();
			request.setAttribute("shops", shops);
			// Điều chỉnh đường dẫn JSP
			request.getRequestDispatcher("/WEB-INF/views/admin/promotions/add.jsp").forward(request, response);

			// 🟦 Trang chỉnh sửa
		} else if (url.endsWith("/edit")) {
			try {
				Long id = Long.parseLong(request.getParameter("id"));
                // Thay đổi hàm và kiểu dữ liệu
				Promotion promotion = adminPromotionService.findById(id); 

				if (promotion == null) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy chương trình khuyến mãi.");
					return;
				}
				List<Shop> shops = shopService.findAllShopsValidate();
				request.setAttribute("shops", shops);

				request.setAttribute("promotion", promotion); // Thay đổi tên attribute
				// Điều chỉnh đường dẫn JSP
				request.getRequestDispatcher("/WEB-INF/views/admin/promotions/edit.jsp").forward(request, response);

			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID không hợp lệ.");
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 * response)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8");
		String uri = req.getRequestURI();
		
        // 🚨 Xử lý Toggle Status (Active/Inactive)
		if (uri.contains("/toggle-status/")) { // Thay đổi từ /toggle-ban/ sang /toggle-status/
		    // Tách ID từ URI, ví dụ: /admin/promotions/toggle-status/5
		    String[] parts = uri.split("/");
		    Long id = Long.parseLong(parts[parts.length - 1]); 

		    Promotion promotion = adminPromotionService.findById(id);
		    if (promotion != null) {
		        // Đảo trạng thái: ACTIVE -> INACTIVE, INACTIVE -> ACTIVE
		        if (promotion.getStatus() == PromotionStatus.ACTIVE) {
		            promotion.setStatus(PromotionStatus.INACTIVE);
		        } else {
		            promotion.setStatus(PromotionStatus.ACTIVE);
		        }
		        adminPromotionService.edit(promotion);
		    }

		    resp.sendRedirect(req.getContextPath() + "/admin/promotions");
		    return;
		}

        // Lấy các tham số của Promotion
		String title = req.getParameter("title");
		String shopsid = req.getParameter("shopId");
		String discountPercentStr = req.getParameter("discountPercent");
		String startDateStr = req.getParameter("startDate");
		String endDateStr = req.getParameter("endDate");
		String applyTo = req.getParameter("applyTo");
		String statusStr = req.getParameter("status");
		
		Long shopid = Long.parseLong(shopsid);;
		Shop shop = shopService.findById(shopid);
        
        // Chuyển đổi kiểu dữ liệu
        BigDecimal discountPercent = new BigDecimal(discountPercentStr);
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        PromotionStatus status = PromotionStatus.valueOf(statusStr.toUpperCase());
        // Giả định shopId được lấy từ session hoặc form (Tạm bỏ qua phần shop)


		// ✅ Xử lý thêm mới
		if (uri.endsWith("/add")) {
			
			Promotion promotion = Promotion.builder()
                    // Giả định shop là một đối tượng đã có
                    // .shop(shopObject) 
					.title(title)
					.shop(shop)
                    .discountPercent(discountPercent)
					.startDate(startDate)
                    .endDate(endDate)
                    .applyTo(applyTo)
					.status(status).build();

			adminPromotionService.add(promotion);
		}

		// ✅ Xử lý chỉnh sửa
		else if (uri.endsWith("/edit")) {
			Long id = Long.parseLong(req.getParameter("promotionId")); // Đảm bảo input name là promotionId
			Promotion promotion = adminPromotionService.findById(id);

			if (promotion != null) {
				promotion.setTitle(title);
				promotion.setShop(shop);
				promotion.setDiscountPercent(discountPercent);
				promotion.setStartDate(startDate);
				promotion.setEndDate(endDate);
				promotion.setApplyTo(applyTo);
                promotion.setStatus(status);

				adminPromotionService.edit(promotion);
			}
		}

		// Điều hướng lại về trang danh sách promotions
		resp.sendRedirect(req.getContextPath() + "/admin/promotions");
	}

}
