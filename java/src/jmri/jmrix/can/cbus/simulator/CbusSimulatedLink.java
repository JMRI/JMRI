package jmri.jmrix.can.cbus.simulator;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;

import jmri.jmrit.simulation.link.Latency;
import jmri.jmrit.simulation.link.LinkPreset;
import jmri.jmrit.simulation.link.SimulatedLink;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.ThreadingUtil;

/**
 * A CAN Simulation connection's {@link SimulatedLink}: the time CBUS frames
 * take between JMRI and the simulated modules, through the chosen interface
 * and over the bus.
 * <ul>
 * <li>A frame JMRI sends crosses the link once, however many simulated
 * modules hear it; each module then handles it on the layout thread (Swing
 * thread) when it arrives.</li>
 * <li>A frame a simulated module sends crosses the link after the module's
 * own delay, and is received by JMRI, with that received time, when it
 * arrives.</li>
 * </ul>
 * Off until an interface other than None is chosen; then nothing changes.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public final class CbusSimulatedLink {

    private static final long MS = 1_000_000L;

    private final SimulatedLink link;
    @GuardedBy("this")
    private final Map<CanMessage, List<Runnable>> onTheirWay = new IdentityHashMap<>();

    CbusSimulatedLink(@Nonnull CanSystemConnectionMemo memo) {
        this(memo, jmri.jmrit.simulation.link.LinkClock::realTime);
    }

    /**
     * @param memo   the connection
     * @param clocks makes the clock the link runs on: for tests
     */
    CbusSimulatedLink(@Nonnull CanSystemConnectionMemo memo,
            @Nonnull java.util.function.Supplier<jmri.jmrit.simulation.link.LinkClock> clocks) {
        link = new SimulatedLink(new CbusWireTiming(), presets(), clocks);
        SimulatedLink.register(memo, link);
    }

    /**
     * The interfaces offered, besides None. The numbers are typical figures,
     * not measurements of any one unit; scripts can build stages with their
     * own.
     *
     * @return the presets
     */
    @Nonnull
    static List<LinkPreset> presets() {
        return List.of(
                new LinkPreset(Bundle.getMessage("LinkCanUsb4"),
                        () -> List.of(new Latency(MS, MS / 5, 1)),
                        () -> List.of(new Latency(MS, MS / 5, 2))),
                new LinkPreset(Bundle.getMessage("LinkNetwork"),
                        () -> List.of(new Latency(3 * MS, MS, 3)),
                        () -> List.of(new Latency(3 * MS, MS, 4))));
    }

    /**
     * @param memo a connection
     * @return its link, if its simulator has made one
     */
    @CheckForNull
    static CbusSimulatedLink of(@Nonnull CanSystemConnectionMemo memo) {
        CbusSimulator sim = memo.get(CbusSimulator.class);
        return sim == null ? null : sim.getLinkIfMade();
    }

    /**
     * @return the link, to choose an interface or read its stages
     */
    @Nonnull
    public SimulatedLink getLink() {
        return link;
    }

    /**
     * A simulated module has heard a frame JMRI sent: handle it now if the
     * link is off, or when the frame arrives. Every module that hears the
     * same frame in the same delivery shares its one crossing.
     *
     * @param m      the frame
     * @param handle what the module does with it
     */
    void towardsLayout(@Nonnull CanMessage m, @Nonnull Runnable handle) {
        if (!link.isOn()) {
            handle.run();
            return;
        }
        synchronized (this) {
            List<Runnable> waiting = onTheirWay.get(m);
            if (waiting != null) {
                waiting.add(handle);
                return;
            }
            waiting = new ArrayList<>();
            waiting.add(handle);
            onTheirWay.put(m, waiting);
        }
        link.send(SimulatedLink.Direction.TOWARDS_LAYOUT, m, System.nanoTime(), t -> {
            List<Runnable> arrived;
            synchronized (this) {
                arrived = onTheirWay.remove(m);
            }
            if (arrived != null) {
                ThreadingUtil.runOnLayoutEventually(() -> arrived.forEach(Runnable::run));
            }
        });
    }

    /**
     * A simulated module sends a frame, to be received by JMRI.
     *
     * @param r         the frame
     * @param sentNanos when it goes on the bus
     * @param received  told when JMRI receives it
     * @return false if the link is off, and the caller should deliver the
     *         frame itself as before
     */
    boolean towardsJmri(@Nonnull CanReply r, long sentNanos, @Nonnull java.util.function.LongConsumer received) {
        if (!link.isOn()) {
            return false;
        }
        link.send(SimulatedLink.Direction.TOWARDS_JMRI, r, sentNanos, received);
        return true;
    }

    void dispose() {
        link.dispose();
    }
}
