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
public class RosterEntryToGroupActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriJFrame jf = new JmriJFrame("TestRosterToGroupWindow");
        RosterEntryToGroupAction t = new RosterEntryToGroupAction("test roster entry to group",jf);
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

    // private final static Logger log = LoggerFactory.getLogger(RosterEntryToGroupActionTest.class);

}
