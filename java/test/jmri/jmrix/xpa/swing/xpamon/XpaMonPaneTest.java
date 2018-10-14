package jmri.jmrix.xpa.swing.xpamon;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * @author Paul Bender Copyright(C) 2016
 */
public class XpaMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        jmri.jmrix.xpa.XpaSystemConnectionMemo memo = new jmri.jmrix.xpa.XpaSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.xpa.XpaSystemConnectionMemo.class,memo);
        // pane for AbstractMonPaneTestBase; panel for JmriPanelTest 
        panel = pane = new XpaMonPane();
        helpTarget = "package.jmri.jmrix.AbstractMonFrame";
        title = Bundle.getMessage("XpaMonFrameTitle");
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
