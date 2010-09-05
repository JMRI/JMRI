// PackageTest.java

package jmri.jmrit.log;

import junit.framework.*;

/**
 * Invokes complete set of tests in the jmri.jmrit.log tree
 *
 * @author	    Bob Jacobsen  Copyright 2003
 * @version         $Revision: 1.2 $
 */
public class PackageTest extends TestCase {
    
    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.util.swing.PackageTest");   // no tests in this class itself

        org.apache.log4j.Logger.getLogger("jmri.jmrix");
        org.apache.log4j.Logger.getLogger("apps.foo");
        org.apache.log4j.Logger.getLogger("jmri.util");

        try { new jmri.util.swing.JmriNamedPaneAction("Log4J Tree", 
            new jmri.util.swing.sdi.JmriJFrameInterface(),
            "jmri.jmrit.log.Log4JTreePane").actionPerformed(null);
        } catch (Exception e) {}
        
            //suite.addTest(jmri.util.swing.mdi.PackageTest.suite());

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
