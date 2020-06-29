package jmri.jmrix.lenz;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

/**
 * XNetSystemConnectionMemoTest.java
 * <p>
 * Test for the jmri.jmrix.lenz.XNetSystemConnectionMemo class
 *
 * @author Paul Bender
 */
public class XNetSystemConnectionMemoTest extends SystemConnectionMemoTestBase<XNetSystemConnectionMemo> {

    private XNetTrafficController trafficController;
    private LenzCommandStation commandStation;

    @Test
    @Override
    public void testCtor() {
        Assert.assertNotNull(scm);
        Assert.assertNotNull(scm.getXNetTrafficController());
        Assert.assertEquals(trafficController,scm.getXNetTrafficController());
        // While we are constructing the memo, we should also set the
        // SystemMemo parameter in the traffic controller.
        Mockito.verify(trafficController).setSystemConnectionMemo(scm);
    }

    @Test
    public void testXNetTrafficControllerSetCtor() {
        XNetTrafficController tc2 = Mockito.mock(XNetTrafficController.class);
        LenzCommandStation cs = Mockito.mock(LenzCommandStation.class);
        Mockito.when(tc2.getCommandStation()).thenReturn(cs);
        Assert.assertNotNull(scm);
        // the default constructor does not set the traffic controller
        Assert.assertNotEquals(tc2, scm.getXNetTrafficController());
        // so we need to do this ourselves.
        scm.setXNetTrafficController(tc2);
        Assert.assertNotNull(scm.getXNetTrafficController());
        Assert.assertEquals(tc2,scm.getXNetTrafficController());
        // and while we're doing that, we should also set the SystemMemo
        // parameter in the traffic controller.
        Mockito.verify(tc2).setSystemConnectionMemo(scm);
    }

    @Test
    public void testProivdesConsistManagerMultiMaus() {
        Mockito.when(commandStation.getCommandStationType()).thenReturn(0x10); // MultiMaus
        scm.setCommandStation(trafficController.getCommandStation());
        Assert.assertFalse(scm.provides(jmri.ConsistManager.class));
    }

    @Test
    public void testProivdesCommandStaitonCompact() {
        Mockito.when(commandStation.getCommandStationType()).thenReturn(0x02); // Lenz Compact/Atlas Commander
        scm.setCommandStation(trafficController.getCommandStation());
        Assert.assertFalse(scm.provides(jmri.CommandStation.class));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        commandStation = Mockito.mock(LenzCommandStation.class);
        trafficController = Mockito.mock(XNetTrafficController.class);
        Mockito.when(trafficController.getCommandStation()).thenReturn(commandStation);
        Mockito.when(commandStation.getCommandStationType()).thenReturn(0x00); // LZV100

        scm = new XNetSystemConnectionMemo(trafficController);
        scm.setPowerManager(Mockito.mock(XNetPowerManager.class));
        scm.setThrottleManager(Mockito.mock(XNetThrottleManager.class));
        scm.setSensorManager(Mockito.mock(XNetSensorManager.class));
        scm.setLightManager(Mockito.mock(XNetLightManager.class));
        scm.setTurnoutManager(Mockito.mock(XNetTurnoutManager.class));
        scm.setProgrammerManager(Mockito.mock(XNetProgrammerManager.class));
        scm.setConsistManager(Mockito.mock(XNetConsistManager.class));
    }

    @AfterEach
    @Override
    public void tearDown() {
        scm.dispose();
        scm = null;
        JUnitUtil.tearDown();
    }

}
