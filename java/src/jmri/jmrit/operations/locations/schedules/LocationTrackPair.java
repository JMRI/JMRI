package jmri.jmrit.operations.locations.schedules;

import jmri.jmrit.operations.locations.Track;

/**
 * An object displaying a location and track. Used for combo boxes.
 *
 * @author Daniel Boudreau Copyright (C) 2009
 *
 */
public class LocationTrackPair {

    Track _track;

    public LocationTrackPair(Track track) {
        _track = track;
    }

    // for combo boxes
    @Override
    public String toString() {
        return _track.getLocation().getName() + " (" + _track.getName() + ")";
    }

    public Track getTrack() {
        return _track;
    }
}
