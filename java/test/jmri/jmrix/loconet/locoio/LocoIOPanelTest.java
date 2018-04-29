package jmri.jmrix.loconet.locoio;

import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.loconet.locoio.LocoIOFrame class
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 */
public class LocoIOPanelTest extends jmri.util.swing.JmriPanelTest {

    private LocoNetInterfaceScaffold lnis;

    @Test
    public void testReadAll() {
        LocoIOPanel f = (LocoIOPanel) panel;
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

    @Test
    public void testAddrField() {
        // make sure that the address field does a notify
        // and new address is used
        // prepare an interface

        LocoIOPanel f = (LocoIOPanel) panel;
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

    @Test
    public void testSetAddr() {
        // skip the warning dialog box
        LocoIOPanel f = new LocoIOPanel() {
            @Override
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

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        lnis = new LocoNetInterfaceScaffold();
        panel = new LocoIOPanel();
        helpTarget = "package.jmri.jmrix.loconet.locoio.LocoIOFrame";
        title = Bundle.getMessage("MenuItemLocoIOProgrammer");
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
