// JSpinnerUtilTest.java

package jmri.util;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests for the jmri.util.JSpinnerUtil class.
 * @author	Bob Jacobsen  Copyright 2005
 * @version	$Revision: 1.1 $
 */
public class JSpinnerUtilTest extends TestCase {

    /**
     * Regardless of JVM, a request for a JSpinner should
     * never throw an exception
     */

    public void testNoException() {
        JSpinnerUtil.getJSpinner();
    }

	// from here down is testing infrastructure
    
	public JSpinnerUtilTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {JSpinnerUtilTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(JSpinnerUtilTest.class);
		return suite;
	}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JSpinnerUtilTest.class.getName());

}
