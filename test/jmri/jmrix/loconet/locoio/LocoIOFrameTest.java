// LocoIOFrameTest.java

package jmri.jmrix.loconet.locoio;

import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetMessage;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.locoio.LocoIOFrame class
 * @author	    Bob Jacobsen Copyright (C) 2002
 * @version         $Revision: 1.8 $
 */
public class LocoIOFrameTest extends TestCase {

    public void testFrameCreate() {
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        new LocoIOFrame();
    }

    public void testDispose() {
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        LocoIOFrame f = new LocoIOFrame();
        f.dispose();
    }

    public void testReadAll() {
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

        LocoIOFrame f = new LocoIOFrame();

        // click button
        f.readAllButton.doClick();

        // check first message of ReadAll
        Assert.assertEquals("One message sent", 1, lnis.outbound.size());
        LocoNetMessage msg = (LocoNetMessage)lnis.outbound.elementAt(0);
        Assert.assertEquals("message length", 16, msg.getNumDataElements());
        Assert.assertEquals("message opCode", 0xE5, msg.getOpCode());
        Assert.assertEquals("message bytes", "E5 10 50 51 01 00 02 04 00 00 10 00 00 00 00 00", msg.toString());

        // dispose and end operation
        f.dispose();
    }

    public void testAddrField() {
        // make sure that the address field does a notify
        // and new address is used
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

        LocoIOFrame f = new LocoIOFrame();

        f.addrField.setText("1234");
        f.addrField.postActionEvent();

        // click button
        f.readAllButton.doClick();

        // check first message of readAll
        Assert.assertEquals("One message sent", 1, lnis.outbound.size());
        LocoNetMessage msg = (LocoNetMessage)lnis.outbound.elementAt(0);
        Assert.assertEquals("message length", 16, msg.getNumDataElements());
        Assert.assertEquals("message opCode", 0xE5, msg.getOpCode());
        Assert.assertEquals("message bytes", "E5 10 50 34 12 00 02 04 00 00 10 00 00 00 00 00", msg.toString());

        // dispose and end operation
        f.dispose();
    }

    public void testSetAddr() {
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

        // skip the warning dialog box
        LocoIOFrame f = new LocoIOFrame(){
            protected int cautionAddrSet() { return 1;}
        };

        f.addrField.setText("0134");

        // click button
        f.addrSetButton.doClick();

        // check first message of readAll
        Assert.assertEquals("One message sent", 1, lnis.outbound.size());
        LocoNetMessage msg = (LocoNetMessage)lnis.outbound.elementAt(0);
        Assert.assertEquals("message length", 16, msg.getNumDataElements());
        Assert.assertEquals("message opCode", 0xE5, msg.getOpCode());
        Assert.assertEquals("message bytes", "E5 10 50 00 10 00 01 01 00 34 10 00 00 00 00 00", msg.toString());

        // dispose and end operation
        f.dispose();
    }

    // from here down is testing infrastructure

    public LocoIOFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LocoIOFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LocoIOFrameTest.class);
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoIOFrameTest.class.getName());
    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
