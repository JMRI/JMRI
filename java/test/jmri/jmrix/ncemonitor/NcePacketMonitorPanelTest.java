package jmri.jmrix.ncemonitor;

import apps.tests.Log4JFixture;
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
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
