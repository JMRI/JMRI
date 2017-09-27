package jmri.jmrix.nce.clockmon;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ClockMonPanelTest {

    @Test
    public void testCTor() {
        ClockMonPanel t = new ClockMonPanel();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testGetHelpTarget() {
        ClockMonPanel t = new ClockMonPanel();
        Assert.assertEquals("help target","package.jmri.jmrix.nce.clockmon.ClockMonFrame",t.getHelpTarget());
    }

    @Test
    public void testGetTitle() {
        ClockMonPanel t = new ClockMonPanel();
        Assert.assertEquals("title","NCE_: " + Bundle.getMessage("TitleNceClockMonitor"),t.getTitle());
    }

    @Test
    public void testInitComponents() throws Exception {
        ClockMonPanel t = new ClockMonPanel();
        // we are just making sure that initComponents doesn't cause an exception.
        t.initComponents();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ClockMonPanelTest.class);

}
