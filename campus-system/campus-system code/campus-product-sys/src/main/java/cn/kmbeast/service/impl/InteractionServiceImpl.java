package cn.kmbeast.service.impl;

import cn.kmbeast.context.LocalThreadHolder;
import cn.kmbeast.mapper.InteractionMapper;
import cn.kmbeast.mapper.MessageMapper;
import cn.kmbeast.mapper.ProductMapper;
import cn.kmbeast.mapper.UserMapper;
import cn.kmbeast.pojo.api.ApiResult;
import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.pojo.dto.query.extend.InteractionQueryDto;
import cn.kmbeast.pojo.dto.query.extend.ProductQueryDto;
import cn.kmbeast.pojo.em.InteractionEnum;
import cn.kmbeast.pojo.entity.Interaction;
import cn.kmbeast.pojo.entity.Message;
import cn.kmbeast.pojo.entity.User;
import cn.kmbeast.pojo.vo.ProductVO;
import cn.kmbeast.service.InteractionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 互动行为业务逻辑接口实现类
 */
@Service
public class InteractionServiceImpl implements InteractionService {

    @Resource
    private InteractionMapper interactionMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private UserMapper userMapper;

    /**
     * "我想要"操作
     *
     * @param productId 商品ID
     * @return Result<String> 后台通用返回封装类
     */
    @Override
    public Result<String> likeProduct(Integer productId) {
        // 用户不能重复去"想要"商品
        InteractionQueryDto interactionQueryDto
                = createInteractionQueryDto(productId, InteractionEnum.LOVE.getType());
        int count = interactionMapper.queryCount(interactionQueryDto);
        if (count != 0) {
            return ApiResult.error("Please do not repeat this action");
        }
        ProductQueryDto productQueryDto = new ProductQueryDto();
        productQueryDto.setId(productId);
        List<ProductVO> productVOS = productMapper.query(productQueryDto);
        // 如果商品信息不存在，直接返回
        if (productVOS.isEmpty()) {
            return ApiResult.error("Failed to retrieve product information");
        }
        ProductVO productVO = productVOS.get(0);
        // 用户自己感兴趣自己的商品，明显不合理，所以如果商品是自己的商品，不做处理了
        if (Objects.equals(productVO.getUserId(), LocalThreadHolder.getUserId())) {
            return ApiResult.error("You cannot show interest in your own product!");
        }
        // 取得发布者用户ID
        Integer publisherId = productVO.getUserId();
        Integer userId = LocalThreadHolder.getUserId();
        User user = new User();
        user.setId(userId);
        User operator = userMapper.getByActive(user);
        // 查询用户信息
        Message message = new Message();
        message.setUserId(publisherId);
        // 消息设置为未读
        message.setIsRead(false);
        message.setCreateTime(LocalDateTime.now());
        message.setContent("User [" + operator.getUserName() + "] is interested in your [" + productVO.getName() + "]!");
        messageMapper.save(message);
        // 设置上互动信息
        Interaction interaction = createInteraction(productId, InteractionEnum.LOVE.getType());
        interactionMapper.save(interaction);
        return ApiResult.success("The seller has noticed your interest. Place your order now!");
    }

    /**
     * 新增
     *
     * @param interaction 参数
     * @return Result<String> 后台通用返回封装类
     */
    @Override
    public Result<String> save(Interaction interaction) {
        interactionMapper.save(interaction);
        return ApiResult.success("Interaction recorded successfully");
    }

    /**
     * 删除
     *
     * @param ids 待删除ID集合
     * @return Result<String> 后台通用返回封装类
     */
    @Override
    public Result<String> batchDelete(List<Integer> ids) {
        interactionMapper.batchDelete(ids);
        return ApiResult.success("Interaction deleted successfully");
    }

    /**
     * 收藏操作 （取消收藏与收藏是一组对立的操作）
     *
     * @param productId 商品ID
     * @return Result<Boolean> 后台通用返回封装类
     */
    @Override
    public Result<Boolean> saveOperation(Integer productId) {
        InteractionQueryDto interactionQueryDto =
                createInteractionQueryDto(productId, InteractionEnum.SAVE.getType());
        List<Interaction> interactionList = interactionMapper.query(interactionQueryDto);
        if (interactionList.isEmpty()) { // 对应收藏
            Interaction interaction = createInteraction(productId, InteractionEnum.SAVE.getType());
            interactionMapper.save(interaction);
        } else {
            // 对应取消收藏
            List<Integer> interactionIds = interactionList.stream().map(Interaction::getId)
                    .collect(Collectors.toList());
            interactionMapper.batchDelete(interactionIds);
        }
        return ApiResult.success(interactionList.isEmpty() ? "Added to favorites successfully" : "Removed from favorites successfully", interactionList.isEmpty());
    }

    /**
     * 创建互动信息行为查询条件实体
     *
     * @param productId 商品ID
     * @param type      行为类型
     * @return Interaction
     */
    private InteractionQueryDto createInteractionQueryDto(Integer productId, Integer type) {
        InteractionQueryDto queryDto = new InteractionQueryDto();
        queryDto.setUserId(LocalThreadHolder.getUserId());
        queryDto.setType(type);
        queryDto.setProductId(productId);
        return queryDto;
    }

    /**
     * 创建互动信息行为实体
     *
     * @param productId 商品ID
     * @param type      行为类型
     * @return Interaction
     */
    private Interaction createInteraction(Integer productId, Integer type) {
        Interaction interaction = new Interaction();
        interaction.setUserId(LocalThreadHolder.getUserId());
        interaction.setType(type);
        interaction.setProductId(productId);
        interaction.setCreateTime(LocalDateTime.now());
        return interaction;
    }

    /**
     * 查询
     *
     * @param interactionQueryDto 查询参数
     * @return Result<List < Interaction>> 后台通用返回封装类
     */
    @Override
    public Result<List<Interaction>> query(InteractionQueryDto interactionQueryDto) {
        int totalCount = interactionMapper.queryCount(interactionQueryDto);
        List<Interaction> interactionList = interactionMapper.query(interactionQueryDto);
        return ApiResult.success(interactionList, totalCount);
    }

    /**
     * 查询用户自己收藏的商品
     *
     * @return Result<List < Interaction>> 响应结果
     */
    @Override
    public Result<List<ProductVO>> queryUser() {
        InteractionQueryDto interactionQueryDto = new InteractionQueryDto();
        interactionQueryDto.setUserId(LocalThreadHolder.getUserId());
        interactionQueryDto.setType(InteractionEnum.SAVE.getType());
        List<Interaction> interactionList = interactionMapper.query(interactionQueryDto);

        List<Integer> productIds = interactionList.stream()
                .map(Interaction::getProductId)
                .collect(Collectors.toList());

        // 如果用户没有收藏任何商品，直接返回空列表，避免 SQL 出现 WHERE p.id IN
        if (productIds == null || productIds.isEmpty()) {
            return ApiResult.success(new ArrayList<>());
        }

        // 通过商品的ID列表，查询用户收藏的这些商品返回
        List<ProductVO> productVOS = productMapper.queryProductList(productIds);
        return ApiResult.success(productVOS);
    }

    /**
     * 记录用户对于商品的浏览行为
     *
     * @param productId 商品ID
     * @return Result<Void> 响应结果
     */
    @Override
    public Result<Void> view(Integer productId) {
        InteractionQueryDto interactionQueryDto
                = createInteractionQueryDto(productId, InteractionEnum.VIEW.getType());
        List<Interaction> interactionList = interactionMapper.query(interactionQueryDto);
        // 没浏览过才需要记录
        if (interactionList.isEmpty()) {
            Interaction interaction = createInteraction(productId, InteractionEnum.VIEW.getType());
            interactionMapper.save(interaction);
        }
        return ApiResult.success();
    }

    /**
     * 查询用户自己浏览过的商品
     *
     * @return Result<List < ProductVO>> 响应结果
     */
    @Override
    public Result<List<ProductVO>> myView() {
        InteractionQueryDto interactionQueryDto = new InteractionQueryDto();
        interactionQueryDto.setUserId(LocalThreadHolder.getUserId());
        interactionQueryDto.setType(InteractionEnum.VIEW.getType());
        List<Interaction> interactionList = interactionMapper.query(interactionQueryDto);
        List<Integer> productIds = interactionList.stream()
                .map(Interaction::getProductId).collect(Collectors.toList());
        if (productIds.isEmpty()) {
            return ApiResult.success(new ArrayList<>());
        }
        // 通过商品的ID列表，查询用户浏览过的这些商品返回
        List<ProductVO> productVOS = productMapper.queryProductList(productIds);
        return ApiResult.success(productVOS);
    }

    /**
     * 用户删除自己的浏览记录
     */
    @Override
    public Result<String> batchDeleteInteraction() {
        InteractionQueryDto interactionQueryDto = new InteractionQueryDto();
        interactionQueryDto.setUserId(LocalThreadHolder.getUserId());
        interactionQueryDto.setType(InteractionEnum.VIEW.getType());
        List<Interaction> interactionList = interactionMapper.query(interactionQueryDto);
        List<Integer> ids = interactionList.stream()
                .map(Interaction::getId).collect(Collectors.toList());
        interactionMapper.batchDelete(ids);
        return ApiResult.success();
    }
}