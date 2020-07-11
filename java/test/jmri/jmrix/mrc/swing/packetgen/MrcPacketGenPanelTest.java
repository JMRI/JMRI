package jmri.jmrix.mrc.swing.packetgen;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MrcPacketGenPanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new MrcPacketGenPanel();
        helpTarget = "package.jmri.jmrix.mrc.swing.packetgen.MrcPacketGenPanel";
        title = "MRC_: Send MRC command";
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MrcPacketGenPanelTest.class);

}
