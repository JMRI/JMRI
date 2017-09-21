package jmri.jmrix.ncemonitor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;

/**
 * Test simple functioning of NcePacketMonitorPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NcePacketMonitorPanelTest {

    private NceSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        NcePacketMonitorPanel action = new NcePacketMonitorPanel();
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testInitComponents() throws Exception {
        NcePacketMonitorPanel action = new NcePacketMonitorPanel();
        // this test currently only verifies there is no exception thrown.
        action.initComponents(memo);
    }

    @Test
    public void testHelpTarget() {
        NcePacketMonitorPanel action = new NcePacketMonitorPanel();
        Assert.assertEquals("help target","package.jmri.jmrix.nce.analyzer.NcePacketMonitorFrame",action.getHelpTarget());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new NceSystemConnectionMemo();
        memo.setNceTrafficController(new NceTrafficController());
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
