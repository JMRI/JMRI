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
        TimeTableDataManager dataMgr = InstanceManager.getDefault(TimeTableFrame.class).getDataManager();
        TimeTableXmlFile x = new TimeTableXmlFile();
        File file = x.getFile(true);
        try {
            FileUtil.rotate(file, 4, "bup");
        } catch (IOException ex) {
            log.warn("Rotate failed, reverting to xml backup");
            x.makeBackupFile(TimeTableXmlFile.getDefaultFileName());
        }

        // Create root element
        Element root = new Element("timetable-data");
        root.setAttribute("noNamespaceSchemaLocation",
                "http://jmri.org/xml/schema/timetable.xsd",
                org.jdom2.Namespace.getNamespace("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance"));
        Document doc = new Document(root);
//
//         // add XSLT processing instruction
//         java.util.Map<String, String> m = new java.util.HashMap<String, String>();
//         m.put("type", "text/xsl");
//         m.put("href", SpeedometerXml.xsltLocation + "speedometer.xsl");
//         ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
//         doc.addContent(0, p);
//
        Element values;

        root.addContent(values = new Element("layouts"));
        for (Layout layout : dataMgr.getLayouts(false)) {
            Element e = new Element("layout");
            e.addContent(new Element("layout_id").addContent("" + layout.getLayoutId()));
            e.addContent(new Element("layout_name").addContent(layout.getLayoutName()));
            e.addContent(new Element("fast_clock").addContent("" + layout.getFastClock()));
            e.addContent(new Element("throttles").addContent("" + layout.getThrottles()));
            e.addContent(new Element("metric").addContent((layout.getMetric()) ? "yes" : "no"));
            values.addContent(e);
        }

        root.addContent(values = new Element("train_types"));
        for (TrainType type : dataMgr.getTrainTypes(0, false)) {
            Element e = new Element("train_type");
            e.addContent(new Element("type_id").addContent("" + type.getTypeId()));
            e.addContent(new Element("layout_id").addContent("" + type.getLayoutId()));
            e.addContent(new Element("type_name").addContent(type.getTypeName()));
            e.addContent(new Element("type_color").addContent(type.getTypeColor()));
            values.addContent(e);
        }

        root.addContent(values = new Element("segments"));
        for (Segment segment : dataMgr.getSegments(0, false)) {
            Element e = new Element("segment");
            e.addContent(new Element("segment_id").addContent("" + segment.getSegmentId()));
            e.addContent(new Element("layout_id").addContent("" + segment.getLayoutId()));
            e.addContent(new Element("segment_name").addContent(segment.getSegmentName()));
            values.addContent(e);
        }

        root.addContent(values = new Element("stations"));
        for (Station station : dataMgr.getStations(0, false)) {
            Element e = new Element("station");
            e.addContent(new Element("station_id").addContent("" + station.getStationId()));
            e.addContent(new Element("segment_id").addContent("" + station.getSegmentId()));
            e.addContent(new Element("station_name").addContent(station.getStationName()));
            e.addContent(new Element("distance").addContent("" + station.getDistance()));
            e.addContent(new Element("double_track").addContent((station.getDoubleTrack()) ? "yes" : "no"));
            e.addContent(new Element("sidings").addContent("" + station.getSidings()));
            e.addContent(new Element("staging").addContent("" + station.getStaging()));
            values.addContent(e);
        }

        root.addContent(values = new Element("schedules"));
        for (Schedule schedule : dataMgr.getSchedules(0, false)) {
            Element e = new Element("schedule");
            e.addContent(new Element("schedule_id").addContent("" + schedule.getScheduleId()));
            e.addContent(new Element("layout_id").addContent("" + schedule.getLayoutId()));
            e.addContent(new Element("schedule_name").addContent(schedule.getScheduleName()));
            e.addContent(new Element("eff_date").addContent(schedule.getEffDate()));
            e.addContent(new Element("start_hour").addContent("" + schedule.getStartHour()));
            e.addContent(new Element("duration").addContent("" + schedule.getDuration()));
            values.addContent(e);
        }

        root.addContent(values = new Element("trains"));
        for (Train train : dataMgr.getTrains(0, 0, false)) {
            Element e = new Element("train");
            e.addContent(new Element("train_id").addContent("" + train.getTrainId()));
            e.addContent(new Element("schedule_id").addContent("" + train.getScheduleId()));
            e.addContent(new Element("type_id").addContent("" + train.getTypeId()));
            e.addContent(new Element("train_name").addContent(train.getTrainName()));
            e.addContent(new Element("train_desc").addContent(train.getTrainDesc()));
            e.addContent(new Element("default_speed").addContent("" + train.getDefaultSpeed()));
            e.addContent(new Element("start_time").addContent("" + train.getStartTime()));
            e.addContent(new Element("throttle").addContent("" + train.getThrottle()));
            e.addContent(new Element("route_duration").addContent("" + train.getRouteDuration()));
            e.addContent(new Element("train_notes").addContent(train.getTrainNotes()));
            values.addContent(e);
        }

        root.addContent(values = new Element("stops"));
        for (Stop stop : dataMgr.getStops(0, 0, false)) {
            Element e = new Element("stop");
            e.addContent(new Element("stop_id").addContent("" + stop.getStopId()));
            e.addContent(new Element("train_id").addContent("" + stop.getTrainId()));
            e.addContent(new Element("station_id").addContent("" + stop.getStationId()));
            e.addContent(new Element("seq").addContent("" + stop.getSeq()));
            e.addContent(new Element("duration").addContent("" + stop.getDuration()));
            e.addContent(new Element("next_speed").addContent("" + stop.getNextSpeed()));
            e.addContent(new Element("arrive_time").addContent("" + stop.getArriveTime()));
            e.addContent(new Element("depart_time").addContent("" + stop.getDepartTime()));
            e.addContent(new Element("staging_track").addContent("" + stop.getStagingTrack()));
            e.addContent(new Element("stop_notes").addContent(stop.getStopNotes()));
            values.addContent(e);
        }

        try {
            x.writeXML(file, doc);
        } catch (FileNotFoundException ex) {
            log.error("File not found when writing: " + ex);
            return false;
        } catch (IOException ex) {
            log.error("IO Exception when writing: " + ex);
            return false;
        }

        log.debug("...done");
        return true;
    }

    public static boolean doLoad() {
        TimeTableDataManager dataMgr = InstanceManager.getDefault(TimeTableFrame.class).getDataManager();
        TimeTableXmlFile x = new TimeTableXmlFile();
        File file = x.getFile(false);

        if (file == null) {
            log.debug("Nothing to load");
            return false;
        }

        // Validate foreign keys
        List<Integer> checkLayoutIds = new ArrayList<>();
        List<Integer> checkTypeIds = new ArrayList<>();
        List<Integer> checkSegmentIds = new ArrayList<>();
        List<Integer> checkStationIds = new ArrayList<>();
        List<Integer> checkScheduleIds = new ArrayList<>();
        List<Integer> checkTrainIds = new ArrayList<>();

        log.debug("Start loading timetable data...");

        // Find root
        Element root;
        try {
            root = x.rootFromFile(file);
            if (root == null) {
                log.debug("File could not be read");
                return false;
            }

            // Layouts
            Element layouts = root.getChild("layouts");
            if (layouts == null) {
                log.error("Unable to find a layout entry");
                return false;
            }
            for (Element layout : layouts.getChildren("layout")) {
                Element layout_id = layout.getChild("layout_id");
                int layoutId = (layout_id == null) ? 0 : Integer.parseInt(layout_id.getValue());
                Element layout_name = layout.getChild("layout_name");
                String layoutName = (layout_name == null) ? "" : layout_name.getValue();
                Element fast_clock = layout.getChild("fast_clock");
                int fastClock = (fast_clock == null) ? 1 : Integer.parseInt(fast_clock.getValue());
                Element throttlesE = layout.getChild("throttles");
                int throttles = (throttlesE == null) ? 0 : Integer.parseInt(throttlesE.getValue());
                Element metricE = layout.getChild("metric");
                boolean metric = (metricE == null) ? false : metricE.getValue().equals("yes");
                log.debug("  Layout: {} - {} - {} - {} - {}",
                    layoutId, layoutName, fastClock, throttles, metric);

                // Create a Layout
                Layout newLayout = new Layout(layoutId, layoutName, fastClock, throttles, metric);
                dataMgr.addLayout(layoutId, newLayout);
                checkLayoutIds.add(layoutId);
            }

            // Train Types
            Element train_types = root.getChild("train_types");
            if (train_types == null) {
                log.error("Unable to find train types");
                return false;
            }
            for (Element train_type : train_types.getChildren("train_type")) {
                Element type_id = train_type.getChild("type_id");
                int typeId = (type_id == null) ? 0 : Integer.parseInt(type_id.getValue());
                Element layout_id = train_type.getChild("layout_id");
                int layoutId = (layout_id == null) ? 0 : Integer.parseInt(layout_id.getValue());
                Element type_name = train_type.getChild("type_name");
                String typeName = (type_name == null) ? "" : type_name.getValue();
                Element type_color = train_type.getChild("type_color");
                String typeColor = (type_color == null) ? "#000000" : type_color.getValue();
                log.debug("    Type: {} - {} - {}", typeId, typeName, typeColor);

                // Validate layoutId
                if (!checkLayoutIds.contains(layoutId)) {
                    log.warn("TrainType {} layout id not found", typeName);
                    continue;
                }

                // Create a train type
                TrainType newType = new TrainType(typeId, layoutId, typeName, typeColor);
                dataMgr.addTrainType(typeId, newType);
                checkTypeIds.add(typeId);
            }

            // Segments
            Element segments = root.getChild("segments");
            if (segments == null) {
                log.error("Unable to find segments");
                return false;
            }
            for (Element segment : segments.getChildren("segment")) {
                Element segment_id = segment.getChild("segment_id");
                int segmentId = (segment_id == null) ? 0 : Integer.parseInt(segment_id.getValue());
                Element layout_id = segment.getChild("layout_id");
                int layoutId = (layout_id == null) ? 0 : Integer.parseInt(layout_id.getValue());
                Element segment_name = segment.getChild("segment_name");
                String segmentName = (segment_name == null) ? "" : segment_name.getValue();
                log.debug("    Segment: {} - {} - {}", segmentId, layoutId, segmentName);

                // Validate layoutId
                if (!checkLayoutIds.contains(layoutId)) {
                    log.warn("Segment {} layout id not found", segmentName);
                    continue;
                }

                // Create a segment
                Segment newSegment = new Segment(segmentId, layoutId, segmentName);
                dataMgr.addSegment(segmentId, newSegment);
                checkSegmentIds.add(segmentId);
            }

            // Stations
            Element stations = root.getChild("stations");
            if (stations == null) {
                log.error("Unable to find stations");
                return false;
            }
            for (Element station : stations.getChildren("station")) {
                Element station_id = station.getChild("station_id");
                int stationId = (station_id == null) ? 0 : Integer.parseInt(station_id.getValue());
                Element segment_id = station.getChild("segment_id");
                int segmentId = (segment_id == null) ? 0 : Integer.parseInt(segment_id.getValue());
                Element station_name = station.getChild("station_name");
                String stationName = (station_name == null) ? "" : station_name.getValue();
                Element distanceE = station.getChild("distance");
                double distance = (distanceE == null) ? 1.0 : Double.parseDouble(distanceE.getValue());
                Element double_track = station.getChild("double_track");
                boolean doubleTrack = (double_track == null) ? false : double_track.getValue().equals("yes");
                Element sidingsE = station.getChild("sidings");
                int sidings = (sidingsE == null) ? 0 : Integer.parseInt(sidingsE.getValue());
                Element stagingE = station.getChild("staging");
                int staging = (stagingE == null) ? 0 : Integer.parseInt(stagingE.getValue());
                log.debug("      Station: {} - {} - {} - {} - {} - {}", stationId, stationName, distance, doubleTrack, sidings, staging);

                // Validate segmentId
                if (!checkSegmentIds.contains(segmentId)) {
                    log.warn("Station {} segment id not found", stationName);
                    continue;
                }

                // Create a station
                Station newStation = new Station(stationId, segmentId, stationName, distance, doubleTrack, sidings, staging);
                dataMgr.addStation(stationId, newStation);
                checkStationIds.add(stationId);
            }

            Element schedules = root.getChild("schedules");
            if (schedules == null) {
                log.error("Unable to find schedules");
                return false;
            }
            for (Element schedule : schedules.getChildren("schedule")) {
                Element schedule_id = schedule.getChild("schedule_id");
                int scheduleId = (schedule_id == null) ? 0 : Integer.parseInt(schedule_id.getValue());
                Element layout_id = schedule.getChild("layout_id");
                int layoutId = (layout_id == null) ? 0 : Integer.parseInt(layout_id.getValue());
                Element schedule_name = schedule.getChild("schedule_name");
                String scheduleName = (schedule_name == null) ? "" : schedule_name.getValue();
                Element eff_date = schedule.getChild("eff_date");
                String effDate = (eff_date == null) ? "" : eff_date.getValue();
                Element start_hour = schedule.getChild("start_hour");
                int startHour = (start_hour == null) ? 0 : Integer.parseInt(start_hour.getValue());
                Element durationE = schedule.getChild("duration");
                int duration = (durationE == null) ? 0 : Integer.parseInt(durationE.getValue());
                log.debug("    Schedule: {} - {} - {} - {} - {} - {}", scheduleId, layoutId, scheduleName, effDate, startHour, duration);

                // Validate layoutId
                if (!checkLayoutIds.contains(layoutId)) {
                    log.warn("Schdule {} layout id not found", scheduleName);
                    continue;
                }

                // Create a schedule
                Schedule newSchedule = new Schedule(scheduleId, layoutId, scheduleName, effDate, startHour, duration);
                dataMgr.addSchedule(scheduleId, newSchedule);
                checkScheduleIds.add(scheduleId);
            }

            Element trains = root.getChild("trains");
            if (trains == null) {
                log.error("Unable to find trains");
                return false;
            }
            for (Element train : trains.getChildren("train")) {
                Element train_id = train.getChild("train_id");
                int trainId = (train_id == null) ? 0 : Integer.parseInt(train_id.getValue());
                Element schedule_id = train.getChild("schedule_id");
                int scheduleId = (schedule_id == null) ? 0 : Integer.parseInt(schedule_id.getValue());
                Element type_id = train.getChild("type_id");
                int typeId = (type_id == null) ? 0 : Integer.parseInt(type_id.getValue());
                Element train_name = train.getChild("train_name");
                String trainName = (train_name == null) ? "" : train_name.getValue();
                Element train_desc = train.getChild("train_desc");
                String trainDesc = (train_desc == null) ? "" : train_desc.getValue();
                Element default_speed = train.getChild("default_speed");
                int defaultSpeed = (default_speed == null) ? 1 : Integer.parseInt(default_speed.getValue());
                Element start_time = train.getChild("start_time");
                int startTime = (start_time == null) ? 0 : Integer.parseInt(start_time.getValue());
                Element throttleE = train.getChild("throttle");
                int throttle = (throttleE == null) ? 0 : Integer.parseInt(throttleE.getValue());
                Element route_duration = train.getChild("route_duration");
                int routeDuration = (route_duration == null) ? 0 : Integer.parseInt(route_duration.getValue());
                Element train_notes = train.getChild("train_notes");
                String trainNotes = (train_notes == null) ? "" : train_notes.getValue();
                log.debug("      Train: {} - {} - {} - {} - {} - {} - {} - {} - {}",
                        trainId, scheduleId, typeId, trainName, trainDesc, defaultSpeed, startTime, throttle, routeDuration, trainNotes);

                // Validate scheduleId
                if (!checkScheduleIds.contains(scheduleId)) {
                    log.warn("Train {} schedule id not found", trainName);
                    continue;
                }
                // Validate typeId
                if (!checkTypeIds.contains(typeId)) {
                    log.warn("Train {} type id not found", trainName);
                    continue;
                }

                // Create a train
                Train newTrain = new Train(trainId, scheduleId, typeId, trainName, trainDesc,
                        defaultSpeed, startTime, throttle, routeDuration, trainNotes);
                dataMgr.addTrain(trainId, newTrain);
                checkTrainIds.add(trainId);
            }

            Element stops = root.getChild("stops");
            if (stops == null) {
                log.error("Unable to find stops");
                return false;
            }
            for (Element stop : stops.getChildren("stop")) {
                Element stop_id = stop.getChild("stop_id");
                int stopId = (stop_id == null) ? 0 : Integer.parseInt(stop_id.getValue());
                Element train_id = stop.getChild("train_id");
                int trainId = (train_id == null) ? 0 : Integer.parseInt(train_id.getValue());
                Element station_id = stop.getChild("station_id");
                int stationId = (station_id == null) ? 0 : Integer.parseInt(station_id.getValue());
                Element seqE = stop.getChild("seq");
                int seq = (seqE == null) ? 0 : Integer.parseInt(seqE.getValue());
                Element durationE = stop.getChild("duration");
                int duration = (durationE == null) ? 0 : Integer.parseInt(durationE.getValue());
                Element next_speed = stop.getChild("next_speed");
                int nextSpeed = (next_speed == null) ? 0 : Integer.parseInt(next_speed.getValue());
                Element arrive_time = stop.getChild("arrive_time");
                int arriveTime = (arrive_time == null) ? 0 : Integer.parseInt(arrive_time.getValue());
                Element depart_time = stop.getChild("depart_time");
                int departTime = (depart_time == null) ? 0 : Integer.parseInt(depart_time.getValue());
                Element staging_track = stop.getChild("staging_track");
                int stagingTrack = (staging_track == null) ? 0 : Integer.parseInt(staging_track.getValue());
                Element stop_notes = stop.getChild("stop_notes");
                String stopNotes = (stop_notes == null) ? "" : stop_notes.getValue();

                log.debug("        Stop: {} - {} - {} - {} - {} - {} - {} - {} - {} - {}",
                        stopId, trainId, stationId, seq, duration, nextSpeed, arriveTime, departTime, stagingTrack, stopNotes);

                // Validate trainId
                if (!checkTrainIds.contains(trainId)) {
                    log.warn("Stop train id not found");
                    continue;
                }
                // Validate stationId
                if (!checkStationIds.contains(stationId)) {
                    log.warn("Stop station id not found");
                    continue;
                }

                // Create a stop
                Stop newStop = new Stop(stopId, trainId, stationId, seq, duration,
                        nextSpeed, arriveTime, departTime, stagingTrack, stopNotes);
                dataMgr.addStop(stopId, newStop);
            }
        } catch (JDOMException ex) {
            log.error("File invalid: " + ex);
            return false;
        } catch (IOException ex) {
            log.error("Error reading file: " + ex);
            return false;
        }

        log.debug("...done");
        return true;
    }



    private static class TimeTableXmlFile extends XmlFile {
        private static String fileLocation = FileUtil.getUserFilesPath() + "timetable/";
        private static String demoLocation = FileUtil.getProgramPath() + "xml/demoTimetable/";
        private static String baseFileName = "TimeTableData.xml";

        public static String getDefaultFileName() {
            return getFileLocation() + getFileName();
        }

        public File getFile(boolean store) {
            // Verify that preference:timetable exists
            File chkdir = new File(getFileLocation());
            if (!chkdir.exists()) {
                if (!chkdir.mkdir()) {
                    log.error("Create preference:timetable failed");
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
                    log.error("Copy TimeTable demo file failed");
                    return null;
                }
            }

            File file = findFile(getDefaultFileName());
            if (file == null && store) {
                log.info("create new file");
                file = new File(getDefaultFileName());
            }
            return file;
        }

        public static String getFileName() {
            return baseFileName;
        }

        /**
         * Absolute path to location of TimeTable files.
         *
         * @return path to location
         */
        public static String getFileLocation() {
            return fileLocation;
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableXml.class);
}
