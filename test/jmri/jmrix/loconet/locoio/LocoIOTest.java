// LocoIOTest.java

package jmri.jmrix.loconet.locoio;

import apps.tests.*;
import junit.framework.*;

/**
 * Tests for the jmri.jmrix.loconet.locoio package
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version     $Revision: 1.4 $
 */
public class LocoIOTest extends TestCase {

    // from here down is testing infrastructure

    public LocoIOTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LocoIOTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.loconet.LocoNetTest");  // no tests in this class itself
        suite.addTest(LocoIOFrameTest.suite());
        suite.addTest(LocoIOTableModelTest.suite());
        return suite;
    }

    Log4JFixture log4jfixtureInst = new Log4JFixture(this);

    protected void setUp() {
    	log4jfixtureInst.setUp();
    }

    protected void tearDown() {
    	log4jfixtureInst.tearDown();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoIOTest.class.getName());

}
