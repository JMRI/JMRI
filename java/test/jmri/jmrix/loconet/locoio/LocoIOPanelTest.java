package jmri.jmrix.loconet.locoio;

import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.locoio.LocoIOFrame class
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 */
public class LocoIOPanelTest extends TestCase {

    public void testFrameCreate() {
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        new LocoIOPanel();
        Assert.assertNotNull("exists", lnis);
    }

    public void testReadAll() {
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

        LocoIOPanel f = new LocoIOPanel();
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        memo.setLnTrafficController(lnis);
        f.initComponents(memo);

        // click button
        f.readAllButton.doClick();

        // check first message of ReadAll
        Assert.assertEquals("One message sent", 1, lnis.outbound.size());
        LocoNetMessage msg = lnis.outbound.elementAt(0);
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

        LocoIOPanel f = new LocoIOPanel();
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        memo.setLnTrafficController(lnis);
        f.initComponents(memo);

        f.addrField.setText("1234");
        f.addrField.postActionEvent();

        // click button
        f.readAllButton.doClick();

        // check first message of readAll
        Assert.assertEquals("One message sent", 1, lnis.outbound.size());
        LocoNetMessage msg = lnis.outbound.elementAt(0);
        Assert.assertEquals("message length", 16, msg.getNumDataElements());
        Assert.assertEquals("message opCode", 0xE5, msg.getOpCode());
        Assert.assertEquals("message bytes", "E5 10 50 34 01 00 02 04 00 00 10 00 00 00 00 00", msg.toString());

        // dispose and end operation
        f.dispose();
    }

    public void testSetAddr() {
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

        // skip the warning dialog box
        LocoIOPanel f = new LocoIOPanel() {
            protected int cautionAddrSet() {
                return 1;
            }
        };
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        memo.setLnTrafficController(lnis);
        f.initComponents(memo);

        f.addrField.setText("0134");

        // click button
        f.addrSetButton.doClick();

        // check first message of readAll
        Assert.assertEquals("One message sent", 1, lnis.outbound.size());
        LocoNetMessage msg = lnis.outbound.elementAt(0);
        Assert.assertEquals("message length", 16, msg.getNumDataElements());
        Assert.assertEquals("message opCode", 0xE5, msg.getOpCode());
        Assert.assertEquals("message bytes", "E5 10 50 00 01 00 01 01 00 34 10 00 00 00 00 00", msg.toString());

        // dispose and end operation
        f.dispose();
    }

    // from here down is testing infrastructure
    public LocoIOPanelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LocoIOPanelTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LocoIOPanelTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
