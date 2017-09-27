package jmri.jmrix.powerline.swing.serialmon;

import jmri.jmrix.powerline.SerialTrafficControlScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of SerialMonPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SerialMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {


    private SerialTrafficControlScaffold tc = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists",pane);
    }

    @Test
    public void testGetHelpTarget() {
        Assert.assertNull("help target",pane.getHelpTarget());
    }

    @Test
    public void testGetTitle() {
        Assert.assertEquals("title","Powerline_: Communication Monitor",pane.getTitle());
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
        tc = new SerialTrafficControlScaffold();
        pane = new SerialMonPane();
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();        tc = null;
    }
}
