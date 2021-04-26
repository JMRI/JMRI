package jmri.jmrix.tams.swing.packetgen;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of PacketGenPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PacketGenPanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new PacketGenPanel();
        helpTarget="package.jmri.jmrix.tams.swing.packetgen.PacketGenFrame";
        title="Send Tams command";
    }

    @Override
    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
