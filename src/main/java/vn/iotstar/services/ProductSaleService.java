// filepath: src/main/java/vn/iotstar/services/ProductSaleService.java
package vn.iotstar.services;

import vn.iotstar.configs.JPAConfig;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class ProductSaleService {

    // ViewModel đơn giản cho JSP/Servlet dùng
    public static class SaleVM {
        public final Long productId;
        public final BigDecimal percent;   // vd: 10.00 = giảm 10%
        public final java.time.LocalDate endDate;

        public SaleVM(Long pid, BigDecimal pct, LocalDate end) {
            this.productId = pid;
            this.percent = pct;
            this.endDate = end;
        }
    }

    /**
     * Lấy các khuyến mãi đang ACTIVE cho danh sách productIds (trong ngày hôm nay).
     * Yêu cầu bảng product_sale có cột: product_id (FK), percent_val (DECIMAL), start_at (DATETIME), end_at (DATETIME), status (NVARCHAR).
     */
    public Map<Long, SaleVM> activeByProductIds(List<Long> productIds) {
        Map<Long, SaleVM> out = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) return out;

        EntityManager em = JPAConfig.getEntityManager();
        try {
            // SỬA: Dùng cột percent_val, start_at, end_at
            String sql =
                "SELECT ps.product_id, ps.percent_val, ps.end_at " +
                "FROM product_sale ps " +
                "WHERE ps.product_id IN (:ids) " +
                "  AND ps.[status] = 'ACTIVE' " +
                "  AND :today BETWEEN ps.start_at AND ps.end_at"; // So sánh ngày hiện tại với cột DATETIME

            Query q = em.createNativeQuery(sql);
            q.setParameter("ids", productIds);
            // SỬA: Lấy ngày hôm nay
            q.setParameter("today", java.sql.Date.valueOf(LocalDate.now())); 

            @SuppressWarnings("unchecked")
            List<Object[]> rows = q.getResultList();
            for (Object[] r : rows) {
                Long pid = ((Number) r[0]).longValue();
                
                // Lấy giá trị phần trăm (cột index 1: percent_val)
                BigDecimal pct = (r[1] != null) ? (r[1] instanceof BigDecimal ? (BigDecimal) r[1]
                                                                              : new BigDecimal(r[1].toString()))
                                                 : BigDecimal.ZERO;
                
                // Lấy ngày kết thúc (cột index 2: end_at - thường là java.sql.Timestamp trong SQL Server)
                LocalDate end;
                if (r[2] instanceof java.sql.Timestamp) {
                    end = ((java.sql.Timestamp) r[2]).toLocalDateTime().toLocalDate();
                } else if (r[2] instanceof java.sql.Date) {
                    end = ((java.sql.Date) r[2]).toLocalDate();
                } else if (r[2] instanceof java.time.LocalDate) {
                    end = (LocalDate) r[2];
                } else {
                    end = null;
                }

                // Nếu trùng nhiều dòng cho 1 product (trùng thời gian), ưu tiên dòng có phần trăm lớn hơn
                SaleVM cur = out.get(pid);
                if (cur == null || (pct != null && cur.percent != null && pct.compareTo(cur.percent) > 0)) {
                    out.put(pid, new SaleVM(pid, pct, end));
                }
            }
            return out;
        } finally {
            em.close();
        }
    }

    /** Tính giá sau giảm: price * (100 - percent)/100, làm tròn 0 chữ số thập phân (đơn vị ₫). */
    public BigDecimal discounted(BigDecimal price, BigDecimal percent) {
        if (price == null || percent == null) return price;
        BigDecimal hundred = new BigDecimal("100");
        BigDecimal factor = hundred.subtract(percent).divide(hundred, 4, java.math.RoundingMode.HALF_UP);
        return price.multiply(factor).setScale(0, java.math.RoundingMode.HALF_UP);
    }
}