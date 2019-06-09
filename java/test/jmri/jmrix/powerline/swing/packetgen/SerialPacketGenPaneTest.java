package jmri.jmrix.powerline.swing.packetgen;

import jmri.jmrix.powerline.SerialTrafficControlScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of SerialPacketGenPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SerialPacketGenPaneTest extends jmri.util.swing.JmriPanelTest {

    // private SerialTrafficControlScaffold tc = null;

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // tc = new SerialTrafficControlScaffold();
        panel = new SerialPacketGenPane();
        title = "Powerline_: Command Generator";
        helpTarget="package.jmri.jmrix.powerline.packetgen.PowerlinePacketGenPane";
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        // tc = null;
    }
}
