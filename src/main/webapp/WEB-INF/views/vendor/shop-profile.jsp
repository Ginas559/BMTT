<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<!doctype html>
<html lang="vi">
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<title>${pageTitle != null ? pageTitle : 'BMTT Shop'}</title>

<sitemesh:write property="head" />

<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
	rel="stylesheet">

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/web.css">

<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
</head>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="main-shop-profile py-4">

	<h3 class="mb-3 gradient-text">Hồ sơ cửa hàng</h3>

	<c:if test="${not empty shop and not empty shop.shopId}">
		<div class="mb-3 d-flex flex-wrap gap-2">
		
			<a href="${ctx}/chat?shopId=${shop.shopId}" class="btn btn-chat">
				💬 Chat nội bộ
			</a>

			<a href="${ctx}/vendor/staffs" class="btn btn-success">
				👥 Quản lý nhân viên
				<c:if test="${not empty staffCount}">
					<span class="badge bg-light text-dark">${staffCount}</span>
				</c:if>
			</a>
		</div>
	</c:if>

	<form action="${ctx}/vendor/shop" method="post"
		enctype="multipart/form-data" class="card kpi-card p-3 shadow-sm">
		<div class="mb-3">
			<label class="form-label">Tên cửa hàng</label> 
			<input type="text" class="form-control" name="shopName"
				value="${shop.shopName}" required />
		</div>

		<div class="mb-3">
			<label class="form-label">Mô tả</label>
			<textarea class="form-control" name="description" rows="3">${shop.description}</textarea>
		</div>

		<div class="row">
			<div class="col-md-6 mb-3">
				<label class="form-label">Logo (Avatar)</label> 
				<input type="file" class="form-control" name="logo" accept="image/*" />
				<div class="mt-2 d-flex align-items-center gap-3">
					<c:choose>
						<c:when test="${not empty shop.logoUrl}">
							<img src="${shop.logoUrl}" class="rounded-circle border shadow-sm"
								style="width: 96px; height: 96px; object-fit: cover;">
						</c:when>
						<c:otherwise>
							<img src="https://via.placeholder.com/96?text=Logo"
								class="rounded-circle border shadow-sm"
								style="width: 96px; height: 96px; object-fit: cover;">
						</c:otherwise>
					</c:choose>
					<small class="text-muted">Chọn ảnh mới để thay logo (tùy chọn).</small>
				</div>
			</div>

			<div class="col-md-6 mb-3">
				<label class="form-label">Ảnh bìa (Cover)</label> 
				<input type="file" class="form-control" name="cover" accept="image/*" />
				<div class="mt-2">
					<c:choose>
						<c:when test="${not empty shop.coverUrl}">
							<img src="${shop.coverUrl}" class="img-fluid rounded border"
								style="width: 100%; max-height: 220px; object-fit: cover;">
						</c:when>
						<c:otherwise>
							<img src="https://via.placeholder.com/800x220?text=Cover"
								class="img-fluid rounded border"
								style="width: 100%; max-height: 220px; object-fit: cover;">
						</c:otherwise>
					</c:choose>
				</div>
			</div>
		</div>

		<div class="d-flex gap-2">
			<button type="submit" class="btn btn-primary">Lưu thay đổi</button>
			<a href="${ctx}/vendor/dashboard" class="btn btn-secondary">Hủy</a>
		</div>
	</form>

	<c:if test="${not empty shop}">
	
		<div class="card kpi-card mt-4 shadow-sm">
			<div class="card-body">
				<h5 class="card-title">Thêm nhân viên nhanh</h5>
				<form method="post" action="${ctx}/vendor/staffs/add" class="row g-2">
					<div class="col-md-6">
						<input type="email" name="email" class="form-control"
							placeholder="Nhập email user để thêm vào shop" required />
					</div>
					<div class="col-auto">
						<button class="btn btn-success">Thêm</button>
					</div>
				</form>
				<small class="text-muted">
					Chỉ chấp nhận tài khoản USER đã kích hoạt email. 
					Xem danh sách chi tiết tại 
					<a href="${ctx}/vendor/staffs">/vendor/staffs</a>.
				</small>
			</div>
		</div>
	</c:if>

</div>