package jmri.jmrit.timetable.configurexml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
// import org.jdom2.ProcessingInstruction;

import jmri.jmrit.timetable.*;
import jmri.jmrit.timetable.swing.*;

/**
 * Load and store the timetable data file: TimeTableData.xml
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableXml {

    public static boolean doStore() {
        TimeTableDataManager dataMgr = TimeTableDataManager.getDataManager();
        TimeTableXmlFile x = new TimeTableXmlFile();
        File file = x.getFile(true);
        try {
            FileUtil.rotate(file, 4, "bup");  // NOI18N
        } catch (IOException ex) {
            log.warn("Rotate failed, reverting to xml backup");  // NOI18N
            x.makeBackupFile(TimeTableXmlFile.getDefaultFileName());
        }

        // Create root element
        Element root = new Element("timetable-data");  // NOI18N
        root.setAttribute("noNamespaceSchemaLocation",  // NOI18N
                "http://jmri.org/xml/schema/timetable.xsd",  // NOI18N
                org.jdom2.Namespace.getNamespace("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance"));  // NOI18N
        Document doc = new Document(root);
        Element values;

        root.addContent(values = new Element("layouts"));  // NOI18N
        for (Layout layout : dataMgr.getLayouts(false)) {
            Element e = new Element("layout");  // NOI18N
            e.addContent(new Element("layout_id").addContent("" + layout.getLayoutId()));  // NOI18N
            e.addContent(new Element("layout_name").addContent(layout.getLayoutName()));  // NOI18N
            e.addContent(new Element("scale").addContent(layout.getScale().getScaleName()));  // NOI18N
            e.addContent(new Element("fast_clock").addContent("" + layout.getFastClock()));  // NOI18N
            e.addContent(new Element("throttles").addContent("" + layout.getThrottles()));  // NOI18N
            e.addContent(new Element("metric").addContent((layout.getMetric()) ? "yes" : "no"));  // NOI18N
            values.addContent(e);
        }

        root.addContent(values = new Element("train_types"));  // NOI18N
        for (TrainType type : dataMgr.getTrainTypes(0, false)) {
            Element e = new Element("train_type");  // NOI18N
            e.addContent(new Element("type_id").addContent("" + type.getTypeId()));  // NOI18N
            e.addContent(new Element("layout_id").addContent("" + type.getLayoutId()));  // NOI18N
            e.addContent(new Element("type_name").addContent(type.getTypeName()));  // NOI18N
            e.addContent(new Element("type_color").addContent(type.getTypeColor()));  // NOI18N
            values.addContent(e);
        }

        root.addContent(values = new Element("segments"));  // NOI18N
        for (Segment segment : dataMgr.getSegments(0, false)) {
            Element e = new Element("segment");  // NOI18N
            e.addContent(new Element("segment_id").addContent("" + segment.getSegmentId()));  // NOI18N
            e.addContent(new Element("layout_id").addContent("" + segment.getLayoutId()));  // NOI18N
            e.addContent(new Element("segment_name").addContent(segment.getSegmentName()));  // NOI18N
            values.addContent(e);
        }

        root.addContent(values = new Element("stations"));  // NOI18N
        for (Station station : dataMgr.getStations(0, false)) {
            Element e = new Element("station");  // NOI18N
            e.addContent(new Element("station_id").addContent("" + station.getStationId()));  // NOI18N
            e.addContent(new Element("segment_id").addContent("" + station.getSegmentId()));  // NOI18N
            e.addContent(new Element("station_name").addContent(station.getStationName()));  // NOI18N
            e.addContent(new Element("distance").addContent("" + station.getDistance()));  // NOI18N
            e.addContent(new Element("double_track").addContent((station.getDoubleTrack()) ? "yes" : "no"));  // NOI18N
            e.addContent(new Element("sidings").addContent("" + station.getSidings()));  // NOI18N
            e.addContent(new Element("staging").addContent("" + station.getStaging()));  // NOI18N
            values.addContent(e);
        }

        root.addContent(values = new Element("schedules"));  // NOI18N
        for (Schedule schedule : dataMgr.getSchedules(0, false)) {
            Element e = new Element("schedule");  // NOI18N
            e.addContent(new Element("schedule_id").addContent("" + schedule.getScheduleId()));  // NOI18N
            e.addContent(new Element("layout_id").addContent("" + schedule.getLayoutId()));  // NOI18N
            e.addContent(new Element("schedule_name").addContent(schedule.getScheduleName()));  // NOI18N
            e.addContent(new Element("eff_date").addContent(schedule.getEffDate()));  // NOI18N
            e.addContent(new Element("start_hour").addContent("" + schedule.getStartHour()));  // NOI18N
            e.addContent(new Element("duration").addContent("" + schedule.getDuration()));  // NOI18N
            values.addContent(e);
        }

        root.addContent(values = new Element("trains"));  // NOI18N
        for (Train train : dataMgr.getTrains(0, 0, false)) {
            Element e = new Element("train");  // NOI18N
            e.addContent(new Element("train_id").addContent("" + train.getTrainId()));  // NOI18N
            e.addContent(new Element("schedule_id").addContent("" + train.getScheduleId()));  // NOI18N
            e.addContent(new Element("type_id").addContent("" + train.getTypeId()));  // NOI18N
            e.addContent(new Element("train_name").addContent(train.getTrainName()));  // NOI18N
            e.addContent(new Element("train_desc").addContent(train.getTrainDesc()));  // NOI18N
            e.addContent(new Element("default_speed").addContent("" + train.getDefaultSpeed()));  // NOI18N
            e.addContent(new Element("start_time").addContent("" + train.getStartTime()));  // NOI18N
            e.addContent(new Element("throttle").addContent("" + train.getThrottle()));  // NOI18N
            e.addContent(new Element("route_duration").addContent("" + train.getRouteDuration()));  // NOI18N
            e.addContent(new Element("train_notes").addContent(train.getTrainNotes()));  // NOI18N
            values.addContent(e);
        }

        root.addContent(values = new Element("stops"));  // NOI18N
        for (Stop stop : dataMgr.getStops(0, 0, false)) {
            Element e = new Element("stop");  // NOI18N
            e.addContent(new Element("stop_id").addContent("" + stop.getStopId()));  // NOI18N
            e.addContent(new Element("train_id").addContent("" + stop.getTrainId()));  // NOI18N
            e.addContent(new Element("station_id").addContent("" + stop.getStationId()));  // NOI18N
            e.addContent(new Element("seq").addContent("" + stop.getSeq()));  // NOI18N
            e.addContent(new Element("duration").addContent("" + stop.getDuration()));  // NOI18N
            e.addContent(new Element("next_speed").addContent("" + stop.getNextSpeed()));  // NOI18N
            e.addContent(new Element("arrive_time").addContent("" + stop.getArriveTime()));  // NOI18N
            e.addContent(new Element("depart_time").addContent("" + stop.getDepartTime()));  // NOI18N
            e.addContent(new Element("staging_track").addContent("" + stop.getStagingTrack()));  // NOI18N
            e.addContent(new Element("stop_notes").addContent(stop.getStopNotes()));  // NOI18N
            values.addContent(e);
        }

        try {
            x.writeXML(file, doc);
        } catch (FileNotFoundException ex) {
            log.error("File not found when writing: " + ex);  // NOI18N
            return false;
        } catch (IOException ex) {
            log.error("IO Exception when writing: " + ex);  // NOI18N
            return false;
        }

        log.debug("...done");  // NOI18N
        return true;
    }

    public static boolean doLoad() {
        TimeTableDataManager dataMgr = TimeTableDataManager.getDataManager();
        TimeTableXmlFile x = new TimeTableXmlFile();
        File file = x.getFile(false);

        if (file == null) {
            log.debug("Nothing to load");  // NOI18N
            return false;
        }

        // Validate foreign keys
        List<Integer> checkLayoutIds = new ArrayList<>();
        List<Integer> checkTypeIds = new ArrayList<>();
        List<Integer> checkSegmentIds = new ArrayList<>();
        List<Integer> checkStationIds = new ArrayList<>();
        List<Integer> checkScheduleIds = new ArrayList<>();
        List<Integer> checkTrainIds = new ArrayList<>();

        log.debug("Start loading timetable data...");  // NOI18N

        // Find root
        Element root;
        try {
            root = x.rootFromFile(file);
            if (root == null) {
                log.debug("File could not be read");  // NOI18N
                return false;
            }

            // Layouts
            Element layouts = root.getChild("layouts");  // NOI18N
            if (layouts == null) {
                log.error("Unable to find a layout entry");  // NOI18N
                return false;
            }
            for (Element layout : layouts.getChildren("layout")) {  // NOI18N
                Element layout_id = layout.getChild("layout_id");  // NOI18N
                int layoutId = (layout_id == null) ? 0 : Integer.parseInt(layout_id.getValue());
                Element layout_name = layout.getChild("layout_name");  // NOI18N
                String layoutName = (layout_name == null) ? "" : layout_name.getValue();
                Element scaleE = layout.getChild("scale");  // NOI18N
                jmri.Scale scale = (scaleE == null) ? jmri.ScaleManager.getScale("HO") : jmri.ScaleManager.getScale(scaleE.getValue());  // NOI18N
                Element fast_clock = layout.getChild("fast_clock");  // NOI18N
                int fastClock = (fast_clock == null) ? 1 : Integer.parseInt(fast_clock.getValue());
                Element throttlesE = layout.getChild("throttles");  // NOI18N
                int throttles = (throttlesE == null) ? 0 : Integer.parseInt(throttlesE.getValue());
                Element metricE = layout.getChild("metric");  // NOI18N
                boolean metric = (metricE == null) ? false : metricE.getValue().equals("yes");  // NOI18N
                log.debug("  Layout: {} - {} - {} - {} - {} - {}",  // NOI18N
                    layoutId, layoutName, scale, fastClock, throttles, metric);

                // Create a Layout
                Layout newLayout = new Layout(layoutId, layoutName, scale, fastClock, throttles, metric);
                dataMgr.addLayout(layoutId, newLayout);
                checkLayoutIds.add(layoutId);
            }

            // Train Types
            Element train_types = root.getChild("train_types");  // NOI18N
            if (train_types == null) {
                log.error("Unable to find train types");  // NOI18N
                return false;
            }
            for (Element train_type : train_types.getChildren("train_type")) {  // NOI18N
                Element type_id = train_type.getChild("type_id");  // NOI18N
                int typeId = (type_id == null) ? 0 : Integer.parseInt(type_id.getValue());
                Element layout_id = train_type.getChild("layout_id");  // NOI18N
                int layoutId = (layout_id == null) ? 0 : Integer.parseInt(layout_id.getValue());
                Element type_name = train_type.getChild("type_name");  // NOI18N
                String typeName = (type_name == null) ? "" : type_name.getValue();
                Element type_color = train_type.getChild("type_color");  // NOI18N
                String typeColor = (type_color == null) ? "#000000" : type_color.getValue();  // NOI18N
                log.debug("    Type: {} - {} - {}", typeId, typeName, typeColor);  // NOI18N

                // Validate layoutId
                if (!checkLayoutIds.contains(layoutId)) {
                    log.warn("TrainType {} layout id not found", typeName);  // NOI18N
                    continue;
                }

                // Create a train type
                TrainType newType = new TrainType(typeId, layoutId, typeName, typeColor);
                dataMgr.addTrainType(typeId, newType);
                checkTypeIds.add(typeId);
            }

            // Segments
            Element segments = root.getChild("segments");  // NOI18N
            if (segments == null) {
                log.error("Unable to find segments");  // NOI18N
                return false;
            }
            for (Element segment : segments.getChildren("segment")) {  // NOI18N
                Element segment_id = segment.getChild("segment_id");  // NOI18N
                int segmentId = (segment_id == null) ? 0 : Integer.parseInt(segment_id.getValue());
                Element layout_id = segment.getChild("layout_id");  // NOI18N
                int layoutId = (layout_id == null) ? 0 : Integer.parseInt(layout_id.getValue());
                Element segment_name = segment.getChild("segment_name");  // NOI18N
                String segmentName = (segment_name == null) ? "" : segment_name.getValue();
                log.debug("    Segment: {} - {} - {}", segmentId, layoutId, segmentName);  // NOI18N

                // Validate layoutId
                if (!checkLayoutIds.contains(layoutId)) {
                    log.warn("Segment {} layout id not found", segmentName);  // NOI18N
                    continue;
                }

                // Create a segment
                Segment newSegment = new Segment(segmentId, layoutId, segmentName);
                dataMgr.addSegment(segmentId, newSegment);
                checkSegmentIds.add(segmentId);
            }

            // Stations
            Element stations = root.getChild("stations");  // NOI18N
            if (stations == null) {
                log.error("Unable to find stations");  // NOI18N
                return false;
            }
            for (Element station : stations.getChildren("station")) {  // NOI18N
                Element station_id = station.getChild("station_id");  // NOI18N
                int stationId = (station_id == null) ? 0 : Integer.parseInt(station_id.getValue());
                Element segment_id = station.getChild("segment_id");  // NOI18N
                int segmentId = (segment_id == null) ? 0 : Integer.parseInt(segment_id.getValue());
                Element station_name = station.getChild("station_name");  // NOI18N
                String stationName = (station_name == null) ? "" : station_name.getValue();
                Element distanceE = station.getChild("distance");  // NOI18N
                double distance = (distanceE == null) ? 1.0 : Double.parseDouble(distanceE.getValue());
                Element double_track = station.getChild("double_track");  // NOI18N
                boolean doubleTrack = (double_track == null) ? false : double_track.getValue().equals("yes");  // NOI18N
                Element sidingsE = station.getChild("sidings");  // NOI18N
                int sidings = (sidingsE == null) ? 0 : Integer.parseInt(sidingsE.getValue());
                Element stagingE = station.getChild("staging");  // NOI18N
                int staging = (stagingE == null) ? 0 : Integer.parseInt(stagingE.getValue());
                log.debug("      Station: {} - {} - {} - {} - {} - {}", stationId, stationName, distance, doubleTrack, sidings, staging);  // NOI18N

                // Validate segmentId
                if (!checkSegmentIds.contains(segmentId)) {
                    log.warn("Station {} segment id not found", stationName);  // NOI18N
                    continue;
                }

                // Create a station
                Station newStation = new Station(stationId, segmentId, stationName, distance, doubleTrack, sidings, staging);
                dataMgr.addStation(stationId, newStation);
                checkStationIds.add(stationId);
            }

            Element schedules = root.getChild("schedules");  // NOI18N
            if (schedules == null) {
                log.error("Unable to find schedules");  // NOI18N
                return false;
            }
            for (Element schedule : schedules.getChildren("schedule")) {  // NOI18N
                Element schedule_id = schedule.getChild("schedule_id");  // NOI18N
                int scheduleId = (schedule_id == null) ? 0 : Integer.parseInt(schedule_id.getValue());
                Element layout_id = schedule.getChild("layout_id");  // NOI18N
                int layoutId = (layout_id == null) ? 0 : Integer.parseInt(layout_id.getValue());
                Element schedule_name = schedule.getChild("schedule_name");  // NOI18N
                String scheduleName = (schedule_name == null) ? "" : schedule_name.getValue();
                Element eff_date = schedule.getChild("eff_date");  // NOI18N
                String effDate = (eff_date == null) ? "" : eff_date.getValue();
                Element start_hour = schedule.getChild("start_hour");  // NOI18N
                int startHour = (start_hour == null) ? 0 : Integer.parseInt(start_hour.getValue());
                Element durationE = schedule.getChild("duration");  // NOI18N
                int duration = (durationE == null) ? 0 : Integer.parseInt(durationE.getValue());
                log.debug("    Schedule: {} - {} - {} - {} - {} - {}", scheduleId, layoutId, scheduleName, effDate, startHour, duration);  // NOI18N

                // Validate layoutId
                if (!checkLayoutIds.contains(layoutId)) {
                    log.warn("Schdule {} layout id not found", scheduleName);  // NOI18N
                    continue;
                }

                // Create a schedule
                Schedule newSchedule = new Schedule(scheduleId, layoutId, scheduleName, effDate, startHour, duration);
                dataMgr.addSchedule(scheduleId, newSchedule);
                checkScheduleIds.add(scheduleId);
            }

            Element trains = root.getChild("trains");  // NOI18N
            if (trains == null) {
                log.error("Unable to find trains");  // NOI18N
                return false;
            }
            for (Element train : trains.getChildren("train")) {  // NOI18N
                Element train_id = train.getChild("train_id");  // NOI18N
                int trainId = (train_id == null) ? 0 : Integer.parseInt(train_id.getValue());
                Element schedule_id = train.getChild("schedule_id");  // NOI18N
                int scheduleId = (schedule_id == null) ? 0 : Integer.parseInt(schedule_id.getValue());
                Element type_id = train.getChild("type_id");  // NOI18N
                int typeId = (type_id == null) ? 0 : Integer.parseInt(type_id.getValue());
                Element train_name = train.getChild("train_name");  // NOI18N
                String trainName = (train_name == null) ? "" : train_name.getValue();
                Element train_desc = train.getChild("train_desc");  // NOI18N
                String trainDesc = (train_desc == null) ? "" : train_desc.getValue();
                Element default_speed = train.getChild("default_speed");  // NOI18N
                int defaultSpeed = (default_speed == null) ? 1 : Integer.parseInt(default_speed.getValue());
                Element start_time = train.getChild("start_time");  // NOI18N
                int startTime = (start_time == null) ? 0 : Integer.parseInt(start_time.getValue());
                Element throttleE = train.getChild("throttle");  // NOI18N
                int throttle = (throttleE == null) ? 0 : Integer.parseInt(throttleE.getValue());
                Element route_duration = train.getChild("route_duration");  // NOI18N
                int routeDuration = (route_duration == null) ? 0 : Integer.parseInt(route_duration.getValue());
                Element train_notes = train.getChild("train_notes");  // NOI18N
                String trainNotes = (train_notes == null) ? "" : train_notes.getValue();
                log.debug("      Train: {} - {} - {} - {} - {} - {} - {} - {} - {}",  // NOI18N
                        trainId, scheduleId, typeId, trainName, trainDesc, defaultSpeed, startTime, throttle, routeDuration, trainNotes);

                // Validate scheduleId
                if (!checkScheduleIds.contains(scheduleId)) {
                    log.warn("Train {} schedule id not found", trainName);  // NOI18N
                    continue;
                }
                // Validate typeId
                if (!checkTypeIds.contains(typeId)) {
                    log.warn("Train {} type id not found", trainName);  // NOI18N
                    continue;
                }

                // Create a train
                Train newTrain = new Train(trainId, scheduleId, typeId, trainName, trainDesc,
                        defaultSpeed, startTime, throttle, routeDuration, trainNotes);
                dataMgr.addTrain(trainId, newTrain);
                checkTrainIds.add(trainId);
            }

            Element stops = root.getChild("stops");  // NOI18N
            if (stops == null) {
                log.error("Unable to find stops");  // NOI18N
                return false;
            }
            for (Element stop : stops.getChildren("stop")) {  // NOI18N
                Element stop_id = stop.getChild("stop_id");  // NOI18N
                int stopId = (stop_id == null) ? 0 : Integer.parseInt(stop_id.getValue());
                Element train_id = stop.getChild("train_id");  // NOI18N
                int trainId = (train_id == null) ? 0 : Integer.parseInt(train_id.getValue());
                Element station_id = stop.getChild("station_id");  // NOI18N
                int stationId = (station_id == null) ? 0 : Integer.parseInt(station_id.getValue());
                Element seqE = stop.getChild("seq");  // NOI18N
                int seq = (seqE == null) ? 0 : Integer.parseInt(seqE.getValue());
                Element durationE = stop.getChild("duration");  // NOI18N
                int duration = (durationE == null) ? 0 : Integer.parseInt(durationE.getValue());
                Element next_speed = stop.getChild("next_speed");  // NOI18N
                int nextSpeed = (next_speed == null) ? 0 : Integer.parseInt(next_speed.getValue());
                Element arrive_time = stop.getChild("arrive_time");  // NOI18N
                int arriveTime = (arrive_time == null) ? 0 : Integer.parseInt(arrive_time.getValue());
                Element depart_time = stop.getChild("depart_time");  // NOI18N
                int departTime = (depart_time == null) ? 0 : Integer.parseInt(depart_time.getValue());
                Element staging_track = stop.getChild("staging_track");  // NOI18N
                int stagingTrack = (staging_track == null) ? 0 : Integer.parseInt(staging_track.getValue());
                Element stop_notes = stop.getChild("stop_notes");  // NOI18N
                String stopNotes = (stop_notes == null) ? "" : stop_notes.getValue();

                log.debug("        Stop: {} - {} - {} - {} - {} - {} - {} - {} - {} - {}",  // NOI18N
                        stopId, trainId, stationId, seq, duration, nextSpeed, arriveTime, departTime, stagingTrack, stopNotes);

                // Validate trainId
                if (!checkTrainIds.contains(trainId)) {
                    log.warn("Stop train id not found");  // NOI18N
                    continue;
                }
                // Validate stationId
                if (!checkStationIds.contains(stationId)) {
                    log.warn("Stop station id not found");  // NOI18N
                    continue;
                }

                // Create a stop
                Stop newStop = new Stop(stopId, trainId, stationId, seq, duration,
                        nextSpeed, arriveTime, departTime, stagingTrack, stopNotes);
                dataMgr.addStop(stopId, newStop);
            }
        } catch (JDOMException ex) {
            log.error("File invalid: " + ex);  // NOI18N
            return false;
        } catch (IOException ex) {
            log.error("Error reading file: " + ex);  // NOI18N
            return false;
        }

        log.debug("...done");  // NOI18N
        return true;
    }


    public static class TimeTableXmlFile extends XmlFile {
        private static String fileLocation = FileUtil.getUserFilesPath() + "timetable/";  // NOI18N
        private static String demoLocation = FileUtil.getProgramPath() + "xml/demoTimetable/";  // NOI18N
        private static String baseFileName = "TimeTableData.xml";  // NOI18N

        public static String getDefaultFileName() {
            return getFileLocation() + getFileName();
        }

        public File getFile(boolean store) {
            // Verify that preference:timetable exists
            File chkdir = new File(getFileLocation());
            if (!chkdir.exists()) {
                if (!chkdir.mkdir()) {
                    log.error("Create preference:timetable failed");  // NOI18N
                    return null;
                }
            }

            // Verify that the TimeTable data file exists
            File chkfile = new File(getDefaultFileName());
            if (!chkfile.exists()) {
                // Copy the demo file
                File demoFile = new File(demoLocation + baseFileName);
                Path toPath = chkdir.toPath();
                Path fromPath = demoFile.toPath();
                try {
                    Files.copy(fromPath, toPath.resolve(fromPath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    log.error("Copy TimeTable demo file failed");  // NOI18N
                    return null;
                }
            }

            File file = findFile(getDefaultFileName());
            if (file == null && store) {
                log.info("create new file");  // NOI18N
                file = new File(getDefaultFileName());
            }
            return file;
        }

        public static String getFileName() {
            if(baseFileName == null) {
               baseFileName = "TimeTableData.xml";  // NOI18N
            }
            return baseFileName;
        }

        /**
         * Absolute path to location of TimeTable files.
         *
         * @return path to location
         */
        public static String getFileLocation() {
            if(fileLocation==null){
               fileLocation = FileUtil.getUserFilesPath() + "timetable/";  // NOI18N
            }
            return fileLocation;
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableXml.class);
}
