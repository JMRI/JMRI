package jmri.jmrix.zimo.swing.packetgen;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of Mx1PacketGenPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class Mx1PacketGenPanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new Mx1PacketGenPanel();
        helpTarget="package.jmri.jmrix.zimo.swing.packetgen.Mx1PacketGenPanel";
        title="MX1_: " + Bundle.getMessage("Title");
    }

    @Override
    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
