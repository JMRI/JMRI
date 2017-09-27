package jmri.jmrix.zimo.swing.monitor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of Mx1MonPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class Mx1MonPanelTest {

    @Test
    public void testMemoCtor() {
        Mx1MonPanel action = new Mx1MonPanel();
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testInitComponents() throws Exception{
        Mx1MonPanel pane = new Mx1MonPanel();
        // for now, just makes ure there isn't an exception.
        pane.initComponents();
    }

    @Test
    public void testGetHelpTarget(){
        Mx1MonPanel pane = new Mx1MonPanel();
        Assert.assertEquals("help target","package.jmri.jmrix.zimo.swing.monitor.Mx1MonPanel",pane.getHelpTarget());
    }

    @Test
    public void testGetTitle(){
        Mx1MonPanel pane = new Mx1MonPanel();
        Assert.assertEquals("title","Mx1_: Command Monitor" ,pane.getTitle());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
