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
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Exports the train roster into a comma delimited file (CSV). Only trains that
 * have the "Build" checkbox selected are exported. If a train is built, a
 * summary of the train's route and work is provided.
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2011
 *
 */
public class ExportTrains extends XmlFile {

    static final String ESC = "\""; // escape character NOI18N
    private String del = ","; // delimiter

    public ExportTrains() {

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
        String header = Bundle.getMessage("Name") +
                del +
                Bundle.getMessage("Description") +
                del +
                Bundle.getMessage("Time") +
                del +
                Bundle.getMessage("Route") +
                del +
                Bundle.getMessage("Departs") +
                del +
                Bundle.getMessage("Terminates") +
                del +
                Bundle.getMessage("Status") +
                del +
                Bundle.getMessage("Comment");
        fileOut.println(header);

        int count = 0;

        for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByTimeList()) {
            if (!train.isBuildEnabled())
                continue;
            count++;
            String routeName = "";
            if (train.getRoute() != null)
                routeName = train.getRoute().getName();
            String line = ESC +
                    train.getName() +
                    ESC +
                    del +
                    ESC +
                    train.getDescription() +
                    ESC +
                    del +
                    ESC +
                    train.getDepartureTime() +
                    ESC +
                    del +
                    ESC +
                    routeName +
                    ESC +
                    del +
                    ESC +
                    train.getTrainDepartsName() +
                    ESC +
                    del +
                    ESC +
                    train.getTrainTerminatesName() +
                    ESC +
                    del +
                    ESC +
                    train.getStatus() +
                    ESC +
                    del +
                    ESC +
                    train.getComment() +
                    ESC;
            fileOut.println(line);
        }

        fileOut.println();
        // second create header for built trains
        header = Bundle.getMessage("Name") +
                del +
                Bundle.getMessage("csvParameters") +
                del +
                Bundle.getMessage("Attributes");
        fileOut.println(header);

        for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByTimeList()) {
            if (!train.isBuildEnabled())
                continue;

            if (train.isBuilt() && train.getRoute() != null) {
                StringBuffer line = new StringBuffer(ESC + train.getName() + ESC + del + Bundle.getMessage("Route"));
                for (RouteLocation rl : train.getRoute().getLocationsBySequenceList()) {
                    line.append(del + ESC + rl.getName() + ESC);
                }
                fileOut.println(line);

                line = new StringBuffer(ESC + train.getName() + ESC + del + Bundle.getMessage("csvArrivalTime"));
                for (RouteLocation rl : train.getRoute().getLocationsBySequenceList()) {
                    line.append(del + ESC + train.getExpectedArrivalTime(rl) + ESC);
                }
                fileOut.println(line);

                line = new StringBuffer(ESC + train.getName() + ESC + del + Bundle.getMessage("csvDepartureTime"));
                for (RouteLocation rl : train.getRoute().getLocationsBySequenceList()) {
                    line.append(del + ESC + train.getExpectedDepartureTime(rl) + ESC);
                }
                fileOut.println(line);

                line = new StringBuffer(ESC + train.getName() + ESC + del + Bundle.getMessage("csvTrainDirection"));
                for (RouteLocation rl : train.getRoute().getLocationsBySequenceList()) {
                    line.append(del + ESC + rl.getTrainDirectionString() + ESC);
                }
                fileOut.println(line);

                line = new StringBuffer(ESC + train.getName() + ESC + del + Bundle.getMessage("csvTrainWeight"));
                for (RouteLocation rl : train.getRoute().getLocationsBySequenceList()) {
                    line.append(del + ESC + train.getTrainWeight(rl) + ESC);
                }
                fileOut.println(line);

                line = new StringBuffer(ESC + train.getName() + ESC + del + Bundle.getMessage("csvTrainLength"));
                for (RouteLocation rl : train.getRoute().getLocationsBySequenceList()) {
                    line.append(del + ESC + train.getTrainLength(rl) + ESC);
                }
                fileOut.println(line);

                line = new StringBuffer(ESC + train.getName() + ESC + del + Bundle.getMessage("Cars"));
                for (RouteLocation rl : train.getRoute().getLocationsBySequenceList()) {
                    line.append(del + ESC + train.getNumberCarsInTrain(rl) + ESC);
                }
                fileOut.println(line);

                line = new StringBuffer(ESC + train.getName() + ESC + del + Bundle.getMessage("csvEmpties"));
                for (RouteLocation rl : train.getRoute().getLocationsBySequenceList()) {
                    line.append(del + ESC + train.getNumberEmptyCarsInTrain(rl) + ESC);
                }
                fileOut.println(line);
                fileOut.println();
            }
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

    private static String operationsFileName = "ExportOperationsTrainRoster.csv"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(ExportTrains.class);

}
