package jmri.jmrix.lenz.swing.mon;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XNetMonPaneTest.java
 * <p>
 * Description:	tests for the jmri.jmrix.lenz.swing.mon.XNetMonPane class
 *
 * @author	Paul Bender Copyright (C) 2014,2016
 */
public class XNetMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    private jmri.jmrix.lenz.XNetSystemConnectionMemo memo = null;

    @Test
    public void testDefault() {
        jmri.util.swing.JmriNamedPaneAction f = new XNetMonPane.Default();
        Assert.assertNotNull(f);
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.jmrix.lenz.XNetInterfaceScaffold t = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        jmri.jmrix.lenz.XNetSystemConnectionMemo memo = new jmri.jmrix.lenz.XNetSystemConnectionMemo(t);
        jmri.InstanceManager.store(memo, jmri.jmrix.lenz.XNetSystemConnectionMemo.class);
        // pane for AbstractMonPaneTestBase; panel for JmriPanelTest 
        panel = pane = new XNetMonPane();
        helpTarget = "package.jmri.jmrix.AbstractMonFrame";
        title = Bundle.getMessage("MenuItemXNetCommandMonitor");
    }

    @After
    @Override
    public void tearDown() {
        jmri.InstanceManager.deregister(memo, jmri.jmrix.lenz.XNetSystemConnectionMemo.class);
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
