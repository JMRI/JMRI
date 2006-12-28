// SoundLoaderTest.java

package jmri.jmrix.loconet.soundloader;

import junit.framework.*;

/**
 * Tests for the jmri.jmrix.loconet.soundloader package
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 * @version     $Revision: 1.3 $
 */
public class SoundLoaderTest extends TestCase {

    public void testCreate() {
        return;
    }

    public void testRead() throws java.io.IOException {
        return;
    }

    // from here down is testing infrastructure

    public SoundLoaderTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SoundLoaderTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SoundLoaderTest.class);
        suite.addTest(LoaderEngineTest.suite());
        suite.addTest(EditorPaneTest.suite());
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SoundLoaderTest.class.getName());

}
