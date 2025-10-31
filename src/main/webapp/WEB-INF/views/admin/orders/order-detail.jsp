<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<h1 class="mb-4">
	📋 Chi Tiết Đơn Hàng #
	<c:out value="${orderDetail.orderId}" />
</h1>

<div class="row">
	<div class="col-lg-8">
		<div class="card shadow-sm mb-4">
			<div class="card-header bg-light fw-bold">Thông Tin Chung</div>
			<div class="card-body">
				<div class="row">
					<div class="col-md-6 mb-3">
						**Trạng Thái:** <span
							class="badge ">
                            <c:choose>
                                <c:when test="${orderDetail.status=='NEW'}"><span class="badge text-bg-primary">Mới</span></c:when>
            <c:when test="${orderDetail.status=='CONFIRMED'}"><span class="badge text-bg-info">Đã xác nhận</span></c:when>
            <c:when test="${orderDetail.status=='SHIPPING'}"><span class="badge text-bg-warning">Đang giao</span></c:when>
            <c:when test="${orderDetail.status=='DELIVERED'}"><span class="badge text-bg-success">Đã giao</span></c:when>
            <c:when test="${orderDetail.status=='CANCELED'}"><span class="badge text-bg-secondary">Đã hủy</span></c:when>
            <c:when test="${orderDetail.status=='RETURNED'}"><span class="badge text-bg-dark">Hoàn hàng</span></c:when>
            <c:otherwise><span class="badge text-bg-light"><c:out value="${orderDetail.status}"/></span></c:otherwise>
                            </c:choose>
                        
							<c:out value="${orderDetail.status}" />
						</span>
					</div>
					<div class="col-md-6 mb-3">
						**Phương Thức Thanh Toán:** <span class="fw-bold"><c:out
								value="${orderDetail.paymentMethod}" /></span>
					</div>
					
					<div class="col-md-6 mb-3">
						**Shop:**
						<c:out value="${orderDetail.shop.shopName}" />
					</div>

				</div>
			</div>
		</div>

		<div class="card shadow-sm mb-4">
			<div class="card-header bg-light fw-bold">Địa Chỉ Giao Hàng</div>
			<div class="card-body">
				<p class="mb-1">
					**Tên Khách:**
					<c:out value="${orderDetail.address.receiverName}" />
				</p>
				<p class="mb-1">
					**Điện Thoại:**
					<c:out value="${orderDetail.address.phone}" />
				</p>
				<p class="mb-1">
					**Địa chỉ:**
					<c:out value="${orderDetail.address.addressDetail}" />
				</p>
				<a
					href="https://maps.google.com/?q=<c:out value="${orderDetail.address.addressDetail}"/>
					target="_blank" class="btn btn-sm btn-outline-secondary mt-2">
					Mở trên Google Maps </a>
			</div>
		</div>

		<div class="card shadow-sm mb-4">
			<div class="card-header bg-light fw-bold">Sản Phẩm Đặt Mua</div>
			<ul class="list-group list-group-flush">
				<c:forEach var="item" items="${items}">
					<li class="list-group-item d-flex justify-content-between">
						<div>
							<c:out value="${item.product.productName}" />
							(x
							<c:out value="${item.quantity}" />
							)
						</div> <span class="fw-bold"> <fmt:formatNumber
								value="${item.price.multiply(item.quantity)}" type="currency"
								currencyCode="VND" maxFractionDigits="0" />
					</span>
					</li>
				</c:forEach>
			</ul>
		</div>
	</div>

	<div class="col-lg-4">

		<div class="card shadow-sm">
			<div class="card-header bg-light fw-bold">Hành Động</div>
			<div class="card-body d-grid gap-2">

				<a href="${pageContext.request.contextPath}/admin/orders"
					class="btn btn-outline-secondary mt-3"> Quay lại </a>
			</div>
		</div>
	</div>
</div>
