<%-- filepath: src/main/webapp/WEB-INF/views/vendor/products.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<fmt:setLocale value="vi_VN" />

<c:choose>
	<c:when test="${empty p}">
		<c:set var="actionPath" value="/vendor/products/add" />
	</c:when>
	<c:otherwise>
		<c:set var="actionPath" value="/vendor/products/update" />
	</c:otherwise>
</c:choose>

<div class="main-products py-4">
	<div class="d-flex align-items-center justify-content-between mb-3">
		<h2 class="mb-0 gradient-text">Quản lý sản phẩm</h2>
		<c:if test="${not empty shop}">
			<span class="text-dark fw-bold">Shop: <strong><c:out
						value="${shop.shopName}" /></strong></span>
		</c:if>
	</div>

	<c:if test="${not empty errors}">
		<div class="alert alert-danger glass-alert" role="alert">
			<ul class="mb-0">
				<c:forEach var="e" items="${errors}">
					<li><c:out value="${e}" /></li>
				</c:forEach>
			</ul>
		</div>
	</c:if>

	<div class="card kpi-card mb-3">
		<div class="card-body">
			<h5 class="card-title">Thêm / Cập nhật sản phẩm</h5>

			<form method="post" enctype="multipart/form-data"
				action="<c:url value='${actionPath}'/>" class="row g-3">
				<c:if test="${not empty p}">
					<input type="hidden" name="productId" value="${p.productId}" />
				</c:if>

				<div class="col-md-4">
					<label class="form-label" for="prodName">Tên</label> <input
						id="prodName" name="name" class="form-control" required
						value="${not empty p ? p.productName : ''}" />
				</div>

				<div class="col-md-2">
					<label class="form-label" for="prodPrice">Giá</label> <input
						id="prodPrice" name="price" type="number" step="0.01" min="0.01"
						class="form-control" required
						value="${not empty p ? p.price : ''}" />
				</div>

				<div class="col-md-2">
					<label class="form-label" for="prodStock">Tồn</label>
					<c:choose>
						<c:when test="${empty p}">
							<input id="prodStock" name="stock" type="number"
								class="form-control" value="0" readonly />
						</c:when>
						<c:otherwise>
							<input id="prodStock" name="stock" type="number" min="0"
								class="form-control" required value="${p.stock}" />
						</c:otherwise>
					</c:choose>
				</div>

				<div class="col-md-4">
					<label class="form-label" for="prodCat">Danh mục</label> <select
						id="prodCat" name="categoryId" class="form-select" required>
						<option value="">-- Chọn danh mục --</option>
						<c:forEach var="c" items="${categories}">
							<option value="${c.categoryId}"
								${not empty p && p.category != null && p.category.categoryId == c.categoryId ? 'selected' : ''}>
								<c:out value="${c.categoryName}" />
							</option>
						</c:forEach>
					</select>
				</div>

				<div class="col-md-6">
					<label class="form-label" for="prodImg">Ảnh sản phẩm</label> <input
						id="prodImg" type="file" name="image"
						accept=".jpg,.jpeg,.png,.webp,image/*" class="form-control" /> <small
						class="text-muted d-block mt-1">Chọn 1 ảnh (≤ 2MB) — sẽ
						lấy làm thumbnail.</small>

					<c:if test="${not empty thumbEditing}">
						<div class="mt-2">
							<img src="${thumbEditing}" width="120" class="rounded shadow-sm"
								alt="Ảnh hiện tại" />
						</div>
					</c:if>
				</div>

				<c:if test="${not empty p}">
					<div class="col-md-3">
						<label class="form-label" for="prodStatus">Trạng thái</label> <select
							id="prodStatus" name="status" class="form-select">
							<option value="ACTIVE"
								${p.status == 'ACTIVE'   ? 'selected' : ''}>ACTIVE</option>
							<option value="INACTIVE"
								${p.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
						</select>
					</div>
				</c:if>

				<div class="col-12 d-flex justify-content-end">
					<button class="btn btn-primary" type="submit">${empty p ? 'Thêm' : 'Lưu'}</button>
				</div>
			</form>
		</div>
	</div>

	<div class="card kpi-card mb-4">
		<div class="card-body">
			<h5 class="card-title">Tìm kiếm & Lọc</h5>

			<form method="get" action="<c:url value='/vendor/products'/>"
				class="row g-2" id="vendorFilterForm">
				<div class="col-12 col-md-4">
					<input class="form-control" name="q" placeholder="Tìm theo tên..."
						value="${param.q != null ? param.q : (q != null ? q : '')}" />
				</div>

				<div class="col-6 col-md-3">
					<select class="form-select" name="categoryId">
						<option value="">-- Tất cả danh mục --</option>
						<c:forEach var="c" items="${categories}">
							<option value="${c.categoryId}"
								${ (param.categoryId != null ? param.categoryId : categoryId) == c.categoryId ? 'selected' : ''}>
								<c:out value="${c.categoryName}" />
							</option>
						</c:forEach>
					</select>
				</div>

				<div class="col-6 col-md-2">
					<select class="form-select" name="status">
						<c:set var="stVal"
							value="${param.status != null ? param.status : status}" />
						<option value="" ${empty stVal ? 'selected' : ''}>-- Tất
							cả --</option>
						<option value="ACTIVE" ${stVal == 'ACTIVE'   ? 'selected' : ''}>ACTIVE</option>
						<option value="INACTIVE" ${stVal == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
					</select>
				</div>

				<div class="col-6 col-md-2">
					<c:set var="sz"
						value="${param.size != null ? param.size : (size != null ? size : 10)}" />
					<select class="form-select" name="size">
						<option ${sz == 10  ? 'selected' : ''} value="10">10 /
							trang</option>
						<option ${sz == 20  ? 'selected' : ''} value="20">20 /
							trang</option>
						<option ${sz == 50  ? 'selected' : ''} value="50">50 /
							trang</option>
					</select>
				</div>

				<div class="col-12 col-md-5">
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
					<select class="form-select" name="sort">
						<c:set var="sortVal" value="${param.sort}" />
						<option value="" ${empty sortVal ? 'selected' : ''}>Sắp
							xếp</option>
						<option value="new_desc"
							${sortVal == 'new_desc'     ? 'selected' : ''}>Mới nhất</option>
						<option value="price_asc"
							${sortVal == 'price_asc'    ? 'selected' : ''}>Giá ↑</option>
						<option value="price_desc"
							${sortVal == 'price_desc'   ? 'selected' : ''}>Giá ↓</option>
						<option value="rating_desc"
							${sortVal == 'rating_desc'  ? 'selected' : ''}>Đánh giá
							↓</option>
					</select>
				</div>

				<div class="col-6 col-md-2">
					<input class="form-control" name="brand" value="${param.brand}"
						placeholder="Brand">
				</div>
				<div class="col-6 col-md-2">
					<select class="form-select" name="gender">
						<c:set var="gVal" value="${param.gender}" />
						<option value="">Gender</option>
						<option value="male" ${gVal=='male'   ? 'selected' : ''}>Male</option>
						<option value="female" ${gVal=='female' ? 'selected' : ''}>Female</option>
						<option value="unisex" ${gVal=='unisex' ? 'selected' : ''}>Unisex</option>
					</select>
				</div>
				<div class="col-6 col-md-2">
					<input class="form-control" name="style" value="${param.style}"
						placeholder="Style">
				</div>


				<div class="col-12 col-md-auto d-flex gap-2">
					<button class="btn btn-secondary" type="submit">Lọc</button>
					<a class="btn btn-outline-secondary"
						href="<c:url value='/vendor/products'/>">Xóa lọc</a>
				</div>
			</form>
		</div>
	</div>

	<div class="card recent-orders-card">
		<div class="card-body">
			<h5 class="card-title">Danh sách sản phẩm</h5>

			<div class="row row-cols-1 row-cols-md-2 row-cols-lg-4 g-4 mt-2">
				<c:forEach var="it" items="${products}">
					<c:url var="editUrl" value="/vendor/products/edit">
						<c:param name="id" value="${it.productId}" />
					</c:url>
					<c:url var="variantsUrl" value="/vendor/product/variants">
						<c:param name="productId" value="${it.productId}" />
					</c:url>
					<c:url var="attrUrl" value="/vendor/attribute">
						<c:param name="productId" value="${it.productId}" />
					</c:url>
					<c:url var="saleUrl" value="/vendor/sales/new">
						<c:param name="productId" value="${it.productId}" />
					</c:url>


					<div class="col product-card-animation">
						<div class="card-3d-hover">
							<div class="card h-100 vendor-card">

								<c:choose>
									<c:when test="${not empty thumbnails[it.productId]}">
										<img src="${thumbnails[it.productId]}"
											class="card-img-top product-card-img cover"
											alt="Ảnh sản phẩm" />
									</c:when>
									<c:otherwise>
										<img
											src="${pageContext.request.contextPath}/assets/img/placeholder.png"
											class="card-img-top product-card-img cover" alt="Placeholder" />
									</c:otherwise>
								</c:choose>

								<div class="card-body">
									<h6 class="fw-bold product-card-title"
										title="${it.productName}">
										<c:out value="${it.productName}" />
									</h6>

									<div class="small text-muted">
										<c:out
											value="${empty it.category ? '—' : it.category.categoryName}" />
									</div>

									<div
										class="d-flex justify-content-between mt-2 align-items-center">
										<div>
											<c:choose>
												<%-- Có khuyến mãi: show giá đã giảm + giá gạch + badge -X% --%>
												<c:when test="${not empty discountedPrice[it.productId]}">
													<span class="fw-bold text-danger"> <fmt:formatNumber
															value="${discountedPrice[it.productId]}" type="number"
															groupingUsed="true" /> ₫
													</span>
													<small class="text-muted text-decoration-line-through ms-1">
														<fmt:formatNumber value="${it.price}" type="number"
															groupingUsed="true" /> ₫
													</small>
													<span
														class="badge bg-danger-subtle text-danger-emphasis ms-1">
														-<fmt:formatNumber value="${salePercent[it.productId]}"
															type="number" maxFractionDigits="0" />%
													</span>
													<c:if test="${not empty saleEndDate[it.productId]}">
														<div class="small text-muted mt-1">
															Đến:
															<c:out value="${saleEndDate[it.productId]}" /> <%-- ĐÃ SỬA LỖI TẠI ĐÂY --%>
														</div>
													</c:if>
												</c:when>

												<%-- Không có khuyến mãi: giữ như cũ --%>
												<c:otherwise>
													<span class="fw-bold text-danger"> <fmt:formatNumber
															value="${it.price}" type="currency" currencySymbol="₫" />
													</span>
												</c:otherwise>
											</c:choose>
										</div>


										<span
											class="badge ${stocks[it.productId] > 0 ? 'bg-success-subtle text-success-emphasis' : 'bg-danger-subtle text-danger-emphasis'}">
											Tồn: <c:out value="${stocks[it.productId]}" />
										</span>
									</div>


									<span
										class="badge status-badge bg-${it.status == 'ACTIVE' ? 'success' : 'secondary'} mt-2">
										<c:out value="${it.status}" />
									</span>
								</div>

								<div class="card-footer product-actions-footer">
									<div class="product-actions-grid">
										<a class="btn btn-sm btn-outline-primary" href="${editUrl}">Sửa</a>
										<a class="btn btn-sm btn-outline-warning"
											href="${variantsUrl}">Biến thể</a> <a
											class="btn btn-sm btn-outline-secondary" href="${attrUrl}">Thuộc
											tính</a> <a class="btn btn-sm btn-outline-success"
											href="${saleUrl}"> Khuyến mãi </a>


										<button type="button"
											class="btn btn-sm btn-toggle ${it.status == 'ACTIVE' ? 'btn-outline-danger' : 'btn-outline-success'}"
											data-id="${it.productId}">${it.status == 'ACTIVE' ? 'Ẩn' : 'Hiện'}
										</button>
									</div>
								</div>

							</div>
						</div>
					</div>
				</c:forEach>
			</div>

			<c:if test="${totalPages > 1}">
				<nav class="mt-3" aria-label="Pagination">
					<ul class="pagination mb-0 pagination-glass">
						<c:set var="cur" value="${page}" />

						<li class="page-item ${cur <= 1 ? 'disabled' : ''}"><a
							class="page-link"
							href="<c:url value='/vendor/products'>
                         <c:param name='page' value='${cur-1}'/>
                         <c:param name='size' value='${size}'/>
                         <c:if test='${not empty q}'><c:param name='q' value='${q}'/></c:if>
                         <c:if test='${not empty categoryId}'><c:param name='categoryId' value='${categoryId}'/></c:if>
                         <c:if test='${not empty status}'><c:param name='status' value='${status}'/></c:if>

                         <c:if test='${not empty param.minPrice}'><c:param name='minPrice' value='${param.minPrice}'/></c:if>
                         <c:if test='${not empty param.maxPrice}'><c:param name='maxPrice' value='${param.maxPrice}'/></c:if>
                         <c:if test='${not empty param.sort}'><c:param name='sort' value='${param.sort}'/></c:if>
                         <c:if test='${not empty param.brand}'><c:param name='brand' value='${param.brand}'/></c:if>
                         <c:if test='${not empty param.gender}'><c:param name='gender' value='${param.gender}'/></c:if>
                         <c:if test='${not empty param.style}'><c:param name='style' value='${param.style}'/></c:if>
                         <c:if test='${not empty param.province}'><c:param name='province' value='${param.province}'/></c:if>
                       </c:url>">Prev</a>
						</li>

						<c:forEach var="i" begin="1" end="${totalPages}">
							<li class="page-item ${i == cur ? 'active' : ''}"><a
								class="page-link"
								href="<c:url value='/vendor/products'>
                           <c:param name='page' value='${i}'/>
                           <c:param name='size' value='${size}'/>
                           <c:if test='${not empty q}'><c:param name='q' value='${q}'/></c:if>
                           <c:if test='${not empty categoryId}'><c:param name='categoryId' value='${categoryId}'/></c:if>
                           <c:if test='${not empty status}'><c:param name='status' value='${status}'/></c:if>

                           <c:if test='${not empty param.minPrice}'><c:param name='minPrice' value='${param.minPrice}'/></c:if>
                           <c:if test='${not empty param.maxPrice}'><c:param name='maxPrice' value='${param.maxPrice}'/></c:if>
                           <c:if test='${not empty param.sort}'><c:param name='sort' value='${param.sort}'/></c:if>
                           <c:if test='${not empty param.brand}'><c:param name='brand' value='${param.brand}'/></c:if>
                           <c:if test='${not empty param.gender}'><c:param name='gender' value='${param.gender}'/></c:if>
                           <c:if test='${not empty param.style}'><c:param name='style' value='${param.style}'/></c:if>
                           <c:if test='${not empty param.province}'><c:param name='province' value='${param.province}'/></c:if>
                         </c:url>">${i}</a>
							</li>
						</c:forEach>

						<li class="page-item ${cur >= totalPages ? 'disabled' : ''}">
							<a class="page-link"
							href="<c:url value='/vendor/products'>
                         <c:param name='page' value='${cur+1}'/>
                         <c:param name='size' value='${size}'/>
                         <c:if test='${not empty q}'><c:param name='q' value='${q}'/></c:if>
                         <c:if test='${not empty categoryId}'><c:param name='categoryId' value='${categoryId}'/></c:if>
                         <c:if test='${not empty status}'><c:param name='status' value='${status}'/></c:if>

                         <c:if test='${not empty param.minPrice}'><c:param name='minPrice' value='${param.minPrice}'/></c:if>
                         <c:if test='${not empty param.maxPrice}'><c:param name='maxPrice' value='${param.maxPrice}'/></c:if>
                         <c:if test='${not empty param.sort}'><c:param name='sort' value='${param.sort}'/></c:if>
                         <c:if test='${not empty param.brand}'><c:param name='brand' value='${param.brand}'/></c:if>
                         <c:if test='${not empty param.gender}'><c:param name='gender' value='${param.gender}'/></c:if>
                         <c:if test='${not empty param.style}'><c:param name='style' value='${param.style}'/></c:if>
                         <c:if test='${not empty param.province}'><c:param name='province' value='${param.province}'/></c:if>
                       </c:url>">Next</a>
						</li>
					</ul>
				</nav>
			</c:if>
		</div>
	</div>
</div>

<style>
/* Không cho overlay hiệu ứng bắt chuột */
.card-3d-hover::before, .card-3d-hover::after {
	pointer-events: none !important;
}

/* Ảnh không che phần footer */
.product-card-img.cover {
	position: relative;
	z-index: 1;
	display: block;
}

/* Đảm bảo footer & các nút ở trên overlay */
.vendor-card .product-actions-footer, .vendor-card .product-actions-footer .product-actions-grid,
	.vendor-card .card-footer a, .vendor-card .card-footer button {
	position: relative;
	z-index: 3;
	pointer-events: auto;
}

/* Hạ mọi lớp phủ tổng thể của card nếu có */
.card-3d-hover, .vendor-card {
	position: relative;
	z-index: 1;
}
</style>

<script>
document.addEventListener('DOMContentLoaded', function () {
  const $$ = (q, el = document) => Array.from(el.querySelectorAll(q));

  $$('.btn-toggle').forEach(btn => {
    btn.addEventListener('click', async () => {
      const id = btn.dataset.id;
      if (!id) return;
      try {
        const res = await fetch('<c:url value="/vendor/products/toggle"/>', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
          },
          body: new URLSearchParams({ id })
        });

        if (!res.ok) {
          alert('Lỗi kết nối');
          return;
        }

        const data = await res.json();
        if (!data || !data.ok) {
          alert((data && data.message) || 'Lỗi thao tác');
          return;
        }

        // Cập nhật UI không reload
        const card = btn.closest('.vendor-card');
        if (!card) return;

        // Badge trạng thái
        const badge = card.querySelector('.status-badge');
        if (badge) {
          badge.textContent = data.status;
          badge.classList.remove('bg-success', 'bg-secondary');
          badge.classList.add(data.status === 'ACTIVE' ? 'bg-success' : 'bg-secondary');
        }

        // Nút toggle
        btn.classList.remove('btn-outline-danger', 'btn-outline-success');
        if (data.status === 'ACTIVE') {
          btn.classList.add('btn-outline-danger');
          btn.textContent = 'Ẩn';
        } else {
          btn.classList.add('btn-outline-success');
          btn.textContent = 'Hiện';
        }
      } catch (e) {
        alert('Lỗi kết nối');
      }
    });
  });
});
</script>