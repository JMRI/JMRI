package jmri.jmrit.timetable;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TimeTableImport {
    BufferedReader bufferedReader;
    FileReader fileReader;
    String line;
    TimeTableDataManager _dm;
    int _layoutId = 0;
    int _segmentId = 0;
    int _scheduleId = 0;
    int _trainId = 0;
    int _routeFirst = 0;
    int _routeLast = 0;
    List<Integer> _stationIds = new ArrayList<>();
    HashMap<String, Integer> _typeIds = new HashMap<>();

    public void importSgn(TimeTableDataManager dm, File file) throws IOException {
        _dm = dm;
        _dm.setLockCalculate(true);
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);

            int row = 1;
            String currType = "";
            int stationCount = 0;
            int typeCount = 0;
            int trainCount = 0;
            int stopCount = 0;
            int stopSeq = 0;

            while ((line = bufferedReader.readLine()) != null) {
                // Split line and remove double quotes
                String[] lineStrings = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");  // NOI18N
                for (int i = 0; i < lineStrings.length; i++) {
                    lineStrings[i] = lineStrings[i].replace("\"", "");
                }

                switch (row) {
                    case 1:
                        log.info("SchedGen Import, version: {}", lineStrings[0]);  // NOI18N
                        break;

                    case 2:
                        createLayout(lineStrings);
                        createSegment(lineStrings);
                        createSchedule(lineStrings);
                        break;

                    case 3:
                        stationCount = Integer.parseInt(lineStrings[0]);
                        currType = "Station";  // NOI18N
                        break;

                    default:
                        if (stationCount > 0) {
                            createStation(lineStrings);
                            stationCount--;
                            break;
                        }

                        if (currType.equals("Station")) {  // NOI18N
                            currType = "Type";  // NOI18N
                            typeCount = Integer.parseInt(lineStrings[0]) + 1;
                            break;
                        }
                        if (typeCount > 0) {
                            createTrainType(lineStrings);
                            typeCount--;
                            break;
                        }

                        if (currType.equals("Type")) {  // NOI18N
                            currType = "Train";  // NOI18N
                            trainCount = Integer.parseInt(lineStrings[0]) + 1;
                            stopCount = 0;
                            break;
                        }
                        if (trainCount > 0) {
                            if (stopCount == 0) {
                                // Create train record
                                createTrain(lineStrings);

                                stopCount = -1;
                                break;
                            }
                            if (stopCount == -1) {
                                stopCount = Integer.parseInt(lineStrings[0]);
                                stopSeq = 1;
                                _routeFirst = 0;
                                _routeLast = 0;
                                break;

                            }
                            if (stopCount > 0) {
                                // create stop record
                                createStop(lineStrings, stopSeq);

                                stopCount--;
                                stopSeq++;
                                if (stopCount == 0) {
                                    int routeDur;
                                    if (_routeLast > _routeFirst) {
                                        routeDur = _routeLast - _routeFirst;
                                    } else {
                                        routeDur = 1440 - _routeFirst + _routeLast;
                                    }
                                    _dm.getTrain(_trainId).setRouteDuration(routeDur);

                                    trainCount--;
                                }
                                break;
                            }
                        }
                }
                row++;
            }
        } catch (IOException e) {
            log.error("Error reading file: " + e);  // NOI18N
        } finally {
            if(bufferedReader != null) {
               bufferedReader.close();
            }
            if(fileReader != null) {
               fileReader.close();
            }
        }
        _dm.setLockCalculate(false);
    }

    void createLayout(String[] lineStrings) {
// "Sierra Western","Default","08/01/11",0,24,"5:1","HO",5
//         _layoutId = layoutId;
//         _layoutName = layoutName;
//         _scale = scale;
//         _fastClock = fastClock;
//         _throttles = throttles;
//         _metric = metric;
//         String clockString = lineStrings[5].replace("\"", "");
        String[] clockComp = lineStrings[5].split(":");  // NOI18N
        int clock = Integer.parseInt(clockComp[0]);
        int throttles = Integer.parseInt(lineStrings[7]);
        _layoutId = _dm.getNextId("Layout");  // NOI18N
        Layout layout = new Layout(_layoutId,
                lineStrings[0],
                jmri.ScaleManager.getScale(lineStrings[6]),
                clock,
                throttles,
                false);
        _dm.addLayout(_layoutId, layout);
    }

    void createTrainType(String[] lineStrings) {
// for (int i = 0; i < lineStrings.length; i++) {
//     log.info("@@ type: {}", lineStrings[i]);
// }
// "Freight, general",32768
//         _typeId = typeId;
//         _layoutId = layoutId;
//         _typeName = typeName;
//          typeColor

        int colorInt = Integer.parseInt(lineStrings[lineStrings.length - 1]);
        String colorStr = String.format("#%06X", colorInt);  // NOI18N
        String typeName = lineStrings[0];
        int typeId = _dm.getNextId("TrainType");  // NOI18N
        TrainType trainType = new TrainType(typeId,
                _layoutId,
                typeName,
                colorStr);
        _dm.addTrainType(typeId, trainType);
        _typeIds.put(typeName, typeId);
    }

    void createSegment(String[] lineStrings) {
//         _segmentId = segmentId;
//         _layoutId = layoutId;
//         _segmentName = segmentName;
        _segmentId = _dm.getNextId("Segment");  // NOI18N
        Segment segment = new Segment(_segmentId,
                _layoutId,
                "Mainline");  // NOI18N
        _dm.addSegment(_segmentId, segment);
    }

    void createStation(String[] lineStrings) {
// "Butte",0,0,9,"N"
//         D S St Dbl
//         _stationId = stationId;
//         _segmentId = segmentId;
//         _stationName = stationName;
//         _distance = distance;
//         _doubleTrack = doubleTrack;
//         _sidings = sidings;
//         _staging = staging;
        int stationId = _dm.getNextId("Station");  // NOI18N
        Station station = new Station(stationId,
                _segmentId,
                lineStrings[0],
                Double.parseDouble(lineStrings[1]),
                (lineStrings[4].equals("N")) ? false : true,
                Integer.parseInt(lineStrings[2]),
                Integer.parseInt(lineStrings[3]));
        _dm.addStation(stationId, station);
        _stationIds.add(stationId);
    }

    void createSchedule(String[] lineStrings) {
 // "Sierra Western","Default","08/01/11",0,24,"5:1","HO",5

//          _scheduleId = scheduleId;
//         _layoutId = layoutId;
//         _scheduleName = scheduleName;
//         _effDate = effDate;
//         _startHour = startHour;
//         _duration = duration;
        _scheduleId = _dm.getNextId("Schedule");  // NOI18N
        Schedule schedule = new Schedule(_scheduleId,
                _layoutId,
                lineStrings[1],
                lineStrings[2],
                Integer.parseInt(lineStrings[3]),
                Integer.parseInt(lineStrings[4]));
        _dm.addSchedule(_scheduleId, schedule);
    }

    void createTrain(String[] lineStrings) {
// "201","Expediter","Freight,priority",0,740,0,""
//  name  desc        type              speed start throttle notes
//         _trainId = trainId;
//         _scheduleId = scheduleId;
//         _typeId = typeId;
//         _trainName = trainName;
//         _trainDesc = trainDesc;
//         _defaultSpeed = defaultSpeed;
//         _startTime = startTime;
//         _throttle = throttle;
//         _routeDuration = routeDuration;
//         _trainNotes = trainNotes;
        String notes = lineStrings[6];

        int typeId = _typeIds.get(lineStrings[2]);
        _trainId = _dm.getNextId("Train");  // NOI18N
        Train train = new Train(_trainId,
                _scheduleId,
                typeId,
                lineStrings[0],
                lineStrings[1],
                Integer.parseInt(lineStrings[3]),
                Integer.parseInt(lineStrings[4]),
                Integer.parseInt(lineStrings[5]),
                0,
                notes);
        _dm.addTrain(_trainId, train);
    }

    void createStop(String[] lineStrings, int seq) {
// 13,0,30,555,555,0,""
// ST D NX  A   D  T  N
//         _trainId = trainId;
//         _stationId = stationId;
//         _seq = seq;
//         _duration = duration;
//         _nextSpeed = nextSpeed;
//         _arriveTime = arriveTime;
//         _departTime = departTime;
//         _stagingTrack = stagingTrack;
//         _stopNotes = stopNotes;


        int _stopId = _dm.getNextId("Stop");  // NOI18N
        Stop stop = new Stop(_stopId,
                _trainId,
                _stationIds.get(Integer.parseInt(lineStrings[0]) - 1),
                seq,
                Integer.parseInt(lineStrings[1]),
                Integer.parseInt(lineStrings[2]),
                Integer.parseInt(lineStrings[3]),
                Integer.parseInt(lineStrings[4]),
                Integer.parseInt(lineStrings[5]),
                lineStrings[6]);
        _dm.addStop(_stopId, stop);

        if (seq == 1) {
            _routeFirst = Integer.parseInt(lineStrings[3]);
        }
        _routeLast = Integer.parseInt(lineStrings[4]);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableImport.class);
}
