package jmri.jmrix.loconet.locobuffer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.locobuffer package.
 * @author	Bob Jacobsen Copyright 2001, 2003, 2006
 * @version     $Revision: 1.1 $
 */
public class LocoBufferTest extends TestCase {

    // from here down is testing infrastructure

    public LocoBufferTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LocoBufferTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.loconet.locobuffer.LocoBufferTest");  // no tests in this class itself
        suite.addTest(LocoBufferStatsFrameTest.suite());
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoBufferTest.class.getName());

}
