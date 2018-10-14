package jmri.jmrit.consisttool;

import java.awt.GraphicsEnvironment;
import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ConsistToolAction
 *
 * @author	Paul Bender Copyright (C) 2016
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
