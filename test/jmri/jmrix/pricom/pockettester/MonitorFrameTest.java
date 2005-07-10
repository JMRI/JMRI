// MonitorFrameTest.java

package jmri.jmrix.pricom.pockettester;
import jmri.NmraPacket;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the MakePacketTest class
 * @author		Bob Jacobsen  Copyright 2004
 * @version		$Revision: 1.1 $
 */
public class MonitorFrameTest extends TestCase {

    public void testCreate() {
    }


    // from here down is testing infrastructure
    public MonitorFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MonitorFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MonitorFrameTest.class);
        return suite;
    }
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MonitorFrameTest.class.getName());

}

