    package jmri.jmrix.jmriclient.swing.mon;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of JMRIClientMonPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class JMRIClientMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    private JMRIClientSystemConnectionMemo memo = null;

    @Test
    public void testDefault() {
        jmri.util.swing.JmriNamedPaneAction f = new JMRIClientMonPane.Default();
        Assert.assertNotNull(f);
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new JMRIClientSystemConnectionMemo();
        jmri.InstanceManager.setDefault(JMRIClientSystemConnectionMemo.class,memo);
                // pane for AbstractMonPaneTestBase; panel for JmriPanelTest 
        panel = pane = new JMRIClientMonPane();
        helpTarget = "package.jmri.jmrix.AbstractMonFrame";
        title = Bundle.getMessage("MenuItemJmriClientCommandMonitorTitle");
    }

    @After
    @Override
    public void tearDown() {
	    memo = null;
	    panel = pane = null;
	    helpTarget = null;
	    title = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
    	JUnitUtil.tearDown();
    }
}
