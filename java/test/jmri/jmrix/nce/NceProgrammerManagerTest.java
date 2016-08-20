package jmri.jmrix.nce;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.GlobalProgrammerManager;

/**
 * JUnit tests for the NceProgrammerManager class
 *
 * @author	Bob Jacobsen
 */
public class NceProgrammerManagerTest extends TestCase {

    NceSystemConnectionMemo memo;

    public void testDefaultAccess() {
        // this is checking the "as default ctor built" options, which might not be valid
        Assert.assertTrue("provides global programmerManager", memo.provides(GlobalProgrammerManager.class));
        Assert.assertNotNull("global ProgrammerManager exists", memo.get(GlobalProgrammerManager.class));
        Assert.assertNotNull("global Programmer exists", ((GlobalProgrammerManager)memo.get(GlobalProgrammerManager.class)).getGlobalProgrammer());
    }

    public void test_USB_SYSTEM_POWERCAB_PROGTRACK() {
        memo.setNceUsbSystem(NceTrafficController.USB_SYSTEM_POWERCAB);
        memo.setNceCmdGroups(NceTrafficController.CMDS_PROGTRACK);
        
        Assert.assertTrue("provides global programmerManager", memo.provides(GlobalProgrammerManager.class));
        Assert.assertNotNull("global ProgrammerManager exists", memo.get(GlobalProgrammerManager.class));
        Assert.assertNotNull("global Programmer exists", ((GlobalProgrammerManager)memo.get(GlobalProgrammerManager.class)).getGlobalProgrammer());
    }

    public void test_USB_SYSTEM_SB3_NO_PROGTRACK() {
        memo.setNceUsbSystem(NceTrafficController.USB_SYSTEM_SB3);
        memo.setNceCmdGroups(0);
        
        Assert.assertTrue("provides global programmerManager", memo.provides(GlobalProgrammerManager.class));
        Assert.assertNotNull("global ProgrammerManager exists", memo.get(GlobalProgrammerManager.class));
        Assert.assertNull("no global Programmer exists", ((GlobalProgrammerManager)memo.get(GlobalProgrammerManager.class)).getGlobalProgrammer());
    }


    // from here down is testing infrastructure
    public NceProgrammerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NceProgrammerManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NceProgrammerManagerTest.class);
        return suite;
    }

    // The minimal setup is for log4J
    public void setUp() {
        apps.tests.Log4JFixture.setUp(); 
        jmri.util.JUnitUtil.resetInstanceManager();
        
        memo = new NceSystemConnectionMemo();
        memo.setNceTrafficController(new NceTrafficController());
    }

    public void tearDown() {        
        apps.tests.Log4JFixture.tearDown();
    }

    //private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NceProgrammerManagerTest.class.getName());

}
