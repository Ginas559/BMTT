package vn.iotstar.DAO;

import java.util.List;

import vn.iotstar.entities.Category;
import vn.iotstar.entities.Product;
import vn.iotstar.entities.Shop;

public interface IProductDao {
	
	void delete(Product product);

	void edit(Product product);

	void add(Product product);

	int countProducts(String keyword, Boolean isBanned, Shop shop, Category category);

	List<Product> searchProducts(String keyword, Boolean isBanned, Shop shop, Category category, int page, int pageSize);

	Product findById(Long id);
}
