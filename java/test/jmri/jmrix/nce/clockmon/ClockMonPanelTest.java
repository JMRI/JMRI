package jmri.jmrix.nce.clockmon;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ClockMonPanelTest extends jmri.util.swing.JmriPanelTest {


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new ClockMonPanel();
        helpTarget="package.jmri.jmrix.nce.clockmon.ClockMonFrame";
        title="NCE_: " + Bundle.getMessage("TitleNceClockMonitor");
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ClockMonPanelTest.class);

}
