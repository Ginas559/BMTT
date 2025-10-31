package vn.iotstar.controllers.order;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

// ✅ NEW: thêm các import phục vụ map & sắp xếp nhóm
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;

import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Order;
import vn.iotstar.entities.OrderItem;
import vn.iotstar.entities.Payment;

public class OrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Long resolveUserId(HttpSession session) {
        Object val = session.getAttribute("userId");
        if (val instanceof Long) return (Long) val;
        if (val instanceof Integer) return ((Integer) val).longValue();
        Long demo = 1L;
        session.setAttribute("userId", demo);
        return demo;
    }

    private int parsePositiveInt(String s, int def) {
        try {
            int v = Integer.parseInt(s);
            return v > 0 ? v : def;
        } catch (Exception e) {
            return def;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Long userId = resolveUserId(session);

        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();

        if ("/orders".equals(servletPath)) {
            list(req, resp, userId);
            return;
        }

        if ("/order".equals(servletPath) && pathInfo != null && pathInfo.length() > 1) {
            String idStr = pathInfo.substring(1);
            detail(req, resp, userId, idStr);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/orders");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Long userId = resolveUserId(session);

        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();
        String action = req.getParameter("action");

        if ("/order".equals(servletPath) && pathInfo != null && pathInfo.length() > 1) {
            String idStr = pathInfo.substring(1);
            if ("cancel".equalsIgnoreCase(action)) {
                cancel(req, resp, userId, idStr);
                return;
            }
        }

        resp.sendRedirect(req.getContextPath() + "/orders");
    }

    // ------------------ LIST ------------------
    private void list(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws ServletException, IOException {

        String statusParam = req.getParameter("status");
        int page = parsePositiveInt(req.getParameter("page"), 1);
        int size = parsePositiveInt(req.getParameter("size"), 10);
        int offset = (page - 1) * size;

        EntityManager em = JPAConfig.getEntityManager();

        try {
            List<Order> orders;
            long totalItems;

            boolean isAll = (statusParam == null || statusParam.isBlank() || "ALL".equalsIgnoreCase(statusParam));

            if (isAll) {
                TypedQuery<Long> cq = em.createQuery(
                        "SELECT COUNT(o) FROM vn.iotstar.entities.Order o WHERE o.user.id = :uid", Long.class);
                cq.setParameter("uid", userId);
                totalItems = cq.getSingleResult();

                TypedQuery<Order> q = em.createQuery(
                        "SELECT o FROM vn.iotstar.entities.Order o WHERE o.user.id = :uid ORDER BY o.createdAt DESC",
                        Order.class);
                q.setParameter("uid", userId);
                q.setFirstResult(offset);
                q.setMaxResults(size);
                orders = q.getResultList();
            } else {
                Order.OrderStatus st;
                try {
                    st = Order.OrderStatus.valueOf(statusParam.toUpperCase(Locale.ROOT));
                } catch (Exception e) {
                    st = Order.OrderStatus.NEW;
                }

                TypedQuery<Long> cq = em.createQuery(
                        "SELECT COUNT(o) FROM vn.iotstar.entities.Order o WHERE o.user.id = :uid AND o.status = :st",
                        Long.class);
                cq.setParameter("uid", userId);
                cq.setParameter("st", st);
                totalItems = cq.getSingleResult();

                TypedQuery<Order> q = em.createQuery(
                        "SELECT o FROM vn.iotstar.entities.Order o " +
                                "WHERE o.user.id = :uid AND o.status = :st ORDER BY o.createdAt DESC",
                        Order.class);
                q.setParameter("uid", userId);
                q.setParameter("st", st);
                q.setFirstResult(offset);
                q.setMaxResults(size);
                orders = q.getResultList();
            }

            long totalPages = (totalItems + size - 1) / size;

            Map<Long, String> paymentsByOrderId = new HashMap<>();
            if (orders != null && !orders.isEmpty()) {
                List<Long> orderIds = new ArrayList<>(orders.size());
                for (Order o : orders) {
                    if (o != null && o.getOrderId() != null) orderIds.add(o.getOrderId());
                }

                if (!orderIds.isEmpty()) {
                    TypedQuery<Object[]> qp = em.createQuery(
                            "SELECT p.order.orderId, p.transactionCode " +
                                    "FROM vn.iotstar.entities.Payment p " +
                                    "WHERE p.order.orderId IN :ids AND p.paymentStatus = :st " +
                                    "ORDER BY p.createdAt DESC",
                            Object[].class);
                    qp.setParameter("ids", orderIds);
                    qp.setParameter("st", Payment.PaymentStatus.SUCCESS);

                    List<Object[]> rows = qp.getResultList();
                    for (Object[] row : rows) {
                        if (row == null || row.length < 2) continue;
                        Long oid = (row[0] instanceof Number) ? ((Number) row[0]).longValue() : null;
                        String txn = (row[1] != null) ? row[1].toString() : null;
                        if (oid != null && txn != null && !paymentsByOrderId.containsKey(oid)) {
                            paymentsByOrderId.put(oid, txn);
                        }
                    }
                }
            }

            Map<String, List<Order>> groups = new LinkedHashMap<>();
            for (Order o : orders) {
                String txn = paymentsByOrderId.get(o.getOrderId());
                if (txn == null) txn = "";
                groups.computeIfAbsent(txn, k -> new ArrayList<>()).add(o);
            }

            List<Map.Entry<String, List<Order>>> entries = new ArrayList<>(groups.entrySet());
            entries.sort((e1, e2) -> {
                java.time.LocalDateTime max1 = null, max2 = null;
                for (Order o : e1.getValue()) {
                    java.time.LocalDateTime t = o.getCreatedAt();
                    if (t != null && (max1 == null || t.isAfter(max1))) max1 = t;
                }
                for (Order o : e2.getValue()) {
                    java.time.LocalDateTime t = o.getCreatedAt();
                    if (t != null && (max2 == null || t.isAfter(max2))) max2 = t;
                }
                if (max1 == null && max2 == null) return 0;
                if (max1 == null) return 1;
                if (max2 == null) return -1;
                return max2.compareTo(max1);
            });

            List<Order> ordersSorted = new ArrayList<>();
            Set<Long> groupFirst = new HashSet<>();
            for (Map.Entry<String, List<Order>> e : entries) {
                List<Order> list = e.getValue();
                list.sort((a, b) -> {
                    java.time.LocalDateTime ta = a.getCreatedAt();
                    java.time.LocalDateTime tb = b.getCreatedAt();
                    if (ta == null && tb == null) return 0;
                    if (ta == null) return 1;
                    if (tb == null) return -1;
                    return tb.compareTo(ta);
                });
                if (!list.isEmpty()) groupFirst.add(list.get(0).getOrderId());
                ordersSorted.addAll(list);
            }

            req.setAttribute("orders", ordersSorted);
            req.setAttribute("paymentsByOrderId", paymentsByOrderId);
            req.setAttribute("groupFirst", groupFirst);
            req.setAttribute("status", isAll ? "ALL" : statusParam.toUpperCase(Locale.ROOT));
            req.setAttribute("page", page);
            req.setAttribute("size", size);
            req.setAttribute("totalItems", totalItems);
            req.setAttribute("totalPages", totalPages);
        } catch (Exception e) {
            req.setAttribute("error", "Không tải được danh sách đơn hàng: " + e.getMessage());
        } finally {
            em.close();
        }

        req.getRequestDispatcher("/WEB-INF/views/order/order_list.jsp").forward(req, resp);
    }

    // ------------------ DETAIL ------------------
    private void detail(HttpServletRequest req, HttpServletResponse resp, Long userId, String idStr)
            throws ServletException, IOException {

        EntityManager em = JPAConfig.getEntityManager();

        try {
            Long oid = Long.valueOf(idStr);

            // ✅ JOIN FETCH thêm shop.vendor để lấy email và tên chủ shop
            TypedQuery<Order> qo = em.createQuery(
                "SELECT o FROM vn.iotstar.entities.Order o " +
                "LEFT JOIN FETCH o.address " +
                "LEFT JOIN FETCH o.user " +
                "LEFT JOIN FETCH o.shop s " +
                "LEFT JOIN FETCH s.vendor v " +
                "LEFT JOIN FETCH o.shipper " +
                "WHERE o.orderId = :oid", Order.class);
            qo.setParameter("oid", oid);

            Order order;
            try {
                order = qo.getSingleResult();
            } catch (NoResultException nre) {
                order = null;
            }

            if (order == null || order.getUser() == null || !order.getUser().getId().equals(userId)) {
                req.setAttribute("error", "Không tìm thấy đơn hàng hoặc bạn không có quyền xem.");
                req.getRequestDispatcher("/WEB-INF/views/order/order_detail.jsp").forward(req, resp);
                return;
            }

            TypedQuery<OrderItem> qi = em.createQuery(
                "SELECT oi FROM vn.iotstar.entities.OrderItem oi JOIN FETCH oi.product WHERE oi.order.orderId = :oid",
                OrderItem.class);
            qi.setParameter("oid", oid);
            List<OrderItem> items = qi.getResultList();

            req.setAttribute("order", order);
            req.setAttribute("items", items);
        } catch (Exception e) {
            req.setAttribute("error", "Lỗi khi tải chi tiết đơn hàng: " + e.getMessage());
        } finally {
            em.close();
        }

        req.getRequestDispatcher("/WEB-INF/views/order/order_detail.jsp").forward(req, resp);
    }

    // ------------------ CANCEL ------------------
    private void cancel(HttpServletRequest req, HttpServletResponse resp, Long userId, String idStr)
            throws IOException {

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = null;
        String redirectBase = req.getContextPath() + "/order/" + idStr;

        try {
            Long oid = Long.valueOf(idStr);
            Order order = em.find(Order.class, oid);
            if (order == null || order.getUser() == null || !order.getUser().getId().equals(userId)) {
                resp.sendRedirect(redirectBase + "?msg=not_allowed");
                return;
            }

            Order.OrderStatus current = order.getStatus();
            if (current != Order.OrderStatus.NEW) {
                resp.sendRedirect(redirectBase + "?msg=cannot_cancel_in_status_" + current.name());
                return;
            }

            tx = em.getTransaction();
            tx.begin();
            order.setStatus(Order.OrderStatus.CANCELED);
            tx.commit();

            resp.sendRedirect(redirectBase + "?msg=cancel_success");
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) tx.rollback();
            resp.sendRedirect(redirectBase + "?msg=cancel_failed");
        } finally {
            em.close();
        }
    }
}
