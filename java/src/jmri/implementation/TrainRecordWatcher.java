package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.InvokeOnLayoutThread;
import jmri.Path;
import jmri.Sensor;
import jmri.TrainRecord;
import jmri.TrainRecord.Confidence;
import jmri.TrainRecord.Reason;
import jmri.util.ThreadingUtil;

/**
 * Follows placed trains from Block to Block, for their
 * {@link DefaultTrainRecord}s. It watches only: it sets nothing on the layout.
 * <p>
 * While any record is placed, it listens to every Block's occupancy sensor:
 * <ul>
 * <li>When a Block goes occupied next to an end of exactly one placed
 * train, along a path that is set, that train has entered it at that end.
 * It is dated by when the sensor first reported it (its raw state), not by
 * when any debounce let it through.</li>
 * <li>When it is next to an end of several trains, each becomes
 * {@link Confidence#AMBIGUOUS} until the Block's value says which train it
 * is.</li>
 * <li>When it is next to no train's end, but can be reached from an end of
 * one train through a run of Blocks with no detector (each joined to the
 * next along a path that is set, by the one shortest way), that train has
 * entered it through them: the dark Blocks are added to its Blocks too. If
 * several trains could have, each is {@link Confidence#AMBIGUOUS} as
 * above.</li>
 * <li>When a Block a train is in goes unoccupied and the train is still in
 * other Blocks, it has left that Block if the Block is at one of its ends.
 * If it was the train's only Block, or one between others, the train is
 * kept there, {@link Confidence#ESTIMATED}: a drop-out and a departure onto
 * undetected track can't be told apart here. If the train is then seen to
 * enter the next Block, the Block it was kept in has been left.</li>
 * <li>A Block with no detector can't show that it is empty. A train leaves
 * the dark Blocks at its rear when it is seen to leave a detected Block in
 * front of them, since its rear must have passed them first. A train seen
 * in none of its Blocks is {@link Confidence#ESTIMATED}.</li>
 * </ul>
 * It also places a record when a Block's value is set to something that
 * identifies it, and makes a record for a roster entry or ID tag value that
 * has none.
 * <p>
 * Everything is handled on the layout thread (Swing thread), in the order
 * it happened.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public class TrainRecordWatcher {

    private final Supplier<List<DefaultTrainRecord>> records;
    private final Placer placer;
    private final Map<Block, Watched> watched = new HashMap<>();
    private final Map<DefaultTrainRecord, Confidence> beforeAmbiguity = new HashMap<>();
    private final Map<Block, List<Candidate>> ambiguous = new HashMap<>();
    private final PropertyChangeListener blockManagerListener = this::blockManagerChanged;
    private final PropertyChangeListener instanceManagerListener = this::instanceManagerChanged;
    private BlockManager blockManager = null;
    private boolean sensorsWatched = false;

    /**
     * Places records from Block values; done by the manager, which can find
     * and make records.
     */
    public interface Placer {

        /**
         * A Block's value was set while it was occupied, or the Block went
         * occupied with a value. Place the record it identifies there if
         * that record is not placed, making a record for a roster entry or
         * ID tag value that has none.
         *
         * @param block the Block
         * @param value its value
         */
        void valueSet(@Nonnull Block block, @Nonnull Object value);

        /**
         * @param value a Block value
         * @return the record it identifies, or null
         */
        @CheckForNull
        TrainRecord recordFor(@CheckForNull Object value);
    }

    /**
     * @param records the records, read each time they are needed
     * @param placer  places records from Block values
     */
    public TrainRecordWatcher(@Nonnull Supplier<List<DefaultTrainRecord>> records, @Nonnull Placer placer) {
        this.records = Objects.requireNonNull(records, "records");
        this.placer = Objects.requireNonNull(placer, "placer");
    }

    /**
     * Start listening to the Blocks' values, and to their sensors while any
     * record is placed. The Blocks are those of the default BlockManager,
     * and of any BlockManager later made the default.
     */
    @InvokeOnLayoutThread
    public void start() {
        InstanceManager.addPropertyChangeListener(InstanceManager.getDefaultsPropertyName(BlockManager.class),
                instanceManagerListener);
        useBlockManager(InstanceManager.getDefault(BlockManager.class));
        placementChanged();
    }

    /**
     * Stop listening to everything.
     */
    @InvokeOnLayoutThread
    public void dispose() {
        InstanceManager.removePropertyChangeListener(InstanceManager.getDefaultsPropertyName(BlockManager.class),
                instanceManagerListener);
        useBlockManager(null);
        ambiguous.clear();
        beforeAmbiguity.clear();
    }

    private void instanceManagerChanged(PropertyChangeEvent e) {
        onLayoutThread(() -> useBlockManager(InstanceManager.containsDefault(BlockManager.class)
                ? InstanceManager.getDefault(BlockManager.class) : null));
    }

    /**
     * Follow the Blocks of a BlockManager, leaving those of any other.
     */
    private void useBlockManager(@CheckForNull BlockManager bm) {
        if (bm == blockManager) {
            return;
        }
        if (blockManager != null) {
            blockManager.removePropertyChangeListener(jmri.Manager.PROPERTY_BEANS, blockManagerListener);
        }
        for (Block b : new ArrayList<>(watched.keySet())) {
            unwatch(b);
        }
        blockManager = bm;
        if (bm != null) {
            bm.addPropertyChangeListener(jmri.Manager.PROPERTY_BEANS, blockManagerListener);
            for (Block b : bm.getNamedBeanSet()) {
                watch(b);
            }
        }
    }

    /**
     * A record was placed, unplaced or removed: listen to the sensors while
     * any record is placed, and forget anything pending for a record no
     * longer placed.
     */
    @InvokeOnLayoutThread
    public void placementChanged() {
        boolean any = false;
        for (DefaultTrainRecord r : records.get()) {
            if (r.isPlaced() && !r.isDisposed()) {
                any = true;
                break;
            }
        }
        if (any != sensorsWatched) {
            sensorsWatched = any;
            for (Watched w : watched.values()) {
                w.setSensorWatched(any);
            }
        }
        ambiguous.values().forEach(list -> list.removeIf(c -> !c.record.isPlaced() || c.record.isDisposed()));
        ambiguous.values().removeIf(List::isEmpty);
        beforeAmbiguity.keySet().removeIf(r -> !r.isPlaced() || r.isDisposed());
    }

    private void blockManagerChanged(PropertyChangeEvent e) {
        onLayoutThread(() -> {
            if (e.getOldValue() instanceof Block) {
                unwatch((Block) e.getOldValue());
            }
            if (e.getNewValue() instanceof Block) {
                watch((Block) e.getNewValue());
            }
        });
    }

    private void watch(Block b) {
        if (!watched.containsKey(b)) {
            Watched w = new Watched(b);
            watched.put(b, w);
            w.setSensorWatched(sensorsWatched);
        }
    }

    private void unwatch(Block b) {
        Watched w = watched.remove(b);
        if (w != null) {
            w.dispose();
        }
        ambiguous.remove(b);
    }

    private static void onLayoutThread(Runnable r) {
        if (ThreadingUtil.isLayoutThread()) {
            r.run();
        } else {
            ThreadingUtil.runOnLayoutEventually(r::run);
        }
    }

    /**
     * A Block went occupied.
     */
    private void occupied(Block block, long timeNanos, Instant time) {
        ambiguous.remove(block);
        List<Candidate> candidates = new ArrayList<>();
        for (DefaultTrainRecord r : records.get()) {
            if (!r.isPlaced() || r.isDisposed()) {
                continue;
            }
            List<Block> blocks = r.getBlocks();
            if (blocks.contains(block)) {
                if (r.getConfidence() == Confidence.ESTIMATED) {
                    r.setConfidence(Confidence.CONFIRMED, reason(Reason.Rule.SEEN_AGAIN, block, time));
                }
                return;
            }
            Block first = blocks.get(0);
            Block last = blocks.get(blocks.size() - 1);
            List<Block> fromFirst = joinedBySetPath(first, block) ? List.of() : null;
            List<Block> fromLast = last != first && joinedBySetPath(last, block) ? List.of() : null;
            if (fromFirst != null || fromLast != null) {
                candidates.add(new Candidate(r, fromFirst, fromLast, timeNanos, time));
            }
        }
        if (candidates.isEmpty()) {
            candidates = throughDarkBlocks(block, timeNanos, time);
        }
        if (candidates.size() == 1 && candidates.get(0).end() != null) {
            enter(block, candidates.get(0), false);
        } else if (!candidates.isEmpty()) {
            // one train alone here is next to both of its ends, and which leads isn't known
            Reason.Rule rule = candidates.size() == 1 ? Reason.Rule.ENTERED_END_UNKNOWN : Reason.Rule.AMBIGUOUS;
            for (Candidate c : candidates) {
                if (!beforeAmbiguity.containsKey(c.record)) {
                    beforeAmbiguity.put(c.record, c.record.getConfidence());
                }
                c.record.setConfidence(Confidence.AMBIGUOUS, reason(rule, block, time));
            }
            ambiguous.put(block, candidates);
            settleByValue(block, block.getValue());
        }
    }

    /**
     * The trains that could have reached a Block through a run of Blocks with
     * no detector, from one of their ends, along paths that are set.
     *
     * @param block     the Block that went occupied
     * @param timeNanos when
     * @param time      when, as a wall-clock time
     * @return the candidates, each with the dark Blocks it would have passed
     *         through from each end it could have left
     */
    private List<Candidate> throughDarkBlocks(Block block, long timeNanos, Instant time) {
        List<Candidate> candidates = new ArrayList<>();
        for (DefaultTrainRecord r : records.get()) {
            if (!r.isPlaced() || r.isDisposed()) {
                continue;
            }
            List<Block> blocks = r.getBlocks();
            Block first = blocks.get(0);
            Block last = blocks.get(blocks.size() - 1);
            List<Block> fromFirst = darkRoute(first, block, blocks);
            List<Block> fromLast = last != first ? darkRoute(last, block, blocks) : null;
            if (fromFirst != null || fromLast != null) {
                candidates.add(new Candidate(r, fromFirst, fromLast, timeNanos, time));
            }
        }
        return candidates;
    }

    /**
     * The one shortest run of Blocks with no detector from one Block to
     * another, each joined to the next along a path that is set.
     *
     * @param from    the Block at a train's end
     * @param to      the Block entered
     * @param exclude the train's own Blocks, which the run can't pass through
     * @return the dark Blocks in order from {@code from}, or null if there is
     *         no such run, or more than one shortest
     */
    @CheckForNull
    static List<Block> darkRoute(@Nonnull Block from, @Nonnull Block to, @Nonnull List<Block> exclude) {
        // breadth first through dark Blocks, counting the shortest ways to each
        Map<Block, Block> cameFrom = new HashMap<>();
        Map<Block, Integer> ways = new HashMap<>();
        Map<Block, Integer> depth = new HashMap<>();
        List<Block> frontier = new ArrayList<>();
        for (Block d : darkNeighbours(from, exclude)) {
            cameFrom.put(d, null);
            ways.put(d, 1);
            depth.put(d, 1);
            frontier.add(d);
        }
        List<Block> reaching = new ArrayList<>();
        int level = 1;
        while (!frontier.isEmpty() && reaching.isEmpty()) {
            for (Block d : frontier) {
                if (joinedBySetPath(d, to)) {
                    reaching.add(d);
                }
            }
            if (!reaching.isEmpty()) {
                break;
            }
            List<Block> next = new ArrayList<>();
            for (Block d : frontier) {
                for (Block n : darkNeighbours(d, exclude)) {
                    Integer seen = depth.get(n);
                    if (seen == null) {
                        depth.put(n, level + 1);
                        cameFrom.put(n, d);
                        ways.put(n, ways.get(d));
                        next.add(n);
                    } else if (seen == level + 1) {
                        ways.put(n, ways.get(n) + ways.get(d));
                    }
                }
            }
            frontier = next;
            level++;
        }
        if (reaching.size() != 1 || ways.get(reaching.get(0)) != 1) {
            return null; // none, or several ways: which dark Blocks it is in isn't known
        }
        List<Block> route = new ArrayList<>();
        for (Block d = reaching.get(0); d != null; d = cameFrom.get(d)) {
            route.add(0, d);
        }
        return route;
    }

    /**
     * @param b       a Block
     * @param exclude Blocks to leave out
     * @return the Blocks with no detector joined to it along a path that is
     *         set
     */
    private static List<Block> darkNeighbours(Block b, List<Block> exclude) {
        List<Block> found = new ArrayList<>();
        for (Path p : b.getPaths()) {
            Block n = p.getBlock();
            if (n != null && n != b && n.getSensor() == null && !exclude.contains(n) && !found.contains(n)
                    && joinedBySetPath(b, n)) {
                found.add(n);
            }
        }
        return found;
    }

    private void enter(Block block, Candidate c, boolean settled) {
        enter(block, c, Objects.requireNonNull(c.end(), "end"), settled);
    }

    private void enter(Block block, Candidate c, boolean atFirst, boolean settled) {
        c.record.entered(c.through(atFirst), block, atFirst, c.timeNanos, c.time, settled);
        beforeAmbiguity.remove(c.record);
        leaveEmptyFarEnd(c.record);
    }

    private static Reason reason(Reason.Rule rule, Block block, Instant time) {
        return new Reason(rule, block, null, block.getSensor(), List.of(), time);
    }

    /**
     * After a train has entered a Block at one end, a Block at the other end
     * that is already empty has been left: it emptied while it was the
     * train's only Block, before the train was seen in the next, as with a
     * short train or a gap in its pick-up at the boundary.
     *
     * @param r the record of the train that entered
     */
    private static void leaveEmptyFarEnd(DefaultTrainRecord r) {
        while (true) {
            List<Block> blocks = r.getBlocks();
            if (blocks.size() < 2) {
                return;
            }
            Block far = r.getLeadingEnd() == TrainRecord.End.REAR ? blocks.get(0) : blocks.get(blocks.size() - 1);
            if (far.getSensor() == null || far.getState() != Block.UNOCCUPIED) {
                return;
            }
            r.left(far, Reason.Rule.LEFT);
        }
    }

    /**
     * The Block's value names one of the trains that could have entered it:
     * that train entered it. If it could have entered at either of its ends
     * and its leading end isn't known, the others are cleared and it waits
     * until it leaves one end, which shows it entered at the other.
     */
    private void settleByValue(Block block, Object value) {
        List<Candidate> candidates = ambiguous.get(block);
        if (candidates == null || value == null) {
            return;
        }
        TrainRecord named = placer.recordFor(value);
        Candidate chosen = null;
        for (Candidate c : candidates) {
            if (c.record == named) {
                chosen = c;
                break;
            }
        }
        if (chosen == null) {
            return;
        }
        if (chosen.end() == null) {
            if (candidates.size() > 1) {
                candidates.remove(chosen);
                restore(block, candidates);
                ambiguous.put(block, new ArrayList<>(List.of(chosen)));
                chosen.record.setConfidence(Confidence.AMBIGUOUS,
                        reason(Reason.Rule.ENTERED_END_UNKNOWN, block, chosen.time));
            }
            return;
        }
        ambiguous.remove(block);
        candidates.remove(chosen);
        restore(block, candidates);
        enter(block, chosen, true);
    }

    /**
     * Put back the confidence each of these records had before it became
     * ambiguous, unless it is still ambiguous at another Block.
     */
    private void restore(Block block, List<Candidate> candidates) {
        for (Candidate c : candidates) {
            if (!isAmbiguousElsewhere(block, c.record)) {
                Confidence before = beforeAmbiguity.remove(c.record);
                if (before != null) {
                    c.record.setConfidence(before, reason(Reason.Rule.AMBIGUITY_CLEARED, block, Instant.now()));
                }
            }
        }
    }

    private boolean isAmbiguousElsewhere(Block block, DefaultTrainRecord r) {
        return ambiguousElsewhere(block, r) != null;
    }

    /**
     * @param block a Block
     * @param r     a train
     * @return another Block the train could have entered, with its entry
     *         there, or null if there is none
     */
    @CheckForNull
    private Map.Entry<Block, Candidate> ambiguousElsewhere(Block block, DefaultTrainRecord r) {
        for (Map.Entry<Block, List<Candidate>> e : ambiguous.entrySet()) {
            if (e.getKey() == block) {
                continue;
            }
            for (Candidate c : e.getValue()) {
                if (c.record == r) {
                    return Map.entry(e.getKey(), c);
                }
            }
        }
        return null;
    }

    /**
     * A Block went unoccupied.
     */
    private void unoccupied(Block block) {
        List<Candidate> pending = ambiguous.remove(block);
        if (pending != null) {
            restore(block, pending);
        }
        for (DefaultTrainRecord r : records.get()) {
            if (!r.isPlaced() || r.isDisposed()) {
                continue;
            }
            List<Block> blocks = r.getBlocks();
            int i = blocks.indexOf(block);
            if (i < 0) {
                continue;
            }
            boolean atEnd = (i == 0 || i == blocks.size() - 1);
            if (blocks.size() > 1 && atEnd) {
                enterAtOtherEnd(r, i == 0);
                r.left(block, Reason.Rule.LEFT);
            } else if (blocks.size() > 1 && onlyDarkBehind(r, blocks, i)) {
                // the rear has left this Block, so it has left the dark Blocks behind it too
                List<Block> behind = r.getLeadingEnd() == TrainRecord.End.REAR
                        ? new ArrayList<>(blocks.subList(0, i + 1)) : new ArrayList<>(blocks.subList(i, blocks.size()));
                if (r.getLeadingEnd() != TrainRecord.End.REAR) {
                    Collections.reverse(behind);
                }
                for (Block b : behind) {
                    r.left(b, b == block ? Reason.Rule.LEFT : Reason.Rule.LEFT_DARK_BEHIND);
                }
            } else if (r.getConfidence() == Confidence.CONFIRMED
                    && (blocks.size() > 1 || darkNeighbours(block, blocks).isEmpty())) {
                // it can't have left; from its only Block it may have gone on into a dark Block, and is just not seen
                r.setConfidence(Confidence.ESTIMATED, reason(Reason.Rule.KEPT_AFTER_DROPOUT, block, Instant.now()));
            }
            if (r.getConfidence() == Confidence.CONFIRMED && !anyDetectedOccupied(r)) {
                // not seen anywhere now
                r.setConfidence(Confidence.ESTIMATED, reason(Reason.Rule.NOT_SEEN, block, Instant.now()));
            }
        }
    }

    /**
     * @param r      a moving train's record
     * @param blocks its Blocks
     * @param i      the index of one of them, not at an end
     * @return true if every Block between it and the end that doesn't lead
     *         has no detector
     */
    private static boolean onlyDarkBehind(DefaultTrainRecord r, List<Block> blocks, int i) {
        TrainRecord.End leading = r.getLeadingEnd();
        if (leading == null) {
            return false;
        }
        List<Block> behind = leading == TrainRecord.End.REAR ? blocks.subList(0, i) : blocks.subList(i + 1, blocks.size());
        for (Block b : behind) {
            if (b.getSensor() != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param r a record
     * @return true if one of its Blocks with a detector shows it occupied
     */
    private static boolean anyDetectedOccupied(DefaultTrainRecord r) {
        for (Block b : r.getBlocks()) {
            if (b.getSensor() != null && b.getState() == Block.OCCUPIED) {
                return true;
            }
        }
        return false;
    }

    /**
     * A train has left the Block at one of its ends. If it alone could have
     * entered a Block next to both its ends, it entered that Block at the
     * other end, the one it moved towards.
     *
     * @param r         the train
     * @param leftFirst true if it left the first of its Blocks
     */
    private void enterAtOtherEnd(DefaultTrainRecord r, boolean leftFirst) {
        for (Map.Entry<Block, List<Candidate>> e : new ArrayList<>(ambiguous.entrySet())) {
            List<Candidate> list = e.getValue();
            if (list.size() == 1 && list.get(0).record == r && list.get(0).atFirst && list.get(0).atLast) {
                ambiguous.remove(e.getKey());
                Confidence before = beforeAmbiguity.get(r);
                enter(e.getKey(), list.get(0), !leftFirst, false);
                Map.Entry<Block, Candidate> still = ambiguousElsewhere(e.getKey(), r);
                if (still != null) {
                    beforeAmbiguity.put(r, before);
                    r.setConfidence(Confidence.AMBIGUOUS,
                            reason(Reason.Rule.AMBIGUOUS, still.getKey(), still.getValue().time));
                }
                return;
            }
        }
    }

    /**
     * @param from a Block
     * @param to   another
     * @return true if a path from one to the other is set, either way
     */
    static boolean joinedBySetPath(@Nonnull Block from, @Nonnull Block to) {
        for (Path p : from.getPaths()) {
            if (p.getBlock() == to && p.checkPathSet()) {
                return true;
            }
        }
        for (Path p : to.getPaths()) {
            if (p.getBlock() == from && p.checkPathSet()) {
                return true;
            }
        }
        return false;
    }

    /**
     * A train that could have entered a Block, at one or both of its ends.
     */
    private static final class Candidate {

        final DefaultTrainRecord record;
        final boolean atFirst;
        final boolean atLast;
        private final List<Block> fromFirst;
        private final List<Block> fromLast;
        final long timeNanos;
        final Instant time;

        /**
         * @param record    the train
         * @param fromFirst the dark Blocks between the first of its Blocks
         *                  and the Block entered (empty if next to it), or
         *                  null if it can't have entered from there
         * @param fromLast  the same from the last of its Blocks
         * @param timeNanos when
         * @param time      when, as a wall-clock time
         */
        Candidate(DefaultTrainRecord record, @CheckForNull List<Block> fromFirst, @CheckForNull List<Block> fromLast,
                long timeNanos, Instant time) {
            this.record = record;
            this.atFirst = fromFirst != null;
            this.atLast = fromLast != null;
            this.fromFirst = fromFirst;
            this.fromLast = fromLast;
            this.timeNanos = timeNanos;
            this.time = time;
        }

        /**
         * @return true if entered at the first of the train's Blocks, false
         *         if at the last, null if it is next to both and the train's
         *         leading end isn't known
         */
        @CheckForNull
        Boolean end() {
            if (atFirst != atLast) {
                return atFirst;
            }
            TrainRecord.End leading = record.isFrontKnown() ? record.getLeadingEnd() : null;
            if (leading == null) {
                return null;
            }
            return leading == TrainRecord.End.FRONT;
        }

        /**
         * @param atFirst true for the first of the train's Blocks, false for
         *                the last
         * @return the dark Blocks it passed through from that end, empty if
         *         it was next to the Block entered
         */
        @Nonnull
        List<Block> through(boolean atFirst) {
            return Objects.requireNonNull(atFirst ? fromFirst : fromLast, "not entered from that end");
        }
    }

    /**
     * One Block: its value always, its sensor while any record is placed.
     */
    private final class Watched {

        private final Block block;
        private Sensor sensor = null;
        private long rawActiveNanos = 0;
        private Instant rawActiveTime = null;
        private final PropertyChangeListener blockListener = this::blockChanged;
        private final PropertyChangeListener sensorListener = this::sensorChanged;
        private final Sensor.RawStateListener rawListener = this::rawChanged;
        private boolean sensorWatched = false;

        Watched(Block block) {
            this.block = block;
            block.addPropertyChangeListener(blockListener);
        }

        void setSensorWatched(boolean on) {
            if (on == sensorWatched) {
                return;
            }
            sensorWatched = on;
            if (on) {
                attachSensor(block.getSensor());
            } else {
                attachSensor(null);
            }
        }

        private void attachSensor(Sensor s) {
            if (sensor != null) {
                sensor.removePropertyChangeListener(sensorListener);
                sensor.removeRawStateListener(rawListener);
            }
            sensor = s;
            rawActiveTime = null;
            if (sensor != null) {
                sensor.addPropertyChangeListener(sensorListener);
                sensor.addRawStateListener(rawListener);
            }
        }

        void dispose() {
            block.removePropertyChangeListener(blockListener);
            attachSensor(null);
        }

        private void blockChanged(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            if (Block.OCC_SENSOR_CHANGE.equals(name)) {
                onLayoutThread(() -> {
                    if (sensorWatched) {
                        attachSensor(block.getSensor());
                    }
                });
            } else if (Block.PROPERTY_STATE.equals(name)) {
                Object value = block.getValue();
                boolean nowOccupied = Integer.valueOf(Block.OCCUPIED).equals(e.getNewValue());
                onLayoutThread(() -> {
                    if (nowOccupied && value != null) {
                        placer.valueSet(block, value); // Block sets a value it takes before it goes occupied
                    }
                });
            } else if (Block.PROPERTY_VALUE.equals(name)) {
                Object value = e.getNewValue();
                onLayoutThread(() -> {
                    if (value != null) {
                        settleByValue(block, value);
                        if (block.getState() == Block.OCCUPIED) {
                            placer.valueSet(block, value);
                        }
                    }
                });
            }
        }

        private void rawChanged(Sensor s, int oldState, int newState, long timeNanos) {
            Instant now = Instant.now();
            onLayoutThread(() -> {
                if (s != sensor) {
                    return;
                }
                if (newState == Sensor.ACTIVE) {
                    if (rawActiveTime == null) {
                        rawActiveNanos = timeNanos;
                        rawActiveTime = now;
                    }
                } else if (s.getKnownState() != Sensor.ACTIVE) {
                    rawActiveTime = null; // a flicker the debounce hid, or the sensor going inactive
                }
            });
        }

        private void sensorChanged(PropertyChangeEvent e) {
            if (!Sensor.PROPERTY_KNOWN_STATE.equals(e.getPropertyName())) {
                return;
            }
            Object v = e.getNewValue();
            int state = (v instanceof Integer) ? (Integer) v : Sensor.UNKNOWN;
            long nowNanos = System.nanoTime();
            Instant now = Instant.now();
            onLayoutThread(() -> {
                if (e.getSource() != sensor) {
                    return;
                }
                if (state == Sensor.ACTIVE) {
                    long t = (rawActiveTime != null) ? rawActiveNanos : nowNanos;
                    Instant wall = (rawActiveTime != null) ? rawActiveTime : now;
                    occupied(block, t, wall);
                } else if (state == Sensor.INACTIVE) {
                    rawActiveTime = null;
                    unoccupied(block);
                }
            });
        }
    }
}
