<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
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
<body>
	<div class="floating-wings-left">
		<img src="https://res.cloudinary.com/dwzvtnip3/image/upload/v1761833448/shoes-shop/products/szovtbw9yhnitqsb19zx.png" alt="float 1" /> 
		<img src="https://res.cloudinary.com/dwzvtnip3/image/upload/v1761833312/shoes-shop/products/aocrzvupagu6ifdjmxva.png" alt="float 2" /> 
		<img src="https://tse3.mm.bing.net/th/id/OIP.fOEu5v5LvP4urmTCGM5AfgHaD2?rs=1&pid=ImgDetMain&o=7&rm=3" alt="Puma" /> <%-- 👈 Thêm ảnh Puma vào cánh trái --%>
		<img src="https://res.cloudinary.com/dwzvtnip3/image/upload/v1761833279/shoes-shop/products/e5hvop9hqlm8rnolefgx.png" alt="float 3" />
	</div>

	<div class="floating-wings-right">
		<img src="https://agiay.vn/wp-content/uploads/2023/05/dep-nam-hot-ag0275-768x768.jpg" alt="float 4" />
		<img src="https://static.nc-myus.com/images/pub/www/uploads/image/26eb4dfc4ab74dc6863e2b3ce05e01de/How_to_Shop_Puma_and_Ship_Internationally.jpg" alt="Puma shop" /> <%-- 👈 Thêm ảnh Puma khác vào cánh phải --%>
		<img src="https://w7.pngwing.com/pngs/902/804/png-transparent-cr7-hd-logo-thumbnail.png" alt="CR7 Logo" /> <%-- 👈 Thêm ảnh CR7 vào cánh phải --%>
		<img src="https://res.cloudinary.com/dwzvtnip3/image/upload/v1761833266/shoes-shop/products/cjsmdftzijebdwndluha.png" alt="float 5" />
		<%-- (Bro tự thêm link "jxzhahfnhu5sfzljb1wg.png" vào đây nhé, link đó bị lỗi) --%>
	</div>

	<nav id="mainNavbar"
		class="navbar navbar-expand-lg navbar-dark navbar-colorful mb-3 py-2 navbar-big">
		<div class="container">

			<a class="navbar-brand d-flex align-items-center"
				href="${pageContext.request.contextPath}/"> <img
				src="https://iconape.com/wp-content/png_logo_vector/hcmute-logo.png"
				alt="Logo" class="navbar-logo"> BMTT Shop
			</a>

			<button class="navbar-toggler" type="button"
				data-bs-toggle="collapse" data-bs-target="#mainNav"
				aria-controls="mainNav" aria-expanded="false"
				aria-label="Toggle navigation">
				<span class="navbar-toggler-icon"></span>
			</button>

			<div class="collapse navbar-collapse" id="mainNav">
				<ul class="navbar-nav me-auto mb-2 mb-lg-0">

					<li class="nav-item"><a class="nav-link"
						href="${pageContext.request.contextPath}/products">Sản phẩm</a></li>

					<li class="nav-item"><a class="nav-link"
						href="${pageContext.request.contextPath}/favorites">Yêu thích</a>
					</li>

					<li class="nav-item"><a class="nav-link"
						href="${pageContext.request.contextPath}/cart">Giỏ hàng</a></li>

					<li class="nav-item"><a class="nav-link"
						href="${pageContext.request.contextPath}/orders">Đơn hàng của
							tôi</a></li>

					<li class="nav-item"><a class="nav-link"
						href="${pageContext.request.contextPath}/vendors">Vendor</a></li>

					<c:if test="${sessionScope.role == 'VENDOR'}">
						<li class="nav-item dropdown"><a
							class="nav-link dropdown-toggle" href="#" role="button"
							data-bs-toggle="dropdown" aria-expanded="false"> Vendor </a>
							<ul class="dropdown-menu">
								<li><a class="dropdown-item"
									href="${pageContext.request.contextPath}/vendor/dashboard">Dashboard</a>
								</li>
								<li><a class="dropdown-item"
									href="${pageContext.request.contextPath}/vendor/products">Sản
										phẩm</a></li>
								<li><a class="dropdown-item"
									href="${pageContext.request.contextPath}/vendor/orders">Đơn
										hàng</a></li>
								<li><a class="dropdown-item"
									href="${pageContext.request.contextPath}/vendor/shop">Hồ sơ
										Shop</a></li>
								<li><a class="dropdown-item"
									href="${pageContext.request.contextPath}/vendor/statistics/view">Thống
										kê</a></li>

								<li><a class="dropdown-item"
									href="${pageContext.request.contextPath}/vendor/vouchers">🎟️
										Voucher</a></li>

							</ul></li>
					</c:if>
				</ul>

				<div class="d-flex align-items-center gap-2">
					<c:choose>
						<c:when test="${empty sessionScope.userId}">
							<a class="btn btn-sm btn-outline-secondary"
								href="${pageContext.request.contextPath}/login">Đăng nhập</a>
							<a class="btn btn-sm btn-primary"
								href="${pageContext.request.contextPath}/register">Đăng ký</a>
						</c:when>
						<c:otherwise>
							<a class="btn btn-sm btn-outline-secondary"
								href="${pageContext.request.contextPath}/user/profile"> Xin
								chào, ${sessionScope.email} </a>
							<a class="btn btn-sm btn-primary"
								href="${pageContext.request.contextPath}/user/addresses">Địa
								chỉ</a>
							<form method="post"
								action="${pageContext.request.contextPath}/logout"
								class="d-inline m-0">
								<button class="btn btn-sm btn-danger" type="submit">Đăng
									xuất</button>
							</form>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
		</div>
	</nav>

	<main class="container">
		<sitemesh:write property="body" />
	</main>

	<footer class="mt-5 footer-colorful">
		<div class="container pt-5 pb-4">
			<div class="row g-4">

				<div class="col-lg-4 col-md-6">
					<h5 class="footer-heading">BMTT Shop</h5>
					<p class="footer-text">Chuyên cung cấp các sản phẩm "màu mè",
						"hiệu ứng" với công nghệ CSS và Bootstrap mới nhất. Cam kết code
						"cháy" nhất thị trường.</p>
				</div>

				<div class="col-lg-4 col-md-6">
					<h5 class="footer-heading">Thông tin liên lạc</h5>
					<ul class="list-unstyled contact-info">
						<li><i class="bi bi-geo-alt-fill"></i> <span>123 Đường
								CSS, P. Bootstrap, Q. HTML, TP. Web</span></li>
						<li><i class="bi bi-telephone-fill"></i> <span>(028)
								38 123 456</span></li>
						<li><i class="bi bi-envelope-fill"></i> <span>support@bmttshop.local</span>
						</li>
					</ul>
				</div>

				<div class="col-lg-4 col-md-12">
					<h5 class="footer-heading">Bản đồ (Fake)</h5>
					<div class="map-responsive">
						<iframe
							src="http://googleusercontent.com/maps/google.com/0"
							width="100%" height="150" style="border: 0;" allowfullscreen=""
							loading="lazy" referrerpolicy="no-referrer-when-downgrade">
						</iframe>
					</div>
				</div>

			</div>
		</div>

		<div class="footer-bottom py-3">
			<div
				class="container small d-flex flex-wrap gap-2 justify-content-between">
				<span>© 2025 BMTTShop - Đã "độ" bởi Coding Partner</span> <span>
					<a class="text-decoration-none"
					href="${pageContext.request.contextPath}/products">Sản phẩm</a> • <a
					class="text-decoration-none" href="#">Liên hệ</a>
				</span>
			</div>
		</div>
	</footer>

	<script
		src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
		
	<script>
	  document.addEventListener('DOMContentLoaded', function() {
	    var navbar = document.getElementById('mainNavbar');
	    if (!navbar) return;
	    
	    // Khi tải trang, nó đã "to" sẵn (nhờ class .navbar-big)
	    
	    // Lắng nghe sự kiện cuộn
	    window.onscroll = function() {
	      // Nếu cuộn xuống quá 50px
	      if (document.body.scrollTop > 50 || document.documentElement.scrollTop > 50) {
	        navbar.classList.remove('navbar-big');
	      } else {
	        // Khi cuộn lên đỉnh, "phình" ra lại
	        navbar.classList.add('navbar-big');
	      }
	    };
	  });
	</script>
</body>
</html>