<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<h1 class="mb-4">📋 Chi Tiết Đơn Hàng #<c:out value="${orderDetail.orderId}"/></h1>

<div class="row">
    <div class="col-lg-8">
        <div class="card shadow-sm mb-4">
            <div class="card-header bg-light fw-bold">
                Thông Tin Chung
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        **Trạng Thái:** <span class="badge 
                            <c:choose>
                                <c:when test="${orderDetail.status == 'SHIPPING'}">bg-warning text-dark</c:when>
                                <c:when test="${orderDetail.status == 'DELIVERED'}">bg-success</c:when>
                                <c:when test="${orderDetail.status == 'CONFIRMED'}">bg-info</c:when>
                                <c:otherwise>bg-secondary</c:otherwise>
                            </c:choose>
                        ">
                            <c:out value="${orderDetail.status}"/>
                        </span>
                    </div>
                    <div class="col-md-6 mb-3">
                        **Phương Thức Thanh Toán:** <span class="fw-bold"><c:out value="${orderDetail.paymentMethod}"/></span>
                    </div>
                    
                    <div class="col-md-6 mb-3">
                        **Shop:** <c:out value="${orderDetail.shop.shopName}"/>
                    </div>
                    
                </div>
            </div>
        </div>

        <div class="card shadow-sm mb-4">
            <div class="card-header bg-light fw-bold">
                Địa Chỉ Giao Hàng
            </div>
            <div class="card-body">
                <p class="mb-1">**Tên Khách:** <c:out value="${orderDetail.address.receiverName}"/></p>
                <p class="mb-1">**Điện Thoại:** <c:out value="${orderDetail.address.phone}"/></p>
                <p class="mb-1">**Địa chỉ:** <c:out value="${orderDetail.address.addressDetail}"/></p>
                <a href="https://maps.google.com/?q=<c:out value="${orderDetail.address.addressDetail}"/>, target="_blank" class="btn btn-sm btn-outline-secondary mt-2">
                    Mở trên Google Maps
                </a>
            </div>
        </div>
        
        <div class="card shadow-sm mb-4">
            <div class="card-header bg-light fw-bold">
                Sản Phẩm Đặt Mua
            </div>
            <ul class="list-group list-group-flush">
                <c:forEach var="item" items="${items}">
                    <li class="list-group-item d-flex justify-content-between">
                        <div>
                            <c:out value="${item.product.productName}"/> (x<c:out value="${item.quantity}"/>)
                        </div>
                        <span class="fw-bold">
                            <fmt:formatNumber value="${item.price.multiply(item.quantity)}" type="currency" currencyCode="VND" maxFractionDigits="0"/>
                        </span>
                    </li>
                </c:forEach>
            </ul>
        </div>
    </div>

    <div class="col-lg-4">
        <div class="card text-white bg-info shadow-sm mb-4">
            <div class="card-header fw-bold">
                💰 Thanh Toán & Thu Hộ (COD)
            </div>
            <div class="card-body">
                <h2 class="card-title display-5 fw-bold text-center">
                    <fmt:formatNumber value="${orderDetail.totalAmount}" type="currency" currencyCode="VND" maxFractionDigits="0"/>
                </h2>
                <p class="text-center small">Đây là số tiền bạn cần thu hộ từ khách hàng.</p>
            </div>
        </div>

        <div class="card shadow-sm">
            <div class="card-header bg-light fw-bold">
                Hành Động
            </div>
            <div class="card-body d-grid gap-2">
                
                <%-- Logic hành động: Chỉ hiển thị các nút phù hợp với trạng thái --%>
                
                <c:choose>
                    <%-- 1. Nếu đơn hàng CÓ SẴN (CONFIRMED) --%>
                    <c:when test="${orderDetail.status == 'CONFIRMED'}">
                        <form method="post" action="${pageContext.request.contextPath}/shipper/available-orders/accept" class="d-grid">
                            <input type="hidden" name="orderId" value="${orderDetail.orderId}">
                            <button type="submit" class="btn btn-success btn-lg">
                                <i class="bi bi-check-circle"></i> **NHẬN ĐƠN HÀNG**
                            </button>
                        </form>
                    </c:when>

                    <%-- 2. Nếu đơn hàng ĐANG GIAO (SHIPPING) --%>
                    <c:when test="${orderDetail.status == 'SHIPPING'}">
                        <form method="post" action="${pageContext.request.contextPath}/shipper/my-orders/complete" class="d-grid">
                            <input type="hidden" name="orderId" value="${orderDetail.orderId}">
                            <button type="submit" class="btn btn-success btn-lg">
                                <i class="bi bi-truck"></i> **GIAO HÀNG THÀNH CÔNG**
                            </button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/shipper/my-orders/return" class="d-grid">
                            <input type="hidden" name="orderId" value="${orderDetail.orderId}">
                            <button type="submit" class="btn btn-danger btn-sm mt-2" 
                                    onclick="return confirm('Bạn có chắc chắn muốn TRẢ LẠI đơn hàng này không?')">
                                Trả Lại Đơn Hàng / Báo cáo sự cố
                            </button>
                        </form>
                    </c:when>
                    
                    <%-- 3. Nếu đơn hàng đã GIAO XONG (DELIVERED) hoặc HỦY --%>
                    <c:otherwise>
                        <button type="button" class="btn btn-secondary" disabled>
                            Không có hành động
                        </button>
                    </c:otherwise>
                </c:choose>
                
                <a href="${pageContext.request.contextPath}/shipper/dashboard" class="btn btn-outline-secondary mt-3">
                    Quay lại Dashboard
                </a>
            </div>
        </div>
    </div>
</div>