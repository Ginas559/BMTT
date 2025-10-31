<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <title>${pageTitle != null ? pageTitle : 'BMTT Shop - Shipper'}</title>

  <sitemesh:write property="head"/>

  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
  <nav class="navbar navbar-expand-lg navbar-light bg-info border-bottom mb-3">
    <div class="container">
      <a class="navbar-brand text-white" href="${pageContext.request.contextPath}/">BMTT Shipper</a>

      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav"
              aria-controls="mainNav" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>

      <div class="collapse navbar-collapse" id="mainNav">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0">
          
          <c:if test="${sessionScope.role == 'SHIPPER'}">
            <li class="nav-item">
              <a class="nav-link text-white" href="${pageContext.request.contextPath}/shipper/available-orders">Đơn hàng có sẵn</a>
            </li>

            <li class="nav-item">
              <a class="nav-link text-white" href="${pageContext.request.contextPath}/shipper/my-orders">Đơn hàng của tôi</a>
            </li>

            <li class="nav-item">
              <a class="nav-link text-white" href="${pageContext.request.contextPath}/shipper/history">Lịch sử giao hàng</a>
            </li>

            <li class="nav-item">
              <a class="nav-link text-white" href="${pageContext.request.contextPath}/shipper/social">Mạng xã hội Shipper</a>
            </li>
          </c:if>
        </ul>

        <div class="d-flex align-items-center gap-2">
          <c:choose>
            <c:when test="${empty sessionScope.userId}">
              <a class="btn btn-sm btn-outline-light" href="${pageContext.request.contextPath}/login">Đăng nhập</a>
              <a class="btn btn-sm btn-warning" href="${pageContext.request.contextPath}/register">Đăng ký (Shipper)</a>
            </c:when>
            <c:otherwise>
              <a class="btn btn-sm btn-outline-light" href="${pageContext.request.contextPath}/shipper/statistics/view">
                Xin chào, ${sessionScope.email}
              </a>
              <form method="post" action="${pageContext.request.contextPath}/logout" class="d-inline m-0">
                <button class="btn btn-sm btn-danger" type="submit">Đăng xuất</button>
              </form>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </nav>

  <main class="container">
    <sitemesh:write property="body"/>
  </main>

  <footer class="border-top mt-5 py-3">
    <div class="container small text-muted d-flex flex-wrap gap-2 justify-content-between">
      <span>&copy; 2025 BMTTShop - Dành cho Shipper</span>
      <span>
        <a class="text-decoration-none" href="#">Trợ giúp</a>
        • <a class="text-decoration-none" href="#">Liên hệ</a>
      </span>
    </div>
  </footer>

  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>