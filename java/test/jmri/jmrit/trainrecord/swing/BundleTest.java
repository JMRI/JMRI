package jmri.jmrit.trainrecord.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * Tests for the Bundle class.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class BundleTest {

    @Test
    public void testGoodKeysMessage() {
        assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"), "from the parent bundle");
    }

    @Test
    public void testBadKeyMessage() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
    }

    @Test
    public void testGoodKeysMessageArg() {
        assertEquals("Train Tracking", Bundle.getMessage("MenuTrainTracking"));
        assertEquals("Block LB4...", Bundle.getMessage("MenuItemBlockDialog", "LB4"));
        assertEquals("Block \"A\" has a path to Block \"B\", but \"B\" has none back to \"A\".",
                Bundle.getMessage("CheckPathOneWay", "A", "B"), "from the parent package's bundle");
    }

    @Test
    public void testBadKeyMessageArg() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT", new Object[]{}));
    }

    @Test
    public void testLocaleMessage() {
        assertEquals("Scambio", Bundle.getMessage(Locale.ITALY, "BeanNameTurnout"));
        assertEquals("Sensor \"S\" is the detector of more than one Block: A, B.",
                Bundle.getMessage(Locale.ENGLISH, "CheckSensorShared", "S", "A, B"));
    }
}
