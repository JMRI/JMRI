// StatusFrameTest.java

package jmri.jmrix.pricom.pockettester;
import jmri.NmraPacket;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the StatusFrame class
 * @author		Bob Jacobsen  Copyright 2005
 * @version		$Revision: 1.1 $
 */
public class StatusFrameTest extends TestCase {

    public void testCreate() {
        new StatusFrame();
    }

    // create and show, with some data present
    public void testShow() throws Exception {
        StatusFrame f = new StatusFrame();
        f.initComponents();
        f.show();
        f.asciiFormattedMessage(PocketTesterTest.version);
        f.asciiFormattedMessage(PocketTesterTest.speed0003A);
        f.asciiFormattedMessage(PocketTesterTest.idlePacket);
        f.asciiFormattedMessage(PocketTesterTest.status1);
        f.asciiFormattedMessage(PocketTesterTest.status2);
        f.asciiFormattedMessage(PocketTesterTest.status3);
        f.asciiFormattedMessage(PocketTesterTest.status4);
    }

    // from here down is testing infrastructure
    public StatusFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {StatusFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(StatusFrameTest.class);
        return suite;
    }

}

