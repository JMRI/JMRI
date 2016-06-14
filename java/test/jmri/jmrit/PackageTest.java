package jmri.jmrit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.jmrit tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
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
        TestSuite suite = new TestSuite("jmri.jmrit.PackageTest");   // no tests in this class itself

        // this next line causes obscure AWT errors when here,
        // and errors in other places when later, e.g. in
        // jmri.jmrit.beantable.MemoryTableAction$1.getValue(MemoryTableAction.java:56)
        // suite.addTest(jmri.jmrit.MemoryContentsTest.suite());
        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.jmrit.AbstractIdentifyTest.suite());
        }
        suite.addTest(BundleTest.suite());
        suite.addTest(DccLocoAddressSelectorTest.suite());
        suite.addTest(MemoryContentsTest.suite());
        suite.addTest(SoundTest.suite());
        suite.addTest(XmlFileTest.suite());

        suite.addTest(jmri.jmrit.automat.AutomatTest.suite());
        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.jmrit.beantable.PackageTest.suite());
        }
        suite.addTest(jmri.jmrit.blockboss.PackageTest.suite());
        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.jmrit.catalog.PackageTest.suite());
        }
        suite.addTest(jmri.jmrit.decoderdefn.PackageTest.suite());
        suite.addTest(jmri.jmrit.dispatcher.DispatcherTest.suite());
        suite.addTest(jmri.jmrit.display.PackageTest.suite());
        suite.addTest(jmri.jmrit.jython.PackageTest.suite());
        suite.addTest(jmri.jmrit.log.PackageTest.suite());
        suite.addTest(jmri.jmrit.logix.PackageTest.suite());
        suite.addTest(jmri.jmrit.operations.OperationsTest.suite());
        suite.addTest(jmri.jmrit.progsupport.PackageTest.suite());
        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            //suite.addTest(jmri.jmrit.mastbuilder.MastBuilderTest.suite());
            suite.addTest(jmri.jmrit.powerpanel.PowerPanelTest.suite());
        }
        suite.addTest(jmri.jmrit.revhistory.FileHistoryTest.suite());
        suite.addTest(jmri.jmrit.roster.PackageTest.suite());
        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.jmrit.sendpacket.SendPacketTest.suite());
            suite.addTest(jmri.jmrit.sensorgroup.SensorGroupTest.suite());
        }
        suite.addTest(jmri.jmrit.simpleclock.SimpleClockTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.PackageTest.suite());
        suite.addTest(jmri.jmrit.tracker.TrackerTest.suite());
        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.jmrit.ussctc.UssCtcTest.suite());
        }
        suite.addTest(jmri.jmrit.consisttool.PackageTest.suite());
        suite.addTest(jmri.jmrit.withrottle.PackageTest.suite());
        suite.addTest(jmri.jmrit.ampmeter.PackageTest.suite());
        suite.addTest(jmri.jmrit.lcdclock.PackageTest.suite());
        suite.addTest(jmri.jmrit.throttle.PackageTest.suite());
        suite.addTest(jmri.jmrit.audio.PackageTest.suite());
        suite.addTest(jmri.jmrit.turnoutoperations.PackageTest.suite());
        suite.addTest(jmri.jmrit.dualdecoder.PackageTest.suite());
        suite.addTest(jmri.jmrit.nixieclock.PackageTest.suite());
        suite.addTest(jmri.jmrit.simpleprog.PackageTest.suite());
        suite.addTest(jmri.jmrit.signalling.PackageTest.suite());
        suite.addTest(jmri.jmrit.picker.PackageTest.suite());
        suite.addTest(jmri.jmrit.speedometer.PackageTest.suite());
        suite.addTest(jmri.jmrit.analogclock.PackageTest.suite());

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
