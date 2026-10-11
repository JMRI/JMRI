package jmri.jmrit.consisttool;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ConsistToolAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ConsistToolActionTest {

    @Test
    @DisabledIfHeadless
    public void testStringCtor() {
        ConsistToolAction action = new ConsistToolAction("Test Consist Tool Action");
        assertNotNull(action, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testCtor() {
        ConsistToolAction action = new ConsistToolAction();
        assertNotNull(action, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
