/**
 * CatalogTest.java
 *
 * Description:	    tests for the jmri.jmrit.catalog package
 * @author			Bob Jacobsen  2009
 * @version         $Revision$
 */

package jmri.jmrit.catalog;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CatalogTest extends TestCase {

	// from here down is testing infrastructure
	public CatalogTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {CatalogTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.jmrit.catalog");   // no tests in this class itself
		suite.addTest(CatalogTreeFSTest.suite());
		suite.addTest(CatalogTreeIndexTest.suite());
		return suite;
	}

}
