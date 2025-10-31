package vn.iotstar.DAO.Impl;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import vn.iotstar.DAO.IProductReviewDao;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.ProductReview;

public class ProductReviewDaoImpl implements IProductReviewDao {
	
	@Override
	public List<ProductReview> productReviews(Long productId){
	    
	    EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = null;
	    List<ProductReview> reviews = null;
	    
	    // JPQL ĐÃ ĐƯỢC CHỈNH SỬA
	    String jpql = "SELECT pr FROM ProductReview pr " +
	                  "JOIN FETCH pr.user u " + // Tải User (người đánh giá)
	                  "JOIN pr.orderItem oi " + // Tham gia vào OrderItem
	                  "WHERE oi.product.productId = :productId " + // ✅ Lọc theo ID của Product trong OrderItem
	                  "ORDER BY pr.createdAt DESC"; // Sắp xếp đánh giá mới nhất lên đầu

	    try {
	        tx = em.getTransaction();
	        tx.begin();

	        reviews = em.createQuery(jpql, ProductReview.class)
	                     .setParameter("productId", productId) // Thiết lập tham số productId
	                     .getResultList();

	        tx.commit();
	        
	    } catch (Exception e) {
	        if (tx != null && tx.isActive()) {
	            tx.rollback();
	        }
	        e.printStackTrace();
	        // Có thể ném lại ngoại lệ RuntimeException nếu không muốn trả về null
	    } finally {
	        if (em != null) {
	            em.close();
	        }
	    }
	    return reviews;
	}
}
