package jmri.implementation;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.BasicRosterEntry;
import jmri.Block;
import jmri.IdTag;
import jmri.InvokeOnLayoutThread;
import jmri.LocoAddress;
import jmri.NamedBean;
import jmri.TrainRecord;
import jmri.beans.PropertyChangeSupport;

/**
 * The default {@link TrainRecord}. Records are made by a
 * {@link jmri.TrainRecordManager}, which is told of every change of name,
 * roster entries and ID tags before it is made, so it can keep each unique.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public class DefaultTrainRecord extends PropertyChangeSupport implements TrainRecord {

    /**
     * What a record's manager is told before a change that must be unique
     * among the records. Each method throws IllegalArgumentException to
     * refuse the change.
     */
    public interface Registry {

        /**
         * @param record the record being renamed
         * @param name   its new name, already normalised
         * @throws IllegalArgumentException if another record has the name
         */
        void checkName(@Nonnull TrainRecord record, @Nonnull String name);

        /**
         * @param record the record being given a roster entry
         * @param entry  the roster entry
         * @throws IllegalArgumentException if another record has the entry
         */
        void checkRosterEntry(@Nonnull TrainRecord record, @Nonnull BasicRosterEntry entry);

        /**
         * @param record the record being given an ID tag
         * @param tag    the tag
         * @throws IllegalArgumentException if another record has the tag
         */
        void checkIdTag(@Nonnull TrainRecord record, @Nonnull IdTag tag);

        /**
         * Told after the record is placed or unplaced.
         *
         * @param record the record
         */
        void placementChanged(@Nonnull TrainRecord record);

        /**
         * Told after the record's roster entries, and so its loco addresses,
         * change.
         *
         * @param record the record
         */
        default void rosterEntriesChanged(@Nonnull TrainRecord record) {
        }
    }

    private final Registry registry;
    private String name;
    private List<BasicRosterEntry> rosterEntries = Collections.emptyList();
    private final Set<IdTag> idTags = new LinkedHashSet<>();
    private List<TrainComponent> components = Collections.singletonList(new TrainComponent(null, null));
    private Detection detection = Detection.ENTIRE_TRAIN;
    private List<DetectionMarker> customMarkers = null;
    private final Map<LengthSource, Double> lengths = new EnumMap<>(LengthSource.class);
    private boolean measuredConsistent = false;
    private boolean disposed = false;
    private final List<Block> blocks = new ArrayList<>();
    private boolean frontKnown = false;
    private End leadingEnd = null;
    private final Deque<TrailEntry> trail = new ArrayDeque<>();
    private Confidence confidence = null;
    private final Deque<SpeedCommand> commands = new ArrayDeque<>();

    /**
     * Make a record: one component with no length, no locos, detected as
     * Entire Train.
     *
     * @param name     the train's name, already normalised and checked
     * @param registry the manager to tell of changes that must be unique
     */
    public DefaultTrainRecord(@Nonnull String name, @Nonnull Registry registry) {
        this.name = Objects.requireNonNull(name, "name");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    /**
     * Normalise a train name as for a user name.
     *
     * @param name the name given
     * @return the name to use
     * @throws IllegalArgumentException if it is empty
     */
    @Nonnull
    public static String normalizeName(@Nonnull String name) {
        String n = NamedBean.normalizeUserName(Objects.requireNonNull(name, "name"));
        if (n == null || n.isEmpty()) {
            throw new IllegalArgumentException("a train record needs a name");
        }
        return n;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void setName(@Nonnull String name) {
        checkNotDisposed();
        String n = normalizeName(name);
        if (n.equals(this.name)) {
            return;
        }
        registry.checkName(this, n);
        String old = this.name;
        this.name = n;
        firePropertyChange(PROPERTY_NAME, old, n);
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public BasicRosterEntry getFirstRosterEntry() {
        return rosterEntries.isEmpty() ? null : rosterEntries.get(0);
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void insertFirstRosterEntry(@Nonnull BasicRosterEntry entry) {
        Objects.requireNonNull(entry, "entry");
        checkNotDisposed();
        List<BasicRosterEntry> list = new ArrayList<>(rosterEntries);
        list.add(0, entry);
        setRosterEntries(list); // refuses an entry this record or another already has
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void removeFirstRosterEntry() {
        checkNotDisposed();
        if (rosterEntries.isEmpty()) {
            return;
        }
        setRosterEntries(rosterEntries.subList(1, rosterEntries.size()));
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<BasicRosterEntry> getRosterEntries() {
        return new ArrayList<>(rosterEntries);
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void setRosterEntries(@Nonnull List<BasicRosterEntry> entries) {
        Objects.requireNonNull(entries, "entries");
        checkNotDisposed();
        List<BasicRosterEntry> list = new ArrayList<>(entries.size());
        for (BasicRosterEntry e : entries) {
            Objects.requireNonNull(e, "entry");
            if (indexOf(list, e) >= 0) {
                throw new IllegalArgumentException("roster entry " + e.getId() + " is listed twice");
            }
            list.add(e);
        }
        if (list.equals(rosterEntries)) {
            return;
        }
        for (BasicRosterEntry e : list) {
            if (indexOf(rosterEntries, e) < 0) {
                registry.checkRosterEntry(this, e);
            }
        }
        List<BasicRosterEntry> old = getRosterEntries();
        rosterEntries = Collections.unmodifiableList(list);
        registry.rosterEntriesChanged(this);
        firePropertyChange(PROPERTY_ROSTER_ENTRIES, old, getRosterEntries());
    }

    /**
     * @param list  roster entries
     * @param entry a roster entry
     * @return where the entry, or one with its roster ID, is in the list, or
     *         -1
     */
    private static int indexOf(List<BasicRosterEntry> list, BasicRosterEntry entry) {
        String id = entry.getId();
        for (int i = 0; i < list.size(); i++) {
            BasicRosterEntry e = list.get(i);
            if (e == entry || (id != null && id.equals(e.getId()))) {
                return i;
            }
        }
        return -1;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Set<IdTag> getIdTags() {
        return new LinkedHashSet<>(idTags);
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void addIdTag(@Nonnull IdTag tag) {
        Objects.requireNonNull(tag, "tag");
        checkNotDisposed();
        if (idTags.contains(tag)) {
            return;
        }
        registry.checkIdTag(this, tag);
        Set<IdTag> old = getIdTags();
        idTags.add(tag);
        firePropertyChange(PROPERTY_ID_TAGS, old, getIdTags());
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void removeIdTag(@Nonnull IdTag tag) {
        Objects.requireNonNull(tag, "tag");
        checkNotDisposed();
        if (!idTags.contains(tag)) {
            return;
        }
        Set<IdTag> old = getIdTags();
        idTags.remove(tag);
        firePropertyChange(PROPERTY_ID_TAGS, old, getIdTags());
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<TrainComponent> getTrainComponents() {
        return new ArrayList<>(components);
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void setTrainComponents(@Nonnull List<TrainComponent> components) {
        Objects.requireNonNull(components, "components");
        checkNotDisposed();
        if (components.isEmpty()) {
            throw new IllegalArgumentException("a train has at least one component");
        }
        for (TrainComponent c : components) {
            Objects.requireNonNull(c, "component");
        }
        if (components.equals(this.components)) {
            return;
        }
        Double oldComponentsLength = getLength(LengthSource.TRAIN_COMPONENTS);
        List<TrainComponent> old = getTrainComponents();
        this.components = Collections.unmodifiableList(new ArrayList<>(components));
        firePropertyChange(PROPERTY_TRAIN_COMPONENTS, old, getTrainComponents());
        if (!Objects.equals(oldComponentsLength, getLength(LengthSource.TRAIN_COMPONENTS))) {
            firePropertyChange(PROPERTY_LENGTHS, null, null);
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Detection getDetection() {
        return detection;
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void setDetection(@Nonnull Detection detection) {
        Objects.requireNonNull(detection, "detection");
        checkNotDisposed();
        if (detection == Detection.CUSTOM) {
            throw new IllegalArgumentException("set a custom list of markers with setCustomMarkers");
        }
        if (detection == this.detection) {
            return;
        }
        List<DetectionMarker> old = getMarkers();
        this.detection = detection;
        customMarkers = null;
        firePropertyChange(PROPERTY_DETECTION, old, getMarkers());
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void setCustomMarkers(@Nonnull List<DetectionMarker> markers) {
        Objects.requireNonNull(markers, "markers");
        checkNotDisposed();
        if (markers.isEmpty()) {
            throw new IllegalArgumentException("a custom list has at least one marker");
        }
        for (DetectionMarker m : markers) {
            Objects.requireNonNull(m, "marker");
            if (m.getPlacement() == DetectionMarker.Placement.WHOLE_TRAIN) {
                throw new IllegalArgumentException("a whole-train marker is only for Entire Train");
            }
        }
        List<DetectionMarker> list = Collections.unmodifiableList(new ArrayList<>(markers));
        if (detection == Detection.CUSTOM && list.equals(customMarkers)) {
            return;
        }
        List<DetectionMarker> old = getMarkers();
        detection = Detection.CUSTOM;
        customMarkers = list;
        firePropertyChange(PROPERTY_DETECTION, old, getMarkers());
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<DetectionMarker> getMarkers() {
        if (detection == Detection.CUSTOM) {
            return customMarkers;
        }
        return DetectionMarker.forDetection(detection);
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public Double getLength(@Nonnull LengthSource source) {
        Objects.requireNonNull(source, "source");
        if (source == LengthSource.TRAIN_COMPONENTS) {
            double sum = 0;
            for (TrainComponent c : components) {
                Double l = c.getLength();
                if (l == null) {
                    return null;
                }
                sum += l;
            }
            return sum;
        }
        return lengths.get(source);
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void setLength(@Nonnull LengthSource source, @CheckForNull Double mm) {
        Objects.requireNonNull(source, "source");
        checkNotDisposed();
        if (source == LengthSource.TRAIN_COMPONENTS) {
            throw new IllegalArgumentException("the components' length comes from the components");
        }
        if (source == LengthSource.MEASURED) {
            throw new IllegalArgumentException("set a measured length with setMeasuredLength");
        }
        checkLength(mm);
        if (Objects.equals(mm, lengths.get(source))) {
            return;
        }
        putLength(source, mm);
        firePropertyChange(PROPERTY_LENGTHS, null, null);
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void setMeasuredLength(@CheckForNull Double mm, boolean consistent) {
        checkNotDisposed();
        checkLength(mm);
        boolean c = mm != null && consistent;
        if (Objects.equals(mm, lengths.get(LengthSource.MEASURED)) && c == measuredConsistent) {
            return;
        }
        putLength(LengthSource.MEASURED, mm);
        measuredConsistent = c;
        firePropertyChange(PROPERTY_LENGTHS, null, null);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isMeasuredLengthConsistent() {
        return measuredConsistent;
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public Double getHoldingLength() {
        Double longest = null;
        for (LengthSource s : LengthSource.values()) {
            Double l = getLength(s);
            if (l != null && (longest == null || l > longest)) {
                longest = l;
            }
        }
        return longest;
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public Double getPlacingLength() {
        if (measuredConsistent) {
            return lengths.get(LengthSource.MEASURED);
        }
        return getHoldingLength();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDisposed() {
        return disposed;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPlaced() {
        return !blocks.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void place(@Nonnull List<Block> blocks, boolean frontKnown) {
        Objects.requireNonNull(blocks, "blocks");
        checkNotDisposed();
        if (blocks.isEmpty()) {
            throw new IllegalArgumentException("a train is placed in at least one Block");
        }
        if (new HashSet<>(blocks).size() != blocks.size()) {
            throw new IllegalArgumentException("a Block is given twice");
        }
        for (Block b : blocks) {
            Objects.requireNonNull(b, "block");
        }
        List<Block> old = getBlocks();
        this.blocks.clear();
        this.blocks.addAll(blocks);
        long now = System.nanoTime();
        Instant time = Instant.now();
        trail.clear();
        for (int i = blocks.size() - 1; i >= 0; i--) {
            addToTrail(new TrailEntry(blocks.get(i), null, null, now, time));
        }
        setDirection(frontKnown, null);
        setConfidence(Confidence.CONFIRMED);
        firePropertyChange(PROPERTY_BLOCKS, old, getBlocks());
        registry.placementChanged(this);
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void unplace() {
        checkNotDisposed();
        if (blocks.isEmpty()) {
            return;
        }
        List<Block> old = getBlocks();
        blocks.clear();
        setDirection(false, null);
        setConfidence(null);
        firePropertyChange(PROPERTY_BLOCKS, old, getBlocks());
        registry.placementChanged(this);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<Block> getBlocks() {
        return new ArrayList<>(blocks);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFrontKnown() {
        return frontKnown;
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public End getLeadingEnd() {
        return leadingEnd;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<TrailEntry> getTrail() {
        return new ArrayList<>(trail);
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public Confidence getConfidence() {
        return confidence;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<LocoAddress> getLocoAddresses() {
        List<LocoAddress> addresses = new ArrayList<>();
        for (BasicRosterEntry e : rosterEntries) {
            LocoAddress a = e.getDccLocoAddress();
            if (a != null) {
                addresses.add(a);
            }
        }
        return addresses;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<SpeedCommand> getCommands() {
        return new ArrayList<>(commands);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<SpeedCommand> getCommands(@Nonnull LocoAddress address) {
        List<SpeedCommand> list = new ArrayList<>();
        for (SpeedCommand c : commands) {
            if (c.isFor(address)) {
                list.add(c);
            }
        }
        return list;
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public SpeedCommand getLastCommand() {
        return commands.peekLast();
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public SpeedCommand getLastCommand(@Nonnull LocoAddress address) {
        for (Iterator<SpeedCommand> i = commands.descendingIterator(); i.hasNext();) {
            SpeedCommand c = i.next();
            if (c.isFor(address)) {
                return c;
            }
        }
        return null;
    }

    /**
     * A speed command, or a release, for one of the train's locos. A command
     * repeating the speed and direction of the last for the same loco is not
     * kept, unless that was a release. Called by the
     * {@link TrainRecordSpeedWatcher}.
     *
     * @param command the command
     */
    @InvokeOnLayoutThread
    void commanded(@Nonnull SpeedCommand command) {
        if (removed) {
            return;
        }
        SpeedCommand last = getLastCommand(command.getAddress());
        if (last != null && !last.isRelease() && !command.isRelease() && command.sameSpeedAs(last)) {
            return;
        }
        if (last != null && last.isRelease() && command.isRelease()) {
            return;
        }
        commands.addLast(command);
        while (commands.size() > COMMANDS_LENGTH) {
            commands.removeFirst();
        }
        firePropertyChange(PROPERTY_COMMAND, last, command);
    }

    /**
     * The train has entered a Block at one end of the Blocks it is in. That
     * end leads; if the front wasn't known, it is now: the end that led.
     * Called by the {@link TrainRecordWatcher}.
     *
     * @param block     the Block entered
     * @param atFirst   true if entered at the first of {@link #getBlocks()},
     *                  false if at the last
     * @param timeNanos when, on the {@link System#nanoTime()} scale
     * @param time      when, as a wall-clock time
     */
    @InvokeOnLayoutThread
    void entered(@Nonnull Block block, boolean atFirst, long timeNanos, @Nonnull Instant time) {
        if (disposed || blocks.isEmpty() || blocks.contains(block)) {
            return;
        }
        List<Block> old = getBlocks();
        Block from = atFirst ? blocks.get(0) : blocks.get(blocks.size() - 1);
        if (!frontKnown && !atFirst) {
            Collections.reverse(blocks);
            atFirst = true;
        }
        End end;
        if (atFirst) {
            blocks.add(0, block);
            end = End.FRONT;
        } else {
            blocks.add(block);
            end = End.REAR;
        }
        TrailEntry entry = new TrailEntry(block, from, end, timeNanos, time);
        addToTrail(entry);
        setDirection(true, end);
        setConfidence(Confidence.CONFIRMED);
        firePropertyChange(PROPERTY_BLOCKS, old, getBlocks());
        firePropertyChange(PROPERTY_TRAIL, null, entry);
    }

    /**
     * The train has left a Block at one end of the Blocks it is in, while it
     * is still in others. Called by the {@link TrainRecordWatcher}.
     *
     * @param block the Block left
     */
    @InvokeOnLayoutThread
    void left(@Nonnull Block block) {
        if (disposed || blocks.size() < 2) {
            return;
        }
        int i = blocks.indexOf(block);
        if (i != 0 && i != blocks.size() - 1) {
            return;
        }
        List<Block> old = getBlocks();
        blocks.remove(i);
        firePropertyChange(PROPERTY_BLOCKS, old, getBlocks());
    }

    /**
     * Set how sure JMRI is of where the train is. Called by the
     * {@link TrainRecordWatcher}, and by placing.
     *
     * @param c the confidence, or null when not placed
     */
    @InvokeOnLayoutThread
    void setConfidence(@CheckForNull Confidence c) {
        if (c == confidence) {
            return;
        }
        Confidence old = confidence;
        confidence = c;
        firePropertyChange(PROPERTY_CONFIDENCE, old, c);
    }

    private void setDirection(boolean frontKnown, End leading) {
        if (frontKnown == this.frontKnown && leading == leadingEnd) {
            return;
        }
        this.frontKnown = frontKnown;
        this.leadingEnd = leading;
        firePropertyChange(PROPERTY_DIRECTION, null, null);
    }

    private void addToTrail(TrailEntry e) {
        trail.addLast(e);
        while (trail.size() > TRAIL_LENGTH) {
            trail.removeFirst();
        }
    }

    /**
     * Dispose of the record, so it can't be changed. Called by its manager
     * when the record is removed.
     */
    @InvokeOnLayoutThread
    public void dispose() {
        disposed = true;
    }

    @Override
    public String toString() {
        return name;
    }

    private void putLength(LengthSource source, Double mm) {
        if (mm == null) {
            lengths.remove(source);
        } else {
            lengths.put(source, mm);
        }
    }

    private static void checkLength(Double mm) {
        if (mm != null && !(mm > 0)) {
            throw new IllegalArgumentException("a length must be more than 0: " + mm);
        }
    }

    private void checkNotDisposed() {
        if (disposed) {
            throw new IllegalStateException("train record " + name + " has been disposed");
        }
    }
}
