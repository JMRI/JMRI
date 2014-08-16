// IntlUtilitiesTest.java

package jmri.util;

import org.apache.log4j.Logger;
import jmri.*;
import jmri.implementation.AbstractTurnout;
import junit.framework.*;
import java.util.*;

/**
 * Tests for the jmri.util.IntlUtilities class.
 * @author	Bob Jacobsen  Copyright 20014
 * @version	$Revision$
 */
public class IntlUtilitiesTest extends TestCase {


    public void testFloatInEnglish() throws java.text.ParseException {
        Assert.assertEquals("1.0", 1.0f, jmri.util.IntlUtilities.floatValue("1.0"));
        Assert.assertEquals("2.3", 2.3f, jmri.util.IntlUtilities.floatValue("2.3"));
    }

    public void testFloatInItalian() throws java.text.ParseException {
        Locale startingLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ITALY);
            Assert.assertEquals("1,0", 1.0f, jmri.util.IntlUtilities.floatValue("1,0"));            
            Assert.assertEquals("2,3", 2.3f, jmri.util.IntlUtilities.floatValue("2,3"));            
        } finally {
            Locale.setDefault(startingLocale);
        }
    }

    
    public void testDoubleInEnglish() throws java.text.ParseException {
        Assert.assertEquals("1.0", 1.0, jmri.util.IntlUtilities.doubleValue("1.0"));
        Assert.assertEquals("2.3", 2.3, jmri.util.IntlUtilities.doubleValue("2.3"));
    }

    public void testDoubleInItalian() throws java.text.ParseException {
        Locale startingLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ITALY);
            Assert.assertEquals("1,0", 1.0, jmri.util.IntlUtilities.doubleValue("1,0"));            
            Assert.assertEquals("2,3", 2.3, jmri.util.IntlUtilities.doubleValue("2,3"));            
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
		String[] testCaseName = {IntlUtilitiesTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(IntlUtilitiesTest.class);
		return suite;
	}

	static Logger log = Logger.getLogger(IntlUtilitiesTest.class.getName());

}
