<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<sitemesh:decorate template="/WEB-INF/decorators/layout-admin.jsp" />

<div class="container-fluid">

	<!-- Header -->
	<div class="d-flex justify-content-between align-items-center mb-4">
		<h4 class="fw-bold text-primary mb-0">
			<i class="fas fa-list-alt me-2"></i> Category Management
		</h4>
		<a href="${pageContext.request.contextPath}/admin/categories/add"
			class="btn btn-primary"> <i class="fas fa-plus me-1"></i> Add
			Category
		</a>
	</div>

	<!-- Search + Filter -->
	<form class="row g-3 align-items-center mb-4" method="get"
		action="${pageContext.request.contextPath}/admin/categories">
		<div class="col-md-5">
			<input type="text" name="keyword" class="form-control"
				placeholder="🔍 Search by category name..." value="${param.keyword}">
		</div>
		<div class="col-md-3">
			<select name="banned" class="form-select">
				<option value="">All Status</option>
				<option value="false" ${param.banned == 'false' ? 'selected' : ''}>Active</option>
				<option value="true" ${param.banned == 'true' ? 'selected' : ''}>Banned</option>
			</select>
		</div>
		<div class="col-md-4 text-md-end">
			<button type="submit" class="btn btn-primary me-2">
				<i class="fas fa-search me-1"></i> Search
			</button>
			<a href="${pageContext.request.contextPath}/admin/categories"
				class="btn btn-outline-secondary"> <i class="fas fa-undo"></i>
				Reset
			</a>
		</div>
	</form>

	<!-- Category Table -->
	<div class="card shadow-sm">
		<div class="card-body">
			<div class="table-responsive">
				<table
					class="table align-middle table-striped table-hover text-center">
					<thead class="table-dark">
						<tr>
							<th>#</th>
							<th>Category Name</th>
							<!--  th>Image</th>-->
							<th>Description</th>
							<th>Status</th>
							<th>Actions</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="category" items="${categories}" varStatus="status">
							<tr>
								<td>${status.index + 1 + (currentPage - 1) * pageSize}</td>
								<td class="fw-semibold">${category.categoryName}</td>

								

								<td class="text-truncate" style="max-width: 250px;">
									${category.description}</td>

								<td><span
									class="badge bg-${category.isBanned ? 'danger' : 'success'} px-3 py-2">
										${category.isBanned ? 'Banned' : 'Active'} </span></td>

								<td><a
									href="${pageContext.request.contextPath}/admin/categories/edit?id=${category.categoryId}"
									class="btn btn-sm btn-outline-primary me-1" title="Edit"> <i
										class="fas fa-edit"></i>
								</a>
									<form
										action="${pageContext.request.contextPath}/admin/categories/toggle-ban/${category.categoryId}"
										method="post" class="d-inline"
										onsubmit="return confirm('Are you sure you want to ${category.isBanned ? 'unban' : 'ban'} this category?');">
										<button type="submit"
											class="btn btn-sm btn-outline-${category.isBanned ? 'success' : 'danger'}"
											title="${category.isBanned ? 'Unban' : 'Ban'} Category">
											<i class="fas fa-ban"></i>
										</button>
									</form></td>
							</tr>
						</c:forEach>

						<c:if test="${empty categories}">
							<tr>
								<td colspan="6" class="text-center text-muted py-4"><i
									class="fas fa-folder-open fa-2x mb-2 d-block"></i> No
									categories found.</td>
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
							href="?page=${currentPage - 1}&keyword=${param.keyword}&banned=${param.banned}">
								Previous </a></li>
					</c:if>

					<c:forEach begin="1" end="${totalPages}" var="page">
						<li class="page-item ${page == currentPage ? 'active' : ''}">
							<a class="page-link"
							href="?page=${page}&keyword=${param.keyword}&banned=${param.banned}">
								${page} </a>
						</li>
					</c:forEach>

					<c:if test="${currentPage < totalPages}">
						<li class="page-item"><a class="page-link"
							href="?page=${currentPage + 1}&keyword=${param.keyword}&banned=${param.banned}">
								Next </a></li>
					</c:if>
				</ul>
			</nav>
		</div>
	</div>
</div>
