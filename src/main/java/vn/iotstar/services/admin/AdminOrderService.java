package vn.iotstar.services.admin;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transaction;
import vn.iotstar.DAO.IOrderDao;
import vn.iotstar.DAO.IOrderItemDao;
import vn.iotstar.DAO.Impl.OrderDaoImpl;
import vn.iotstar.DAO.Impl.OrderItemDaoImpl;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Category;
import vn.iotstar.entities.Order;
import vn.iotstar.entities.Shop;
import vn.iotstar.entities.Order.OrderStatus;
import vn.iotstar.entities.OrderItem;

public class AdminOrderService {
	private IOrderDao orderDao = new OrderDaoImpl();
	
	public List<Order> searchOrders(OrderStatus status, Shop shop, int page, int pageSize){
		return orderDao.searchOrders(status, shop, page, pageSize);
	}
	
	public int countOrders(OrderStatus status, Shop shop) {
		return orderDao.countOrders(status, shop);
	}
	
	public Order findById(Long id) {
		return orderDao.findById(id);
	}
	
	private IOrderItemDao orderItemDAO = new OrderItemDaoImpl();

    public List<OrderItem> getOrderItemsForDetail(Long orderId) {
        // Có thể thêm logic kiểm tra ở đây, ví dụ:
        // - Đơn hàng có tồn tại không?
        // - Người dùng hiện tại có quyền xem đơn hàng này không?
        
        if (orderId == null) {
            return List.of(); // Trả về danh sách rỗng nếu ID null
        }
        
        return orderItemDAO.findOrderItemsByOrderId(orderId);
    }
}
