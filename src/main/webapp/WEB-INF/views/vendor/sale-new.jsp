<%-- filepath: src/main/webapp/WEB-INF/views/vendor/sale-new.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<!doctype html>
<html lang="vi">
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <title>Khuyến mãi theo sản phẩm</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
</head>
<body class="container py-4">

  <h3 class="mb-3">Tạo khuyến mãi theo sản phẩm</h3>

  <c:if test="${not empty sessionScope.flash}">
    <div class="alert alert-success">${sessionScope.flash}</div>
    <c:remove var="flash" scope="session"/>
  </c:if>
  <c:if test="${not empty sessionScope.flashErrors}">
    <div class="alert alert-danger">
      <ul class="mb-0">
        <c:forEach var="e" items="${sessionScope.flashErrors}">
          <li>${e}</li>
        </c:forEach>
      </ul>
    </div>
    <c:remove var="flashErrors" scope="session"/>
  </c:if>

  <form class="row g-3" method="post" action="${ctx}/vendor/sales/create">
    <div class="col-md-6">
      <label class="form-label">Sản phẩm</label>
      <select name="productId" class="form-select" required>
        <option value="">-- Chọn sản phẩm --</option>
        <c:forEach var="p" items="${products}">
          <option value="${p.productId}" ${p.productId==productId?'selected':''}>
            #${p.productId} — ${p.productName}
          </option>
        </c:forEach>
      </select>
    </div>

    <div class="col-md-3">
      <label class="form-label">% giảm</label>
      <div class="input-group">
        <input type="number" step="0.01" min="0.01" max="90" name="percent" class="form-control" placeholder="vd 15" required/>
        <span class="input-group-text">%</span>
      </div>
      <div class="form-text">Giới hạn đề xuất: ≤ 90%</div>
    </div>

    <div class="col-md-3"></div>

    <div class="col-md-3">
      <label class="form-label">Từ ngày</label>
      <input type="date" name="startDate" class="form-control" required/>
    </div>
    <div class="col-md-3">
      <label class="form-label">Đến ngày</label>
      <input type="date" name="endDate" class="form-control" required/>
    </div>

    <div class="col-12 d-flex gap-2">
      <button class="btn btn-primary" type="submit">Tạo</button>
      <a class="btn btn-outline-secondary" href="${ctx}/vendor/products">← Quản lý sản phẩm</a>
    </div>
  </form>

</body>
</html>
