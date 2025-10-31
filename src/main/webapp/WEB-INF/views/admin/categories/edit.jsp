<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>Chỉnh sửa danh mục</title>
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
	rel="stylesheet">
<style>
body {
	background-color: #f8f9fa;
}

.card {
	max-width: 650px;
	margin: 40px auto;
	border-radius: 15px;
}

.card h3 {
	color: #198754;
}

img#preview {
	max-width: 200px;
	max-height: 200px;
	object-fit: cover;
	border-radius: 10px;
	box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}
</style>
</head>
<body>
	<div class="container">
		<div class="card shadow-sm p-4">
			<h3 class="text-center mb-4">Chỉnh sửa danh mục</h3>

			<form
				action="${pageContext.request.contextPath}/admin/categories/edit"
				method="post" enctype="multipart/form-data">
				<input type="hidden" name="categoryId"
					value="${category.categoryId}">

				<!-- Tên danh mục -->
				<div class="mb-3">
					<label for="categoryName" class="form-label">Tên danh mục:</label>
					<input type="text" class="form-control" id="categoryName"
						name="categoryName" value="${category.categoryName}" required>
				</div>

				<!-- Mô tả -->
				<div class="mb-3">
					<label for="description" class="form-label">Mô tả:</label>
					<textarea class="form-control" id="description" name="description"
						rows="3">${category.description}</textarea>
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


				<!-- Trạng thái -->
				<div class="mb-3 form-check">
					<input type="checkbox" class="form-check-input" id="isBanned"
						name="isBanned" <c:if test="${category.isBanned}">checked</c:if>>
					<label class="form-check-label" for="isBanned">Bị cấm</label>
				</div>

				<!-- Nút -->
				<div class="text-center">
					<button type="submit" class="btn btn-success px-4 me-2">Cập
						nhật</button>
					<a href="${pageContext.request.contextPath}/admin/categories"
						class="btn btn-secondary px-4">Quay lại</a>
				</div>
			</form>
		</div>
	</div>

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
