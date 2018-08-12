package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class FunctionPanelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        WarrantFrame wf = new WarrantFrame(new Warrant("IW0", "AllTestWarrant"));
        LearnThrottleFrame ltf = new LearnThrottleFrame(wf);
        RosterEntry re = new RosterEntry("file here");
        FunctionPanel t = new FunctionPanel(re,ltf);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(ltf);
        JUnitUtil.dispose(wf);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(FunctionPanelTest.class);

}
