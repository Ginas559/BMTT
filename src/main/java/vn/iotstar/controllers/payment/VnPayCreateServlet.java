package vn.iotstar.controllers.payment;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

import vn.iotstar.services.VnPayService;

@WebServlet(urlPatterns = {"/api/payment/create"})
public class VnPayCreateServlet extends HttpServlet {

    private final VnPayService vnPayService = new VnPayService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String amountStr   = req.getParameter("amount");
        String bankCode    = req.getParameter("bankCode");
        String addressIdStr= req.getParameter("addressId"); // ✅ bắt buộc theo flow
        String orderInfo   = req.getParameter("orderInfo");

        if (orderInfo == null || orderInfo.isBlank()) {
            orderInfo = "Thanh toan don hang";
        }

        long amount;
        try {
            amount = Long.parseLong(amountStr);
            if (amount <= 0) throw new NumberFormatException("amount <= 0");
        } catch (Exception e) {
            resp.setStatus(400);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"success\":false,\"message\":\"Số tiền không hợp lệ\"}");
            return;
        }

        long addressId;
        try {
            addressId = Long.parseLong(addressIdStr);
            if (addressId <= 0) throw new NumberFormatException("addressId <= 0");
        } catch (Exception e) {
            resp.setStatus(400);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"success\":false,\"message\":\"Địa chỉ giao hàng không hợp lệ\"}");
            return;
        }

        // ✅ Lưu addressId vào session để callback dùng lại
        HttpSession session = req.getSession();
        session.setAttribute("VNPAY_ADDRESS_ID", addressId);

        String baseUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
        String payUrl = vnPayService.createPaymentUrl(amount, orderInfo, baseUrl, bankCode, req);

        resp.setContentType("application/json;charset=UTF-8");
        String json = "{\"success\":true,\"paymentUrl\":\"" + payUrl.replace("\"","\\\"") + "\"}";
        resp.getWriter().write(json);
    }
}
