package vn.iotstar.DAO;

import vn.iotstar.entities.Shop;

import java.util.List;

import vn.iotstar.entities.Coupon;

public interface ICouponDao {

	List<Coupon> searchCoupons(String keyword, Shop shop, int page, int pageSize);

	int countCoupons(String keyword, Shop shop);

}
