package vn.iotstar.DAO.Impl;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import vn.iotstar.DAO.IPromotionDao;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Promotion;
import vn.iotstar.entities.Promotion.PromotionStatus;

public class PromotionDaoImpl implements IPromotionDao {
	
	@Override
	public void delete(Promotion promotion) { // Thay đổi Category -> Promotion
		EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();
	        // Dùng trạng thái (status) để xóa mềm, giả sử muốn đổi sang INACTIVE
	        promotion.setStatus(PromotionStatus.INACTIVE); 
	        em.merge(promotion);
	        tx.commit();
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        e.printStackTrace();
	    } finally {
	        em.close();
	    }
		
	}

	@Override
	public void edit(Promotion promotion) {
		EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();
	        em.merge(promotion); 
	        tx.commit();
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        e.printStackTrace();
	    } finally {
	        em.close();
	    }
		
	}

	@Override
	public void add(Promotion promotion) {
		EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();
	        em.persist(promotion); // thêm mới category
	        tx.commit();
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        e.printStackTrace();
	    } finally {
	        em.close();
	    }
		
	}

	@Override
	public int countPromotions(String keyword, PromotionStatus status) { // Thay đổi Category -> Promotion và Boolean banned -> PromotionStatus status
		EntityManager em = JPAConfig.getEntityManager();
		try {
			// Sửa JPQL: Thay Category -> Promotion. Thay c.isBanned -> p.status
			String jpql = "SELECT COUNT(p) FROM Promotion p WHERE 1=1";
		    if (keyword != null && !keyword.isEmpty()) {
		        // Sửa: c.categoryName -> p.title
		        jpql += " AND (LOWER(p.title) LIKE LOWER(:kw))"; 
		    }
		    if (status != null) {
		        jpql += " AND p.status = :status";
		    }

		    TypedQuery<Long> query = em.createQuery(jpql, Long.class);

		    if (keyword != null && !keyword.isEmpty()) {
		        query.setParameter("kw", "%" + keyword + "%");
		    }
		    if (status != null) {
		        query.setParameter("status", status);
		    }

		    return query.getSingleResult().intValue();
		}finally {
			em.close();
		}
		
	}

    // Phương thức tìm kiếm và phân trang Promotion
	@Override
	public List<Promotion> searchPromotions(String keyword, PromotionStatus status, int page, int pageSize) { // Thay đổi Category -> Promotion và Boolean banned -> PromotionStatus status
		EntityManager em = JPAConfig.getEntityManager();
		try {
			// Sửa JPQL: Thay Category -> Promotion. Thay c.isBanned -> p.status
			String jpql = "SELECT p FROM Promotion p join fetch p.shop WHERE 1=1";
		    if (keyword != null && !keyword.isEmpty()) {
		        // Sửa: c.categoryName -> p.title
		        jpql += " AND (LOWER(p.title) LIKE LOWER(:kw))"; 
		    }
		    if (status != null) {
		        jpql += " AND p.status = :status";
		    }
            // Thêm sắp xếp mặc định (nên sắp xếp theo promotionId)
            jpql += " ORDER BY p.promotionId DESC"; 

		    TypedQuery<Promotion> query = em.createQuery(jpql, Promotion.class); // Thay đổi Category -> Promotion

		    if (keyword != null && !keyword.isEmpty()) {
		        query.setParameter("kw", "%" + keyword + "%");
		    }
		    if (status != null) {
		        query.setParameter("status", status);
		    }

		    query.setFirstResult((page - 1) * pageSize);
		    query.setMaxResults(pageSize);

		    return query.getResultList();
		}finally {
			em.close();
		}
	}
	
    // Phương thức tìm kiếm theo ID
	@Override
	public Promotion findById(Long id) { // Thay đổi Category -> Promotion
		EntityManager em = JPAConfig.getEntityManager();
        // Sửa JPQL: Thay Category -> Promotion. Sửa c.categoryId -> p.promotionId
		String jpql = "select p from Promotion p where p.promotionId = :id";
		TypedQuery<Promotion> query = em.createQuery(jpql, Promotion.class); // Thay đổi Category -> Promotion
		
		query.setParameter("id", id);
        // Dùng getSingleResult() nếu chắc chắn có, hoặc getResultList().stream().findFirst().orElse(null)
		return query.getSingleResult(); 
	}
}
