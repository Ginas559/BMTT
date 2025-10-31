<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<sitemesh:decorate template="/WEB-INF/decorators/layout-admin.jsp" />

<div class="container-fluid">

	<!-- Header -->
	<!-- Header -->
	<div class="d-flex justify-content-between align-items-center mb-4">
		<h4 class="fw-bold text-primary mb-0">
			<i class="fas fa-list-alt me-2"></i> Shops Management
		</h4>
		<a href="${pageContext.request.contextPath}/admin/shops/add"
			class="btn btn-primary"> <i class="fas fa-plus me-1"></i> Add
			Shop
		</a>
	</div>

	<!-- Search + Filter -->
	<form class="row g-3 align-items-center mb-4" method="get"
		action="${pageContext.request.contextPath}/admin/shops">
		<div class="col-md-5">
			<input type="text" name="keyword" class="form-control"
				placeholder="🔍 Search by shop name..." value="${param.keyword}">
		</div>
		<div class="col-md-3">
			<select name="status" class="form-select">
				<option value="">All Status</option>
				<option value="ACTIVE" ${param.status == 'ACTIVE' ? 'selected' : ''}>Active</option>
				<option value="PENDING"
					${param.status == 'PENDING' ? 'selected' : ''}>Pending</option>
				<option value="BANNED" ${param.status == 'BANNED' ? 'selected' : ''}>Banned</option>
			</select>
		</div>
		<div class="col-md-4 text-md-end">
			<button type="submit" class="btn btn-primary me-2">
				<i class="fas fa-search me-1"></i> Search
			</button>
			<a href="${pageContext.request.contextPath}/admin/shops"
				class="btn btn-outline-secondary"> <i class="fas fa-undo"></i>
				Reset
			</a>
		</div>
	</form>

	<!-- Shop Table -->
	<div class="card shadow-sm">
		<div class="card-body">
			<div class="table-responsive">
				<table
					class="table align-middle table-striped table-hover text-center">
					<thead class="table-dark">
						<tr>
							<th>#</th>
							<th>Shop Name</th>
							<th>Owner</th>
							<th>Description</th>
							<th>Status</th>
							<th>Created At</th>
							<th>Actions</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="shop" items="${shops}" varStatus="statusLoop">
							<tr>
								<td>${statusLoop.index + 1 + (currentPage - 1) * pageSize}</td>
								<td class="fw-semibold">${shop.shopName}</td>
								<td>${shop.vendor.firstname}${shop.vendor.lastname}</td>
								<td class="text-truncate" style="max-width: 250px;">
									${shop.description}</td>

								<!-- ✅ HIỂN THỊ STATUS -->
								<td><span
									class="badge 
										${shop.status == 'ACTIVE' ? 'bg-success' :
										 shop.status == 'PENDING' ? 'bg-warning text-dark' :
										 shop.status == 'BANNED' ? 'bg-danger' : 'bg-secondary'} px-3 py-2">
										${shop.status} </span></td>

								<td>
									${shop.createdAt.dayOfMonth}/${shop.createdAt.monthValue}/${shop.createdAt.year}
									${shop.createdAt.hour}:${shop.createdAt.minute}</td>

								<td><a
									href="${pageContext.request.contextPath}/admin/shops/detail?id=${shop.shopId}"
									class="btn btn-sm btn-outline-primary me-1"
									title="View Details"> <i class="fas fa-eye"></i>
								</a>
									<form
										action="${pageContext.request.contextPath}/admin/shops/toggle-status/${shop.shopId}"
										method="post" class="d-inline"
										onsubmit="return confirm('Are you sure you want to change status of this shop?');">
										<button type="submit"
											class="btn btn-sm btn-outline-${shop.status == 'BANNED' ? 'success' : 'danger'}"
											title="${shop.status == 'BANNED' ? 'Unban Shop' : 'Ban Shop'}">
											<i class="fas fa-ban"></i>
										</button>
									</form></td>
							</tr>
						</c:forEach>

						<c:if test="${empty shops}">
							<tr>
								<td colspan="7" class="text-center text-muted py-4"><i
									class="fas fa-store-slash fa-2x mb-2 d-block"></i> No shops
									found.</td>
							</tr>
						</c:if>
					</tbody>
				</table>
			</div>

			<!-- Pagination -->
			<nav class="mt-3">
				<ul class="pagination justify-content-center">
					<c:if test="${currentPage > 1}">
						<li class="page-item"><a class="page-link"
							href="?page=${currentPage - 1}&keyword=${param.keyword}&status=${param.status}">
								Previous </a></li>
					</c:if>

					<c:forEach begin="1" end="${totalPages}" var="page">
						<li class="page-item ${page == currentPage ? 'active' : ''}">
							<a class="page-link"
							href="?page=${page}&keyword=${param.keyword}&status=${param.status}">
								${page} </a>
						</li>
					</c:forEach>

					<c:if test="${currentPage < totalPages}">
						<li class="page-item"><a class="page-link"
							href="?page=${currentPage + 1}&keyword=${param.keyword}&status=${param.status}">
								Next </a></li>
					</c:if>
				</ul>
			</nav>
		</div>
	</div>
</div>
