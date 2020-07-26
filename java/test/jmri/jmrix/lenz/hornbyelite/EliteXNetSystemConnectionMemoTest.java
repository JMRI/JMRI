package jmri.jmrix.lenz.hornbyelite;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.jmrix.lenz.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

/**
 * EliteXNetSystemConnectionMemoTest.java
 * <p>
 * Test for the jmri.jmrix.lenz.EliteXNetSystemConnectionMemo class
 *
 * @author Paul Bender
 */
public class EliteXNetSystemConnectionMemoTest extends SystemConnectionMemoTestBase<EliteXNetSystemConnectionMemo> {

    private XNetTrafficController tc;

    @Test
    @Override
    public void testCtor() {
        Assert.assertNotNull(scm);
        Assert.assertNotNull(scm.getXNetTrafficController());
        Assert.assertEquals(tc,scm.getXNetTrafficController());
        // While we are constructing the memo, we should also set the 
        // SystemMemo parameter in the traffic controller.
        Mockito.verify(tc).setSystemConnectionMemo(scm);
    }

    @Test
    public void testXNetTrafficControllerSetCtor() {
        XNetTrafficController tc2 = Mockito.mock(XNetTrafficController.class);
        HornbyEliteCommandStation cs = Mockito.mock(HornbyEliteCommandStation.class);
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

    @Override
    @Test
    public void testProvidesConsistManager() {
        scm.setCommandStation(scm.getXNetTrafficController().getCommandStation());
        Assert.assertFalse(scm.provides(jmri.ConsistManager.class));
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        HornbyEliteCommandStation cs = Mockito.mock(HornbyEliteCommandStation.class);
        tc = Mockito.mock(XNetTrafficController.class);
        Mockito.when(tc.getCommandStation()).thenReturn(cs);

        scm = new EliteXNetSystemConnectionMemo(tc);
        scm.setPowerManager(Mockito.mock(XNetPowerManager.class));
        scm.setThrottleManager(Mockito.mock(EliteXNetThrottleManager.class));
        scm.setSensorManager(Mockito.mock(XNetSensorManager.class));
        scm.setLightManager(Mockito.mock(XNetLightManager.class));
        scm.setTurnoutManager(Mockito.mock(EliteXNetTurnoutManager.class));
        scm.setProgrammerManager(Mockito.mock(XNetProgrammerManager.class));
    }

    @AfterEach
    @Override
    public void tearDown() {
        scm.dispose();
        scm = null;
        JUnitUtil.tearDown();
    }

}
