package vn.iotstar.DAO.Impl;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import vn.iotstar.DAO.ICategoryDao;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Category;
import vn.iotstar.entities.User;

public class CategoryDaoImpl implements ICategoryDao {

	@Override
	public void delete(Category category) {
		EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();
	        category.setIsBanned(true);
	        em.merge(category);
	        tx.commit();
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        e.printStackTrace();
	    } finally {
	        em.close();
	    }
		
	}

	@Override
	public void edit(Category category) {
		EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();
	        em.merge(category); 
	        tx.commit();
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        e.printStackTrace();
	    } finally {
	        em.close();
	    }
		
	}

	@Override
	public void add(Category category) {
		EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();
	        em.persist(category); // thêm mới category
	        tx.commit();
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        e.printStackTrace();
	    } finally {
	        em.close();
	    }
		
	}

	@Override
	public int countCategories(String keyword, Boolean banned) {
		EntityManager em = JPAConfig.getEntityManager();
		try {
			String jpql = "SELECT COUNT(c) FROM Category c WHERE 1=1";
		    if (keyword != null && !keyword.isEmpty()) {
		        jpql += " AND (LOWER(c.categoryName) LIKE LOWER(:kw))";
		    }
		    if (banned != null) {
		        jpql += " AND c.isBanned = :banned";
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
	public List<Category> searchCategories(String keyword, Boolean banned, int page, int pageSize) {
		EntityManager em = JPAConfig.getEntityManager();
		try {
			String jpql = "SELECT c FROM Category c WHERE 1=1";
		    if (keyword != null && !keyword.isEmpty()) {
		        jpql += " AND (LOWER(c.categoryName) LIKE LOWER(:kw))";
		    }
		    if (banned != null) {
		        jpql += " AND c.isBanned = :banned";
		    }

		    TypedQuery<Category> query = em.createQuery(jpql, Category.class);

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
	public Category findById(Long id) {
		EntityManager em = JPAConfig.getEntityManager();
		String jpql = "select c from Category c where c.categoryId = :id";
		TypedQuery<Category> query = em.createQuery(jpql, Category.class);
		
		query.setParameter("id", id);
		return query.getSingleResult();
	}
	
	@Override
	public List<Category> findAllCategoriesValidate() {
		EntityManager em = JPAConfig.getEntityManager();
		String jpql = "select c from Category c where c.isBanned = :id";
		TypedQuery<Category> query = em.createQuery(jpql, Category.class);
		
		query.setParameter("id", false);
		return query.getResultList();
	}
}
