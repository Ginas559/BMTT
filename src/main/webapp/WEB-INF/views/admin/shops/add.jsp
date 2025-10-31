<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>Thêm cửa hàng</title>
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
	rel="stylesheet">
<style>
body {
	background-color: #f8f9fa;
}

.card {
	max-width: 700px;
	margin: 40px auto;
	border-radius: 12px;
}

h3 {
	font-weight: 600;
}

label {
	font-weight: 500;
}

img#preview {
	border: 1px solid #dee2e6;
	border-radius: 8px;
	object-fit: cover;
}
</style>
</head>
<body>
	<div class="container">
		<div class="card shadow-sm">
			<div class="card-body">
				<h3 class="text-center text-primary mb-4">
					<i class="fas fa-store me-2"></i>Thêm cửa hàng mới
				</h3>

				<form action="${pageContext.request.contextPath}/admin/shops/add"
					method="post" enctype="multipart/form-data">

					<!-- Shop Name -->
					<div class="mb-3">
						<label for="shopName" class="form-label">Tên cửa hàng:</label>
						<input type="text" class="form-control" id="shopName" name="shopName"
							placeholder="Nhập tên cửa hàng..." required maxlength="150">
					</div>

					<!-- Description -->
					<div class="mb-3">
						<label for="description" class="form-label">Mô tả:</label>
						<textarea class="form-control" id="description" name="description"
							rows="3" placeholder="Nhập mô tả ngắn cho cửa hàng..."></textarea>
					</div>

					<!-- Vendor -->
					<div class="mb-3">
						<label for="vendorId" class="form-label">Chủ cửa hàng (Vendor):</label>
						<select class="form-select" id="vendorId" name="vendorId" required>
							<option value="">-- Chọn chủ cửa hàng --</option>
							<c:forEach var="vendor" items="${vendors}">
								<option value="${vendor.id}">
									${vendor.firstname} ${vendor.lastname} (${vendor.email})
								</option>
							</c:forEach>
						</select>
					</div>

					<!-- Logo -->
					<div class="mb-3">
						<label for="logoFile" class="form-label">Logo cửa hàng:</label>
						<input type="file" class="form-control" id="logoFile" name="logoFile"
							accept="image/*" onchange="previewImage(event)">
					</div>

					<div class="text-center mb-3">
						<img id="preview"
							src="${pageContext.request.contextPath}/assets/img/no-image.png"
							alt="Preview" class="img-thumbnail mt-2"
							style="max-width: 200px; max-height: 200px;">
					</div>

					<!-- Status -->
					<div class="mb-3">
						<label for="status" class="form-label">Trạng thái:</label>
						<select class="form-select" id="status" name="status">
							<option value="ACTIVE">Hoạt động</option>
							<option value="PENDING">Chờ duyệt</option>
							<option value="BANNED">Bị cấm</option>
						</select>
					</div>

					<!-- Buttons -->
					<div class="d-flex justify-content-between">
						<a href="${pageContext.request.contextPath}/admin/shops"
							class="btn btn-outline-secondary">
							<i class="fas fa-arrow-left me-1"></i> Quay lại
						</a>
						<button type="submit" class="btn btn-primary">
							<i class="fas fa-save me-1"></i> Thêm cửa hàng
						</button>
					</div>
				</form>
			</div>
		</div>
	</div>

	<!-- Font Awesome & Preview Script -->
	<script src="https://kit.fontawesome.com/a076d05399.js" crossorigin="anonymous"></script>
	<script>
		function previewImage(event) {
			const file = event.target.files[0];
			const preview = document.getElementById('preview');
			if (file) {
				preview.src = URL.createObjectURL(file);
			}
		}
	</script>
</body>
</html>
