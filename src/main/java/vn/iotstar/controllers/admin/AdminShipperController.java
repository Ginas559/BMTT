package vn.iotstar.controllers.admin;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import vn.iotstar.DAO.Impl.ShipperDaoImpl; // ⚠️ Cần tạo ShipperDaoImpl
import vn.iotstar.entities.Shipper; // ⚠️ Cần có entity Shipper
import vn.iotstar.entities.User;
import vn.iotstar.services.shipper.ShipperService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;


@MultipartConfig
@WebServlet(urlPatterns = { "/admin/shippers", "/admin/shippers/*" }) // ⚠️ Thay đổi URL
public class AdminShipperController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// ⚠️ Thay đổi Service và DAO
	private ShipperService shipperService = new ShipperService();
	private ShipperDaoImpl shipperDaoImpl = new ShipperDaoImpl();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String path = request.getPathInfo(); // phần sau /admin/shippers
		if (path == null || path.equals("/") || path.equals("")) {
			listShippers(request, response); // ⚠️ Thay đổi tên phương thức
		} else if (path.contains("add")) {
			showAddForm(request, response);
		} else {
			response.sendRedirect(request.getContextPath() + "/admin/shippers");
		}
	}

	// ==========================
	// Danh sách Shipper
	// ==========================
	private void listShippers(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Lấy tham số từ query string
		String keyword = request.getParameter("keyword");
		String bannedParam = request.getParameter("banned");
		String pageParam = request.getParameter("page");

		String banned = null;
		if (bannedParam != null && !bannedParam.isEmpty()) {
			banned = bannedParam;
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
		// ⚠️ Thay đổi phương thức service
		List<User> shippers = shipperService.searchShippers(keyword, banned, currentPage, pageSize);
		int totalShippers = shipperService.countShipper(keyword, banned);
		int totalPages = (int) Math.ceil((double) totalShippers / pageSize);

		// Đưa dữ liệu sang JSP
		request.setAttribute("shippers", shippers); // ⚠️ Thay đổi tên thuộc tính
		request.setAttribute("keyword", keyword);
		request.setAttribute("banned", bannedParam);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("pageSize", pageSize);

		// ⚠️ Thay đổi đường dẫn JSP
		RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/admin/shippers/list.jsp");
		rd.forward(request, response);
	}

	// ==========================
	// Hiển thị form thêm
	// ==========================
	private void showAddForm(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// ⚠️ Thay đổi đường dẫn JSP
		RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/admin/shippers/add.jsp");
		rd.forward(request, response);
	}

	

	

	

}
