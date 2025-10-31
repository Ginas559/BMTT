<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<h1 class="mb-4 text-info">🚛 Đơn Hàng Có Sẵn</h1>
<p class="lead">Chọn đơn hàng phù hợp với lộ trình của bạn.</p>

<div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
    <%-- Duyệt qua danh sách đơn hàng có sẵn (Giả định: List<Order> có tên là 'availableOrders') --%>
    <c:forEach var="order" items="${availableOrders}">
        <div class="col">
            <div class="card shadow-sm h-100 border-info">
                <div class="card-body">
                    <h5 class="card-title text-primary">Đơn hàng #<c:out value="${order.orderId}"/></h5>

                    <p class="card-text mb-1">
                        **Địa điểm giao:** <c:out value="${order.address.addressDetail}"/>
                    </p>
                    <p class="card-text mb-1">
                        **Tổng tiền (COD):** <span class="fw-bold">
                            <fmt:formatNumber value="${order.totalAmount}" type="currency" currencyCode="VND" maxFractionDigits="0"/>
                        </span>
                    </p>
                 
                </div>
                <div class="card-footer d-flex gap-2">
                    <a href="${pageContext.request.contextPath}/shipper/available-orders/detail/${order.orderId}" class="btn btn-sm btn-outline-info">
                        <i class="bi bi-eye"></i> Xem Chi Tiết
                    </a>
                    <form method="post" action="${pageContext.request.contextPath}/shipper/available-orders/accept" class="d-inline m-0">
                        <input type="hidden" name="orderId" value="${order.orderId}">
                        <button type="submit" class="btn btn-sm btn-success">
                            <i class="bi bi-check-circle"></i> Nhận Đơn Hàng
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </c:forEach>
    <c:if test="${empty availableOrders}">
        <div class="col-12">
            <div class="alert alert-light text-center" role="alert">
                🎉 Tuyệt vời! Hiện tại không có đơn hàng có sẵn nào.
            </div>
        </div>
    </c:if>
</div>