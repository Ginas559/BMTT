package vn.iotstar.DAO.Impl;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import vn.iotstar.DAO.IProductCommentDao;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.ProductComment;

public class ProductCommentDaoImpl implements IProductCommentDao {
	
	@Override
	public List<ProductComment> productComments(Long productId) {
	    EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = null;
	    List<ProductComment> comments = null;
	    
	    // JPQL đã điều chỉnh: Lọc theo Product ID và sắp xếp theo ngày tạo
	    String jpql = "SELECT pc FROM ProductComment pc " +
	                  "JOIN FETCH pc.user u " + // Tải User cùng lúc để tránh N+1, nếu cần
	                  "WHERE pc.product.productId = :productId " + // ✅ Điều chỉnh này là quan trọng
	                  "ORDER BY pc.createdAt DESC"; // Sắp xếp comment mới nhất lên đầu

	    try {
	        tx = em.getTransaction();
	        tx.begin();

	        comments = em.createQuery(jpql, ProductComment.class)
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
	    return comments;
	}
}
