package vn.iotstar.DAO;

import java.util.List;

import vn.iotstar.entities.Category;

public interface ICategoryDao {

	void delete(Category category);

	void edit(Category category);

	void add(Category category);

	int countCategories(String keyword, Boolean banned);

	List<Category> searchCategories(String keyword, Boolean banned, int page, int pageSize);

	Category findById(Long id);

	List<Category> findAllCategoriesValidate();
}
