// filepath: src/main/java/vn/iotstar/services/ProductImageService.java
package vn.iotstar.services;

import jakarta.persistence.*;
import vn.iotstar.configs.JPAConfig;

public class ProductImageService {

    public void addImage(Long productId, String imageUrl, boolean isThumbnail) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // FIX 1: Dùng "Product_Image" (có quote)
            // FIX 2: Dùng true/false thay vì 1/0 cho PostgreSQL
            em.createNativeQuery(
                "INSERT INTO \"Product_Image\"(image_url, is_thumbnail, product_id) VALUES(?,?,?)")
              .setParameter(1, imageUrl)
              .setParameter(2, isThumbnail) // <- Gửi thẳng boolean
              .setParameter(3, productId)
              .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void clearThumbnail(Long productId) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // FIX 1: Dùng "Product_Image" (có quote)
            em.createNativeQuery("UPDATE \"Product_Image\" SET is_thumbnail = false WHERE product_id = ?")
              .setParameter(1, productId)
              .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Lấy URL ảnh thumbnail của 1 sản phẩm (có thể trả về null). */
    public String getThumbnailUrl(Long productId) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            // FIX 1: Dùng "Product_Image" (có quote)
            // FIX 2: Bỏ "TOP 1", dùng "LIMIT 1" ở cuối
            // FIX 3: Dùng "is_thumbnail = true"
            return (String) em.createNativeQuery(
                "SELECT image_url FROM \"Product_Image\" " +
                "WHERE product_id = ? AND is_thumbnail = true " +
                "LIMIT 1") // <- FIX 2
                .setParameter(1, productId)
                .getResultStream()
                .findFirst()
                .orElse(null);
        } finally {
            em.close();
        }
    }
}