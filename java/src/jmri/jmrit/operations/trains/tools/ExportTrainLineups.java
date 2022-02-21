package jmri.jmrit.operations.trains.tools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import javax.swing.JOptionPane;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Builds the train lineups in a comma delimited file (CSV). Only trains that
 * are built and have the "Build" checkbox selected are exported.
 *
 * @author Daniel Boudreau Copyright (C) 2020
 */
public class ExportTrainLineups extends XmlFile {

    public ExportTrainLineups() {
        // do nothing
    }

    public void writeOperationsTrainsFile() {
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
            log.error("Exception while writing the new CSV operations file, may not be complete", e);
        }
    }

    /**
     * Writes the train lineups for each location a train visits. The lineup
     * includes: Train Name, Lead Engine, Location, Arrival Time, Departure
     * Time, Direction, Pulls, Drops, Loads, Empties, Length, Weight, Engineer, Conductor,
     * Train Description, Train Comment
     *
     * @param name file path name
     */
    public void writeFile(String name) {
        log.debug("writeFile {}", name);
        // This is taken in large part from "Java and XML" page 368
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }

        try (CSVPrinter fileOut = new CSVPrinter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)),
                CSVFormat.DEFAULT)) {

            // create header
            fileOut.printRecord(Bundle.getMessage("Name"),
                    Bundle.getMessage("Engine"),
                    Bundle.getMessage("Location"),
                    Bundle.getMessage("Direction"),
                    Bundle.getMessage("Arrives"),
                    Bundle.getMessage("Departs"),
                    Bundle.getMessage("Pulls"),
                    Bundle.getMessage("Drops"),
                    Bundle.getMessage("Loads"),
                    Bundle.getMessage("Empties"),
                    Bundle.getMessage("Length"),
                    Bundle.getMessage("Weight"),
                    Bundle.getMessage("Engineer"),
                    Bundle.getMessage("Conductor"),
                    Bundle.getMessage("RouteLocationComment"),
                    Bundle.getMessage("Description"),
                    Bundle.getMessage("Comment"));

            int count = 0; // number of trains that were exported

            for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByTimeList()) {
                if (!train.isBuildEnabled() || !train.isBuilt() || train.getRoute() == null) {
                    continue;
                }
                count++;
                for (RouteLocation rl : train.getRoute().getLocationsBySequenceList()) {
                    fileOut.printRecord(train.getName(),
                            train.getLeadEngineRoadAndNumber(),
                            TrainCommon.splitString(rl.getLocation().getName()),
                            rl.getTrainDirectionString(),
                            train.getExpectedArrivalTime(rl),
                            train.getExpectedDepartureTime(rl),
                            train.getNumberCarsPickedUp(rl),
                            train.getNumberCarsSetout(rl),
                            train.getNumberLoadedCarsInTrain(rl),
                            train.getNumberEmptyCarsInTrain(rl),
                            train.getTrainLength(rl),
                            train.getTrainWeight(rl),
                            "",
                            "",
                            rl.getComment(),
                            train.getDescription(),
                            train.getComment());
                }
                fileOut.println();
            }

            fileOut.flush();
            fileOut.close();
            log.info("Exported {} trains to file {}", count, defaultOperationsFilename());
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ExportedTrainsToFile"), new Object[]{
                            count, defaultOperationsFilename()}),
                    Bundle.getMessage("ExportComplete"),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            log.error("Can not open export trains CSV file: {}", file.getName());
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ExportedTrainsToFile"), new Object[]{
                            0, defaultOperationsFilename()}),
                    Bundle.getMessage("ExportFailed"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Operation files always use the same directory
    public static String defaultOperationsFilename() {
        return OperationsSetupXml.getFileLocation()
                + OperationsSetupXml.getOperationsDirectoryName()
                + File.separator
                + getOperationsFileName();
    }

    public static void setOperationsFileName(String name) {
        operationsFileName = name;
    }

    public static String getOperationsFileName() {
        return operationsFileName;
    }

    private static String operationsFileName = "ExportOperationsTrainLineups.csv"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(ExportTrainLineups.class);

}
