package jmri.jmrix.nce.macro;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of NceMacroGenPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NceMacroGenPanelTest extends jmri.util.swing.JmriPanelTest {


    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new NceMacroGenPanel();
        helpTarget="package.jmri.jmrix.nce.macro.NceMacroEditFrame";
        title="NCE_: " + Bundle.getMessage("TitleNceMacroGen");
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
