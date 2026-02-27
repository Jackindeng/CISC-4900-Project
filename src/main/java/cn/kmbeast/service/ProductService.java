package cn.kmbeast.service;

import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.pojo.dto.query.extend.ProductQueryDto;
import cn.kmbeast.pojo.entity.Product;
import cn.kmbeast.pojo.vo.ProductVO;

import java.util.List;

/**
 * 商品的业务逻辑接口
 */
public interface ProductService {

    Result<String> save(Product product);

    Result<String> update(Product product);

    Result<String> batchDelete(List<Integer> ids);

    Result<List<ProductVO>> query(ProductQueryDto productQueryDto);

}