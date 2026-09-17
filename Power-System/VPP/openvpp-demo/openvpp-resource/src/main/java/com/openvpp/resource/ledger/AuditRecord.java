package com.openvpp.resource.ledger;

import java.time.LocalDateTime;

/**
 * 档案变更审计记录 —— 台账的每一次建档/变更/注销都留痕。
 * 审计是结算争议时的裁判证据：申报容量随档案走，谁改了容量、
 * 何时改的、从多少改成多少，必须可追溯（47241 运行管理要求）。
 */
public class AuditRecord {

    public enum Operation {
        CREATE, UPDATE, RETIRE
    }

    private final String resourceId;
    private final Operation operation;
    private final String field;
    private final String beforeValue;
    private final String afterValue;
    private final LocalDateTime at;

    public AuditRecord(String resourceId, Operation operation, String field,
                       String beforeValue, String afterValue) {
        this.resourceId = resourceId;
        this.operation = operation;
        this.field = field;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.at = LocalDateTime.now();
    }

    public String getResourceId() {
        return resourceId;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getField() {
        return field;
    }

    public String getBeforeValue() {
        return beforeValue;
    }

    public String getAfterValue() {
        return afterValue;
    }

    public LocalDateTime getAt() {
        return at;
    }
}
