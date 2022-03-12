package jmri.jmrix.nce;

import jmri.GlobalProgrammerManager;
import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the NceSystemConnectionMemo class
 *
 * @author Bob Jacobsen
 */
public class NceSystemConnectionMemoTest extends SystemConnectionMemoTestBase<NceSystemConnectionMemo> {

    @Test
    public void testDefaultAccess() {
        // this is checking the "as default ctor built" options, which might not be valid
        Assert.assertTrue("provides global programmerManager", scm.provides(GlobalProgrammerManager.class));
        Assert.assertNotNull("global ProgrammerManager exists", scm.get(GlobalProgrammerManager.class));
        Assert.assertNotNull("global Programmer exists", ((GlobalProgrammerManager) scm.get(GlobalProgrammerManager.class)).getGlobalProgrammer());
    }

    @Test
    public void test_USB_SYSTEM_POWERCAB_PROGTRACK() {
        scm.setNceUsbSystem(NceTrafficController.USB_SYSTEM_POWERCAB);
        scm.setNceCmdGroups(NceTrafficController.CMDS_PROGTRACK);

        Assert.assertTrue("provides global programmerManager", scm.provides(GlobalProgrammerManager.class));
        Assert.assertNotNull("global ProgrammerManager exists", scm.get(GlobalProgrammerManager.class));
        Assert.assertNotNull("global Programmer exists", ((GlobalProgrammerManager) scm.get(GlobalProgrammerManager.class)).getGlobalProgrammer());
    }

    @Test
    public void test_USB_SYSTEM_SB3_NO_PROGTRACK() {
        scm.setNceUsbSystem(NceTrafficController.USB_SYSTEM_SB3);
        scm.setNceCmdGroups(0);

        Assert.assertTrue("provides global programmerManager", scm.provides(GlobalProgrammerManager.class));
        Assert.assertNotNull("global ProgrammerManager exists", scm.get(GlobalProgrammerManager.class));
        Assert.assertNull("no global Programmer exists", ((GlobalProgrammerManager) scm.get(GlobalProgrammerManager.class)).getGlobalProgrammer());
    }

    // The minimal setup is for log4J
    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        scm = new NceSystemConnectionMemo();
        scm.setNceTrafficController(new NceTrafficController() {
            @Override
            public void transmitLoop() {
            }

            @Override
            public void receiveLoop() {
            }
        });
        scm.configureManagers();
    }

    @AfterEach
    @Override
    public void tearDown() {
        scm.getNceTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();
    }

    //private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NceSystemConnectionMemoTest.class);
}
