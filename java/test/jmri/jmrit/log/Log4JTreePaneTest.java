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
public class Log4JTreePaneTest {

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

        @Test
    public void testGetHelpTarget() {
        Log4JTreePane t = new Log4JTreePane();
        Assert.assertEquals("help target","package.jmri.jmrit.log.Log4JTreePane",t.getHelpTarget());
    }

    @Test
    public void testGetTitle() {
        Log4JTreePane t = new Log4JTreePane();
        Assert.assertEquals("title",Bundle.getMessage("MenuItemLogTreeAction"),t.getTitle());
    }

    @Test
    public void testInitComponents() throws Exception {
        Log4JTreePane t = new Log4JTreePane();
        // we are just making sure that initComponents doesn't cause an exception.
        t.initComponents();
    }



    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
