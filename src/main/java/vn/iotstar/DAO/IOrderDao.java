package vn.iotstar.DAO;

import java.util.List;

import vn.iotstar.entities.Category;
import vn.iotstar.entities.Order;
import vn.iotstar.entities.Order.OrderStatus;
import vn.iotstar.entities.Shop;

public interface IOrderDao {

	List<Order> searchOrders(OrderStatus status, Shop shop, int page, int pageSize);

	int countOrders(OrderStatus status, Shop shop);

	Order findById(Long id);

}
