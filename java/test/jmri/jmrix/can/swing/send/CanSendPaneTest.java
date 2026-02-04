package jmri.jmrix.can.swing.send;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of CanSendPane
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class CanSendPaneTest extends jmri.util.swing.JmriPanelTest {

    private jmri.jmrix.can.CanSystemConnectionMemo memo = null;
    private jmri.jmrix.can.TrafficController tc = null;

    @Test
    @Override
    public void testInitComponents() {
        Assertions.assertDoesNotThrow( () ->
            ((CanSendPane) panel).initComponents(memo));
    }

    @Test
    public void testInitContext() {
        Assertions.assertDoesNotThrow( () ->
        ((CanSendPane) panel).initContext(memo));
    }

    @Override
    @BeforeEach
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
    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(memo);
        Assertions.assertNotNull(tc);
        Assertions.assertNotNull(panel);
        panel.dispose();
        tc.terminateThreads();
        memo.dispose();
        JUnitUtil.tearDown();
    }

}
