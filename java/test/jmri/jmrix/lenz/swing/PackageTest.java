package jmri.jmrix.lenz.swing;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.swing package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.swing.SwingTest");  // no tests in this class itself
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.lenz.swing.liusb.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.lenz.swing.li101.PackageTest.class));
        suite.addTest(jmri.jmrix.lenz.swing.mon.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.lenz.swing.stackmon.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.lenz.swing.systeminfo.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.lenz.swing.packetgen.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.lenz.swing.lz100.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.lenz.swing.lzv100.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        return suite;
    }

}
