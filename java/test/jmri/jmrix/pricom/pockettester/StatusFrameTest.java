package jmri.jmrix.pricom.pockettester;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the StatusFrame class
 *
 * @author	Bob Jacobsen Copyright 2005
 * @version	$Revision$
 */
public class StatusFrameTest extends TestCase {

    public void testCreate() {
        new StatusFrame();
    }

    // create and show, with some data present
    public void testShow() throws Exception {
        StatusFrame f = new StatusFrame();
        f.initComponents();
        f.setVisible(true);
        f.setSource(new DataSource() {

            void sendBytes(byte[] bytes) {
            }
        });
        f.asciiFormattedMessage(PocketTesterTest.version);
        f.asciiFormattedMessage(PocketTesterTest.speed0003A);
        f.asciiFormattedMessage(PocketTesterTest.idlePacket);
        f.asciiFormattedMessage(PocketTesterTest.status1);
        f.asciiFormattedMessage(PocketTesterTest.status2);
        f.asciiFormattedMessage(PocketTesterTest.status3);
        f.asciiFormattedMessage(PocketTesterTest.status4);
        f.asciiFormattedMessage(PocketTesterTest.status5);

        f.dispose();
    }

    // create and show, with zero address data
    public void testZeroAddr() throws Exception {
        StatusFrame f = new StatusFrame();
        f.initComponents();
        f.setVisible(true);
        f.setSource(new DataSource() {
            void sendBytes(byte[] bytes) {
            }
        });
        f.asciiFormattedMessage(PocketTesterTest.version);
        f.asciiFormattedMessage(PocketTesterTest.speed0003A);
        f.asciiFormattedMessage(PocketTesterTest.idlePacket);
        f.asciiFormattedMessage(PocketTesterTest.status6);
        f.asciiFormattedMessage(PocketTesterTest.status2);

        f.dispose();
    }

    // from here down is testing infrastructure
    public StatusFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {StatusFrameTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(StatusFrameTest.class);
        return suite;
    }

}
