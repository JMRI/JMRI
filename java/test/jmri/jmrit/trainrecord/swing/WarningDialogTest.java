package jmri.jmrit.trainrecord.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.TrainRecordManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.trainrecord.TrainTrackingWarnings;
import jmri.jmrit.trainrecord.TrainTrackingWarnings.Warning;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 * A window opens by itself only for a stalled or lost train, and not when that
 * is turned off in Preferences &gt; Messages. Occupancy that no train explains
 * opens no window: the Warnings window lists it, and its "Nothing: Detector
 * Stuck" marks the detector untrusted, to be trusted again there.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class WarningDialogTest {

    @Test
    public void testWhichWarningsOpenAWindow() {
        Block b = new Block("IB1", "A");
        jmri.TrainRecord r = InstanceManager.getDefault(TrainRecordManager.class).newRecord("T1");
        Warning unexplained = new Warning(Warning.Kind.UNEXPLAINED, b, null);
        Warning stalled = new Warning(Warning.Kind.STALLED, b, r);
        Warning lost = new Warning(Warning.Kind.LOST, b, r);

        assertFalse(WarningDialog.opensWindow(unexplained), "unexplained occupancy: listed, no window");
        assertTrue(WarningDialog.opensWindow(stalled), "stalled: a window by default");
        assertTrue(WarningDialog.opensWindow(lost), "lost: a window by default");

        UserPreferencesManager p = InstanceManager.getDefault(UserPreferencesManager.class);
        p.setPreferenceState(WarningDialog.Preferences.class.getName(), WarningDialog.HIDE_WINDOWS, true);
        assertFalse(WarningDialog.opensWindow(stalled), "stalled: no window once turned off");
        assertFalse(WarningDialog.opensWindow(lost), "lost: no window once turned off");

        p.setPreferenceState(WarningDialog.Preferences.class.getName(), WarningDialog.HIDE_WINDOWS, false);
        assertTrue(WarningDialog.opensWindow(stalled), "stalled: a window again once turned back on");
    }

    @Test
    public void testPreferenceNamedInMessages() {
        UserPreferencesManager p = InstanceManager.getDefault(UserPreferencesManager.class);
        new WarningDialog.Preferences().setMessagePreferencesDetails();
        assertEquals(Bundle.getMessage("WarningPreferences"), new WarningDialog.Preferences().getClassDescription());
        assertEquals(Bundle.getMessage("WarningHideWindows"),
                p.getPreferenceItemDescription(WarningDialog.Preferences.class.getName(), WarningDialog.HIDE_WINDOWS));
    }

    @Test
    @DisabledIfHeadless
    public void testUnexplainedListedAndDetectorStuckThere() {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        Block a = new Block("IB1", "A");
        Block far = new Block("IB2", "B7 Goods Loop");
        a.setSensor(sm.provideSensor("IS1").getSystemName());
        far.setSensor(sm.provideSensor("IS2").getSystemName());
        InstanceManager.getDefault(jmri.BlockManager.class).register(a);
        InstanceManager.getDefault(jmri.BlockManager.class).register(far);
        TrainRecordManager manager = InstanceManager.getDefault(TrainRecordManager.class);
        TrainTrackingWarnings warnings = InstanceManager.getDefault(TrainTrackingWarnings.class);
        WarningDialog.start();
        ThreadingUtil.runOnLayout(() -> {
            sm.provideSensor("IS1").setCommandedState(Sensor.ACTIVE);
            manager.newRecord("T1").place(List.of(a), false);
            sm.provideSensor("IS2").setCommandedState(Sensor.ACTIVE);
        });
        JUnitUtil.waitFor(() -> warnings.getWarnings().size() == 1, "an unexplained occupancy warning");
        // the window, if any, is opened from the GUI queue after the warning is added: let that queue run first
        ThreadingUtil.runOnGUI(() -> {
        });
        ThreadingUtil.runOnGUI(() -> {
        });
        assertNull(JmriJFrame.getFrame(Bundle.getMessage("WarningDialogTitle")), "no window opened by itself");

        ThreadingUtil.runOnGUI(() -> new WarningsFrame.Action().actionPerformed(null));
        JFrameOperator frame = new JFrameOperator(Bundle.getMessage("WarningsTitle"));
        JTableOperator listed = new JTableOperator(frame, 0);
        JUnitUtil.waitFor(() -> listed.getRowCount() == 1, "listed in the Warnings window");
        listed.selectCell(0, 0);
        new JButtonOperator(frame, Bundle.getMessage("WarningDetectorStuck")).doClick();
        JUnitUtil.waitFor(() -> manager.isUntrusted(far), "marked untrusted");

        JTableOperator untrusted = new JTableOperator(frame, 1);
        JUnitUtil.waitFor(() -> untrusted.getRowCount() == 1, "listed as untrusted");
        untrusted.selectCell(0, 0);
        new JButtonOperator(frame, Bundle.getMessage("WarningsTrustAgain")).doClick();
        JUnitUtil.waitFor(() -> !manager.isUntrusted(far), "trusted again");
        assertTrue(manager.getUntrusted().isEmpty());
        JUnitUtil.dispose(frame.getWindow());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
