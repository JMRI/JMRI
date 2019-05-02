package jmri.jmrix.tams.swing.packetgen;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of PacketGenPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class PacketGenPanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new PacketGenPanel();
        helpTarget="package.jmri.jmrix.tams.swing.packetgen.PacketGenFrame";
        title="Send Tams command";
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
