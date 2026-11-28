package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jmri.Block;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.loconet.LnSensorManager;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.hexfile.LnHexFilePort;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link SimulatorConnectionSensorOutput}, with a LocoNet Simulator
 * connection. The LocoNet frames are checked against the LocoNet Personal
 * Edition's sensor report (OPC_INPUT_REP, 0xB2; for sensor n, IN1 holds
 * (n - 1) / 2 and IN2 has bit 5 for (n - 1) odd, bit 6 always and bit 4 for
 * "high"), worked out here.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class SimulatorConnectionSensorOutputTest {

    private static final int OPC_INPUT_REP = 0xB2; // from the LocoNet specification, not JMRI's constants

    @Test
    public void testReportsSensorsOfASimulatorConnectionOnly() {
        SimulatorConnectionSensorOutput out = new SimulatorConnectionSensorOutput();
        Sensor s = sensors.provideSensor("LS1");
        assertTrue(ThreadingUtil.runOnGUIwithReturn(() -> out.reports(s)), "a LocoNet Simulator sensor");

        Sensor internal = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1");
        assertFalse(ThreadingUtil.runOnGUIwithReturn(() -> out.reports(internal)),
                "the Internal connection's sensors are the internal output's");

        InstanceManager.getDefault(ConnectionConfigManager.class).remove(config);
        assertFalse(ThreadingUtil.runOnGUIwithReturn(() -> out.reports(s)), "a connection no longer listed");

        jmri.jmrix.ConnectionConfig hardware = new jmri.jmrix.loconet.hexfile.ConnectionConfig(port) {
            @Override
            public boolean isSimulator() {
                return false;
            }
        };
        InstanceManager.getDefault(ConnectionConfigManager.class).add(hardware);
        assertFalse(ThreadingUtil.runOnGUIwithReturn(() -> out.reports(s)), "a connection with hardware behind it");
    }

    @Test
    public void testReportSetsTheSensorAsTheConnectionWould() {
        SimulatorConnectionSensorOutput out = new SimulatorConnectionSensorOutput();
        Sensor s = sensors.provideSensor("LS1");
        out.report(s, Sensor.ACTIVE, System.nanoTime());
        JUnitUtil.waitFor(() -> !lnis.outbound.isEmpty(), "a sensor report sent on LocoNet");
        assertEquals(1, lnis.outbound.size());
        assertEquals(List.of(OPC_INPUT_REP, 0x00, 0x50), elements(lnis.outbound.get(0)),
                "sensor 1 high: IN1 0, IN2 sensor and high bits");
    }

    @Test
    public void testSimulatedTrainSetsTheLayoutsOwnSensors() {
        Block[] b = SimulationTestUtil.line(500, "A", "B", "C");
        for (int i = 0; i < b.length; i++) {
            Sensor s = sensors.provideSensor("LS" + (i + 1));
            b[i].setSensor(s.getSystemName());
        }
        VirtualTrain t = SimulationTestUtil.train("T", 3, 100, 400);
        SimulationTestUtil.place(t, b[0], null, 150);
        assertTrue(SimulationTestUtil.layout().getSensorsNotReported().isEmpty(), "every Block's sensor is reported");
        DccThrottle th = SimulationTestUtil.throttle(3);
        ThreadingUtil.runOnLayout(() -> {
            th.setIsForward(true);
            th.setSpeedSetting(0.5f);
        });
        SimulationTestUtil.waitForFrontIn(t, b[2]);
        ThreadingUtil.runOnLayout(() -> th.setSpeedSetting(0f));
        JUnitUtil.waitFor(() -> sent(0x01, 0x50), "C's sensor, LS3, reported active on LocoNet");
        assertTrue(sent(0x00, 0x70), "B's sensor, LS2, reported active on LocoNet");
        ThreadingUtil.runOnLayout(() -> th.release(null));
    }

    private boolean sent(int in1, int in2) {
        for (LocoNetMessage m : new ArrayList<>(lnis.outbound)) {
            if (elements(m).equals(List.of(OPC_INPUT_REP, in1, in2))) {
                return true;
            }
        }
        return false;
    }

    private static List<Integer> elements(LocoNetMessage m) {
        return List.of(m.getElement(0), m.getElement(1), m.getElement(2));
    }

    private LocoNetInterfaceScaffold lnis;
    private LnSensorManager sensors;
    private LnHexFilePort port;
    private jmri.jmrix.ConnectionConfig config;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        SimulationTestUtil.setUpSimulatedProfile();
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        sensors = new LnSensorManager(memo, false);
        InstanceManager.setSensorManager(sensors);
        port = new LnHexFilePort(memo);
        config = new jmri.jmrix.loconet.hexfile.ConnectionConfig(port);
        InstanceManager.getDefault(ConnectionConfigManager.class).add(config);
    }

    @AfterEach
    public void tearDown() {
        SimulationTestUtil.removeTrains();
        sensors.dispose();
        lnis = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
