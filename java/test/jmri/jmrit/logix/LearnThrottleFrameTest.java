package jmri.jmrit.logix;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 *
 * @author Pete Cressman Copyright (C) 2020
 */
public class LearnThrottleFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        WarrantFrame wf = new WarrantFrame(new Warrant("IW0", "AllTestWarrant"));
        LearnThrottleFrame ltf = new LearnThrottleFrame(wf);
        assertThat(ltf).withFailMessage("exists").isNotNull();
        JUnitUtil.dispose(ltf);
        JUnitUtil.dispose(wf);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
