package cn.kmbeast.pojo.vo;

import cn.kmbeast.pojo.entity.Interaction;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 互动行为的出参VO类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InteractionVO extends Interaction {
    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 商品名
     */
    private String productTitle;

    /**
     * 商品类别名
     */
    private String categoryName;

    /**
     * 想要人数
     */
    private Integer likeNumber;

    /**
     * 收藏人数
     */
    private Integer saveNumber;

    /**
     * 浏览人数
     */
    private Integer viewNumber;
}