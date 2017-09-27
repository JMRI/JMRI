package jmri.jmrix.nce.cab;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NceShowCabPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NceShowCabPanelTest {

    @Test
    public void testCtor() {
        NceShowCabPanel action = new NceShowCabPanel();
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testInitComponents() throws Exception{
        NceShowCabPanel pane = new NceShowCabPanel();
        // for now, just makes ure there isn't an exception.
        pane.initComponents();
    }

    @Test
    public void testGetHelpTarget(){
        NceShowCabPanel pane = new NceShowCabPanel();
        Assert.assertEquals("help target","package.jmri.jmrix.nce.cab.NceShowCabFrame",pane.getHelpTarget());
    }

    @Test
    public void testGetTitle(){
        NceShowCabPanel pane = new NceShowCabPanel();
        Assert.assertEquals("title","NCE_: " + Bundle.getMessage("Title"),pane.getTitle());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
