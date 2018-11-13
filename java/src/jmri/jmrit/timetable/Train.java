package jmri.jmrit.timetable;

/**
 * Define the content of a Train record.
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class Train {

    public Train(int trainId, int scheduleId) {
        _trainId = trainId;
        _scheduleId = scheduleId;
    }

    public Train(int trainId, int scheduleId, int typeId, String trainName, String trainDesc,
                int defaultSpeed, int startTime, int throttle, int routeDuration, String trainNotes) {
        _trainId = trainId;
        _scheduleId = scheduleId;
        _typeId = typeId;
        _trainName = trainName;
        _trainDesc = trainDesc;
        _defaultSpeed = defaultSpeed;
        _startTime = startTime;
        _throttle = throttle;
        _routeDuration = routeDuration;
        _trainNotes = trainNotes;
    }

    private int _trainId = 0;
    private int _scheduleId = 0;
    private int _typeId = 0;
    private String _trainName = "";
    private String _trainDesc = "";
    private int _defaultSpeed = 1;
    private int _startTime = 0;
    private int _throttle = 0;
    private int _routeDuration = 0;
    private String _trainNotes = "";

    public int getTrainId() {
        return _trainId;
    }

    public int getScheduleId() {
        return _scheduleId;
    }

    public int getTypeId() {
        return _typeId;
    }

    public void setTypeId(int newType) {
        _typeId = newType;
    }

    public String getTrainName() {
        return _trainName;
    }

    public void setTrainName(String newName) {
        _trainName = newName;
    }

    public String getTrainDesc() {
        return _trainDesc;
    }

    public void setTrainDesc(String newDesc) {
        _trainDesc = newDesc;
    }

    public int getDefaultSpeed() {
        return _defaultSpeed;
    }

    public void setDefaultSpeed(int newSpeed) {
        _defaultSpeed = newSpeed;
    }

    public int getStartTime() {
        return _startTime;
    }

    public void setStartTime(int newStartTime) {
        _startTime = newStartTime;
    }

    public int getThrottle() {
        return _throttle;
    }

    public void setThrottle(int newThrottle) {
        _throttle = newThrottle;
    }

    public int getRouteDuration() {
        return _routeDuration;
    }

    public void setRouteDuration(int newRouteDuration) {
        _routeDuration = newRouteDuration;
    }

    public String getTrainNotes() {
        return _trainNotes;
    }

    public void setTrainNotes(String newNotes) {
        _trainNotes = newNotes;
    }

    public String toString() {
        return _trainName;
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Train.class);
}
