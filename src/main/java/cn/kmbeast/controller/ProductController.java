package cn.kmbeast.controller;

import cn.kmbeast.aop.Pager;
import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.pojo.dto.query.extend.ProductQueryDto;
import cn.kmbeast.pojo.entity.Product;
import cn.kmbeast.pojo.vo.ProductVO;
import cn.kmbeast.service.ProductService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Resource
    private ProductService productService;

    /**
     * 新增
     *
     * @param product 参数
     * @return Result<String> 响应结果
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody Product product) {
        return productService.save(product);
    }

    /**
     * 修改
     *
     * @param product 参数
     * @return Result<String> 响应结果
     */
    @PutMapping(value = "/update")
    @ResponseBody
    public Result<String> update(@RequestBody Product product) {
        return productService.update(product);
    }

    /**
     * 批量删除
     */
    @PostMapping(value = "/batchDelete")
    @ResponseBody
    public Result<String> batchDelete(@RequestBody List<Integer> ids) {
        return productService.batchDelete(ids);
    }

    /**
     * 查询
     *
     * @param productQueryDto 查询参数
     * @return Result<List < ProductVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/query")
    @ResponseBody
    public Result<List<ProductVO>> query(@RequestBody ProductQueryDto productQueryDto) {
        return productService.query(productQueryDto);
    }
}