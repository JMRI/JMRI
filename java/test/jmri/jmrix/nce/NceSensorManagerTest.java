package jmri.jmrix.nce;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the NceAIU class.
 *
 * @author	Bob Jacobsen Copyright 2002
 * @version	$Revision$
 */
public class NceSensorManagerTest extends TestCase {

    public void testNceSensorCreate() {
        // prepare an interface
        NceInterfaceScaffold lnis = new NceInterfaceScaffold();
        Assert.assertNotNull("exists", lnis);

        // create and register the manager object
        NceSensorManager n = new NceSensorManager(lnis, "N");
        jmri.InstanceManager.setSensorManager(n);
    }

    // from here down is testing infrastructure
    public NceSensorManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NceSensorManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NceSensorManagerTest.class);
        return suite;
    }

}
