package cn.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 禁言状态枚举
 */
@Getter
@AllArgsConstructor
public enum WordStatusEnum {

    USE(false, "Available"),
    BANK_USE(true, "Banned");

    /**
     * 状态
     */
    private final Boolean flag;
    /**
     * 名称
     */
    private final String name;

}
