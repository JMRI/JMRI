package jmri.util;

import java.util.Locale;

import org.junit.*;

/**
 * Tests for the jmri.util.IntlUtilities class.
 *
 * @author	Bob Jacobsen Copyright 2014
 */
public class IntlUtilitiesTest {

    @Test
    public void testFloatInUSEnglish() throws java.text.ParseException {
        Locale startingLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            Assert.assertEquals("1.0", 1.0f, IntlUtilities.floatValue("1.0"), 0.0);
            Assert.assertEquals("2.3", 2.3f, IntlUtilities.floatValue("2.3"), 0.0);
        } finally {
            Locale.setDefault(startingLocale);
        }
    }

    @Test
    public void testFloatInItalyItalian() throws java.text.ParseException {
        Locale startingLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ITALY);
            Assert.assertEquals("1,0", 1.0f, IntlUtilities.floatValue("1,0"), 0.0);
            Assert.assertEquals("2,3", 2.3f, IntlUtilities.floatValue("2,3"), 0.0);
        } finally {
            Locale.setDefault(startingLocale);
        }
    }

    @Test
    public void testDoubleInUSEnglish() throws java.text.ParseException {
        Locale startingLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            Assert.assertEquals("1.0", 1.0, IntlUtilities.doubleValue("1.0"), 0.0);
            Assert.assertEquals("2.3", 2.3, IntlUtilities.doubleValue("2.3"), 0.0);
        } finally {
            Locale.setDefault(startingLocale);
        }
    }

    @Test
    public void testDoubleInItalyItalian() throws java.text.ParseException {
        Locale startingLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ITALY);
            Assert.assertEquals("1,0", 1.0, IntlUtilities.doubleValue("1,0"), 0.0);
            Assert.assertEquals("2,3", 2.3, IntlUtilities.doubleValue("2,3"), 0.0);
        } finally {
            Locale.setDefault(startingLocale);
        }
    }

    @Test
    public void testOutputAssumption() {
        // tests the assumption that output requires using specific formatting
        // because String.valueOf() doesn't do I18N
        Locale startingLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            Assert.assertEquals("US outputs as 2.3", "2.3", String.valueOf(2.3));
            Locale.setDefault(Locale.ITALY);
            Assert.assertEquals("ITALY outputs as 2.3", "2.3", String.valueOf(2.3));
        } finally {
            Locale.setDefault(startingLocale);
        }
    }

    @Test
    public void testStringInUSEnglish() {
        Locale startingLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            Assert.assertEquals("1.1", "1.1", IntlUtilities.valueOf(1.1));
            Assert.assertEquals("1.1f", "1.1", IntlUtilities.valueOf(1.1f));
            Assert.assertEquals("2.3", "2.3", IntlUtilities.valueOf(2.3));
            Assert.assertEquals("2.3f", "2.3", IntlUtilities.valueOf(2.3f));
            Assert.assertEquals("5", "5", IntlUtilities.valueOf(5));
            Assert.assertEquals("1", "1", IntlUtilities.valueOf(1));
        } finally {
            Locale.setDefault(startingLocale);
        }
    }

    @Test
    public void testStringInItalyItalian() {
        Locale startingLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ITALY);
            Assert.assertEquals("1.1", "1,1", IntlUtilities.valueOf(1.1));
            Assert.assertEquals("1.1f", "1,1", IntlUtilities.valueOf(1.1f));
            Assert.assertEquals("2.3", "2,3", IntlUtilities.valueOf(2.3));
            Assert.assertEquals("2.3f", "2,3", IntlUtilities.valueOf(2.3f));
            Assert.assertEquals("5", "5", IntlUtilities.valueOf(5));
            Assert.assertEquals("1", "1", IntlUtilities.valueOf(1));
        } finally {
            Locale.setDefault(startingLocale);
        }
    }

}
