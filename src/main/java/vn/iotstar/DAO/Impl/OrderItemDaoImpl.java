package vn.iotstar.DAO.Impl;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.iotstar.DAO.IOrderItemDao;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.OrderItem;

public class OrderItemDaoImpl implements IOrderItemDao {
	
	@Override
	public List<OrderItem> findOrderItemsByOrderId(Long orderId) {
        EntityManager em = JPAConfig.getEntityManager();
        
        try {
            // JPQL: Chọn tất cả OrderItem (oi) với điều kiện orderId của chúng khớp với ID truyền vào.
            // JOIN FETCH oi.product: RẤT QUAN TRỌNG! Để tránh Lazy Initialization Exception 
            // khi cố gắng truy cập thông tin sản phẩm (Product) trên JSP.
            String jpql = "SELECT oi FROM OrderItem oi JOIN FETCH oi.product WHERE oi.order.orderId = :orderId";
            
            TypedQuery<OrderItem> query = em.createQuery(jpql, OrderItem.class);
            query.setParameter("orderId", orderId);
            
            return query.getResultList();
            
        } finally {
            // Đảm bảo EntityManager luôn được đóng
            if (em != null && em.isOpen()) {
                 em.close();
            }
        }
    }
}
