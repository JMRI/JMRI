package jmri.jmrit.log;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.slf4j.LoggerFactory;

/**
 * Invokes complete set of tests in the jmri.jmrit.log tree
 *
 * @author	Bob Jacobsen Copyright 2003, 2010
 */
public class Log4JTreePaneTest extends jmri.util.swing.JmriPanelTest {

    @Test
    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LoggerFactory.getLogger("jmri.jmrix");
        LoggerFactory.getLogger("apps.foo");
        LoggerFactory.getLogger("jmri.util");

        new jmri.util.swing.JmriNamedPaneAction("Log4J Tree",
                new jmri.util.swing.sdi.JmriJFrameInterface(),
                "jmri.jmrit.log.Log4JTreePane").actionPerformed(null);
        JFrame f = JFrameOperator.waitJFrame(Bundle.getMessage("MenuItemLogTreeAction"), true, true);
        Assert.assertNotNull(f);
        JUnitUtil.dispose(f);
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.resetProfileManager();

        panel = new Log4JTreePane();
        title=Bundle.getMessage("MenuItemLogTreeAction");
        helpTarget="package.jmri.jmrit.log.Log4JTreePane";
    }

    @After
    @Override
    public void tearDown() {
        panel = null;
        title = null;
        helpTarget = null;
        
        JUnitUtil.tearDown();
    }

}
