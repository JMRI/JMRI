package jmri.jmrit.timetable;

/**
 * Define the content of a Station record.
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class Station {

    /**
     * Create a new station with default values.
     * @param segmentId The parent segment id.
     * @throws IllegalArgumentException STATION_ADD_FAIL
     */
    public Station(int segmentId) {
        if (_dm.getSegment(segmentId) == null) {
            throw new IllegalArgumentException(TimeTableDataManager.STATION_ADD_FAIL);
        }
        _stationId = _dm.getNextId("Station");  // NOI18N
        _segmentId = segmentId;
        _dm.addStation(_stationId, this);
    }

    public Station(int stationId, int segmentId, String stationName, double distance, boolean doubleTrack, int sidings, int staging) {
        _stationId = stationId;
        _segmentId = segmentId;
        setStationName(stationName);
        setDistance(distance);
        setDoubleTrack(doubleTrack);
        setSidings(sidings);
        setStaging(staging);
    }

    TimeTableDataManager _dm = TimeTableDataManager.getDataManager();

    private final int _stationId;
    private final int _segmentId;
    private String _stationName = Bundle.getMessage("NewStationName");  // NOI18N
    private double _distance = 1.0;
    private boolean _doubleTrack = false;
    private int _sidings = 0;
    private int _staging = 0;

    /**
     * Make a copy of the station.
     * @param segmentId The new segmentId, if zero use the current segment id.
     * @return a new station instance.
     */
    public Station getCopy(int segmentId) {
        if (segmentId == 0) segmentId = getSegmentId();
        Station copy = new Station(segmentId);
        copy.setStationName(Bundle.getMessage("DuplicateCopyName", _stationName));
        copy.setDistance(_distance);
        copy.setDoubleTrack(_doubleTrack);
        copy.setSidings(_sidings);
        copy.setStaging(_staging);
        return copy;
    }

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
     * Set a new distance.
     * @param newDistance The value to be used.
     * @throws IllegalArgumentException (DISTANCE_LT_0) if the value is less than 0.0.
     */
    public void setDistance(double newDistance) {
        if (newDistance < 0) {
            throw new IllegalArgumentException(TimeTableDataManager.DISTANCE_LT_0);
        }
        double oldDistance = _distance;
        _distance = newDistance;

        try {
            int layoutId = _dm.getSegment(getSegmentId()).getLayoutId();
            _dm.calculateLayoutTrains(layoutId, false);
            _dm.calculateLayoutTrains(layoutId, true);
        } catch (IllegalArgumentException ex) {
            _distance = oldDistance;  // Roll back distance change
            throw ex;
        }
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

    /**
     * Set a new siding count.
     * @param newSidings The value to be used.
     * @throws IllegalArgumentException (SIDINGS_LT_0) if the value is less than 0.
     */
    public void setSidings(int newSidings) {
        if (newSidings < 0) {
            throw new IllegalArgumentException(TimeTableDataManager.SIDINGS_LT_0);
        }
        _sidings = newSidings;
    }

    public int getStaging() {
        return _staging;
    }

    /**
     * Set a new staging track count.
     * @param newStaging The value to be used.
     * @throws IllegalArgumentException (STAGING_LT_0, STAGING_IN_USE) if the value is
     * less than 0 or a staging track is referenced by a train stop.
     */
    public void setStaging(int newStaging) {
        if (newStaging < 0) {
            throw new IllegalArgumentException(TimeTableDataManager.STAGING_LT_0);
        }
        for (Stop stop : _dm.getStops(0, getStationId(), false)) {
            if (stop.getStagingTrack() > newStaging) {
                throw new IllegalArgumentException(TimeTableDataManager.STAGING_IN_USE);
            }
        }
        _staging = newStaging;
    }

    @Override
    public String toString() {
        return _stationName;
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Station.class);
}
