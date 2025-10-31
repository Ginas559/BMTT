package vn.iotstar.DAO.Impl;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import vn.iotstar.DAO.IShopDao;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Shop;
import vn.iotstar.entities.Shop.ShopStatus;

public class ShopDaoImpl implements IShopDao {
	
	@Override
	public void delete(Shop shop) {
		EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();
	        shop.setStatus(ShopStatus.BANNED);
	        em.merge(shop);
	        tx.commit();
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        e.printStackTrace();
	    } finally {
	        em.close();
	    }
		
	}

	@Override
	public void edit(Shop shop) {
		EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();
	        em.merge(shop); 
	        tx.commit();
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        e.printStackTrace();
	    } finally {
	        em.close();
	    }
		
	}

	@Override
	public void add(Shop shop) {
		EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();
	        em.persist(shop); // thêm mới category
	        tx.commit();
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        e.printStackTrace();
	    } finally {
	        em.close();
	    }
		
	}

	@Override
	public int countShops(String keyword, ShopStatus banned) {
		EntityManager em = JPAConfig.getEntityManager();
		try {
			String jpql = "SELECT COUNT(s) FROM Shop s WHERE 1=1";
		    if (keyword != null && !keyword.isEmpty()) {
		        jpql += " AND (LOWER(s.shopName) LIKE LOWER(:kw))";
		    }
		    if (banned != null) {
		        jpql += " AND s.status = :banned";
		    }

		    TypedQuery<Long> query = em.createQuery(jpql, Long.class);

		    if (keyword != null && !keyword.isEmpty()) {
		        query.setParameter("kw", "%" + keyword + "%");
		    }
		    if (banned != null) {
		        query.setParameter("banned", banned);
		    }

		    return query.getSingleResult().intValue();
		}finally {
			em.close();
		}
		
	}

	@Override
	public List<Shop> searchShops(String keyword, ShopStatus banned, int page, int pageSize) {
		EntityManager em = JPAConfig.getEntityManager();
		try {
			String jpql = "SELECT s FROM Shop s JOIN FETCH s.vendor WHERE 1=1";
		    if (keyword != null && !keyword.isEmpty()) {
		        jpql += " AND (LOWER(s.shopName) LIKE LOWER(:kw))";
		    }
		    if (banned != null) {
		        jpql += " AND c.status = :banned";
		    }

		    TypedQuery<Shop> query = em.createQuery(jpql, Shop.class);

		    if (keyword != null && !keyword.isEmpty()) {
		        query.setParameter("kw", "%" + keyword + "%");
		    }
		    if (banned != null) {
		        query.setParameter("banned", banned);
		    }

		    query.setFirstResult((page - 1) * pageSize);
		    query.setMaxResults(pageSize);

		    return query.getResultList();
		}finally {
			em.close();
		}
	}
	
	@Override
	public Shop findById(Long id) {
		EntityManager em = JPAConfig.getEntityManager();
		String jpql = "select s from Shop s JOIN FETCH s.vendor where s.shopId= :id";
		TypedQuery<Shop> query = em.createQuery(jpql, Shop.class);
		
		query.setParameter("id", id);
		return query.getSingleResult();
	}
	
	@Override
	public List<Shop> findAllShopsValidate(){
		EntityManager em = JPAConfig.getEntityManager();
		String jpql = "select s from Shop s JOIN FETCH s.vendor where s.status= :id";
		TypedQuery<Shop> query = em.createQuery(jpql, Shop.class);
		
		query.setParameter("id", ShopStatus.ACTIVE);
		return query.getResultList();
	}
}
