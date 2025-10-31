package vn.iotstar.services.admin;

import java.util.List;

import vn.iotstar.DAO.IProductCommentDao;
import vn.iotstar.DAO.IProductDao;
import vn.iotstar.DAO.IProductReviewDao;
import vn.iotstar.DAO.Impl.ProductCommentDaoImpl;
import vn.iotstar.DAO.Impl.ProductDaoImpl;
import vn.iotstar.DAO.Impl.ProductReviewDaoImpl;
import vn.iotstar.entities.Category;
import vn.iotstar.entities.Product;
import vn.iotstar.entities.ProductComment;
import vn.iotstar.entities.ProductReview;
import vn.iotstar.entities.Shop;

public class AdminProductService {
	
	private IProductDao productDao = new ProductDaoImpl();
	private IProductCommentDao productCommentDao = new ProductCommentDaoImpl();
	private IProductReviewDao productReviewDao = new ProductReviewDaoImpl();
	
	public List<Product> searchProducts(String keyword, Boolean isBanned, Shop shop, Category category, int page, int pageSize){
		return productDao.searchProducts(keyword, isBanned, shop, category, page, pageSize);
	}
	
	public int countProducts(String keyword, Boolean isBanned, Shop shop, Category category) {
		return productDao.countProducts(keyword, isBanned, shop, category);
	}
	
	public Product findById(Long id) {
		return productDao.findById(id);
	}
	
	public void edit(Product product) {
		productDao.edit(product);
		return;
	}
	
	public List<ProductComment> productComments(Long id){
		return productCommentDao.productComments(id);
	}
	
	public List<ProductReview> productReviews(Long id){
		return productReviewDao.productReviews(id);
	}
}
