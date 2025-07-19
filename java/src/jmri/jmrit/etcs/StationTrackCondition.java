package jmri.jmrit.etcs;

import org.apiguardian.api.API;

/**
 * Class to represent a TrackCondition which is a station stop.
 * @author Steve Young Copyright (C) 2024
 */
@API(status=API.Status.EXPERIMENTAL)
public class StationTrackCondition extends TrackCondition {

    /**
     * Create a new Station Track Condition.
     * @param distance distance from start of Movement Authority.
     * @param name the Station name.
     */
    public StationTrackCondition(int distance, String name) {
        super(distance, false, "", "ATO_21", "", "", "Station: " + name, "");
    }

}
