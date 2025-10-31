package vn.iotstar.DAO;

import java.util.List;

import vn.iotstar.entities.User;

public interface IUserDao {

	void activateEmail(Long userId);

	User findByPhone(String phone);

	User findByEmail(String email);

	User findById(long id);

	void delete(User user);

	void edit(User user);

	void add(User user);

	int countUsers(String keyword, Boolean banned);

	List<User> searchUsers(String keyword, Boolean banned, int page, int pageSize);

	List<User> users(int page, int size, boolean isBanned);

	void save(User user);

	List<User> getAllVendorValidate(String keyword, int page, int pageSize);

	List<User> getAllVendorValidate();

	
}
