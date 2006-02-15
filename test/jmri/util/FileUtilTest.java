// FileUtilTest.java

package jmri.util;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.util.FileUtil class.
 * @author	Bob Jacobsen  Copyright 2003
 * @version	$Revision: 1.1 $
 */
public class FileUtilTest extends TestCase {


    public void testVoid() {
    }


	// from here down is testing infrastructure

	public FileUtilTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {FileUtilTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(FileUtilTest.class);
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(FileUtilTest.class.getName());

}
