package cn.kmbeast.service.impl;

import cn.kmbeast.context.LocalThreadHolder;
import cn.kmbeast.mapper.ProductMapper;
import cn.kmbeast.pojo.api.ApiResult;
import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.pojo.dto.query.extend.ProductQueryDto;
import cn.kmbeast.pojo.entity.Product;
import cn.kmbeast.pojo.vo.ProductVO;
import cn.kmbeast.service.ProductService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品类别业务逻辑接口实现类
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductMapper productMapper;

    /**
     * 新增
     *
     * @param product 参数
     * @return Result<String> 后台通用返回封装类
     */
    @Override
    public Result<String> save(Product product) {
        product.setUserId(LocalThreadHolder.getUserId());
        product.setCreateTime(LocalDateTime.now());
        productMapper.save(product);
        return ApiResult.success("Product added successfully");
    }

    /**
     * 修改
     *
     * @param product 参数
     * @return Result<String> 后台通用返回封装类
     */
    @Override
    public Result<String> update(Product product) {
        productMapper.update(product);
        return ApiResult.success("Product modification successful");
    }

    /**
     * 删除
     *
     * @param ids 待删除ID集合
     * @return Result<String> 后台通用返回封装类
     */
    @Override
    public Result<String> batchDelete(List<Integer> ids) {
        productMapper.batchDelete(ids);
        return ApiResult.success("Product successfully deleted");
    }

    /**
     * 查询
     *
     * @param productQueryDto 查询参数
     * @return Result<List < ProductVO>> 后台通用返回封装类
     */
    @Override
    public Result<List<ProductVO>> query(ProductQueryDto productQueryDto) {
        int totalCount = productMapper.queryCount(productQueryDto);
        List<ProductVO> productVOList = productMapper.query(productQueryDto);
        return ApiResult.success(productVOList, totalCount);
    }
}
