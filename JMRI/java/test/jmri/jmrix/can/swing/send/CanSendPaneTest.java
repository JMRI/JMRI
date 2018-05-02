package jmri.jmrix.can.swing.send;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CanSendPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CanSendPaneTest extends jmri.util.swing.JmriPanelTest {

    jmri.jmrix.can.CanSystemConnectionMemo memo = null;
    jmri.jmrix.can.TrafficController tc = null;

    @Test
    @Override
    public void testInitComponents() throws Exception{
        // for now, just makes ure there isn't an exception.
        ((CanSendPane) panel).initComponents(memo);
    }

    @Test
    public void testInitContext() throws Exception {
        // for now, just makes ure there isn't an exception.
        ((CanSendPane) panel).initContext(memo);
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        tc = new jmri.jmrix.can.TrafficControllerScaffold();
        memo.setTrafficController(tc);
        panel = new CanSendPane();
        helpTarget="package.jmri.jmrix.can.swing.send.CanSendFrame";
        title="Send CAN Frame";
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
