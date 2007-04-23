// SoundTest.java

package jmri.jmrit.sound;

import junit.framework.*;

/**
 * Invokes complete set of tests in the jmri.jmrit.sound tree
 *
 * @author	    Bob Jacobsen  Copyright 2001, 2003
 * @version         $Revision: 1.2 $
 */
public class SoundTest extends TestCase {

    // from here down is testing infrastructure
    public SoundTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SoundTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.sound.SoundTest");
        suite.addTest(jmri.jmrit.sound.WavBufferTest.suite());
        suite.addTest(jmri.jmrit.sound.SoundUtilTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
