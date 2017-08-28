package jmri.jmrix.ncemonitor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NcePacketMonitorPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NcePacketMonitorPanelTest {

    @Test
    public void testCtor() {
        NcePacketMonitorPanel action = new NcePacketMonitorPanel();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
