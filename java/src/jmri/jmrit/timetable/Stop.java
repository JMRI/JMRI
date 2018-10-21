package jmri.jmrit.timetable;

import jmri.jmrit.timetable.swing.*;

/**
 * Define the content of a Stop record.
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class Stop {

    public Stop(int stopId, int trainId, int seq) {
        _stopId = stopId;
        _trainId = trainId;
        _seq = seq;
    }

    public Stop(int stopId, int trainId, int stationId, int seq, int duration,
            int nextSpeed, int arriveTime, int departTime, int stagingTrack, String stopNotes) {
        _stopId = stopId;
        _trainId = trainId;
        _stationId = stationId;
        _seq = seq;
        _duration = duration;
        _nextSpeed = nextSpeed;
        _arriveTime = arriveTime;
        _departTime = departTime;
        _stagingTrack = stagingTrack;
        _stopNotes = stopNotes;
    }

    private int _stopId = 0;
    private int _trainId = 0;
    private int _stationId = 0;
    private int _seq = 0;
    private int _duration = 0;
    private int _nextSpeed = 0;
    private int _arriveTime = 0;
    private int _departTime = 0;
    private int _stagingTrack = 0;
    private String _stopNotes = "";

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
        _stationId = newStationId;
    }

    public int getSeq() {
        return _seq;
    }

    public void setSeq(int newSeq) {
        _seq = newSeq;
    }

    public String getSeqSort() {
        return String.format("%03d", _seq);  // NOI18N
    }

    public int getDuration() {
        return _duration;
    }

    public void setDuration(int newDuration) {
        _duration = newDuration;
    }

    public int getNextSpeed() {
        return _nextSpeed;
    }

    public void setNextSpeed(int newNextSpeed) {
        _nextSpeed = newNextSpeed;
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
        _stagingTrack = newStagingTrack;
    }

    public String getStopNotes() {
        return _stopNotes;
    }

    public void setStopNotes(String newNotes) {
        _stopNotes = newNotes;
    }

    public String toString() {
        TimeTableDataManager dataMgr = jmri.InstanceManager.getDefault(TimeTableFrame.class).getDataManager();
        Station station = dataMgr.getStation(_stationId);
        return _seq + " :: " + station.getStationName();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Stop.class);
}
