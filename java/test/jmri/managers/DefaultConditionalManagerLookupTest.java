package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Map;

import jmri.Conditional;
import jmri.ConditionalManager;
import jmri.InstanceManager;
import jmri.Logix;
import jmri.LogixManager;
import jmri.implementation.DefaultConditional;
import jmri.implementation.DefaultLogix;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Check how DefaultConditionalManager finds the parent Logix of a
 * Conditional, for standard and non-standard Conditional system names,
 * including after the Logixes have changed.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultConditionalManagerLookupTest {

    private ConditionalManager cm;
    private LogixManager lm;

    private Logix addLogix(String systemName, String... conditionals) {
        Logix x = new DefaultLogix(systemName, cm);
        for (String c : conditionals) {
            x.addConditional(c, 0);
            x.addConditional(c, new DefaultConditional(c));
        }
        lm.register(x);
        return x;
    }

    @Test
    public void testStandardNames() {
        Logix ix1 = addLogix("IX1", "IX1C1", "IX1C2");
        Logix rtx = addLogix("RTX123", "RTX1231T", "RTX1231F");
        Logix rtx2 = addLogix("RTX12", "RTX1210A");

        assertSame(ix1, cm.getParentLogix("IX1C2"));
        assertSame(rtx, cm.getParentLogix("RTX1231T"));
        assertSame(rtx2, cm.getParentLogix("RTX1210A"));
        assertSame(ix1.getConditional("IX1C1"), cm.getBySystemName("IX1C1"));
        assertNull(cm.getParentLogix("IX9C1"), "no such Logix");
    }

    @Test
    public void testNonStandardNames() {
        addLogix("IX1", "IX1C1");
        Logix sys = addLogix("IX:SYS", "IX:SYS_SGC_GroupA", "IX:SYS_SGC_GroupB");

        for (int i = 0; i < 3; i++) {
            assertSame(sys, cm.getParentLogix("IX:SYS_SGC_GroupB"));
            Conditional c = cm.getBySystemName("IX:SYS_SGC_GroupA");
            assertSame(sys.getConditional("IX:SYS_SGC_GroupA"), c);
        }
        assertNull(cm.getParentLogix("IX:SYS_SGC_GroupC"), "no such Conditional");
    }

    @Test
    public void testNonStandardNameAfterChanges() {
        Logix first = addLogix("IX:FIRST", "IX:ODD_NAME");
        assertSame(first, cm.getParentLogix("IX:ODD_NAME"));

        // the Conditional is deleted from its Logix
        first.deleteConditional("IX:ODD_NAME");
        assertNull(cm.getParentLogix("IX:ODD_NAME"), "deleted Conditional has no parent");

        // a Logix holding a Conditional with that name is created again
        Logix second = addLogix("IX:SECOND", "IX:ODD_NAME");
        assertSame(second, cm.getParentLogix("IX:ODD_NAME"));

        // that Logix is replaced by another one with the same system name
        lm.deregister(second);
        Logix third = addLogix("IX:SECOND", "IX:ODD_NAME");
        assertSame(third, cm.getParentLogix("IX:ODD_NAME"));

        // the Logix is deleted
        lm.deregister(third);
        assertNull(cm.getParentLogix("IX:ODD_NAME"), "deleted Logix is not found");
    }

    @Test
    public void testDeletedLogixIsForgotten() {
        Logix keep = addLogix("IX:KEEP", "IX:KEEP_ONE");
        Logix gone = addLogix("IX:GONE", "IX:GONE_ONE", "IX:GONE_TWO");
        assertSame(keep, cm.getParentLogix("IX:KEEP_ONE"));
        assertSame(gone, cm.getParentLogix("IX:GONE_ONE"));
        assertSame(gone, cm.getParentLogix("IX:GONE_TWO"));

        lm.deleteLogix(gone);
        Map<String, Logix> remembered = ((DefaultConditionalManager) cm).nonStandardParents;
        assertFalse(remembered.containsValue(gone), "deleted Logix is not kept");
        assertSame(keep, remembered.get("IX:KEEP_ONE"), "other Logixes are still remembered");
        assertNull(cm.getParentLogix("IX:GONE_ONE"), "deleted Logix is not found");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initConditionalManager();
        cm = InstanceManager.getDefault(ConditionalManager.class);
        lm = InstanceManager.getDefault(LogixManager.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
