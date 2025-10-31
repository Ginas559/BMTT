package vn.iotstar.services.admin;

import java.util.List;

import vn.iotstar.DAO.ICouponDao;
import vn.iotstar.DAO.Impl.CouponDaoImpl;
import vn.iotstar.entities.Shop;
import vn.iotstar.entities.Coupon;

public class AdminCouponService {
	private ICouponDao couponDao = new CouponDaoImpl();
	
	public List<Coupon> searchCoupons(String keyword, Shop shop, int page, int pageSize){
		return couponDao.searchCoupons(keyword, shop, page, pageSize);
	}
	
	public int countCoupons(String keyword, Shop shop) {
		return couponDao.countCoupons(keyword, shop);
	}
}
