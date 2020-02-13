package jmri.jmrit.operations.trains.tools;

import java.io.*;
import java.text.MessageFormat;

import javax.swing.JOptionPane;

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

    static final String ESC = "\""; // escape character NOI18N
    private String del = ","; // delimiter

    public ExportTrainLineups() {
    }

    public void setDeliminter(String delimiter) {
        del = delimiter;
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
            log.error("Can not open export trains CSV file: " + file.getName());
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ExportedTrainsToFile"), new Object[]{
                            0, defaultOperationsFilename()}),
                    Bundle.getMessage("ExportFailed"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // create header
        fileOut.println(createHeader());

        int count = 0; // number of trains that were exported

        for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByTimeList()) {
            if (!train.isBuildEnabled() || !train.isBuilt() || train.getRoute() == null) {
                continue;
            }
            count++;

            for (RouteLocation rl : train.getRoute().getLocationsBySequenceList()) {

                String line = ESC +
                        train.getName() +
                        ESC +
                        del +
                        ESC +
                        train.getLeadEngineRoadAndNumber() +
                        ESC +
                        del +
                        ESC +
                        TrainCommon.splitString(rl.getLocation().getName()) +
                        ESC +
                        del +
                        ESC +
                        train.getExpectedArrivalTime(rl) +
                        ESC +
                        del +
                        ESC +
                        train.getExpectedDepartureTime(rl) +
                        ESC +
                        del +
                        ESC +
                        train.getNumberCarsPickedUp(rl) +
                        ESC +
                        del +
                        ESC +
                        train.getNumberCarsSetout(rl) +
                        ESC +
                        del +
                        ESC +
                        train.getNumberLoadedCarsInTrain(rl) +
                        ESC +
                        del +
                        ESC +
                        train.getNumberEmptyCarsInTrain(rl) +
                        ESC +
                        del +
                        ESC +
                        train.getTrainLength(rl) +
                        ESC +
                        del +
                        ESC +
                        train.getTrainWeight(rl)+
                        ESC +
                        del +
                        del +
                        del +
                        ESC +
                        train.getDescription() +
                        ESC +
                        del +
                        ESC +
                        train.getComment() +
                        ESC;               
                fileOut.println(line);
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
    }

    /*
     * Train Name, Lead Engine, Location, Arrival Time, Departure Time, Pulls,
     * Drops, Loads, Empties, Length, Weight, Engineer, Conductor, Train
     * Description, Train Comment
     */
    private String createHeader() {
        String header = Bundle.getMessage("Name") +
                del +
                Bundle.getMessage("Engine") +
                del +
                Bundle.getMessage("Location") +
                del +
                Bundle.getMessage("Arrives") +
                del +
                Bundle.getMessage("Departs") +
                del +
                Bundle.getMessage("Pulls") +
                del +
                Bundle.getMessage("Drops") +
                del +
                Bundle.getMessage("Loads") +
                del +
                Bundle.getMessage("Empties") +
                del +
                Bundle.getMessage("Length") +
                del +
                Bundle.getMessage("Weight") +
                del +
                Bundle.getMessage("Engineer") +
                del +
                Bundle.getMessage("Conductor") +
                del +
                Bundle.getMessage("Description") +
                del +
                Bundle.getMessage("Comment");
        return header;
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

    private static String operationsFileName = "ExportOperationsTrainLineups.csv"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(ExportTrainLineups.class);

}
