package jmri.jmrit.trainrecord.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.InstanceManager;
import jmri.TrainRecordManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The Train Tracking menu's check box for using Block values passed along.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class FollowedValuesMenuItemTest {

    @Test
    public void testShowsAndChangesTheSetting() {
        FollowedValuesMenuItem item = ThreadingUtil.runOnGUIwithReturn(() -> new FollowedValuesMenuItem());
        assertFalse(InstanceManager.containsDefault(TrainRecordManager.class), "making the menu starts no tracking");
        assertEquals(Bundle.getMessage("MenuItemFollowedValues"), item.getText());
        assertFalse(item.isSelected(), "off unless turned on");

        ThreadingUtil.runOnGUI(() -> item.doClick());
        TrainRecordManager manager = InstanceManager.getDefault(TrainRecordManager.class);
        assertTrue(manager.isFollowedValuesUsed(), "checked: turned on");

        ThreadingUtil.runOnGUI(() -> item.doClick());
        assertFalse(manager.isFollowedValuesUsed(), "unchecked: turned off");

        ThreadingUtil.runOnLayout(() -> manager.setFollowedValuesUsed(true));
        assertTrue(ThreadingUtil.runOnGUIwithReturn(() -> new FollowedValuesMenuItem().isSelected()),
                "a new check box shows the setting");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
