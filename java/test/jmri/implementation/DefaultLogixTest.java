package jmri.implementation;

import jmri.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test the DefaultLogixTest implementation class
 *
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class DefaultLogixTest extends NamedBeanTest {

    /**
     * Operate parent NamedBeanTest tests.
     */
    protected NamedBean createInstance() {
        return new DefaultLogix("IX 0");
    }

    public void testCtorDouble() {
        new DefaultLogix("IX 1", "IX 1 user name");
    }

    public void testCtorSingle() {
        new DefaultLogix("IX 2");
    }

    public void testBasicBeanOperations() {
        Logix ix1 = new DefaultLogix("IX 3", "IX 3 user name");

        Logix ix2 = new DefaultLogix("IX 4");

        Assert.assertTrue("object not equals", !ix1.equals(ix2));
        Assert.assertTrue("object not equals reverse", !ix2.equals(ix1));

        Assert.assertTrue("hash not equals", ix1.hashCode() != ix2.hashCode());

    }

    // from here down is testing infrastructure
    public DefaultLogixTest(String s) {
        super(s);
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    @Override
    protected void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DefaultLogixTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DefaultLogixTest.class);
        return suite;
    }

}
