package vn.iotstar.DAO;

import vn.iotstar.entities.User;
import vn.iotstar.entities.UserVerification;

public interface IUserVerificationDao {
	void save (UserVerification userVerification);
	UserVerification findValidOTP(User user, String otp, UserVerification.VerificationType type);
	
}
