// MakePacketTest.java

package jmri.jmrix.direct;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the MakePacketTest class
 * @author		Bob Jacobsen  Copyright 2004
 * @version		$Revision: 1.1 $
 */
public class MakePacketTest extends TestCase {

    public void testCreate() {
    }

    // from here down is testing infrastructure
    public MakePacketTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MakePacketTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MakePacketTest.class);
        return suite;
    }

}
