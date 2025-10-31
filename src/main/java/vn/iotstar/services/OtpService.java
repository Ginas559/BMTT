// filepath: src/main/java/vn/iotstar/services/OtpService.java
package vn.iotstar.services;

import vn.iotstar.entities.User;
import vn.iotstar.entities.UserOtp;
import vn.iotstar.repositories.UserRepository;
import vn.iotstar.repositories.UserOtpRepository;

import java.time.LocalDateTime;
import java.util.Random;

public class OtpService {

    private final UserRepository userRepo   = new UserRepository();
    private final UserOtpRepository otpRepo = new UserOtpRepository();
    private final MailService mailService   = new MailService();

    private String random6() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    // ===== REGISTER (đã chạy OK, giữ nguyên) =====
    public boolean sendRegisterOtp(String email) {
        User u = userRepo.findByEmail(email);
        if (u == null) return false;
        otpRepo.deleteByUserAndPurpose(u, UserOtp.Purpose.REGISTER);

        UserOtp otp = new UserOtp();
        otp.setUser(u);
        otp.setPurpose(UserOtp.Purpose.REGISTER);
        otp.setCode(random6());
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRepo.save(otp);

        mailService.sendOtpEmail(u.getEmail(), otp.getCode(), "Kích hoạt tài khoản UTESHOP");
        return true;
    }

    public boolean resendRegisterOtp(String email) {
        return sendRegisterOtp(email);
    }

    public boolean verifyActivateOtp(String email, String code) {
        User u = userRepo.findByEmail(email);
        if (u == null) return false;

        UserOtp found = otpRepo.findValid(u, UserOtp.Purpose.REGISTER, code, LocalDateTime.now());
        if (found == null) return false;

        u.setIsEmailActive(true);
        userRepo.update(u);
        otpRepo.deleteByUserAndPurpose(u, UserOtp.Purpose.REGISTER);
        return true;
    }

    // ===== RESET PASSWORD (mới thêm) =====
    public boolean sendResetOtp(String email) {
        User u = userRepo.findByEmail(email);
        if (u == null) return false;
        otpRepo.deleteByUserAndPurpose(u, UserOtp.Purpose.RESET);

        UserOtp otp = new UserOtp();
        otp.setUser(u);
        otp.setPurpose(UserOtp.Purpose.RESET);
        otp.setCode(random6());
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRepo.save(otp);

        mailService.sendOtpEmail(u.getEmail(), otp.getCode(), "Đặt lại mật khẩu UTESHOP");
        return true;
    }

    public boolean resendResetOtp(String email) {
        return sendResetOtp(email);
    }

    public boolean verifyResetOtp(String email, String code) {
        User u = userRepo.findByEmail(email);
        if (u == null) return false;

        UserOtp found = otpRepo.findValid(u, UserOtp.Purpose.RESET, code, LocalDateTime.now());
        if (found == null) return false;

        // Không xoá ngay ở đây để phòng user back lại; sẽ dọn ở ResetPasswordServlet sau khi đổi mật khẩu
        return true;
    }
}
