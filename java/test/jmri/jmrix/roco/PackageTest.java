// PackageTest.java


package jmri.jmrix.roco;

import org.apache.log4j.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.roco package
 * @author			Bob Jacobsen
 * @version			$Revision$
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure

    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.roco.RocoTest");  // no tests in this class itself
        suite.addTest(new TestSuite(RocoConnectionTypeListTest.class));
        suite.addTest(jmri.jmrix.roco.z21.z21Test.suite());
        
        if (!System.getProperty("jmri.headlesstest","false").equals("true")) {
            suite.addTest(jmri.jmrix.lenz.swing.SwingTest.suite());
        }
        
        return suite;
    }

    static Logger log = Logger.getLogger(PackageTest.class.getName());

}
