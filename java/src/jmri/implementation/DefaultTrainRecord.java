package jmri.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.BasicRosterEntry;
import jmri.IdTag;
import jmri.InvokeOnLayoutThread;
import jmri.NamedBean;
import jmri.TrainRecord;
import jmri.beans.PropertyChangeSupport;

/**
 * The default {@link TrainRecord}. Records are made by a
 * {@link jmri.TrainRecordManager}, which is told of every change of name,
 * roster entry and ID tag before it is made, so it can keep each unique.
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
    }

    private final Registry registry;
    private String name;
    private BasicRosterEntry rosterEntry;
    private final Set<IdTag> idTags = new LinkedHashSet<>();
    private List<Unit> units = Collections.singletonList(new Unit(null, null));
    private Detection detection = Detection.ENTIRE_TRAIN;
    private List<DetectionMarker> customMarkers = null;
    private final Map<LengthSource, Double> lengths = new EnumMap<>(LengthSource.class);
    private boolean measuredConsistent = false;
    private boolean removed = false;

    /**
     * Make a record: one unit with no length, detected as Entire Train.
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
        checkNotRemoved();
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
    public BasicRosterEntry getRosterEntry() {
        return rosterEntry;
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void setRosterEntry(@CheckForNull BasicRosterEntry entry) {
        checkNotRemoved();
        if (entry == rosterEntry) {
            return;
        }
        if (entry != null) {
            registry.checkRosterEntry(this, entry);
        }
        BasicRosterEntry old = rosterEntry;
        rosterEntry = entry;
        firePropertyChange(PROPERTY_ROSTER_ENTRY, old, entry);
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
        checkNotRemoved();
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
        checkNotRemoved();
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
    public List<Unit> getUnits() {
        return new ArrayList<>(units);
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void setUnits(@Nonnull List<Unit> units) {
        Objects.requireNonNull(units, "units");
        checkNotRemoved();
        if (units.isEmpty()) {
            throw new IllegalArgumentException("a train has at least one unit");
        }
        for (Unit u : units) {
            Objects.requireNonNull(u, "unit");
        }
        if (units.equals(this.units)) {
            return;
        }
        Double oldUnitsLength = getLength(LengthSource.UNITS);
        List<Unit> old = getUnits();
        this.units = Collections.unmodifiableList(new ArrayList<>(units));
        firePropertyChange(PROPERTY_UNITS, old, getUnits());
        if (!Objects.equals(oldUnitsLength, getLength(LengthSource.UNITS))) {
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
        checkNotRemoved();
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
        checkNotRemoved();
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
        if (source == LengthSource.UNITS) {
            double sum = 0;
            for (Unit u : units) {
                Double l = u.getLength();
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
        checkNotRemoved();
        if (source == LengthSource.UNITS) {
            throw new IllegalArgumentException("the units' length comes from the units");
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
        checkNotRemoved();
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
    public boolean isRemoved() {
        return removed;
    }

    /**
     * Mark the record removed, so it can't be changed. Called by its manager.
     */
    @InvokeOnLayoutThread
    public void markRemoved() {
        removed = true;
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

    private void checkNotRemoved() {
        if (removed) {
            throw new IllegalStateException("train record " + name + " has been removed");
        }
    }
}
