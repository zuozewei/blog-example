-- 园区需求响应贯穿案例：核心业务结果持久化（教学用 H2 文件库）
-- 关联标识：response_id 贯穿 任务→指令→基线→账单 四表，是争议核查与追溯的锚点。
-- 生产形态：MySQL/PostgreSQL 同构建表，见第 25 篇。

CREATE TABLE IF NOT EXISTS dr_task (
    response_id     VARCHAR(64) PRIMARY KEY,
    event_id        VARCHAR(64) NOT NULL,
    declared_kwh    DECIMAL(14,3),
    target_kw       DECIMAL(14,3),
    window_start    BIGINT,
    window_end      BIGINT,
    state           VARCHAR(16) NOT NULL,
    gap_kw          DECIMAL(14,3),
    created_ms      BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS dispatch_instruction (
    instruction_id  VARCHAR(64) PRIMARY KEY,
    response_id     VARCHAR(64) NOT NULL,
    resource_id     VARCHAR(64) NOT NULL,
    command_kw      DECIMAL(14,3),
    state           VARCHAR(16) NOT NULL,
    sent_ms         BIGINT,
    reached_ms      BIGINT
);

CREATE TABLE IF NOT EXISTS baseline_record (
    response_id     VARCHAR(64) NOT NULL,
    point_index     INT NOT NULL,
    rule_version    VARCHAR(32) NOT NULL,
    baseline_kw     DECIMAL(14,3) NOT NULL,
    actual_kw       DECIMAL(14,3),
    computed_ms     BIGINT NOT NULL,
    PRIMARY KEY (response_id, point_index)
);

CREATE TABLE IF NOT EXISTS bill (
    response_id     VARCHAR(64) NOT NULL,
    subject         VARCHAR(64) NOT NULL,   -- 'PLATFORM' 或 用户名
    amount_yuan     DECIMAL(14,2) NOT NULL,
    bill_type       VARCHAR(16) NOT NULL,   -- SETTLE / GUARANTEE / SHARE / PLATFORM_CUT / CORRECTION
    memo            VARCHAR(255),
    created_ms      BIGINT NOT NULL,
    PRIMARY KEY (response_id, subject, bill_type)
);
