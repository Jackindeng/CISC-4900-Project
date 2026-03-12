package cn.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuditStatusEnum {

    NO_AUDIT(1, "Unreviewed"),
    HAVE_AUDIT(2, "Approved");

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 状态解释
     */
    private final String detail;


}
