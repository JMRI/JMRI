/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.acela;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.acela package
 *
 * @author	Bob Coleman
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
        TestSuite suite = new TestSuite("jmri.jmrix.acela.AcelaTest");  // no tests in this class itself
        suite.addTest(new TestSuite(AcelaNodeTest.class));
//        suite.addTest(new TestSuite(AcelaLightManagerTest.class));
//        suite.addTest(new TestSuite(AcelaLightTest.class));
        suite.addTest(new TestSuite(AcelaTurnoutManagerTest.class));
        suite.addTest(new TestSuite(AcelaTurnoutTest.class));
        suite.addTest(ActiveFlagTest.suite());
        return suite;
    }
}
