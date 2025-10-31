package vn.iotstar.DAO.Impl;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import vn.iotstar.DAO.IProductDao;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Category;
import vn.iotstar.entities.Product;
import vn.iotstar.entities.Shop;

public class ProductDaoImpl implements IProductDao {

	@Override
	public void delete(Product product) {
		EntityManager em = JPAConfig.getEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			product.setIsBanned(true);
			em.merge(product);
			tx.commit();
		} catch (Exception e) {
			if (tx.isActive())
				tx.rollback();
			e.printStackTrace();
		} finally {
			em.close();
		}

	}

	@Override
	public void edit(Product product) {
		EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();
	        em.merge(product); 
	        tx.commit();
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        e.printStackTrace();
	    } finally {
	        em.close();
	    }

	}

	@Override
	public void add(Product product) {
		// TODO Auto-generated method stub

	}

	@Override
	public int countProducts(String keyword, Boolean isBanned, Shop shop, Category category) {
		EntityManager em = JPAConfig.getEntityManager();
		try {
			String jpql = "SELECT COUNT(p) FROM Product p WHERE 1=1";
			if (keyword != null && !keyword.isEmpty()) {
				jpql += " AND (LOWER(p.productName) LIKE LOWER(:kw))";
			}

			if (isBanned != null) {
				jpql += " AND p.isBanned = :isBanned";
			}

			if (shop != null) {
				jpql += " AND p.shop = :shop";
			}

			if (category != null) {
				jpql += " AND p.category = :category";
			}

			TypedQuery<Long> query = em.createQuery(jpql, Long.class);

			if (keyword != null && !keyword.isEmpty()) {
				query.setParameter("kw", "%" + keyword + "%");
			}

			if (isBanned != null) {
				query.setParameter("isBanned", isBanned);
			}

			if (shop != null) {
				query.setParameter("shop", shop);
			}

			if (category != null) {
				query.setParameter("category", category);
			}

			return query.getSingleResult().intValue();
		} finally {
			em.close();
		}
	}

	@Override
	public List<Product> searchProducts(String keyword, Boolean isBanned, Shop shop, Category category, int page, int pageSize) {
		EntityManager em = JPAConfig.getEntityManager();
		try {
			String jpql = "SELECT p FROM Product p JOIN FETCH p.category JOIN FETCH p.shop WHERE 1=1";
			if (keyword != null && !keyword.isEmpty()) {
				jpql += " AND (LOWER(p.productName) LIKE LOWER(:kw))";
			}
			if (isBanned != null) {
				jpql += " AND p.isBanned = :isBanned";
			}

			if (shop != null) {
				jpql += " AND p.shop = :shop";
			}

			if (category != null) {
				jpql += " AND p.category = :category";
			}

			TypedQuery<Product> query = em.createQuery(jpql, Product.class);

			if (keyword != null && !keyword.isEmpty()) {
				query.setParameter("kw", "%" + keyword + "%");
			}
			if (isBanned != null) {
				query.setParameter("isBanned", isBanned);
			}

			if (shop != null) {
				query.setParameter("shop", shop);
			}

			if (category != null) {
				query.setParameter("category", category);
			}

			query.setFirstResult((page - 1) * pageSize);
			query.setMaxResults(pageSize);

			return query.getResultList();
		} finally {
			em.close();
		}

	}

	@Override
	public Product findById(Long id) {
		EntityManager em = JPAConfig.getEntityManager();
		String jpql = "select p from Product p JOIN FETCH p.category JOIN FETCH p.shop where p.productId = :id";
		TypedQuery<Product> query = em.createQuery(jpql, Product.class);

		query.setParameter("id", id);
		return query.getSingleResult();
	}

}
