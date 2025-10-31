<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<sitemesh:decorate template="/WEB-INF/decorators/layout-admin.jsp" />

<div class="container-fluid">

	<!-- Header -->
	<div class="d-flex justify-content-between align-items-center mb-4">
		<h4 class="fw-bold text-primary mb-0">
			<i class="fas fa-store me-2"></i> Edit Shop
		</h4>
		<a href="${pageContext.request.contextPath}/admin/shops"
			class="btn btn-outline-secondary"> <i class="fas fa-arrow-left me-1"></i>
			Back to List
		</a>
	</div>

	<!-- Form -->
	<div class="card shadow-sm">
		<div class="card-body">
			<form action="${pageContext.request.contextPath}/admin/shops/update" method="post"
				enctype="multipart/form-data">

				<input type="hidden" name="shopId" value="${shop.shopId}">

				<div class="row mb-3">
					<label class="col-sm-2 col-form-label fw-semibold">Shop Name:</label>
					<div class="col-sm-10">
						<input type="text" name="shopName" class="form-control"
							value="${shop.shopName}" required maxlength="150">
					</div>
				</div>

				<div class="row mb-3">
					<label class="col-sm-2 col-form-label fw-semibold">Owner (Vendor):</label>
					<div class="col-sm-10">
						<input type="text" class="form-control"
							value="${shop.vendor.firstname} ${shop.vendor.lastname}" readonly>
					</div>
				</div>

				<div class="row mb-3">
					<label class="col-sm-2 col-form-label fw-semibold">Description:</label>
					<div class="col-sm-10">
						<textarea name="description" class="form-control" rows="4"
							placeholder="Enter shop description...">${shop.description}</textarea>
					</div>
				</div>

				<!-- Logo Upload -->
				<div class="row mb-3 align-items-center">
					<label class="col-sm-2 col-form-label fw-semibold">Logo:</label>
					<div class="col-sm-10 d-flex align-items-center">
						<c:choose>
							<c:when test="${not empty shop.logoUrl}">
								<img src="${pageContext.request.contextPath}/image?type=shops&fname=${shop.logoUrl}"
									alt="Logo" class="img-thumbnail me-3" style="width: 100px; height: 100px;">
							</c:when>
							<c:otherwise>
								<span class="text-muted fst-italic me-3">No logo uploaded</span>
							</c:otherwise>
						</c:choose>

						<input type="file" name="logoFile" class="form-control"
							accept="image/*" style="max-width: 300px;">
					</div>
				</div>

				<div class="row mb-3">
					<label class="col-sm-2 col-form-label fw-semibold">Status:</label>
					<div class="col-sm-10">
						<select name="status" class="form-select" required>
							<option value="ACTIVE" ${shop.status == 'ACTIVE' ? 'selected' : ''}>Active</option>
							<option value="PENDING" ${shop.status == 'PENDING' ? 'selected' : ''}>Pending</option>
							<option value="BANNED" ${shop.status == 'BANNED' ? 'selected' : ''}>Banned</option>
						</select>
					</div>
				</div>

				<div class="row mb-3">
					<label class="col-sm-2 col-form-label fw-semibold">Created At:</label>
					<div class="col-sm-10">
						<input type="text" class="form-control"
							value="${shop.createdAt}" readonly>
					</div>
				</div>

				<!-- Submit Buttons -->
				<div class="text-end">
					<button type="submit" class="btn btn-primary px-4">
						<i class="fas fa-save me-1"></i> Save Changes
					</button>
					<a href="${pageContext.request.contextPath}/admin/shops"
						class="btn btn-outline-secondary px-4 ms-2">Cancel</a>
				</div>
			</form>
		</div>
	</div>
</div>
