package jmri.jmrix.zimo.swing.packetgen;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of Mx1PacketGenPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class Mx1PacketGenPanelTest {

    @Test
    public void testMemoCtor() {
        Mx1PacketGenPanel action = new Mx1PacketGenPanel();
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
