package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;

import jmri.InstanceManager;
import jmri.ScaleManager;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.trainrecord.TrainTrackingChecks;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Finding;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Kind;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Dispatcher's layout scale is checked against the Warrant preferences'
 * while Dispatcher is open, and the finding is listed with train tracking's
 * set-up checks.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DispatcherScaleCheckTest {

    private static List<Finding> check() {
        return ThreadingUtil.runOnGUIwithReturn(() -> new DispatcherScaleCheck().check());
    }

    @Test
    public void testNothingWithoutDispatcher() {
        InstanceManager.getDefault(SignalSpeedMap.class).setLayoutScale(22.5f);
        assertTrue(check().isEmpty(), "Dispatcher isn't open, so its scale isn't known");
    }

    @Test
    @DisabledIfHeadless
    public void testScalesDisagree() {
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        SignalSpeedMap map = InstanceManager.getDefault(SignalSpeedMap.class);
        try {
            d.setScale(Objects.requireNonNull(ScaleManager.getScale("HO")));
            map.setLayoutScale(87.1f);
            assertTrue(check().isEmpty(), "both HO");
            map.setLayoutScale(87f);
            assertTrue(check().isEmpty(), "87 and 87.1 are both HO");

            map.setLayoutScale(76.2f);
            List<Finding> found = check();
            assertEquals(1, found.size());
            Finding f = found.get(0);
            assertEquals(Kind.SETTINGS_DISAGREE, f.getKind());
            assertEquals(Bundle.getMessage("TitleDispatcher"), f.getSubject());
            assertTrue(f.getMessage().contains("HO (1:87.1)"), f.getMessage());
            assertTrue(f.getMessage().contains("UK-OO (1:76.2)"), f.getMessage());

            map.setLayoutScale(100f);
            f = check().get(0);
            assertTrue(f.getMessage().contains("HO (1:87.1)"), f.getMessage());
            assertTrue(f.getMessage().contains("1:100") && !f.getMessage().contains("(1:100)"),
                    "no named scale is 1:100: " + f.getMessage());
            map.setLayoutScale(76.2f);

            // listed with train tracking's own checks
            List<Finding> all = ThreadingUtil.runOnGUIwithReturn(() -> TrainTrackingChecks.run());
            assertEquals(1, all.stream().filter(x -> x.getKind() == Kind.SETTINGS_DISAGREE).count(), all.toString());

            d.setScale(Objects.requireNonNull(ScaleManager.getScale("UK-OO")));
            assertTrue(check().isEmpty(), "both 1:76.2");
        } finally {
            new JFrameOperator(Bundle.getMessage("TitleDispatcher")).requestClose();
            JUnitUtil.dispose(d);
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
        // the options the Dispatcher reads when it is created: Auto Allocate off,
        // whatever options file a test run before this one left set
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }
}
