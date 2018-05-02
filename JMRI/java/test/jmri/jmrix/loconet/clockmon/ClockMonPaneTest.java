package jmri.jmrix.loconet.clockmon;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ClockMonPaneTest extends jmri.util.swing.JmriPanelTest {


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new ClockMonPane();
        helpTarget = "package.jmri.jmrix.loconet.clockmon.ClockMonFrame";
        title = Bundle.getMessage("MenuItemClockMon");
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ClockMonPaneTest.class);

}
