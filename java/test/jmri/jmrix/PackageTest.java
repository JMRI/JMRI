package jmri.jmrix;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Set of tests for the jmri.jmrix package
 *
 * @author	Bob Jacobsen Copyright 2003, 2007
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.PackageTest");

        suite.addTest(jmri.jmrix.ActiveSystemFlagTest.suite());
        suite.addTest(jmri.jmrix.AbstractMonPaneTest.suite());
        suite.addTest(jmri.jmrix.AbstractProgrammerTest.suite());
        suite.addTest(jmri.jmrix.AbstractPortControllerTest.suite());
        suite.addTest(jmri.jmrix.AbstractMRReplyTest.suite());
        suite.addTest(new TestSuite(jmri.jmrix.AbstractThrottleTest.class));
        suite.addTest(jmri.jmrix.BundleTest.suite());
        suite.addTest(jmri.jmrix.ConnectionConfigManagerTest.suite());

        suite.addTest(jmri.jmrix.acela.PackageTest.suite());
        suite.addTest(jmri.jmrix.can.PackageTest.suite());
        suite.addTest(jmri.jmrix.cmri.serial.PackageTest.suite());
        suite.addTest(jmri.jmrix.dcc.PackageTest.suite());
        suite.addTest(jmri.jmrix.direct.PackageTest.suite());
        suite.addTest(jmri.jmrix.easydcc.PackageTest.suite());
        suite.addTest(jmri.jmrix.grapevine.PackageTest.suite());
        suite.addTest(jmri.jmrix.jmriclient.PackageTest.suite());
        suite.addTest(jmri.jmrix.lenz.PackageTest.suite());
        suite.addTest(jmri.jmrix.loconet.PackageTest.suite());
        suite.addTest(jmri.jmrix.maple.PackageTest.suite());
        suite.addTest(jmri.jmrix.modbus.PackageTest.suite());
        suite.addTest(jmri.jmrix.nce.PackageTest.suite());
        suite.addTest(jmri.jmrix.oaktree.PackageTest.suite());
        suite.addTest(jmri.jmrix.openlcb.PackageTest.suite());
        suite.addTest(jmri.jmrix.powerline.PackageTest.suite());
        suite.addTest(jmri.jmrix.pricom.PackageTest.suite());
        suite.addTest(jmri.jmrix.qsi.PackageTest.suite());
        suite.addTest(jmri.jmrix.rps.PackageTest.suite());
        suite.addTest(jmri.jmrix.sprog.PackageTest.suite());
        suite.addTest(jmri.jmrix.secsi.PackageTest.suite());
        suite.addTest(jmri.jmrix.tmcc.PackageTest.suite());
        suite.addTest(jmri.jmrix.xpa.PackageTest.suite());
        suite.addTest(jmri.jmrix.srcp.PackageTest.suite());
        suite.addTest(jmri.jmrix.ieee802154.PackageTest.suite());
        suite.addTest(jmri.jmrix.roco.PackageTest.suite());
        suite.addTest(jmri.jmrix.rfid.PackageTest.suite());
        suite.addTest(jmri.jmrix.dccpp.PackageTest.suite());
        suite.addTest(jmri.jmrix.zimo.PackageTest.suite());
        suite.addTest(jmri.jmrix.mrc.PackageTest.suite());
        suite.addTest(jmri.jmrix.ecos.PackageTest.suite());
	
        return suite;

    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
