package jmri.jmrix.powerline.insteon2412s;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.powerline.insteon2412s package.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008, 2009
 */
public class Insteon2412sTest extends TestCase {

    // from here down is testing infrastructure
    public Insteon2412sTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {Insteon2412sTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.powerline.insteon2412s.Insteon2412sTest");
        suite.addTest(SpecificMessageTest.suite());
        suite.addTest(SpecificReplyTest.suite());
        suite.addTest(SpecificTrafficControllerTest.suite());
        return suite;
    }

}
