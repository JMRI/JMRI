package jmri.jmrix.nce.macro;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of NceMacroEditPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NceMacroEditPanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new NceMacroEditPanel();
        helpTarget="package.jmri.jmrix.nce.macro.NceMacroEditFrame";
        title="NCE_: " + Bundle.getMessage("TitleEditNCEMacro");
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
