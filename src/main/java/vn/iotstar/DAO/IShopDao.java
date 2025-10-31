package vn.iotstar.DAO;

import java.util.List;

import vn.iotstar.entities.Shop;
import vn.iotstar.entities.Shop.ShopStatus;

public interface IShopDao {

	Shop findById(Long id);

	List<Shop> searchShops(String keyword, ShopStatus banned, int page, int pageSize);

	int countShops(String keyword, ShopStatus banned);

	void add(Shop shop);

	void edit(Shop shop);

	void delete(Shop shop);

	List<Shop> findAllShopsValidate();

}
