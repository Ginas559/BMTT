package vn.iotstar.DAO;

import java.util.List;

import vn.iotstar.entities.OrderItem;

public interface IOrderItemDao {

	List<OrderItem> findOrderItemsByOrderId(Long orderId);

}
