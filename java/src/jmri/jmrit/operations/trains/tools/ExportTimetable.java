package jmri.jmrit.operations.trains.tools;

import java.io.*;
import java.text.MessageFormat;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Provides an export to the Timetable feature.
 * 
 * @author Daniel Boudreau Copyright (C) 2019
 * 
 * <pre>
 * Copied from TimeTableCsvImport on 11/25/2019
 * 
 * CSV Record Types. The first field is the record type keyword (not I18N).
 * Most fields are optional.
 * 
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
 */
public class ExportTimetable extends XmlFile {

    static final String ESC = "\""; // escape character NOI18N
    private String del = ","; // delimiter

    public ExportTimetable() {

    }

    public void setDeliminter(String delimiter) {
        del = delimiter;
    }

    public void writeOperationsTimetableFile() {
        makeBackupFile(defaultOperationsFilename());
        try {
            if (!checkFile(defaultOperationsFilename())) {
                // The file does not exist, create it before writing
                java.io.File file = new java.io.File(defaultOperationsFilename());
                java.io.File parentDir = file.getParentFile();
                if (!parentDir.exists()) {
                    if (!parentDir.mkdir()) {
                        log.error("Directory wasn't created");
                    }
                }
                if (file.createNewFile()) {
                    log.debug("File created");
                }
            }
            writeFile(defaultOperationsFilename());
        } catch (Exception e) {
            log.error("Exception while writing the new CSV operations file, may not be complete: " + e);
        }
    }

    public void writeFile(String name) {
        log.debug("writeFile {}", name);
        // This is taken in large part from "Java and XML" page 368
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }

        PrintWriter fileOut = null;

        try {
            fileOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")), // NOI18N
                    true); // NOI18N
        } catch (IOException e) {
            log.error("Can not open export timetable CSV file: " + file.getName());
            return;
        }

        // Layout field
        // "Layout", "layout name", "scale", fastClock, throttles, "metric"
        String line =
                "Layout" +
                        del +
                        ESC +
                        Setup.getRailroadName() +
                        ESC +
                        del +
                        "HO" +
                        del +
                        "4" +
                        del +
                        "0" +
                        del +
                        "No";
        fileOut.println(line);
        
        // TrainType field
        // "TrainType", "type name", color number
        line = "TrainType" +
                del +
                "Freight";
        fileOut.println(line);

        // Segment fields
        // "Segment", "segment name"
        line = "Segment" +
                del +
                ESC +
                "Locations" +
                ESC;
        fileOut.println(line);

        // Station fields
        // "Station", "station name", distance, doubleTrack, sidings, staging

        double distance = 0.0;
        for (Location location : InstanceManager.getDefault(LocationManager.class).getLocationsByNameList()) {
            distance += 1.0;
            line = "Station" +
                    del +
                    ESC +
                    location.getName() +
                    ESC +
                    del +
                    distance +
                    del +
                    "No" +
                    del +
                    "0" +
                    del +
                    "0";
            fileOut.println(line);
        }

        // Schedule fields
        // "Schedule", "schedule name", "effective date", startHour, duration

        line = "Schedule" + del + "default" + del + "Today" + del + "0" + del + "24";
        fileOut.println(line);

        // Train fields
        // "Train", "train name", "train description", type, defaultSpeed,
        // starttime, throttle, notes

        for (

        Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByTimeList()) {
            line = "Train" +
                    del +
                    ESC +
                    train.getName() +
                    ESC +
                    del +
                    ESC +
                    train.getDescription() +
                    ESC +
                    del +
                    "0" +
                    del +
                    "1" +
                    del +
                    train.getDepartTimeMinutes() +
                    del +
                    "0" +
                    del +
                    ESC +
                    train.getComment() +
                    ESC;
            fileOut.println(line);

            // Stop fields
            // "Stop", station, duration, nextSpeed, stagingTrack, notes
            for (RouteLocation rl : train.getRoute().getLocationsBySequenceList()) {
                // calculate station stop
                int station = 0;
                for (Location location : InstanceManager.getDefault(LocationManager.class).getLocationsByNameList()) {
                    station++;
                    if (rl.getLocation() == location) {
                        break;
                    }
                }
                int duration = rl.getMaxCarMoves() * Setup.getSwitchTime();
                line = "Stop" +
                        del +
                        station +
                        del +
                        duration +
                        del +
                        "0" +
                        del +
                        "0" +
                        del +
                        ESC +
                        rl.getComment() +
                        ESC;

                fileOut.println(line);
                // break;
            }
        }

        JOptionPane.showMessageDialog(null,
                MessageFormat.format(Bundle.getMessage("ExportedTimetableToFile"),
                        new Object[]{defaultOperationsFilename()}),
                Bundle.getMessage("ExportComplete"), JOptionPane.INFORMATION_MESSAGE);

        fileOut.flush();
        fileOut.close();
    }
    
    public File getExportFile() {
        return findFile(defaultOperationsFilename());
    }

    // Operation files always use the same directory
    public static String defaultOperationsFilename() {
        return OperationsSetupXml.getFileLocation() +
                OperationsSetupXml.getOperationsDirectoryName() +
                File.separator +
                getOperationsFileName();
    }

    public static void setOperationsFileName(String name) {
        operationsFileName = name;
    }

    public static String getOperationsFileName() {
        return operationsFileName;
    }

    private static String operationsFileName = "ExportOperationsTimetable.csv"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(ExportTimetable.class);

}
