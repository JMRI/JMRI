package jmri.jmrix.marklin.swing.packetgen;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of PacketGenPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class PacketGenPanelTest {


    @Test
    public void testCtor() {
        PacketGenPanel action = new PacketGenPanel();
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testGetHelpTarget() {
        PacketGenPanel t = new PacketGenPanel();
        Assert.assertEquals("help target","package.jmri.jmrix.marklin.swing.packetgen.PacketGenFrame",t.getHelpTarget());
    }

    @Test
    public void testGetTitle() {
        PacketGenPanel t = new PacketGenPanel();
        Assert.assertEquals("title",Bundle.getMessage("SendCommandTitle"),t.getTitle());
    }

    @Test
    public void testInitComponents() throws Exception {
        PacketGenPanel t = new PacketGenPanel();
        // we are just making sure that initComponents doesn't cause an exception.
        t.initComponents();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
