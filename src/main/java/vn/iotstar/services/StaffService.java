// src/main/java/vn/iotstar/services/StaffService.java
package vn.iotstar.services;

import jakarta.persistence.*;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Shop;
import vn.iotstar.entities.User;

import java.util.List;
import java.util.Objects;

public class StaffService {

    private EntityManager em() { return JPAConfig.getEntityManager(); }

    /** Lấy danh sách nhân viên (USER) của 1 shop (staffShop = shop). */
    public List<User> listStaff(Long shopId) {
        EntityManager em = em();
        try {
            Shop shopRef = em.getReference(Shop.class, shopId);
            return em.createQuery(
                    "SELECT u FROM User u WHERE u.staffShop = :shop ORDER BY u.firstname, u.lastname",
                    User.class
            ).setParameter("shop", shopRef).getResultList();
        } finally {
            em.close();
        }
    }

    /** Thêm nhân viên bằng email: set user.staffShop = shop (chỉ owner shop được phép). */
    public void addStaffByEmail(Long ownerId, Long shopId, String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email không được trống.");
        }
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Shop shop = em.find(Shop.class, shopId);
            if (shop == null) throw new IllegalArgumentException("Shop không tồn tại.");
            if (!Objects.equals(shop.getVendor().getId(), ownerId)) {
                throw new SecurityException("Bạn không có quyền trên shop này.");
            }

            User target = em.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email", User.class
            ).setParameter("email", email.trim()).getResultStream().findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại."));

            if (Objects.equals(target.getId(), ownerId))
                throw new IllegalArgumentException("Không thể thêm chính chủ shop.");
            if (!Boolean.TRUE.equals(target.getIsEmailActive()))
                throw new IllegalArgumentException("Tài khoản chưa kích hoạt email.");
            if (target.getRole() != User.Role.USER)
                throw new IllegalArgumentException("Chỉ tài khoản USER mới được thêm làm nhân viên.");
            if (target.getStaffShop() != null)
                throw new IllegalStateException("Người dùng đang thuộc shop khác.");

            target.setStaffShop(shop);
            em.merge(target);

            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Xóa nhân viên: set user.staffShop = null (chỉ owner). */
    public void removeStaff(Long ownerId, Long shopId, Long userId) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Shop shop = em.find(Shop.class, shopId);
            if (shop == null) throw new IllegalArgumentException("Shop không tồn tại.");
            if (!Objects.equals(shop.getVendor().getId(), ownerId))
                throw new SecurityException("Bạn không có quyền trên shop này.");
            if (Objects.equals(ownerId, userId))
                throw new IllegalArgumentException("Không thể xóa chủ shop.");

            User target = em.find(User.class, userId);
            if (target == null) throw new IllegalArgumentException("Người dùng không tồn tại.");
            if (target.getStaffShop() == null || !Objects.equals(target.getStaffShop().getShopId(), shopId))
                throw new IllegalStateException("Người dùng không thuộc shop này.");

            target.setStaffShop(null);
            em.merge(target);

            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
