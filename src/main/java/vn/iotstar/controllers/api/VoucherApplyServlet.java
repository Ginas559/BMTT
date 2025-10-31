package vn.iotstar.controllers.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;

import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Voucher;

@WebServlet("/api/voucher/apply")
public class VoucherApplyServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();

        String code = req.getParameter("code");
        if (code == null || code.isBlank()) {
            result.put("success", false);
            result.put("message", "Vui lòng nhập mã giảm giá.");
            mapper.writeValue(resp.getWriter(), result);
            return;
        }

        EntityManager em = JPAConfig.getEntityManager();
        try {
            // ⚙️ Tìm voucher theo code (không phân biệt hoa/thường)
            TypedQuery<Voucher> q = em.createQuery(
                "SELECT v FROM Voucher v WHERE v.codeUpper = :c",
                Voucher.class
            );
            q.setParameter("c", code.trim().toUpperCase());
            List<Voucher> list = q.getResultList();

            if (list.isEmpty()) {
                result.put("success", false);
                result.put("message", "Mã giảm giá không tồn tại.");
                mapper.writeValue(resp.getWriter(), result);
                return;
            }

            Voucher v = list.get(0);

            // ⚠️ Kiểm tra trạng thái & thời gian hiệu lực
            if (v.getStatus() == Voucher.Status.INACTIVE) {
                result.put("success", false);
                result.put("message", "Mã giảm giá này đã bị vô hiệu hóa.");
            } else if (v.getEndAt().isBefore(LocalDateTime.now())) {
                result.put("success", false);
                result.put("message", "Mã giảm giá đã hết hạn.");
            } else if (v.getStartAt().isAfter(LocalDateTime.now())) {
                result.put("success", false);
                result.put("message", "Mã giảm giá chưa đến thời gian sử dụng.");
            } else {
                // ✅ Tính toán giảm giá mẫu (chưa áp dụng cho từng sản phẩm)
                BigDecimal discountValue = BigDecimal.ZERO;

                if (v.getType() == Voucher.VoucherType.PERCENT) {
                    discountValue = v.getPercent(); // lưu phần trăm
                } else if (v.getType() == Voucher.VoucherType.AMOUNT) {
                    discountValue = v.getAmount(); // lưu tiền giảm
                }

                // ✅ Lưu session để checkout.jsp đọc
                session.setAttribute("voucherCode", v.getCode());
                session.setAttribute("voucherDiscount", discountValue);

                result.put("success", true);
                result.put("message", "Áp dụng mã giảm giá thành công!");
                result.put("discount", discountValue);
                result.put("discountFormatted", discountValue.toString());
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi hệ thống: " + e.getMessage());
        } finally {
            em.close();
        }

        PrintWriter out = resp.getWriter();
        new ObjectMapper().writeValue(out, result);
    }
}
