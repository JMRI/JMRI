package jmri.progdebugger;

import jmri.InstanceManager;
import jmri.Programmer;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the DebugProgrammerManager class.
 *
 * @author	Bob Jacobsen Copyright 2002
 */
public class DebugProgrammerManagerTest extends TestCase {

    /**
     * Service mode request returns a programmer
     */
    public void testServiceModeRequest() {
        InstanceManager.setProgrammerManager(
                new DebugProgrammerManager());
        Programmer p = InstanceManager.getDefault(jmri.ProgrammerManager.class)
                .getGlobalProgrammer();
        Assert.assertTrue("got service mode", p != null);
        Assert.assertTrue("correct type", (p instanceof ProgDebugger));
    }

    /**
     * Any service mode request gets the same object
     */
    public void testServiceModeUnique() {
        InstanceManager.setProgrammerManager(
                new DebugProgrammerManager());
        Programmer p = InstanceManager.getDefault(jmri.ProgrammerManager.class)
                .getGlobalProgrammer();
        Assert.assertTrue("same service mode programmer",
                InstanceManager.getDefault(jmri.ProgrammerManager.class)
                .getGlobalProgrammer() == p);
    }

    /**
     * ops mode request returns a programmer
     */
    public void testOpsModeRequest() {
        InstanceManager.setProgrammerManager(
                new DebugProgrammerManager());
        Programmer p = InstanceManager.getDefault(jmri.ProgrammerManager.class)
                .getAddressedProgrammer(true, 777);
        Assert.assertTrue("got ops mode", p != null);
        Assert.assertTrue("correct type", (p instanceof ProgDebugger));
    }

    /**
     * Any identical ops mode request gets the same object
     */
    public void testOpsModeUnique() {
        InstanceManager.setProgrammerManager(
                new DebugProgrammerManager());
        Programmer p = InstanceManager.getDefault(jmri.ProgrammerManager.class)
                .getAddressedProgrammer(true, 777);
        Assert.assertTrue("same ops mode programmer",
                InstanceManager.getDefault(jmri.ProgrammerManager.class)
                .getAddressedProgrammer(true, 777) == p);
    }

    /**
     * Any identical ops mode request gets the same object
     */
    public void testOpsModeDistinct() {
        InstanceManager.setProgrammerManager(
                new DebugProgrammerManager());
        Programmer p = InstanceManager.getDefault(jmri.ProgrammerManager.class)
                .getAddressedProgrammer(true, 777);
        Assert.assertTrue("different ops mode programmer",
                InstanceManager.getDefault(jmri.ProgrammerManager.class)
                .getAddressedProgrammer(true, 888) != p);
        Assert.assertTrue("same ops mode programmer",
                InstanceManager.getDefault(jmri.ProgrammerManager.class)
                .getAddressedProgrammer(true, 777) == p);
    }

    // from here down is testing infrastructure
    public DebugProgrammerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DebugProgrammerManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(DebugProgrammerManagerTest.class);
        return suite;
    }

}
