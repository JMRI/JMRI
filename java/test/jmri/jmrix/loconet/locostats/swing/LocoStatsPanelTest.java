package jmri.jmrix.loconet.locostats.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LocoStatsPanelTest {

    @Test
    public void testCTor() {
        LocoStatsPanel t = new LocoStatsPanel();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testInitComponents() throws Exception{
        LocoStatsPanel pane = new LocoStatsPanel();
        // for now, just makes ure there isn't an exception.
        pane.initComponents();
    }

    @Test
    public void testGetHelpTarget(){
        LocoStatsPanel pane = new LocoStatsPanel();
        Assert.assertEquals("help target","package.jmri.jmrix.loconet.locostats.LocoStatsFrame",pane.getHelpTarget());
    }

    @Test
    public void testGetTitle(){
        LocoStatsPanel pane = new LocoStatsPanel();
        Assert.assertEquals("title",Bundle.getMessage("MenuItemLocoStats") ,pane.getTitle());
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

    // private final static Logger log = LoggerFactory.getLogger(LocoStatsPanelTest.class);

}
