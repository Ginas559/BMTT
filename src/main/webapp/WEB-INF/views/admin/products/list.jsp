<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<sitemesh:decorate template="/WEB-INF/decorators/layout-admin.jsp" />

<div class="container-fluid">

	<!-- Header -->
	<div class="d-flex justify-content-between align-items-center mb-4">
		<h4 class="fw-bold text-primary mb-0">
			<i class="fas fa-box me-2"></i> Product Management
		</h4>
		<a href="${pageContext.request.contextPath}/admin/products/add"
			class="btn btn-primary">
			<i class="fas fa-plus me-1"></i> Add Product
		</a>
	</div>

	<!-- Search + Filter -->
	<form class="row g-3 align-items-center mb-4" method="get"
		action="${pageContext.request.contextPath}/admin/products">

		<!-- Search by name -->
		<div class="col-md-4">
			<input type="text" name="keyword" class="form-control"
				placeholder="🔍 Search by product name..." value="${param.keyword}">
		</div>

		<!-- Filter by Category -->
		<div class="col-md-3">
			<select name="categoryId" class="form-select">
				<option value="">All Categories</option>
				<c:forEach var="cat" items="${categories}">
					<option value="${cat.categoryId}"
						${param.categoryId == cat.categoryId ? 'selected' : ''}>
						${cat.categoryName}
					</option>
				</c:forEach>
			</select>
		</div>

		<!-- Filter by Shop -->
		<div class="col-md-3">
			<select name="shopId" class="form-select">
				<option value="">All Shops</option>
				<c:forEach var="s" items="${shops}">
					<option value="${s.shopId}"
						${param.shopId == s.shopId ? 'selected' : ''}>
						${s.shopName}
					</option>
				</c:forEach>
			</select>
		</div>

		<!-- Filter by Status -->
		<div class="col-md-2">
			<select name="banned" class="form-select">
				<option value="">All Status</option>
				<option value="false" ${param.banned == 'false' ? 'selected' : ''}>Active</option>
				<option value="true" ${param.banned == 'true' ? 'selected' : ''}>Banned</option>
			</select>
		</div>

		<!-- Buttons -->
		<div class="col-12 text-end">
			<button type="submit" class="btn btn-primary me-2">
				<i class="fas fa-search me-1"></i> Search
			</button>
			<a href="${pageContext.request.contextPath}/admin/products"
				class="btn btn-outline-secondary">
				<i class="fas fa-undo"></i> Reset
			</a>
		</div>
	</form>

	<!-- Product Table -->
	<div class="card shadow-sm">
		<div class="card-body">
			<div class="table-responsive">
				<table class="table align-middle table-striped table-hover text-center">
					<thead class="table-dark">
						<tr>
							<th>#</th>
							<th>Product Name</th>
							<th>Category</th>
							<th>Shop</th>
							<th>Price</th>
							<th>Stock</th>
							<th>Status</th>
							
							<th>Actions</th>
						</tr>
					</thead>

					<tbody>
						<c:forEach var="product" items="${products}" varStatus="status">
							<tr>
								<td>${status.index + 1 + (currentPage - 1) * pageSize}</td>
								<td class="fw-semibold text-start">${product.productName}</td>
								<td>${product.category.categoryName}</td>
								<td>${product.shop.shopName}</td>

								<td>
									<fmt:formatNumber value="${product.price}" type="currency" currencySymbol="₫"/>
								</td>

								<td>${product.stock}</td>

								<td>
									<span class="badge bg-${product.isBanned ? 'danger' : 'success'} px-3 py-2">
										${product.isBanned ? 'Banned' : 'Active'}
									</span>
								</td>

								<td>
									<!-- View Details -->
									<a href="${pageContext.request.contextPath}/admin/products/detail?id=${product.productId}"
										class="btn btn-sm btn-outline-info me-1" title="View Details">
										<i class="fas fa-eye"></i>
									</a>

									<!-- Edit -->
									<a href="${pageContext.request.contextPath}/admin/products/edit?id=${product.productId}"
										class="btn btn-sm btn-outline-primary me-1" title="Edit">
										<i class="fas fa-edit"></i>
									</a>

									<!-- Ban / Unban -->
									<form action="${pageContext.request.contextPath}/admin/products/toggle-ban/${product.productId}"
										method="post" class="d-inline"
										onsubmit="return confirm('Are you sure you want to ${product.isBanned ? 'unban' : 'ban'} this product?');">
										<button type="submit"
											class="btn btn-sm btn-outline-${product.isBanned ? 'success' : 'danger'}"
											title="${product.isBanned ? 'Unban' : 'Ban'} Product">
											<i class="fas fa-ban"></i>
										</button>
									</form>
								</td>
							</tr>
						</c:forEach>

						<c:if test="${empty products}">
							<tr>
								<td colspan="8" class="text-center text-muted py-4">
									<i class="fas fa-box-open fa-2x mb-2 d-block"></i>
									No products found.
								</td>
							</tr>
						</c:if>
					</tbody>
				</table>
			</div>

			<!-- Pagination -->
			<nav class="mt-3">
				<ul class="pagination justify-content-center">
					<c:if test="${currentPage > 1}">
						<li class="page-item">
							<a class="page-link"
								href="?page=${currentPage - 1}&keyword=${param.keyword}&categoryId=${param.categoryId}&shopId=${param.shopId}&banned=${param.banned}">
								Previous
							</a>
						</li>
					</c:if>

					<c:forEach begin="1" end="${totalPages}" var="page">
						<li class="page-item ${page == currentPage ? 'active' : ''}">
							<a class="page-link"
								href="?page=${page}&keyword=${param.keyword}&categoryId=${param.categoryId}&shopId=${param.shopId}&banned=${param.banned}">
								${page}
							</a>
						</li>
					</c:forEach>

					<c:if test="${currentPage < totalPages}">
						<li class="page-item">
							<a class="page-link"
								href="?page=${currentPage + 1}&keyword=${param.keyword}&categoryId=${param.categoryId}&shopId=${param.shopId}&banned=${param.banned}">
								Next
							</a>
						</li>
					</c:if>
				</ul>
			</nav>
		</div>
	</div>
</div>
