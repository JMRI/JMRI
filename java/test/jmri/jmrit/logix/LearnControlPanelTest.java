package jmri.jmrit.logix;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Pete Cressman Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LearnControlPanelTest {

    @Test
    public void testCTor() throws Exception {

        WarrantFrame wf = new WarrantFrame(new Warrant("IW0", "AllTestWarrant"));
        LearnThrottleFrame ltf = new LearnThrottleFrame(wf);
        LearnControlPanel t = new LearnControlPanel(ltf);
        Assertions.assertNotNull( t, "exists");
        JUnitUtil.dispose(ltf);
        JUnitUtil.dispose(wf);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
