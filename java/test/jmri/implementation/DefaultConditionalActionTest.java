// DefaultConditionalActionTest.java
package jmri.implementation;

import jmri.ConditionalAction;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the DefaultConditionalAction implementation class
 *
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class DefaultConditionalActionTest extends TestCase {

    public void testCtor() {
        new DefaultConditionalAction();
    }

    public void testBasicBeanOperations() {
        ConditionalAction ix1 = new DefaultConditionalAction(1,2,"3",4,"5");
        ConditionalAction ix2 = new DefaultConditionalAction(1,2,"3",4,"5");

        ConditionalAction ix3 = new DefaultConditionalAction(0,2,"3",4,"5");
        ConditionalAction ix4 = new DefaultConditionalAction(1,0,"3",4,"5");
        ConditionalAction ix5 = new DefaultConditionalAction(1,2,"0",4,"5");
        ConditionalAction ix6 = new DefaultConditionalAction(1,2,"3",0,"5");
        ConditionalAction ix7 = new DefaultConditionalAction(1,2,"3",4,"0");

        Assert.assertTrue(!ix1.equals(null));
        Assert.assertTrue(ix1.equals(ix1));
        Assert.assertTrue(ix1.equals(ix2));

        Assert.assertTrue(!ix1.equals(ix3));
        Assert.assertTrue(!ix1.equals(ix4));
        Assert.assertTrue(!ix1.equals(ix5));
        Assert.assertTrue(!ix1.equals(ix6));
        Assert.assertTrue(!ix1.equals(ix7));

        Assert.assertTrue(ix1.hashCode() == ix2.hashCode());

    }

    // from here down is testing infrastructure
    public DefaultConditionalActionTest(String s) {
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
        String[] testCaseName = {DefaultConditionalActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DefaultConditionalActionTest.class);
        return suite;
    }

}
