// filepath: src/main/java/vn/iotstar/services/VoucherService.java
package vn.iotstar.services;

import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Product;
import vn.iotstar.entities.Shop;
import vn.iotstar.entities.Voucher;
import vn.iotstar.entities.Voucher.VoucherType;
import vn.iotstar.entities.Voucher.Status;
import vn.iotstar.entities.VoucherProduct;
import vn.iotstar.entities.VoucherProductId;

public class VoucherService {

    public static class PageResult<T> {
        public final List<T> items;
        public final int page, size, totalPages;
        public PageResult(List<T> items, int page, int size, int totalPages) {
            this.items = items; this.page = page; this.size = size; this.totalPages = totalPages;
        }
    }

    // ======= FIND PAGED (6 tham số) =======
    public PageResult<Voucher> findByShopPaged(Long shopId, int page, int size,
                                               String q, String type, String status) {
        if (shopId == null) return new PageResult<>(List.of(), 1, size, 1);
        page = Math.max(1, page);
        size = Math.max(1, size);
        int first = (page - 1) * size;

        StringBuilder where = new StringBuilder(" WHERE v.shop.shopId = :sid ");
        if (q != null && !q.isBlank()) where.append(" AND (LOWER(v.code) LIKE :kw OR LOWER(v.codeUpper) LIKE :kw) ");
        if (type != null && !type.isBlank()) where.append(" AND v.type = :tp ");
        if (status != null && !status.isBlank()) where.append(" AND v.status = :st ");

        EntityManager em = JPAConfig.getEntityManager();
        try {
            TypedQuery<Long> cq = em.createQuery("SELECT COUNT(v) FROM Voucher v" + where, Long.class);
            cq.setParameter("sid", shopId);
            if (q != null && !q.isBlank()) cq.setParameter("kw", "%" + q.toLowerCase() + "%");
            if (type != null && !type.isBlank()) cq.setParameter("tp", VoucherType.valueOf(type));
            if (status != null && !status.isBlank()) cq.setParameter("st", Status.valueOf(status));
            long total = cq.getSingleResult();
            int totalPages = (int)Math.max(1, (total + size - 1) / size);

            TypedQuery<Voucher> pq = em.createQuery(
                    "SELECT v FROM Voucher v JOIN FETCH v.shop s" + where + " ORDER BY v.voucherId DESC",
                    Voucher.class);
            pq.setParameter("sid", shopId);
            if (q != null && !q.isBlank()) pq.setParameter("kw", "%" + q.toLowerCase() + "%");
            if (type != null && !type.isBlank()) pq.setParameter("tp", VoucherType.valueOf(type));
            if (status != null && !status.isBlank()) pq.setParameter("st", Status.valueOf(status));
            List<Voucher> items = pq.setFirstResult(first).setMaxResults(size).getResultList();

            // nạp nhẹ products (tránh N+1)
            items.forEach(v -> v.getVoucherProducts().size());

            return new PageResult<>(items, page, size, totalPages);
        } finally { em.close(); }
    }

    public Voucher findByIdForShop(Long id, Long shopId) {
        if (id == null || shopId == null) return null;
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT v FROM Voucher v JOIN FETCH v.shop s WHERE v.voucherId = :id AND s.shopId = :sid",
                    Voucher.class)
                .setParameter("id", id)
                .setParameter("sid", shopId)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally { em.close(); }
    }

    // ======= CREATE =======
    public Long createForShop(HttpServletRequest req, Long shopId) {
        if (shopId == null) throw new RuntimeException("Thiếu shopId");
        String code = req.getParameter("code");
        String type = req.getParameter("type");
        String percentStr = req.getParameter("percent");
        String amountStr = req.getParameter("amount");
        String minOrderStr = req.getParameter("minOrderAmount");
        String startAtStr = req.getParameter("startAt");
        String endAtStr   = req.getParameter("endAt");
        String[] productIds = req.getParameterValues("productIds");

        // validate
        List<String> errs = new ArrayList<>();
        if (code == null || code.trim().length() < 3) errs.add("Code tối thiểu 3 ký tự.");
        VoucherType vt = null;
        try { vt = VoucherType.valueOf(type); } catch (Exception e) { errs.add("Type không hợp lệ."); }

        BigDecimal percent = null, amount = null, minOrder = null;
        if (vt == VoucherType.PERCENT) {
            try { percent = new BigDecimal(nullIfBlank(percentStr)); } catch (Exception e){ errs.add("Percent không hợp lệ."); }
            if (percent != null && (percent.compareTo(BigDecimal.ONE) < 0 || percent.compareTo(new BigDecimal("100")) > 0))
                errs.add("Percent phải trong 1–100.");
        } else if (vt == VoucherType.AMOUNT) {
            try { amount = new BigDecimal(nullIfBlank(amountStr)); } catch (Exception e){ errs.add("Amount không hợp lệ."); }
            if (amount != null && amount.compareTo(new BigDecimal("0.01")) < 0) errs.add("Amount phải > 0.");
        }
        if (minOrderStr != null && !minOrderStr.isBlank()) {
            try { minOrder = new BigDecimal(minOrderStr.trim()); } catch (Exception e){ errs.add("Min order không hợp lệ."); }
            if (minOrder != null && minOrder.compareTo(BigDecimal.ZERO) < 0) errs.add("Min order phải ≥ 0.");
        }

        LocalDateTime startAt = parseLdtOrNull(startAtStr);
        LocalDateTime endAt   = parseLdtOrNull(endAtStr);
        if (startAt != null && endAt != null && !endAt.isAfter(startAt)) errs.add("End At phải sau Start At.");

        if (!errs.isEmpty()) throw new RuntimeException(String.join(" ", errs));

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // unique code per shop
            Long dup = em.createQuery(
                    "SELECT COUNT(v) FROM Voucher v WHERE v.shop.shopId = :sid AND v.codeUpper = :cu",
                    Long.class)
                .setParameter("sid", shopId)
                .setParameter("cu", code.trim().toUpperCase())
                .getSingleResult();
            if (dup != null && dup > 0) throw new RuntimeException("Code đã tồn tại trong shop.");

            Voucher v = Voucher.builder()
                    .shop(em.getReference(Shop.class, shopId))
                    .code(code.trim())
                    .codeUpper(code.trim().toUpperCase())
                    .type(vt)
                    .percent(percent)           // null nếu AMOUNT
                    .amount(amount)             // null nếu PERCENT
                    .minOrderAmount(minOrder == null ? BigDecimal.ZERO : minOrder)
                    .startAt(startAt)
                    .endAt(endAt)
                    .status(Status.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build();

            em.persist(v);
            em.flush(); // lấy voucherId

            if (vt == VoucherType.PERCENT && productIds != null && productIds.length > 0) {
                // lấy list product thuộc đúng shop
                List<Long> ids = new ArrayList<>();
                for (String s : productIds) {
                    try { ids.add(Long.valueOf(s)); } catch (Exception ignore) {}
                }
                if (!ids.isEmpty()) {
                    List<Product> products = em.createQuery(
                            "SELECT p FROM Product p WHERE p.shop.shopId = :sid AND p.productId IN :ids",
                            Product.class)
                        .setParameter("sid", shopId)
                        .setParameter("ids", ids)
                        .getResultList();

                    for (Product p : products) {
                        VoucherProductId vid = new VoucherProductId(v.getVoucherId(), p.getProductId());
                        VoucherProduct vp = new VoucherProduct();
                        vp.setId(vid);
                        vp.setVoucher(v);
                        vp.setProduct(p);
                        em.persist(vp);
                    }
                }
            }

            tx.commit();
            return v.getVoucherId();

        } catch (PersistenceException pe) {                 // <-- BẮT TRƯỚC
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Không lưu được voucher (DB).", pe);
        } catch (RuntimeException e) {                      // <-- BẮT SAU
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // ======= UPDATE =======
    public void updateForShop(HttpServletRequest req, Long shopId) {
        if (shopId == null) throw new RuntimeException("Thiếu shopId");
        Long id = parseLong(req.getParameter("id"));
        if (id == null) throw new RuntimeException("Thiếu id");

        String code = req.getParameter("code");
        String type = req.getParameter("type");
        String percentStr = req.getParameter("percent");
        String amountStr = req.getParameter("amount");
        String minOrderStr = req.getParameter("minOrderAmount");
        String startAtStr = req.getParameter("startAt");
        String endAtStr   = req.getParameter("endAt");
        String statusStr  = req.getParameter("status");
        String[] productIds = req.getParameterValues("productIds");

        List<String> errs = new ArrayList<>();
        if (code == null || code.trim().length() < 3) errs.add("Code tối thiểu 3 ký tự.");

        VoucherType vt = null;
        try { vt = VoucherType.valueOf(type); } catch (Exception e) { errs.add("Type không hợp lệ."); }

        BigDecimal percent = null, amount = null, minOrder = null;
        if (vt == VoucherType.PERCENT) {
            try { percent = new BigDecimal(nullIfBlank(percentStr)); } catch (Exception e){ errs.add("Percent không hợp lệ."); }
            if (percent != null && (percent.compareTo(BigDecimal.ONE) < 0 || percent.compareTo(new BigDecimal("100")) > 0))
                errs.add("Percent phải trong 1–100.");
        } else if (vt == VoucherType.AMOUNT) {
            try { amount = new BigDecimal(nullIfBlank(amountStr)); } catch (Exception e){ errs.add("Amount không hợp lệ."); }
            if (amount != null && amount.compareTo(new BigDecimal("0.01")) < 0) errs.add("Amount phải > 0.");
        }
        if (minOrderStr != null && !minOrderStr.isBlank()) {
            try { minOrder = new BigDecimal(minOrderStr.trim()); } catch (Exception e){ errs.add("Min order không hợp lệ."); }
            if (minOrder != null && minOrder.compareTo(BigDecimal.ZERO) < 0) errs.add("Min order phải ≥ 0.");
        }
        LocalDateTime startAt = parseLdtOrNull(startAtStr);
        LocalDateTime endAt   = parseLdtOrNull(endAtStr);
        if (startAt != null && endAt != null && !endAt.isAfter(startAt)) errs.add("End At phải sau Start At.");

        if (!errs.isEmpty()) throw new RuntimeException(String.join(" ", errs));

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Voucher v = em.find(Voucher.class, id, LockModeType.PESSIMISTIC_WRITE);
            if (v == null || v.getShop() == null || !shopId.equals(v.getShop().getShopId()))
                throw new SecurityException("Không có quyền với voucher này.");

            // unique code per shop (trừ chính nó)
            Long dup = em.createQuery(
                    "SELECT COUNT(vv) FROM Voucher vv WHERE vv.shop.shopId = :sid AND vv.codeUpper = :cu AND vv.voucherId <> :id",
                    Long.class)
                .setParameter("sid", shopId)
                .setParameter("cu", code.trim().toUpperCase())
                .setParameter("id", id)
                .getSingleResult();
            if (dup != null && dup > 0) throw new RuntimeException("Code đã tồn tại trong shop.");

            v.setCode(code.trim());
            v.setCodeUpper(code.trim().toUpperCase());
            v.setType(vt);
            v.setPercent(vt == VoucherType.PERCENT ? percent : null);
            v.setAmount(vt == VoucherType.AMOUNT  ? amount  : null);
            v.setMinOrderAmount(minOrder == null ? BigDecimal.ZERO : minOrder);
            v.setStartAt(startAt);
            v.setEndAt(endAt);

            if (statusStr != null && !statusStr.isBlank()) {
                v.setStatus(Status.valueOf(statusStr));
            }

            // cập nhật products (xóa cũ → thêm mới) nếu là PERCENT
            em.createQuery("DELETE FROM VoucherProduct vp WHERE vp.voucher.voucherId = :vid")
                .setParameter("vid", v.getVoucherId())
                .executeUpdate();

            if (vt == VoucherType.PERCENT && productIds != null && productIds.length > 0) {
                List<Long> ids = new ArrayList<>();
                for (String s : productIds) {
                    try { ids.add(Long.valueOf(s)); } catch (Exception ignore) {}
                }
                if (!ids.isEmpty()) {
                    List<Product> products = em.createQuery(
                            "SELECT p FROM Product p WHERE p.shop.shopId = :sid AND p.productId IN :ids",
                            Product.class)
                        .setParameter("sid", shopId)
                        .setParameter("ids", ids)
                        .getResultList();

                    for (Product p : products) {
                        VoucherProductId vid = new VoucherProductId(v.getVoucherId(), p.getProductId());
                        VoucherProduct vp = new VoucherProduct();
                        vp.setId(vid);
                        vp.setVoucher(v);
                        vp.setProduct(p);
                        em.persist(vp);
                    }
                }
            }

            em.merge(v);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally { em.close(); }
    }

    // ======= TOGGLE =======
    public Status toggleStatus(Long voucherId, Long shopId) {
        if (voucherId == null || shopId == null) throw new RuntimeException("Thiếu tham số");
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Voucher v = em.find(Voucher.class, voucherId, LockModeType.PESSIMISTIC_WRITE);
            if (v == null || v.getShop() == null || !shopId.equals(v.getShop().getShopId()))
                throw new SecurityException("Không có quyền thao tác voucher này");
            Status newSt = (v.getStatus() == Status.ACTIVE) ? Status.INACTIVE : Status.ACTIVE;
            v.setStatus(newSt);
            em.merge(v);
            tx.commit();
            return newSt;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally { em.close(); }
    }

    // ======= helpers =======
    private static String nullIfBlank(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }

    private static Long parseLong(String s) {
        try { return (s == null || s.isBlank()) ? null : Long.valueOf(s.trim()); }
        catch (Exception e) { return null; }
    }

    private static LocalDateTime parseLdtOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim();
        // chuẩn HTML datetime-local: 2025-10-01T08:54
        try { return LocalDateTime.parse(t); } catch (DateTimeParseException ignored) {}
        // fallback 1: 2025-10-01 08:54
        try { return LocalDateTime.parse(t.replace(' ', 'T')); } catch (DateTimeParseException ignored) {}
        // fallback 2: 01/10/2025 08:54 AM
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
            return LocalDateTime.parse(t, f);
        } catch (DateTimeParseException ignored) {}
        return null;
    }
}
