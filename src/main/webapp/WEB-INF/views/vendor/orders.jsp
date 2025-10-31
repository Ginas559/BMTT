<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!doctype html>
<html lang="vi">
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<title>${pageTitle != null ? pageTitle : 'Quản lý Đơn hàng - BMTT Shop'}</title>

<%-- Sitemesh sẽ chèn các file CSS chung (như web.css v1) vào đây --%>
<sitemesh:write property="head" />

<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
	rel="stylesheet">

<%-- Đây là file CSS "thêm" của riêng trang này --%>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/web2.css">

<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
</head>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="statuses" value="NEW,CONFIRMED,SHIPPING,DONE,CANCELLED"/>

<%-- 
  ĐÃ THÊM: class "main-vendor-orders" 
  Đây là "hook" để CSS nhận diện và tô nền gradient pastel cho cả trang
--%>
<main class="container py-4 main-vendor-orders">
  <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-3">
    <%-- 
      ĐÃ THÊM: class "gradient-text" 
      Lấy từ web.css (v1) để làm tiêu đề "cháy" hơn 
    --%>
    <h2 class="mb-0 gradient-text" style="font-weight: 700;">Quản lý Đơn hàng</h2>
    <a class="btn btn-outline-secondary btn-sm" href="${ctx}/vendor/dashboard">← Về Dashboard</a>
  </div>

  <%-- Khung Flash Message (Giữ nguyên) --%>
  <c:if test="${not empty flashMsg}">
    <div class="alert alert-${empty flashType ? 'info' : flashType} alert-dismissible fade show" role="alert">
      ${flashMsg}
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  </c:if>

  <%-- 
    ĐÃ THÊM: Bọc form lọc vào card "filter-card"
    Style của card này sẽ được định nghĩa trong web2.css
  --%>
  <div class="card filter-card shadow-sm mb-3">
    <div class="card-body">
      <h5 class="card-title mb-3" style="font-weight: 600;">🔍 Bộ lọc đơn hàng</h5>
      <form class="row g-2" method="get" action="${ctx}/vendor/orders">
        <div class="col-md-3">
          <input class="form-control" name="q" placeholder="Tìm khách (tên/email)..." value="${q}"/>
        </div>
        <div class="col-md-3">
          <select name="status" class="form-select">
            <option value="">-- Tất cả trạng thái --</option>
            <c:forEach var="st" items="${fn:split(statuses, ',')}">
              <option value="${st}" <c:if test="${st eq status}">selected</c:if>>${st}</option>
            </c:forEach>
          </select>
        </div>
        <div class="col-md-2">
          <select name="size" class="form-select">
            <c:set var="sz" value="${size!=null?size:20}"/>
            <option value="10"  ${sz==10 ? 'selected':''}>10 / trang</option>
            <option value="20"  ${sz==20 ? 'selected':''}>20 / trang</option>
            <option value="50"  ${sz==50 ? 'selected':''}>50 / trang</button>
          </select>
        </div>
        <div class="col-md-2">
          <button class="btn btn-primary w-100">Lọc</button>
        </div>
      </form>
    </div>
  </div>

  <%-- 
    ĐÃ THÊM: class "recent-orders-card"
    Lấy từ web.css (v1) để tự động áp dụng nền tím pastel 
    và style bảng "cháy" (header hồng, row hover...)
  --%>
  <div class="card recent-orders-card shadow-sm">
    <div class="card-body">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <p class="text-muted mb-0">
          Hiển thị ${fn:length(orders)} trên ${totalItems} kết quả.
        </p>
      </div>

      <div class="table-responsive">
        <%-- ĐÃ XÓA: class="table-striped" (vì đã dùng nền trong suốt) --%>
        <table class="table table-hover align-middle">
          <%-- ĐÃ XÓA: class="table-light" (để header gradient đè lên) --%>
          <thead class="">
          <tr>
            <th>ID</th>
            <th>Khách hàng</th>
            <th>Tổng tiền</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
            <th width="220">Cập nhật</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach var="o" items="${orders}">
            <tr>
              <td>
                ${o.orderId}
                <button type="button" class="btn btn-link btn-sm p-0 ms-2 js-detail" data-id="${o.orderId}" title="Xem chi tiết và tồn kho">
                  Chi tiết
                </button>
              </td>
              <td>${o.user.firstname} ${o.user.lastname}</td>
              <td>${o.totalAmount}</td>
              <td>
                <%-- 
                  ĐÃ NÂNG CẤP: Badge màu mè động 
                  Tự động nhận hiệu ứng "glow" từ web.css (v1)
                --%>
                <c:set var="statusName" value="${o.status.name()}" />
                <c:choose>
                  <c:when test="${statusName == 'NEW'}"><span class="badge bg-primary">${statusName}</span></c:when>
                  <c:when test="${statusName == 'CONFIRMED'}"><span class="badge bg-info">${statusName}</span></c:when>
                  <c:when test="${statusName == 'SHIPPING'}"><span class="badge bg-warning">${statusName}</span></c:when>
                  <c:when test="${statusName == 'DONE'}"><span class="badge bg-success">${statusName}</span></c:when>
                  <c:when test="${statusName == 'CANCELLED'}"><span class="badge bg-danger">${statusName}</span></c:when>
                  <c:otherwise><span class="badge bg-secondary">${statusName}</span></c:otherwise>
                </c:choose>
              </td>
              <td>${o.createdAt}</td>
              <td>
                <form method="post" action="${ctx}/vendor/orders" class="d-flex gap-2">
                  <input type="hidden" name="orderId" value="${o.orderId}"/>
                  <input type="hidden" name="page" value="${page}"/> 
                  <c:if test="${not empty status}"><input type="hidden" name="status" value="${status}"/></c:if>
                  <c:if test="${not empty q}"><input type="hidden" name="q" value="${q}"/></c:if>
                  
                  <%-- ĐÃ XÓA: class="form-select-sm" (cho to rõ hơn) --%>
                  <select name="newStatus" class="form-select form-select-sm">
                    <c:forEach var="st" items="${fn:split(statuses, ',')}">
                      <option value="${st}" <c:if test="${st eq o.status.name()}">selected</c:if>>${st}</option>
                    </c:forEach>
                  </select>
                  <button class="btn btn-sm btn-primary" type="submit">Lưu</button>
                </form>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty orders}">
            <tr><td colspan="6" class="text-muted">Không có đơn phù hợp.</td></tr>
          </c:if>
          </tbody>
        </table>
      </div>

      <c:if test="${totalPages > 1}">
        <%-- 
          ĐÃ THÊM: class "pagination-glass" và "justify-content-center"
          Lấy từ web.css (v1) để áp dụng hiệu ứng kính mờ
        --%>
        <nav>
          <ul class="pagination pagination-glass justify-content-center">
            <c:set var="cur" value="${page}" />
            <li class="page-item ${cur<=1?'disabled':''}">
              <a class="page-link"
                 href="<c:url value='/vendor/orders'>
                          <c:param name='page' value='${cur-1}'/>
                          <c:param name='size' value='${size}'/>
                          <c:if test='${not empty q}'><c:param name='q' value='${q}'/></c:if>
                          <c:if test='${not empty status}'><c:param name='status' value='${status}'/></c:if>
                        </c:url>">Prev</a>
            </li>
            <c:forEach var="i" begin="1" end="${totalPages}">
              <li class="page-item ${i==cur?'active':''}">
                <a class="page-link"
                   href="<c:url value='/vendor/orders'>
                            <c:param name='page' value='${i}'/>
                            <c:param name='size' value='${size}'/>
                            <c:if test='${not empty q}'><c:param name='q' value='${q}'/></c:if>
                            <c:if test='${not empty status}'><c:param name='status' value='${status}'/></c:if>
                         </c:url>">${i}</a>
              </li>
            </c:forEach>
            <li class="page-item ${cur>=totalPages?'disabled':''}">
              <a class="page-link"
                 href="<c:url value='/vendor/orders'>
                          <c:param name='page' value='${cur+1}'/>
                          <c:param name='size' value='${size}'/>
                          <c:if test='${not empty q}'><c:param name='q' value='${q}'/></c:if>
                          <c:if test='${not empty status}'><c:param name='status' value='${status}'/></c:if>
                        </c:url>">Next</a>
            </li>
          </ul>
        </nav>
      </c:if>
    </div>
  </div>
</main>

<%-- Modal hiển thị chi tiết --%>
<div class="modal fade" id="orderDetailModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-lg modal-dialog-scrollable">
    <div class="modal-content modal-colorful">
      <div class="modal-header">
        <h5 class="modal-title">Chi tiết đơn hàng</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <div id="orderDetailBody">Đang tải…</div>
      </div>
    </div>
  </div>
</div>

<%-- ⭐ CSS ưu tiên cao nhất cho modal chi tiết (ghi thẳng trong JSP để override file ngoài) --%>
<style>
  /* Modal nền gradient */
  .modal-colorful {
    background: linear-gradient(145deg, #f9f9f9 0%, #ffffff 50%, #f3f6f9 100%);
    border-radius: 12px;
    border: 1px solid #e3e6ea;
    box-shadow: 0 10px 35px rgba(0, 0, 0, 0.25);
  }

  /* Header modal */
  #orderDetailModal .modal-header {
    background: #2a4365;
    color: #fff;
    border-bottom: none;
    border-radius: 12px 12px 0 0;
  }

  /* Nút đóng */
  #orderDetailModal .btn-close {
    filter: brightness(0) invert(1);
  }

  /* Body */
  #orderDetailModal .modal-body {
    background: #fff;
    padding: 1.5rem;
  }

  /* Bảng chi tiết */
  #orderDetailModal table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.9rem;
  }

  #orderDetailModal th {
    background: #edf2f7;
    color: #2d3748;
    font-weight: 600;
    text-transform: uppercase;
    border-bottom: 2px solid #cbd5e0;
  }

  #orderDetailModal td {
    padding: 0.5rem 0.75rem;
    border-bottom: 1px solid #e2e8f0;
    vertical-align: middle;
  }

  #orderDetailModal tbody tr:hover {
    background-color: #f7fafc;
    transition: background 0.2s;
  }

  #orderDetailModal .text-danger {
    color: #e53e3e !important;
    font-weight: 700 !important;
  }

  /* Ảnh sản phẩm */
  #orderDetailModal img {
    width: 60px;
    height: 60px;
    border-radius: 8px;
    object-fit: cover;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  }

  /* Tổng cộng */
  #orderDetailModal tfoot th {
    background: #2b6cb0;
    color: #fff;
    text-align: right;
    font-size: 1rem;
    border-top: 3px solid #1a365d;
  }

  /* Hiệu ứng khi modal hiện */
  .modal.show .modal-dialog {
    transform: scale(1.02);
    transition: transform 0.2s ease-out;
  }
</style>


<%-- JavaScript xử lý Ajax và Modal (Giữ nguyên) --%>
<script>
document.addEventListener('DOMContentLoaded', () => {
  const BASE_URL = '${ctx}'; 
  const modalEl = document.getElementById('orderDetailModal');
  const modal   = new bootstrap.Modal(modalEl);
  const body    = document.getElementById('orderDetailBody');

  async function openDetail(orderId) {
      body.innerHTML = '<div class="text-center text-muted py-3">Đang tải…</div>'; 
      modal.show();

      try {
        const url = BASE_URL + '/vendor/orders/detail?orderId=' + encodeURIComponent(orderId);
        const res = await fetch(url);
        
        if (!res.ok) {
           throw new Error('Lỗi Server: ' + res.status);
        }
        
        body.innerHTML = await res.text();
      } catch (e) {
        console.error("Lỗi tải chi tiết đơn hàng:", e);
        body.innerHTML = '<div class="alert alert-danger">Không tải được chi tiết đơn hàng. Vui lòng thử lại.</div>';
      }
  }

  document.querySelectorAll('.js-detail').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = btn.dataset.id;
      openDetail(id); 
    });
  });
});
</script>