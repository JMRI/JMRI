// LocoMonTest.java

package jmri.jmrix.loconet.locomon;

import junit.framework.*;

/**
 * Tests for the jmri.jmrix.loconet.locomon package
 * @author	Bob Jacobsen Copyright (C) 2002, 2007
 * @version     $Revision$
 */
public class LocoMonTest extends TestCase {

    // from here down is testing infrastructure

    public LocoMonTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LocoMonTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.loconet.locomon.LocoMonTest");  // no tests in this class itself
        suite.addTest(LlnmonTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoMonTest.class.getName());

}
