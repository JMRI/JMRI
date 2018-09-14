package jmri.implementation;

import jmri.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test the DefaultConditional implementation class
 *
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class DefaultConditionalTest {

    /**
     * Operate parent NamedBeanTest tests.
     */
    @Test
    public void createInstance() {
        new DefaultConditional("IXIC 0");
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists",new DefaultConditional("IXIC 1"));
    }

    @Test
    public void testBasicBeanOperations() {
        Conditional ix1 = new DefaultConditional("IXIC 2");

        Conditional ix2 = new DefaultConditional("IXIC 3");

        Assert.assertTrue("object not equals", !ix1.equals(ix2));
        Assert.assertTrue("object not equals reverse", !ix2.equals(ix1));

        Assert.assertTrue("hash not equals", ix1.hashCode() != ix2.hashCode());
    }

    
    // from here down is testing infrastructure

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initIdTagManager();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

}
