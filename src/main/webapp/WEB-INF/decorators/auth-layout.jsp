<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <title>BMTT - Đăng nhập & Đăng ký</title>

    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- Bootstrap & Font -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600&display=swap" rel="stylesheet">

    <style>
        body {
            font-family: 'Inter', sans-serif;
            background-color: #f7f8fa;
            color: #333;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }

        header {
            background: #2563eb;
            color: white;
            padding: 1rem 0;
            text-align: center;
            font-size: 1.8rem;
            font-weight: 600;
            letter-spacing: 1px;
            box-shadow: 0 2px 6px rgba(0,0,0,0.1);
        }

        main {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 2rem;
        }

        .auth-card {
            background: white;
            border-radius: 16px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.1);
            padding: 2.5rem;
            width: 100%;
            max-width: 450px;
        }

        footer {
            background: #111827;
            color: #d1d5db;
            text-align: center;
            padding: 1rem;
            font-size: 0.9rem;
        }

        footer a {
            color: #60a5fa;
            text-decoration: none;
        }

        footer a:hover {
            text-decoration: underline;
        }
    </style>

    <!-- ✅ Trang con có thể bổ sung thêm CSS -->
    <sitemesh:write property="head"/>
</head>
<body>

<header>
    🛍️ Sàn Thương Mại <strong>BMTT</strong>
</header>

<main>
    <div class="auth-card">
        <sitemesh:write property="body"/>
    </div>
</main>

<footer>
    <div>Liên hệ hỗ trợ: <a href="mailto:support@bmtt.vn">support@bmtt.vn</a> | Hotline: 1900-6789</div>
    <div>&copy; 2025 BMTT Marketplace. All rights reserved.</div>
</footer>

<!-- Bootstrap -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

<!-- ✅ Trang con có thể bổ sung thêm JS -->
<sitemesh:write property="scripts"/>

</body>
</html>
