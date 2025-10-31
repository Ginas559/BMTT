package vn.iotstar.utilities;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Servlet implementation class ImageLoad
 */
@WebServlet("/image")
public class ImageLoad extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Thư mục gốc chứa tất cả ảnh
    private static final String BASE_DIR = "F:/HK1_25_26/LTWEB/uploads/";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // type: loại ảnh (ví dụ: categories, products, users)
        // fname: tên file ảnh
        String type = req.getParameter("type");
        String fname = req.getParameter("fname");

        if (fname == null || fname.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "File name is missing!");
            return;
        }

        // Nếu không có type thì mặc định là categories
        if (type == null || type.isBlank()) {
            type = "categories";
        }

        // Đường dẫn đầy đủ đến file
        File file = new File(BASE_DIR + type + File.separator + fname);
        if (!file.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found!");
            return;
        }

        // Xác định content type dựa trên đuôi file
        if (fname.endsWith(".jpg") || fname.endsWith(".jpeg")) {
            resp.setContentType("image/jpeg");
        } else if (fname.endsWith(".png")) {
            resp.setContentType("image/png");
        } else if (fname.endsWith(".gif")) {
            resp.setContentType("image/gif");
        } else {
            resp.setContentType("application/octet-stream");
        }

        // Gửi ảnh về client
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = resp.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }

        System.out.println("[ImageLoad] Sent: " + file.getAbsolutePath());
    }
}
