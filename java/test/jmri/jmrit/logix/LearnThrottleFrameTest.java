package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LearnThrottleFrameTest extends jmri.util.JmriJFrameTestBase {

    private WarrantFrame wf;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        if (!GraphicsEnvironment.isHeadless()) {
            wf = new WarrantFrame(new Warrant("IW0", "AllTestWarrant"));
            frame = new LearnThrottleFrame(wf);
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (wf != null) {
            JUnitUtil.dispose(wf);
        }
        wf = null;
        JUnitUtil.clearShutDownManager(); // should be converted to check of scheduled ShutDownActions
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LearnThrottleFrameTest.class);
}
