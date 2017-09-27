package jmri.jmrix.nce.macro;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NceMacroEditPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NceMacroEditPanelTest {

    @Test
    public void testCtor() {
        NceMacroEditPanel action = new NceMacroEditPanel();
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testGetHelpTarget() {
        NceMacroEditPanel t = new NceMacroEditPanel();
        Assert.assertEquals("help target","package.jmri.jmrix.nce.macro.NceMacroEditFrame",t.getHelpTarget());
    }

    @Test
    public void testGetTitle() {
        NceMacroEditPanel t = new NceMacroEditPanel();
        Assert.assertEquals("title","NCE_: " + Bundle.getMessage("TitleEditNCEMacro"), t.getTitle());
    }

    @Test
    public void testInitComponents() throws Exception {
        NceMacroEditPanel t = new NceMacroEditPanel();
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
