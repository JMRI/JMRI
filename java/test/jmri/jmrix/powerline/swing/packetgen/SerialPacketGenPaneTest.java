package jmri.jmrix.powerline.swing.packetgen;

import jmri.jmrix.powerline.SerialTrafficControlScaffold;
import jmri.util.JUnitUtil;
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

    @Test
    public void testGetHelpTarget() {
        SerialPacketGenPane t = new SerialPacketGenPane();
        Assert.assertEquals("help target","package.jmri.jmrix.powerline.swing.packetgen.PowerlinePacketGenPane",t.getHelpTarget());
    }

    @Test
    public void testGetTitle() {
        SerialPacketGenPane t = new SerialPacketGenPane();
        Assert.assertEquals("title","Powerline_: Command Generator",t.getTitle());
    }

    @Test
    public void testInitComponents() throws Exception {
        SerialPacketGenPane t = new SerialPacketGenPane();
        // we are just making sure that initComponents doesn't cause an exception.
        t.initComponents();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new SerialTrafficControlScaffold();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();        tc = null;
    }
}
