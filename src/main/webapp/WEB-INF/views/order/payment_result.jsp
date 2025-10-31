<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Kết quả thanh toán</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-5">
<%
    // Ưu tiên attribute do servlet forward; fallback query param (khi bị redirect ngoài)
    Object successAttr = request.getAttribute("success");
    Boolean ok = (successAttr instanceof Boolean) ? (Boolean) successAttr
                   : "true".equalsIgnoreCase(request.getParameter("success"));

    String message = (String) request.getAttribute("message");
    if (message == null) message = request.getParameter("message");

    String code   = (String) request.getAttribute("vnp_ResponseCode");
    if (code == null) code = request.getParameter("vnp_ResponseCode");

    String txnRef = (String) request.getAttribute("vnp_TxnRef");
    if (txnRef == null) txnRef = request.getParameter("vnp_TxnRef");

    String amount = (String) request.getAttribute("vnp_Amount");
    if (amount == null) amount = request.getParameter("vnp_Amount");

    String bank   = (String) request.getAttribute("vnp_BankCode");
    if (bank == null) bank = request.getParameter("vnp_BankCode");

    String transNo = (String) request.getAttribute("vnp_TransactionNo");
    if (transNo == null) transNo = request.getParameter("vnp_TransactionNo");

    // Nếu chưa xác định ok mà code là "00" thì coi là thành công (theo RETURN URL)
    if (ok == null) ok = "00".equals(code);

    // Parse amount (callback dùng đơn vị VND x 100)
    double amt = 0;
    try { if (amount != null && amount.trim().length() > 0) amt = Double.parseDouble(amount) / 100d; } catch (Exception ignore) {}

    // Nhận orderId do servlet forward (khi đã tạo đơn thành công)
    Long orderId = null;
    Object orderIdAttr = request.getAttribute("orderId");
    if (orderIdAttr instanceof Long) {
        orderId = (Long) orderIdAttr;
    } else if (orderIdAttr instanceof Integer) {
        orderId = ((Integer) orderIdAttr).longValue();
    } else {
        String oid = request.getParameter("orderId");
        try { if (oid != null) orderId = Long.parseLong(oid); } catch (Exception ignore) {}
    }
%>

    <div class="text-center mb-4">
        <h2 class="fw-bold <%= Boolean.TRUE.equals(ok) ? "text-success" : "text-danger" %>">
            <%= Boolean.TRUE.equals(ok) ? "✅ Thanh toán thành công" : "❌ Thanh toán thất bại hoặc bị hủy" %>
        </h2>
        <p class="text-muted"><%= message != null ? message : "" %></p>
    </div>

    <div class="card shadow-sm mx-auto" style="max-width: 720px;">
        <div class="card-body">
            <table class="table table-bordered mb-0">
                <tbody>
                <tr>
                    <th style="width: 260px;">Mã giao dịch (vnp_TxnRef)</th>
                    <td><%= txnRef != null ? txnRef : "-" %></td>
                </tr>
                <tr>
                    <th>Mã phản hồi (vnp_ResponseCode)</th>
                    <td><%= code != null ? code : "-" %></td>
                </tr>
                <tr>
                    <th>Số tiền</th>
                    <td><fmt:formatNumber value="<%= amt %>" type="currency" currencySymbol="₫" /></td>
                </tr>
                <tr>
                    <th>Ngân hàng</th>
                    <td><%= bank != null ? bank : "-" %></td>
                </tr>
                <tr>
                    <th>Mã giao dịch VNPAY</th>
                    <td><%= transNo != null ? transNo : "-" %></td>
                </tr>
                <% if (Boolean.TRUE.equals(ok) && orderId != null) { %>
                <tr>
                    <th>Mã đơn hàng</th>
                    <td>#<%= orderId %></td>
                </tr>
                <% } %>
                </tbody>
            </table>

            <div class="text-center mt-4">
                <% if (Boolean.TRUE.equals(ok) && orderId != null) { %>
                    <a href="${pageContext.request.contextPath}/order/<%= orderId %>" class="btn btn-success">Xem chi tiết đơn #<%= orderId %></a>
                <% } %>
                <a href="${pageContext.request.contextPath}/orders" class="btn btn-primary ms-2">Về danh sách đơn hàng</a>
            </div>
        </div>
    </div>
</div>
</body>
</html>
