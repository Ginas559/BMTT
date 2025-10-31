// filepath: src/main/java/vn/iotstar/configs/CloudinaryConfig.java
package vn.iotstar.configs;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
// import io.github.cdimascio.dotenv.Dotenv; // <-- BỎ THƯ VIỆN NÀY ĐI

public class CloudinaryConfig {

    // BỎ DÒNG Dotenv.load() GÂY LỖI ĐI
    // private static final Dotenv dotenv = Dotenv.load(); 

    // Khởi tạo Cloudinary bằng cách đọc thẳng từ System Environment
    private static final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
        // Sửa dotenv.get("...") thành System.getenv("...")
        "cloud_name", System.getenv("CLOUDINARY_CLOUD_NAME"),
        "api_key", System.getenv("CLOUDINARY_API_KEY"),
        "api_secret", System.getenv("CLOUDINARY_API_SECRET")
    ));

    public static Cloudinary getCloudinary() {
        return cloudinary;
    }
}