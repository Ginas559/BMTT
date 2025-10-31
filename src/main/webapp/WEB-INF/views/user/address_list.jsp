<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<h1 class="h5 mb-3">Địa chỉ nhận hàng</h1>

<c:if test="${not empty flash}">
  <div class="alert alert-success">${flash}</div>
  <c:remove var="flash" scope="session"/>
</c:if>
<c:if test="${not empty flash_error}">
  <div class="alert alert-danger">${flash_error}</div>
  <c:remove var="flash_error" scope="session"/>
</c:if>

<div class="mb-3 text-end">
  <a href="${ctx}/user/address/new" class="btn btn-primary">➕ Thêm địa chỉ</a>
</div>

<c:choose>
  <c:when test="${not empty addresses}">
    <div class="row row-cols-1 row-cols-md-2 g-3">
      <c:forEach var="a" items="${addresses}">
        <div class="col">
          <div class="card h-100">
            <div class="card-body">
              <div class="fw-semibold mb-1">${a.receiverName}</div>
              <div class="text-muted small mb-1">${a.phone}</div>
              <div class="mb-2">${a.addressDetail}</div>
              <c:if test="${a.isDefault}">
                <span class="badge bg-success">Mặc định</span>
              </c:if>
            </div>
            <div class="card-footer bg-white border-top-0 d-flex justify-content-end gap-2">
              <a href="${ctx}/user/address/edit?id=${a.addressId}" class="btn btn-sm btn-outline-primary">Sửa</a>
              
              <!-- Form xóa với modal -->
              <form id="deleteForm${a.addressId}" method="post" action="${ctx}/user/address/delete" style="display:inline-block;">
                <input type="hidden" name="id" value="${a.addressId}">
                <button type="button" class="btn btn-sm btn-outline-danger" 
                        onclick="confirmDelete('deleteForm${a.addressId}')">Xoá</button>
              </form>
            </div>
          </div>
        </div>
      </c:forEach>
    </div>
  </c:when>
  <c:otherwise>
    <div class="text-center text-muted py-5">Chưa có địa chỉ nào.</div>
  </c:otherwise>
</c:choose>

<div class="mt-3">
  <a href="${ctx}/user/profile" class="btn btn-outline-secondary">← Quay lại hồ sơ</a>
</div>

<!-- Modal Xác nhận Xóa -->
<div class="modal fade" id="deleteModal" tabindex="-1" aria-labelledby="deleteModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content border-0 shadow-sm">
      <div class="modal-header bg-danger text-white">
        <h5 class="modal-title" id="deleteModalLabel">Xác nhận xoá địa chỉ</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Đóng"></button>
      </div>
      <div class="modal-body">
        <p>Bạn có chắc chắn muốn xoá địa chỉ này không?</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Huỷ</button>
        <button type="button" class="btn btn-danger" id="confirmDeleteBtn">Xoá</button>
      </div>
    </div>
  </div>
</div>

<!-- Script xử lý Modal -->
<script>
  let deleteForm;

  function confirmDelete(formId) {
    deleteForm = document.getElementById(formId);
    const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
    modal.show();
  }

  document.addEventListener("DOMContentLoaded", function () {
    const btn = document.getElementById("confirmDeleteBtn");
    if (btn) {
      btn.addEventListener("click", function () {
        if (deleteForm) {
          deleteForm.submit();
          deleteForm = null;
        }
        const modalEl = document.getElementById('deleteModal');
        const modal = bootstrap.Modal.getInstance(modalEl);
        modal.hide();
      });
    }
  });
</script>
