<%-- filepath: src/main/webapp/WEB-INF/views/products/list.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
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

<%-- Lấy các tham số filter thuộc tính và tỉnh/thành từ request scope (Servlet đặt) hoặc param --%>
<c:set var="brand" value="${empty brand ? param.brand : brand}" />
<c:set var="gender" value="${empty gender ? param.gender : gender}" />
<c:set var="style" value="${empty style ? param.style : style}" />
<c:set var="province"
	value="${empty province ? param.province : province}" />

<div class="product-list-simple">

	<%-- HERO SHOP (hiện khi lọc theo 1 shop cụ thể) --%>
	<c:if test="${not empty shop}">
		<c:set var="coverRaw"
			value="${empty shop.coverUrl ? '' : shop.coverUrl}" />
		<c:set var="logoRaw"
			value="${empty shop.logoUrl  ? '' : shop.logoUrl }" />

		<%-- resolve cover --%>
		<c:choose>
			<c:when
				test="${fn:startsWith(coverRaw,'http://') or fn:startsWith(coverRaw,'https://')}">
				<c:set var="resolvedCover" value="${coverRaw}" />
			</c:when>
			<c:when test="${fn:startsWith(coverRaw,'/assets/')}">
				<c:set var="resolvedCover" value="${ctx.concat(coverRaw)}" />
			</c:when>
			<c:when test="${fn:startsWith(coverRaw,'/')}">
				<c:set var="resolvedCover" value="${coverRaw}" />
			</c:when>
			<c:otherwise>
				<c:set var="resolvedCover"
					value="${ctx.concat('/assets/img/').concat(coverRaw)}" />
			</c:otherwise>
		</c:choose>

		<%-- resolve logo --%>
		<c:choose>
			<c:when
				test="${fn:startsWith(logoRaw,'http://') or fn:startsWith(logoRaw,'https://')}">
				<c:set var="resolvedLogo" value="${logoRaw}" />
			</c:when>
			<c:when test="${fn:startsWith(logoRaw,'/assets/')}">
				<c:set var="resolvedLogo" value="${ctx.concat(logoRaw)}" />
			</c:when>
			<c:when test="${fn:startsWith(logoRaw,'/')}">
				<c:set var="resolvedLogo" value="${logoRaw}" />
			</c:when>
			<c:otherwise>
				<c:set var="resolvedLogo"
					value="${ctx.concat('/assets/img/').concat(logoRaw)}" />
			</c:otherwise>
		</c:choose>

		<style>
.shop-header {
	position: relative;
	border-radius: .75rem;
	overflow: hidden;
	background: #f8f9fa
}

.shop-cover {
	width: 100%;
	aspect-ratio: 16/5;
	object-fit: cover;
	opacity: .9;
	display: block
}

.shop-header-content {
	position: absolute;
	inset: 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	background: linear-gradient(to bottom, rgba(0, 0, 0, .2),
		rgba(0, 0, 0, .45));
	text-align: center;
	padding: 1rem
}

.shop-logo {
	width: 88px;
	height: 88px;
	border-radius: 50%;
	object-fit: cover;
	border: 3px solid #fff;
	background: #fff;
	box-shadow: 0 2px 10px rgba(0, 0, 0, .25)
}

.shop-title {
	margin-top: .75rem;
	color: #fff;
	letter-spacing: .08em;
	text-shadow: 0 2px 8px rgba(0, 0, 0, .35)
}
</style>

		<div class="shop-header mb-4">
			<img class="shop-cover"
				src="${empty resolvedCover ? (ctx.concat('/assets/img/placeholder.png')) : resolvedCover}"
				alt="${shop.shopName}"
				onerror="this.onerror=null;this.src='${ctx}/assets/img/placeholder.png';">
			<div class="shop-header-content">
				<img class="shop-logo"
					src="${empty resolvedLogo ? (ctx.concat('/assets/img/placeholder.png')) : resolvedLogo}"
					alt="${shop.shopName}"
					onerror="this.onerror=null;this.src='${ctx}/assets/img/placeholder.png';">
				<h1 class="h4 fw-bold shop-title">WELCOME SHOP ${shop.shopName}</h1>
			</div>
		</div>
	</c:if>


	

	<%-- ĐÃ THÊM: class "filter-card-simple" (thay cho "lỏ") --%>
	<form class="row g-2 mb-3 filter-card-simple" method="get" action=""
		id="filterForm">
		<div class="col-12 col-md-4">
			<input class="form-control" name="q" value="${param.q}"
				placeholder="Tìm kiếm...">
		</div>

		<div class="col-6 col-md-3">
			<input class="form-control" name="shopQ" list="shopList"
				value="${shopQ}" placeholder="Shop...">
			<datalist id="shopList">
				<c:forEach var="s" items="${shops}">
					<option value="${s.name}" data-id="${s.id}"></option>
				</c:forEach>
			</datalist>
			<input type="hidden" name="shopId" id="shopIdInput"
				value="${param.shopId}">
		</div>

		<div class="col-6 col-md-3">
			<select class="form-select" name="catId">
				<option value="">Danh mục</option>
				<c:forEach var="c" items="${categories}">
					<option value="${c.categoryId}"
						${c.categoryId==param.catId?'selected':''}>${c.categoryName}</option>
				</c:forEach>
			</select>
		</div>

		<%-- Gộp Giá từ + Giá đến thành một nhóm nằm cùng hàng --%>
		<div class="col-12 col-md-4">
			<div class="input-group">
				<span class="input-group-text">Giá</span> <input
					class="form-control" type="number" min="0" name="minPrice"
					value="${param.minPrice}" placeholder="Từ"> <span
					class="input-group-text">→</span> <input class="form-control"
					type="number" min="0" name="maxPrice" value="${param.maxPrice}"
					placeholder="Đến">
			</div>
		</div>

		<div class="col-6 col-md-2">
			<select class="form-select" name="minRating">
				<option value="">Đánh giá</option>
				<c:forEach var="r" begin="1" end="5">
					<option value="${r}" ${param.minRating==r?'selected':''}>≥
						${r}★</option>
				</c:forEach>
			</select>
		</div>

		<div class="col-6 col-md-2">
			<select class="form-select" name="sort">
				<option value="">Sắp xếp</option>
				<option value="new_desc" ${param.sort=='new_desc' ? 'selected' : ''}>Mới
					nhất</option>
				<option value="price_asc"
					${param.sort=='price_asc' ? 'selected' : ''}>Giá ↑</option>
				<option value="price_desc"
					${param.sort=='price_desc' ? 'selected' : ''}>Giá ↓</option>
				<option value="rating_desc"
					${param.sort=='rating_desc' ? 'selected' : ''}>Đánh giá ↓</option>
			</select>
		</div>

		<div class="col-6 col-md-2">
			<select class="form-select" name="size">
				<c:set var="currSize"
					value="${empty param.size ? (empty page.size ? 12 : page.size) : param.size}" />
				<option value="12" ${currSize==12?'selected':''}>12 / trang</option>
				<option value="24" ${currSize==24?'selected':''}>24 / trang</option>
				<option value="36" ${currSize==36?'selected':''}>36 / trang</option>
			</select>
		</div>

		<%-- Filter thuộc tính và tỉnh/thành --%>
		<div class="col-6 col-md-2">
			<input class="form-control" name="brand" value="${brand}"
				placeholder="Brand">
		</div>
		<div class="col-6 col-md-2">
			<select class="form-select" name="gender">
				<option value="">Gender</option>
				<option value="male" ${gender=='male'?'selected':''}>Male</option>
				<option value="female" ${gender=='female'?'selected':''}>Female</option>
				<option value="unisex" ${gender=='unisex'?'selected':''}>Unisex</option>
			</select>
		</div>
		<div class="col-6 col-md-2">
			<input class="form-control" name="style" value="${style}"
				placeholder="Style">
		</div>
		<div class="col-6 col-md-2">
			<input class="form-control" name="province" value="${province}"
				placeholder="Tỉnh/Thành">
		</div>

		<div class="col-12 col-md-auto d-flex gap-2">
			<%-- ĐÃ THÊM: class "btn-gradient" (từ v13) --%>
			<button class="btn btn-gradient">Lọc</button>
			<a class="btn btn-outline-secondary"
				href="<c:url value='/products'/>">Xóa lọc</a>
		</div>
	</form>

	<c:set var="items" value="${empty page ? null : page.items}" />

	<c:choose>
		<c:when test="${not empty items}">
			<div class="row row-cols-2 row-cols-md-4 g-3">
				<c:forEach var="p" items="${items}">
					<div class="col">
						<%-- 
						  =============================================================
						  (FIX V21) TỈ LỆ CẢ KHUNG SẢN PHẨM (DÀI 4 RỘNG 3)
						  ĐÃ THÊM: class "product-card-simple"
						  =============================================================
						--%>
						<div class="card h-100 product-card-simple">

							<a href="${ctx}/product/${p.id}"> <%-- Resolve cover image robustly --%>
								<c:set var="coverRaw"
									value="${empty p.coverUrl ? '' : p.coverUrl}" /> <%-- Sửa nhầm thư mục /assset -> /assets --%>
								<c:set var="coverFixed"
									value="${fn:replace(coverRaw, '/assset/', '/assets/')}" /> <c:choose>
									<%-- URL tuyệt đối http/https: giữ nguyên --%>
									<c:when
										test="${fn:startsWith(coverFixed,'http://') or fn:startsWith(coverFixed,'https://')}">
										<c:set var="resolvedCover" value="${coverFixed}" />
									</c:when>

									<%-- Đường dẫn bắt đầu bằng /assets/... : tự ghép ctx --%>
									<c:when test="${fn:startsWith(coverFixed,'/assets/')}">
										<c:set var="resolvedCover" value="${ctx.concat(coverFixed)}" />
									</c:when>

									<%-- Đường dẫn bắt đầu bằng / (nhưng không phải /assets): dùng nguyên như đã lưu --%>
									<c:when test="${fn:startsWith(coverFixed,'/')}">
										<c:set var="resolvedCover" value="${coverFixed}" />
									</c:when>

									<%-- Chỉ là tên file: trỏ về /assets/products/ --%>
									<c:otherwise>
										<c:set var="resolvedCover"
											value="${ctx.concat('/assets/products/').concat(coverFixed)}" />
									</c:otherwise>
								</c:choose> <%-- (FIX V21) ĐÃ XÓA style="aspect-ratio: 3 / 4;" vì sẽ set cho cả card --%>
								<img class="card-img-top"
								src="${empty resolvedCover ? (ctx.concat('/assets/img/placeholder.png')) : resolvedCover}"
								alt="${p.name}"
								onerror="this.onerror=null;this.src='${ctx}/assets/img/placeholder.png';">
							</a>

							<div class="card-body p-2 d-flex flex-column">
								<div class="small text-muted text-truncate"
									title="${p.categoryName}">${p.categoryName}</div>
								<div class="fw-semibold text-truncate mb-1" title="${p.name}">${p.name}</div>

								<div class="d-flex flex-wrap gap-1 small mb-1">
									<%-- CSS V19 sẽ "tóm" mấy cái badge này --%>
									<c:if test="${not empty p.brand}">
										<span class="badge bg-light border text-secondary">Brand:
											${p.brand}</span>
									</c:if>
									<c:if test="${not empty p.gender}">
										<span class="badge bg-light border text-secondary">Gender:
											${p.gender}</span>
									</c:if>
									<c:if test="${not empty p.style}">
										<span class="badge bg-light border text-secondary">Style:
											${p.style}</span>
									</c:if>
								</div>

								<div class="small text-muted mb-1">
									Tồn: <span class="fw-semibold">${empty p.stockTotal ? 0 : p.stockTotal}</span>
								</div>

								<c:if test="${not empty p.shopName}">
									<div
										class="d-flex align-items-center gap-2 small text-truncate mb-1"
										title="${p.shopName}">
										<c:if test="${not empty p.shopLogoUrl}">
											<img src="${ctx}${p.shopLogoUrl}" alt="${p.shopName}"
												width="18" height="18" class="rounded"
												onerror="this.onerror=null;this.src='${ctx}/assets/img/placeholder.png';">
										</c:if>
										<%-- ĐÃ THÊM: class "badge-shop-simple" --%>
										<a
											class="badge text-secondary text-decoration-none badge-shop-simple"
											href="<c:url value='/products'>
	                              <c:param name='q'         value='${param.q}'/>
	                              <c:param name='shopQ'     value='${p.shopName}'/>
	                              <c:param name='catId'     value='${param.catId}'/>
	                              <c:param name='minPrice'  value='${param.minPrice}'/>
	                              <c:param name='maxPrice'  value='${param.maxPrice}'/>
	                              <c:param name='minRating' value='${param.minRating}'/>
	                              <c:param name='sort'      value='${param.sort}'/>
	                              <c:param name='size'      value='${empty param.size ? (empty page.size ? 12 : page.size) : param.size}'/>
	                              <c:param name='page'      value='1'/>
	                              <c:param name='shopId'    value='${p.shopId}'/>
	                              <c:param name="brand"     value="${brand}"/>
	                              <c:param name="gender"    value="${gender}"/>
	                              <c:param name="style"     value="${style}"/>
	                              <c:param name="province"  value="${province}"/>
	                           </c:url>">
											${p.shopName} </a>
									</div>
								</c:if>

								<div
									class="d-flex justify-content-between align-items-center mb-1">
									<div class="fw-bold">
										<c:choose>
											<c:when test="${not empty p.discountPrice}">
												<%-- ĐÃ THÊM: class "price-sale" --%>
												<span class="text-danger price-sale"> <fmt:formatNumber
														value="${p.discountPrice}" type="number"
														groupingUsed="true" /> ₫
												</span>
												<c:if test="${not empty p.price}">
													<small class="text-muted text-decoration-line-through ms-2">
														<fmt:formatNumber value="${p.price}" type="number"
															groupingUsed="true" /> ₫
													</small>
												</c:if>
											</c:when>
											<c:when test="${not empty p.price}">
												<%-- ĐÃ THÊM: class "price-normal" --%>
												<span class="price-normal"> <fmt:formatNumber
														value="${p.price}" type="number" groupingUsed="true" /> ₫
												</span>
											</c:when>
											<c:otherwise>—</c:otherwise>
										</c:choose>
									</div>

									<span class="small"> <c:set var="rAvg"
											value="${empty p.ratingAvg ? 0 : p.ratingAvg}" /> <c:forEach
											var="i" begin="1" end="5">
											<%-- ĐÃ THÊM: Dùng icon "Pro" (từ v13) --%>
											<c:choose>
												<c:when test="${i <= rAvg}">
													<span class="text-warning"><i
														class="bi bi-star-fill"></i></span>
												</c:when>
												<c:otherwise>
													<span class="text-secondary opacity-50"><i
														class="bi bi-star"></i></span>
												</c:otherwise>
											</c:choose>
										</c:forEach>
									</span>
								</div>

								<%-- ĐÃ THÊM: class "btn-buy-simple" (thay cho outline) --%>
								<a href="${ctx}/product/${p.id}"
									class="btn btn-buy-simple btn-sm mt-auto">Xem chi tiết</a>
							</div>
						</div>
					</div>
				</c:forEach>
			</div>

			<c:if test="${page.totalPages > 1}">
				<%-- ĐÃ THÊM: class "pagination-simple" --%>
				<nav class="mt-3 pagination-simple">
					<ul class="pagination justify-content-center">

						<%-- PREV --%>
						<c:url var="prevUrl" value="/products">
							<c:param name="q" value="${param.q}" />
							<c:param name="shopQ" value="${shopQ}" />
							<c:param name="catId" value="${param.catId}" />
							<c:param name="minPrice" value="${param.minPrice}" />
							<c:param name="maxPrice" value="${param.maxPrice}" />
							<c:param name="minRating" value="${param.minRating}" />
							<c:param name="sort" value="${param.sort}" />
							<c:param name="size"
								value="${empty param.size ? (empty page.size ? 12 : page.size) : param.size}" />
							<c:param name="page" value="${page.number-1}" />
							<c:param name="shopId" value="${param.shopId}" />
							<c:param name="brand" value="${brand}" />
							<c:param name="gender" value="${gender}" />
							<c:param name="style" value="${style}" />
							<c:param name="province" value="${province}" />
						</c:url>
						<li class="page-item ${page.hasPrev ? '' : 'disabled'}"><a
							class="page-link" href="${prevUrl}">«</a></li>

						<%-- NUMBERS --%>
						<c:forEach var="i" begin="1" end="${page.totalPages}">
							<c:url var="iUrl" value="/products">
								<c:param name="q" value="${param.q}" />
								<c:param name="shopQ" value="${shopQ}" />
								<c:param name="catId" value="${param.catId}" />
								<c:param name="minPrice" value="${param.minPrice}" />
								<c:param name="maxPrice" value="${param.maxPrice}" />
								<c:param name="minRating" value="${param.minRating}" />
								<c:param name="sort" value="${param.sort}" />
								<c:param name="size"
									value="${empty param.size ? (empty page.size ? 12 : page.size) : param.size}" />
								<c:param name="page" value="${i}" />
								<c:param name="shopId" value="${param.shopId}" />
								<c:param name="brand" value="${brand}" />
								<c:param name="gender" value="${gender}" />
								<c:param name="style" value="${style}" />
								<c:param name="province" value="${province}" />
							</c:url>
							<li class="page-item ${i==page.number?'active':''}"><a
								class="page-link" href="${iUrl}">${i}</a></li>
						</c:forEach>

						<%-- NEXT --%>
						<c:url var="nextUrl" value="/products">
							<c:param name="q" value="${param.q}" />
							<c:param name="shopQ" value="${shopQ}" />
							<c:param name="catId" value="${param.catId}" />
							<c:param name="minPrice" value="${param.minPrice}" />
							<c:param name="maxPrice" value="${param.maxPrice}" />
							<c:param name="minRating" value="${param.minRating}" />
							<c:param name="sort" value="${param.sort}" />
							<c:param name="size"
								value="${empty param.size ? (empty page.size ? 12 : page.size) : param.size}" />
							<c:param name="page" value="${page.number+1}" />
							<c:param name="shopId" value="${param.shopId}" />
							<c:param name="brand" value="${brand}" />
							<c:param name="gender" value="${gender}" />
							<c:param name="style" value="${style}" />
							<c:param name="province" value="${province}" />
						</c:url>
						<li class="page-item ${page.hasNext ? '' : 'disabled'}"><a
							class="page-link" href="${nextUrl}">»</a></li>

					</ul>
				</nav>

			</c:if>
		</c:when>

		<c:otherwise>
			<%-- ĐÃ THÊM: class "empty-results-card-simple" --%>
			<div class="text-center text-muted py-5 empty-results-card-simple">Chưa
				có sản phẩm để hiển thị.</div>
		</c:otherwise>
	</c:choose>
</div>
<%-- End .product-list-simple --%>

<script>
	(function() {
		var form = document.getElementById('filterForm');
		var input = document.querySelector('input[name="shopQ"]');
		var hidden = document.getElementById('shopIdInput');
		var list = document.getElementById('shopList');
		if (!form || !input || !hidden || !list)
			return;

		function stripVN(s) {
			return (s || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '');
		}
		function norm(s) {
			return stripVN(s).trim().toLowerCase().replace(/\s+/g, ' ');
		}

		function sync() {
			var nv = norm(input.value);
			var id = '';
			var opts = list.querySelectorAll('option');

			if (nv) {
				for (var i = 0; i < opts.length; i++) {
					if (norm(opts[i].value) === nv) {
						id = opts[i].dataset.id || '';
						break;
					}
				}
				if (!id) {
					var candidates = [];
					for (var j = 0; j < opts.length; j++) {
						if (norm(opts[j].value).indexOf(nv) === 0)
							candidates.push(opts[j]);
					}
					if (candidates.length === 1)
						id = candidates[0].dataset.id || '';
				}
			}
			hidden.value = id;
		}

		input.addEventListener('input', function() {
			if (!input.value)
				hidden.value = '';
			else
				sync();
		});
		input.addEventListener('change', sync);
		input.addEventListener('blur', sync);
		input.addEventListener('keydown', function(e) {
			if (e.key === 'Enter')
				sync();
		});
		form.addEventListener('submit', function() {
			sync();
		}, true);
		sync();
	})();
</script>