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

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<%-- ĐÃ THÊM: class "main-vendor-vouchers" để "ăn" nền pastel --%>
<main class="container py-4 main-vendor-vouchers">
  <div class="d-flex align-items-center justify-content-between mb-3">
    <%-- ĐÃ THÊM: class "gradient-text" (từ v1) cho "cháy" --%>
    <h2 class="mb-0 gradient-text" style="font-weight: 700;">Quản lý Voucher</h2>
    <div class="d-flex gap-2">
      <%-- Nút này sẽ tự "ăn" hiệu ứng pulse/shine từ web.css (v1) --%>
      <a class="btn btn-primary" href="${ctx}/vendor/vouchers/new">+ Tạo voucher</a>
    </div>
  </div>

  <%-- Hiển thị thông báo lỗi (Giữ nguyên, chỉ thêm style) --%>
  <c:if test="${not empty errors}">
    <%-- ĐÃ THÊM: Hiệu ứng "glow" đỏ cho alert --%>
    <div class="alert alert-danger" style="box-shadow: 0 0 15px rgba(220, 53, 69, 0.5);">
      <h5 class="alert-heading mb-2">Lỗi!</h5>
      <ul class="mb-0">
        <c:forEach var="e" items="${errors}">
          <li><c:out value="${e}"/></li>
        </c:forEach>
      </ul>
    </div>
  </c:if>

  <%-- ĐÃ THÊM: class "filter-card" (từ v2) để "ăn" nền xanh --%>
  <div class="card shadow-sm mb-3 filter-card">
    <div class="card-body">
      <h5 class="card-title mb-3" style="font-weight: 600;">🔍 Bộ lọc voucher</h5>
      <form method="get" action="${ctx}/vendor/vouchers" class="row g-2">
        <div class="col-md-3">
          <input class="form-control" name="q" placeholder="Tìm theo code..." value="${q}"/>
        </div>
        <div class="col-md-2">
          <select class="form-select" name="type">
            <option value="" ${empty type ? 'selected' : ''}>-- Loại --</option>
            <option value="PERCENT" ${type == 'PERCENT' ? 'selected' : ''}>PERCENT</option>
            <option value="AMOUNT" ${type == 'AMOUNT' ? 'selected' : ''}>AMOUNT</option>
          </select>
        </div>
        <div class="col-md-2">
          <select class="form-select" name="status">
            <option value="" ${empty status ? 'selected' : ''}>-- Trạng thái --</option>
            <option value="ACTIVE" ${status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
            <option value="INACTIVE" ${status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
          </select>
        </div>
        <div class="col-md-2">
          <input class="form-control" type="datetime-local" name="from" value="${from}" placeholder="Từ"/>
        </div>
        <div class="col-md-2">
          <input class="form-control" type="datetime-local" name="to" value="${to}" placeholder="Đến"/>
        </div>
        <div class="col-md-1">
          <select class="form-select" name="size">
            <c:set var="sz" value="${size != null ? size : 10}"/>
            <option ${sz == 10 ? 'selected' : ''} value="10">10</option>
            <option ${sz == 20 ? 'selected' : ''} value="20">20</option>
            <option ${sz == 50 ? 'selected' : ''} value="50">50</option>
          </select>
        </div>
        <div class="col-md-12 d-flex justify-content-end">
          <%-- ĐÃ SỬA: "Lọc" dùng btn-primary (ăn style v2) thay vì outline --%>
          <button class="btn btn-primary">Lọc</button>
        </div>
      </form>
    </div>
  </div>

  <%-- ĐÃ THÊM: class "recent-orders-card" (từ v2) để "ăn" nền tím --%>
  <div class="card shadow-sm recent-orders-card">
    <div class="card-body">
      <h5 class="card-title mb-3" style="font-weight: 600;">Danh sách voucher</h5>
      <div class="table-responsive">
        <%-- ĐÃ XÓA: "table-bordered" (xấu) --%>
        <table class="table table-hover align-middle mb-0">
          <%-- ĐÃ XÓA: "table-light" (để "ăn" header v2) --%>
          <thead class="">
          <tr>
            <th>ID</th>
            <th>Code</th>
            <th>Loại</th>
            <th>Giá trị</th>
            <th>Min Order</th>
            <th>Hiệu lực</th>
            <th>Trạng thái</th>
            <th width="180">Hành động</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach var="v" items="${vouchers}">
            <tr data-row-id="${v.voucherId}">
              <td>${v.voucherId}</td>
              <%-- ĐÃ THÊM: class "code-highlight" cho đẹp --%>
              <td><span class="fw-semibold code-highlight"><c:out value="${v.code}"/></span></td>
              <td>${v.type}</td>
              <%-- ĐÃ THÊM: class "price-highlight" cho đẹp --%>
              <td class="price-highlight">
                <c:choose>
                  <c:when test="${v.type == 'PERCENT'}">
                    ${v.percent}%
                  </c:when>
                  <c:otherwise>
                    ${v.amount}
                  </c:otherwise>
                </c:choose>
              </td>
              <td>${v.minOrderAmount}</td>
              <td class="voucher-dates">
                <div><small>Bắt đầu:</small> ${v.startAt}</div>
                <div><small>Kết thúc:</small> ${v.endAt}</div>
              </td>
              <td>
                <%-- Badge sẽ tự "ăn" glow từ v1 (cho success) và v2 (cho secondary) --%>
                <span class="badge status-badge bg-${v.status == 'ACTIVE' ? 'success' : 'secondary'}">
                  ${v.status}
                </span>
              </td>
              <td>
                <%-- ĐÃ THÊM: "voucher-actions" (giống product-actions) --%>
                <div class="voucher-actions">
                  <a class="btn btn-sm btn-outline-primary" href="${ctx}/vendor/vouchers/edit?id=${v.voucherId}">Sửa</a>
                  <button type."button"
                          class="btn btn-sm btn-toggle ${v.status == 'ACTIVE' ? 'btn-outline-danger' : 'btn-outline-success'}"
                          data-id="${v.voucherId}">
                    ${v.status == 'ACTIVE' ? 'Ẩn' : 'Hiện'}
                  </button>
                </div>
              </td>
            </tr>
          </c:forEach>
           <c:if test="${empty vouchers}">
            <tr><td colspan="8" class="text-center text-muted py-3">Không có voucher nào phù hợp.</td></tr>
          </c:if>
          </tbody>
        </table>
      </div>

      <c:if test="${totalPages > 1}">
        <nav class="mt-3">
          <%-- ĐÃ THÊM: "pagination-glass" và "justify-content-center" --%>
          <ul class="pagination pagination-glass justify-content-center mb-0">
            <c:set var="cur" value="${page}"/>
            
            <%-- Nút Previous --%>
            <li class="page-item ${cur <= 1 ? 'disabled' : ''}">
              <a class="page-link"
                 href="<c:url value='/vendor/vouchers'>
                         <c:param name='page' value='${cur-1}'/>
                         <c:param name='size' value='${size}'/>
                         <c:if test='${not empty q}'><c:param name='q' value='${q}'/></c:if>
                         <c:if test='${not empty type}'><c:param name='type' value='${type}'/></c:if>
                         <c:if test='${not empty status}'><c:param name='status' value='${status}'/></c:if>
                         <c:if test='${not empty from}'><c:param name='from' value='${from}'/></c:if>
                         <c:if test='${not empty to}'><c:param name='to' value='${to}'/></c:if>
                       </c:url>">Prev</a>
            </li>

            <%-- Các nút số trang --%>
            <c:forEach var="i" begin="1" end="${totalPages}">
              <li class="page-item ${i == cur ? 'active' : ''}">
                <a class="page-link"
                   href="<c:url value='/vendor/vouchers'>
                           <c:param name='page' value='${i}'/>
                           <c:param name='size' value='${size}'/>
                           <c:if test='${not empty q}'><c:param name='q' value='${q}'/></c:if>
                           <c:if test='${not empty type}'><c:param name='type' value='${type}'/></c:if>
                           <c:if test='${not empty status}'><c:param name='status' value='${status}'/></c:if>
                           <c:if test='${not empty from}'><c:param name='from' value='${from}'/></c:if>
                           <c:if test='${not empty to}'><c:param name='to' value='${to}'/></c:if>
                         </c:url>">${i}</a>
              </li>
            </c:forEach>

            <%-- Nút Next --%>
            <li class="page-item ${cur >= totalPages ? 'disabled' : ''}">
              <a class="page-link"
                 href="<c:url value='/vendor/vouchers'>
                         <c:param name='page' value='${cur+1}'/>
                         <c:param name='size' value='${size}'/>
                         <c:if test='${not empty q}'><c:param name='q' value='${q}'/></c:if>
                         <c:if test='${not empty type}'><c:param name='type' value='${type}'/></c:if>
                         <c:if test='${not empty status}'><c:param name='status' value='${status}'/></c:if>
                         <c:if test='${not empty from}'><c:param name='from' value='${from}'/></c:if>
                         <c:if test='${not empty to}'><c:param name='to' value='${to}'/></c:if>
                       </c:url>">Next</a>
            </li>
          </ul>
        </nav>
      </c:if>
    </div>
  </div>
</main>

<%-- Khối JavaScript giữ nguyên, nó đã "xịn" rồi --%>
<script>
document.addEventListener('DOMContentLoaded', function () {
  const $$ = (q, el = document) => Array.from(el.querySelectorAll(q));
  
  $$('.btn-toggle').forEach(btn => {
    btn.addEventListener('click', async () => {
      const id = btn.dataset.id;
      // Xác nhận trước khi thực hiện toggle (Optional: có thể thêm SweetAlert hoặc modal)
      // if (!confirm('Bạn có chắc muốn thay đổi trạng thái voucher này?')) return;

      try {
        const res = await fetch('<c:url value="/vendor/vouchers/toggle"/>', {
          method: 'POST',
          headers: {'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'},
          body: new URLSearchParams({id})
        });
        
        const data = await res.json();
        
        if (!res.ok || !data.ok) { 
          alert(data.message || 'Lỗi thao tác'); 
          return; 
        }

        // Cập nhật giao diện sau khi thành công
        const newStatus = data.status;
        const row = btn.closest('tr');
        const badge = row.querySelector('.status-badge');
        
        // Cập nhật Badge
        if (badge) {
          badge.textContent = newStatus;
          badge.classList.remove('bg-success', 'bg-secondary');
          badge.classList.add(newStatus === 'ACTIVE' ? 'bg-success' : 'bg-secondary');
        }
        
        // Cập nhật Button (Đã sửa lại class cho đúng)
        btn.classList.remove('btn-outline-danger', 'btn-outline-success');
        if (newStatus === 'ACTIVE') {
          btn.classList.add('btn-outline-danger'); 
          btn.textContent = 'Ẩn';
        } else {
          btn.classList.add('btn-outline-success'); 
          btn.textContent = 'Hiện';
        }
        
      } catch (e) {
        console.error("Toggle Error:", e);
        alert('Lỗi kết nối hoặc lỗi máy chủ.');
      }
    });
  });
});
</script>