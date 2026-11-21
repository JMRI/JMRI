package jmri.jmrit.trainrecord.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.SensorManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests for the Train Tracking Set-up Checks window.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class SetUpChecksFrameTest {

    private SetUpChecksFrame open() {
        ThreadingUtil.runOnGUI(() -> new SetUpChecksFrame.Action().actionPerformed(null));
        return InstanceManager.getOptionalDefault(SetUpChecksFrame.class).orElse(null);
    }

    @Test
    public void testListsFindingsAndChecksAgain() {
        Block b = InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        b.setUserName("Platform 1");
        b.setSensor(InstanceManager.getDefault(SensorManager.class).provideSensor("IS1").getSystemName());

        SetUpChecksFrame f = open();
        assertNotNull(f, "opened");
        assertTrue(f.isVisible(), "shown");
        assertEquals(1, f.getTable().getRowCount(), "one finding: a detected Block with no paths");
        assertEquals("Platform 1", f.getTable().getValueAt(0, 0), "for the Block, by its name");
        assertTrue(f.getTable().getValueAt(0, 1).toString().contains("no paths"), "what is wrong");
        assertTrue(f.getSummary().startsWith("1 problem found"), f.getSummary());

        ThreadingUtil.runOnGUI(() -> b.setSensor(null));
        assertSame(f, open(), "the same window, checked again");
        assertEquals(0, f.getTable().getRowCount(), "nothing found once the Block has no detector");
        assertEquals(Bundle.getMessage("SetUpChecksNone"), f.getSummary());

        ThreadingUtil.runOnGUI(f::dispose);
        assertFalse(InstanceManager.getOptionalDefault(SetUpChecksFrame.class).isPresent(), "forgotten when closed");
    }

    @Test
    public void testSeveralFound() {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        for (String n : new String[]{"1", "2"}) {
            InstanceManager.getDefault(BlockManager.class).provideBlock("IB" + n)
                    .setSensor(sm.provideSensor("IS" + n).getSystemName());
        }
        SetUpChecksFrame f = open();
        assertNotNull(f, "opened");
        assertEquals(2, f.getTable().getRowCount());
        assertTrue(f.getSummary().startsWith("2 problems found"), f.getSummary());
        ThreadingUtil.runOnGUI(f::dispose);
    }

    @Test
    public void testTrainNamed() {
        ThreadingUtil.runOnGUI(() -> InstanceManager.getDefault(jmri.TrainRecordManager.class).newRecord("Goods"));
        SetUpChecksFrame f = open();
        assertNotNull(f, "opened");
        assertEquals("Goods", f.getTable().getValueAt(0, 0), "for the train, by its name");
        ThreadingUtil.runOnGUI(f::dispose);
    }

    @Test
    public void testInToolsMenu() {
        jmri.jmrit.ToolsMenu tools = ThreadingUtil.runOnGUIwithReturn(jmri.jmrit.ToolsMenu::new);
        javax.swing.JMenu trainTracking = null;
        for (int i = 0; i < tools.getItemCount(); i++) {
            if (tools.getItem(i) instanceof javax.swing.JMenu
                    && SetUpChecksFrame.menuText().equals(tools.getItem(i).getText())) {
                trainTracking = (javax.swing.JMenu) tools.getItem(i);
            }
        }
        javax.swing.JMenu found = java.util.Objects.requireNonNull(trainTracking, "a Train Tracking submenu");
        java.util.List<String> items = new java.util.ArrayList<>();
        for (int i = 0; i < found.getItemCount(); i++) {
            items.add(found.getItem(i).getText());
        }
        assertTrue(items.contains(Bundle.getMessage("MenuItemSetUpChecks")), "Set-up Checks is in it: " + items);
        assertTrue(items.contains(Bundle.getMessage("MenuItemWarnings")), "Warnings is in it: " + items);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
