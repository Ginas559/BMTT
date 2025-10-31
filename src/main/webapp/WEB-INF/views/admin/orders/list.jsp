<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%-- Sử dụng layout chung cho trang quản trị --%>
<sitemesh:decorate template="/WEB-INF/decorators/layout-admin.jsp" />

<div class="container-fluid">

	<div class="d-flex justify-content-between align-items-center mb-4">
		<h4 class="fw-bold text-primary mb-0">
			<i class="fas fa-box-open me-2"></i> Order Management
		</h4>
		<%-- Thường không có nút "Add Order" trong trang quản trị, nên bỏ qua --%>
	</div>

	<form class="row g-3 align-items-center mb-4" method="get"
		action="${pageContext.request.contextPath}/admin/orders">
		
		<div class="col-md-3">
			<select name="status" class="form-select">
				<option value="">All Status</option>
				<%-- Dùng JSTL để lặp qua Enum/List các trạng thái đơn hàng (được truyền từ Controller) --%>
				<c:forEach var="stt" items="${orderStatuses}">
					<option value="${stt}" ${param.status == stt ? 'selected' : ''}>
						${stt} 
					</option>
				</c:forEach>
			</select>
		</div>
		
		<div class="col-md-3">
			<select name="shopId" class="form-select">
				<option value="">All Shops</option>
				<%-- Lặp qua danh sách các Shop (được truyền từ Controller) --%>
				<c:forEach var="shop" items="${shops}">
					<option value="${shop.shopId}" ${param.shopId == shop.shopId ? 'selected' : ''}>
						${shop.shopName}
					</option>
				</c:forEach>
			</select>
		</div>
		
		<div class="col-md-3">
			<select name="categoryId" class="form-select">
				<option value="">All Categories</option>
				<%-- Lặp qua danh sách các Category (được truyền từ Controller) --%>
				<c:forEach var="category" items="${categories}">
					<option value="${category.categoryId}" ${param.categoryId == category.categoryId ? 'selected' : ''}>
						${category.categoryName}
					</option>
				</c:forEach>
			</select>
		</div>

		<div class="col-md-3 text-md-end">
			<button type="submit" class="btn btn-primary me-2">
				<i class="fas fa-search me-1"></i> Search
			</button>
			<a href="${pageContext.request.contextPath}/admin/orders"
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
							<th>Order ID</th>
							<th>Customer</th>
							<th>Total Amount</th>
							<th>Shop</th>
							
							<th>Status</th>
							<th>Actions</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="order" items="${orders}" varStatus="status">
							<tr>
								<td>${status.index + 1 + (currentPage - 1) * pageSize}</td>
								<td class="fw-semibold">${order.orderId}</td>
								<td>${order.user.firstname} ${order.user.lastname}</td> <%-- Giả sử Order có thuộc tính user --%>
								<td>
									<fmt:formatNumber value="${order.totalAmount}" type="currency" currencyCode="VND" maxFractionDigits="0"/>
								</td>
								<td>${order.shop.shopName}</td> <%-- Giả sử Order có thuộc tính shop --%>
								
								<td>
									<span class="badge bg-secondary px-3 py-2">
										${order.status}
									</span>
								</td>

								<td>
									<%-- Nút xem chi tiết đơn hàng --%>
									<a href="${pageContext.request.contextPath}/admin/orders/details?id=${order.orderId}"
										class="btn btn-sm btn-outline-info me-1" title="View Details"> 
										<i class="fas fa-eye"></i>
									</a>
									<%-- Nút cập nhật trạng thái (ví dụ: Change Status) --%>
									<a href="${pageContext.request.contextPath}/admin/orders/status?id=${order.orderId}"
										class="btn btn-sm btn-outline-warning" title="Update Status"> 
										<i class="fas fa-sync-alt"></i>
									</a>
								</td>
							</tr>
						</c:forEach>

						<c:if test="${empty orders}">
							<tr>
								<td colspan="8" class="text-center text-muted py-4"><i
									class="fas fa-shopping-basket fa-2x mb-2 d-block"></i> No
									orders found matching the criteria.</td>
							</tr>
						</c:if>
					</tbody>
				</table>
			</div>

			<nav class="mt-3">
				<ul class="pagination justify-content-center">
					<%-- Lấy tất cả các tham số tìm kiếm hiện tại để giữ lại khi chuyển trang --%>
					<c:url var="paginationBaseUrl" value="/admin/orders">
					    <c:param name="status" value="${param.status}"/>
					    <c:param name="shopId" value="${param.shopId}"/>
					    <c:param name="categoryId" value="${param.categoryId}"/>
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