package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;

/**
 * Tests for DCCppExrailEntry.
 *
 * @author Chad Francis Copyright (C) 2026
 */
public class DCCppExrailEntryTest {

    @Test
    public void testRouteEntry() {
        DCCppExrailEntry entry = new DCCppExrailEntry(42, "R", "Station Loop");
        Assertions.assertEquals(42, entry.getId());
        Assertions.assertEquals("R", entry.getType());
        Assertions.assertEquals("Station Loop", entry.getDescription());
        Assertions.assertTrue(entry.isRoute());
        Assertions.assertFalse(entry.isAutomation());
    }

    @Test
    public void testAutomationEntry() {
        DCCppExrailEntry entry = new DCCppExrailEntry(7, "A", "Yard Switcher");
        Assertions.assertEquals(7, entry.getId());
        Assertions.assertEquals("A", entry.getType());
        Assertions.assertTrue(entry.isAutomation());
        Assertions.assertFalse(entry.isRoute());
    }

    @Test
    public void testNegativeId() {
        DCCppExrailEntry entry = new DCCppExrailEntry(-5, "R", "Reverse Loop");
        Assertions.assertEquals(-5, entry.getId());
    }

    @Test
    public void testDisplayNameDefaultsToDescription() {
        DCCppExrailEntry entry = new DCCppExrailEntry(1, "R", "My Route");
        Assertions.assertEquals("My Route", entry.getDisplayName());
    }

    @Test
    public void testCaptionOverridesDisplayName() {
        DCCppExrailEntry entry = new DCCppExrailEntry(1, "R", "My Route");
        entry.setCaption("Platform 1");
        Assertions.assertEquals("Platform 1", entry.getDisplayName());
        Assertions.assertEquals("Platform 1", entry.getCaption());
    }

    @Test
    public void testClearCaptionRestoresDescription() {
        DCCppExrailEntry entry = new DCCppExrailEntry(1, "R", "My Route");
        entry.setCaption("Platform 1");
        entry.setCaption(null);
        Assertions.assertEquals("My Route", entry.getDisplayName());
        Assertions.assertNull(entry.getCaption());
    }

    @Test
    public void testState() {
        DCCppExrailEntry entry = new DCCppExrailEntry(1, "R", "My Route");
        Assertions.assertNull(entry.getState()); // default: unknown
        entry.setState(DCCppExrailEntry.State.HIDDEN);
        Assertions.assertEquals(DCCppExrailEntry.State.HIDDEN, entry.getState());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
