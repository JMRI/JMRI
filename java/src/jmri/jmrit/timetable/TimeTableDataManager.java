package jmri.jmrit.timetable;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.TreeMap;

/**
 * Provide data base management services.
 * <p>
 * The data structure was migrated from a MySQL database.  As such, it contains
 * <strong>tables</strong> implemented as TreeMaps and <strong>records</strong>
 * implemented as Classes.  The logical relationships are handled using foreign keys.
 *
 * <pre>
 * Data Structure:
 *   Layout -- Global data.
 *     TrainTypes -- Assigned to trains for diagram colors.
 *     Segments -- Used for division / sub-division arrangements.
 *       Stations -- Any place a train can stop.
 *     Schedules -- Basic information about a schedule.
 *       Trains -- Train characteristics.
 *         Stops -- A junction between a train and a station that contains arrival and departure times.
 * </pre>
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableDataManager {

    /**
     * Create a TimeTableDataManager instance.
     * @param loadData False to create an empty instance, otherwise load the data
     */
    public TimeTableDataManager(boolean loadData) {
        jmri.InstanceManager.setDefault(TimeTableDataManager.class, this);
        if (loadData) {
            _lockCalculate = true;
            if (!jmri.jmrit.timetable.configurexml.TimeTableXml.doLoad()) {
                log.error("Unabled to load the time table data");  // NOI18N
            }
            _lockCalculate = false;
        }
    }

    /**
     * Use the InstanceManager to only allow a single data manager instance.
     * @return the current or new data manager.
     */
    public static TimeTableDataManager getDataManager() {
        TimeTableDataManager dm = jmri.InstanceManager.getNullableDefault(TimeTableDataManager.class);
        if (dm != null) {
            return dm;
        }
        return new TimeTableDataManager(true);
    }

    // Exception key words
    public final String CLOCK_LT_1 = "FastClockLt1";    // NOI18N
    public final String DURATION_LT_0 = "DurationLt0";    // NOI18N
    public final String THROTTLES_LT_0 = "ThrottlesLt0";    // NOI18N
    public final String THROTTLES_IN_USE = "ThrottlesInUse";    // NOI18N
    public final String SCALE_NF = "ScaleNotFound";    // NOI18N
    public final String TIME_OUT_OF_RANGE = "TimeOutOfRange";    // NOI18N
    public final String SEGMENT_CHANGE_ERROR = "SegmentChangeError";    // NOI18N
    public final String DISTANCE_LT_0 = "DistanceLt0";    // NOI18N
    public final String SIDINGS_LT_0 = "SidingsLt0";    // NOI18N
    public final String STAGING_LT_0 = "StagingLt0";    // NOI18N
    public final String STAGING_IN_USE = "StagingInUse";    // NOI18N
    public final String START_HOUR_RANGE = "StartHourRange";    // NOI18N
    public final String DURATION_RANGE = "DurationRange";    // NOI18N
    public final String DEFAULT_SPEED_LT_0 = "DefaultSpeedLt0";    // NOI18N
    public final String START_TIME_FORMAT = "StartTimeFormat";    // NOI18N
    public final String START_TIME_RANGE = "StartTimeRange";    // NOI18N
    public final String THROTTLE_RANGE = "ThrottleRange";    // NOI18N
    public final String STAGING_RANGE = "StagingRange";    // NOI18N
    public final String STOP_DURATION_LT_0 = "StopDurationLt0";    // NOI18N
    public final String NEXT_SPEED_LT_0 = "NextSpeedLt0";    // NOI18N
    public final String LAYOUT_HAS_CHILDREN = "LayoutHasChildren";    // NOI18N
    public final String TYPE_HAS_REFERENCE = "TypeHasReference";    // NOI18N
    public final String SEGMENT_HAS_CHILDREN = "SegmentHaSChildren";    // NOI18N
    public final String STATION_HAS_REFERENCE = "StationHasReference";    // NOI18N
    public final String SCHEDULE_HAS_CHILDREN = "ScheduleHasChildren";    // NOI18N
    public final String TRAIN_HAS_CHILDREN = "TrainHasChildren";    // NOI18N
    public final String TYPE_ADD_FAIL = "TypeAddFail";    // NOI18N
    public final String SEGMENT_ADD_FAIL = "SegmentAddFail";    // NOI18N
    public final String STATION_ADD_FAIL = "StationAddFail";    // NOI18N
    public final String SCHEDULE_ADD_FAIL = "ScheduleAddFail";    // NOI18N
    public final String TRAIN_ADD_FAIL = "TrainAddFail";    // NOI18N
    public final String STOP_ADD_FAIL = "StopAddFail";    // NOI18N

    private TreeMap<Integer, Layout> _layoutMap = new TreeMap<>();
    private TreeMap<Integer, TrainType> _trainTypeMap = new TreeMap<>();
    private TreeMap<Integer, Segment> _segmentMap = new TreeMap<>();
    private TreeMap<Integer, Station> _stationMap = new TreeMap<>();
    private TreeMap<Integer, Schedule> _scheduleMap = new TreeMap<>();
    private TreeMap<Integer, Train> _trainMap = new TreeMap<>();
    private TreeMap<Integer, Stop> _stopMap = new TreeMap<>();

    private List<SegmentStation> _segmentStations = new ArrayList<>();

    boolean _lockCalculate = false;
    public void setLockCalculate(boolean lock) {
        _lockCalculate = lock;
    }

    // ------------ Map maintenance methods ------------ //

    public void addLayout(int id, Layout newLayout) {
        _layoutMap.put(id, newLayout);
    }

    public void addTrainType(int id, TrainType newTrainType) {
        _trainTypeMap.put(id, newTrainType);
    }

    public void addSegment(int id, Segment newSegment) {
        _segmentMap.put(id, newSegment);
    }

    /**
     * Add a new station
     * Create a SegmentStation instance.
     * Add it to the SegmentStation list.
     */
    public void addStation(int id, Station newStation) {
        _stationMap.put(id, newStation);
        SegmentStation segmentStation = new SegmentStation(newStation.getSegmentId(), id);
        if (!_segmentStations.contains(segmentStation)) {
            _segmentStations.add(segmentStation);
        }
    }

    public void addSchedule(int id, Schedule newSchedule) {
        _scheduleMap.put(id, newSchedule);
    }

    public void addTrain(int id, Train newTrain) {
        _trainMap.put(id, newTrain);
    }

    public void addStop(int id, Stop newStop) {
        _stopMap.put(id, newStop);
    }

    /**
     * Delete the layout if there are no train types, segments or schedules.
     * param id The layout id.
     * @throws IllegalArgumentException LAYOUT_HAS_CHILDREN
     */
    public void deleteLayout(int id) throws IllegalArgumentException {
        if (getTrainTypes(id, false).size() > 0
                || getSegments(id, false).size() > 0
                || getSchedules(id, false).size() > 0) {
            throw new IllegalArgumentException(LAYOUT_HAS_CHILDREN);
        }
        _layoutMap.remove(id);
    }

    /**
     * Delete the train type if there are no train references.
     * param id The train type id.
     * @throws IllegalArgumentException TYPE_HAS_REFERENCE
     */
    public void deleteTrainType(int id) throws IllegalArgumentException {
        if (getTrains(0, id, false).size() > 0) {
            throw new IllegalArgumentException(TYPE_HAS_REFERENCE);
        }
        _trainTypeMap.remove(id);
    }

    /**
     * Delete the segment if it has no stations.
     * param id The segment id.
     * @throws IllegalArgumentException SEGMENT_HAS_CHILDREN
     */
    public void deleteSegment(int id) throws IllegalArgumentException {
        if (getStations(id, false).size() > 0) {
            throw new IllegalArgumentException(SEGMENT_HAS_CHILDREN);
        }
        _segmentMap.remove(id);
    }

    /**
     * Delete the station if there are no stop references.
     * param id The station id.
     * @throws IllegalArgumentException STATION_HAS_REFERENCE
     */
    public void deleteStation(int id) throws IllegalArgumentException {
        if (getStops(0, id, false).size() > 0) {
            throw new IllegalArgumentException(STATION_HAS_REFERENCE);
        }

        int segmentId = getStation(id).getSegmentId();
        List<SegmentStation> list = new ArrayList<>();
        for (SegmentStation segmentStation : _segmentStations) {
            if (segmentStation.getStationId() == id && segmentStation.getSegmentId() == segmentId) {
                list.add(segmentStation);
            }
        }
        for (SegmentStation ss : list) {
            _segmentStations.remove(ss);
        }

        _stationMap.remove(id);
    }

    /**
     * Delete the schedule if it has no trains.
     * param id The schedule id.
     * @throws IllegalArgumentException SCHEDULE_HAS_CHILDREN
     */
    public void deleteSchedule(int id) throws IllegalArgumentException {
        if (getTrains(id, 0, false).size() > 0) {
            throw new IllegalArgumentException(SCHEDULE_HAS_CHILDREN);
        }
        _scheduleMap.remove(id);
    }

    /**
     * Delete the train if it has no stops.
     * param id The train id.
     * @throws IllegalArgumentException TRAIN_HAS_CHILDREN
     */
    public void deleteTrain(int id) throws IllegalArgumentException {
        if (getStops(id, 0, false).size() > 0) {
            throw new IllegalArgumentException(TRAIN_HAS_CHILDREN);
        }
        _trainMap.remove(id);
    }

    /**
     * Delete the stop and update train schedule.
     * param id The stop id.
     */
    public void deleteStop(int id) {
        int trainId = getStop(id).getTrainId();
        _stopMap.remove(id);
        calculateTrain(trainId, true);
    }

    // ------------ Map access methods: get by id  ------------ //

    public Layout getLayout(int id) {
        return _layoutMap.get(id);
    }

    public TrainType getTrainType(int id) {
        return _trainTypeMap.get(id);
    }

    public Segment getSegment(int id) {
        return _segmentMap.get(id);
    }

    public Station getStation(int id) {
        return _stationMap.get(id);
    }

    public Schedule getSchedule(int id) {
        return _scheduleMap.get(id);
    }

    public Train getTrain(int id) {
        return _trainMap.get(id);
    }

    public Stop getStop(int id) {
        return _stopMap.get(id);
    }

    /**
     * Get the last key from the map and add 1.
     * @param type The record type which is used to select the appropriate map.
     * @return the next id, or 0 if there is an error.
     */
    public int getNextId(String type) {
        int nextId = 0;
        switch (type) {
            case "Layout":    // NOI18N
                nextId = (_layoutMap.isEmpty()) ? 1 : _layoutMap.lastKey() + 1;
                break;
            case "TrainType": // NOI18N
                nextId = (_trainTypeMap.isEmpty()) ? 1 : _trainTypeMap.lastKey() + 1;
                break;
            case "Segment":   // NOI18N
                nextId = (_segmentMap.isEmpty()) ? 1 : _segmentMap.lastKey() + 1;
                break;
            case "Station":   // NOI18N
                nextId = (_stationMap.isEmpty()) ? 1 : _stationMap.lastKey() + 1;
                break;
            case "Schedule":  // NOI18N
                nextId = (_scheduleMap.isEmpty()) ? 1 : _scheduleMap.lastKey() + 1;
                break;
            case "Train":     // NOI18N
                nextId = (_trainMap.isEmpty()) ? 1 : _trainMap.lastKey() + 1;
                break;
            case "Stop":      // NOI18N
                nextId = (_stopMap.isEmpty()) ? 1 : _stopMap.lastKey() + 1;
                break;
            default:
                log.error("getNextId: Invalid record type: {}", type);  // NOI18N
        }
        return nextId;
    }

    // ------------ Map access methods: get all entries or by foreign key ------------ //

    /**
     * Create a list of layouts
     * @param sort If true, sort the resulting list
     * @return a list of layouts
     */
    public ArrayList<Layout> getLayouts(boolean sort) {
        // No foreign keys
        ArrayList<Layout> list = new ArrayList<>(_layoutMap.values());
        if (sort) {
            Collections.sort(list, (o1, o2) -> o1.getLayoutName().compareTo(o2.getLayoutName()));
        }
        return list;
    }

    /**
     * Create a list of train types
     * @param fKeyLayout If non-zero, select the types that have the specified foreign key
     * @param sort If true, sort the resulting list
     * @return a list of train types
     */
    public ArrayList<TrainType> getTrainTypes(int fKeyLayout, boolean sort) {
        ArrayList<TrainType> list = new ArrayList<>();
        for (TrainType type : _trainTypeMap.values()) {
            if (fKeyLayout == 0 || fKeyLayout == type.getLayoutId()) {
                list.add(type);
            }
        }
        if (sort) {
            Collections.sort(list, (o1, o2) -> o1.getTypeName().compareTo(o2.getTypeName()));
        }
        return list;
    }

    /**
     * Create a list of segments
     * @param fKeyLayout If non-zero, select the segments that have the specified foreign key
     * @param sort If true, sort the resulting list
     * @return a list of segments
     */
    public ArrayList<Segment> getSegments(int fKeyLayout, boolean sort) {
        ArrayList<Segment> list = new ArrayList<>();
        for (Segment segment : _segmentMap.values()) {
            if (fKeyLayout == 0 || fKeyLayout == segment.getLayoutId()) {
                list.add(segment);
            }
        }
        if (sort) {
            Collections.sort(list, (o1, o2) -> o1.getSegmentName().compareTo(o2.getSegmentName()));
        }
        return list;
    }

    /**
     * Create a list of stations
     * @param fKeySegment If non-zero, select the stations that have the specified foreign key
     * @param sort If true, sort the resulting list
     * @return a list of stations
     */
    public ArrayList<Station> getStations(int fKeySegment, boolean sort) {
        ArrayList<Station> list = new ArrayList<>();
        for (Station station : _stationMap.values()) {
            if (fKeySegment == 0 || fKeySegment == station.getSegmentId()) {
                list.add(station);
            }
        }
        if (sort) {
            Collections.sort(list, (o1, o2) -> Double.compare(o1.getDistance(), o2.getDistance()));
        }
        return list;
    }

    /**
     * Create a list of schedules
     * @param fKeyLayout If non-zero, select the schedules that have the specified foreign key
     * @param sort If true, sort the resulting list
     * @return a list of schedules
     */
    public ArrayList<Schedule> getSchedules(int fKeyLayout, boolean sort) {
        ArrayList<Schedule> list = new ArrayList<>();
        for (Schedule schedule : _scheduleMap.values()) {
            if (fKeyLayout == 0 || fKeyLayout == schedule.getLayoutId()) {
                list.add(schedule);
            }
        }
        if (sort) {
            Collections.sort(list, (o1, o2) -> o1.getScheduleName().compareTo(o2.getScheduleName()));
        }
        return list;
    }

    /**
     * Create a list of trains
     * @param fKeySchedule If non-zero, select the trains that have the specified foreign key
     * @param fKeyType If non-zero, select the trains that have the specified foreign key
     * @param sort If true, sort the resulting list
     * @return a list of trains
     */
    public ArrayList<Train> getTrains(int fKeySchedule, int fKeyType, boolean sort) {
        ArrayList<Train> list = new ArrayList<>();
        for (Train train : _trainMap.values()) {
            if ((fKeySchedule == 0 && fKeyType == 0)
                    || fKeySchedule == train.getScheduleId()
                    || fKeyType == train.getTypeId()) {
                list.add(train);
            }
        }
        if (sort) {
            Collections.sort(list, (o1, o2) -> o1.getTrainName().compareTo(o2.getTrainName()));
        }
        return list;
    }

    /**
     * Create a list of stops
     * @param fKeyTrain If non-zero, select the stops that have the specified foreign key
     * @param fKeyStation If non-zero, select the stops that have the specified foreign key
     * @param sort If true, sort the resulting list
     * @return a list of stops
     */
    public ArrayList<Stop> getStops(int fKeyTrain, int fKeyStation, boolean sort) {
        ArrayList<Stop> list = new ArrayList<>();
        for (Stop stop : _stopMap.values()) {
            if ((fKeyTrain == 0 && fKeyStation == 0)
                    || fKeyTrain == stop.getTrainId()
                    || fKeyStation == stop.getStationId()) {
                list.add(stop);
            }
        }
        if (sort) {
            Collections.sort(list, (o1, o2) -> Integer.compare(o1.getSeq(), o2.getSeq()));
        }
        return list;
    }

    // ------------ Special Map access methods ------------ //

    public Layout getLayoutForStop(int stopId) {
        return getLayout(getSchedule(getTrain(getStop(stopId).getTrainId()).getScheduleId()).getLayoutId());
    }

    public List<SegmentStation> getSegmentStations(int layoutId) {
        List<SegmentStation> list = new ArrayList<>();
        for (SegmentStation segmentStation : _segmentStations) {
            if (getSegment(segmentStation.getSegmentId()).getLayoutId() == layoutId) {
                list.add(segmentStation);
            }
        }
        Collections.sort(list, (o1, o2) -> o1.toString().compareTo(o2.toString()));
        return list;
    }

    // ------------  Calculate Train Times ------------

    /**
     * Update the stops for all of the trains for this layout.
     * Invoked by updates to fast clock speed, metric, scale and station distances.
     * @param layoutId The id for the layout that has been updated.
     */
    void calculateLayoutTrains(int layoutId, boolean updateStops) {
        if (_lockCalculate) return;
        for (Schedule schedule : getSchedules(layoutId, false)) {
            calculateScheduleTrains(schedule.getScheduleId(), updateStops);
        }
    }

    /**
     * Update the stop times for all of the trains that use this schedule.
     * @param scheduleId The id for the schedule that has been updated.
     */
    void calculateScheduleTrains(int scheduleId, boolean updateStops) {
        if (_lockCalculate) return;
        for (Train train : getTrains(scheduleId, 0, false)) {
            calculateTrain(train.getTrainId(), updateStops);
        }
    }

    /**
     * Calculate the arrival and departure times for all of the stops.
     * @param trainId The id of the train to be updated.
     * @param updateStops When true, update the arrive and depart times.
     * @throws IllegalArgumentException when stop times are outside of the
     * schedule times or a segment change failed.  The TIME_OUT_OF_RANGE
     * exception message includes the stop id and train name.  The SEGMENT_CHANGE_ERROR
     * message includes the segment name and the station name.  The tilde
     * character is used as the string separator.
     */
    public void calculateTrain(int trainId, boolean updateStops) throws IllegalArgumentException {
        if (_lockCalculate) return;
        Train train = getTrain(trainId);
        Schedule schedule = getSchedule(train.getScheduleId());
        Layout layout = getLayout(schedule.getLayoutId());
        ArrayList<Stop> stops = getStops(trainId, 0, true);

        double smile = layout.getScaleMK();
        int startHH = schedule.getStartHour();
        int duration = schedule.getDuration();
        int currentTime = train.getStartTime();
        int defaultSpeed = train.getDefaultSpeed();

        int checkStart = startHH;
        int checkDuration = duration;

        String currentStationName = "";
        double currentDistance = 0.0;
        int currentSegment = 0;
        int currentSpeed = 0;
        int newArrive = 0;
        int newDepart = 0;
        int elapseTime = 0;
        boolean firstStop = true;

        for (Stop stop : stops) {
            Station station = getStation(stop.getStationId());
            Segment segment = getSegment(station.getSegmentId());
            if (firstStop) {
                newArrive = currentTime;
                currentTime += stop.getDuration();
                newDepart = currentTime;
                currentDistance = station.getDistance();
                currentSpeed = (stop.getNextSpeed() > 0) ? stop.getNextSpeed() : defaultSpeed;
                currentStationName = station.getStationName();
                currentSegment = segment.getSegmentId();

                if (validateTime(checkStart, checkDuration, newArrive) && validateTime(checkStart, checkDuration, newDepart)) {
                    if (updateStops) {
                        stop.setArriveTime(newArrive);
                        stop.setDepartTime(newDepart);
                    }
                } else {
                    throw new IllegalArgumentException(String.format("%s~%d~%s", TIME_OUT_OF_RANGE, stop.getSeq(), train.getTrainName()));  // NOI18N
                }
                firstStop = false;
                continue;
            }

            // Calculate times for remaining stops
            double wrkDistance = Math.abs(currentDistance - station.getDistance());

            // If the segment has changed, a new distance will need to be calculated.
            if (segment.getSegmentId() != currentSegment) {
                // Find the station in the current segment that has the same name
                // as the station in the previous segment.
                Station wrkStation = null;
                for (Station findStation : getStations(segment.getSegmentId(), false)) {
                    if (findStation.getStationName().equals(currentStationName)) {
                        wrkStation = findStation;
                        break;
                    }
                }
                if (wrkStation == null) {
                    throw new IllegalArgumentException(SEGMENT_CHANGE_ERROR);
                }
                wrkDistance = Math.abs(station.getDistance() - wrkStation.getDistance());
            }

            elapseTime = (int) Math.round(wrkDistance / smile / currentSpeed * 60);
            if (elapseTime < 1) {
                elapseTime = 1;
            }
            currentTime += elapseTime;
            if (currentTime > 1439)
                currentTime -= 1440;
            newArrive = currentTime;
            currentTime += stop.getDuration();
            if (currentTime > 1439)
                currentTime -= 1440;
            newDepart = currentTime;

            currentDistance = station.getDistance();
            currentSpeed = (stop.getNextSpeed() > 0) ? stop.getNextSpeed() : defaultSpeed;
            currentSegment = station.getSegmentId();
            currentStationName = station.getStationName();

            if (validateTime(checkStart, checkDuration, newArrive) && validateTime(checkStart, checkDuration, newDepart)) {
                if (updateStops) {
                    stop.setArriveTime(newArrive);
                    stop.setDepartTime(newDepart);
                }
            } else {
                throw new IllegalArgumentException(String.format("%s~%d~%s", TIME_OUT_OF_RANGE, stop.getSeq(), train.getTrainName()));  // NOI18N
            }
        }
    }

    /**
     * Check to see if the supplied time is within the time range for the supplied schedule.
     * If the duration is 24 hours, then all times are valid.
     * Otherwise, we need to calculate the valid range, which can span midnight.
     * @param checkStart The schedule start hour.
     * @param checkDuration The schedule duration.
     * @param checkTime The time value to be check.
     * @return true if the time is valid.
     */
    public boolean validateTime(int checkStart, int checkDuration, int checkTime) {
        if (checkDuration == 24 && checkTime < 1440) {
            return true;
        }

        boolean dayWrap;
        int lowLimit;
        int highLimit;

        if (checkStart + checkDuration > 24) {
            dayWrap = true;
            lowLimit = checkStart * 60;
            highLimit = ((checkStart + checkDuration - 24) * 60) - 1;
        } else {
            dayWrap = false;
            lowLimit = checkStart * 60;
            highLimit = ((checkStart + checkDuration) * 60) - 1;
        }

        if (dayWrap) {
            if (checkTime < 1440 && (checkTime >= lowLimit || checkTime <= highLimit)) {
                return true;
            }
        } else {
            if (checkTime < 1440 && (checkTime >= lowLimit && checkTime <= highLimit)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Internal class that provides a combined segment and station view.
     */
    public class SegmentStation {
        private int _segmentId;
        private int _stationId;

        public SegmentStation(int segmentId, int stationId) {
            _segmentId = segmentId;
            _stationId = stationId;
        }

        public int getSegmentId() {
            return _segmentId;
        }

        public int getStationId() {
            return _stationId;
        }

        @Override
        public String toString() {
            return String.format("%s : %s", getSegment(_segmentId).getSegmentName(), getStation(_stationId).getStationName());  // NOI18N
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableDataManager.class);
}
