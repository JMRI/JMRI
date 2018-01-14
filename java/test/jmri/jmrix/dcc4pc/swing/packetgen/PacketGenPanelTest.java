package jmri.jmrix.dcc4pc.swing.packetgen;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of PacketGenPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class PacketGenPanelTest extends jmri.util.swing.JmriPanelTest {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        panel = new PacketGenPanel();
        title="Send DCC4PC command";
        helpTarget="package.jmri.jmrix.dcc4pc.swing.packetgen.PacketGenFrame";
    }

    @After
    @Override
    public void tearDown() {        JUnitUtil.tearDown();    }
}
