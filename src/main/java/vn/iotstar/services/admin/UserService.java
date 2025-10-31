package vn.iotstar.services.admin;

import java.util.List;

import vn.iotstar.DAO.Impl.UserDaoImpl;
import vn.iotstar.entities.User;

public class UserService {
	UserDaoImpl userDaoImpl = new UserDaoImpl();
	
	public List<User> users(int page, int size, boolean isBanned){
		List<User> listusers = userDaoImpl.users(page, size, isBanned);
		
		
		return listusers;
	}
	
	
	public List<User> searchUsers(String keyword, Boolean banned, int page, int pageSize) {
        return userDaoImpl.searchUsers(keyword, banned, page, pageSize);
    }

    public int countUsers(String keyword, Boolean banned) {
        return userDaoImpl.countUsers(keyword, banned);
    }
    
    public List<User> getAllVendorsValidate(){
    		return userDaoImpl.getAllVendorValidate();
    }
    
	
	
	public void add (User user) {
		this.checkEmail(user.getEmail());
		this.checkPhone(user.getPhone());
		userDaoImpl.add(user);
		return;
		
	}
	public void delete (User user) {
		userDaoImpl.delete(user);
		return;
	}
	public void edit (User user) {
		userDaoImpl.edit(user);
		return;
	}
	public void checkEmail(String email) {
	    if (email == null || email.isBlank()) {
	        throw new IllegalArgumentException("Email không được để trống");
	    }

	    User existing = userDaoImpl.findByEmail(email.trim().toLowerCase());
	    if (existing != null) {
	        throw new IllegalArgumentException("Email đã được sử dụng!");
	    }
	}

	public void checkPhone(String phone) {
	    if (phone == null || phone.isBlank()) {
	        throw new IllegalArgumentException("Số điện thoại không được để trống");
	    }

	    User existing = userDaoImpl.findByPhone(phone.trim());
	    if (existing != null) {
	        throw new IllegalArgumentException("Số điện thoại đã được sử dụng!");
	    }
	}
	
	public User findById(Long id) {
		return userDaoImpl.findById(id);
	}

	
}
