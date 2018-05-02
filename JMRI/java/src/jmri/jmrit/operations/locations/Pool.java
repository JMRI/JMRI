package jmri.jmrit.operations.locations;

import java.util.ArrayList;
import java.util.List;
import jmri.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a pool of tracks that share their length.
 *
 * @author Daniel Boudreau Copyright (C) 2011
 * @author Gregory Madsen Copyright (C) 2012
 *
 */
public class Pool extends Bean {

    public static final String LISTCHANGE_CHANGED_PROPERTY = "poolListChange"; // NOI18N
    public static final String DISPOSE = "poolDispose"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(Pool.class);

    // stores tracks for this pool
    protected List<Track> _tracks = new ArrayList<Track>();

    protected String _id = "";

    public String getId() {
        return _id;
    }

    protected String _name = "";

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        String old = _name;
        _name = name;
        this.propertyChangeSupport.firePropertyChange("Name", old, name);
    }

    /**
     * The number of tracks in this pool.
     *
     * @return the number of tracks in this pool.
     */
    public int getSize() {
        return _tracks.size();
    }

    // for combo boxes
    @Override
    public String toString() {
        return _name;
    }

    public Pool(String id, String name) {
        log.debug("New pool ({}) id: {}", name, id);
        _name = name;
        _id = id;
    }

    public void dispose() {
        this.propertyChangeSupport.firePropertyChange(DISPOSE, null, DISPOSE);
    }

    /**
     * Adds a track to this pool
     *
     * @param track to be added.
     */
    public void add(Track track) {
        if (!_tracks.contains(track)) {

            int oldSize = _tracks.size();
            _tracks.add(track);

            this.propertyChangeSupport.firePropertyChange(LISTCHANGE_CHANGED_PROPERTY, Integer.valueOf(oldSize), Integer.valueOf(_tracks.size()));
        }
    }

    /**
     * Removes a track from this pool
     *
     * @param track to be removed.
     */
    public void remove(Track track) {
        if (_tracks.contains(track)) {

            int oldSize = _tracks.size();
            _tracks.remove(track);

            this.propertyChangeSupport.firePropertyChange(LISTCHANGE_CHANGED_PROPERTY, Integer.valueOf(oldSize), Integer.valueOf(_tracks.size()));
        }
    }

    public List<Track> getTracks() {
        // Return a copy to protect the internal list
        return new ArrayList<Track>(_tracks);
    }

    public int getTotalLengthTracks() {
        int total = 0;
        for (Track track : getTracks()) {
            total += track.getLength();
        }
        return total;
    }

    /**
     * Request track length from one of the other tracks in this pool. Other
     * tracks in the same pool may have their length shortened or lengthened by
     * this operation.
     *
     * @param track  the track requesting additional length
     * @param length the length of rolling stock
     * @return true if successful
     */
    public boolean requestTrackLength(Track track, int length) {
        // only request enough length for the rolling stock to fit
        int additionalLength = track.getUsedLength() + track.getReserved() + length - track.getLength();

        for (Track t : getTracks()) {
            // note that the reserved track length can be either positive or negative
            if (t != track) {
                if (t.getUsedLength() + t.getReserved() + additionalLength <= t.getLength()
                        && t.getLength() - additionalLength >= t.getMinimumLength()) {
                    log.debug("Pool ({}) increasing track ({}) length ({}) decreasing ({})", getName(),
                            track.getName(), additionalLength, t.getName()); // NOI18N
                    t.setLength(t.getLength() - additionalLength);
                    track.setLength(track.getLength() + additionalLength);
                    return true;
                } else {
                    // steal whatever isn't being used by this track
                    int available = t.getLength() - (t.getUsedLength() + t.getReserved());
                    int min = t.getLength() - t.getMinimumLength();
                    if (min < available) {
                        available = min;
                    }
                    if (available > 0) {
                        // adjust track lengths and reduce the additional length needed
                        log.debug("Pool ({}) incremental increase for track ({}) length ({}) decreasing ({})",
                                getName(), track.getName(), available, t.getName()); // NOI18N
                        t.setLength(t.getLength() - available);
                        track.setLength(track.getLength() + available);
                        additionalLength = additionalLength - available;
                    }
                }
            }
        }
        return false;
    }
}
