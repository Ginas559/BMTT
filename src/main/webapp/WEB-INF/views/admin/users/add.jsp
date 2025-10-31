<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>Thêm người dùng</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
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
					<i class="fas fa-user-plus me-2"></i>Thêm người dùng mới
				</h3>

				<form action="${pageContext.request.contextPath}/admin/users/add" 
					  method="post" enctype="multipart/form-data">

					<!-- Họ -->
					<div class="mb-3">
						<label for="firstname" class="form-label">Họ:</label>
						<input type="text" class="form-control" id="firstname" name="firstname"
							   placeholder="Nhập họ..." required maxlength="32">
					</div>

					<!-- Tên -->
					<div class="mb-3">
						<label for="lastname" class="form-label">Tên:</label>
						<input type="text" class="form-control" id="lastname" name="lastname"
							   placeholder="Nhập tên..." required maxlength="32">
					</div>

					<!-- Email -->
					<div class="mb-3">
						<label for="email" class="form-label">Email:</label>
						<input type="email" class="form-control" id="email" name="email"
							   placeholder="Nhập email..." required maxlength="120">
					</div>

					<!-- Số điện thoại -->
					<div class="mb-3">
						<label for="phone" class="form-label">Số điện thoại:</label>
						<input type="text" class="form-control" id="phone" name="phone"
							   placeholder="Nhập số điện thoại..." required maxlength="15">
					</div>

					<!-- CCCD -->
					<div class="mb-3">
						<label for="idCard" class="form-label">CCCD/CMND:</label>
						<input type="text" class="form-control" id="idCard" name="idCard"
							   placeholder="Nhập số CCCD (nếu có)" maxlength="20">
					</div>

					<!-- Mật khẩu -->
					<div class="mb-3">
						<label for="password" class="form-label">Mật khẩu:</label>
						<input type="password" class="form-control" id="password" name="password"
							   placeholder="Nhập mật khẩu..." required>
					</div>

					<!-- Vai trò -->
					<div class="mb-3">
						<label for="role" class="form-label">Vai trò:</label>
						<select class="form-select" id="role" name="role" required>
							<option value="">-- Chọn vai trò --</option>
							<option value="USER">USER</option>
							<option value="VENDOR">VENDOR</option>
							<option value="SHIPPER">SHIPPER</option>
							<option value="ADMIN">ADMIN</option>
						</select>
					</div>

					<!-- Avatar -->
					<div class="mb-3">
						<label for="avatarFile" class="form-label">Ảnh đại diện:</label>
						<input type="file" class="form-control" id="avatarFile" name="avatarFile"
							   accept="image/*" onchange="previewImage(event)">
					</div>

					<div class="text-center mb-3">
						<img id="preview"
							src="${pageContext.request.contextPath}/assets/img/no-avatar.png"
							alt="Preview" class="img-thumbnail mt-2"
							style="max-width: 150px; max-height: 150px;">
					</div>

					<!-- Trạng thái -->
					<div class="mb-3">
						<label for="isBanned" class="form-label">Trạng thái tài khoản:</label>
						<select class="form-select" id="isBanned" name="isBanned">
							<option value="false" selected>Hoạt động</option>
							<option value="true">Bị cấm</option>
						</select>
					</div>

					<!-- Buttons -->
					<div class="d-flex justify-content-between">
						<a href="${pageContext.request.contextPath}/admin/users"
							class="btn btn-outline-secondary">
							<i class="fas fa-arrow-left me-1"></i> Quay lại
						</a>
						<button type="submit" class="btn btn-primary">
							<i class="fas fa-save me-1"></i> Thêm người dùng
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
