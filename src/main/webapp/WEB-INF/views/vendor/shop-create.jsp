<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
	href="${pageContext.request.contextPath}/assets/css/web2.css">

<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
</head>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%-- ĐÃ THÊM: class "main-vendor-shop-create" để "ăn" nền pastel --%>
<%-- ... (Phần đầu file giữ nguyên) ... --%>

<main class="container py-4 main-vendor-shop-create">
  
  <h2 class="mb-3 gradient-text" style="font-weight: 700;">Tạo cửa hàng</h2>

  <div class="card shadow-sm form-card-pink">
    <div class="card-body">
      <h5 class="card-title mb-3" style="font-weight: 600;">Thông tin cơ bản</h5>
      
      <%-- 
        ✅ SỬA LỖI: Thêm enctype="multipart/form-data" 
        để form có thể gửi file lên servlet
      --%>
      <form method="post" enctype="multipart/form-data">
        <div class="mb-3">
          <label class="form-label">Tên cửa hàng</label>
          <input name="shopName" class="form-control" required>
        </div>
        <div class="mb-3">
          <label class="form-label">Mô tả</label>
          <textarea name="description" class="form-control" rows="3"></textarea>
        </div>

        <%-- 
          ✅ BỔ SUNG: Thêm 2 trường input file 
          mà servlet đang chờ xử lý
        --%>
        <div class="mb-3">
          <label for="logo" class="form-label">Logo (ảnh vuông)</label>
          <input class="form-control" type="file" id="logo" name="logo" accept="image/*">
        </div>
        <div class="mb-3">
          <label for="cover" class="form-label">Ảnh bìa (ảnh ngang)</label>
          <input class="form-control" type="file" id="cover" name="cover" accept="image/*">
        </div>
        
        <button class="btn btn-primary">Tạo shop</button>
      </form>
    </div>
  </div>
</main>