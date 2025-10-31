package vn.iotstar.DAO;

import java.util.List;

import vn.iotstar.entities.ProductComment;

public interface IProductCommentDao {

	List<ProductComment> productComments(Long productId);

}
