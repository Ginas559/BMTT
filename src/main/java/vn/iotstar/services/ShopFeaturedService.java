// src/main/java/vn/iotstar/services/ShopFeaturedService.java
package vn.iotstar.services;

import jakarta.persistence.*;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Shop;
import java.util.*;

public class ShopFeaturedService {

    public List<String> getImages(Long shopId) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            List<String> urls = em.createNativeQuery(
                "SELECT url FROM shop_featured_images WHERE shop_id = ? ORDER BY id DESC")
                .setParameter(1, shopId)
                .getResultList();
            return urls;
        } finally {
            em.close();
        }
    }

    public void addImage(Long shopId, String url) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNativeQuery(
                "INSERT INTO shop_featured_images (shop_id, url) VALUES (?, ?)")
                .setParameter(1, shopId)
                .setParameter(2, url)
                .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Long id, Long shopId) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNativeQuery(
                "DELETE FROM shop_featured_images WHERE id = ? AND shop_id = ?")
                .setParameter(1, id)
                .setParameter(2, shopId)
                .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
