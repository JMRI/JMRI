package jmri.jmrit.sound;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.jmrit.sound tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003
 */
public class SoundTest extends TestCase {

    // from here down is testing infrastructure
    public SoundTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SoundTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.sound.SoundTest");
        suite.addTest(jmri.jmrit.sound.WavBufferTest.suite());
        suite.addTest(jmri.jmrit.sound.SoundUtilTest.suite());
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
