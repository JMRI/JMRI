// PackageTest.java

package jmri;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invoke complete set of tests for the Jmri package
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002, 2007
 * @version         $Revision$
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
        TestSuite suite = new TestSuite("jmri.JmriTest");  // no tests in this class itself

		suite.addTest(jmri.BeanSettingTest.suite());
        suite.addTest(jmri.NamedBeanHandleManagerTest.suite());
		suite.addTest(jmri.BlockTest.suite());
		suite.addTest(jmri.BlockManagerTest.suite());
        suite.addTest(jmri.DccLocoAddressTest.suite());
        suite.addTest(jmri.DccConsistTest.suite());
		suite.addTest(jmri.InstanceManagerTest.suite());
		suite.addTest(jmri.LightTest.suite());
        suite.addTest(jmri.NmraPacketTest.suite());
		suite.addTest(jmri.PathTest.suite());
		suite.addTest(jmri.PushbuttonPacketTest.suite());
		suite.addTest(jmri.TurnoutTest.suite());
                suite.addTest(jmri.ApplicationTest.suite());
                suite.addTest(jmri.AudioTest.suite());
                suite.addTest(jmri.IdTagTest.suite());
                suite.addTest(jmri.VersionTest.suite());
        
        if (!System.getProperty("jmri.headlesstest","false").equals("true"))
            suite.addTest(jmri.progdebugger.PackageTest.suite());
        
        suite.addTest(jmri.configurexml.PackageTest.suite());
		suite.addTest(jmri.implementation.PackageTest.suite());
        suite.addTest(jmri.managers.PackageTest.suite());
        suite.addTest(jmri.jmrix.PackageTest.suite());  // last due to threading issues?
        suite.addTest(jmri.jmrit.PackageTest.suite());  // last due to classloader issues?
        suite.addTest(jmri.util.PackageTest.suite());
        suite.addTest(jmri.web.PackageTest.suite());
        suite.addTest(jmri.jmris.PackageTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
