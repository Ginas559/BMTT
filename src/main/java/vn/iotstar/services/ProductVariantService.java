package vn.iotstar.services;

import java.util.*;
import vn.iotstar.DAO.ProductVariantDAO;
import vn.iotstar.entities.Product;
import vn.iotstar.entities.ProductVariant;

public class ProductVariantService {

    private final ProductVariantDAO dao = new ProductVariantDAO();

    public void addVariant(ProductVariant variant) { dao.save(variant); }

    public List<ProductVariant> listByProduct(Product product) { return dao.findByProduct(product); }

    // NEW
    public List<ProductVariant> findByProductId(Long productId) { return dao.findByProductId(productId); }

    // NEW
    public Map<String, List<String>> colorToSizes(Long productId) { return dao.colorToSizes(productId); }

    // NEW
    public Integer stockOf(Long productId, String color, String size) { return dao.stockOf(productId, color, size); }

    // NEW
    public ProductVariant findOne(Long productId, String color, String size) { return dao.findOne(productId, color, size); }

    // NEW
    public Map<String, Long> mapVariantIdByKey(Long productId) { return dao.mapVariantIdByKey(productId); }

    public void deleteVariant(Long variantId) { dao.delete(variantId); }

    /** Dùng ở bảng sản phẩm */
    public int sumStockByProductId(Long productId) { return dao.sumStockByProductId(productId); }
}
