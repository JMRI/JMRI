package jmri.jmrix.zimo.swing.packetgen;

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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
