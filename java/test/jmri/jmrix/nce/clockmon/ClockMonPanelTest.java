package jmri.jmrix.nce.clockmon;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ClockMonPanelTest extends jmri.util.swing.JmriPanelTest {


    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new ClockMonPanel();
        helpTarget="package.jmri.jmrix.nce.clockmon.ClockMonFrame";
        title="NCE_: " + Bundle.getMessage("TitleNceClockMonitor");
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ClockMonPanelTest.class);

}
