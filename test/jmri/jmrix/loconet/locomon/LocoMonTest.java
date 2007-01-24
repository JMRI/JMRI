// LocoMonTest.java

package jmri.jmrix.loconet.locomon;

import apps.tests.*;
import junit.framework.*;

/**
 * Tests for the jmri.jmrix.loconet.locomon package
 * @author	Bob Jacobsen Copyright (C) 2002, 2007
 * @version     $Revision: 1.1 $
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

    Log4JFixture log4jfixtureInst = new Log4JFixture(this);

    protected void setUp() {
    	log4jfixtureInst.setUp();
    }

    protected void tearDown() {
    	log4jfixtureInst.tearDown();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoMonTest.class.getName());

}
