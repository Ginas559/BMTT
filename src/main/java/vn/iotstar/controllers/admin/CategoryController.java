package vn.iotstar.controllers.admin;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import vn.iotstar.entities.Category;
import vn.iotstar.services.admin.AdminCategoryService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@MultipartConfig
@WebServlet(urlPatterns = { "/admin/categories", "/admin/categories/add", "/admin/categories/edit",
		"/admin/categories/toggle-ban/*" })
public class CategoryController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private AdminCategoryService adminCategoryService = new AdminCategoryService();

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String url = request.getRequestURI();

		// 🟩 Trang danh sách
		if (url.endsWith("/admin/categories")) {
			// Lấy tham số từ query string
			String keyword = request.getParameter("keyword");
			String bannedParam = request.getParameter("banned");
			String pageParam = request.getParameter("page");

			Boolean banned = null;
			if (bannedParam != null && !bannedParam.isEmpty()) {
				banned = Boolean.parseBoolean(bannedParam);
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
			List<Category> categories = adminCategoryService.categories(keyword, banned, currentPage, pageSize);
			int totalCategories = adminCategoryService.countCategories(keyword, banned);
			int totalPages = (int) Math.ceil((double) totalCategories / pageSize);

			// Đưa dữ liệu sang JSP
			request.setAttribute("categories", categories);
			request.setAttribute("keyword", keyword);
			request.setAttribute("banned", bannedParam);
			request.setAttribute("currentPage", currentPage);
			request.setAttribute("totalPages", totalPages);
			request.setAttribute("pageSize", pageSize);

			request.getRequestDispatcher("/WEB-INF/views/admin/categories/list.jsp").forward(request, response);

			// 🟨 Trang thêm mới
		} else if (url.endsWith("/add")) {
			request.getRequestDispatcher("/WEB-INF/views/admin/categories/add.jsp").forward(request, response);

			// 🟦 Trang chỉnh sửa
		} else if (url.endsWith("/edit")) {
			try {
				Long id = Long.parseLong(request.getParameter("id"));
				Category category = adminCategoryService.findById(id);

				if (category == null) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy danh mục.");
					return;
				}

				request.setAttribute("category", category);
				request.getRequestDispatcher("/WEB-INF/views/admin/categories/edit.jsp").forward(request, response);

			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID không hợp lệ.");
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8");
		String uri = req.getRequestURI();
		
		if (uri.contains("/toggle-ban/")) {
		    // Tách ID từ URI, ví dụ: /admin/categories/toggle-ban/5
		    String[] parts = uri.split("/");
		    Long id = Long.parseLong(parts[parts.length - 1]); // Lấy phần cuối cùng là ID

		    Category category = adminCategoryService.findById(id);
		    if (category != null) {
		        // Đảo trạng thái isBanned
		        category.setIsBanned(!category.getIsBanned());
		        adminCategoryService.edit(category);
		    }

		    resp.sendRedirect(req.getContextPath() + "/admin/categories");
		    return;
		}


		// ✅ Đường dẫn lưu ảnh ngoài project (dùng chung với ImageLoad)
		String type = "categories";
		String uploadPath = "F:/HK1_25_26/LTWEB/uploads/" + type;
		File uploadDir = new File(uploadPath);
		if (!uploadDir.exists())
			uploadDir.mkdirs();

		String categoryName = req.getParameter("categoryName");
		String description = req.getParameter("description");
		Boolean isBanned = req.getParameter("isBanned") != null;

//		Part filePart = req.getPart("image");
//		String fileName = null;
//
//		// ✅ Nếu người dùng có chọn ảnh mới
//		if (filePart != null && filePart.getSize() > 0) {
//			fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
//			filePart.write(uploadPath + File.separator + fileName);
//			System.out.println("✅ Uploaded: " + uploadPath + "/" + fileName);
//		}

//		// ✅ Xử lý thêm mới
		if (uri.endsWith("/add")) {
//			// Nếu không có ảnh => đặt ảnh mặc định
//			if (fileName == null)
//				fileName = "default-category.png";

			Category category = Category.builder().categoryName(categoryName).description(description)
					.isBanned(isBanned).build();

			adminCategoryService.add(category);
		}

		// ✅ Xử lý chỉnh sửa
		else if (uri.endsWith("/edit")) {
			Long id = Long.parseLong(req.getParameter("categoryId"));
			Category category = adminCategoryService.findById(id);

			if (category != null) {
				category.setCategoryName(categoryName);
				category.setDescription(description);
				category.setIsBanned(isBanned);

				// Nếu người dùng có upload ảnh mới thì mới đổi ảnh
//				if (fileName != null) {
//					category.setImage(fileName);
//				}

				adminCategoryService.edit(category);
			}
		}

		resp.sendRedirect(req.getContextPath() + "/admin/categories");
	}

}
