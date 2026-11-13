package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.*;

/**
 * Tests for the Bundle class.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class BundleTest {

    @Test
    public void testGoodKeysMessage() {
        assertEquals("the end of the track", Bundle.getMessage("EndOfTrackName"));
        assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"), "from the parent bundle");
    }

    @Test
    public void testBadKeyMessage() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
    }

    @Test
    public void testGoodKeysMessageArg() {
        assertEquals("Train \"T\" is already placed.", Bundle.getMessage("TrainAlreadyPlaced", "T"));
        assertEquals("The front of the train must be between 0 and 450 mm into \"B\".",
                Bundle.getMessage("FrontOutsideBlock", "B", 450));
    }

    @Test
    public void testBadKeyMessageArg() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT", new Object[]{}));
    }

    @Test
    public void testLocaleMessage() {
        assertEquals("Scambio", Bundle.getMessage(Locale.ITALY, "BeanNameTurnout"));
        assertEquals("Train \"T\" is not placed.", Bundle.getMessage(Locale.ENGLISH, "TrainNotPlaced", "T"));
    }
}
