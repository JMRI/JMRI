// IntlUtilitiesTest.java
package jmri.util;

import java.util.Locale;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.util.IntlUtilities class.
 *
 * @author	Bob Jacobsen Copyright 20014
 * @version	$Revision$
 */
public class IntlUtilitiesTest extends TestCase {

    public void testFloatInUSEnglish() throws java.text.ParseException {
        Locale startingLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            Assert.assertEquals("1.0", 1.0f, IntlUtilities.floatValue("1.0"));
            Assert.assertEquals("2.3", 2.3f, IntlUtilities.floatValue("2.3"));
        } finally {
            Locale.setDefault(startingLocale);
        }
    }

    public void testFloatInItalyItalian() throws java.text.ParseException {
        Locale startingLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ITALY);
            Assert.assertEquals("1,0", 1.0f, IntlUtilities.floatValue("1,0"));
            Assert.assertEquals("2,3", 2.3f, IntlUtilities.floatValue("2,3"));
        } finally {
            Locale.setDefault(startingLocale);
        }
    }

    public void testDoubleInUSEnglish() throws java.text.ParseException {
        Locale startingLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            Assert.assertEquals("1.0", 1.0, IntlUtilities.doubleValue("1.0"));
            Assert.assertEquals("2.3", 2.3, IntlUtilities.doubleValue("2.3"));
        } finally {
            Locale.setDefault(startingLocale);
        }
    }

    public void testDoubleInItalyItalian() throws java.text.ParseException {
        Locale startingLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ITALY);
            Assert.assertEquals("1,0", 1.0, IntlUtilities.doubleValue("1,0"));
            Assert.assertEquals("2,3", 2.3, IntlUtilities.doubleValue("2,3"));
        } finally {
            Locale.setDefault(startingLocale);
        }
    }

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

    // from here down is testing infrastructure
    public IntlUtilitiesTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", IntlUtilitiesTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(IntlUtilitiesTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(IntlUtilitiesTest.class.getName());

}
