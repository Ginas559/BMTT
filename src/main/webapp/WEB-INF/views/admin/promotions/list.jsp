<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%-- Sử dụng layout chung cho trang quản trị --%>
<sitemesh:decorate template="/WEB-INF/decorators/layout-admin.jsp" />

<div class="container-fluid">

	<div class="d-flex justify-content-between align-items-center mb-4">
		<h4 class="fw-bold text-primary mb-0">
			<i class="fas fa-bullhorn me-2"></i> Promotion Management
		</h4>
		<a href="${pageContext.request.contextPath}/admin/promotions/add"
			class="btn btn-primary"> <i class="fas fa-plus me-1"></i> Add
			Promotion
		</a>
	</div>

	<form class="row g-3 align-items-center mb-4" method="get"
		action="${pageContext.request.contextPath}/admin/promotions">

		<div class="col-md-3">
			<input type="text" name="keyword" class="form-control"
				placeholder="🔍 Search by promotion title..."
				value="${param.keyword}">
		</div>



		<div class="col-md-2">
			<select name="applyTo" class="form-select">
				<option value="">All Applications</option>
				<option value="PRODUCT"
					${param.applyTo == 'PRODUCT' ? 'selected' : ''}>Product
					Discount</option>
				<option value="SHIPPING"
					${param.applyTo == 'SHIPPING' ? 'selected' : ''}>Shipping
					Fee</option>
			</select>
		</div>

		<div class="col-md-2">
			<select name="status" class="form-select">
				<option value="">All Statuses</option>
				<%-- Giả định Controller truyền vào 'allStatuses' chứa PromotionStatus.values() --%>
				<c:forEach var="s" items="${allStatuses}">
					<option value="${s}" ${param.status == s ? 'selected' : ''}>
						${s == 'ACTIVE' ? 'Active' : 'Inactive'}</option>
				</c:forEach>
			</select>
		</div>

		<div class="col-md-3 text-md-end">
			<button type="submit" class="btn btn-primary me-2">
				<i class="fas fa-search me-1"></i> Search
			</button>
			<a href="${pageContext.request.contextPath}/admin/promotions"
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
							<th>Title</th>
							<th>Shop</th>
							<th>Discount %</th>
							<th>Applies To</th>
							<th>Start Date</th>
							<th>End Date</th>
							<th>Status</th>
							<th>Actions</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="promotion" items="${promotions}"
							varStatus="status">

							<%-- Xác định thời gian để kiểm tra logic Active/Expired --%>
							<%-- LƯU Ý: JSTL và EL KHÔNG HỖ TRỢ LocalDate.now() trực tiếp trong thẻ c:set. --%>
							<%-- Bạn cần truyền nó từ Controller dưới dạng một thuộc tính (Ví dụ: todayDate) --%>



							<tr>
								<td>${status.index + 1 + (currentPage - 1) * pageSize}</td>
								<td class="text-start fw-semibold" style="max-width: 250px;">
									${promotion.title}</td>

								<td>${promotion.shop.shopName}</td>

								<td><fmt:formatNumber value="${promotion.discountPercent}" />
									<i class="fas fa-percent"></i></td>

								<td><span class="badge bg-secondary px-3 py-2">
										${promotion.applyTo == 'PRODUCT' ? 'Product' : 'Shipping'} </span></td>

								<%-- Sửa cách format LocalDate --%>
								<td>
									${promotion.startDate.dayOfMonth}/${promotion.startDate.monthValue}/${promotion.startDate.year}
								</td>
								<td>
									${promotion.endDate.dayOfMonth}/${promotion.endDate.monthValue}/${promotion.endDate.year}
								</td>

								<td><c:choose>
										<%-- Ưu tiên kiểm tra đã hết hạn --%>
										<c:when test="${isExpired}">
											<span class="badge bg-danger px-3 py-2">Expired</span>
										</c:when>
										<%-- Kiểm tra sắp diễn ra --%>
										<c:when test="${isUpcoming}">
											<span class="badge bg-info text-dark px-3 py-2">Upcoming</span>
										</c:when>
										<%-- Kiểm tra trạng thái ACTIVE của Promotion Entity --%>
										<c:when test="${promotion.status == 'ACTIVE'}">
											<span class="badge bg-success px-3 py-2">Active</span>
										</c:when>
										<c:otherwise>
											<span class="badge bg-secondary px-3 py-2">Inactive</span>
										</c:otherwise>
									</c:choose></td>

								<td><a
									href="${pageContext.request.contextPath}/admin/promotions/edit?id=${promotion.promotionId}"
									class="btn btn-sm btn-outline-primary me-1" title="Edit"> <i
										class="fas fa-edit"></i>
								</a> <%-- Nút BẬT/TẮT trạng thái (Toggle Status) --%>
									<form
										action="${pageContext.request.contextPath}/admin/promotions/toggle-status/${promotion.promotionId}"
										method="post" class="d-inline"
										onsubmit="return confirm('Do you want to change the status of promotion: ${promotion.title}?');">

										<c:set var="btnClass"
											value="${promotion.status == 'ACTIVE' ? 'btn-outline-warning' : 'btn-outline-success'}" />
										<c:set var="iconClass"
											value="${promotion.status == 'ACTIVE' ? 'fa-ban' : 'fa-check'}" />
										<c:set var="titleText"
											value="${promotion.status == 'ACTIVE' ? 'Deactivate' : 'Activate'}" />

										<button type="submit" class="btn btn-sm ${btnClass}"
											title="${titleText}">
											<i class="fas ${iconClass}"></i>
										</button>
									</form></td>
							</tr>
						</c:forEach>

						<c:if test="${empty promotions}">
							<tr>
								<td colspan="9" class="text-center text-muted py-4"><i
									class="fas fa-gift fa-2x mb-2 d-block"></i> No promotions
									found.</td>
							</tr>
						</c:if>
					</tbody>
				</table>
			</div>

			<nav class="mt-3">
				<ul class="pagination justify-content-center">
					<%-- Chuẩn bị URL cơ sở cho phân trang, giữ lại keyword, shopId, applyTo và status --%>
					<c:url var="paginationBaseUrl" value="/admin/promotions">
						<c:param name="keyword" value="${param.keyword}" />
						<c:param name="shopId" value="${param.shopId}" />
						<c:param name="applyTo" value="${param.applyTo}" />
						<c:param name="status" value="${param.status}" />
						<%-- Thêm param status --%>
					</c:url>

					<c:if test="${currentPage > 1}">
						<li class="page-item"><a class="page-link"
							href="${paginationBaseUrl}&page=${currentPage - 1}"> Previous
						</a></li>
					</c:if>

					<c:forEach begin="1" end="${totalPages}" var="page">
						<li class="page-item ${page == currentPage ? 'active' : ''}">
							<a class="page-link" href="${paginationBaseUrl}&page=${page}">
								${page} </a>
						</li>
					</c:forEach>

					<c:if test="${currentPage < totalPages}">
						<li class="page-item"><a class="page-link"
							href="${paginationBaseUrl}&page=${currentPage + 1}"> Next </a></li>
					</c:if>
				</ul>
			</nav>
		</div>
	</div>
</div>