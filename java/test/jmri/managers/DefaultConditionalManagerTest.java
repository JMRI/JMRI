package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.Conditional;
import jmri.ConditionalManager;
import jmri.InstanceManager;
import jmri.Logix;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.managers.DefaultConditionalManager class.
 *
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class DefaultConditionalManagerTest extends AbstractManagerTestBase<ConditionalManager,Conditional> {

    @Test
    public void testCtor() {
        assertNotNull( l, "exists");
    }

    @Test
    public void testCreate() {
        ConditionalManager m = l;

        Conditional c1 = m.createNewConditional("IX01C01", "");
        Conditional c2 = m.createNewConditional("IX01C02", "");

        assertNotSame( c1, c2);
        assertFalse( c1.equals(c2));
    }

    @Test
    public void testUserNameOverlap() {
        ConditionalManager m = l;

        Conditional c1 = m.createNewConditional("IX02C01", "Foo");
        Conditional c2 = m.createNewConditional("IX02C02", "Bah");

        assertTrue( "Foo".equals( c1.getUserName()));
        assertTrue( "Bah".equals(c2.getUserName()));
    }

    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}

    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithPrefixNotASystemName() {}

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initIdTagManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initConditionalManager();

        Logix x1 = new jmri.implementation.DefaultLogix("IX01");
        assertNotNull( x1, "Logix x1 is null!");
        InstanceManager.getDefault(jmri.LogixManager.class).register(x1);

        Logix x2 = new jmri.implementation.DefaultLogix("IX02");
        assertNotNull( x2, "Logix x2 is null!");
        InstanceManager.getDefault(jmri.LogixManager.class).register(x2);
        l = new DefaultConditionalManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @AfterEach
    public void tearDown() throws Exception {
        l = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
