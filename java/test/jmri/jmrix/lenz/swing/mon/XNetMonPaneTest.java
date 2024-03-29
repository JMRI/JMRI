package jmri.jmrix.lenz.swing.mon;

import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * XNetMonPaneTest.java
 * <p>
 * Test for the jmri.jmrix.lenz.swing.mon.XNetMonPane class
 *
 * @author Paul Bender Copyright (C) 2014,2016
 */
public class XNetMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    private XNetSystemConnectionMemo memo;

    @Test
    public void testDefault() {
        jmri.util.swing.JmriNamedPaneAction f = new XNetMonPane.Default();
        Assert.assertNotNull(f);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.jmrix.lenz.XNetInterfaceScaffold t = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        memo = new XNetSystemConnectionMemo(t);
        jmri.InstanceManager.store(memo, jmri.jmrix.lenz.XNetSystemConnectionMemo.class);
        // pane for AbstractMonPaneTestBase; panel for JmriPanelTest 
        panel = pane = new XNetMonPane();
        helpTarget = "package.jmri.jmrix.AbstractMonFrame";
        title = Bundle.getMessage("MenuItemXNetCommandMonitor");
    }

    @AfterEach
    @Override
    public void tearDown() {
        panel = pane = null;
        memo.dispose(); // deregisters from instance manager
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
