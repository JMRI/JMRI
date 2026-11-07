package jmri.jmrit.simulation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.ServiceLoader;
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
    private final List<SimulatedSpeedSource> sources = new CopyOnWriteArrayList<>();
    /**
     * Held while telling the speed sources which addresses to watch, so they
     * are told one at a time and in order; never taken with this layout's lock
     * held.
     */
    private final Object watching = new Object();
    /** Held while taking up the adapters' sources and outputs. */
    private final Object adapting = new Object();
    /** The sources and outputs taken from adapters, so those dropped can be removed. */
    @GuardedBy("adapting")
    private final List<SimulatedSpeedSource> adapterSources = new ArrayList<>();
    @GuardedBy("adapting")
    private final List<SimulatedDetectorOutput> adapterOutputs = new ArrayList<>();
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
        useAdapters();
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
        useAdapters();
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
        apply(events, now());
        if (trains.isEmpty()) {
            collisions.clear();
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
        synchronized (adapting) {
            adapterSources.clear();
            adapterOutputs.clear();
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
        useAdapters();
        watchAddresses();
    }

    /**
     * Take up the speed sources and detector outputs the systems' adapters
     * offer now, and drop those they no longer offer. Not for a layout on a
     * manual clock, whose tests give it their own sources and outputs.
     * Called without this layout's lock.
     */
    private void useAdapters() {
        if (manual) {
            return;
        }
        List<SimulatedSpeedSource> offeredSources = new ArrayList<>();
        List<SimulatedDetectorOutput> offeredOutputs = new ArrayList<>();
        for (SimulationAdapter a : ServiceLoader.load(SimulationAdapter.class)) {
            offeredSources.addAll(a.getSpeedSources());
            offeredOutputs.addAll(a.getDetectorOutputs());
        }
        List<SimulatedSpeedSource> dropSources = new ArrayList<>();
        List<SimulatedSpeedSource> newSources = new ArrayList<>();
        List<SimulatedDetectorOutput> dropOutputs = new ArrayList<>();
        List<SimulatedDetectorOutput> newOutputs = new ArrayList<>();
        // only the bookkeeping under the lock: adding an output copies the configuration on the
        // layout thread (Swing thread), which may itself be waiting to take up adapters
        synchronized (adapting) {
            for (SimulatedSpeedSource s : new ArrayList<>(adapterSources)) {
                if (!offeredSources.contains(s)) {
                    adapterSources.remove(s);
                    dropSources.add(s);
                }
            }
            for (SimulatedSpeedSource s : offeredSources) {
                if (!adapterSources.contains(s)) {
                    adapterSources.add(s);
                    newSources.add(s);
                }
            }
            for (SimulatedDetectorOutput o : new ArrayList<>(adapterOutputs)) {
                if (!offeredOutputs.contains(o)) {
                    adapterOutputs.remove(o);
                    dropOutputs.add(o);
                }
            }
            for (SimulatedDetectorOutput o : offeredOutputs) {
                if (!adapterOutputs.contains(o)) {
                    adapterOutputs.add(o);
                    newOutputs.add(o);
                }
            }
        }
        dropSources.forEach(this::removeSpeedSource);
        newSources.forEach(this::addSpeedSource);
        dropOutputs.forEach(this::removeDetectorOutput);
        newOutputs.forEach(this::addDetectorOutput);
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
                    t.crashed, held == null ? null : held.text()));
        }
        long when = manual || thread == null || idle() ? now() : lastTick; // positions are as of the last step
        return new SimulationTruth(when, list, new ArrayList<>(collisions));
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
        final TrainState.Membership membership;
        final boolean enter;

        Event(double time, TrainState train, TrainState.Membership membership, boolean enter) {
            this.time = time;
            this.train = train;
            this.membership = membership;
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
        findCollisions(list, motions, start);
        List<Event> events = new ArrayList<>();
        for (TrainState t : list) {
            crossings(t, motions.get(t), events);
        }
        events.sort((a, b) -> a.time != b.time ? Double.compare(a.time, b.time) : Boolean.compare(b.enter, a.enter));
        apply(events, start);
        for (TrainState t : list) {
            Motion m = motions.get(t);
            t.position = m.endPosition();
            t.speed = m.velocity(dt - 1e-9);
            noteHeld(t, velocities.get(t).after(dt - 1e-9));
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
        if (!t.crashed && pushing > 0 && t.position >= t.frontLimit) {
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
            Block block = e.membership.span.block();
            int count = occupancy.getOrDefault(block, 0);
            if (e.enter) {
                if (!e.train.in.add(e.membership)) {
                    continue;
                }
                occupancy.put(block, count + 1);
                if (count == 0) {
                    report(block, Sensor.ACTIVE, start + Math.round(e.time * 1e9));
                }
            } else {
                if (!e.train.in.remove(e.membership)) {
                    continue;
                }
                if (count <= 1) {
                    occupancy.remove(block);
                    report(block, Sensor.INACTIVE, start + Math.round(e.time * 1e9));
                } else {
                    occupancy.put(block, count - 1);
                }
            }
        }
    }

    /**
     * Queue a sensor report, to be delivered once this layout's lock is
     * released.
     */
    @GuardedBy("this")
    private void report(Block block, int state, long timeNanos) {
        TrackSnapshot snap = models.snapshot();
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
