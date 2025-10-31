package vn.iotstar.services.admin;

import java.util.List;

import vn.iotstar.DAO.IShopDao;
import vn.iotstar.DAO.Impl.ShopDaoImpl;
import vn.iotstar.entities.Shop;
import vn.iotstar.entities.Shop.ShopStatus;

public class AdminShopService {
	private IShopDao shopDao = new ShopDaoImpl();
	
	public List<Shop> shops(String keyword, ShopStatus banned, int page, int pageSize){
		return shopDao.searchShops( keyword,  banned,  page,  pageSize);
	}
	
	public void add(Shop shop) {
		shopDao.add(shop);
		return;
	}
	
	public void edit(Shop shop) {
		shopDao.edit(shop);
		return;
	}
	public void delete(Shop shop) {
		shopDao.delete(shop);
		return;
	}
	
	public int countShops(String keyword, ShopStatus banned) {
		return shopDao.countShops(keyword, banned);
	}
	
	public Shop findById(Long id) {
		return shopDao.findById(id);
	}
	
	public List<Shop> findAllShopsValidate(){
		return shopDao.findAllShopsValidate();
	}
}
