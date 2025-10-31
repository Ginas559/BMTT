package vn.iotstar.services;

import vn.iotstar.DAO.ShoeAttributeDAO;
import vn.iotstar.entities.ShoeAttribute;
import vn.iotstar.entities.Product;

public class ShoeAttributeService {

    private final ShoeAttributeDAO dao = new ShoeAttributeDAO();

    public ShoeAttribute findByProduct(Product product) { return dao.findByProduct(product); }

    // NEW
    public ShoeAttribute findByProductId(Long productId) { return dao.findByProductId(productId); }

    public void saveOrUpdate(ShoeAttribute attr) { dao.saveOrUpdate(attr); }
}
