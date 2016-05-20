// PackageTest.java

package jmri.jmrit.log;

import junit.framework.*;
import org.apache.log4j.Logger;

/**
 * Invokes complete set of tests in the jmri.jmrit.log tree
 *
 * @author	    Bob Jacobsen  Copyright 2003, 2010
 * @version         $Revision$
 */
public class PackageTest extends TestCase {
    
    public void testShow() {
        Logger.getLogger("jmri.jmrix");
        Logger.getLogger("apps.foo");
        Logger.getLogger("jmri.util");

        if (!System.getProperty("jmri.headlesstest","false").equals("true")) {
    
            try { new jmri.util.swing.JmriNamedPaneAction("Log4J Tree", 
                new jmri.util.swing.sdi.JmriJFrameInterface(),
                "jmri.jmrit.log.Log4JTreePane").actionPerformed(null);
            } catch (Exception e) {}
        }
    }
    
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
        return new TestSuite(PackageTest.class);
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
