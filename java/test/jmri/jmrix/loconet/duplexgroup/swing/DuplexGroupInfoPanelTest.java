package jmri.jmrix.loconet.duplexgroup.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of DuplexGroupInfoPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class DuplexGroupInfoPanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new DuplexGroupInfoPanel();
        helpTarget = "package.jmri.jmrix.loconet.duplexgroup.DuplexGroupInfoPanel";
        title = "Configure Duplex Group Information"; 
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
