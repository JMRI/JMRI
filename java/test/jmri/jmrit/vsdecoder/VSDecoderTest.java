package jmri.jmrit.vsdecoder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrix.vsdecoder package & jmrix.vsdecoder.VSDecoder class.
 *
 * @author	Mark Underwood Copyright (C) 2011
 * @version $Revision: 17977 $
 */
public class VSDecoderTest extends TestCase {

    // Tests for the VSDecoder class...
    public void testCreate() {
        // do something
    }

    // from here down is testing infrastructure
    public VSDecoderTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", VSDecoderTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(VSDecoderTest.class);
        suite.addTest(jmri.jmrit.vsdecoder.TriggerTest.suite());
        suite.addTest(jmri.jmrit.vsdecoder.BoolTriggerTest.suite());
        suite.addTest(jmri.jmrit.vsdecoder.ButtonTriggerTest.suite());
        suite.addTest(jmri.jmrit.vsdecoder.FloatTriggerTest.suite());
        suite.addTest(jmri.jmrit.vsdecoder.IntTriggerTest.suite());
        suite.addTest(jmri.jmrit.vsdecoder.NotchTriggerTest.suite());
        suite.addTest(jmri.jmrit.vsdecoder.VSDSoundTest.suite());
        suite.addTest(jmri.jmrit.vsdecoder.SoundBiteTest.suite());
        suite.addTest(BundleTest.suite());
        suite.addTest(jmri.jmrit.vsdecoder.swing.PackageTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
	    // Put swing tests here (?)
        }

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        //super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
