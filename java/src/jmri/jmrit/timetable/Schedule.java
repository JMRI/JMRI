package jmri.jmrit.timetable;

/**
 * Define the content of a Schedule record.
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class Schedule {

    /**
     * Create a new schedule with default values.
     * @param layoutId The parent layout id.
     * @throws IllegalArgumentException SCHEDULE_ADD_FAIL
     */
    public Schedule(int layoutId) throws IllegalArgumentException {
        if (_dm.getLayout(layoutId) == null) {
            throw new IllegalArgumentException(_dm.SCHEDULE_ADD_FAIL);
        }
        _scheduleId = _dm.getNextId("Schedule");  // NOI18N
        _layoutId = layoutId;
        _dm.addSchedule(_scheduleId, this);
    }

    public Schedule(int scheduleId, int layoutId, String scheduleName, String effDate, int startHour, int duration) {
        _scheduleId = scheduleId;
        _layoutId = layoutId;
        setScheduleName(scheduleName);
        setEffDate(effDate);
        setStartHour(startHour);
        setDuration(duration);
    }

    TimeTableDataManager _dm = TimeTableDataManager.getDataManager();

    private int _scheduleId = 0;
    private int _layoutId = 0;
    private String _scheduleName = "New Schedule";  // NOI18N
    private String _effDate = "Today";  // NOI18N
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

    /**
     * Set the start hour, 0 - 23.
     * @param newStartHour The start hour in the range of 0 to 23.
     * @throws IllegalArgumentException (START_HOUR_RANGE).
     */
    public void setStartHour(int newStartHour) throws IllegalArgumentException {
        if (newStartHour < 0 || newStartHour > 23) {
            throw new IllegalArgumentException(_dm.START_HOUR_RANGE);
        }
        int oldStartHour = _startHour;
        _startHour = newStartHour;

        try {
            _dm.calculateScheduleTrains(getScheduleId(), false);
            _dm.calculateScheduleTrains(getScheduleId(), true);
        } catch (IllegalArgumentException ex) {
            _startHour = oldStartHour;  // Roll back start hour change
            throw ex;
        }
    }

    public int getDuration() {
        return _duration;
    }

    /**
     * Set the duration, 1 - 24 hours.
     * @param newDuration The duration in the range of 1 to 24.
     * @throws IllegalArgumentException (DURATION_RANGE).
     */
    public void setDuration(int newDuration) throws IllegalArgumentException {
        if (newDuration < 1 || newDuration > 24) {
            throw new IllegalArgumentException(_dm.DURATION_RANGE);
        }
        int oldDuration = _duration;
        _duration = newDuration;

        try {
            _dm.calculateScheduleTrains(getScheduleId(), false);
            _dm.calculateScheduleTrains(getScheduleId(), true);
        } catch (IllegalArgumentException ex) {
            _duration = oldDuration;  // Roll back duration change
            throw ex;
        }
    }

    @Override
    public String toString() {
        return _scheduleName;
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Schedule.class);
}
