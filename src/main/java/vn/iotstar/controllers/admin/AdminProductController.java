package vn.iotstar.controllers.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// import jakarta.servlet.http.Part; // Giữ lại nếu cần xử lý upload file
import vn.iotstar.entities.Product; // Cần import Product
import vn.iotstar.entities.ProductComment;
import vn.iotstar.entities.ProductReview;
import vn.iotstar.entities.Category; // Cần import Category
import vn.iotstar.entities.Shop; // Cần import Shop (dành cho bộ lọc)
import vn.iotstar.services.admin.AdminCategoryService;
import vn.iotstar.services.admin.AdminProductService;
import vn.iotstar.services.admin.AdminShopService; // Giả định có ShopService

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
// import java.nio.file.Paths; // Giữ lại nếu cần xử lý upload file
import java.util.List;

@MultipartConfig
@WebServlet(urlPatterns = { "/admin/products", "/admin/products/add", "/admin/products/edit", "/admin/products/detail", // Thêm
																														// detail
		"/admin/products/toggle-ban/*" })
public class AdminProductController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// Sử dụng Service đúng cho Product, Category, và Shop
	private AdminProductService adminProductService = new AdminProductService();
	private AdminCategoryService adminCategoryService = new AdminCategoryService();
	private AdminShopService adminShopService = new AdminShopService(); // Khởi tạo Shop Service

	// ===================================================
	// GET REQUESTS (Hiển thị)
	// ===================================================

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String url = request.getRequestURI();

		// ----------------------------------------------------
		// 🟩 Trang danh sách (có bộ lọc và phân trang)
		// ----------------------------------------------------
		if (url.endsWith("/admin/products")) {
			// Lấy tham số từ query string
			String keyword = request.getParameter("keyword");
			String bannedParam = request.getParameter("banned");
			String pageParam = request.getParameter("page");
			String categoryIdParam = request.getParameter("categoryId");
			String shopIdParam = request.getParameter("shopId");

			// Chuyển đổi tham số
			Long categoryId = (categoryIdParam != null && !categoryIdParam.isEmpty()) ? Long.parseLong(categoryIdParam)
					: null;
			Long shopId = (shopIdParam != null && !shopIdParam.isEmpty()) ? Long.parseLong(shopIdParam) : null;
			Boolean isBanned = (bannedParam != null && !bannedParam.isEmpty()) ? Boolean.parseBoolean(bannedParam)
					: null;

			int currentPage = 1;
			int pageSize = 10;
			if (pageParam != null && !pageParam.isEmpty()) {
				try {
					currentPage = Integer.parseInt(pageParam);
				} catch (NumberFormatException e) {
					/* Mặc định 1 */ }
			}

			// Lấy danh sách Product (Cần phải có phương thức tìm kiếm nâng cao trong
			// ProductService)
			// Cần sửa Service để tìm kiếm Product, không phải Category
			
			Shop shop = null;
			if(shopId != null) {
				shop = adminShopService.findById(shopId);
			}
			
			
			Category category = null;
			if(categoryId != null) {
				category = adminCategoryService.findById(categoryId);
			}
			

			List<Product> products = adminProductService.searchProducts(keyword, isBanned, shop, category, currentPage,
					pageSize);

			int totalProducts = adminProductService.countProducts(keyword, isBanned, shop, category);
			int totalPages = (int) Math.ceil((double) totalProducts / pageSize);

			// Lấy danh sách lọc (Category & Shop) để đưa vào Select Box trên JSP
			List<Category> categories = adminCategoryService.findAllCategoriesValidate(); // Giả sử có findAll
			List<Shop> shops = adminShopService.findAllShopsValidate(); // Giả sử có findAll

			// Đưa dữ liệu sang JSP
			request.setAttribute("products", products);
			request.setAttribute("categories", categories); // Cho bộ lọc
			request.setAttribute("shops", shops); // Cho bộ lọc
			request.setAttribute("currentPage", currentPage);
			request.setAttribute("totalPages", totalPages);
			request.setAttribute("pageSize", pageSize);

			// Chuyển đến JSP của Product
			request.getRequestDispatcher("/WEB-INF/views/admin/products/list.jsp").forward(request, response);

			// ----------------------------------------------------
			// 🟨 Trang thêm mới
			// ----------------------------------------------------
		} else if (url.endsWith("/add")) {
			// Cần lấy danh sách Category và Shop để tạo form
			request.setAttribute("categories", adminCategoryService.findAllCategoriesValidate());
			request.setAttribute("shops", adminShopService.findAllShopsValidate());
			request.getRequestDispatcher("/WEB-INF/views/admin/products/add.jsp").forward(request, response);

			// ----------------------------------------------------
			// 🟦 Trang chỉnh sửa
			// ----------------------------------------------------
		} else if (url.endsWith("/edit")) {
			try {
				Long id = Long.parseLong(request.getParameter("id"));
				Product product = adminProductService.findById(id); // Tìm Product

				if (product == null) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy sản phẩm.");
					return;
				}

				// Cần lấy danh sách Category và Shop để tạo form
				request.setAttribute("product", product);
				request.setAttribute("categories", adminCategoryService.findAllCategoriesValidate());
				request.setAttribute("shops", adminShopService.findAllShopsValidate());
				request.getRequestDispatcher("/WEB-INF/views/admin/products/edit.jsp").forward(request, response);

			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID không hợp lệ.");
			}

			// 🟪 Trang chi tiết
		} else if (url.endsWith("/detail")) {
			try {
				Long id = Long.parseLong(request.getParameter("id"));
				Product product = adminProductService.findById(id);
				
				List<ProductComment> comments = adminProductService.productComments(id);
				List<ProductReview> reviews = adminProductService.productReviews(id);
				
				if (product == null) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy sản phẩm.");
					return;
				}

				request.setAttribute("productDetail", product);
				request.setAttribute("comments", comments);
				request.setAttribute("reviews", reviews);
				
				request.getRequestDispatcher("/WEB-INF/views/admin/products/product-detail.jsp").forward(request, response);

			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID không hợp lệ.");
			}
		}
	}

	// ===================================================
	// POST REQUESTS (Thao tác)
	// ===================================================

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8");
		String uri = req.getRequestURI();

		// ----------------------------------------------------
		// 🟥 Xử lý Cấm/Bỏ Cấm (toggle-ban)
		// ----------------------------------------------------
		if (uri.contains("/toggle-ban/")) {
			// Lấy ID từ URI, ví dụ: /admin/products/toggle-ban/5
			String[] parts = uri.split("/");
			Long id = Long.parseLong(parts[parts.length - 1]);

			Product product = adminProductService.findById(id);
			if (product != null) {
				// Đảo trạng thái isBanned
				product.setIsBanned(!product.getIsBanned());
				adminProductService.edit(product);
			}

			// Chuyển hướng về trang danh sách sản phẩm
			resp.sendRedirect(req.getContextPath() + "/admin/products");
			return;
		}

		// ✅ Đường dẫn lưu ảnh ngoài project
//		String type = "products"; // Đổi thành products
//		String uploadPath = "F:/HK1_25_26/LTWEB/uploads/" + type;
//		File uploadDir = new File(uploadPath);
//		if (!uploadDir.exists())
//			uploadDir.mkdirs();

		// ----------------------------------------------------
		// 🟦 Xử lý Thêm mới (Cần hoàn thiện logic lấy data)
		// ----------------------------------------------------
		if (uri.endsWith("/add")) {
			// Lấy dữ liệu Product từ form (cần hoàn thiện)
			String productName = req.getParameter("productName");
			// ... (các trường khác: price, stock, categoryId, shopId, description,
			// isBanned...)

			// Product product = Product.builder()...build();
			// adminProductService.add(product);
		}

		// ----------------------------------------------------
		// 🟧 Xử lý Chỉnh sửa (Cần hoàn thiện logic lấy data)
		// ----------------------------------------------------
		// Giả định bạn đang ở trong phương thức doPost(HttpServletRequest req, HttpServletResponse resp)
		// ...

		else if (uri.endsWith("/edit")) {
		    try {
		        // Lấy ID sản phẩm cần chỉnh sửa
		        Long id = Long.parseLong(req.getParameter("productId"));
		        
		        // 1. Tìm sản phẩm hiện tại trong DB
		        Product product = adminProductService.findById(id);

		        if (product != null) {
		            
		            // 2. Lấy và chuyển đổi dữ liệu từ form
		            
		            // Lấy giá trị chuỗi
		            String priceStr = req.getParameter("price");
		            String discountPriceStr = req.getParameter("discountPrice");
		            String stockStr = req.getParameter("stock");
		            String categoryIdStr = req.getParameter("categoryId");
		            String shopIdStr = req.getParameter("shopId");
		            
		            // Xử lý Checkbox isBanned: Nếu tham số tồn tại (khác null), nghĩa là nó đã được check (TRUE)
		            // Nếu tham số là null, nghĩa là nó không được check (FALSE)
		            Boolean isBanned = (req.getParameter("isBanned") != null);
		            
		            
		            // 3. Chuyển đổi và gán dữ liệu số/đối tượng
		            
		            // Chuyển đổi BigDecimal (Cần xử lý rỗng/null an toàn)
		            BigDecimal price = new BigDecimal(priceStr);
		            BigDecimal discountPrice = (discountPriceStr != null && !discountPriceStr.isEmpty()) 
		                                        ? new BigDecimal(discountPriceStr) 
		                                        : null; // hoặc BigDecimal.ZERO
		            
		            int stock = Integer.parseInt(stockStr);
		            
		            // Lấy đối tượng Category và Shop từ ID
		            Category category = adminCategoryService.findById(Long.parseLong(categoryIdStr));
		            Shop shop = adminShopService.findById(Long.parseLong(shopIdStr));
		            
		            
		            // 4. Set dữ liệu mới vào đối tượng Product
		            product.setProductName(req.getParameter("productName"));
		            product.setPrice(price);
		            product.setDiscountPrice(discountPrice);
		            product.setStock(stock);
		            product.setDescription(req.getParameter("description"));
		            product.setCategory(category);
		            product.setShop(shop);
		            product.setIsBanned(isBanned); // <--- Đã xử lý Checkbox tại đây
		            
		            
		            // 5. Lưu cập nhật vào cơ sở dữ liệu
		            adminProductService.edit(product);
		            
		            // Chuyển hướng sau khi thành công
		            resp.sendRedirect(req.getContextPath() + "/admin/products?id=" + id + "&message=updated");
		            return;
		            
		        } else {
		            // Xử lý lỗi nếu không tìm thấy Product
		            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy sản phẩm để chỉnh sửa.");
		            return;
		        }
		    } catch (NumberFormatException e) {
		        // Xử lý lỗi chuyển đổi kiểu dữ liệu (ID, Price, Stock...)
		        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Dữ liệu gửi lên không hợp lệ.");
		        return;
		    } 
		}

		// Sau khi thêm/sửa, chuyển hướng về trang danh sách
		resp.sendRedirect(req.getContextPath() + "/admin/products");
	}
}