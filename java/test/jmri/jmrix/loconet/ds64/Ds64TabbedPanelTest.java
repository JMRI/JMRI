package jmri.jmrix.loconet.ds64;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Ds64TabbedPanelTest {

    @Test
    public void testCTor() {
        Ds64TabbedPanel t = new Ds64TabbedPanel();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testInitComponents() throws Exception{
        Ds64TabbedPanel pane = new Ds64TabbedPanel();
        // for now, just makes ure there isn't an exception.
        pane.initComponents();
    }

    @Test
    public void testGetHelpTarget(){
        Ds64TabbedPanel pane  = new Ds64TabbedPanel();
        Assert.assertEquals("help target","package.jmri.jmrix.loconet.ds64.DS64TabbedPanel",pane.getHelpTarget());
    }

    @Test
    public void testGetTitle(){
        Ds64TabbedPanel pane = new Ds64TabbedPanel();
        Assert.assertEquals("title",Bundle.getMessage("MenuItemDS64Programmer") ,pane.getTitle());
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

    // private final static Logger log = LoggerFactory.getLogger(Ds64TabbedPanelTest.class);

}
