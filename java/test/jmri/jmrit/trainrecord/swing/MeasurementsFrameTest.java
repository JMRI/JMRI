package jmri.jmrit.trainrecord.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.TrainRecordManager;
import jmri.jmrit.trainrecord.TrainTrackingMeasurements;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 * The Measurements window lists what train tracking has measured.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class MeasurementsFrameTest {

    @Test
    public void testDropOutListed() {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        Block a = new Block("IB1", "Yard Road 2");
        a.setSensor("IS1");
        sm.provideSensor("IS1");
        InstanceManager.getDefault(BlockManager.class).register(a);
        TrainTrackingMeasurements measurements = InstanceManager.getDefault(TrainTrackingMeasurements.class);

        new MeasurementsFrame.Action().actionPerformed(null);
        JFrameOperator f = new JFrameOperator(Bundle.getMessage("MeasurementsTitle"));
        JTableOperator table = new JTableOperator(f);
        new org.netbeans.jemmy.operators.JLabelOperator(f, Bundle.getMessage("MeasurementsIntroSimulated"));
        assertEquals(0, table.getRowCount(), "nothing measured yet");

        ThreadingUtil.runOnLayout(() -> {
            sm.provideSensor("IS1").setCommandedState(Sensor.ACTIVE);
            InstanceManager.getDefault(TrainRecordManager.class).newRecord("T1").place(List.of(a), false);
            sm.provideSensor("IS1").setCommandedState(Sensor.INACTIVE);
        });
        JUnitUtil.waitFor(() -> measurements.getDropOuts(a) == 1, "a drop-out");
        JUnitUtil.waitFor(() -> table.getRowCount() == 1, "the Block listed");
        assertEquals("Yard Road 2", table.getValueAt(0, 0));
        assertEquals(0, table.getValueAt(0, 3), "no crossings");
        assertEquals(1, table.getValueAt(0, 6), "one drop-out");
        JUnitUtil.dispose(f.getWindow());
    }

    @Test
    public void testInToolsMenu() {
        jmri.jmrit.ToolsMenu tools = ThreadingUtil.runOnGUIwithReturn(jmri.jmrit.ToolsMenu::new);
        List<String> items = new java.util.ArrayList<>();
        for (int i = 0; i < tools.getItemCount(); i++) {
            if (tools.getItem(i) instanceof javax.swing.JMenu
                    && SetUpChecksFrame.menuText().equals(tools.getItem(i).getText())) {
                javax.swing.JMenu trainTracking = (javax.swing.JMenu) tools.getItem(i);
                for (int j = 0; j < trainTracking.getItemCount(); j++) {
                    items.add(trainTracking.getItem(j).getText());
                }
            }
        }
        assertTrue(items.contains(Bundle.getMessage("MenuItemMeasurements")),
                "Measurements is in the Train Tracking submenu: " + items);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        // no connections, so no layout hardware: the internal sensors are simulated trains' detectors
        JUnitUtil.initConnectionConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
