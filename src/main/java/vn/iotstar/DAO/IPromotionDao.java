package vn.iotstar.DAO;

import java.util.List;

import vn.iotstar.entities.Promotion;
import vn.iotstar.entities.Promotion.PromotionStatus;

public interface IPromotionDao {

	Promotion findById(Long id);

	List<Promotion> searchPromotions(String keyword, PromotionStatus status, int page, int pageSize);

	int countPromotions(String keyword, PromotionStatus status);

	void add(Promotion promotion);

	void edit(Promotion promotion);

	void delete(Promotion promotion);

}
