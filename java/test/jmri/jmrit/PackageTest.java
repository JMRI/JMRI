package jmri.jmrit;

import jmri.util.JUnitUtil;
import junit.framework.JUnit4TestAdapter;
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

        suite.addTest(jmri.jmrit.AbstractIdentifyTest.suite());
        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        suite.addTest(DccLocoAddressSelectorTest.suite());
        suite.addTest(MemoryContentsTest.suite());
        suite.addTest(new JUnit4TestAdapter(SoundTest.class));
        suite.addTest(XmlFileTest.suite());

        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.automat.PackageTest.class));
        suite.addTest(jmri.jmrit.beantable.PackageTest.suite());
        suite.addTest(jmri.jmrit.blockboss.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.catalog.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.conditional.PackageTest.class));
        suite.addTest(jmri.jmrit.decoderdefn.PackageTest.suite());
        suite.addTest(jmri.jmrit.dispatcher.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.display.PackageTest.class));
        suite.addTest(jmri.jmrit.jython.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.log.PackageTest.class));
        suite.addTest(jmri.jmrit.logix.PackageTest.suite());
        suite.addTest(jmri.jmrit.operations.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.progsupport.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.mastbuilder.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.mailreport.PackageTest.class));
        suite.addTest(jmri.jmrit.powerpanel.PackageTest.suite());
        suite.addTest(jmri.jmrit.roster.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.sendpacket.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.sensorgroup.PackageTest.class));
        suite.addTest(jmri.jmrit.simpleclock.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.symbolicprog.PackageTest.class));
        suite.addTest(jmri.jmrit.tracker.PackageTest.suite());
        suite.addTest(jmri.jmrit.ussctc.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.consisttool.PackageTest.class));
        suite.addTest(jmri.jmrit.withrottle.PackageTest.suite());
        suite.addTest(jmri.jmrit.ampmeter.PackageTest.suite());
        suite.addTest(jmri.jmrit.lcdclock.PackageTest.suite());
        suite.addTest(jmri.jmrit.throttle.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.audio.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.turnoutoperations.PackageTest.class));
        suite.addTest(jmri.jmrit.dualdecoder.PackageTest.suite());
        suite.addTest(jmri.jmrit.nixieclock.PackageTest.suite());
        suite.addTest(jmri.jmrit.simpleprog.PackageTest.suite());
        suite.addTest(jmri.jmrit.signalling.PackageTest.suite());
        suite.addTest(jmri.jmrit.picker.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.speedometer.PackageTest.class));
        suite.addTest(jmri.jmrit.analogclock.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.revhistory.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.sound.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.vsdecoder.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.simplelightctrl.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.simpleturnoutctrl.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(MemoryFrameActionTest.class));
        suite.addTest(new JUnit4TestAdapter(ToolsMenuTest.class));
        suite.addTest(new JUnit4TestAdapter(XmlFileLocationActionTest.class));
        suite.addTest(new JUnit4TestAdapter(XmlFileValidateActionTest.class));
        suite.addTest(new JUnit4TestAdapter(XmlFileValidateStartupActionFactoryTest.class));
        suite.addTest(new JUnit4TestAdapter(DebugMenuTest.class));
        suite.addTest(new JUnit4TestAdapter(LogixLoadActionTest.class));
        suite.addTest(new JUnit4TestAdapter(XmlFileCheckActionTest.class));
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }
}

