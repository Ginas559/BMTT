// filepath: src/main/java/vn/iotstar/services/StatisticService.java
package vn.iotstar.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query; // Thêm import Query
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Order;
import vn.iotstar.entities.Shop;

public class StatisticService {

    // --- SHOP ---
    public Shop findShopByOwner(Long userId) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            List<Shop> list = em.createQuery("SELECT s FROM Shop s WHERE s.vendor.id = :uid", Shop.class)
                    .setParameter("uid", userId).setMaxResults(1).getResultList();
            return list.isEmpty() ? null : list.get(0);
        } finally { em.close(); }
    }
    public Shop findShopById(Long shopId) {
        EntityManager em = JPAConfig.getEntityManager();
        try { return em.find(Shop.class, shopId); } finally { em.close(); }
    }

    // --- LEGACY DASHBOARD (giữ) ---
    public Map<String,Object> getDashboardData(Long shopId){
        EntityManager em = JPAConfig.getEntityManager();
        try{
            Long orderCount = em.createQuery("SELECT COUNT(o) FROM Order o WHERE o.shop.shopId = :sid", Long.class)
                    .setParameter("sid", shopId).getSingleResult();
            BigDecimal revenue = em.createQuery(
                    "SELECT COALESCE(SUM(o.totalAmount),0) FROM Order o " +
                    "WHERE o.shop.shopId=:sid AND o.status=:st", BigDecimal.class)
                    .setParameter("sid", shopId)
                    .setParameter("st", Order.OrderStatus.DELIVERED)
                    .getSingleResult();
            List<Object[]> topProducts = em.createQuery(
                    "SELECT oi.product.productName, SUM(oi.quantity) " +
                    "FROM OrderItem oi WHERE oi.order.shop.shopId=:sid AND oi.order.status=:st " +
                    "GROUP BY oi.product.productName ORDER BY SUM(oi.quantity) DESC", Object[].class)
                    .setParameter("sid", shopId)
                    .setParameter("st", Order.OrderStatus.DELIVERED)
                    .setMaxResults(5).getResultList();
            Map<String,Object> m=new HashMap<>();
            m.put("orderCount",orderCount); m.put("revenue",revenue); m.put("topProducts",topProducts);
            return m;
        } finally { em.close(); }
    }

    // --- REVENUE BY MONTH (legacy, không filter) ---
    public List<Object[]> getRevenueByMonth(Long shopId){
        EntityManager em = JPAConfig.getEntityManager();
        try{
            return em.createQuery(
                "SELECT FUNCTION('MONTH', o.createdAt), COALESCE(SUM(o.totalAmount),0) " +
                "FROM Order o WHERE o.shop.shopId=:sid AND o.status=:st " +
                "GROUP BY FUNCTION('MONTH', o.createdAt) ORDER BY FUNCTION('MONTH', o.createdAt)", Object[].class)
                .setParameter("sid", shopId)
                .setParameter("st", Order.OrderStatus.DELIVERED)
                .getResultList();
        } finally { em.close(); }
    }

    // --- REVENUE BY (YEAR, MONTH) BETWEEN ---
    public List<Object[]> revenueByYearMonthBetween(Long shopId, LocalDateTime start, LocalDateTime end){
        EntityManager em = JPAConfig.getEntityManager();
        try{
            return em.createQuery(
                "SELECT FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt), COALESCE(SUM(o.totalAmount),0) " +
                "FROM Order o WHERE o.shop.shopId=:sid AND o.status=:st " +
                "AND o.createdAt>=:start AND o.createdAt<:end " +
                "GROUP BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt) " +
                "ORDER BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt)", Object[].class)
                .setParameter("sid", shopId)
                .setParameter("st", Order.OrderStatus.DELIVERED)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        } finally { em.close(); }
    }

    // --- PRODUCT REVENUE BETWEEN (MỚI: Có lọc ngày) ---
    public List<Object[]> getProductRevenueBetween(Long shopId, LocalDateTime start, LocalDateTime end){
        EntityManager em = JPAConfig.getEntityManager();
        try{
            // KHÔNG CẦN CHUYỂN SANG STRING NỮA! Truyền thẳng Enum vào.
            return em.createQuery(
                "SELECT p.productName, COALESCE(SUM(oi.price * oi.quantity),0) " +  
                "FROM OrderItem oi JOIN oi.order o JOIN oi.product p " +
                "WHERE o.shop.shopId=:sid AND o.status=:st " + // :st là kiểu OrderStatus
                "AND o.createdAt>=:start AND o.createdAt<:end " +
                "GROUP BY p.productName ORDER BY 2 DESC", Object[].class)
                .setParameter("sid", shopId)
                .setParameter("st", vn.iotstar.entities.Order.OrderStatus.DELIVERED) // Truyền đối tượng Enum
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        } finally { em.close(); }
    }

    // --- PRODUCT REVENUE LIFETIME (MỚI: Không lọc ngày) ---
    public List<Object[]> getProductRevenueLifetime(Long shopId){
        EntityManager em = JPAConfig.getEntityManager();
        try{
            // KHÔNG CẦN CHUYỂN SANG STRING NỮA! Truyền thẳng Enum vào.
            return em.createQuery(
                "SELECT p.productName, COALESCE(SUM(oi.price * oi.quantity),0) " +
                "FROM OrderItem oi JOIN oi.order o JOIN oi.product p " +
                "WHERE o.shop.shopId=:sid AND o.status=:st " + // :st là kiểu OrderStatus
                "GROUP BY p.productName ORDER BY 2 DESC", Object[].class)
                .setParameter("sid", shopId)
                .setParameter("st", vn.iotstar.entities.Order.OrderStatus.DELIVERED) // Truyền đối tượng Enum
                .getResultList();
        } finally { em.close(); }
    }

    // --- Convenience filter year/from-to (giữ) ---
    public List<Object[]> getRevenueByMonth(Long shopId, Integer year, LocalDate from, LocalDate to){
        if (from!=null && to!=null){
            LocalDateTime start=from.atStartOfDay(), end=to.plusDays(1).atStartOfDay();
            return revenueByYearMonthBetween(shopId,start,end);
        }
        if (year!=null){
            LocalDateTime start=LocalDate.of(year,1,1).atStartOfDay();
            LocalDateTime end  =LocalDate.of(year+1,1,1).atStartOfDay();
            return revenueByYearMonthBetween(shopId,start,end);
        }
        return getRevenueByMonth(shopId);
    }

    // --- Others (giữ nguyên chữ ký) ---
    public Map<Order.OrderStatus, Long> countOrdersByStatus(Long shopId, LocalDate from, LocalDate to){
        EntityManager em = JPAConfig.getEntityManager();
        try{
            String jpql = "SELECT o.status, COUNT(o) FROM Order o WHERE o.shop.shopId=:sid " +
                          (from!=null && to!=null ? "AND o.createdAt>=:start AND o.createdAt<:end " : "") +
                          "GROUP BY o.status";
            var q = em.createQuery(jpql,Object[].class).setParameter("sid", shopId);
            if(from!=null && to!=null){ q.setParameter("start", from.atStartOfDay()); q.setParameter("end", to.plusDays(1).atStartOfDay()); }
            List<Object[]> rows=q.getResultList();
            Map<Order.OrderStatus,Long> m=new EnumMap<>(Order.OrderStatus.class);
            for(Object[] r:rows) m.put((Order.OrderStatus)r[0], (Long)r[1]);
            return m;
        } finally { em.close(); }
    }

    public List<vn.iotstar.entities.Order> findRecentOrders(Long shopId, int limit){
        EntityManager em = JPAConfig.getEntityManager();
        try{
            return em.createQuery(
                "SELECT o FROM Order o JOIN FETCH o.user u WHERE o.shop.shopId=:sid ORDER BY o.createdAt DESC",
                vn.iotstar.entities.Order.class)
                .setParameter("sid", shopId).setMaxResults(Math.max(1,limit)).getResultList();
        } finally { em.close(); }
    }

    public BigDecimal calcMonthOverMonthGrowth(Long shopId, YearMonth current){
        EntityManager em = JPAConfig.getEntityManager();
        try{
            YearMonth prev=current.minusMonths(1);
            BigDecimal cur=sumRevenueInMonth(em,shopId,current);
            BigDecimal pre=sumRevenueInMonth(em,shopId,prev);
            if(pre==null || pre.compareTo(BigDecimal.ZERO)==0)
                return (cur!=null && cur.compareTo(BigDecimal.ZERO)>0)?BigDecimal.valueOf(100):BigDecimal.ZERO;
            return cur.subtract(pre).multiply(BigDecimal.valueOf(100)).divide(pre,2,java.math.RoundingMode.HALF_UP);
        } finally { em.close(); }
    }
    private BigDecimal sumRevenueInMonth(EntityManager em, Long shopId, YearMonth ym){
        LocalDateTime start=ym.atDay(1).atStartOfDay();
        LocalDateTime end  =ym.plusMonths(1).atDay(1).atStartOfDay();
        return em.createQuery(
            "SELECT COALESCE(SUM(o.totalAmount),0) FROM Order o " +
            "WHERE o.shop.shopId=:sid AND o.status=:st AND o.createdAt>=:start AND o.createdAt<:end",
            BigDecimal.class)
            .setParameter("sid", shopId)
            .setParameter("st", Order.OrderStatus.DELIVERED)
            .setParameter("start", start)
            .setParameter("end", end)
            .getSingleResult();
    }

    public Map<String,Object> buildMonthSeries(Long shopId, LocalDateTime start, LocalDateTime end,
                                               LocalDateTime prevStart, LocalDateTime prevEnd){
        List<YearMonth> months=enumerateMonths(start,end);
        List<String> labels=new ArrayList<>(months.size());
        for(YearMonth ym:months) labels.add("Tháng "+ym.getMonthValue());

        List<Object[]> curRows=revenueByYearMonthBetween(shopId,start,end);
        List<Object[]> preRows=revenueByYearMonthBetween(shopId,prevStart,prevEnd);
        Map<YearMonth,BigDecimal> curMap=toYearMonthMap(curRows);
        Map<YearMonth,BigDecimal> preMap=toYearMonthMap(preRows);

        List<BigDecimal> current=new ArrayList<>(months.size());
        List<BigDecimal> previous=new ArrayList<>(months.size());

        YearMonth startYM=YearMonth.of(start.getYear(),start.getMonth());
        YearMonth prevStartYM=YearMonth.of(prevStart.getYear(),prevStart.getMonth());
        for(int i=0;i<months.size();i++){
            YearMonth ym=months.get(i);
            BigDecimal cur=curMap.getOrDefault(ym,BigDecimal.ZERO);
            long diff=startYM.until(ym, java.time.temporal.ChronoUnit.MONTHS);
            YearMonth prevYM=prevStartYM.plusMonths(diff);
            BigDecimal prv=preMap.getOrDefault(prevYM,BigDecimal.ZERO);
            current.add(cur); previous.add(prv);
        }
        Map<String,Object> result=new HashMap<>();
        result.put("labels",labels);
        result.put("current",current);
        result.put("previous",previous);
        result.put("values",current);
        return result;
    }

    private static List<YearMonth> enumerateMonths(LocalDateTime start, LocalDateTime end){
        List<YearMonth> list=new ArrayList<>();
        YearMonth cur=YearMonth.of(start.getYear(),start.getMonth());
        YearMonth last=YearMonth.of(end.minusSeconds(1).getYear(), end.minusSeconds(1).getMonth());
        while(!cur.isAfter(last)){ list.add(cur); cur=cur.plusMonths(1); }
        return list;
    }
    private static Map<YearMonth,BigDecimal> toYearMonthMap(List<Object[]> rows){
        Map<YearMonth,BigDecimal> m=new HashMap<>();
        for(Object[] r:rows){
            int y=((Number)r[0]).intValue();
            int mo=((Number)r[1]).intValue();
            BigDecimal sum=(r[2] instanceof BigDecimal)?(BigDecimal)r[2]:new BigDecimal(r[2].toString());
            m.put(YearMonth.of(y,mo),sum);
        }
        return m;
    }
}