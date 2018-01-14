package jmri.jmrix.zimo.swing.packetgen;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of Mx1PacketGenPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class Mx1PacketGenPanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new Mx1PacketGenPanel();
        helpTarget="package.jmri.jmrix.zimo.swing.packetgen.Mx1PacketGenPanel";
        title="MX1_: " + Bundle.getMessage("Title");
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
