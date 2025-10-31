package vn.iotstar.DAO;

import java.util.List;

import vn.iotstar.entities.ProductReview;

public interface IProductReviewDao {

	List<ProductReview> productReviews(Long productId);

}
