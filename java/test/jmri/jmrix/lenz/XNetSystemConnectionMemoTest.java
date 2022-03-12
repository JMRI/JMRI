package jmri.jmrix.lenz;

import jmri.CommandStation;
import jmri.ConsistManager;
import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(scm).isNotNull();
        assertThat(scm.getXNetTrafficController()).isNotNull().isEqualTo(trafficController);
        // While we are constructing the memo, we should also set the
        // SystemMemo parameter in the traffic controller.
        Mockito.verify(trafficController).setSystemConnectionMemo(scm);
    }

    @Test
    public void testXNetTrafficControllerSetCtor() {
        XNetTrafficController tc2 = Mockito.mock(XNetTrafficController.class);
        LenzCommandStation cs = Mockito.mock(LenzCommandStation.class);
        Mockito.when(tc2.getCommandStation()).thenReturn(cs);
        assertThat(scm).isNotNull();
        // the default constructor does not set the traffic controller
        assertThat(scm.getXNetTrafficController()).isNotEqualTo(tc2);
        // so we need to do this ourselves.
        scm.setXNetTrafficController(tc2);
        assertThat(scm.getXNetTrafficController()).isNotNull().isEqualTo(tc2);
        // and while we're doing that, we should also set the SystemMemo
        // parameter in the traffic controller.
        Mockito.verify(tc2).setSystemConnectionMemo(scm);
    }

    @Test
    public void testProivdesConsistManagerMultiMaus() {
        scm.deregister(scm.get(ConsistManager.class), ConsistManager.class);
        scm.deregister(scm.get(LenzCommandStation.class), LenzCommandStation.class);
        scm.deregister(scm.get(CommandStation.class), CommandStation.class);
        commandStation.setCommandStationType(0x10); // MultiMaus
        scm.setCommandStation(trafficController.getCommandStation());
        assertThat(scm.provides(ConsistManager.class)).isTrue();
        assertThat((ConsistManager)scm.get(ConsistManager.class)).isNotInstanceOf(XNetConsistManager.class);
    }

    @Test
    public void testProivdesCommandStaitonCompact() {
        scm.deregister(scm.get(LenzCommandStation.class), LenzCommandStation.class);
        scm.deregister(scm.get(CommandStation.class), CommandStation.class);
        commandStation.setCommandStationType(0x02); // Lenz Compact/Atlas Commander
        scm.setCommandStation(trafficController.getCommandStation());
        assertThat(scm.provides(LenzCommandStation.class)).isTrue();
        assertThat(scm.provides(CommandStation.class)).isFalse();
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        commandStation = new LenzCommandStation();
        trafficController = Mockito.mock(XNetTrafficController.class);
        Mockito.when(trafficController.getCommandStation()).thenReturn(commandStation);
        commandStation.setCommandStationType(0x00); // LZV100

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
