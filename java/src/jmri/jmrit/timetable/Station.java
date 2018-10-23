package jmri.jmrit.timetable;

/**
 * Define the content of a Station record.
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class Station {

    public Station(int stationId, int segmentId) {
        _stationId = stationId;
        _segmentId = segmentId;
    }

    public Station(int stationId, int segmentId, String stationName, double distance, boolean doubleTrack, int sidings, int staging) {
        _stationId = stationId;
        _segmentId = segmentId;
        _stationName = stationName;
        _distance = distance;
        _doubleTrack = doubleTrack;
        _sidings = sidings;
        _staging = staging;
    }

    private int _stationId = 0;
    private int _segmentId = 0;
    private String _stationName = "";
    private double _distance = 1.0;
    private boolean _doubleTrack = false;
    private int _sidings = 0;
    private int _staging = 0;

    public int getStationId() {
        return _stationId;
    }

    public int getSegmentId() {
        return _segmentId;
    }

    public String getStationName() {
        return _stationName;
    }

    public void setStationName(String newName) {
        _stationName = newName;
    }

    public double getDistance() {
        return _distance;
    }

    /**
     * Create a zero padded string for sorting by distance.
     * @return a zero padded 6 character string
     */
    public String getDistanceString() {
        return String.format("%06d", (int) Math.round(_distance * 10));  // NOI18N
    }

    public void setDistance(double newDistance) {
        _distance = newDistance;
    }

    public boolean getDoubleTrack() {
        return _doubleTrack;
    }

    public void setDoubleTrack(boolean newDoubleTrack) {
        _doubleTrack = newDoubleTrack;
    }

    public int getSidings() {
        return _sidings;
    }

    public void setSidings(int newSidings) {
        _sidings = newSidings;
    }

    public int getStaging() {
        return _staging;
    }

    public void setStaging(int newStaging) {
        _staging = newStaging;
    }

    public String toString() {
        return _stationName;
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Station.class);
}
