package jmri.jmrix.powerline;

//import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.powerline package.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

//    public void testDefinitions() {
//        Assert.assertEquals("Node definitions match", SerialSensorManager.SENSORSPERNODE,
//                                    SerialNode.MAXSENSORS+1);
//    }
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.powerline.SerialTest");
        suite.addTest(X10SequenceTest.suite());
        //suite.addTest(SerialTurnoutTest.suite());
        //suite.addTest(SerialSensorManagerTest.suite());
        suite.addTest(SerialNodeTest.suite());
        //suite.addTest(SerialAddressTest.suite());
        suite.addTest(jmri.jmrix.powerline.cm11.CM11Test.suite());
        suite.addTest(jmri.jmrix.powerline.insteon2412s.Insteon2412sTest.suite());
        suite.addTest(ActiveFlagTest.suite());
        return suite;
    }

}
