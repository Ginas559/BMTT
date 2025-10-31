package vn.iotstar.services.shipper;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import vn.iotstar.DAO.IOrderDao;
import vn.iotstar.DAO.IShipperDao;
import vn.iotstar.DAO.IShipperPostDao;
import vn.iotstar.DAO.Impl.OrderDaoImpl;
import vn.iotstar.DAO.Impl.ShipperDaoImpl;
import vn.iotstar.DAO.Impl.ShipperPostDaoImpl;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Order;
import vn.iotstar.entities.Shipper;
import vn.iotstar.entities.ShipperPost;
import vn.iotstar.entities.User;

public class ShipperService {
	
	private IShipperDao shipperDao = new ShipperDaoImpl();
	private IOrderDao orderDao = new OrderDaoImpl();
	private IShipperPostDao shipperPostDao = new ShipperPostDaoImpl();
	
	public List<Order> availableOrders(int page, int pageSize){
		return shipperDao.availableOrders(page, pageSize);
	}
	
	public List<Order> myOrders(User user, int page, int pageSize){
		return shipperDao.myOrders(user, page, pageSize);
	}
	
	public List<Order> history(User user, int page, int pageSize){
		return shipperDao.history(user, page, pageSize);
	}
	
	
	// Giả định lớp này là OrderService hoặc ShipperOrderService

	public void assignOrderToShipper(Long orderId, Long shipperId) {
	    EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();

	        // 1. Tìm kiếm Order và Shipper bằng ID
	        Order order = em.find(Order.class, orderId);
	        User shipper = em.find(User.class, shipperId);

	        // 2. Kiểm tra tính hợp lệ
	        if (order == null) {
	            throw new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId);
	        }
	        if (shipper == null) {
	            throw new IllegalArgumentException("Không tìm thấy Shipper với ID: " + shipperId);
	        }

	        // 3. Kiểm tra Logic nghiệp vụ (Trạng thái đơn hàng)
	        // Đơn hàng chỉ được gán khi đang ở trạng thái CONFIRMED (Đã được Shop xác nhận)
	        if (order.getStatus() != Order.OrderStatus.CONFIRMED) {
	            throw new IllegalStateException(
	                "Đơn hàng không khả dụng. Trạng thái hiện tại: " + order.getStatus().name()
	            );
	        }
	        
	        // 4. Thực hiện gán và cập nhật trạng thái
	        order.setShipper(shipper);
	        order.setStatus(Order.OrderStatus.SHIPPING);
	        

	        // em.merge() không cần thiết nếu order là managed (dùng em.find), 
	        // nhưng an toàn hơn.
	        em.merge(order);

	        tx.commit();
	        
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        e.printStackTrace();
	        // Ném lại ngoại lệ để lớp Controller/Web có thể bắt và hiển thị lỗi
	        throw new RuntimeException("Lỗi khi gán đơn hàng cho Shipper: " + e.getMessage(), e);
	    } finally {
	        em.close();
	    }
	}
	
	public void completeOrder(Long orderId, Long shipperId) {
		EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();

	        // 1. Tìm kiếm Order và Shipper bằng ID
	        Order order = em.find(Order.class, orderId);
	        User shipper = em.find(User.class, shipperId);

	        // 2. Kiểm tra tính hợp lệ
	        if (order == null) {
	            throw new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId);
	        }
	        if (shipper == null) {
	            throw new IllegalArgumentException("Không tìm thấy Shipper với ID: " + shipperId);
	        }

	        // 3. Kiểm tra Logic nghiệp vụ (Trạng thái đơn hàng)
	        // Đơn hàng chỉ được gán khi đang ở trạng thái CONFIRMED (Đã được Shop xác nhận)
	        if (order.getStatus() != Order.OrderStatus.SHIPPING) {
	            throw new IllegalStateException(
	                "Đơn hàng không khả dụng. Trạng thái hiện tại: " + order.getStatus().name()
	            );
	        }
	        
	        // 4. Thực hiện gán và cập nhật trạng thái
	        order.setShipper(shipper);
	        order.setStatus(Order.OrderStatus.DELIVERED);
	        

	        // em.merge() không cần thiết nếu order là managed (dùng em.find), 
	        // nhưng an toàn hơn.
	        em.merge(order);

	        tx.commit();
	        
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        e.printStackTrace();
	        // Ném lại ngoại lệ để lớp Controller/Web có thể bắt và hiển thị lỗi
	        throw new RuntimeException("Lỗi khi gán đơn hàng cho Shipper: " + e.getMessage(), e);
	    } finally {
	        em.close();
	    }
	}
	
	public void dropOrder(Long orderId, Long shipperId) {
		EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();

	        // 1. Tìm kiếm Order và Shipper bằng ID
	        Order order = em.find(Order.class, orderId);
	        User shipper = em.find(User.class, shipperId);

	        // 2. Kiểm tra tính hợp lệ
	        if (order == null) {
	            throw new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId);
	        }
	        if (shipper == null) {
	            throw new IllegalArgumentException("Không tìm thấy Shipper với ID: " + shipperId);
	        }

	        // 3. Kiểm tra Logic nghiệp vụ (Trạng thái đơn hàng)
	        // Đơn hàng chỉ được gán khi đang ở trạng thái CONFIRMED (Đã được Shop xác nhận)
	        if (order.getStatus() != Order.OrderStatus.SHIPPING) {
	            throw new IllegalStateException(
	                "Đơn hàng không khả dụng. Trạng thái hiện tại: " + order.getStatus().name()
	            );
	        }
	        
	        // 4. Thực hiện gán và cập nhật trạng thái
	        order.setShipper(shipper);
	        order.setStatus(Order.OrderStatus.CONFIRMED);
	        

	        // em.merge() không cần thiết nếu order là managed (dùng em.find), 
	        // nhưng an toàn hơn.
	        em.merge(order);

	        tx.commit();
	        
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        e.printStackTrace();
	        // Ném lại ngoại lệ để lớp Controller/Web có thể bắt và hiển thị lỗi
	        throw new RuntimeException(" Shipper không hủy đơn được " + e.getMessage(), e);
	    } finally {
	        em.close();
	    }
	}
	
	public List<ShipperPost> shipperPosts(int page, int pagesize){
		return shipperPostDao.shipperPosts(page, pagesize);
	}
	
	public int countShipperPosts() {
		return shipperPostDao.countShipperPosts();
	}
	
	public void addPost(ShipperPost post) {
		shipperPostDao.addPost(post);
		return;
	}
	
	public List<User> searchShippers(String keyword, String banned, int page, int pageSize){
		return shipperDao.searchShippers(keyword, banned, page, pageSize);
	}
	
	public int countShipper(String keyword, String banned) {
		return shipperDao.countShippers(keyword, banned);
	}
	
	public int SuccessfulOrder(User user) {
		return shipperDao.SuccessfulOrder(user);
	}
	
	public BigDecimal TotalRevenue(User user) {
		return shipperDao.TotalRevenue(user);
	}
	
	
}

