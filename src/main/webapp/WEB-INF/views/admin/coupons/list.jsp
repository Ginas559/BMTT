<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- Sử dụng layout chung cho trang quản trị --%>
<sitemesh:decorate template="/WEB-INF/decorators/layout-admin.jsp" />

<div class="container-fluid">

	<div class="d-flex justify-content-between align-items-center mb-4">
		<h4 class="fw-bold text-primary mb-0">
			<i class="fas fa-tags me-2"></i> Coupon Management
		</h4>
		<a href="${pageContext.request.contextPath}/admin/coupons/add"
			class="btn btn-primary"> <i class="fas fa-plus me-1"></i> Add
			Coupon
		</a>
	</div>

	<form class="row g-3 align-items-center mb-4" method="get"
		action="${pageContext.request.contextPath}/admin/coupons">
		
		<div class="col-md-4">
			<input type="text" name="keyword" class="form-control"
				placeholder="🔍 Search by coupon code..." value="${param.keyword}">
		</div>
		
		<div class="col-md-4">
			<select name="shopId" class="form-select">
				<option value="">All Shops</option>
				<%-- Lặp qua danh sách các Shop (được Controller truyền vào là 'shops') --%>
				<c:forEach var="shop" items="${shops}">
					<option value="${shop.shopId}" ${param.shopId == shop.shopId ? 'selected' : ''}>
						${shop.shopName}
					</option>
				</c:forEach>
			</select>
		</div>
		
		<div class="col-md-4 text-md-end">
			<button type="submit" class="btn btn-primary me-2">
				<i class="fas fa-search me-1"></i> Search
			</button>
			<a href="${pageContext.request.contextPath}/admin/coupons"
				class="btn btn-outline-secondary"> <i class="fas fa-undo"></i>
				Reset
			</a>
		</div>
	</form>

	<div class="card shadow-sm">
		<div class="card-body">
			<div class="table-responsive">
				<table
					class="table align-middle table-striped table-hover text-center">
					<thead class="table-dark">
						<tr>
							<th>#</th>
							<th>Code</th>
							<th>Discount Value</th>
							<th>Minimum Order</th>
							<th>Max Discount</th>
							<th>Shop</th>
							<th>Expiry Date</th>
							<th>Quantity</th>
							<th>Status</th>
							<th>Actions</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="coupon" items="${coupons}" varStatus="status">
							<c:set var="isExpired" value="${coupon.expiryDate.isBefore(java.time.LocalDate.now())}" />
							<c:set var="isUsedUp" value="${coupon.quantity <= 0}" />
							<c:set var="couponStatus" value="" />
							<c:choose>
								<c:when test="${isExpired}">
									<c:set var="couponStatus" value="Expired" />
								</c:when>
								<c:when test="${isUsedUp}">
									<c:set var="couponStatus" value="Used Up" />
								</c:when>
								<c:otherwise>
									<c:set var="couponStatus" value="Active" />
								</c:otherwise>
							</c:choose>

							<tr>
								<td>${status.index + 1 + (currentPage - 1) * pageSize}</td>
								<td class="fw-bold text-success">${coupon.code}</td>
								
								<td>
								    <%-- Giả sử discountValue là giá trị cố định, bạn có thể thêm logic % nếu cần --%>
									<fmt:formatNumber value="${coupon.discountValue}" type="currency" currencyCode="VND" maxFractionDigits="0"/>
								</td>
								
								<td>
									<fmt:formatNumber value="${coupon.minOrderValue}" type="currency" currencyCode="VND" maxFractionDigits="0"/>
								</td>
								
								<td>
									<fmt:formatNumber value="${coupon.maxDiscount}" type="currency" currencyCode="VND" maxFractionDigits="0"/>
								</td>
								
								<td>${coupon.shop.shopName}</td>

								<td><fmt:formatDate value="${coupon.expiryDate}" pattern="dd/MM/yyyy"/></td>
								
								<td>${coupon.quantity}</td>
								
								<td>
									<span class="badge bg-
										${couponStatus == 'Active' ? 'success' : 
										 (couponStatus == 'Expired' ? 'danger' : 'warning')} 
										 px-3 py-2">
										 ${couponStatus} 
									</span>
								</td>

								<td>
									<a
										href="${pageContext.request.contextPath}/admin/coupons/edit?id=${coupon.couponId}"
										class="btn btn-sm btn-outline-primary me-1" title="Edit"> 
										<i class="fas fa-edit"></i>
									</a>
									<%-- Nút xóa, sử dụng form POST --%>
									<form
										action="${pageContext.request.contextPath}/admin/coupons/delete/${coupon.couponId}"
										method="post" class="d-inline"
										onsubmit="return confirm('Are you sure you want to delete coupon ${coupon.code}?');">
										<button type="submit"
											class="btn btn-sm btn-outline-danger"
											title="Delete Coupon">
											<i class="fas fa-trash"></i>
										</button>
									</form>
								</td>
							</tr>
						</c:forEach>

						<c:if test="${empty coupons}">
							<tr>
								<td colspan="10" class="text-center text-muted py-4"><i
									class="fas fa-ticket-alt fa-2x mb-2 d-block"></i> No
									coupons found.</td>
							</tr>
						</c:if>
					</tbody>
				</table>
			</div>

			<nav class="mt-3">
				<ul class="pagination justify-content-center">
					<%-- Chuẩn bị URL cơ sở cho phân trang, giữ lại keyword và shopId --%>
					<c:url var="paginationBaseUrl" value="/admin/coupons">
					    <c:param name="keyword" value="${param.keyword}"/>
					    <c:param name="shopId" value="${param.shopId}"/>
					</c:url>
					
					<c:if test="${currentPage > 1}">
						<li class="page-item"><a class="page-link"
							href="${paginationBaseUrl}&page=${currentPage - 1}">
								Previous </a></li>
					</c:if>

					<c:forEach begin="1" end="${totalPages}" var="page">
						<li class="page-item ${page == currentPage ? 'active' : ''}">
							<a class="page-link"
							href="${paginationBaseUrl}&page=${page}">
								${page} </a>
						</li>
					</c:forEach>

					<c:if test="${currentPage < totalPages}">
						<li class="page-item"><a class="page-link"
							href="${paginationBaseUrl}&page=${currentPage + 1}">
								Next </a></li>
					</c:if>
				</ul>
			</nav>
		</div>
	</div>
</div>