package jmri.implementation;

import jmri.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test the DefaultConditional implementation class
 *
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class DefaultConditionalTest extends NamedBeanTest {

    /**
     * Operate parent NamedBeanTest tests.
     */
    protected NamedBean createInstance() {
        return new DefaultConditional("IXIC 0");
    }

    public void testCtor() {
        new DefaultConditional("IXIC 1");
    }

    public void testBasicBeanOperations() {
        Conditional ix1 = new DefaultConditional("IXIC 2");

        Conditional ix2 = new DefaultConditional("IXIC 3");

        Assert.assertTrue("object not equals", !ix1.equals(ix2));
        Assert.assertTrue("object not equals reverse", !ix2.equals(ix1));

        Assert.assertTrue("hash not equals", ix1.hashCode() != ix2.hashCode());

    }

    // from here down is testing infrastructure
    public DefaultConditionalTest(String s) {
        super(s);
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initIdTagManager();
    }

    @Override
    protected void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DefaultConditionalTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DefaultConditionalTest.class);
        return suite;
    }

}
