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
        suite.addTest(new junit.framework.JUnit4TestAdapter(AcelaLightManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AcelaLightTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AcelaTurnoutManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AcelaTurnoutTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.acela.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.acela.serialdriver.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.acela.nodeconfig.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.acela.acelamon.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.acela.packetgen.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AcelaSystemConnectionMemoTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AcelaPortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AcelaTrafficControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AcelaAddressTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AcelaConnectionTypeListTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AcelaMessageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AcelaReplyTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.acela.swing.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AcelaMenuTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AcelaSensorManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AcelaSensorTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AcelaSignalHeadTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        return suite;
    }
}
