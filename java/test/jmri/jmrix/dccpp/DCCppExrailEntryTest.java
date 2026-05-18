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
        DCCppExrailEntry e = new DCCppExrailEntry(42, "R", "Station Loop");
        Assertions.assertEquals(42, e.getId());
        Assertions.assertEquals("R", e.getType());
        Assertions.assertEquals("Station Loop", e.getDescription());
        Assertions.assertTrue(e.isRoute());
        Assertions.assertFalse(e.isAutomation());
    }

    @Test
    public void testAutomationEntry() {
        DCCppExrailEntry e = new DCCppExrailEntry(7, "A", "Yard Switcher");
        Assertions.assertEquals(7, e.getId());
        Assertions.assertEquals("A", e.getType());
        Assertions.assertTrue(e.isAutomation());
        Assertions.assertFalse(e.isRoute());
    }

    @Test
    public void testNegativeId() {
        DCCppExrailEntry e = new DCCppExrailEntry(-5, "R", "Reverse Loop");
        Assertions.assertEquals(-5, e.getId());
    }

    @Test
    public void testDisplayNameDefaultsToDescription() {
        DCCppExrailEntry e = new DCCppExrailEntry(1, "R", "My Route");
        Assertions.assertEquals("My Route", e.getDisplayName());
    }

    @Test
    public void testCaptionOverridesDisplayName() {
        DCCppExrailEntry e = new DCCppExrailEntry(1, "R", "My Route");
        e.setCaption("Platform 1");
        Assertions.assertEquals("Platform 1", e.getDisplayName());
        Assertions.assertEquals("Platform 1", e.getCaption());
    }

    @Test
    public void testClearCaptionRestoresDescription() {
        DCCppExrailEntry e = new DCCppExrailEntry(1, "R", "My Route");
        e.setCaption("Platform 1");
        e.setCaption(null);
        Assertions.assertEquals("My Route", e.getDisplayName());
        Assertions.assertNull(e.getCaption());
    }

    @Test
    public void testState() {
        DCCppExrailEntry e = new DCCppExrailEntry(1, "R", "My Route");
        Assertions.assertEquals(-1, e.getState()); // default: unknown
        e.setState(2);
        Assertions.assertEquals(2, e.getState());
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
