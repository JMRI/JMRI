package jmri.jmrix.nce;

import jmri.GlobalProgrammerManager;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * JUnit tests for the NceProgrammerManager class
 *
 * @author Bob Jacobsen
 */
public class NceProgrammerManagerTest {

    NceSystemConnectionMemo memo;

    @Test
    public void testDefaultAccess() {
        // this is checking the "as default ctor built" options, which might not be valid
        Assert.assertTrue("provides global programmerManager", memo.provides(GlobalProgrammerManager.class));
        Assert.assertNotNull("global ProgrammerManager exists", memo.get(GlobalProgrammerManager.class));
        Assert.assertNotNull("global Programmer exists", memo.get(GlobalProgrammerManager.class).getGlobalProgrammer());
    }

    @Test
    public void test_USB_SYSTEM_POWERCAB_PROGTRACK() {
        memo.setNceUsbSystem(NceTrafficController.USB_SYSTEM_POWERCAB);
        memo.setNceCmdGroups(NceTrafficController.CMDS_PROGTRACK);

        Assert.assertTrue("provides global programmerManager", memo.provides(GlobalProgrammerManager.class));
        Assert.assertNotNull("global ProgrammerManager exists", memo.get(GlobalProgrammerManager.class));
        Assert.assertNotNull("global Programmer exists", memo.get(GlobalProgrammerManager.class).getGlobalProgrammer());
    }

    @Test
    public void test_USB_SYSTEM_SB3_NO_PROGTRACK() {
        memo.setNceUsbSystem(NceTrafficController.USB_SYSTEM_SB3);
        memo.setNceCmdGroups(0);

        Assert.assertTrue("provides global programmerManager", memo.provides(GlobalProgrammerManager.class));
        Assert.assertNotNull("global ProgrammerManager exists", memo.get(GlobalProgrammerManager.class));
        Assert.assertNull("no global Programmer exists", memo.get(GlobalProgrammerManager.class).getGlobalProgrammer());
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();

        memo = new NceSystemConnectionMemo();
        memo.setNceTrafficController(new NceTrafficController());
        
        NceCmdStationMemory t = new NceCmdStationMemory();
        Assert.assertNotNull("exists",t);
        memo.getNceTrafficController().csm = t;

        memo.configureManagers();
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        memo = null;
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        jmri.util.JUnitUtil.tearDown();
    }

    //private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NceProgrammerManagerTest.class);

}
