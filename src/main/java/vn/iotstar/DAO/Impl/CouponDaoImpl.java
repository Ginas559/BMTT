package vn.iotstar.DAO.Impl;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.iotstar.DAO.ICouponDao;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Coupon;
import vn.iotstar.entities.Shop;

public class CouponDaoImpl implements ICouponDao {
	
	@Override
	public int countCoupons(String keyword, Shop shop) {
		EntityManager em = JPAConfig.getEntityManager();
		try {
			String jpql = "SELECT COUNT(c) FROM Coupon c WHERE 1=1";
		    if (keyword != null && !keyword.isEmpty()) {
		        jpql += " AND (LOWER(c.code) LIKE LOWER(:kw))";
		    }
		    if (shop != null) {
		        jpql += " AND c.shop = :shop";
		    }

		    TypedQuery<Long> query = em.createQuery(jpql, Long.class);

		    if (keyword != null && !keyword.isEmpty()) {
		        query.setParameter("kw", "%" + keyword + "%");
		    }
		    if (shop != null) {
		        query.setParameter("shop", shop);
		    }

		    return query.getSingleResult().intValue();
		}finally {
			em.close();
		}
		
	}

	@Override
	public List<Coupon> searchCoupons(String keyword, Shop shop, int page, int pageSize) {
		EntityManager em = JPAConfig.getEntityManager();
		try {
			String jpql = "SELECT c FROM Coupon c WHERE 1=1";
		    if (keyword != null && !keyword.isEmpty()) {
		        jpql += " AND (LOWER(c.code) LIKE LOWER(:kw))";
		    }
		    if (shop != null) {
		        jpql += " AND c.shop = :shop";
		    }

		    TypedQuery<Coupon> query = em.createQuery(jpql, Coupon.class);

		    if (keyword != null && !keyword.isEmpty()) {
		        query.setParameter("kw", "%" + keyword + "%");
		    }
		    if (shop != null) {
		        query.setParameter("shop", shop);
		    }

		    query.setFirstResult((page - 1) * pageSize);
		    query.setMaxResults(pageSize);

		    return query.getResultList();
		}finally {
			em.close();
		}
	}
}
