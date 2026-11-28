package jmri.jmrit.simulation.link;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.WeakHashMap;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;

import jmri.SystemConnectionMemo;

/**
 * The time a simulated connection's messages take between JMRI and the
 * simulated layout: through the interface's {@link Stage}s and over the
 * shared {@link Medium}.
 * <pre>
 * JMRI --&gt; stages towards the layout --&gt; medium --&gt; layout
 * JMRI &lt;-- stages towards JMRI &lt;------- medium &lt;-- layout
 * </pre>
 * A system makes one link per simulated connection, with its
 * {@link WireTiming} and its {@link LinkPreset}s, registers it with
 * {@link #register}, and calls {@link #send} where its simulator exchanges
 * messages with JMRI. Scripts and tests find it with {@link #forConnection}.
 * <p>
 * Off until a preset other than {@link LinkPreset#NONE} is chosen: then
 * {@link #send} hands each message on at once, on the caller's thread, with
 * no thread or timer, exactly as with no link. Once on, every stage and the
 * medium run on the link's own {@link LinkClock}, and a message comes out on
 * that clock's thread.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public final class SimulatedLink {

    /** Which way a message is going. */
    public enum Direction {
        /** From JMRI to the simulated layout. */
        TOWARDS_LAYOUT,
        /** From the simulated layout to JMRI. */
        TOWARDS_JMRI
    }

    private static final Map<SystemConnectionMemo, SimulatedLink> LINKS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private final WireTiming timing;
    private final List<LinkPreset> presets;
    private final Supplier<LinkClock> clocks;

    @GuardedBy("this")
    private LinkPreset preset = LinkPreset.NONE;
    @GuardedBy("this")
    private LinkClock clock = null;
    // the rest is used only on the clock's thread
    private volatile List<Stage> towardsLayout = List.of(); // read on any thread
    private volatile List<Stage> towardsJmri = List.of();
    private volatile Medium medium = null;
    private Stage.Receiver layoutEntry = null;
    private Stage.Receiver jmriEntry = null;
    private final PriorityQueue<Event> events = new PriorityQueue<>();
    private long eventSequence = 0;

    private static final class Event implements Comparable<Event> {

        final long at;
        final long sequence;
        final Runnable task;

        Event(long at, long sequence, Runnable task) {
            this.at = at;
            this.sequence = sequence;
            this.task = task;
        }

        @Override
        public int compareTo(Event o) {
            long d = at - o.at; // nanoTime values compare by their difference
            if (d != 0) {
                return d < 0 ? -1 : 1;
            }
            return Long.compare(sequence, o.sequence);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Event && ((Event) o).sequence == sequence;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(sequence);
        }
    }

    /**
     * @param timing  the system's wire timing
     * @param presets the system's presets; {@link LinkPreset#NONE} is put
     *                first if it isn't there
     */
    public SimulatedLink(@Nonnull WireTiming timing, @Nonnull List<LinkPreset> presets) {
        this(timing, presets, LinkClock::realTime);
    }

    /**
     * @param timing  the system's wire timing
     * @param presets the system's presets; {@link LinkPreset#NONE} is put
     *                first if it isn't there
     * @param clocks  makes the clock the link runs on each time it is turned
     *                on: for tests
     */
    public SimulatedLink(@Nonnull WireTiming timing, @Nonnull List<LinkPreset> presets,
            @Nonnull Supplier<LinkClock> clocks) {
        this.timing = timing;
        List<LinkPreset> all = new ArrayList<>(presets);
        all.remove(LinkPreset.NONE);
        all.add(0, LinkPreset.NONE);
        this.presets = Collections.unmodifiableList(all);
        this.clocks = clocks;
    }

    /**
     * Make a link the one for a connection, for {@link #forConnection}.
     *
     * @param memo the connection
     * @param link its link
     */
    public static void register(@Nonnull SystemConnectionMemo memo, @Nonnull SimulatedLink link) {
        LINKS.put(memo, link);
    }

    /**
     * @param memo a connection
     * @return its link, or null if it has none
     */
    @CheckForNull
    public static SimulatedLink forConnection(@Nonnull SystemConnectionMemo memo) {
        return LINKS.get(memo);
    }

    /**
     * @return the presets this link offers, {@link LinkPreset#NONE} first
     */
    @Nonnull
    public List<LinkPreset> getPresets() {
        return presets;
    }

    /**
     * @return the preset in use
     */
    @Nonnull
    public synchronized LinkPreset getPreset() {
        return preset;
    }

    /**
     * @return true if a preset other than {@link LinkPreset#NONE} is in use
     */
    public synchronized boolean isOn() {
        return clock != null;
    }

    /**
     * Use a preset. Messages already held by the old preset's stages go on
     * at once; choosing {@link LinkPreset#NONE} turns the link off.
     *
     * @param chosen the preset
     */
    public synchronized void setPreset(@Nonnull LinkPreset chosen) {
        if (chosen == preset) {
            return;
        }
        preset = chosen;
        if (chosen == LinkPreset.NONE) {
            if (clock != null) {
                LinkClock old = clock;
                clock = null;
                old.schedule(old.now(), () -> {
                    layoutEntry = null; // from now on, messages arrive at once
                    jmriEntry = null;
                    takeOut(old.now());
                    runAll(); // what was on its way comes out now
                    old.dispose();
                });
            }
            return;
        }
        List<Stage> layout = chosen.makeTowardsLayout();
        List<Stage> jmri = chosen.makeTowardsJmri();
        if (clock == null) {
            clock = clocks.get();
        }
        LinkClock c = clock;
        c.schedule(c.now(), () -> {
            takeOut(c.now());
            build(layout, jmri);
        });
    }

    private void takeOut(long now) {
        for (Stage s : towardsLayout) {
            s.releaseAll(now);
        }
        for (Stage s : towardsJmri) {
            s.releaseAll(now);
        }
    }

    private void build(@Nonnull List<Stage> layout, @Nonnull List<Stage> jmri) {
        if (medium == null) {
            medium = new Medium(this, timing);
        }
        Medium m = medium;
        // towards the layout: stages, then the medium, then the layout
        Buffer buffer = !layout.isEmpty() && layout.get(layout.size() - 1) instanceof Buffer
                ? (Buffer) layout.get(layout.size() - 1) : null;
        Stage.Receiver next = (f, t) -> m.take(f, t, LinkFrame::arrive, buffer);
        for (int i = layout.size() - 1; i >= 0; i--) {
            layout.get(i).connect(this, next);
            Stage s = layout.get(i);
            next = s::take;
        }
        layoutEntry = next;
        // towards JMRI: the medium, then stages, then JMRI
        next = LinkFrame::arrive;
        for (int i = jmri.size() - 1; i >= 0; i--) {
            jmri.get(i).connect(this, next);
            Stage s = jmri.get(i);
            next = s::take;
        }
        Stage.Receiver afterMedium = next;
        jmriEntry = (f, t) -> m.take(f, t, afterMedium, null);
        towardsLayout = Collections.unmodifiableList(new ArrayList<>(layout));
        towardsJmri = Collections.unmodifiableList(new ArrayList<>(jmri));
    }

    /**
     * Send a message through the link. With the link off, it arrives at once,
     * here and now; otherwise it arrives on the link's clock thread, when the
     * stages and the medium let it through.
     *
     * @param direction which way it goes
     * @param message   the system's message
     * @param sentNanos when it was sent, on the {@link System#nanoTime()}
     *                  scale; a time to come waits for that time
     * @param onArrival told when it arrives, with the time it arrives; never
     *                  told if a stage drops it
     */
    public void send(@Nonnull Direction direction, @Nonnull Object message, long sentNanos,
            @Nonnull LongConsumer onArrival) {
        LinkClock c;
        synchronized (this) {
            c = clock;
        }
        if (c == null) {
            onArrival.accept(sentNanos);
            return;
        }
        LinkFrame frame = new LinkFrame(message, direction, sentNanos, onArrival);
        c.schedule(c.now(), () -> {
            long at = sentNanos - c.now() > 0 ? sentNanos : c.now();
            at(at, () -> {
                Stage.Receiver entry = direction == Direction.TOWARDS_LAYOUT ? layoutEntry : jmriEntry;
                if (entry == null) {
                    frame.arrive(at); // turned off meanwhile
                } else {
                    entry.take(frame, at);
                }
            });
            runDue(c);
        });
    }

    /**
     * Run a task at a time, in order with every other event of the link. On
     * the clock's thread only.
     *
     * @param atNanos when
     * @param task    the task
     */
    void at(long atNanos, @Nonnull Runnable task) {
        events.add(new Event(atNanos, eventSequence++, task));
        LinkClock c;
        synchronized (this) {
            c = clock;
        }
        if (c != null) {
            c.schedule(atNanos, () -> runDue(c));
        }
    }

    private void runAll() {
        while (!events.isEmpty()) {
            events.poll().task.run();
        }
    }

    private void runDue(@Nonnull LinkClock c) {
        long now = c.now();
        while (!events.isEmpty() && events.peek().at - now <= 0) {
            events.poll().task.run();
        }
    }

    /**
     * @param direction which way
     * @return the stages in use that way, in order
     */
    @Nonnull
    public List<Stage> getStages(@Nonnull Direction direction) {
        return direction == Direction.TOWARDS_LAYOUT ? towardsLayout : towardsJmri;
    }

    /**
     * @return the medium, or null if the link has never been on
     */
    @CheckForNull
    public Medium getMedium() {
        return medium;
    }

    /**
     * Turn the link off for good, passing on whatever it holds.
     */
    public void dispose() {
        setPreset(LinkPreset.NONE);
    }
}
