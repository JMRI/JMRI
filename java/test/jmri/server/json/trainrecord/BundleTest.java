package jmri.server.json.trainrecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * The package's messages, and its parent's.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class BundleTest {

    @Test
    public void testOwnKey() {
        assertEquals("A correction needs \"block\" or \"emptyBlock\".",
                Bundle.getMessage(Locale.ENGLISH, "ErrorCorrectionMissing", "block", "emptyBlock"));
    }

    @Test
    public void testParentKey() {
        assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));
    }

    @Test
    public void testBadKey() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
    }
}
