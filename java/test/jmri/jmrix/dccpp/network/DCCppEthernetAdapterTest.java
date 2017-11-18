package jmri.jmrix.dccpp.network;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * DCCppEthernetAdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.network.DCCppEthernetAdapter
 * class
 *
 * @author	Paul Bender
 * @author      Mark Underwood Copyright (C) 2015
 */
public class DCCppEthernetAdapterTest extends TestCase {

    public void testCtor() {
        DCCppEthernetAdapter a = new DCCppEthernetAdapter();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public DCCppEthernetAdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DCCppEthernetAdapterTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DCCppEthernetAdapterTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
