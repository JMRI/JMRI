package jmri.jmrit.simulation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;

import jmri.Block;
import jmri.DccLocoAddress;
import jmri.Disposable;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.InvokeOnAnyThread;
import jmri.Sensor;
import jmri.jmrix.ConnectionConfigManager;
import jmri.util.ThreadingUtil;

/**
 * Simulated trains on the layout: moves them, reports their detection, and
 * keeps the truth of where they are.
 * <p>
 * Nothing runs until a train is placed. Placing the first train starts the
 * mover's own thread; removing the last stops it. A train can only be placed
 * in a fully simulated profile ({@link SimulationCheck}); if a real connection
 * is added while trains are placed, they are all removed with a warning.
 * <p>
 * Every {@value #STEP_MS} ms each moving train is advanced from its locos'
 * commanded speeds, with their decoders' momentum. When a detected stretch of a
 * train crosses a Block boundary within a step, the exact time is worked out,
 * and the Block's sensor is reported with that time. A Block's sensor goes
 * active when the first detected stretch of any train enters it and inactive
 * when the last of every train has left. Changes are reported in the order
 * they happened, across all trains. Two trains that touch both stop, and the
 * collision is kept in the ground truth.
 * <p>
 * Speed commands come from {@link SimulatedSpeedSource}s; detection goes out
 * through {@link SimulatedDetectorOutput}s, one chosen for each sensor by the
 * connection it belongs to. Internal sensors are reported by an
 * {@link InternalSensorOutput}; a sensor no output reports is left alone and
 * listed by {@link #getSensorsNotReported()}. The default layout, and any made
 * with the public constructor, also takes commands from the Internal
 * connection's throttles ({@link InternalThrottleSpeedSource}), so in a
 * profile with an Internal connection placed trains obey Dispatcher, Warrants,
 * throttle windows and scripts with no further set-up.
 * <h2>Threads</h2>
 * The mover's thread never reads layout objects. The layout's configuration
 * (the Layout Editor drawing, Blocks with their paths, lengths and sensors,
 * OBlocks and Portals, and which output reports each sensor) is copied on the
 * layout thread (Swing thread) when a train is placed and when detector
 * outputs change; turnout and other bean states reach the mover through
 * listeners. A change to the drawing or to Block paths while trains are
 * placed is used from the next placement, as Layout Editor gives no notice
 * of such a change.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
@javax.annotation.concurrent.ThreadSafe
public class SimulatedLayout implements InstanceManagerAutoDefault, Disposable {

    /** Step length, ms. */
    public static final int STEP_MS = 50;
    static final long STEP_NANOS = STEP_MS * 1_000_000L;
    static final double STEP_SECONDS = STEP_MS / 1000.0;
    /** Name of the mover's thread. */
    public static final String THREAD_NAME = "Simulated trains";

    private final boolean manual;
    @GuardedBy("this")
    private long manualClock;

    private final TrackModels models = new TrackModels();
    @GuardedBy("this")
    private final Map<VirtualTrain, TrainState> trains = new LinkedHashMap<>();
    @GuardedBy("this")
    private final List<SimulationTruth.Collision> collisions = new ArrayList<>();
    @GuardedBy("this")
    private final Map<Block, Integer> occupancy = new HashMap<>();
    private final SimulatedFaults faults = new SimulatedFaults(this);
    /** The Blocks whose sensors are shown occupied: occupancy as the detectors see it, faults included. */
    @GuardedBy("this")
    private final Set<Block> shown = new HashSet<>();
    /** For each Block, the trains whose front is in a drop-out stretch of it, and in how many. */
    @GuardedBy("this")
    private final Map<Block, Map<TrainState, Integer>> dropping = new HashMap<>();
    @GuardedBy("this")
    private final List<SimulationTruth.DropOut> dropOutRecords = new ArrayList<>();
    /** Random drop-out stretches decided for a train's way through a Block: {from, to} from its start, or none. */
    @GuardedBy("this")
    private final Map<List<Object>, double[]> randomStretches = new HashMap<>();
    private final List<SimulatedSpeedSource> sources = new CopyOnWriteArrayList<>();
    /**
     * Held while telling the speed sources which addresses to watch, so they
     * are told one at a time and in order; never taken with this layout's lock
     * held.
     */
    private final Object watching = new Object();
    private final List<SimulatedDetectorOutput> outputs = new CopyOnWriteArrayList<>();
    /** Sensors already warned about as having no output. */
    @GuardedBy("this")
    private final Set<Sensor> warnedNotReported = new HashSet<>();
    /**
     * Sensor reports, queued in order under this layout's lock and delivered
     * after it is released, so an output is never called with the lock held.
     */
    private final Queue<Runnable> reports = new ConcurrentLinkedQueue<>();
    /** Held while delivering reports, so they are delivered in the order queued. */
    private final Object delivering = new Object();
    @GuardedBy("this")
    private long lastTick;
    @GuardedBy("this")
    @CheckForNull
    private Thread thread;
    @GuardedBy("this")
    @CheckForNull
    private ConnectionConfigManager watchedConnections;

    private final SimulatedSpeedSource.CommandListener commandListener = address -> wake();
    private final PropertyChangeListener connectionListener = this::connectionsChanged;

    /**
     * A simulated layout with its own mover thread, taking speed commands from
     * the Internal connection's throttles.
     */
    @InvokeOnAnyThread
    public SimulatedLayout() {
        this(false);
        InternalThrottleSpeedSource internal = new InternalThrottleSpeedSource();
        sources.add(internal);
        internal.addCommandListener(commandListener);
    }

    /**
     * @param manual true for a layout whose time only moves when
     *               {@link #runFor(double)} is called, with no thread, so
     *               tests can run it step by step
     */
    SimulatedLayout(boolean manual) {
        this.manual = manual;
        this.manualClock = System.nanoTime();
        outputs.add(new InternalSensorOutput());
    }

    /**
     * @return the default simulated layout
     */
    @InvokeOnAnyThread
    public static SimulatedLayout getDefault() {
        return InstanceManager.getDefault(SimulatedLayout.class);
    }

    /**
     * @return the faults that can be set on this layout; none is set unless
     *         asked for
     */
    @Nonnull
    @InvokeOnAnyThread
    public SimulatedFaults getFaults() {
        return faults;
    }

    TrackModels models() {
        return models;
    }

    @GuardedBy("this")
    void checkNoTrains() {
        if (!trains.isEmpty()) {
            throw new IllegalStateException(Bundle.getMessage("FaultBeforePlacing"));
        }
    }

    /**
     * @return the current time on the {@link System#nanoTime()} scale, or the
     *         simulated clock of a manual layout
     */
    synchronized long now() {
        return manual ? manualClock : System.nanoTime();
    }

    // ---------------------------------------------------------------------
    // Sources and outputs

    /**
     * @param source where to get speed commands from
     */
    @InvokeOnAnyThread
    public void addSpeedSource(@Nonnull SimulatedSpeedSource source) {
        synchronized (watching) {
            sources.add(source);
            source.addCommandListener(commandListener);
            source.watch(respondingAddresses());
        }
    }

    /**
     * @param source a speed source to stop using
     */
    @InvokeOnAnyThread
    public void removeSpeedSource(@Nonnull SimulatedSpeedSource source) {
        synchronized (watching) {
            source.removeCommandListener(commandListener);
            if (sources.remove(source)) {
                source.watch(Set.of());
            }
        }
    }

    /**
     * @return the addresses the placed trains' locos obey
     */
    private synchronized Set<DccLocoAddress> respondingAddresses() {
        Set<DccLocoAddress> set = new HashSet<>();
        for (VirtualTrain t : trains.keySet()) {
            for (SimulatedLoco l : t.getLocos()) {
                set.add(l.getRespondingAddress());
            }
        }
        return set;
    }

    /**
     * Tell the speed sources which addresses the placed trains obey, after a
     * placement or removal. Called without this layout's lock.
     */
    private void watchAddresses() {
        synchronized (watching) {
            Set<DccLocoAddress> addresses = respondingAddresses();
            for (SimulatedSpeedSource s : sources) {
                s.watch(addresses);
            }
        }
    }

    /**
     * @param output a detector output for the sensors it reports
     */
    @InvokeOnAnyThread
    public void addDetectorOutput(@Nonnull SimulatedDetectorOutput output) {
        outputs.add(0, output);
        refreshSnapshot();
    }

    /**
     * @param output a detector output to stop using
     */
    @InvokeOnAnyThread
    public void removeDetectorOutput(@Nonnull SimulatedDetectorOutput output) {
        outputs.remove(output);
        refreshSnapshot();
    }

    /**
     * Copy the layout's configuration again, while trains are placed, so the
     * sensors' outputs are chosen again. Copied on the layout thread (Swing
     * thread), without holding this layout's lock.
     */
    private void refreshSnapshot() {
        synchronized (this) {
            if (trains.isEmpty()) {
                return;
            }
        }
        TrackSnapshot fresh = TrackSnapshot.take(new ArrayList<>(outputs));
        TrackSnapshot old;
        synchronized (this) {
            if (trains.isEmpty()) {
                old = fresh;
            } else {
                old = models.use(fresh);
            }
        }
        old.dispose();
    }

    /**
     * Use a new copy of the layout's configuration for a placement. It is
     * copied on the layout thread (Swing thread), before taking this layout's
     * lock, so a placement from any thread can't deadlock with the layout
     * thread.
     *
     * @param placing the placement, run under this layout's lock
     */
    private void withFreshSnapshot(Runnable placing) {
        TrackSnapshot fresh = TrackSnapshot.take(new ArrayList<>(outputs));
        TrackSnapshot old;
        synchronized (this) {
            old = models.use(fresh);
            try {
                placing.run();
            } finally {
                if (trains.isEmpty()) {
                    // nothing placed: keep no listeners
                    old.dispose();
                    old = models.use(TrackSnapshot.empty());
                }
            }
        }
        old.dispose();
    }

    /**
     * The command for an address: the one received last, from whichever
     * source, as a decoder obeys the last packet it is sent.
     *
     * @param address the address
     * @return the command, or null
     */
    @CheckForNull
    SimulatedSpeedSource.Command commandFor(@Nonnull DccLocoAddress address) {
        SimulatedSpeedSource.Command latest = null;
        for (SimulatedSpeedSource s : sources) {
            SimulatedSpeedSource.Command c = s.getCommand(address);
            if (c != null && (latest == null || c.getTimeNanos() - latest.getTimeNanos() > 0)) {
                latest = c;
            }
        }
        return latest;
    }

    /**
     * @return the sensors of Blocks that no detector output reports, so are
     *         left alone, as known when a train was last placed; empty until
     *         a train is placed
     */
    @Nonnull
    @InvokeOnAnyThread
    public Set<Sensor> getSensorsNotReported() {
        return new HashSet<>(models.snapshot().unreported().keySet());
    }

    // ---------------------------------------------------------------------
    // Placing and removing

    /**
     * Place a train with its front in a Block.
     *
     * @param train   the train
     * @param block   the Block its front is in
     * @param from    the neighbouring Block the train came from (the way its
     *                rear is), or null if it came from the end of the track
     * @param frontMm how far its front is into the Block, from where it came
     *                in
     * @throws IllegalStateException    if the profile is not fully simulated,
     *                                  or the train is already placed
     * @throws IllegalArgumentException if the train can't be placed there,
     *                                  with the reason
     */
    @InvokeOnAnyThread
    public void place(@Nonnull VirtualTrain train, @Nonnull Block block, @CheckForNull Block from,
            double frontMm) {
        checkCanPlace(train);
        withFreshSnapshot(() -> {
            checkCanPlace(train);
            TrackEntry entry = models.entryInto(block, from);
            placeAt(train, entry, frontMm);
        });
        deliver();
        watchAddresses();
    }

    /**
     * Place a train behind one already placed, running the same way, with its
     * front just touching the other's rear.
     *
     * @param train the train
     * @param ahead the train already there
     * @throws IllegalStateException    if the profile is not fully simulated,
     *                                  or the train is already placed or the
     *                                  other is not
     * @throws IllegalArgumentException if the train can't be placed there
     */
    @InvokeOnAnyThread
    public void placeBehind(@Nonnull VirtualTrain train, @Nonnull VirtualTrain ahead) {
        checkCanPlace(train);
        withFreshSnapshot(() -> {
            checkCanPlace(train);
            TrainState other = trains.get(ahead);
            if (other == null) {
                throw new IllegalStateException(Bundle.getMessage("TrainNotPlaced", ahead.getName()));
            }
            double rear = other.rearPosition();
            TrainState.Span span = other.spans.get(0);
            for (TrainState.Span s : other.spans) {
                if (rear >= s.start && rear < s.end) {
                    span = s;
                    break;
                }
            }
            placeAt(train, span.passage.entry, rear - span.start);
        });
        deliver();
        watchAddresses();
    }

    private synchronized void checkCanPlace(VirtualTrain train) {
        if (!SimulationCheck.isFullySimulated()) {
            throw new IllegalStateException(Bundle.getMessage("PlacementNotSimulated"));
        }
        if (trains.containsKey(train)) {
            throw new IllegalStateException(Bundle.getMessage("TrainAlreadyPlaced", train.getName()));
        }
    }

    private void placeAt(VirtualTrain train, TrackEntry entry, double frontMm) {
        Passage p = entry.follow();
        if (p.stop == Passage.Stop.BLOCKED) {
            Message reason = p.reason;
            throw new IllegalArgumentException(reason == null ? null : reason.text());
        }
        if (frontMm < 0 || frontMm > p.length) {
            throw new IllegalArgumentException(Bundle.getMessage("FrontOutsideBlock", models.name(entry.getBlock()),
                    p.length));
        }
        long now = now();
        TrainState t = new TrainState(train, now);
        t.spans.add(new TrainState.Span(p, 0.0, p.length));
        t.position = frontMm;
        while (t.rear().start > t.rearPosition()) {
            if (!t.extendRear(models)) {
                Message reason = t.rearReason;
                throw new IllegalArgumentException(Bundle.getMessage("TrainDoesNotFit", train.getName(),
                        reason == null ? "" : reason.text()));
            }
        }
        for (TrainState other : trains.values()) {
            Block where = Collisions.overlapAt(t, t.position, other, other.position);
            if (where != null) {
                throw new IllegalArgumentException(Bundle.getMessage("PlacedOnTrain", train.getName(),
                        other.train.getName(), models.name(where)));
            }
        }
        watchConnections();
        trains.put(train, t);
        // the detected stretches already in Blocks
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < t.markers.size(); i++) {
            double[] m = t.markers.get(i);
            for (TrainState.Span s : t.spans) {
                if (memberAt(t.position, m, s)) {
                    events.add(new Event(0.0, t, new TrainState.Membership(i, s), true));
                }
            }
        }
        apply(events, now);
        log.debug("Placed {} in {}", train.getName(), entry.describe());
        for (Map.Entry<Sensor, String> e : models.snapshot().unreported().entrySet()) {
            if (warnedNotReported.add(e.getKey())) {
                log.warn("Sensor \"{}\" of Block \"{}\" is not on a connection simulated trains can report on, so it is left alone.",
                        e.getKey().getSystemName(), e.getValue());
            }
        }
        startMover();
        wake();
    }

    /**
     * Remove a train. Its Blocks' sensors go inactive where no other train is
     * detected in them.
     *
     * @param train the train
     */
    @InvokeOnAnyThread
    public void remove(@Nonnull VirtualTrain train) {
        synchronized (this) {
            removeLocked(train);
        }
        deliver();
        watchAddresses();
    }

    private synchronized void removeLocked(VirtualTrain train) {
        TrainState t = trains.remove(train);
        if (t == null) {
            return;
        }
        List<Event> events = new ArrayList<>();
        for (TrainState.Membership m : new ArrayList<>(t.in)) {
            events.add(new Event(0.0, t, m, false));
        }
        long when = now();
        apply(events, when);
        endDropOuts(t, when);
        randomStretches.keySet().removeIf(k -> k.get(0) == t);
        if (trains.isEmpty()) {
            for (Block b : new ArrayList<>(faults.phantoms)) {
                faults.phantoms.remove(b);
                detectionChanged(b, when);
            }
            collisions.clear();
            dropOutRecords.clear();
            shown.clear();
            unwatchConnections();
            models.use(TrackSnapshot.empty()).dispose();
        } else {
            collisions.removeIf(c -> c.involves(train) && !trains.containsKey(c.getFirst() == train
                    ? c.getSecond() : c.getFirst()));
        }
        wake();
    }

    /**
     * Remove every train.
     */
    @InvokeOnAnyThread
    public void removeAll() {
        synchronized (this) {
            removeAllLocked();
        }
        deliver();
        watchAddresses();
    }

    private synchronized void removeAllLocked() {
        for (VirtualTrain t : new ArrayList<>(trains.keySet())) {
            removeLocked(t);
        }
    }

    /**
     * @return the trains placed
     */
    @Nonnull
    @InvokeOnAnyThread
    public synchronized List<VirtualTrain> getTrains() {
        return new ArrayList<>(trains.keySet());
    }

    /**
     * Remove every train and stop using the speed sources.
     */
    @Override
    @InvokeOnAnyThread
    public void dispose() {
        removeAll();
        synchronized (watching) {
            for (SimulatedSpeedSource s : sources) {
                s.removeCommandListener(commandListener);
            }
            sources.clear();
        }
    }

    private synchronized void watchConnections() {
        if (watchedConnections == null) {
            watchedConnections = InstanceManager.getNullableDefault(ConnectionConfigManager.class);
            if (watchedConnections != null) {
                watchedConnections.addPropertyChangeListener(ConnectionConfigManager.CONNECTIONS, connectionListener);
            }
        }
    }

    private synchronized void unwatchConnections() {
        if (watchedConnections != null) {
            watchedConnections.removePropertyChangeListener(ConnectionConfigManager.CONNECTIONS, connectionListener);
            watchedConnections = null;
        }
    }

    private void connectionsChanged(PropertyChangeEvent e) {
        synchronized (this) {
            if (!trains.isEmpty() && !SimulationCheck.isFullySimulated()) {
                log.warn("A connection that is not a simulator was added, so the simulated trains have been removed.");
                removeAllLocked();
            }
        }
        deliver();
        watchAddresses();
    }

    // ---------------------------------------------------------------------
    // Ground truth

    /**
     * Where every train is now. Taken under a lock held only for copying.
     * <p>
     * While trains move, positions change a step at a time, so the truth is
     * for the end of the last step, and its time says so; it can be up to
     * one step ({@value #STEP_MS} ms) before this is called. While none can
     * move, it is for now.
     *
     * @return the truth
     */
    @Nonnull
    @InvokeOnAnyThread
    public synchronized SimulationTruth getTruth() {
        List<SimulationTruth.TrainTruth> list = new ArrayList<>();
        for (TrainState t : trains.values()) {
            TrainState.Span front = t.spanAt(t.position);
            Message held = t.held;
            list.add(new SimulationTruth.TrainTruth(t.train, t.blocks(), t.position - front.start, t.speed,
                    t.crashed, t.stalled, t.derailed, held == null ? null : held.text()));
        }
        long when = manual || thread == null || idle() ? now() : lastTick; // positions are as of the last step
        return new SimulationTruth(when, list, new ArrayList<>(collisions), new ArrayList<>(dropOutRecords),
                faults.describe());
    }

    // ---------------------------------------------------------------------
    // The mover

    private synchronized void startMover() {
        if (manual || thread != null) {
            return;
        }
        lastTick = System.nanoTime();
        Thread t = ThreadingUtil.newThread(this::run, THREAD_NAME);
        t.setDaemon(true);
        thread = t;
        t.start();
    }

    private void wake() {
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * @return the received time of the earliest command a train's decoder
     *         has not yet obeyed, or Long.MAX_VALUE if none
     */
    private long earliestNewCommand() {
        long earliest = Long.MAX_VALUE;
        for (TrainState t : trains.values()) {
            for (int i = 0; i < t.decoders.length; i++) {
                SimulatedSpeedSource.Command c = commandFor(t.train.getLocos().get(i).getRespondingAddress());
                if (c != null && !c.equals(t.latched[i])) {
                    earliest = Math.min(earliest, Math.max(c.getTimeNanos(), t.placedNanos));
                }
            }
        }
        return earliest;
    }

    private boolean idle() {
        for (TrainState t : trains.values()) {
            if (!t.idle(this)) {
                return false;
            }
        }
        return true;
    }

    /**
     * The mover thread: a step every {@value #STEP_MS} ms of the clock while
     * any train can move; waiting, at no cost, while none can.
     */
    private void run() {
        try {
            long next;
            synchronized (this) {
                next = lastTick + STEP_NANOS;
            }
            while (true) {
                synchronized (this) {
                    if (trains.isEmpty()) {
                        thread = null; // in the same lock as the check, so a new placement starts a new thread
                        return;
                    }
                    if (idle()) {
                        long idleSince = lastTick;
                        wait();
                        // nothing moved while idle, so start from when the first new command
                        // was received, so that it takes effect from then
                        lastTick = Math.max(idleSince, Math.min(System.nanoTime(), earliestNewCommand()));
                        next = lastTick + STEP_NANOS;
                        continue;
                    }
                    long now = System.nanoTime();
                    if (now < next) {
                        long wait = next - now;
                        wait(wait / 1_000_000L, (int) (wait % 1_000_000L));
                        continue;
                    }
                    if (now - next > 1_000_000_000L) {
                        log.warn("Simulated trains fell {} ms behind; time skipped", (now - next) / 1_000_000L);
                        lastTick = now - STEP_NANOS;
                        next = now;
                    }
                    step(lastTick);
                    lastTick = next;
                    next += STEP_NANOS;
                }
                deliver();
            }
        } catch (InterruptedException e) {
            log.debug("Simulated trains interrupted");
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            log.error("Simulated trains stopped by an error", e);
        } finally {
            synchronized (this) {
                if (thread == Thread.currentThread()) {
                    thread = null;
                }
            }
        }
    }

    /**
     * Run a manual layout for a time, step by step, on the calling thread.
     *
     * @param seconds how long
     */
    void runFor(double seconds) {
        if (!manual) {
            throw new IllegalStateException("only a manual layout can be run step by step");
        }
        long steps = Math.round(seconds / STEP_SECONDS);
        for (long i = 0; i < steps; i++) {
            synchronized (this) {
                step(manualClock);
                manualClock += STEP_NANOS;
            }
            deliver();
        }
    }

    /** A detected stretch going into or out of a span. */
    private static final class Event {

        final double time;
        final TrainState train;
        /** The detected stretch going into or out of a span, or null for a drop-out. */
        @CheckForNull
        final TrainState.Membership membership;
        /** For a drop-out: the Block. */
        @CheckForNull
        final Block dropBlock;
        /** Into the span, or into the drop-out stretch. */
        final boolean enter;

        Event(double time, TrainState train, TrainState.Membership membership, boolean enter) {
            this.time = time;
            this.train = train;
            this.membership = membership;
            this.dropBlock = null;
            this.enter = enter;
        }

        Event(double time, TrainState train, Block dropBlock, boolean enter) {
            this.time = time;
            this.train = train;
            this.membership = null;
            this.dropBlock = dropBlock;
            this.enter = enter;
        }
    }

    private static boolean memberAt(double position, double[] marker, TrainState.Span s) {
        double front = position - marker[0];
        double rear = front - marker[1];
        return front > s.start && rear < s.end;
    }

    /**
     * One step of every train, from a time.
     *
     * @param start when the step starts, nanoseconds
     */
    private void step(long start) {
        double dt = STEP_SECONDS;
        List<TrainState> list = new ArrayList<>(trains.values());
        Map<TrainState, Motion> motions = new HashMap<>();
        Map<TrainState, Velocity> velocities = new HashMap<>();
        for (TrainState t : list) {
            double[] locoSpeeds = new double[t.decoders.length];
            Velocity v = t.velocity(this, start, dt, locoSpeeds);
            checkLocosAgree(t, locoSpeeds);
            double reach = 0;
            for (Velocity.Part p : v.parts) {
                reach += Math.max(Math.abs(p.v0), Math.abs(p.v1)) * (p.t1 - p.t0);
            }
            if (reach > 0) {
                t.planAhead(models, reach + 1.0);
            } else {
                t.frontLimit = t.front().end;
                t.rearLimit = t.rear().start;
            }
            velocities.put(t, v);
            motions.put(t, Motion.of(v, t.position, t.rearLimit + t.length, t.frontLimit));
        }
        stallPoints(list, motions);
        findCollisions(list, motions, start);
        List<Event> events = new ArrayList<>();
        for (TrainState t : list) {
            crossings(t, motions.get(t), events);
            dropOutCrossings(t, motions.get(t), events);
        }
        events.sort((a, b) -> a.time != b.time ? Double.compare(a.time, b.time) : Boolean.compare(b.enter, a.enter));
        apply(events, start);
        for (TrainState t : list) {
            Motion m = motions.get(t);
            t.position = m.endPosition();
            t.speed = m.velocity(dt - 1e-9);
            noteHeld(t, velocities.get(t).after(dt - 1e-9));
            forgetPassed(t);
            t.trim();
        }
    }

    private void checkLocosAgree(TrainState t, double[] speeds) {
        if (speeds.length < 2) {
            return;
        }
        double lo = Double.MAX_VALUE;
        double hi = 0;
        boolean forward = false;
        boolean backward = false;
        for (double s : speeds) {
            lo = Math.min(lo, Math.abs(s));
            hi = Math.max(hi, Math.abs(s));
            forward |= s > 0;
            backward |= s < 0;
        }
        String name = t.train.getName();
        if (forward && backward) {
            if (models.firstWarning(t.train, "opposite ways", name)) {
                log.warn("The locos of train \"{}\" are driving it opposite ways, so it does not move.", name);
            }
        } else if (hi > 0 && hi > lo * 1.1) {
            if (models.firstWarning(t.train, "speeds differ", name)) {
                log.warn("The locos of train \"{}\" differ in speed by more than 10%; it moves at the slowest one's speed.",
                        name);
            }
        }
    }

    private void noteHeld(TrainState t, double pushing) {
        Message reason = null;
        if (t.stalled || t.derailed) {
            // it can't move at all: held by the fault for as long as its locos drive it
            if (!t.crashed && t.drivenSpeed != 0.0) {
                reason = t.faultReason;
            }
        } else if (!t.crashed && pushing > 0 && t.position >= t.frontLimit) {
            reason = t.frontReason;
        } else if (!t.crashed && pushing < 0 && t.rearPosition() <= t.rearLimit) {
            reason = t.rearReason;
        }
        if (reason != null && !reason.equals(t.held)) {
            log.warn("Simulated train \"{}\" has stopped: {}", t.train.getName(), reason.english());
        }
        t.held = reason;
    }

    private void findCollisions(List<TrainState> list, Map<TrainState, Motion> motions, long start) {
        while (true) {
            Collisions.Found first = null;
            TrainState fa = null;
            TrainState fb = null;
            for (int i = 0; i < list.size(); i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    TrainState a = list.get(i);
                    TrainState b = list.get(j);
                    if (a.crashed && b.crashed) {
                        continue;
                    }
                    Collisions.Found f = Collisions.first(a, motions.get(a), b, motions.get(b));
                    if (f != null && (first == null || f.time < first.time)) {
                        first = f;
                        fa = a;
                        fb = b;
                    }
                }
            }
            if (first == null || fa == null || fb == null) {
                return;
            }
            motions.put(fa, motions.get(fa).stoppedAt(first.time));
            motions.put(fb, motions.get(fb).stoppedAt(first.time));
            fa.crashed = true;
            fb.crashed = true;
            long when = start + Math.round(first.time * 1e9);
            collisions.add(new SimulationTruth.Collision(fa.train, fb.train, first.block, when));
            log.warn("Simulated trains \"{}\" and \"{}\" have collided in \"{}\".", fa.train.getName(),
                    fb.train.getName(), models.name(first.block));
        }
    }

    /**
     * The times in a step when the train's detected stretches go into or out
     * of its spans.
     */
    private static void crossings(TrainState t, Motion m, List<Event> out) {
        for (int i = 0; i < t.markers.size(); i++) {
            double[] mk = t.markers.get(i);
            for (TrainState.Span s : t.spans) {
                TrainState.Membership key = new TrainState.Membership(i, s);
                List<Motion.Crossing> fronts = m.crossings(-mk[0], s.start, true);
                List<Motion.Crossing> rears = m.crossings(-mk[0] - mk[1], s.end, false);
                boolean frontIn = m.position(m.startTime()) - mk[0] > s.start;
                boolean rearIn = m.position(m.startTime()) - mk[0] - mk[1] < s.end;
                boolean member = frontIn && rearIn;
                int a = 0;
                int b = 0;
                while (a < fronts.size() || b < rears.size()) {
                    double time;
                    if (b >= rears.size() || (a < fronts.size() && fronts.get(a).time <= rears.get(b).time)) {
                        time = fronts.get(a).time;
                        frontIn = fronts.get(a).inside;
                        a++;
                    } else {
                        time = rears.get(b).time;
                        rearIn = rears.get(b).inside;
                        b++;
                    }
                    boolean now = frontIn && rearIn;
                    if (now != member) {
                        out.add(new Event(time, t, key, now));
                        member = now;
                    }
                }
            }
        }
    }

    /**
     * Apply detection changes in order, reporting each Block's sensor when
     * its count of detected stretches goes from 0 to 1 or 1 to 0.
     */
    private void apply(List<Event> events, long start) {
        for (Event e : events) {
            long when = start + Math.round(e.time * 1e9);
            TrainState.Membership member = e.membership;
            if (member == null) {
                dropOutChanged(Objects.requireNonNull(e.dropBlock), e.train, e.enter, when);
                continue;
            }
            Block block = member.span.block();
            int count = occupancy.getOrDefault(block, 0);
            if (e.enter) {
                if (!e.train.in.add(member)) {
                    continue;
                }
                occupancy.put(block, count + 1);
            } else {
                if (!e.train.in.remove(member)) {
                    continue;
                }
                if (count <= 1) {
                    occupancy.remove(block);
                } else {
                    occupancy.put(block, count - 1);
                }
            }
            detectionChanged(block, when);
        }
    }

    // ---------------------------------------------------------------------
    // Detection as the detectors see it, with faults

    /**
     * Report a Block's sensor if what its detector sees has changed: a train
     * detected there and not dropped out, or a phantom.
     */
    @GuardedBy("this")
    private void detectionChanged(Block block, long when) {
        boolean on = faults.phantoms.contains(block) || (occupancy.getOrDefault(block, 0) > 0 && !droppedOut(block));
        if (on != shown.contains(block)) {
            if (on) {
                shown.add(block);
            } else {
                shown.remove(block);
            }
            report(block, on ? Sensor.ACTIVE : Sensor.INACTIVE, when);
        }
    }

    /**
     * Is every train detected in a Block one whose front is in a drop-out
     * stretch of it?
     */
    @GuardedBy("this")
    private boolean droppedOut(Block block) {
        Map<TrainState, Integer> d = dropping.get(block);
        if (d == null || d.isEmpty()) {
            return false;
        }
        for (TrainState t : trains.values()) {
            if (!d.containsKey(t)) {
                for (TrainState.Membership m : t.in) {
                    if (m.span.block() == block) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @GuardedBy("this")
    private void dropOutChanged(Block block, TrainState t, boolean into, long when) {
        Map<TrainState, Integer> d = dropping.computeIfAbsent(block, b -> new HashMap<>());
        boolean wasOut = droppedOut(block) && occupancy.getOrDefault(block, 0) > 0;
        int n = d.getOrDefault(t, 0) + (into ? 1 : -1);
        if (n > 0) {
            d.put(t, n);
        } else {
            d.remove(t);
        }
        boolean isOut = droppedOut(block) && occupancy.getOrDefault(block, 0) > 0;
        if (isOut && !wasOut) {
            dropOutRecords.add(new SimulationTruth.DropOut(block, t.train, when, 0));
        } else if (wasOut && !isOut) {
            endDropOutRecord(block, when);
        }
        detectionChanged(block, when);
    }

    @GuardedBy("this")
    private void endDropOutRecord(Block block, long when) {
        for (int i = dropOutRecords.size() - 1; i >= 0; i--) {
            SimulationTruth.DropOut r = dropOutRecords.get(i);
            if (r.getBlock() == block && r.getEndNanos() == 0) {
                dropOutRecords.set(i, r.ended(when));
                return;
            }
        }
    }

    /** A train removed or replaced: its front is in no drop-out stretch. */
    @GuardedBy("this")
    private void endDropOuts(TrainState t, long when) {
        for (Map.Entry<Block, Map<TrainState, Integer>> e : new ArrayList<>(dropping.entrySet())) {
            Integer n = e.getValue().get(t);
            for (int i = 0; n != null && i < n; i++) {
                dropOutChanged(e.getKey(), t, false, when);
            }
        }
    }

    /** A Block's neighbour at the end of a span the train went into it from. */
    @CheckForNull
    private static Block startSide(TrainState.Span s) {
        TrackEntry back = s.passage.entry.reverse();
        return back == null ? null : back.getBlock();
    }

    /** A Block's neighbour at the end of a span the train goes on towards. */
    @CheckForNull
    private static Block endSide(TrainState.Span s) {
        TrackEntry next = s.passage.next;
        return next == null ? null : next.getBlock();
    }

    /**
     * Where a stretch of a Block lies in a span through it, in the train's
     * positions: {from, to}, or nothing if the span doesn't run from the end
     * the stretch is measured from or to.
     */
    private static double[] place(TrainState.Span s, SimulatedFaults.Stretch st) {
        if (st.from == startSide(s)) {
            return new double[]{s.start + st.fromMm, s.start + st.toMm};
        }
        if (st.from == endSide(s)) {
            return new double[]{s.end - st.toMm, s.end - st.fromMm};
        }
        return new double[0];
    }

    /** The drop-out stretches of a train's span, set or at random, in the train's positions. */
    @GuardedBy("this")
    private List<double[]> dropOutStretches(TrainState t, TrainState.Span s) {
        List<double[]> list = new ArrayList<>();
        for (SimulatedFaults.Stretch st : faults.dropOuts) {
            if (st.block == s.block()) {
                double[] p = place(s, st);
                if (p.length == 2) {
                    list.add(p);
                }
            }
        }
        java.util.Random random = faults.random;
        if (random != null) {
            List<Object> key = java.util.Arrays.asList(t, s.passage.entry);
            double[] r = randomStretches.get(key);
            if (r == null) {
                if (random.nextDouble() < faults.randomProbability) {
                    double room = Math.max(0.0, s.length() - faults.randomLengthMm);
                    double from = random.nextDouble() * room;
                    r = new double[]{from, Math.min(s.length(), from + faults.randomLengthMm)};
                } else {
                    r = new double[0];
                }
                randomStretches.put(key, r);
            }
            if (r.length == 2) {
                list.add(new double[]{s.start + r[0], s.start + r[1]});
            }
        }
        return list;
    }

    /** Drop the random stretches of the Blocks a train has wholly left behind. */
    @GuardedBy("this")
    private void forgetPassed(TrainState t) {
        double r = t.rearPosition();
        for (TrainState.Span s : t.spans) {
            if (s.end <= r) {
                randomStretches.remove(java.util.Arrays.asList(t, s.passage.entry));
            }
        }
    }

    /**
     * The times in a step when the train's front goes into or out of a
     * drop-out stretch.
     */
    @GuardedBy("this")
    private void dropOutCrossings(TrainState t, Motion m, List<Event> out) {
        double p0 = m.position(m.startTime());
        Map<Block, Integer> insideAtStart = new HashMap<>();
        Map<TrainState.Span, List<double[]>> all = new java.util.LinkedHashMap<>();
        for (TrainState.Span s : t.spans) {
            List<double[]> stretches = dropOutStretches(t, s);
            all.put(s, stretches);
            for (double[] st : stretches) {
                if (p0 > st[0] && p0 < st[1]) {
                    insideAtStart.merge(s.block(), 1, Integer::sum);
                }
            }
        }
        // bring the record into line with where the front is now: a train placed, or parted, in a stretch
        java.util.Set<Block> blocks = new HashSet<>(insideAtStart.keySet());
        for (Map.Entry<Block, Map<TrainState, Integer>> e : dropping.entrySet()) {
            if (e.getValue().containsKey(t)) {
                blocks.add(e.getKey());
            }
        }
        for (Block b : blocks) {
            Map<TrainState, Integer> d = dropping.get(b);
            int recorded = (d == null) ? 0 : d.getOrDefault(t, 0);
            int now = insideAtStart.getOrDefault(b, 0);
            for (int i = recorded; i < now; i++) {
                out.add(new Event(0.0, t, b, true));
            }
            for (int i = now; i < recorded; i++) {
                out.add(new Event(0.0, t, b, false));
            }
        }
        for (Map.Entry<TrainState.Span, List<double[]>> e : all.entrySet()) {
            TrainState.Span s = e.getKey();
            for (double[] st : e.getValue()) {
                List<Motion.Crossing> ins = m.crossings(0.0, st[0], true);
                List<Motion.Crossing> outs = m.crossings(0.0, st[1], false);
                boolean aboveFrom = p0 > st[0];
                boolean belowTo = p0 < st[1];
                boolean inside = aboveFrom && belowTo;
                int a = 0;
                int b = 0;
                while (a < ins.size() || b < outs.size()) {
                    double time;
                    if (b >= outs.size() || (a < ins.size() && ins.get(a).time <= outs.get(b).time)) {
                        time = ins.get(a).time;
                        aboveFrom = ins.get(a).inside;
                        a++;
                    } else {
                        time = outs.get(b).time;
                        belowTo = outs.get(b).inside;
                        b++;
                    }
                    boolean now = aboveFrom && belowTo;
                    if (now != inside) {
                        out.add(new Event(time, t, s.block(), now));
                        inside = now;
                    }
                }
            }
        }
    }

    /** Stop each train whose front reaches a stall point this step. */
    @GuardedBy("this")
    private void stallPoints(List<TrainState> list, Map<TrainState, Motion> motions) {
        if (faults.stallPoints.isEmpty()) {
            return;
        }
        for (TrainState t : list) {
            if (t.stalled || t.unpowered) {
                continue;
            }
            Motion m = motions.get(t);
            double first = Double.POSITIVE_INFINITY;
            Block where = null;
            List<Object> whereKey = null;
            for (TrainState.Span s : t.spans) {
                for (SimulatedFaults.Stretch st : faults.stallPoints) {
                    if (st.block != s.block()) {
                        continue;
                    }
                    double[] p = place(s, st);
                    List<Object> key = java.util.Arrays.asList(st, s.passage.entry);
                    if (p.length != 2 || t.stallsPassed.contains(key)) {
                        continue; // a train stalls once at a point on its way through a Block
                    }
                    for (Motion.Crossing c : m.crossings(0.0, p[0], true)) {
                        if (c.inside && c.time < first) {
                            first = c.time;
                            where = s.block();
                            whereKey = key;
                        }
                    }
                }
            }
            if (where != null) {
                motions.put(t, m.stoppedAt(first));
                t.stallsPassed.add(whereKey);
                t.stalled = true;
                t.faultReason = models.message("StalledAt", models.name(where));
            }
        }
    }

    /**
     * Set or clear a phantom: a Block's sensor shown occupied with no train.
     *
     * @param block the Block
     * @param on    true to set, false to clear
     */
    void phantom(Block block, boolean on) {
        synchronized (this) {
            if (on && trains.isEmpty()) {
                throw new IllegalStateException(Bundle.getMessage("FaultNeedsTrain"));
            }
            if (on) {
                faults.phantoms.add(block);
            } else {
                faults.phantoms.remove(block);
            }
            if (!trains.isEmpty()) {
                detectionChanged(block, now());
            }
        }
        deliver();
    }

    /**
     * Stall a train, or let it move again.
     *
     * @param train the train
     * @param on    true to stall it, false to let it move
     */
    void stall(VirtualTrain train, boolean on) {
        synchronized (this) {
            TrainState t = placed(train);
            t.stalled = on;
            t.faultReason = on ? models.message("Stalled")
                    : (t.derailed ? t.faultReason : null);
            wake();
        }
    }

    /**
     * Put a derailed train back on the track.
     *
     * @param train the train
     */
    void rerail(VirtualTrain train) {
        synchronized (this) {
            TrainState t = placed(train);
            t.derailed = false;
            t.faultReason = t.stalled ? models.message("Stalled") : null;
            wake();
        }
    }

    @GuardedBy("this")
    private TrainState placed(VirtualTrain train) {
        TrainState t = trains.get(train);
        if (t == null) {
            throw new IllegalStateException(Bundle.getMessage("TrainNotPlaced", train.getName()));
        }
        return t;
    }

    /**
     * Part a train: the front part runs on as the same train, the rear
     * becomes stock of its own.
     *
     * @param train the train
     * @param cut   where it parts, mm from its front
     * @return the rear part
     */
    VirtualTrain breakaway(VirtualTrain train, double cut) {
        VirtualTrain rear;
        synchronized (this) {
            TrainState t = placed(train);
            if (cut <= 0 || cut >= t.length) {
                throw new IllegalArgumentException(Bundle.getMessage("BreakawayOutsideTrain", t.length));
            }
            rear = VirtualTrain.stock(Bundle.getMessage("BreakawayRearName", train.getName()), t.length - cut);
            TrainState f = new TrainState(t, train, 0.0, cut);
            TrainState r = new TrainState(t, rear, cut, t.length);
            f.crashed = t.crashed;
            f.stalled = t.stalled;
            f.derailed = t.derailed;
            f.faultReason = t.faultReason;
            r.crashed = t.crashed;
            r.derailed = t.derailed;
            r.faultReason = t.derailed ? t.faultReason : null;
            long when = now();
            List<Event> events = new ArrayList<>();
            for (TrainState part : List.of(f, r)) {
                for (int i = 0; i < part.markers.size(); i++) {
                    double[] mk = part.markers.get(i);
                    for (TrainState.Span s : part.spans) {
                        if (memberAt(part.position, mk, s)) {
                            events.add(new Event(0.0, part, new TrainState.Membership(i, s), true));
                        }
                    }
                }
            }
            for (TrainState.Membership m : new ArrayList<>(t.in)) {
                events.add(new Event(0.0, t, m, false)); // after the parts' entries, so no Block shows empty between
            }
            trains.remove(train);
            trains.put(train, f);
            trains.put(rear, r);
            // the front part's front is where the whole train's was
            for (Map<TrainState, Integer> d : dropping.values()) {
                Integer n = d.remove(t);
                if (n != null) {
                    d.put(f, n);
                }
            }
            randomStretches.keySet().removeIf(k -> k.get(0) == t);
            apply(events, when);
            f.trim();
            r.trim();
            startMover();
            wake();
        }
        deliver();
        log.debug("Train {} parted; its rear is {}", train.getName(), rear.getName());
        return rear;
    }

    /**
     * Queue a sensor report, to be delivered once this layout's lock is
     * released.
     */
    @GuardedBy("this")
    private void report(Block shownBlock, int state, long timeNanos) {
        TrackSnapshot snap = models.snapshot();
        Block block = faults.reportedOn(shownBlock); // a swapped sensor shows another Block's occupancy
        Sensor sensor = snap.sensor(block);
        if (sensor == null) {
            return; // a dark Block: reported to nobody, as on a real layout
        }
        Optional<SimulatedDetectorOutput> output = snap.output(block);
        if (output.isPresent()) {
            SimulatedDetectorOutput o = output.get();
            reports.add(() -> o.report(sensor, state, timeNanos));
        } else {
            log.debug("Sensor {} of Block {} is not reported", sensor.getSystemName(), snap.name(block));
        }
    }

    /**
     * Deliver the queued sensor reports, in the order they were queued.
     * Called without this layout's lock, so an output can't deadlock with a
     * thread waiting for that lock; outputs must not wait for the layout
     * thread either (see {@link SimulatedDetectorOutput#report}).
     */
    private void deliver() {
        synchronized (delivering) {
            Runnable r = reports.poll();
            while (r != null) {
                r.run();
                r = reports.poll();
            }
        }
    }

    /**
     * @return true if the mover thread is running
     */
    synchronized boolean isMoverRunning() {
        return thread != null;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SimulatedLayout.class);
}
