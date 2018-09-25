package jmri.jmrit.operations.locations.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exports the location roster into a comma delimitated file (CSV).
 *
 * @author Daniel Boudreau Copyright (C) 2018
 *
 */
public class ExportLocations extends XmlFile {

    static final String ESC = "\""; // escape character NOI18N
    private String del = ","; // delimiter

    public void setDeliminter(String delimiter) {
        del = delimiter;
    }

    public void writeOperationsLocationFile() {
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
            log.error("Can not open export locations CSV file: {}", file.getName());
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ExportedLocationsToFile"), new Object[]{
                            0, defaultOperationsFilename()}),
                    Bundle.getMessage("ExportFailed"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // create header
        String header = Bundle.getMessage("Location") +
                del +
                Bundle.getMessage("Track") +
                del +
                Bundle.getMessage("Type") +
                del +
                Bundle.getMessage("Length") +
                del +
                Bundle.getMessage("RoadOption") +
                del +
                Bundle.getMessage("Roads") +
                del +
                Bundle.getMessage("LoadOption") +
                del +
                Bundle.getMessage("Loads") +
                del +
                Bundle.getMessage("ShipLoadOption") +
                del +
                Bundle.getMessage("Ships") +
                del +
                Bundle.getMessage("ScheduleName") +
                del +
                Bundle.getMessage("AlternateTrack");

        fileOut.println(header);

        String line = "";
        String locationName;
        String trackName;
        String roadNames;
        String loadNames;
        String shipNames;
        String alternateTrackName;

        List<Location> locations = InstanceManager.getDefault(LocationManager.class).getLocationsByNameList();

        for (Location location : locations) {
            locationName = location.getName();
            if (locationName.contains(del)) {
                locationName = ESC + locationName + ESC;
            }
            for (Track track : location.getTrackByNameList(null)) {
                trackName = track.getName();
                if (trackName.contains(del)) {
                    trackName = ESC + trackName + ESC;
                }
                roadNames = "";
                if (!track.getRoadOption().equals(Track.ALL_ROADS)) {
                    for (String roadName : track.getRoadNames()) {
                        roadNames = roadNames + roadName + "; ";
                    }
                }
                loadNames = "";
                if (!track.getLoadOption().equals(Track.ALL_LOADS)) {
                    for (String loadName : track.getLoadNames()) {
                        loadNames = loadNames + loadName + "; ";
                    }
                }
                shipNames = "";
                if (!track.getShipLoadOption().equals(Track.ALL_LOADS)) {
                    for (String shipName : track.getShipLoadNames()) {
                        shipNames = shipNames + shipName + "; ";
                    }
                }
                alternateTrackName = "";
                if (track.getAlternateTrack() != null) {
                    alternateTrackName = track.getAlternateTrack().getName();
                }
                if (track.isAlternate()) {
                    alternateTrackName = Bundle.getMessage("ButtonYes");
                }

                line = locationName +
                        del +
                        trackName +
                        del +
                        track.getTrackTypeName() +
                        del +
                        track.getLength() +
                        del +
                        track.getRoadOptionString() +
                        del +
                        roadNames +
                        del +
                        track.getLoadOptionString() +
                        del +
                        loadNames +
                        del +
                        track.getShipLoadOptionString() +
                        del +
                        shipNames +
                        del +
                        track.getScheduleName() +
                        del +
                        alternateTrackName;
                
                fileOut.println(line);
            }

        }
        fileOut.flush();
        fileOut.close();
        log.info("Exported " + locations.size() + " locations to file " + defaultOperationsFilename());
        JOptionPane.showMessageDialog(null,
                MessageFormat.format(Bundle.getMessage("ExportedLocationsToFile"), new Object[]{
                        locations.size(), defaultOperationsFilename()}),
                Bundle.getMessage("ExportComplete"),
                JOptionPane.INFORMATION_MESSAGE);
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

    private static String operationsFileName = "ExportOperationsLocationRoster.csv"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(ExportLocations.class);

}
