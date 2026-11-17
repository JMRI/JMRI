package jmri.jmrit.simulation.swing;

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
        assertEquals("Place Simulated Train", Bundle.getMessage("PlaceTitle"));
    }

    @Test
    public void testBadKeyMessage() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
    }

    @Test
    public void testGoodKeysMessageArg() {
        assertEquals("Remove Simulated Train \"Mail\"", Bundle.getMessage("RemoveTrain", "Mail"));
        assertEquals("Train \"Mail\" is not placed.", Bundle.getMessage("TrainNotPlaced", "Mail"),
                "from the parent package's bundle");
    }

    @Test
    public void testBadKeyMessageArg() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT", new Object[]{}));
    }

    @Test
    public void testLocaleMessage() {
        assertEquals("Scambio", Bundle.getMessage(Locale.ITALY, "BeanNameTurnout"));
        assertEquals("A DCC address from 1 to 10239 is needed.",
                Bundle.getMessage(Locale.ENGLISH, "PlaceAddressNeeded", "10239"));
    }
}
