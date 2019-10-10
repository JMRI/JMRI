package jmri.jmrit.cabsignals;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of CabSignalPane.
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class CabSignalPaneTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new CabSignalPane();
        title = Bundle.getMessage("CabSignalPaneTitle");
        helpTarget = "package.jmri.jmrit.cabsignals.CabSignalPane";
        JUnitUtil.initRosterConfigManager();
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
