package vn.iotstar.configs;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

public class CloudinaryConfig {
    private static final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
        "cloud_name", System.getenv("CLOUDINARY_NAME"),
        "api_key", System.getenv("CLOUDINARY_API_KEY"),
        "api_secret", System.getenv("CLOUDINARY_API_SECRET"),
        "secure", true
    ));

    public static Cloudinary getInstance() {
        return cloudinary;
    }
}
