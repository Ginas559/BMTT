package vn.iotstar.controllers.admin;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.DAO.IAdminDashboardDao;
import vn.iotstar.DAO.Impl.AdminDashboardDaoImpl;
import vn.iotstar.entities.Order.OrderStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servlet implementation class DashboardController
 */
@WebServlet("/admin/dashboard")
public class DashboardController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private IAdminDashboardDao dashboardDao = new AdminDashboardDaoImpl();

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 1. Dữ liệu Thống kê Tổng quan (Summary Cards)
		// --------------------------------------------------------------------------------

		// Lấy dữ liệu cho User Count (từ hàm countTotalUsers)
		long userCount = dashboardDao.countTotalUsers();
		request.setAttribute("userCount", userCount);

		// Lấy dữ liệu cho Total Inventory Quantity (từ hàm
		// calculateTotalInventoryQuantity)
		long inventoryQuantity = dashboardDao.calculateTotalInventoryQuantity();
		request.setAttribute("inventoryQuantity", inventoryQuantity);

		// --- Giả định: Lấy dữ liệu Doanh thu & Order Count cho một khoảng thời gian
		// nhất định (ví dụ: 30 ngày) ---
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime last30Days = now.minusDays(30);

		// Lấy dữ liệu cho Revenue (từ hàm calculateRevenue)
		// Cần truyền LocalDate cho hàm calculateRevenue. Cần chuyển đổi LocalDateTime
		LocalDateTime startDate = last30Days;
		LocalDateTime endDate = now;

		// Giả sử chỉ tính các đơn hàng đã HOÀN THÀNH (COMPLETED)
		BigDecimal revenueAmount = dashboardDao.calculateRevenue(startDate, endDate);
		request.setAttribute("revenueAmount", revenueAmount);

		// Lấy dữ liệu cho Order Count (từ hàm countOrdersByStatusAndDateRange)
		// Lấy tổng số đơn hàng đã hoàn thành trong 30 ngày qua
		long orderCount = dashboardDao.countOrdersByStatusAndDateRange(last30Days, now, OrderStatus.DELIVERED);
		// Lưu ý: Tên biến cũ của bạn là "orderCount", nhưng ở đây nó chỉ đếm order hoàn
		// thành trong 30 ngày.
		// Nếu bạn muốn đếm TẤT CẢ order, bạn cần viết thêm hàm riêng.
		request.setAttribute("orderCount", orderCount);

		// 2. Dữ liệu Biểu đồ (Chart Data)
		// --------------------------------------------------------------------------------

		// Lấy dữ liệu cho Biểu đồ Doanh thu (Ví dụ: 30 ngày gần nhất)
		// Hàm getDailyRevenueDataForChart trả về List<Object[]>
		List<Object[]> dailyRevenueData = dashboardDao.getDailyRevenueDataForChart(last30Days, now);

		// Dữ liệu này cần được chuyển đổi sang định dạng JSON để JS có thể sử dụng dễ
		// dàng
		// (Bước chuyển đổi sang JSON không được hiển thị ở đây, nhưng là cần thiết
		// trong thực tế)
		// Giả sử bạn đã có một hàm tiện ích để chuyển đổi sang JSON string
		// String dailyRevenueJson = convertToJson(dailyRevenueData);

		// Để đơn giản, tôi chỉ truyền List<Object[]> sang JSP. Bạn phải xử lý việc
		// chuyển đổi JSON trong JSP.
		request.setAttribute("dailyRevenueData", dailyRevenueData);

		// 3. Chuyển tiếp đến JSP
		// --------------------------------------------------------------------------------
		RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp");
		rd.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
