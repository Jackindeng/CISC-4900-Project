package cn.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InteractionEnum {

    SAVE(1, "Favorite"),
    VIEW(2, "View"),
    LOVE(3, "Want");

    /**
     * 状态
     */
    private final Integer type;
    /**
     * 状态解释
     */
    private final String detail;


}