package com.openvpp.app.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 贯穿案例持久化仓库 —— 教学用 JdbcTemplate + H2。
 * response_id 是任务→指令→基线→账单的统一关联标识。
 * 生产形态：替换为 MyBatis/JPA + MySQL，表结构不变（schema.sql 同构）。
 */
@Repository
public class ResponseRepository {

    private final JdbcTemplate jdbc;

    public ResponseRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---------- 任务 ----------

    public void saveTask(String responseId, String eventId, BigDecimal declaredKwh,
                         BigDecimal targetKw, long windowStart, long windowEnd,
                         String state, BigDecimal gapKw) {
        jdbc.update("MERGE INTO dr_task KEY(response_id) VALUES(?,?,?,?,?,?,?,?,?)",
                responseId, eventId, declaredKwh, targetKw, windowStart, windowEnd,
                state, gapKw, System.currentTimeMillis());
    }

    public void updateTaskState(String responseId, String state) {
        jdbc.update("UPDATE dr_task SET state=? WHERE response_id=?", state, responseId);
    }

    public boolean taskExists(String responseId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM dr_task WHERE response_id=?", Integer.class, responseId);
        return n != null && n > 0;
    }

    public List<Map<String, Object>> listTasks() {
        return jdbc.queryForList("SELECT * FROM dr_task ORDER BY created_ms DESC");
    }

    // ---------- 指令 ----------

    public void saveInstruction(String instructionId, String responseId, String resourceId,
                                BigDecimal commandKw, String state, Long sentMs, Long reachedMs) {
        jdbc.update("MERGE INTO dispatch_instruction KEY(instruction_id) VALUES(?,?,?,?,?,?,?)",
                instructionId, responseId, resourceId, commandKw, state, sentMs, reachedMs);
    }

    public void updateInstructionState(String instructionId, String state, Long reachedMs) {
        jdbc.update("UPDATE dispatch_instruction SET state=?, reached_ms=? WHERE instruction_id=?",
                state, reachedMs, instructionId);
    }

    public List<Map<String, Object>> listInstructions(String responseId) {
        return responseId == null
                ? jdbc.queryForList("SELECT * FROM dispatch_instruction ORDER BY sent_ms DESC")
                : jdbc.queryForList("SELECT * FROM dispatch_instruction WHERE response_id=? ORDER BY sent_ms", responseId);
    }

    // ---------- 基线 ----------

    public void saveBaselinePoint(String responseId, int pointIndex, String ruleVersion,
                                  BigDecimal baselineKw, BigDecimal actualKw) {
        jdbc.update("MERGE INTO baseline_record KEY(response_id, point_index) VALUES(?,?,?,?,?,?)",
                responseId, pointIndex, ruleVersion, baselineKw, actualKw, System.currentTimeMillis());
    }

    public List<Map<String, Object>> listBaselines(String responseId) {
        return jdbc.queryForList(
                "SELECT * FROM baseline_record WHERE response_id=? ORDER BY point_index", responseId);
    }

    // ---------- 账单 ----------

    public void saveBill(String responseId, String subject, BigDecimal amountYuan,
                         String billType, String memo) {
        jdbc.update("MERGE INTO bill KEY(response_id, subject, bill_type) VALUES(?,?,?,?,?,?)",
                responseId, subject, amountYuan, billType, memo, System.currentTimeMillis());
    }

    public boolean billExists(String responseId, String subject, String billType) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bill WHERE response_id=? AND subject=? AND bill_type=?",
                Integer.class, responseId, subject, billType);
        return n != null && n > 0;
    }

    public List<Map<String, Object>> listBills(String responseId) {
        return responseId == null
                ? jdbc.queryForList("SELECT * FROM bill ORDER BY created_ms DESC")
                : jdbc.queryForList("SELECT * FROM bill WHERE response_id=? ORDER BY subject, bill_type", responseId);
    }

    /** 资金守恒核对：某响应下全部账单之和（应等于平台实收） */
    public BigDecimal billSum(String responseId) {
        BigDecimal sum = jdbc.queryForObject(
                "SELECT COALESCE(SUM(amount_yuan),0) FROM bill WHERE response_id=?",
                BigDecimal.class, responseId);
        return sum == null ? BigDecimal.ZERO : sum;
    }

    /** 清理全部演示数据（重置用），返回删除的总行数 */
    public int deleteAll() {
        int n = jdbc.update("DELETE FROM bill");
        n += jdbc.update("DELETE FROM baseline_record");
        n += jdbc.update("DELETE FROM dispatch_instruction");
        n += jdbc.update("DELETE FROM dr_task");
        return n;
    }

    /** 分配侧合计（SHARE + PLATFORM_CUT），守恒核对用：应等于收入侧 SETTLE */
    public BigDecimal allocationSum(String responseId) {
        BigDecimal sum = jdbc.queryForObject(
                "SELECT COALESCE(SUM(amount_yuan),0) FROM bill WHERE response_id=? AND bill_type<>'SETTLE'",
                BigDecimal.class, responseId);
        return sum == null ? BigDecimal.ZERO : sum;
    }

    /** 收入侧实收（SETTLE） */
    public BigDecimal settleAmount(String responseId) {
        BigDecimal v = jdbc.queryForObject(
                "SELECT amount_yuan FROM bill WHERE response_id=? AND subject='PLATFORM' AND bill_type='SETTLE'",
                BigDecimal.class, responseId);
        return v == null ? BigDecimal.ZERO : v;
    }
}
