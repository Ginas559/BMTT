package vn.iotstar.services.admin;

import java.util.List;


import vn.iotstar.DAO.ICategoryDao;
import vn.iotstar.DAO.Impl.CategoryDaoImpl;
import vn.iotstar.entities.Category;

public class AdminCategoryService {
	private ICategoryDao categoryDao = new CategoryDaoImpl();
	
	public List<Category> categories(String keyword, Boolean banned, int page, int pageSize){
		return categoryDao.searchCategories( keyword,  banned,  page,  pageSize);
	}
	
	public void add(Category category) {
		categoryDao.add(category);
		return;
	}
	
	public void edit(Category category) {
		categoryDao.edit(category);
		return;
	}
	public void delete(Category category) {
		categoryDao.delete(category);
		return;
	}
	
	public int countCategories(String keyword, Boolean banned) {
		return categoryDao.countCategories(keyword, banned);
	}
	
	public Category findById(Long id) {
		return categoryDao.findById(id);
	}
	
	public List<Category> findAllCategoriesValidate(){
		return categoryDao.findAllCategoriesValidate();
	}
}
