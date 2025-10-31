<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>Thêm danh mục</title>
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
	rel="stylesheet">
<style>
body {
	background-color: #f8f9fa;
}

.card {
	max-width: 600px;
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
}
</style>
</head>
<body>
	<div class="container">
		<div class="card shadow-sm">
			<div class="card-body">
				<h3 class="text-center text-primary mb-4">
					<i class="fas fa-plus-circle me-2"></i>Thêm danh mục
				</h3>

				<form
					action="${pageContext.request.contextPath}/admin/categories/add"
					method="post" enctype="multipart/form-data">

					<div class="mb-3">
						<label for="categoryName" class="form-label">Tên danh mục:</label>
						<input type="text" class="form-control" id="categoryName"
							name="categoryName" placeholder="Nhập tên danh mục..." required>
					</div>

					<div class="mb-3">
						<label for="description" class="form-label">Mô tả:</label>
						<textarea class="form-control" id="description" name="description"
							rows="3" placeholder="Nhập mô tả ngắn cho danh mục..."></textarea>
					</div>

					<div class="mb-3">
						<label for="image" class="form-label">Ảnh danh mục:</label> <input
							type="file" class="form-control" id="image" name="image"
							accept="image/*" onchange="previewImage(event)">
					</div>

					<div class="text-center mb-3">
						<img id="preview"
							src="${category != null && category.image != null 
             ? pageContext.request.contextPath.concat('/image?type=categories&fname=').concat(category.image)
             : pageContext.request.contextPath.concat('/assets/img/no-image.png')}"
							alt="Preview" class="img-thumbnail mt-2"
							style="max-width: 200px; max-height: 200px; object-fit: cover;">
					</div>


					<div class="form-check mb-3">
						<input type="checkbox" class="form-check-input" id="isBanned"
							name="isBanned"> <label class="form-check-label"
							for="isBanned">Bị cấm</label>
					</div>

					<div class="d-flex justify-content-between">
						<a href="${pageContext.request.contextPath}/admin/categories"
							class="btn btn-outline-secondary"> <i
							class="fas fa-arrow-left me-1"></i> Quay lại
						</a>
						<button type="submit" class="btn btn-primary">
							<i class="fas fa-save me-1"></i> Thêm danh mục
						</button>
					</div>
				</form>
			</div>
		</div>
	</div>

	<!-- Font Awesome & Script preview -->
	<script src="https://kit.fontawesome.com/a076d05399.js"
		crossorigin="anonymous"></script>
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
