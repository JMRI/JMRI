package jmri.jmrix.marklin.swing.monitor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of MarklinMonPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class MarklinMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {


    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", pane );
    }

    @Test
    public void testGetHelpTarget() {
        Assert.assertNull("help target",pane.getHelpTarget());
    }

    @Test
    public void testGetTitle() {
        Assert.assertEquals("title",Bundle.getMessage("MarklinMonitorTitle"), pane.getTitle());
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
        pane = new MarklinMonPane();
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
