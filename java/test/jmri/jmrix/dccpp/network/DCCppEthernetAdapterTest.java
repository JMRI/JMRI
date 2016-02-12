package jmri.jmrix.dccpp.network;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DCCppEthernetAdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.network.DCCppEthernetAdapter
 * class
 *
 * @author	Paul Bender
 * @author      Mark Underwood Copyright (C) 2015
 * @version $Revision$
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
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DCCppEthernetAdapterTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppEthernetAdapterTest.class.getName());

}
