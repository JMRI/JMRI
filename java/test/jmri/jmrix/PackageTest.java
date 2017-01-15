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
        suite.addTest(new junit.framework.JUnit4TestAdapter(AbstractMRTrafficControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AbstractMRNodeTrafficControllerTest.class));

        suite.addTest(jmri.jmrix.ActiveSystemFlagTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(AbstractMonPaneTest.class));
        suite.addTest(jmri.jmrix.AbstractProgrammerTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(AbstractPortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AbstractNetworkPortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AbstractStreamPortControllerTest.class));
        suite.addTest(jmri.jmrix.AbstractMRReplyTest.suite());
        suite.addTest(new TestSuite(jmri.jmrix.AbstractThrottleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.ConnectionConfigManagerTest.class));

        suite.addTest(jmri.jmrix.acela.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.bachrus.PackageTest.class));
        suite.addTest(jmri.jmrix.can.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.configurexml.PackageTest.class));
        //suite.addTest(jmri.jmrix.cmri.serial.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.cmri.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.dcc.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.dcc4pc.PackageTest.class));
        suite.addTest(jmri.jmrix.direct.PackageTest.suite());
        suite.addTest(jmri.jmrix.dccpp.PackageTest.suite());
        suite.addTest(jmri.jmrix.easydcc.PackageTest.suite());
        suite.addTest(jmri.jmrix.ecos.PackageTest.suite());
        suite.addTest(jmri.jmrix.grapevine.PackageTest.suite());
        suite.addTest(jmri.jmrix.ieee802154.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.internal.PackageTest.class));
        suite.addTest(jmri.jmrix.jmriclient.PackageTest.suite());
        suite.addTest(jmri.jmrix.lenz.PackageTest.suite());
        suite.addTest(jmri.jmrix.loconet.PackageTest.suite());
        suite.addTest(jmri.jmrix.maple.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.marklin.PackageTest.class));
        suite.addTest(jmri.jmrix.modbus.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.mrc.PackageTest.class));
        suite.addTest(jmri.jmrix.nce.PackageTest.suite());
        suite.addTest(jmri.jmrix.oaktree.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.openlcb.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.pi.PackageTest.class));
        suite.addTest(jmri.jmrix.powerline.PackageTest.suite());
        suite.addTest(jmri.jmrix.pricom.PackageTest.suite());
        suite.addTest(jmri.jmrix.qsi.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.rfid.PackageTest.class));
        suite.addTest(jmri.jmrix.roco.PackageTest.suite());
        suite.addTest(jmri.jmrix.rps.PackageTest.suite());
        suite.addTest(jmri.jmrix.secsi.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.sprog.PackageTest.class));
        suite.addTest(jmri.jmrix.srcp.PackageTest.suite());
        suite.addTest(jmri.jmrix.tams.PackageTest.suite());
        suite.addTest(jmri.jmrix.tmcc.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.wangrow.PackageTest.class));
        suite.addTest(jmri.jmrix.xpa.PackageTest.suite());
        suite.addTest(jmri.jmrix.zimo.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.jinput.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.serialsensor.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.ncemonitor.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AbstractMRTrafficControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NetworkConfigExceptionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialConfigExceptionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ConnectionStatusTest.class));
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
