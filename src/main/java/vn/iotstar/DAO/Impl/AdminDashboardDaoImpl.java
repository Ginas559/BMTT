package vn.iotstar.DAO.Impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Order;
import vn.iotstar.DAO.IAdminDashboardDao;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Order.OrderStatus;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;

// Giả định: JPAConfig là lớp cấu hình của bạn, và Order là Entity
// Giả định: Order có trường 'totalAmount' (BigDecimal) và 'createdAt' (LocalDate)

public class AdminDashboardDaoImpl implements IAdminDashboardDao {

	@Override
	// Đổi tên hàm sang tiếng Anh: calculateRevenue
	public BigDecimal calculateRevenue(LocalDateTime startDate, LocalDateTime endDate) {
		// Sửa lỗi chính tả: Entitymananger -> EntityManager
		EntityManager em = JPAConfig.getEntityManager();

		// Khối try-finally để đảm bảo EntityManager được đóng nếu cần
		try {
			// Thay đổi JPQL để TÍNH TỔNG (SUM) của trường doanh thu (giả sử là totalAmount)
			// Thay vì chọn đối tượng Order, ta chọn SUM(o.totalAmount)
			String jpql = "SELECT SUM(o.totalAmount) FROM Order o "
					+ "WHERE o.createdAt BETWEEN :startDate AND :endDate " + "AND o.status = :status";

			// Sử dụng TypedQuery để lấy ra BigDecimal
			TypedQuery<BigDecimal> query = em.createQuery(jpql, BigDecimal.class);

			// Đặt các tham số (parameters)
			// 1. Tham số ngày tháng
			// Trong JPQL, BETWEEN bao gồm cả hai ngày.
			query.setParameter("startDate", startDate);
			// Để đảm bảo doanh thu tính đến hết ngày 'endDate',
			// có thể bạn cần chuyển 'endDate' thành 'LocalDateTime' hoặc sử dụng logic khác
			// nếu 'createdAt' là 'LocalDateTime' hoặc 'Timestamp'.
			// Ở đây tôi giữ nguyên 'LocalDate' theo mã gốc.
			query.setParameter("endDate", endDate);

			// 2. Tham số trạng thái
			// Bạn cần xác định trạng thái nào là "đã hoàn thành" hoặc "đã thanh toán"
			// Giả sử có một Enum OrderStatus và trạng thái thành công là
			// OrderStatus.COMPLETED
			// Nếu status là String, bạn có thể truyền thẳng String vào.
			// Ví dụ: query.setParameter("status", OrderStatus.COMPLETED);
			// Hoặc nếu status là String:
			query.setParameter("status", OrderStatus.DELIVERED);

			// Lấy kết quả: getSingleResult() sẽ trả về tổng (SUM).
			// Kết quả có thể là NULL nếu không có đơn hàng nào, nên cần kiểm tra.
			BigDecimal result = query.getSingleResult();

			// Trả về kết quả, nếu null thì trả về 0
			return result != null ? result : BigDecimal.ZERO;

		} finally {
			// Đóng EntityManager sau khi sử dụng (tùy thuộc vào cách quản lý của bạn)
			if (em != null && em.isOpen()) {
				// em.close(); // Bỏ ghi chú nếu bạn quản lý EntityManager thủ công
			}
		}
	}

	@Override
	// Đổi tên hàm sang tiếng Anh: countOrdersByStatusAndDateRange
	public long countOrdersByStatusAndDateRange(LocalDateTime startDate, LocalDateTime endDate, OrderStatus status) {

		// 1. Lấy EntityManager
		EntityManager em = JPAConfig.getEntityManager();

		try {
			// 2. Viết JPQL: Sử dụng hàm COUNT() và thêm điều kiện ngày tháng
			// Sửa lỗi cú pháp: string -> String
			String jpql = "SELECT COUNT(o) FROM Order o " + "WHERE o.createdAt BETWEEN :startDate AND :endDate "
					+ "AND o.status = :status";

			// 3. Tạo TypedQuery: COUNT() trả về kiểu Long
			TypedQuery<Long> query = em.createQuery(jpql, Long.class);

			// 4. Đặt các tham số (parameters)
			query.setParameter("startDate", startDate);
			query.setParameter("endDate", endDate);
			query.setParameter("status", status);
			// Giả sử 'status' trong entity Order là kiểu OrderStatus

			// 5. Lấy kết quả
			// Sử dụng getSingleResult() để lấy kết quả của hàm tổng hợp COUNT()
			Long countResult = query.getSingleResult();

			// Xử lý trường hợp countResult là null (thường không xảy ra với COUNT,
			// nhưng an toàn hơn khi kiểm tra)
			return countResult != null ? countResult : 0L;

		} catch (NoResultException e) {
			// Nếu không có kết quả nào (count là 0), trả về 0
			return 0L;
		} finally {
			// Đảm bảo EntityManager được đóng nếu bạn quản lý thủ công
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	public long countTotalUsers() {
		EntityManager em = JPAConfig.getEntityManager();

		try {
			// JPQL: Đếm tổng số lượng Entity User
			String jpql = "SELECT COUNT(u) FROM User u";

			// Tạo TypedQuery<Long> vì COUNT() trả về kiểu Long
			TypedQuery<Long> query = em.createQuery(jpql, Long.class);

			Long countResult = query.getSingleResult();

			// Trả về kết quả, nếu null thì trả về 0L
			return countResult != null ? countResult : 0L;

		} finally {
			// (Đóng EntityManager nếu cần)
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	public long calculateTotalInventoryQuantity() {
		EntityManager em = JPAConfig.getEntityManager();

		try {
			// JPQL: TÍNH TỔNG (SUM) của trường quantity (số lượng tồn kho)
			// Giả định tên trường là 'quantity'
			String jpql = "SELECT SUM(p.stock) FROM Product p";

			// Tạo TypedQuery<Long> vì SUM của số nguyên có thể trả về Long
			TypedQuery<Long> query = em.createQuery(jpql, Long.class);

			Long sumResult = query.getSingleResult();

			// Trả về tổng số lượng tồn kho. Nếu không có sản phẩm nào (null), trả về 0.
			return sumResult != null ? sumResult : 0L;

		} finally {
			// (Đóng EntityManager nếu cần)
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	public List<Object[]> getDailyRevenueDataForChart(LocalDateTime startDate, LocalDateTime endDate) {

		EntityManager em = JPAConfig.getEntityManager();

		try {
			// JPQL: Chọn Ngày (sử dụng FUNCTION('DATE',...) để trích xuất ngày từ
			// LocalDateTime)
			// và tính TỔNG (SUM) doanh thu của ngày đó.
			String jpql = "SELECT CAST(o.createdAt AS java.sql.Date), SUM(o.totalAmount) FROM Order o "
		            + "WHERE o.createdAt BETWEEN :startDate AND :endDate " 
		            + "AND o.status = :status "
		            + "GROUP BY CAST(o.createdAt AS java.sql.Date) " 
		            + "ORDER BY CAST(o.createdAt AS java.sql.Date) ASC";
			// Sử dụng TypedQuery<Object[]> vì ta đang chọn nhiều cột có kiểu khác nhau.
			TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);

			// Đặt các tham số ngày tháng (LocalDateTime)
			query.setParameter("startDate", startDate);
			query.setParameter("endDate", endDate);

			// Đặt tham số trạng thái (Chỉ tính đơn hàng đã hoàn thành)
			// Giả định: OrderStatus.COMPLETED là trạng thái đơn hàng thành công
			query.setParameter("status", OrderStatus.DELIVERED);

			// Thực thi truy vấn và trả về danh sách kết quả
			return query.getResultList();

		} finally {
			// (Đóng EntityManager nếu cần)
		}
	}
}
