package jmri.jmrix.ncemonitor;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;

/**
 * Test simple functioning of NcePacketMonitorPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class NcePacketMonitorPanelTest extends jmri.util.swing.JmriPanelTest {

    private NceSystemConnectionMemo memo = null;

    @Test
    @Override
    public void testInitComponents() {
        // this test currently only verifies there is no exception thrown.
        ((NcePacketMonitorPanel) panel).initComponents(memo);
        // also check that dispose doesn't cause an exception
        ((NcePacketMonitorPanel) panel).dispose();
    }

    @Test
    public void testInitContext() {
        // this test currently only verifies there is no exception thrown.
        ((NcePacketMonitorPanel) panel).initContext(memo);
        // also check that dispose doesn't cause an exception
        ((NcePacketMonitorPanel) panel).dispose();
    }

    @Test
    public void testGetTitleAfterInit() {
        ((NcePacketMonitorPanel) panel).initComponents(memo);
        Assertions.assertEquals("NCE: DCC Packet Analyzer",panel.getTitle(), "Title");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        memo = new NceSystemConnectionMemo();
        memo.setNceTrafficController(new NceTrafficController());
        panel = new NcePacketMonitorPanel();
        title="NCE_: DCC Packet Analyzer";
        helpTarget="package.jmri.jmrix.nce.analyzer.NcePacketMonitorFrame";
    }

    @AfterEach
    @Override
    public void tearDown() {
        memo.getNceTrafficController().terminateThreads();
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }
}
