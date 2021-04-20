package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assume;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Pete Cressman Copyright (C) 2020
 */
public class LearnSpeedPanelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Warrant w = new Warrant("IW0", "AllTestWarrant");
        LearnSpeedPanel t = new LearnSpeedPanel(w);
        assertThat(t).withFailMessage("exists").isNotNull();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
