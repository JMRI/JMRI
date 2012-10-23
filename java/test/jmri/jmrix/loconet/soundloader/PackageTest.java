// PackageTest.java

package jmri.jmrix.loconet.soundloader;

import junit.framework.*;

/**
 * Tests for the jmri.jmrix.loconet.soundloader package
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 * @version     $Revision$
 */
public class PackageTest extends TestCase {

    public void testCreate() {
        return;
    }

    public void testRead(){
        return;
    }

    // from here down is testing infrastructure

    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class);
        suite.addTest(LoaderEngineTest.suite());
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PackageTest.class.getName());

}
