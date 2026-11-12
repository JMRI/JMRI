package jmri.jmrix.can.cbus.simulator;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.ConfigurationManager;
import jmri.jmrix.can.adapters.loopback.ConnectionConfig;
import jmri.jmrix.can.adapters.loopback.Port;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

/**
 * Helps tests use simulated trains on CAN Simulation connections, made as the
 * connection preferences make them.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public final class CbusSimulationTestUtil {

    private CbusSimulationTestUtil() {
        // static methods only
    }

    /**
     * Add a CAN Simulation connection with MERG CBUS, configured as at start
     * up, and list it with the connection configuration manager, which must
     * already exist (as after {@code SimulationTestUtil.setUpSimulatedProfile()}).
     *
     * @param prefix the connection's system prefix, such as "M" or "M2"
     * @return the connection
     */
    @Nonnull
    public static CanSystemConnectionMemo addCanSimulation(@Nonnull String prefix) {
        Port port = new Port();
        port.getSystemConnectionMemo().setSystemPrefix(prefix);
        port.setOptionState("Protocol", ConfigurationManager.MERGCBUS);
        ThreadingUtil.runOnLayout(port::configure);
        InstanceManager.getDefault(ConnectionConfigManager.class).add(new ConnectionConfig(port));
        return port.getSystemConnectionMemo();
    }

    /**
     * Dispose of a connection made by {@link #addCanSimulation}.
     *
     * @param memo the connection
     */
    public static void dispose(@Nonnull CanSystemConnectionMemo memo) {
        CbusEventTableDataModel events = memo.get(CbusEventTableDataModel.class);
        if (events != null) {
            events.skipSaveOnDispose();
        }
        jmri.jmrix.can.TrafficController tc = memo.getTrafficController();
        ThreadingUtil.runOnLayout(memo::dispose);
        tc.terminateThreads();
    }

    /**
     * A CBUS sensor on a connection.
     *
     * @param memo    the connection
     * @param address its address, such as "+N1E2"
     * @return the sensor
     */
    @Nonnull
    public static Sensor sensor(@Nonnull CanSystemConnectionMemo memo, @Nonnull String address) {
        SensorManager sm = memo.get(SensorManager.class);
        AtomicReference<Sensor> s = new AtomicReference<>();
        ThreadingUtil.runOnLayout(() -> s.set(sm.provideSensor(memo.getSystemPrefix() + "S" + address)));
        return s.get();
    }

    /**
     * A throttle from a connection's own throttle manager, waiting for it.
     *
     * @param memo    the connection
     * @param address the loco address
     * @return the throttle
     */
    @Nonnull
    public static DccThrottle throttle(@Nonnull CanSystemConnectionMemo memo, @Nonnull DccLocoAddress address) {
        ThrottleManager tm = memo.get(ThrottleManager.class);
        AtomicReference<DccThrottle> found = new AtomicReference<>();
        ThrottleListener l = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                found.set(t);
            }

            @Override
            public void notifyFailedThrottleRequest(LocoAddress a, String reason) {
                throw new IllegalStateException("throttle for " + a + " refused: " + reason);
            }

            @Override
            public void notifyDecisionRequired(LocoAddress a, DecisionType question) {
                throw new IllegalStateException("throttle for " + a + " needs a decision: " + question);
            }
        };
        ThreadingUtil.runOnLayout(() -> tm.requestThrottle(address, l, false));
        JUnitUtil.waitFor(() -> found.get() != null, "throttle for " + address);
        return found.get();
    }
}
