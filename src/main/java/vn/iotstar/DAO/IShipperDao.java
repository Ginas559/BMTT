package vn.iotstar.DAO;

import java.math.BigDecimal;
import java.util.List;

import vn.iotstar.entities.Order;
import vn.iotstar.entities.Shipper;
import vn.iotstar.entities.User;

public interface IShipperDao {

	List<Order> history(User user, int page, int pageSize);

	List<Order> myOrders(User user, int page, int pageSize);

	List<Order> availableOrders(int page, int pageSize);

	List<User> searchShippers(String keyword, String banned, int page, int pageSize);

	int countShippers(String keyword, String banned);

	int SuccessfulOrder(User user);

	BigDecimal TotalRevenue(User user);


}
