package vn.iotstar.DAO.Impl;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import vn.iotstar.DAO.IOrderDao;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Category;
import vn.iotstar.entities.Order.OrderStatus;
import vn.iotstar.entities.Order;
import vn.iotstar.entities.Shop;

public class OrderDaoImpl implements IOrderDao {
	
	@Override
	public List<Order> searchOrders(OrderStatus status, Shop shop, int page, int pageSize) {
		EntityManager em = JPAConfig.getEntityManager();
		try {
			String jpql = "SELECT o FROM Order o JOIN FETCH o.user JOIN FETCH o.shop WHERE 1=1";
			
			if (status != null) {
				jpql += " AND o.status = :status";
			}

			if (shop != null) {
				jpql += " AND o.shop = :shop";
			}

			

			TypedQuery<Order> query = em.createQuery(jpql, Order.class);

			
			if (status != null) {
				query.setParameter("status", status);
			}

			if (shop != null) {
				query.setParameter("shop", shop);
			}

			

			query.setFirstResult((page - 1) * pageSize);
			query.setMaxResults(pageSize);

			return query.getResultList();
		} finally {
			em.close();
		}

	}
	
	@Override
	public int countOrders(OrderStatus status, Shop shop) {
		EntityManager em = JPAConfig.getEntityManager();
		try {
			String jpql = "SELECT COUNT(o) FROM Order o  WHERE 1=1";

			if (status != null) {
				jpql += " AND o.status = :status";
			}

			if (shop != null) {
				jpql += " AND o.shop = :shop";
			}
			TypedQuery<Long> query = em.createQuery(jpql, Long.class);

			

			if (status != null) {
				query.setParameter("status", status);
			}

			if (shop != null) {
				query.setParameter("shop", shop);
			}


			return query.getSingleResult().intValue();
		} finally {
			em.close();
		}
	}
	
	@Override
	public Order findById(Long id) {
		EntityManager em = JPAConfig.getEntityManager();
		String jpql = "select o from Order o JOIN FETCH o.user JOIN FETCH o.shop where o.orderId = :id";
		TypedQuery<Order> query = em.createQuery(jpql, Order.class);
		
		query.setParameter("id", id);
		return query.getSingleResult();
	}

	
}
