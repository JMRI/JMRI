package jmri.jmrit.roster.swing;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RosterMenuTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriJFrame jf = new JmriJFrame("TestRosterWindow");
        RosterMenu t = new RosterMenu("Test Roster Menu",RosterMenu.MAINMENU,jf);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(jf);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
         JUnitUtil.resetProfileManager();
   }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RosterMenuTest.class);

}
