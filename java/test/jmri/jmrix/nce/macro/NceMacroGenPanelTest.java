package jmri.jmrix.nce.macro;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NceMacroGenPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NceMacroGenPanelTest {

    @Test
    public void testCtor() {
        NceMacroGenPanel action = new NceMacroGenPanel();
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testGetHelpTarget() {
        NceMacroGenPanel t = new NceMacroGenPanel();
        Assert.assertEquals("help target","package.jmri.jmrix.nce.macro.NceMacroEditFrame",t.getHelpTarget());
    }

    @Test
    public void testGetTitle() {
        NceMacroGenPanel t = new NceMacroGenPanel();
        Assert.assertEquals("title","NCE_: " + Bundle.getMessage("TitleNceMacroGen"), t.getTitle());
    }

    @Test
    public void testInitComponents() throws Exception {
        NceMacroGenPanel t = new NceMacroGenPanel();
        // we are just making sure that initComponents doesn't cause an exception.
        t.initComponents();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
