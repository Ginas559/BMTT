<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title><sitemesh:write property="title"/></title>

    <!-- Bootstrap & Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">

    <!-- Custom CSS -->
    <style>
        body {
            font-family: 'Segoe UI', sans-serif;
            background-color: #f5f6fa;
        }

        #wrapper {
            display: flex;
            height: 100vh;
            overflow: hidden;
        }

        /* Sidebar */
        #sidebar {
            width: 260px;
            background-color: #1e1e2f;
            color: #cfd2da;
            flex-shrink: 0;
            display: flex;
            flex-direction: column;
            transition: all 0.3s ease;
        }

        #sidebar .sidebar-header {
            padding: 20px;
            text-align: center;
            border-bottom: 1px solid #34344a;
        }

        #sidebar .sidebar-header h3 {
            color: #4e73df;
            font-weight: 700;
        }

        #sidebar ul.components {
            list-style: none;
            padding: 0;
            margin-top: 20px;
            flex-grow: 1;
        }

        #sidebar ul li a {
            color: #cfd2da;
            display: block;
            padding: 12px 20px;
            text-decoration: none;
            border-radius: 8px;
            margin: 4px 8px;
            transition: 0.2s;
        }

        #sidebar ul li a:hover, #sidebar ul li a.active {
            background-color: #4e73df;
            color: #fff;
        }

        #sidebar i {
            width: 20px;
        }

        /* Main content */
        #page-content-wrapper {
            flex-grow: 1;
            overflow-y: auto;
            display: flex;
            flex-direction: column;
        }

        nav.navbar {
            background-color: #fff;
            border-bottom: 1px solid #dee2e6;
        }

        .navbar .btn {
            border-radius: 8px;
        }

        main {
            padding: 20px;
        }

        /* Toggle */
        #menu-toggle {
            border: none;
            color: #4e73df;
            background: transparent;
        }

        #menu-toggle:hover {
            color: #224abe;
        }
    </style>
</head>
<body>

<div id="wrapper">
    <!-- Sidebar -->
    <nav id="sidebar">
        <div class="sidebar-header">
            <h3>ShoesShop Admin</h3>
        </div>
        <ul class="components">
            <li><a href="${pageContext.request.contextPath}/admin/dashboard" class="active"><i class="fas fa-chart-line me-2"></i> Dashboard</a></li>
            <li><a href="${pageContext.request.contextPath}/admin/users"><i class="fas fa-users me-2"></i> Users</a></li>
            <li><a href="${pageContext.request.contextPath}/admin/categories"><i class="fas fa-layer-group me-2"></i> Categories</a></li>
            <li><a href="${pageContext.request.contextPath}/admin/products"><i class="fas fa-boxes me-2"></i> Products</a></li>
            <li><a href="${pageContext.request.contextPath}/admin/coupons"><i class="fas fa-ticket-alt me-2"></i> Coupons</a></li>
            <li><a href="${pageContext.request.contextPath}/admin/promotions"><i class="fas fa-bullhorn me-2"></i> Promotions</a></li>
            <li><a href="${pageContext.request.contextPath}/admin/orders"><i class="fas fa-shopping-cart me-2"></i> Orders</a></li>
            <li><a href="${pageContext.request.contextPath}/admin/shops"><i class="fas fa-store me-2"></i> Shops</a></li>
            <li><a href="${pageContext.request.contextPath}/admin/shippers"><i class="fas fa-truck me-2"></i> Shippers</a></li>
        </ul>

        <div class="text-center mb-3">
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-danger btn-sm">
                <i class="fas fa-sign-out-alt"></i> Logout
            </a>
        </div>
    </nav>

    <!-- Content -->
    <div id="page-content-wrapper">
        <nav class="navbar navbar-light shadow-sm">
            <div class="container-fluid">
                <button id="menu-toggle"><i class="fas fa-bars fa-lg"></i></button>
                <h5 class="mb-0 text-primary">Admin Panel</h5>
                <div class="ms-auto d-flex align-items-center">
                    <span class="text-muted me-3">Hello, Admin</span>
                </div>
            </div>
        </nav>

        <main>
            <sitemesh:write property="body"/>
        </main>
    </div>
</div>

<!-- Scripts -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const toggleButton = document.getElementById("menu-toggle");
    const sidebar = document.getElementById("sidebar");
    toggleButton.addEventListener("click", () => {
        sidebar.classList.toggle("d-none");
    });
</script>
</body>
</html>
