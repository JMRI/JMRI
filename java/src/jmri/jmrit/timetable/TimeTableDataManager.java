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
 * Data Structure:
 *   Layout -- Global data.
 *     TrainTypes -- Assigned to trains for diagram colors.
 *     Segments -- Used for division / sub-division arrangements.
 *       Stations -- Any place a train can stop.
 *     Schedules -- Basic information about a schedule.
 *       Trains -- Train characteristics.
 *         Stops -- A junction between a train and a station that contains arrival and departure times.
 * <p>
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableDataManager {

    private TreeMap<Integer, Layout> _layoutMap = new TreeMap<>();
    private TreeMap<Integer, TrainType> _trainTypeMap = new TreeMap<>();
    private TreeMap<Integer, Segment> _segmentMap = new TreeMap<>();
    private TreeMap<Integer, Station> _stationMap = new TreeMap<>();
    private TreeMap<Integer, Schedule> _scheduleMap = new TreeMap<>();
    private TreeMap<Integer, Train> _trainMap = new TreeMap<>();
    private TreeMap<Integer, Stop> _stopMap = new TreeMap<>();

    private List<SegmentStation> _segmentStations = new ArrayList<>();

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

    public void deleteLayout(int id) {
        _layoutMap.remove(id);
    }

    public void deleteTrainType(int id) {
        _trainTypeMap.remove(id);
    }

    public void deleteSegment(int id) {
        _segmentMap.remove(id);
    }

    public void deleteStation(int id) {
        // Remove segment station row.
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

    public void deleteSchedule(int id) {
        _scheduleMap.remove(id);
    }

    public void deleteTrain(int id) {
        _trainMap.remove(id);
    }

    public void deleteStop(int id) {
        _stopMap.remove(id);
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
            Collections.sort(list, (o1, o2) -> o1.getDistanceString().compareTo(o2.getDistanceString()));
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
            Collections.sort(list, (o1, o2) -> o1.getSeqSort().compareTo(o2.getSeqSort()));
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

        public String toString() {
            return String.format("%s : %s", getSegment(_segmentId).getSegmentName(), getStation(_stationId).getStationName());  // NOI18N
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableDataManager.class);
}
