<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<h1 class="mb-4 text-success">✅ Lịch Sử Giao Hàng</h1>
<p class="lead">Các đơn hàng bạn đã hoàn thành giao.</p>

<table class="table table-hover table-striped">
    <thead class="table-success">
        <tr>
            <th>Mã Đơn</th>
            <th>Khách hàng</th>
            
            <th>Thu hộ (COD)</th>
            <th>Hành động</th>
        </tr>
    </thead>
    <tbody>
        <%-- Duyệt qua danh sách lịch sử (Giả định: List<Order> có tên là 'historyOrders') --%>
        <c:forEach var="order" items="${historyOrders}">
            <tr>
                <td>#<c:out value="${order.orderId}"/></td>
                <td><c:out value="${order.user.firstname} ${order.user.lastname}"/></td>
                
                <td>
                    <span class="fw-bold">
                        <fmt:formatNumber value="${order.totalAmount}" type="currency" currencyCode="VND" maxFractionDigits="0"/>
                    </span>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/shipper/history/detail/${order.orderId}" class="btn btn-sm btn-outline-primary">
                        Xem Chi Tiết
                    </a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<c:if test="${empty historyOrders}">
    <div class="alert alert-light text-center" role="alert">
        Chưa có đơn hàng nào trong lịch sử giao thành công.
    </div>
</c:if>