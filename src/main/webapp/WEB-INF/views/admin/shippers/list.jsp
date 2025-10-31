<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<sitemesh:decorate template="/WEB-INF/decorators/layout-admin.jsp" />

<div class="container-fluid">

	<div class="d-flex justify-content-between align-items-center mb-4">
		<h4 class="fw-bold text-primary mb-0">
			<i class="fas fa-shipping-fast me-2"></i> Shipper Management
		</h4>
		<a href="${pageContext.request.contextPath}/admin/shippers/add"
			class="btn btn-primary"> <i class="fas fa-plus me-1"></i> Add
			Shipper
		</a>
	</div>

	<form class="row g-3 mb-4" method="get"
		action="${pageContext.request.contextPath}/admin/shippers">
		<div class="col-md-4">
			<input type="text" name="keyword" class="form-control"
				placeholder="Search by name or email..." value="${param.keyword}">
		</div>
		<div class="col-md-3">
			<select name="banned" class="form-select">
				<option value="">All Shippers</option>
				<option value="false" ${param.banned == 'false' ? 'selected' : ''}>Active</option>
				<option value="true" ${param.banned == 'true' ? 'selected' : ''}>Banned</option>
			</select>
		</div>
		<div class="col-md-3">
			<button type="submit" class="btn btn-primary">
				<i class="fas fa-search me-1"></i> Search
			</button>
			<a href="${pageContext.request.contextPath}/admin/shippers"
				class="btn btn-outline-secondary ms-2"> <i class="fas fa-undo"></i>
				Reset
			</a>
		</div>
	</form>

	<div class="card shadow-sm">
		<div class="card-body">
			<div class="table-responsive">
				<table class="table align-middle table-striped table-hover">
					<thead class="table-dark">
						<tr>
							<th class="text-center">#</th>
							<th>FullName</th>
							<th>Email</th>
							<th>Phone</th>
							

							<th class="text-center">Actions</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="shipper" items="${shippers}" varStatus="status">
							<tr>
								<td class="text-center">${status.index + 1 + (currentPage - 1) * pageSize}</td>

								<td>${shipper.firstname}${shipper.lastname}</td>
								<td>${shipper.email}</td>
								<td>${shipper.phone}</td>


								<td class="text-center">
									<%-- Hành động 1: Xem chi tiết Shipper --%> <a
									href="${pageContext.request.contextPath}/admin/shippers/details?id=${shipper.id}"
									class="btn btn-sm btn-outline-info me-1" title="View Details">
										<i class="fas fa-eye"></i>
								</td>
							</tr>
						</c:forEach>

						<c:if test="${empty shippers}">
							<tr>
								<td colspan="9" class="text-center text-muted py-4"><i
									class="fas fa-truck-monster fa-2x mb-2 d-block"></i> No
									shippers found.</td>
							</tr>
						</c:if>
					</tbody>
				</table>
			</div>

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