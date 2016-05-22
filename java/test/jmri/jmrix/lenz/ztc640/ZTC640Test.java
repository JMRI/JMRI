// ZTC640Test.java


package jmri.jmrix.lenz.ztc640;

import org.apache.log4j.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.ztc640 package
 * @author                      Paul Bender  
 * @version                     $Revision$
 */
public class ZTC640Test extends TestCase {

    // from here down is testing infrastructure

    public ZTC640Test(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ZTC640Test.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.ztc640.ZTC640Test");  // no tests in this class itself
        suite.addTest(new TestSuite(ZTC640AdapterTest.class));
        suite.addTest(new TestSuite(ZTC640XNetPacketizerTest.class));
        return suite;
    }

    static Logger log = Logger.getLogger(ZTC640Test.class.getName());

}

