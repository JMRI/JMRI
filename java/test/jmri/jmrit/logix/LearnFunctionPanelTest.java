package jmri.jmrit.logix;

import java.io.File;
import java.io.IOException;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrix.debugthrottle.DebugThrottle;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Pete Cressman Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LearnFunctionPanelTest {

    @Test
    public void testCTor() {

        WarrantFrame wf = new WarrantFrame(new Warrant("IW0", "AllTestWarrant"));
        LearnThrottleFrame ltf = new LearnThrottleFrame(wf);
        LearnFunctionPanel t = new LearnFunctionPanel(ltf);
        Assertions.assertNotNull( t, "exists");
        JUnitUtil.dispose(ltf);
        JUnitUtil.dispose(wf);
    }

    /**
     * A throttle tells its listeners of a change on the thread that made it, here
     * the test thread, as a Warrant or a script does. The learn panel records a
     * function change once, though the panel shows it on the GUI thread (Swing
     * thread).
     */
    @Test
    public void testFunctionChangeOffGuiThreadRecordedOnce() {
        WarrantFrame wf = ThreadingUtil.runOnGUIwithReturn(() -> new WarrantFrame(new Warrant("IW0", "AllTestWarrant")));
        LearnThrottleFrame ltf = ThreadingUtil.runOnGUIwithReturn(() -> new LearnThrottleFrame(wf));
        DebugThrottle throttle = new DebugThrottle(new DccLocoAddress(3, false),
            InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        ThreadingUtil.runOnGUI(() -> ltf.notifyThrottleFound(throttle));
        int before = ThreadingUtil.runOnGUIwithReturn(() -> functionCommands(wf));

        throttle.setFunction(1, true); // on the test thread
        ThreadingUtil.runOnGUI(() -> { }); // the panel's update, passed to the GUI thread, has run

        Assertions.assertEquals(before + 1, (int) ThreadingUtil.runOnGUIwithReturn(() -> functionCommands(wf)),
            "one function command recorded for one change");
        ThreadingUtil.runOnGUI(() -> {
            ltf.dispose();
            wf.dispose();
        });
    }

    private static int functionCommands(WarrantFrame wf) {
        return (int) wf.getThrottleCommands().stream()
            .filter(ts -> ts.getCommand() == ThrottleSetting.Command.FKEY).count();
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
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }
}
