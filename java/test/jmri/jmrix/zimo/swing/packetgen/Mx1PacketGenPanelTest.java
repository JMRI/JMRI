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

    @Test
    public void testInitComponents() throws Exception{
        Mx1PacketGenPanel pane = new Mx1PacketGenPanel();
        // for now, just makes ure there isn't an exception.
        pane.initComponents();
    }

    @Test
    public void testGetHelpTarget(){
        Mx1PacketGenPanel pane = new Mx1PacketGenPanel();
        Assert.assertEquals("help target","package.jmri.jmrix.zimo.swing.packetgen.Mx1PacketGenPanel",pane.getHelpTarget());
    }

    @Test
    public void testGetTitle(){
        Mx1PacketGenPanel pane = new Mx1PacketGenPanel();
        Assert.assertEquals("title","MX1_: " + Bundle.getMessage("Title"),pane.getTitle());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
