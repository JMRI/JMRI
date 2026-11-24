package jmri.jmrit.simulation.link;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.*;

/**
 * Tests for the Bundle class.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class BundleTest {

    @Test
    public void testGoodKeysMessage() {
        assertEquals("None (instant)", Bundle.getMessage("PresetNone"));
        assertEquals("the end of the track", Bundle.getMessage("EndOfTrackName"), "from the parent bundle");
    }

    @Test
    public void testBadKeyMessage() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
    }

    @Test
    public void testGoodKeysMessageArg() {
        assertEquals("Buffer of 16, waits when full", Bundle.getMessage("StageBufferBlock", 16));
    }
}
