package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.InstanceManager;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link SimulationCheck}. These check JMRI's code only: no
 * simulated train is involved.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class SimulationCheckTest {

    /** Every simulator connection type JMRI has, by its settings class. */
    private static final String[] SIMULATORS = {
        "jmri.jmrix.bidib.simulator.ConnectionConfig",
        "jmri.jmrix.cmri.serial.sim.ConnectionConfig",
        "jmri.jmrix.dccpp.simulator.ConnectionConfig",
        "jmri.jmrix.direct.simulator.ConnectionConfig",
        "jmri.jmrix.easydcc.simulator.ConnectionConfig",
        "jmri.jmrix.grapevine.simulator.ConnectionConfig",
        "jmri.jmrix.internal.ConnectionConfig",
        "jmri.jmrix.lenz.xnetsimulator.ConnectionConfig",
        "jmri.jmrix.loconet.hexfile.ConnectionConfig",
        "jmri.jmrix.maple.simulator.ConnectionConfig",
        "jmri.jmrix.mrc.simulator.ConnectionConfig",
        "jmri.jmrix.nce.simulator.ConnectionConfig",
        "jmri.jmrix.oaktree.simulator.ConnectionConfig",
        "jmri.jmrix.powerline.simulator.ConnectionConfig",
        "jmri.jmrix.secsi.simulator.ConnectionConfig",
        "jmri.jmrix.sprog.simulator.ConnectionConfig",
        "jmri.jmrix.tams.simulator.ConnectionConfig",
        "jmri.jmrix.tmcc.simulator.ConnectionConfig",
        "jmri.jmrix.can.adapters.loopback.ConnectionConfig",
    };

    @Test
    public void testEverySimulatorSaysSo() throws ReflectiveOperationException {
        for (String name : SIMULATORS) {
            ConnectionConfig cc = (ConnectionConfig) Class.forName(name).getConstructor().newInstance();
            assertTrue(cc.isSimulator(), name + " is a simulator");
        }
    }

    @Test
    public void testRealConnectionIsNotSimulator() {
        assertFalse(new jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver.ConnectionConfig().isSimulator(),
                "CANUSB is real hardware");
        assertFalse(new jmri.jmrix.loconet.locobuffer.ConnectionConfig().isSimulator(),
                "LocoBuffer is real hardware");
    }

    @Test
    public void testNoConnectionManager() {
        InstanceManager.reset(ConnectionConfigManager.class);
        assertFalse(SimulationCheck.isFullySimulated(), "connections not known");
    }

    @Test
    public void testProfileWithOnlySimulators() {
        ConnectionConfigManager manager = InstanceManager.getDefault(ConnectionConfigManager.class);
        assertTrue(SimulationCheck.isFullySimulated(), "no connections is simulated");
        manager.add(new jmri.jmrix.internal.ConnectionConfig());
        manager.add(new jmri.jmrix.can.adapters.loopback.ConnectionConfig());
        assertTrue(SimulationCheck.isFullySimulated(), "Internal and CAN Simulation");
    }

    @Test
    public void testProfileWithRealConnection() {
        ConnectionConfigManager manager = InstanceManager.getDefault(ConnectionConfigManager.class);
        manager.add(new jmri.jmrix.can.adapters.loopback.ConnectionConfig());
        ConnectionConfig real = new jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver.ConnectionConfig();
        manager.add(real);
        assertFalse(SimulationCheck.isFullySimulated(), "a real connection is present");
        manager.remove(real);
        assertTrue(SimulationCheck.isFullySimulated(), "real connection removed");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initConnectionConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
