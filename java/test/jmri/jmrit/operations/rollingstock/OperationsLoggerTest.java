// OperationsLoggerTest.java

package jmri.jmrit.operations.rollingstock;

import java.io.File;
import java.util.Locale;

import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Operations Logger class
 * 
 * @author	Dan Boudreau Copyright (C) 2010
 * 
 */

public class OperationsLoggerTest extends TestCase {

	// test creation
	public void testCreate() {
		// log cars
		Setup.setCarLoggerEnabled(true);
		// turn on logging
		RollingStockLogger.instance().enableCarLogging(true);
		
		// confirm creation of directory
		File dir = new File(RollingStockLogger.instance().getDirectoryName());		
		Assert.assertTrue("directory exists", dir.exists());
		File file = new File(RollingStockLogger.instance().getFullLoggerFileName());	
		Assert.assertTrue("file exists", file.exists());
		
		// now delete file
		Assert.assertTrue("delete file", file.delete());
		// now delete directory
		dir.delete();
		//Assert.assertTrue("delete directory", dir.delete()); TODO fails on some machines?
		
	}

    // Ensure minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        
		// set the locale to US English
		Locale.setDefault(Locale.ENGLISH);
        
		// Repoint OperationsSetupXml to JUnitTest subdirectory
		String tempstring = OperationsSetupXml.getOperationsDirectoryName();
		if (!tempstring.contains(File.separator+"JUnitTest")){
			OperationsSetupXml.setOperationsDirectoryName("operations"+File.separator+"JUnitTest");
		}
    }

	public OperationsLoggerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsLoggerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsLoggerTest.class);
		return suite;
	}

	Locale defaultLocale = Locale.getDefault();
    // The minimal setup for log4J
    @Override
    protected void tearDown() { 
    	// restore locale
    	Locale.setDefault(defaultLocale);
    	apps.tests.Log4JFixture.tearDown(); 
    }
}
