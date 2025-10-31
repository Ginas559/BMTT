<%@ page contentType="text/html; charset=UTF-8" %>
<%
  // Redirect về trang danh sách sản phẩm, tự động lấy context-path hiện tại
  response.sendRedirect(request.getContextPath() + "/products");
%>
