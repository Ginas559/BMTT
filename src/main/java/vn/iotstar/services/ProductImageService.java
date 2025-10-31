// src/main/java/vn/iotstar/services/ProductImageService.java
package vn.iotstar.services;

import jakarta.persistence.*;
import vn.iotstar.configs.JPAConfig;

public class ProductImageService {

    public void addImage(Long productId, String imageUrl, boolean isThumbnail) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNativeQuery(
                "INSERT INTO Product_Image(image_url, is_thumbnail, product_id) VALUES(?,?,?)")
              .setParameter(1, imageUrl)
              .setParameter(2, isThumbnail ? 1 : 0)
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
            em.createNativeQuery("UPDATE Product_Image SET is_thumbnail = 0 WHERE product_id = ?")
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
            return (String) em.createNativeQuery(
                "SELECT TOP 1 image_url FROM Product_Image " +
                "WHERE product_id = ? AND is_thumbnail = 1")
                .setParameter(1, productId)
                .getResultStream()
                .findFirst()
                .orElse(null);
        } finally {
            em.close();
        }
    }
}
