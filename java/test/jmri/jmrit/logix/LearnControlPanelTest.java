package jmri.jmrit.logix;

import java.io.File;
import java.io.IOException;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrix.debugthrottle.DebugThrottle;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Pete Cressman Copyright (C) 2020
 */
@DisabledIfHeadless
public class LearnControlPanelTest {

    @Test
    public void testCTor() {

        WarrantFrame wf = new WarrantFrame(new Warrant("IW0", "AllTestWarrant"));
        LearnThrottleFrame ltf = new LearnThrottleFrame(wf);
        LearnControlPanel t = new LearnControlPanel(ltf);
        Assertions.assertNotNull( t, "exists");
        JUnitUtil.dispose(ltf);
        JUnitUtil.dispose(wf);
    }

    /**
     * A throttle tells its listeners of a change on the thread that made it, here
     * the test thread, as a Warrant or a script does. The learn panel records the
     * change once, though the panel shows it on the GUI thread (Swing thread).
     */
    @Test
    public void testSpeedChangeOffGuiThreadRecordedOnce() {
        WarrantFrame wf = ThreadingUtil.runOnGUIwithReturn(() -> new WarrantFrame(new Warrant("IW0", "AllTestWarrant")));
        LearnThrottleFrame ltf = ThreadingUtil.runOnGUIwithReturn(() -> new LearnThrottleFrame(wf));
        DebugThrottle throttle = new DebugThrottle(new DccLocoAddress(3, false),
            InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        ThreadingUtil.runOnGUI(() -> ltf.notifyThrottleFound(throttle));
        int before = ThreadingUtil.runOnGUIwithReturn(() -> speedCommands(wf));

        throttle.setSpeedSetting(0.5f); // on the test thread
        ThreadingUtil.runOnGUI(() -> { }); // the panel's update, passed to the GUI thread, has run

        Assertions.assertEquals(before + 1, (int) ThreadingUtil.runOnGUIwithReturn(() -> speedCommands(wf)),
            "one speed command recorded for one change");
        ThreadingUtil.runOnGUI(() -> {
            ltf.dispose();
            wf.dispose();
        });
    }

    private static int speedCommands(WarrantFrame wf) {
        return (int) wf.getThrottleCommands().stream()
            .filter(ts -> ts.getCommand() == ThrottleSetting.Command.SPEED).count();
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
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
