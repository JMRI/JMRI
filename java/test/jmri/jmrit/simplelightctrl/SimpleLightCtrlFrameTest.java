package jmri.jmrit.simplelightctrl;

import java.awt.GraphicsEnvironment;
import jmri.Timebase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of SimpleLightCtrlFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SimpleLightCtrlFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        Timebase clock = jmri.InstanceManager.getDefault(jmri.Timebase.class);
        clock.setRun(false);
        clock.setTime(java.time.Instant.EPOCH);  // just a specific time

        JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new SimpleLightCtrlFrame();
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        // force a new default Timebase object
        jmri.InstanceManager.reset(jmri.Timebase.class);
        super.tearDown();
    }
}
