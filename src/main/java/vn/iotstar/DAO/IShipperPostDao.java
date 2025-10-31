package vn.iotstar.DAO;

import java.util.List;

import vn.iotstar.entities.ShipperPost;

public interface IShipperPostDao {

	void addPost(ShipperPost post);

	List<ShipperPost> shipperPosts(int page, int pageSize);

	int countShipperPosts();

}
