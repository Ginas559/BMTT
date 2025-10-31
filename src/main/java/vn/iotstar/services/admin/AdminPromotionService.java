package vn.iotstar.services.admin;

import java.util.List;

import vn.iotstar.DAO.ICategoryDao;
import vn.iotstar.DAO.IPromotionDao;
import vn.iotstar.DAO.Impl.PromotionDaoImpl;
import vn.iotstar.entities.Promotion;
import vn.iotstar.entities.Promotion.PromotionStatus;

public class AdminPromotionService {
	private IPromotionDao promotionDao = new PromotionDaoImpl();
	
	public List<Promotion> searchPromotions(String keyword, PromotionStatus status, int page, int pageSize){
		return promotionDao.searchPromotions( keyword,  status,  page,  pageSize);
	}
	
	public void add(Promotion promotion) {
		promotionDao.add(promotion);
		return;
	}
	
	public void edit(Promotion promotion) {
		promotionDao.edit(promotion);
		return;
	}
	public void delete(Promotion promotion) {
		promotionDao.delete(promotion);
		return;
	}
	
	public int countPromotions(String keyword, PromotionStatus status) {
		return promotionDao.countPromotions(keyword, status);
	}
	
	public Promotion findById(Long id) {
		return promotionDao.findById(id);
	}
	
	
}
