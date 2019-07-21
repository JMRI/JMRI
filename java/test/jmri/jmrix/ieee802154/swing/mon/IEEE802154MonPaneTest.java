package jmri.jmrix.ieee802154.swing.mon;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of IEEE802154MonPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class IEEE802154MonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    private IEEE802154SystemConnectionMemo memo = null;

    @Test
    public void testDefault() {
        jmri.util.swing.JmriNamedPaneAction f = new IEEE802154MonPane.Default();
        Assert.assertNotNull(f);
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new IEEE802154SystemConnectionMemo();
        jmri.InstanceManager.setDefault(IEEE802154SystemConnectionMemo.class,memo);
                // pane for AbstractMonPaneTestBase; panel for JmriPanelTest 
        panel = pane = new IEEE802154MonPane();
        helpTarget = "package.jmri.jmrix.AbstractMonFrame";
        title = Bundle.getMessage("MonFrameTitle");
    }

    @After
    @Override
    public void tearDown() {
            panel = pane = null;
            helpTarget = null;
            title = null;	    
	    memo = null;
	    JUnitUtil.tearDown();    
    }
}
