package jmri.jmrix.nce.cab;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of NceShowCabPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NceShowCabPanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new NceShowCabPanel();
        helpTarget="package.jmri.jmrix.nce.cab.NceShowCabFrame";
        title="NCE_: " + Bundle.getMessage("Title");
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
