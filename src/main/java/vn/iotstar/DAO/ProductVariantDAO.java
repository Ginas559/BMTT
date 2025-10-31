package vn.iotstar.DAO;

import jakarta.persistence.*;
import java.util.*;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Product;
import vn.iotstar.entities.ProductVariant;

public class ProductVariantDAO {

    public void save(ProductVariant v) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (v.getVariantId() == null) em.persist(v); else em.merge(v);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<ProductVariant> findByProduct(Product product) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT v FROM ProductVariant v WHERE v.product = :p ORDER BY v.color, v.size",
                    ProductVariant.class)
                .setParameter("p", product)
                .getResultList();
        } finally { em.close(); }
    }

    /** NEW: lấy theo productId (không cần entity Product) */
    public List<ProductVariant> findByProductId(Long productId) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT v FROM ProductVariant v WHERE v.product.productId = :pid ORDER BY v.color, v.size",
                    ProductVariant.class)
                .setParameter("pid", productId)
                .getResultList();
        } finally { em.close(); }
    }

    /** NEW: map color -> các size có stock > 0 (duy nhất, sắp xếp alpha) */
    public Map<String, List<String>> colorToSizes(Long productId) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            List<Object[]> rows = em.createQuery(
                    "SELECT DISTINCT v.color, v.size FROM ProductVariant v " +
                    "WHERE v.product.productId = :pid AND COALESCE(v.stock,0) > 0 " +
                    "ORDER BY v.color, v.size",
                    Object[].class)
                .setParameter("pid", productId)
                .getResultList();
            Map<String, List<String>> map = new LinkedHashMap<>();
            for (Object[] r : rows) {
                String color = (String) r[0];
                String size  = (String) r[1];
                map.computeIfAbsent(color, k -> new ArrayList<>()).add(size);
            }
            return map;
        } finally { em.close(); }
    }

    /** NEW: tồn kho theo (product, color, size) */
    public Integer stockOf(Long productId, String color, String size) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            Long total = em.createQuery(
                    "SELECT COALESCE(SUM(v.stock),0) FROM ProductVariant v " +
                    "WHERE v.product.productId = :pid AND LOWER(v.color) = :c AND LOWER(v.size) = :s",
                    Long.class)
                .setParameter("pid", productId)
                .setParameter("c", color.toLowerCase())
                .setParameter("s", size.toLowerCase())
                .getSingleResult();
            return total != null ? total.intValue() : 0;
        } finally { em.close(); }
    }

    /** NEW: tìm một biến thể cụ thể (color+size) */
    public ProductVariant findOne(Long productId, String color, String size) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT v FROM ProductVariant v " +
                    "WHERE v.product.productId = :pid AND LOWER(v.color)=:c AND LOWER(v.size)=:s",
                    ProductVariant.class)
                .setParameter("pid", productId)
                .setParameter("c", color.toLowerCase())
                .setParameter("s", size.toLowerCase())
                .setMaxResults(1)
                .getResultStream().findFirst().orElse(null);
        } finally { em.close(); }
    }

    /** NEW: map "color|size" -> variantId, chỉ lấy những biến thể có stock > 0 */
    public Map<String, Long> mapVariantIdByKey(Long productId) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            List<Object[]> rows = em.createQuery(
                    "SELECT v.color, v.size, v.variantId FROM ProductVariant v " +
                    "WHERE v.product.productId = :pid AND COALESCE(v.stock,0) > 0",
                    Object[].class)
                .setParameter("pid", productId)
                .getResultList();
            Map<String, Long> map = new HashMap<>();
            for (Object[] r : rows) {
                String color = (String) r[0];
                String size  = (String) r[1];
                Long id      = (Long)   r[2];
                String key = (color == null ? "" : color) + "|" + (size == null ? "" : size);
                map.put(key, id);
            }
            return map;
        } finally { em.close(); }
    }

    public void delete(Long id) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ProductVariant v = em.find(ProductVariant.class, id);
            if (v != null) em.remove(v);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally { em.close(); }
    }

    /** Giữ: tổng tồn kho cho bảng sản phẩm */
    public int sumStockByProductId(Long productId) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            Long total = em.createQuery(
                    "SELECT COALESCE(SUM(v.stock), 0) FROM ProductVariant v WHERE v.product.productId = :pid",
                    Long.class)
                .setParameter("pid", productId)
                .getSingleResult();
            return total != null ? total.intValue() : 0;
        } finally { em.close(); }
    }
}
