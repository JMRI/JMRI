package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

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
        FunctionPanel t = new FunctionPanel(re, ltf);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(ltf);
        JUnitUtil.dispose(wf);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // should be converted to check of scheduled ShutDownActions
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(FunctionPanelTest.class);
}
