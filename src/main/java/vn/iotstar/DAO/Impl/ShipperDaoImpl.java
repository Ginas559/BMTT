package vn.iotstar.DAO.Impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import vn.iotstar.DAO.IOrderDao;
import vn.iotstar.DAO.IShipperDao;
import vn.iotstar.DAO.IUserDao;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Category;
import vn.iotstar.entities.Order;
import vn.iotstar.entities.Shipper;
import vn.iotstar.entities.Order.OrderStatus;
import vn.iotstar.entities.User;
import vn.iotstar.entities.User.Role;

public class ShipperDaoImpl implements IShipperDao {

	@Override
	public List<Order> availableOrders(int page, int pageSize) {
		EntityManager em = JPAConfig.getEntityManager();

		// JPQL để chọn các đơn hàng có trạng thái cụ thể
		String jpql = "SELECT o FROM Order o WHERE o.status = :status";

		try {
			// 1. Tạo TypedQuery
			// Giả định Order.class là lớp Entity của bạn
			TypedQuery<Order> query = em.createQuery(jpql, Order.class);

			// 2. Thiết lập tham số (Parameter) cho trạng thái
			query.setParameter("status", OrderStatus.CONFIRMED); // Truyền trạng thái vào

			// 3. Thiết lập Phân trang (Pagination)
			// Tính toán vị trí bắt đầu (offset)
			int startPosition = (page - 1) * pageSize;

			query.setFirstResult(startPosition); // Vị trí bắt đầu
			query.setMaxResults(pageSize); // Số lượng kết quả tối đa

			// 4. Thực thi truy vấn và trả về kết quả
			return query.getResultList();

		} catch (Exception e) {
			// Xử lý ngoại lệ (ví dụ: in stack trace hoặc ném lại RuntimeException)
			e.printStackTrace();
			return null; // Hoặc ném ngoại lệ để lớp gọi xử lý
		}
	}

	@Override
	public List<Order> myOrders(User user, int page, int pageSize) {
		EntityManager em = JPAConfig.getEntityManager();

		// JPQL đã sửa: Thêm khoảng trắng sau JOIN FETCH o.address
		String jpql = "SELECT o FROM Order o " + "JOIN FETCH o.address a " + // ✅ Thêm khoảng trắng và alias 'a'
				"JOIN FETCH o.shipper s " + "WHERE o.status = :status " + "AND s.id = :shipperId " + // So sánh qua ID
				"ORDER BY o.createdAt DESC";

		try {
			// ... (Các bước còn lại không đổi)
			TypedQuery<Order> query = em.createQuery(jpql, Order.class);

			// Thiết lập tham số (Parameter)
			query.setParameter("status", Order.OrderStatus.SHIPPING); // Truyền trạng thái ĐANG GIAO
			query.setParameter("shipperId", user.getId());

			// Thiết lập Phân trang (Pagination)
			int startPosition = (page - 1) * pageSize;
			query.setFirstResult(startPosition);
			query.setMaxResults(pageSize);

			// Thực thi truy vấn và trả về kết quả
			return query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	@Override
	public List<Order> history(User user, int page, int pageSize) {
		EntityManager em = JPAConfig.getEntityManager();

		// JPQL để chọn các đơn hàng có trạng thái cụ thể
		String jpql = "SELECT o FROM Order o " + "JOIN FETCH o.address a " + // ✅ Thêm khoảng trắng và alias 'a'
				"JOIN FETCH o.shipper s " + "WHERE o.status = :status " + "AND s.id = :shipperId " + // So sánh qua ID
				"ORDER BY o.createdAt DESC";

		try {
			// 1. Tạo TypedQuery
			// Giả định Order.class là lớp Entity của bạn
			TypedQuery<Order> query = em.createQuery(jpql, Order.class);

			// 2. Thiết lập tham số (Parameter) cho trạng thái
			query.setParameter("status", OrderStatus.DELIVERED); // Truyền trạng thái vào

			query.setParameter("shipperId", user.getId());

			// 3. Thiết lập Phân trang (Pagination)
			// Tính toán vị trí bắt đầu (offset)
			int startPosition = (page - 1) * pageSize;

			query.setFirstResult(startPosition); // Vị trí bắt đầu
			query.setMaxResults(pageSize); // Số lượng kết quả tối đa

			// 4. Thực thi truy vấn và trả về kết quả
			return query.getResultList();

		} catch (Exception e) {
			// Xử lý ngoại lệ (ví dụ: in stack trace hoặc ném lại RuntimeException)
			e.printStackTrace();
			return null; // Hoặc ném ngoại lệ để lớp gọi xử lý
		}
	}

	@Override
	public int countShippers(String keyword, String isBanned) { // ⚠️ Thay đổi kiểu dữ liệu tham số
	    EntityManager em = JPAConfig.getEntityManager();
	    try {
	        // 1. Loại bỏ JOIN FETCH, chỉ cần JOIN (hoặc không cần nếu không dùng u.trường_gì)
	        // Dùng JOIN để có thể lọc theo trường của User (u.firstname, u.email, ...)
	        String jpql = "SELECT COUNT(u) FROM User u WHERE 1=1 AND u.role = :role"; 
	        
	        if (keyword != null && !keyword.isEmpty()) {
	            jpql += " AND (LOWER(u.firstname) LIKE LOWER(:kw) OR LOWER(u.lastname) LIKE LOWER(:kw) OR LOWER(u.email) LIKE LOWER(:kw) OR LOWER(s.name) LIKE LOWER(:kw))"; 
	            // Thêm s.name nếu Shipper có trường tên riêng
	        }
	        
	        // 2. Sử dụng tham số Boolean
	        
	        TypedQuery<Long> query = em.createQuery(jpql, Long.class);

	        if (keyword != null && !keyword.isEmpty()) {
	            query.setParameter("kw", "%" + keyword + "%");
	        }
	        
	        // 3. Set tham số với giá trị Boolean
	        query.setParameter("role", Role.SHIPPER);

	        // JPQL COUNT trả về Long, cần chuyển về int
	        return query.getSingleResult().intValue();
	    } finally {
	        em.close();
	    }
	}

	@Override
	public List<User> searchShippers(String keyword, String banned, int page, int pageSize) {
		EntityManager em = JPAConfig.getEntityManager();
		try {
			String jpql = "SELECT u FROM User u WHERE 1=1 AND u.role = :role";
			if (keyword != null && !keyword.isEmpty()) {
				jpql += " AND (LOWER(u.firstname) LIKE LOWER(:kw) OR LOWER(u.lastname) LIKE LOWER(:kw) OR LOWER(u.email) LIKE LOWER(:kw))";
			}
			

			TypedQuery<User> query = em.createQuery(jpql, User.class);

			if (keyword != null && !keyword.isEmpty()) {
				query.setParameter("kw", "%" + keyword + "%");
			}
			
			query.setParameter("role", Role.SHIPPER);
			query.setFirstResult((page - 1) * pageSize);
			query.setMaxResults(pageSize);

			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	@Override
	public BigDecimal TotalRevenue(User user) {
	    EntityManager em = JPAConfig.getEntityManager();
	    try {
	        
	        
	        String jpql = "SELECT SUM(o.totalAmount) FROM Order o WHERE o.shipper = :user "
	                    + "AND o.status = :statusSuccess ";
	                    
	        
	        // Hoặc dùng JOIN FETCH như bạn đã bắt đầu, nhưng không cần thiết khi chỉ dùng SUM:
	        // String jpql = "SELECT SUM(o.totalPrice) FROM Order o JOIN o.user u WHERE u = :user ...";
	        
	        // Thực thi truy vấn
	        BigDecimal result = em.createQuery(jpql, BigDecimal.class)
	                .setParameter("user", user)
	                .setParameter("statusSuccess", OrderStatus.DELIVERED) // Thay 'SUCCESS' bằng enum/String status thực tế của bạn
	                // Đặt tham số ngày: Đảm bảo orderDate trong Order là kiểu Date/Timestamp hoặc tương đương
	                
	                .getSingleResult();
	        
	        // Xử lý trường hợp không có đơn hàng nào khớp
	        return result != null ? result : BigDecimal.ZERO;

	    } catch (Exception e) {
	        // Ghi log lỗi nếu cần
	        e.printStackTrace();
	        return BigDecimal.ZERO; // Trả về 0 nếu có lỗi xảy ra
	    } finally {
	        em.close();
	    }
	}

	@Override
	public int SuccessfulOrder(User user) {
	    EntityManager em = JPAConfig.getEntityManager();
	    try {
	        String jpql = "SELECT COUNT(o) FROM Order o WHERE o.shipper = :user "
	                    + "AND o.status = :statusSuccess";

	        // Thay int.class bằng Long.class
	        Long countResult = em.createQuery(jpql, Long.class)
	                .setParameter("user", user)
	                .setParameter("statusSuccess", OrderStatus.DELIVERED)
	                .getSingleResult();

	        // Chuyển đổi sang int
	        return countResult != null ? countResult.intValue() : 0;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return 0;
	    } finally {
	        em.close();
	    }
	}
	
}
