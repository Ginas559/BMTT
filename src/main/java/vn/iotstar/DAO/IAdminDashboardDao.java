package vn.iotstar.DAO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import vn.iotstar.entities.Order.OrderStatus;

public interface IAdminDashboardDao {

	List<Object[]> getDailyRevenueDataForChart(LocalDateTime startDate, LocalDateTime endDate);

	long calculateTotalInventoryQuantity();

	long countTotalUsers();

	long countOrdersByStatusAndDateRange(LocalDateTime startDate, LocalDateTime endDate, OrderStatus status);

	BigDecimal calculateRevenue(LocalDateTime startDate, LocalDateTime endDate);

}
