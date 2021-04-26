package jmri.jmrit.consisttool;

import java.awt.GraphicsEnvironment;

import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 * Test simple functioning of ConsistToolAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ConsistToolActionTest {

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConsistToolAction action = new ConsistToolAction("Test Consist Tool Action");
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConsistToolAction action = new ConsistToolAction();
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
