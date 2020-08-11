package jmri.jmrit.timetable.swing;

import java.util.Locale;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the Bundle Class
 * @author Dave Sand Copyright (C) 2018
 */
public class BundleTest {

    @Test
    public void testGoodKeyMessage() {
        Assert.assertEquals("Open Timetable", Bundle.getMessage("TimeTableAction"));  // NOI18N
    }

    @Test
    public void testBadKeyMessage() {
        Assert.assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));  // NOI18N
    }

    @Test
    public void testGoodKeyMessageArg() {
        Assert.assertEquals("Open Timetable", Bundle.getMessage("TimeTableAction", new Object[]{}));  // NOI18N
        Assert.assertEquals("One -- Two", Bundle.getMessage("LabelTrain", "One", "Two"));  // NOI18N
    }

    @Test
    public void testBadKeyMessageArg() {
        Assert.assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT", new Object[]{}));  // NOI18N
    }

    @Test public void testLocaleMessage() {
        Assert.assertEquals("Hintergrund:", Bundle.getMessage(Locale.GERMANY, "setBackground"));  // NOI18N
    }

    @Test public void testLocaleMessageArg() {
        Assert.assertEquals("Hintergrund:", Bundle.getMessage(Locale.GERMANY, "setBackground", new Object[]{}));  // NOI18N
        // Using escape for u-with
        Assert.assertEquals("ID-Nummer 1", Bundle.getMessage(Locale.GERMANY, "IDnumber", 1));  // NOI18N
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
