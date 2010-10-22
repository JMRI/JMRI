/**
 * EasyDccConsistManagerTest.java
 *
 * Description:	    tests for the jmri.jmrix.nce.EasyDccConsistManager class
 * @author			Paul Bender
 * @version
 */

package jmri.jmrix.easydcc;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

public class EasyDccConsistManagerTest extends TestCase {

        public void testCtor() {
          EasyDccConsistManager m = new EasyDccConsistManager();
          Assert.assertNotNull(m);
        }

	// from here down is testing infrastructure

	public EasyDccConsistManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {EasyDccConsistManagerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(EasyDccConsistManagerTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
 	
 	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EasyDccConsistManagerTest.class.getName());

}
