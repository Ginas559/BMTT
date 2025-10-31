package vn.iotstar.controllers.admin;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.entities.Category;
import vn.iotstar.entities.Shop;
import vn.iotstar.entities.User;
import vn.iotstar.entities.Shop.ShopStatus;
import vn.iotstar.services.admin.AdminShopService;
import vn.iotstar.services.admin.UserService;

@MultipartConfig
@WebServlet(urlPatterns = { "/admin/shops", "/admin/shops/add", "/admin/shops/edit",
		"/admin/shops/toggle-ban/*" })
public class AdminShopController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private AdminShopService shopService = new AdminShopService();
	private UserService userService = new UserService();
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String url = request.getRequestURI();

		// 🟩 Trang danh sách cửa hàng
		if (url.endsWith("/admin/shops")) {

			String keyword = request.getParameter("keyword");
			String statusParam = request.getParameter("status");
			String pageParam = request.getParameter("page");

			// Xử lý lọc theo trạng thái
			ShopStatus status = null;
			if (statusParam != null && !statusParam.isEmpty()) {
				try {
					status = ShopStatus.valueOf(statusParam.toUpperCase());
				} catch (IllegalArgumentException e) {
					status = null; // nếu truyền sai enum
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

			// Lấy danh sách cửa hàng (có tìm kiếm + lọc + phân trang)
			List<Shop> shops = shopService.shops(keyword, status, currentPage, pageSize);
			int totalShops = shopService.countShops(keyword, status);
			int totalPages = (int) Math.ceil((double) totalShops / pageSize);

			request.setAttribute("shops", shops);
			request.setAttribute("keyword", keyword);
			request.setAttribute("statusParam", statusParam);
			request.setAttribute("currentPage", currentPage);
			request.setAttribute("totalPages", totalPages);
			request.setAttribute("pageSize", pageSize);

			request.getRequestDispatcher("/WEB-INF/views/admin/shops/list.jsp").forward(request, response);

		}
		// 🟨 Trang thêm cửa hàng
		else if (url.endsWith("/add")) {

			List<User> vendors = userService.getAllVendorsValidate();
			request.setAttribute("vendors", vendors);

			request.getRequestDispatcher("/WEB-INF/views/admin/shops/add.jsp").forward(request, response);
		}
		// 🟦 Trang chỉnh sửa cửa hàng
		else if (url.endsWith("/edit")) {
			try {
				Long id = Long.parseLong(request.getParameter("id"));
				Shop shop = shopService.findById(id);

				if (shop == null) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy cửa hàng.");
					return;
				}

				List<User> vendors = userService.getAllVendorsValidate();
				request.setAttribute("vendors", vendors);
				request.setAttribute("shop", shop);

				request.getRequestDispatcher("/WEB-INF/views/admin/shops/edit.jsp").forward(request, response);

			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID cửa hàng không hợp lệ.");
			}
		}
	}
}
