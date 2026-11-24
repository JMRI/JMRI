package jmri.jmrit.simulation.link.swing;

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
        assertEquals("Link Timing", Bundle.getMessage("MenuLinkTiming"));
        assertEquals("None (instant)", Bundle.getMessage("PresetNone"), "from the parent bundle");
    }

    @Test
    public void testBadKeyMessage() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
    }
}
