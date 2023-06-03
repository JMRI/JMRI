package jmri.jmrit.timetable;

/**
 * Define the content of a Stop record.
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class Stop {

    /**
     * Create a new stop with default values.
     * @param trainId The parent train id.
     * @param seq The next stop sequence number.
     * @throws IllegalArgumentException STOP_ADD_FAIL
     */
    public Stop(int trainId, int seq) {
        if (_dm.getTrain(trainId) == null) {
            throw new IllegalArgumentException(TimeTableDataManager.STOP_ADD_FAIL);
        }
        _stopId = _dm.getNextId("Stop");  // NOI18N
        _trainId = trainId;
        _seq = seq;
        _dm.addStop(_stopId, this);
    }

    public Stop(int stopId, int trainId, int stationId, int seq, int duration,
            int nextSpeed, int arriveTime, int departTime, int stagingTrack, String stopNotes) {
        _stopId = stopId;
        _trainId = trainId;
        _stationId = stationId;
        _seq = seq;
        setDuration(duration);
        setNextSpeed(nextSpeed);
        setArriveTime(arriveTime);
        setDepartTime(departTime);
        setStagingTrack(stagingTrack);
        setStopNotes(stopNotes);
    }

    TimeTableDataManager _dm = TimeTableDataManager.getDataManager();

    private final int _stopId;
    private final int _trainId;
    private int _stationId = 0;
    private int _seq = 0;
    private int _duration = 0;
    private int _nextSpeed = 0;
    private int _arriveTime = 0;
    private int _departTime = 0;
    private int _stagingTrack = 0;
    private String _stopNotes = "";

    /**
     * Make a copy of the stop.
     * @param trainId The new train id, if zero use the current train id.
     * @param stationId The new station id.  If zero use the current station id.
     * @param seq The sequence for the new stop.
     * @return a new Stop instance.
     */
    public Stop getCopy(int trainId, int stationId, int seq) {
        if (trainId == 0) trainId = getTrainId();
        if (stationId == 0) stationId = getStationId();

        Stop copy = new Stop(trainId, seq);
        copy.setStationId(stationId);
        copy.setDuration(_duration);
        copy.setNextSpeed(_nextSpeed);
        copy.setArriveTime(_arriveTime);
        copy.setDepartTime(_departTime);
        copy.setStagingTrack(_stagingTrack);
        copy.setStopNotes(_stopNotes);
        return copy;
    }

    public int getStopId() {
        return _stopId;
    }

    public int getTrainId() {
        return _trainId;
    }

    public int getStationId() {
        return _stationId;
    }

    public void setStationId(int newStationId) {
        int oldDStationId = _stationId;
        _stationId = newStationId;

        try {
            _dm.calculateTrain(_trainId, false);
            _dm.calculateTrain(_trainId, true);
        } catch (IllegalArgumentException ex) {
            _stationId = oldDStationId;  // Roll back station change
            throw ex;
        }
    }

    public int getSeq() {
        return _seq;
    }

    public void setSeq(int newSeq) {
        _seq = newSeq;
    }

    public int getDuration() {
        return _duration;
    }

    public void setDuration(int newDuration) {
        if (newDuration < 0) {
            throw new IllegalArgumentException(TimeTableDataManager.STOP_DURATION_LT_0);
        }
        int oldDuration = _duration;
        _duration = newDuration;

        try {
            _dm.calculateTrain(_trainId, false);
            _dm.calculateTrain(_trainId, true);
        } catch (IllegalArgumentException ex) {
            _duration = oldDuration;  // Roll back duration change
            throw ex;
        }
    }

    public int getNextSpeed() {
        return _nextSpeed;
    }

    public void setNextSpeed(int newNextSpeed) {
        if (newNextSpeed < 0) {
            throw new IllegalArgumentException(TimeTableDataManager.NEXT_SPEED_LT_0);
        }
        int oldNextSpeed = _nextSpeed;
        _nextSpeed = newNextSpeed;

        try {
            _dm.calculateTrain(_trainId, false);
            _dm.calculateTrain(_trainId, true);
        } catch (IllegalArgumentException ex) {
            _nextSpeed = oldNextSpeed;  // Roll back next speed change
            throw ex;
        }
    }

    public int getArriveTime() {
        return _arriveTime;
    }

    public void setArriveTime(int newArriveTime) {
        _arriveTime = newArriveTime;
    }

    public int getDepartTime() {
        return _departTime;
    }

    public void setDepartTime(int newDepartTime) {
        _departTime = newDepartTime;
    }

    public int getStagingTrack() {
        return _stagingTrack;
    }

    public void setStagingTrack(int newStagingTrack) {
        Station station = _dm.getStation(_stationId);
        if (newStagingTrack < 0 || newStagingTrack > station.getStaging()) {
            throw new IllegalArgumentException(TimeTableDataManager.STAGING_RANGE);
        }

        _stagingTrack = newStagingTrack;
    }

    public String getStopNotes() {
        return _stopNotes;
    }

    public void setStopNotes(String newNotes) {
        _stopNotes = newNotes;
    }

    @Override
    public String toString() {
        TimeTableDataManager dataMgr = TimeTableDataManager.getDataManager();
        Station station = dataMgr.getStation(_stationId);
        return _seq + " :: " + station.getStationName();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Stop.class);
}
