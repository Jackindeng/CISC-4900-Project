package cn.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录状态枚举
 */
@Getter
@AllArgsConstructor
public enum LoginStatusEnum {

    USE(false, "Login available"),
    BANK_USE(true, "Login status error");

    /**
     * 编码
     */
    private final Boolean flag;
    /**
     * 名成
     */
    private final String name;

}
