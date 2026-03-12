package cn.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 题型枚举
 */
@Getter
@AllArgsConstructor
public enum PracticeTypeEnum {

    ONE_SELECTED(1, "Single choice"),
    DOUBLE_SELECTED(2, "Multiple choice"),
    WORD_PUT(3, "Fill in the blanks"),
    JUDGEMENT(4, "True or False");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String detail;

}
