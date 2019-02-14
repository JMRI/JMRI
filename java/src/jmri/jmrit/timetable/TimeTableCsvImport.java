package jmri.jmrit.timetable;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CSV Record Types. The first field is the record type keyword (not I18N).
 * Most fields are optional.
 * <pre>
 * "Layout", "layout name", "scale", fastClock, throttles, "metric"
 *            Defaults:  "New Layout", "HO", 4, 0, "No"
 *            Occurs:  Must be first record, occurs once
 *
 * "TrainType", "type name", color number
 *            Defaults: "New Type", #000000
 *            Occurs:  Follows Layout record, occurs 0 to n times.  If none, a default train type is created which will be used for all trains.
 *            Notes:  #000000 is black.
 *                    If the type name is UseLayoutTypes, the train types for the current layout will be used.
 *
 * "Segment", "segment name"
 *            Default: "New Segment"
 *            Occurs: Follows last TrainType, if any.  Occurs 1 to n times.
 *
 * "Station", "station name", distance, doubleTrack, sidings, staging
 *            Defaults: "New Station", 1.0, No, 0, 0
 *            Occurs:  Follows parent segment, occurs 1 to n times.
 *            Note:  If the station name is UseSegmentStations, the stations for the current segment will be used.
 *
 * "Schedule", "schedule name", "effective date", startHour, duration
 *            Defaults:  "New Schedule", "Today", 0, 24
 *            Occurs: Follows last station, occurs 1 to n times.
 *
 * "Train", "train name", "train description", type, defaultSpeed, starttime, throttle, notes
 *            Defaults:  "NT", "New Train", 0, 1, 0, 0, ""
 *            Occurs:  Follows parent schedule, occurs 1 to n times.
 *            Note1:  The type is the relative number of the train type listed above starting with 1 for the first train type.
 *            Note2:  The start time is an integer between 0 and 1439, subject to the schedule start time and duration.
 *
 * "Stop", station, duration, nextSpeed, stagingTrack, notes
 *            Defaults:  0, 0, 0, 0, ""
 *            Required: station number.
 *            Occurs:  Follows parent train in the proper sequence.  Occurs 1 to n times.
 *            Notes:  The station is the relative number of the station listed above starting with 1 for the first station.
 *                    If more that one segment is used, the station number is cumulative.
 *
 * Except for Stops, each record can have one of three actions:
 *    1) If no name is supplied, a default object will be created.
 *    2) If the name matches an existing name, the existing object will be used.
 *    3) A new object will be created with the supplied name.  The remaining fields, if any, will replace the default values.
 *
 * Minimal file using defaults except for station names and distances:
 * "Layout"
 * "Segment"
 * "Station", "Station 1", 0.0
 * "Station", "Station 2", 25.0
 * "Schedule"
 * "Train"
 * "Stop", 1
 * "Stop", 2
 * </pre>
 * The import applies the changes to the data in memory.  At the end of the import
 * a dialog is displayed with the option to save the changes to the timetable data file.
 * @author Dave Sand Copyright (C) 2019
 * @since 4.15.3
 */
public class TimeTableCsvImport {

    TimeTableDataManager tdm = TimeTableDataManager.getDataManager();
    boolean errorOccurred;
    List<String> importFeedback = new ArrayList<>();
    FileReader fileReader;
    BufferedReader bufferedReader;
    com.csvreader.CsvReader csvFile;

    int recordNumber = 0;
    int layoutId = 0;       //Current layout object id
    int segmentId = 0;      //Current segment object id
    int scheduleId = 0;     //Current schedule object id
    int trainId = 0;        //Current train object id
    List<Integer> trainTypes = new ArrayList<>();    //List of train type ids, translates the relative type occurrence to a type id
    List<Integer> stations = new ArrayList<>();      //List of stations ids, translates the relative station occurence to a station id

    public List<String> importCsv(File file) throws IOException {
        tdm.setLockCalculate(true);
        errorOccurred = false;
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            csvFile = new com.csvreader.CsvReader(bufferedReader);
            csvFile.setUseComments(true);
            while (csvFile.readRecord()) {
                if (errorOccurred) {
                    break;
                }
                recordNumber += 1;
                if (csvFile.getColumnCount() > 0) {
                    String[] values;
                    values = csvFile.getValues();
                    String recd = values[0];

                    if (recd.equals("Layout") && layoutId == 0) {
                        doLayout(values);
                    } else if (recd.equals("TrainType") && layoutId != 0) {
                        doTrainType(values);
                    } else if (recd.equals("Segment") && layoutId != 0) {
                        doSegment(values);
                    } else if (recd.equals("Station") && segmentId != 0) {
                        doStation(values);
                    } else if (recd.equals("Schedule") && layoutId != 0) {
                        doSchedule(values);
                    } else if (recd.equals("Train") && scheduleId != 0) {
                        doTrain(values);
                    } else if (recd.equals("Stop") && trainId != 0) {
                        doStop(values);
                    } else {
                        log.warn("Unable to process record {}, content = {}", recordNumber, values);
                        importFeedback.add(String.format("Unable to process record %d, content = %s",
                                recordNumber, csvFile.getRawRecord()));
                        errorOccurred = true;
                    }
                }
            }
            csvFile.close();
        } catch (IOException ex) {
            log.error("CSV Import failed: ", ex);
            importFeedback.add(String.format("CSV Import failed: %s", ex.getMessage()));
            errorOccurred = true;
        } finally {
            if(bufferedReader != null) {
               bufferedReader.close();
            }
            if(fileReader != null) {
               fileReader.close();
            }
        }
        tdm.setLockCalculate(false);
        if (!errorOccurred) {
            // Force arrive/depart calculations
            Layout layout = tdm.getLayout(layoutId);
            if (layout != null) {
                int fastClock = layout.getFastClock();
                try {
                    layout.setFastClock(fastClock + 1);
                    layout.setFastClock(fastClock);
                } catch (IllegalArgumentException ex) {
                    log.error("Calculation error occured: ", ex);
                    importFeedback.add(String.format("Calculation error occured: %s", ex.getMessage()));
                }
            }
        }
        return importFeedback;
    }

    void doLayout(String[] values) {
        if (recordNumber != 1) {
            log.error("Invalid file structure");
            importFeedback.add("Invalid file structure, the first record must be a layout record.");
            errorOccurred = true;
            return;
        }
        log.debug("Layout values: {}", Arrays.toString(values));
        if (values.length == 1) {
            // Create default layout
            Layout defaultLayout = new Layout();
            layoutId = defaultLayout.getLayoutId();
            return;
        }

        String layoutName = values[1];
        for (Layout layout : tdm.getLayouts(false)) {
            if (layout.getLayoutName().equals(layoutName)) {
                // Use existing layout
                layoutId = layout.getLayoutId();
                return;
            }
        }

        // Create a new layout and set the name
        Layout newLayout = new Layout();
        layoutId = newLayout.getLayoutId();
        newLayout.setLayoutName(layoutName);

        // Change the defaults to the supplied values if available
        String scaleName = (values.length > 2) ? values[2] : "HO";
        jmri.Scale scale = jmri.ScaleManager.getScale(scaleName);
        if (scale != null) {
            newLayout.setScale(scale);
        }

        String clockString = (values.length > 3) ? values[3] : "4";
        int clock = convertToInteger(clockString);
        if (clock > 0) {
            newLayout.setFastClock(clock);
        }

        String throttlesString = (values.length > 4) ? values[4] : "0";
        int throttles = convertToInteger(throttlesString);
        if (throttles >= 0) {
            newLayout.setThrottles(throttles);
        }

        String metric = (values.length > 5) ? values[5] : "No";
        if (metric.equals("Yes") || metric.equals("No")) {
            newLayout.setMetric((metric.equals("Yes")) ? true : false);
        }
    }

    void doTrainType(String[] values) {
        log.debug("TrainType values: {}", Arrays.toString(values));
        if (values.length == 1) {
            // Create default train type
            TrainType defaultType = new TrainType(layoutId);
            trainTypes.add(defaultType.getTypeId());
            return;
        }

        String typeName = values[1];
        if (typeName.equals("UseLayoutTypes")) {
            for (TrainType currType : tdm.getTrainTypes(layoutId, true)) {
                trainTypes.add(currType.getTypeId());
            }
            return;
        }
        for (TrainType trainType : tdm.getTrainTypes(layoutId, false)) {
            if (trainType.getTypeName().equals(typeName)) {
                // Use existing train type
                trainTypes.add(trainType.getTypeId());
                return;
            }
        }

        // Create a new train type and set the name and color if available
        TrainType newType = new TrainType(layoutId);
        trainTypes.add(newType.getTypeId());
        newType.setTypeName(typeName);

        String typeColor = (values.length > 2) ? values[2] : "#000000";
        try {
            java.awt.Color checkColor = java.awt.Color.decode(typeColor);
            log.debug("Color = {}", checkColor);
            newType.setTypeColor(typeColor);
        } catch (java.lang.NumberFormatException ex) {
            log.error("Invalid color value");
        }
    }

    void doSegment(String[] values) {
        if (recordNumber == 2) {
            // No  train type, create one
            TrainType trainType = new TrainType(layoutId);
            trainTypes.add(trainType.getTypeId());
        }

        log.debug("Segment values: {}", Arrays.toString(values));
        if (values.length == 1) {
            // Create default segment
            Segment defaultSegment = new Segment(layoutId);
            segmentId = defaultSegment.getSegmentId();
            return;
        }

        String segmentName = values[1];
        for (Segment segment : tdm.getSegments(layoutId, false)) {
            if (segment.getSegmentName().equals(segmentName)) {
                // Use existing segment
                segmentId = segment.getSegmentId();
                return;
            }
        }

        // Create a new segment
        Segment newSegment = new Segment(layoutId);
        newSegment.setSegmentName(segmentName);
        segmentId = newSegment.getSegmentId();
    }

    void doStation(String[] values) {
        log.debug("Station values: {}", Arrays.toString(values));
        if (values.length == 1) {
            // Create default station
            Station defaultStation = new Station(segmentId);
            stations.add(defaultStation.getStationId());
            return;
        }

        String stationName = values[1];
        if (stationName.equals("UseSegmentStations")) {
            for (Station currStation : tdm.getStations(segmentId, true)) {
                stations.add(currStation.getStationId());
            }
            return;
        }
        for (Station station : tdm.getStations(segmentId, false)) {
            if (station.getStationName().equals(stationName)) {
                // Use existing station
                stations.add(station.getStationId());
                return;
            }
        }

        // Create a new station
        Station newStation = new Station(segmentId);
        newStation.setStationName(stationName);
        stations.add(newStation.getStationId());

        // Change the defaults to the supplied values if available
        String distanceString = (values.length > 2) ? values[2] : "1.0";
        double distance = convertToDouble(distanceString);
        if (distance >= 0.0) {
            newStation.setDistance(distance);
        }

        String doubleTrack = (values.length > 3) ? values[3] : "No";
        if (doubleTrack.equals("Yes") || doubleTrack.equals("No")) {
            newStation.setDoubleTrack((doubleTrack.equals("Yes")) ? true : false);
        }

        String sidingsString = (values.length > 4) ? values[4] : "0";
        int sidings = convertToInteger(sidingsString);
        if (sidings >= 0) {
            newStation.setSidings(sidings);
        }

        String stagingString = (values.length > 5) ? values[5] : "0";
        int staging = convertToInteger(stagingString);
        if (staging >= 0) {
            newStation.setStaging(staging);
        }
    }

    void doSchedule(String[] values) {
        log.debug("Schedule values: {}", Arrays.toString(values));
        if (values.length == 1) {
            // Create default schedule
            Schedule defaultSchedule = new Schedule(layoutId);
            scheduleId = defaultSchedule.getScheduleId();
            return;
        }

        String scheduleName = values[1];
        for (Schedule schedule : tdm.getSchedules(layoutId, false)) {
            if (schedule.getScheduleName().equals(scheduleName)) {
                // Use existing schedule
                scheduleId = schedule.getScheduleId();
                return;
            }
        }

        // Create a new schedule
        Schedule newSchedule = new Schedule(layoutId);
        newSchedule.setScheduleName(scheduleName);
        scheduleId = newSchedule.getScheduleId();

        // Change the defaults to the supplied values if available
        String effectiveDate = (values.length > 2) ? values[2] : "Today";
        if (!effectiveDate.isEmpty()) {
            newSchedule.setEffDate(effectiveDate);
        }

        String startString = (values.length > 3) ? values[3] : "0";
        int startHour = convertToInteger(startString);
        if (startHour >= 0 && startHour < 24) {
            newSchedule.setStartHour(startHour);
        }

        String durationString = (values.length > 4) ? values[4] : "24";
        int duration = convertToInteger(durationString);
        if (duration > 0 && duration < 25) {
            newSchedule.setDuration(duration);
        }
    }

    void doTrain(String[] values) {
        log.debug("Train values: {}", Arrays.toString(values));
        if (values.length == 1) {
            // Create default train
            Train defaultTrain = new Train(scheduleId);
            defaultTrain.setTypeId(trainTypes.get(0));  // Set default train type using first type
            trainId = defaultTrain.getTrainId();
            return;
        }

        String trainName = values[1];
        for (Train train : tdm.getTrains(scheduleId, 0, false)) {
            if (train.getTrainName().equals(trainName)) {
                // Use existing train
                trainId = train.getTrainId();
                return;
            }
        }

        // Create a new train
        Train newTrain = new Train(scheduleId);
        newTrain.setTrainName(trainName);
        newTrain.setTypeId(trainTypes.get(0));  // Set default train type using first type
        trainId = newTrain.getTrainId();

        // Change the defaults to the supplied values if available
        String description = (values.length > 2) ? values[2] : "";
        if (!description.isEmpty()) {
            newTrain.setTrainDesc(description);
        }

        String typeIndexString = (values.length > 3) ? values[3] : "1";
        int typeIndex = convertToInteger(typeIndexString);
        typeIndex -= 1;      // trainTypes list is 0 to n-1
        if (typeIndex >= 0 && typeIndex < trainTypes.size()) {
            newTrain.setTypeId(trainTypes.get(typeIndex));
        }

        String speedString = (values.length > 4) ? values[4] : "1";
        int defaultSpeed = convertToInteger(speedString);
        if (defaultSpeed >= 0) {
            newTrain.setDefaultSpeed(defaultSpeed);
        }

        String startTimeString = (values.length > 5) ? values[5] : "0";
        int startTime = convertToInteger(startTimeString);
        if (startTime >= 0 && startTime < 1440) {
            // Validate time
            Schedule schedule = tdm.getSchedule(scheduleId);
            if (tdm.validateTime(schedule.getStartHour(), schedule.getDuration(), startTime)) {
                newTrain.setStartTime(startTime);
            } else {
                errorOccurred = true;
                log.error("Train start time outside of schedule time: {}", startTime);
                importFeedback.add(String.format("Train start time outside of schedule time: %d", startTime));
            }
        }

        String throttleString = (values.length > 6) ? values[6] : "0";
        int throttle = convertToInteger(throttleString);
        int throttles = tdm.getLayout(layoutId).getThrottles();
        if (throttle >= 0 && throttle <= throttles) {
            newTrain.setThrottle(throttle);
        }

        String trainNotes = (values.length > 7) ? values[7] : "";
        if (!trainNotes.isEmpty()) {
            newTrain.setTrainNotes(trainNotes);
        }
    }

    void doStop(String[] values) {
        // The stop sequence number is one higher than the last sequence number.
        // Each stop record creates a new stop.
        // Stops don't reuse any existing entries.
        log.debug("Stop values: {}", Arrays.toString(values));
        String stopStationString = (values.length > 1) ? values[1] : "-1";
        int stopStationIndex = convertToInteger(stopStationString);
        stopStationIndex -= 1;       // stations list is 0 to n-1
        if (stopStationIndex >= 0 && stopStationIndex < stations.size()) {
            ArrayList<Stop> stops = tdm.getStops(trainId, 0, false);
            Stop newStop = new Stop(trainId, stops.size() + 1);
            newStop.setStationId(stations.get(stopStationIndex));

            // Change the defaults to the supplied values if available
            String durationString = (values.length > 2) ? values[2] : "0";
            int stopDuration = convertToInteger(durationString);
            if (stopDuration > 0) {
                newStop.setDuration(stopDuration);
            }

            String nextSpeedString = (values.length > 3) ? values[3] : "0";
            int nextSpeed = convertToInteger(nextSpeedString);
            if (nextSpeed > 0) {
                newStop.setNextSpeed(nextSpeed);
            }

            String stagingString = (values.length > 4) ? values[4] : "0";
            int stagingTrack = convertToInteger(stagingString);
            Station station = tdm.getStation(stations.get(stopStationIndex));
            if (stagingTrack >= 0 && stagingTrack <= station.getStaging()) {
                newStop.setStagingTrack(stagingTrack);
            }

            String stopNotes = (values.length > 5) ? values[5] : "";
            if (!stopNotes.isEmpty()) {
                newStop.setStopNotes(stopNotes);
            }
        }
    }

    int convertToInteger(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    double convertToDouble(String number) {
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException ex) {
            return -1.0;
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableCsvImport.class);
}
