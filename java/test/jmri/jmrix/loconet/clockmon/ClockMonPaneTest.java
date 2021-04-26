package jmri.jmrix.loconet.clockmon;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ClockMonPaneTest extends jmri.util.swing.JmriPanelTest {


    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new ClockMonPane();
        helpTarget = "package.jmri.jmrix.loconet.clockmon.ClockMonFrame";
        title = Bundle.getMessage("MenuItemClockMon");
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ClockMonPaneTest.class);

}
