package vn.iotstar.controllers;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.iotstar.configs.CloudinaryConfig;
import com.cloudinary.Cloudinary;

@WebServlet("/test-cloudinary")
public class TestCloudinaryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Cloudinary cloud = CloudinaryConfig.getCloudinary();
        resp.setContentType("text/plain; charset=UTF-8");
        resp.getWriter().println("Cloudinary Connected ✅");
        resp.getWriter().println("Cloud Name: " + cloud.config.cloudName);
    }
}
