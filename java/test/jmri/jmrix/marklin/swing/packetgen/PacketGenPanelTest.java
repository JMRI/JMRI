package jmri.jmrix.marklin.swing.packetgen;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of PacketGenPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PacketGenPanelTest extends jmri.util.swing.JmriPanelTest {


    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        panel = new PacketGenPanel();
        helpTarget="package.jmri.jmrix.marklin.swing.packetgen.PacketGenFrame";
        title=Bundle.getMessage("SendCommandTitle");
    }

    @AfterEach
    @Override
    public void tearDown() {        JUnitUtil.tearDown();    }
}
