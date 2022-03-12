package jmri.jmrix.powerline.swing.packetgen;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of SerialPacketGenPane
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialPacketGenPaneTest extends jmri.util.swing.JmriPanelTest {

    // private SerialTrafficControlScaffold tc = null;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // tc = new SerialTrafficControlScaffold();
        panel = new SerialPacketGenPane();
        title = "Powerline_: Command Generator";
        helpTarget="package.jmri.jmrix.powerline.packetgen.PowerlinePacketGenPane";
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
        // tc = null;
    }
}
