package jmri.jmrix.loconet.locostats.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LocoStatsPanelTest extends jmri.util.swing.JmriPanelTest {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        panel = new LocoStatsPanel();
        title = Bundle.getMessage("MenuItemLocoStats");
        helpTarget = "package.jmri.jmrix.loconet.locostats.LocoStatsFrame";
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocoStatsPanelTest.class);

}
