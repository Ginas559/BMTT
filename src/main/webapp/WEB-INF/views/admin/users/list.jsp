<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<sitemesh:decorate template="/WEB-INF/decorators/layout-admin.jsp" />

<div class="container-fluid">

	<!-- Header -->
	<div class="d-flex justify-content-between align-items-center mb-4">
		<h4 class="fw-bold text-primary mb-0">
			<i class="fas fa-users me-2"></i> User Management
		</h4>
		<a href="${pageContext.request.contextPath}/admin/users/add"
			class="btn btn-primary"> <i class="fas fa-plus me-1"></i> Add
			User
		</a>
	</div>

	<!-- Search + Filter -->
	<form class="row g-3 mb-4" method="get"
		action="${pageContext.request.contextPath}/admin/users">
		<div class="col-md-4">
			<input type="text" name="keyword" class="form-control"
				placeholder="Search by name or email..." value="${param.keyword}">
		</div>
		<div class="col-md-3">
			<select name="banned" class="form-select">
				<option value="">All Users</option>
				<option value="false" ${param.banned == 'false' ? 'selected' : ''}>Active</option>
				<option value="true" ${param.banned == 'true' ? 'selected' : ''}>Banned</option>
			</select>
		</div>
		<div class="col-md-3">
			<button type="submit" class="btn btn-primary">
				<i class="fas fa-search me-1"></i> Search
			</button>
			<a href="${pageContext.request.contextPath}/admin/users"
				class="btn btn-outline-secondary ms-2"> <i class="fas fa-undo"></i>
				Reset
			</a>
		</div>
	</form>

	<!-- User Table -->
	<div class="card shadow-sm">
		<div class="card-body">
			<div class="table-responsive">
				<table class="table align-middle table-striped table-hover">
					<thead class="table-dark">
						<tr>
							<th class="text-center">#</th>
							<th>Avatar</th>
							<th>Full Name</th>
							<th>Email</th>
							<th>Phone</th>
							<th>Role</th>
							<th class="text-center">Verified</th>
							<th class="text-center">Status</th>
							<th class="text-center">Created</th>
							<th class="text-center">Actions</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="user" items="${users}" varStatus="status">
							<tr>
								<td class="text-center">${status.index + 1 + (currentPage - 1) * pageSize}</td>
								<td><img
									src="<c:out value='${user.avatar != null ? pageContext.request.contextPath.concat("/uploads/avatars/").concat(user.avatar) : pageContext.request.contextPath.concat("/assets/img/default-avatar.png")}'/>"
									alt="avatar" class="rounded-circle" width="45" height="45" /></td>
								<td>${user.firstname}${user.lastname}</td>
								<td>${user.email}</td>
								<td>${user.phone}</td>
								<td><span
									class="badge bg-${user.role.name() == 'ADMIN' ? 'danger' : (user.role.name() == 'VENDOR' ? 'info' : (user.role.name() == 'SHIPPER' ? 'warning' : 'secondary'))}">
										${user.role.name()} </span></td>
								<td class="text-center"><c:if test="${user.isEmailActive}">
										<i class="fas fa-envelope-circle-check text-success"
											title="Email verified"></i>
									</c:if> <c:if test="${!user.isEmailActive}">
										<i class="fas fa-envelope text-muted" title="Not verified"></i>
									</c:if> <c:if test="${user.isPhoneActive}">
										<i class="fas fa-phone-circle-check text-success ms-2"
											title="Phone verified"></i>
									</c:if> <c:if test="${!user.isPhoneActive}">
										<i class="fas fa-phone text-muted ms-2" title="Not verified"></i>
									</c:if></td>
								<td class="text-center"><span
									class="badge bg-${user.isBanned ? 'danger' : 'success'}">
										${user.isBanned ? 'Banned' : 'Active'} </span></td>
								<td class="text-center">${user.createdAt.toLocalDate()}
									${user.createdAt.toLocalTime()}</td>

								<td class="text-center"><a
									href="${pageContext.request.contextPath}/admin/users/edit?id=${user.id}"
									class="btn btn-sm btn-outline-primary me-1" title="Edit"> <i
										class="fas fa-edit"></i>
								</a>
									<form
										action="${pageContext.request.contextPath}/admin/users/toggle-ban/${user.id}"
										method="post" class="d-inline"
										onsubmit="return confirm('Are you sure you want to ${user.isBanned ? 'unban' : 'ban'} this user?');">
										<button type="submit"
											class="btn btn-sm btn-outline-${user.isBanned ? 'success' : 'danger'}"
											title="${user.isBanned ? 'Unban User' : 'Ban User'}">
											<i class="fas fa-user-slash"></i>
										</button>
									</form></td>
							</tr>
						</c:forEach>

						<c:if test="${empty users}">
							<tr>
								<td colspan="10" class="text-center text-muted py-4"><i
									class="fas fa-user-slash fa-2x mb-2 d-block"></i> No users
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
