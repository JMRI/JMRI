// RpsPositionIconTest.java

package jmri.jmrix.rps;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the rps.Reading class.
 * @author	Bob Jacobsen Copyright 2006
 * @version	$Revision: 1.2 $
 */
public class RpsPositionIconTest extends TestCase {

	public void testCtorAndID() {
        new jmri.configurexml.ConfigXmlManager().load(new java.io.File("java/test/jmri/jmrix/rps/LocationTestPanel.xml"));        
  	}
        
	// from here down is testing infrastructure

	public RpsPositionIconTest(String s) {
            super(s);
	}

	// Main entry point
	static public void main(String[] args) {
            String[] testCaseName = {RpsPositionIconTest.class.getName()};
            junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
            TestSuite suite = new TestSuite(RpsPositionIconTest.class);
            return suite;
	}

}
