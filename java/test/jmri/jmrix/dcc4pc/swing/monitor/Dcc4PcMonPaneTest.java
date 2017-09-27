package jmri.jmrix.dcc4pc.swing.monitor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of Dcc4PcMonPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class Dcc4PcMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    @Test
    public void testMemoCtor() {
        Assert.assertNotNull("exists", pane);
    }

    @Test
    public void testGetHelpTarget() {
        Assert.assertNull("help target",pane.getHelpTarget());
    }

    @Test
    public void testGetTitle() {
        Assert.assertEquals("title","Dcc4PC Command Monitor",pane.getTitle());
    }

    @Test
    public void testInitComponents() throws Exception {
        // we are just making sure that initComponents doesn't cause an exception.
        pane.initComponents();
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        pane = new Dcc4PcMonPane();
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
