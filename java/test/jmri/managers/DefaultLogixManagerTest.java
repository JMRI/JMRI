package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.Logix;
import jmri.LogixManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

/**
 * Tests for the jmri.managers.DefaultLogixManager class.
 *
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class DefaultLogixManagerTest extends AbstractManagerTestBase<LogixManager,Logix> {

    @Test
    public void testCtor() {
        assertNotNull( l, "exists");
    }

    @Test
    public void testCreateForms() {
        LogixManager m = l;

        Logix l1 = m.createNewLogix("User name 1");
        Logix l2 = m.createNewLogix("User name 2");

        assertNotNull(m.getByUserName("User name 1"));
        assertNotNull(m.getByUserName("User name 2"));

        assertNotSame(l1, l2);
        assertFalse( l1.equals(l2));

        assertNotNull( m.getBySystemName(l1.getSystemName()));
        assertNotNull( m.getBySystemName(l2.getSystemName()));

        Logix l3 = m.createNewLogix("IX03", "User name 3");

        assertNotSame( l1, l3);
        assertNotSame( l2, l3);
        assertFalse( l1.equals(l3));
        assertFalse( l2.equals(l3));

        // test of some fails
        assertNull(m.createNewLogix(l1.getUserName()));
        assertNull(m.createNewLogix(l1.getSystemName(),""));
    }

    @Test
    public void testEmptyUserName() {
        LogixManager m = l;

        Logix l1 = m.createNewLogix("IX01", "");
        Logix l2 = m.createNewLogix("IX02", "");

        assertNotSame( l1, l2);
        assertFalse( l1.equals(l2));

        assertNotNull(m.getBySystemName(l1.getSystemName()));
        assertNotNull(m.getBySystemName(l2.getSystemName()));

        m.createNewLogix("IX03", "User name 3");

        // test of some fails
        assertNull(m.createNewLogix(l1.getSystemName(),""));
    }

    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}

    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithPrefixNotASystemName() {}

    /**
     * A Logix that records whether it was activated.
     */
    private static class RecordingLogix extends jmri.implementation.DefaultLogix {
        boolean activated = false;

        RecordingLogix(String systemName) {
            super(systemName);
        }

        @Override
        public void activateLogix() {
            activated = true;
            super.activateLogix();
        }
    }

    @Test
    public void testInitializerNotActivatedWhenLoadedDisabled() {
        RecordingLogix init = new RecordingLogix(jmri.jmrit.beantable.LRouteTableAction.getLogixInitializer());
        RecordingLogix other = new RecordingLogix("IX1");
        l.register(init);
        l.register(other);
        l.setLoadDisabled(true);
        l.activateAllLogixs();
        jmri.util.JUnitAppender.suppressWarnMessageStartsWith("load disabled set");
        jmri.util.JUnitAppender.suppressWarnMessageStartsWith("load disabled set");
        assertFalse(other.activated, "other Logix not activated");
        assertFalse(init.activated, "LRoute initializer not activated either");
        assertFalse(init.getEnabled());
    }

    @Test
    public void testDisabledInitializerNotActivated() {
        RecordingLogix init = new RecordingLogix(jmri.jmrit.beantable.LRouteTableAction.getLogixInitializer());
        init.setEnabled(false);
        l.register(init);
        l.activateAllLogixs();
        assertFalse(init.activated, "a disabled initializer stays inactive");
        RecordingLogix enabled = new RecordingLogix("IX2");
        l.register(enabled);
        l.activateAllLogixs();
        Assertions.assertTrue(enabled.activated);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initIdTagManager();
        l = new DefaultLogixManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @AfterEach
    public void tearDown() {
        l = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
