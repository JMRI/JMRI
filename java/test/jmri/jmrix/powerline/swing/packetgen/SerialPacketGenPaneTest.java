package jmri.jmrix.powerline.swing.packetgen;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.jmrix.powerline.SerialTrafficControlScaffold;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of SerialPacketGenPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SerialPacketGenPaneTest {


    private SerialTrafficControlScaffold tc = null;

    @Test
    public void testCtor() {
        SerialPacketGenPane action = new SerialPacketGenPane();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        tc = new SerialTrafficControlScaffold();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
        tc = null;
    }
}
