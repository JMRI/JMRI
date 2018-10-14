package jmri.jmrit.timetable;

/**
 * Define the content of a Schedule record.
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class Schedule {

    public Schedule(int scheduleId, int layoutId) {
        _scheduleId = scheduleId;
        _layoutId = layoutId;
    }

    public Schedule(int scheduleId, int layoutId, String scheduleName, String effDate, int startHour, int duration) {
        _scheduleId = scheduleId;
        _layoutId = layoutId;
        _scheduleName = scheduleName;
        _effDate = effDate;
        _startHour = startHour;
        _duration = duration;
    }

    private int _scheduleId = 0;
    private int _layoutId = 0;
    private String _scheduleName = "";
    private String _effDate = "";
    private int _startHour = 0;
    private int _duration = 24;

    public int getScheduleId() {
        return _scheduleId;
    }

    public int getLayoutId() {
        return _layoutId;
    }

    public String getScheduleName() {
        return _scheduleName;
    }

    public void setScheduleName(String newName) {
        _scheduleName = newName;
    }

    public String getEffDate() {
        return _effDate;
    }

    public void setEffDate(String newDate) {
        _effDate = newDate;
    }

    public int getStartHour() {
        return _startHour;
    }

    public void setStartHour(int newStartHour) {
        _startHour = newStartHour;
    }

    public int getDuration() {
        return _duration;
    }

    public void setDuration(int newDuration) {
        _duration = newDuration;
    }

    public String toString() {
        return _scheduleName;
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Schedule.class);
}
