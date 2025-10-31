package vn.iotstar.controllers.payment;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

import java.util.*;
import java.util.stream.Collectors;

import vn.iotstar.configs.JPAConfig;
import vn.iotstar.services.VnPayService;

// Entities
import vn.iotstar.entities.Order;
import vn.iotstar.entities.Payment;
import vn.iotstar.entities.Address;
import vn.iotstar.entities.User;
import vn.iotstar.entities.Shop;
import vn.iotstar.entities.Cart;
import vn.iotstar.entities.CartItem;
import vn.iotstar.entities.OrderItem;
import vn.iotstar.entities.Product;

@WebServlet(urlPatterns = {"/api/payment/callback"})
public class VnPayCallbackServlet extends HttpServlet {

    private final VnPayService vnPayService = new VnPayService();

    private long resolveLong(Object v, long fallback) {
        if (v instanceof Long) return (Long) v;
        if (v instanceof Integer) return ((Integer) v).longValue();
        if (v instanceof String) {
            try { return Long.parseLong((String) v); } catch (Exception ignore) {}
        }
        return fallback;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // 1) Verify VNPAY return
            int status = vnPayService.handleReturn(req);

            String vnp_ResponseCode  = req.getParameter("vnp_ResponseCode");
            String vnp_TxnRef        = req.getParameter("vnp_TxnRef");
            String vnp_Amount        = req.getParameter("vnp_Amount");        // amount * 100
            String vnp_BankCode      = req.getParameter("vnp_BankCode");
            String vnp_TransactionNo = req.getParameter("vnp_TransactionNo");
            String vnp_Message       = req.getParameter("vnp_Message");

            boolean success = (status == 1);
            String message = success ? "Thanh toán thành công"
                    : (status == 0 ? "Thanh toán bị hủy hoặc thất bại" : "Lỗi xác thực giao dịch");

            // Trả về 1 orderId để UI bấm vào (nếu có nhiều đơn thì trả order đầu tiên)
            Long firstOrderIdCreated = null;

            if (success && "00".equals(vnp_ResponseCode)) {
                HttpSession session = req.getSession();

                long addressId = resolveLong(session.getAttribute("VNPAY_ADDRESS_ID"), -1L);
                long userId    = resolveLong(session.getAttribute("userId"), 1L);

                long amountVndLong = 0L;
                try { amountVndLong = Long.parseLong(vnp_Amount) / 100L; } catch (Exception ignore) {}
                BigDecimal paidAmount = BigDecimal.valueOf(amountVndLong);

                if (addressId <= 0 || amountVndLong <= 0) {
                    success = false;
                    message = "Dữ liệu thanh toán không hợp lệ (address/amount)";
                } else {
                    EntityManager em = JPAConfig.getEntityManager();
                    EntityTransaction tx = em.getTransaction();
                    try {
                        tx.begin();

                        // Lấy giỏ + items
                        Cart cart = null;
                        try {
                            cart = em.createQuery(
                                "SELECT c FROM vn.iotstar.entities.Cart c WHERE c.user.id = :uid",
                                Cart.class
                            ).setParameter("uid", userId).getSingleResult();
                        } catch (NoResultException ignore) { cart = null; }

                        List<CartItem> cartItems = Collections.emptyList();
                        if (cart != null) {
                            cartItems = em.createQuery(
                                "SELECT ci FROM vn.iotstar.entities.CartItem ci " +
                                "JOIN FETCH ci.product WHERE ci.cart.cartId = :cid",
                                CartItem.class
                            ).setParameter("cid", cart.getCartId()).getResultList();
                        }

                        if (cartItems == null || cartItems.isEmpty()) {
                            success = false;
                            message = "Giỏ hàng trống hoặc không tìm thấy khi tạo đơn.";
                        } else {
                            Address addressRef = em.getReference(Address.class, addressId);
                            User userRef = em.getReference(User.class, userId);

                            // --- GROUP BY SHOP ---
                            // Yêu cầu: Product phải có getShop()
                            Map<Shop, List<CartItem>> byShop = new LinkedHashMap<>();
                            for (CartItem ci : cartItems) {
                                Product p = ci.getProduct();
                                Shop s = (p != null) ? p.getShop() : null;
                                if (s == null) {
                                    // Nếu sản phẩm không có shop, bỏ qua/dừng tuỳ policy
                                    continue;
                                }
                                byShop.computeIfAbsent(s, k -> new ArrayList<>()).add(ci);
                            }

                            if (byShop.isEmpty()) {
                                success = false;
                                message = "Không xác định được cửa hàng cho các sản phẩm.";
                            } else {
                                // Tạo 1 order cho mỗi shop
                                for (Map.Entry<Shop, List<CartItem>> entry : byShop.entrySet()) {
                                    Shop shop = entry.getKey();
                                    List<CartItem> itemsOfShop = entry.getValue();

                                    // Tính tổng của đơn theo shop
                                    BigDecimal orderTotal = BigDecimal.ZERO;
                                    for (CartItem ci : itemsOfShop) {
                                        Product p = ci.getProduct();
                                        if (p == null) continue;
                                        BigDecimal price = (p.getPrice() != null) ? p.getPrice() : BigDecimal.ZERO;
                                        int qty = (ci.getQuantity() != null) ? ci.getQuantity() : 0;
                                        if (qty <= 0) continue;
                                        orderTotal = orderTotal.add(price.multiply(BigDecimal.valueOf(qty)));
                                    }

                                    // Nếu đơn theo shop không có item hợp lệ thì bỏ qua
                                    if (orderTotal.compareTo(BigDecimal.ZERO) <= 0) {
                                        continue;
                                    }

                                    // Create Order
                                    Order order = new Order();
                                    order.setPaymentMethod(Order.PaymentMethod.VNPAY);
                                    order.setStatus(Order.OrderStatus.NEW);
                                    order.setTotalAmount(orderTotal); // mỗi đơn đúng tổng của shop
                                    order.setAddress(addressRef);
                                    order.setUser(userRef);
                                    order.setShop(shop);
                                    em.persist(order);
                                    em.flush();

                                    if (firstOrderIdCreated == null) firstOrderIdCreated = order.getOrderId();

                                    // Create OrderItems
                                    for (CartItem ci : itemsOfShop) {
                                        Product p = ci.getProduct();
                                        if (p == null) continue;
                                        Integer qty = ci.getQuantity();
                                        if (qty == null || qty <= 0) continue;

                                        OrderItem oi = new OrderItem();
                                        oi.setOrder(order);
                                        oi.setProduct(p);
                                        oi.setQuantity(qty);
                                        oi.setPrice(p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO);
                                        oi.setDiscount(null); // nếu có field khuyến mãi thì set sau
                                        em.persist(oi);

                                        // (Tuỳ chọn) trừ tồn:
                                        // if (p.getStock() != null) p.setStock(p.getStock() - qty);
                                    }

                                    // Create Payment cho đơn này (ghi số tiền đúng theo orderTotal)
                                    Payment payment = new Payment();
                                    payment.setPaymentMethod(Payment.PaymentMethod.VNPAY);
                                    payment.setPaymentStatus(Payment.PaymentStatus.SUCCESS);
                                    payment.setAmount(orderTotal);
                                    payment.setTransactionCode(vnp_TransactionNo);
                                    payment.setOrder(order);
                                    em.persist(payment);
                                }

                                // (Tuỳ chọn) Nếu muốn xoá luôn Cart rỗng:
                                // em.remove(em.contains(cart) ? cart : em.merge(cart));
                            }
                        }

                        if (success) {
                            tx.commit();
                            session.removeAttribute("VNPAY_ADDRESS_ID");
                        } else {
                            if (tx.isActive()) tx.rollback();
                        }
                    } catch (Exception e) {
                        if (tx.isActive()) tx.rollback();
                        success = false;
                        message = "Không thể tạo đơn hàng sau khi thanh toán: " + e.getMessage();
                    } finally {
                        em.close();
                    }
                }
            }

            // 3) Truyền dữ liệu cho JSP kết quả
            req.setAttribute("success", success);
            req.setAttribute("message", message);
            req.setAttribute("orderId", firstOrderIdCreated); // trả về ID của đơn đầu tiên (nếu có)

            req.setAttribute("vnp_ResponseCode", vnp_ResponseCode);
            req.setAttribute("vnp_TxnRef", vnp_TxnRef);
            req.setAttribute("vnp_Amount", vnp_Amount);
            req.setAttribute("vnp_BankCode", vnp_BankCode);
            req.setAttribute("vnp_TransactionNo", vnp_TransactionNo);
            req.setAttribute("vnp_Message", vnp_Message);

            // 4) Forward nội bộ
            req.getRequestDispatcher("/WEB-INF/views/order/payment_result.jsp").forward(req, resp);

        } catch (Exception e) {
            String url = req.getContextPath() + "/error.jsp?message=" +
                    URLEncoder.encode("Lỗi hệ thống: " + e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(url);
        }
    }
}
