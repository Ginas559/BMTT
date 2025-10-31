// filepath: src/main/java/vn/iotstar/services/CloudinaryService.java
package vn.iotstar.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.servlet.http.Part;
import vn.iotstar.configs.CloudinaryConfig;

import java.io.InputStream;
import java.util.Map;

public class CloudinaryService {

    private final Cloudinary cloud;

    public CloudinaryService() {
        this.cloud = CloudinaryConfig.getCloudinary();
    }

    /** Upload ảnh BIẾN THỂ: đọc thành byte[] rồi upload (tránh lỗi Unexpected file parameter). */
    public String uploadVariant(Part part, String folder) {
        if (part == null || part.getSize() <= 0) return null;
        try (InputStream is = part.getInputStream()) {
            byte[] bytes = is.readAllBytes(); // <- QUAN TRỌNG: dùng byte[] thay vì InputStream
            @SuppressWarnings("rawtypes")
            Map res = cloud.uploader().upload(bytes, ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "use_filename", true,
                    "unique_filename", true
            ));
            return String.valueOf(res.get("secure_url"));
        } catch (Exception e) {
            throw new RuntimeException("Upload variant image failed", e);
        }
    }
}
