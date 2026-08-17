package jmri.jmrit.logix;

import java.io.File;
import java.io.IOException;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Pete Cressman Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LearnSpeedPanelTest {

    @Test
    public void testCTor() {
        Warrant w = new Warrant("IW0", "AllTestWarrant");
        LearnSpeedPanel t = new LearnSpeedPanel(w);
        Assertions.assertNotNull( t, "exists");
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
