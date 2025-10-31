<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<h1 class="mb-4 text-warning">🚚 Đơn Hàng Của Tôi</h1>
<p class="lead">Các đơn hàng bạn đang thực hiện giao.</p>

<div class="list-group">
    <%-- Duyệt qua danh sách đơn hàng đã nhận (Giả định: List<Order> có tên là 'myOrders') --%>
    <c:forEach var="order" items="${myOrders}">
        <div class="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
            <div>
                <h5 class="mb-1">Đơn hàng #<c:out value="${order.orderId}"/></h5>
                <p class="mb-1">Giao đến: **<c:out value="${order.address.addressDetail}"/></p>
            
            </div>
            <div class="d-flex gap-2">
                <a href="${pageContext.request.contextPath}/shipper/my-orders/detail/${order.orderId}" class="btn btn-sm btn-outline-warning">
                    Xem Chi Tiết
                </a>
                <form method="post" action="${pageContext.request.contextPath}/shipper/my-orders/return" class="d-inline m-0">
                    <input type="hidden" name="orderId" value="${order.orderId}">
                    <button type="submit" class="btn btn-sm btn-danger" 
                            onclick="return confirm('Bạn có chắc chắn muốn TRẢ LẠI đơn hàng này không? (Cần cung cấp lý do sau khi xác nhận)')">
                        Trả Lại Đơn Hàng
                    </button>
                </form>
                <form method="post" action="${pageContext.request.contextPath}/shipper/my-orders/complete" class="d-inline m-0">
                    <input type="hidden" name="orderId" value="${order.orderId}">
                    <button type="submit" class="btn btn-sm btn-success">
                        Đã Giao Xong
                    </button>
                </form>
            </div>
        </div>
    </c:forEach>
    <c:if test="${empty myOrders}">
        <div class="list-group-item">
            <div class="alert alert-light text-center" role="alert">
                😴 Bạn chưa có đơn hàng nào đang giao. Hãy nhận đơn mới!
            </div>
        </div>
    </c:if>
</div>