package jmri.jmrix.roco.z21;

import jmri.InstanceManager;
import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.jmrix.lenz.XNetProgrammerManager;
import jmri.jmrix.lenz.XNetStreamPortController;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.streamport.LnStreamPortController;
import jmri.util.JUnitUtil;

import org.mockito.Mockito;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.roco.z21.z21SystemConnectionMemo class
 *
 * @author Paul Bender
 */
public class Z21SystemConnectionMemoTest extends SystemConnectionMemoTestBase<Z21SystemConnectionMemo> {

    @Test
    public void testProvidesReporterManager() {
        Assert.assertTrue(scm.provides(jmri.ReporterManager.class));
    }

    @Test
    public void testProvidesAddressedProgrammerManager() {
        // There is an addressed program manager, but it is provided by delegation to the XPressNet tunnel.
        Assert.assertFalse("Provides Addressed programmer", scm.provides(jmri.AddressedProgrammerManager.class));
    }

    @Test
    public void testProvidesGlobalProgrammerManager() {
        // There is a global program manager, but it is provided by delegation to the XPressNet tunnel.
        Assert.assertFalse("provides golbal programmer", scm.provides(jmri.GlobalProgrammerManager.class));
    }

    @Override
    @Test
    public void testProvidesConsistManager() {
        // there is a consist manager provided by delegation to the XPressNet tunnel.
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Test
    public void testGetMeters() {
        Assert.assertNotNull("Get current meter",
                InstanceManager.getDefault(jmri.MeterManager.class)
                        .getBySystemName("ZVCommandStationCurrent"));
        Assert.assertNotNull("Get voltage meter",
                InstanceManager.getDefault(jmri.MeterManager.class)
                        .getBySystemName("ZVCommandStationVoltage"));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new Z21SystemConnectionMemo();
        scm.setTrafficController(new Z21InterfaceScaffold());
        scm.setRocoZ21CommandStation(new RocoZ21CommandStation());

        Z21LocoNetTunnel locoNetTunnel = Mockito.mock(Z21LocoNetTunnel.class);
        LnStreamPortController loconetStreamPortController = Mockito.mock(LnStreamPortController.class);
        Mockito.when(locoNetTunnel.getStreamPortController()).thenReturn(loconetStreamPortController);
        LocoNetSystemConnectionMemo locoNetSystemConnectionMemo = Mockito.mock(LocoNetSystemConnectionMemo.class);
        Mockito.when(loconetStreamPortController.getSystemConnectionMemo()).thenReturn(locoNetSystemConnectionMemo);
        scm.store(locoNetTunnel,Z21LocoNetTunnel.class);

        Z21XPressNetTunnel xNetTunnel = Mockito.mock(Z21XPressNetTunnel.class);
        XNetStreamPortController xNetPortController = Mockito.mock(XNetStreamPortController.class);
        Mockito.when(xNetTunnel.getStreamPortController()).thenReturn(xNetPortController);
        XNetSystemConnectionMemo xNetSystemConnectionMemo = Mockito.mock(XNetSystemConnectionMemo.class);
        Mockito.when(xNetPortController.getSystemConnectionMemo()).thenReturn(xNetSystemConnectionMemo);
        XNetProgrammerManager xNetProgrammerManager = Mockito.mock(XNetProgrammerManager.class);
        Mockito.when(xNetSystemConnectionMemo.getProgrammerManager()).thenReturn(xNetProgrammerManager);
        scm.store(xNetTunnel,Z21XPressNetTunnel.class);

        scm.configureManagers();
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        scm = null;
        JUnitUtil.tearDown();
    }

}
