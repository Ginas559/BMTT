package vn.iotstar.controllers.admin;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import vn.iotstar.DAO.Impl.UserDaoImpl;
import vn.iotstar.entities.Category;
import vn.iotstar.entities.User;
import vn.iotstar.services.admin.UserService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;


@MultipartConfig
@WebServlet(urlPatterns = { "/admin/users", "/admin/users/*" })
public class UserController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private UserService userService = new UserService();
	private UserDaoImpl userDaoImpl = new UserDaoImpl();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String path = request.getPathInfo(); // phần sau /admin/users
		if (path == null || path.equals("/") || path.equals("")) {
			listUsers(request, response);
		} else if (path.contains("add")) {
			showAddForm(request, response);
		} else if (path.contains("edit")) {
			showEditForm(request, response);
		} else {
			response.sendRedirect(request.getContextPath() + "/admin/users");
		}
	}

	// ==========================
	// Danh sách người dùng
	// ==========================
	private void listUsers(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

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

		// Lấy danh sách từ service (có thể thêm tìm kiếm, lọc, phân trang)
		List<User> users = userService.searchUsers(keyword, banned, currentPage, pageSize);
		int totalUsers = userService.countUsers(keyword, banned);
		int totalPages = (int) Math.ceil((double) totalUsers / pageSize);

		// Đưa dữ liệu sang JSP
		request.setAttribute("users", users);
		request.setAttribute("keyword", keyword);
		request.setAttribute("banned", bannedParam);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("pageSize", pageSize);

		RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/admin/users/list.jsp");
		rd.forward(request, response);
	}

	// ==========================
	// Hiển thị form thêm
	// ==========================
	private void showAddForm(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/admin/users/add.jsp");
		rd.forward(request, response);
	}

	// ==========================
	// Hiển thị form sửa
	// ==========================
	private void showEditForm(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String sid = request.getParameter("id");
		if (sid == null) {
			response.sendRedirect(request.getContextPath() + "/admin/users");
			return;
		}

		try {
			long id = Long.parseLong(sid);
			User user = userDaoImpl.findById(id);

			if (user == null) {
				response.sendRedirect(request.getContextPath() + "/admin/users");
				return;
			}

			request.setAttribute("user", user);
			RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/admin/users/edit.jsp");
			rd.forward(request, response);
		} catch (NumberFormatException e) {
			response.sendRedirect(request.getContextPath() + "/admin/users");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8");
		String uri = req.getRequestURI();

		if (uri.contains("/toggle-ban/")) {
			// Tách ID từ URI, ví dụ: /admin/categories/toggle-ban/5
			String[] parts = uri.split("/");
			Long id = Long.parseLong(parts[parts.length - 1]); // Lấy phần cuối cùng là ID

			User user = userService.findById(id);
			if (user != null) {
				// Đảo trạng thái isBanned
				user.setIsBanned(!user.getIsBanned());
				userService.edit(user);
			}

			resp.sendRedirect(req.getContextPath() + "/admin/users");
			return;
		}

		// ✅ Đường dẫn lưu ảnh ngoài project (dùng chung với ImageLoad)
		String type = "users";
		String uploadPath = "F:/HK1_25_26/LTWEB/uploads/" + type;
		File uploadDir = new File(uploadPath);
		if (!uploadDir.exists())
			uploadDir.mkdirs();

		String firstname = req.getParameter("firstname");
		String lastname = req.getParameter("lastname");
		String email = req.getParameter("email");
		String phone = req.getParameter("phone");
		String idCard = req.getParameter("idCard");
		
		String hashedPassword = null;
		if(uri.endsWith("/add")) {
			String password = req.getParameter("password");
			hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
		}
		String roleParam = req.getParameter("role");
		User.Role role = User.Role.USER; // default
		if (roleParam != null && !roleParam.isBlank()) {
		    try {
		        role = User.Role.valueOf(roleParam);
		    } catch (IllegalArgumentException e) {
		        role = User.Role.USER;
		    }
		}

		
		Boolean isBanned = Boolean.parseBoolean(req.getParameter("isBanned"));

		Part filePart = req.getPart("avatarFile");
		String fileName = null;

		// ✅ Nếu người dùng có chọn ảnh mới
		if (filePart != null && filePart.getSize() > 0) {
			fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
			filePart.write(uploadPath + File.separator + fileName);
			System.out.println("✅ Uploaded: " + uploadPath + "/" + fileName);
		}

		// ✅ Xử lý thêm mới
		if (uri.endsWith("/add")) {
			// Nếu không có ảnh => đặt ảnh mặc định
			if (fileName == null)
				fileName = "default-user.png";
			
			User user = User.builder()
			        .firstname(firstname)
			        .lastname(lastname)
			        .email(email)
			        .phone(phone)
			        .idCard(idCard)
			        .hashedPassword(hashedPassword)
			        .role(role)
			        .isBanned(isBanned)
			        .avatar(fileName)
			        .build();

//			Category category = Category.builder().categoryName(categoryName).description(description)
//					.isBanned(isBanned).image(fileName).build();

			userService.add(user);
		}

		// ✅ Xử lý chỉnh sửa
		else if (uri.endsWith("/edit")) {
			Long id = Long.parseLong(req.getParameter("id"));
			User user = userService.findById(id);

			if (user != null) {
				user.setFirstname(firstname);
				user.setLastname(lastname);
				user.setEmail(email);
				user.setPhone(phone);
				user.setIdCard(idCard);
				user.setRole(role);
				user.setIsBanned(isBanned);

				// Nếu người dùng có upload ảnh mới thì mới đổi ảnh
				if (fileName != null) {
					user.setAvatar(fileName);
				}

				userService.edit(user);
			}
		}

		resp.sendRedirect(req.getContextPath() + "/admin/users");
	}

}
