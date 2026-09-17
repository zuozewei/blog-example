package com.openvpp.resource;

import com.openvpp.common.enums.ResourceType;
import com.openvpp.resource.ledger.AuditRecord;
import com.openvpp.resource.ledger.AuditTrail;
import com.openvpp.resource.ledger.ResourceLedgerService;
import com.openvpp.resource.profile.AcLoadResourceProfile;
import com.openvpp.resource.profile.EvChargerResourceProfile;
import com.openvpp.resource.profile.PvResourceProfile;
import com.openvpp.resource.profile.ResourceProfile;
import com.openvpp.resource.profile.StorageResourceProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 资源台账全场景单测：
 * 四类建档 / 排他冲突 / 期限校验 / 容量变更审计 / 注销留痕 / 类型位置查询。
 */
class ResourceLedgerServiceTest {

    private AuditTrail auditTrail;
    private ResourceLedgerService ledger;

    @BeforeEach
    void setUp() {
        auditTrail = new AuditTrail();
        ledger = new ResourceLedgerService(auditTrail);
    }

    private static ResourceProfile withContract(ResourceProfile p, String id, String geo, String node) {
        p.setResourceId(id);
        p.setCapacity(new BigDecimal("1000"));
        p.setGeoLocation(geo);
        p.setElectricalNode(node);
        p.setContractEnd(LocalDate.now().plusYears(1));
        return p;
    }

    @Test
    void 四类资源建档入台账() {
        ledger.enroll(withContract(new PvResourceProfile(), "pv-001", "华南/广州", "10kV/馈线A1"), "acct-pv");
        ledger.enroll(withContract(new StorageResourceProfile(), "es-001", "华南/广州", "10kV/馈线A2"), "acct-es");
        ledger.enroll(withContract(new AcLoadResourceProfile(), "ac-001", "华东/上海", "35kV/馈线B1"), "acct-ac");
        ledger.enroll(withContract(new EvChargerResourceProfile(), "ev-001", "华东/上海", "35kV/馈线B2"), "acct-ev");

        assertEquals(4, ledger.query(null, null).size());
        assertEquals(ResourceType.DG, ledger.require("pv-001").getType());
        assertEquals(ResourceType.ES, ledger.require("es-001").getType());
        assertEquals(ResourceType.FL, ledger.require("ac-001").getType());
        assertEquals(4, auditTrail.totalCount(), "每次建档都应有审计");
    }

    @Test
    void 排他性冲突拒绝建档() {
        ledger.enroll(withContract(new StorageResourceProfile(), "es-001", "华南/广州", "10kV/馈线A1"), "acct-x");

        StorageResourceProfile dup = new StorageResourceProfile();
        withContract(dup, "es-999", "华南/广州", "10kV/馈线A1");   // 同一地理+电气位置

        assertThrows(IllegalStateException.class,
                () -> ledger.enroll(dup, "acct-x"), "同位置重复建档必须被拦截");
    }

    @Test
    void 代理期限不足1个月拒绝建档() {
        StorageResourceProfile shortTerm = new StorageResourceProfile();
        shortTerm.setResourceId("es-short");
        shortTerm.setCapacity(new BigDecimal("500"));
        shortTerm.setGeoLocation("华南/深圳");
        shortTerm.setElectricalNode("10kV/馈线C1");
        shortTerm.setContractEnd(LocalDate.now().plusDays(15));   // 仅 15 天

        assertThrows(IllegalArgumentException.class,
                () -> ledger.enroll(shortTerm, "acct-short"), "代理期限 <1 个月必须拒绝");
    }

    @Test
    void 容量变更留前后值审计() {
        ledger.enroll(withContract(new StorageResourceProfile(), "es-001", "华南/广州", "10kV/馈线A2"), "acct-es");
        ledger.updateCapacity("es-001", new BigDecimal("2000"));

        List<AuditRecord> history = auditTrail.historyOf("es-001");
        assertEquals(2, history.size());
        AuditRecord update = history.get(1);
        assertEquals(AuditRecord.Operation.UPDATE, update.getOperation());
        assertEquals("capacity", update.getField());
        assertEquals("1000", update.getBeforeValue());
        assertEquals("2000", update.getAfterValue());
        assertEquals(new BigDecimal("2000"), ledger.require("es-001").getCapacity());
    }

    @Test
    void 注销留痕且台账移除() {
        ledger.enroll(withContract(new PvResourceProfile(), "pv-001", "华南/广州", "10kV/馈线A1"), "acct-pv");
        ledger.retire("pv-001");

        assertThrows(IllegalArgumentException.class, () -> ledger.require("pv-001"));
        List<AuditRecord> history = auditTrail.historyOf("pv-001");
        assertEquals(2, history.size());
        assertEquals(AuditRecord.Operation.RETIRE, history.get(1).getOperation());
    }

    @Test
    void 按类型与电气位置查询支撑聚合取数() {
        ledger.enroll(withContract(new StorageResourceProfile(), "es-001", "华南/广州", "10kV/馈线A1"), "a1");
        ledger.enroll(withContract(new StorageResourceProfile(), "es-002", "华南/广州", "10kV/馈线A2"), "a2");
        ledger.enroll(withContract(new StorageResourceProfile(), "es-003", "华北/北京", "110kV/馈线C1"), "a3");
        ledger.enroll(withContract(new PvResourceProfile(), "pv-001", "华南/广州", "10kV/馈线A3"), "a4");

        assertEquals(3, ledger.query(ResourceType.ES, null).size());
        assertEquals(2, ledger.query(ResourceType.ES, "10kV/").size(), "按电压等级过滤失效");
        assertEquals(new BigDecimal("2000"),
                ledger.totalCapacityKw(ResourceType.ES).subtract(new BigDecimal("1000")),
                "总容量统计口径错误");
    }
}
