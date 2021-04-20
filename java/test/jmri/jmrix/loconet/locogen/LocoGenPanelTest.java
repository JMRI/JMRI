package jmri.jmrix.loconet.locogen;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LocoGenPanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new LocoGenPanel();
        helpTarget = "package.jmri.jmrix.loconet.locogen.LocoGenFrame";
        title = Bundle.getMessage("MenuItemSendPacket");
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocoGenPanelTest.class);

}
