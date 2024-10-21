package jmri.jmrit.operations.trains.tools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.*;
import jmri.util.swing.JmriJOptionPane;

/**
 * Exports the train roster into a comma delimited file (CSV). Only trains that
 * have the "Build" checkbox selected are exported. If a train is built, a
 * summary of the train's route and work is provided.
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2011, 2019
 *
 */
public class ExportTrains extends XmlFile {

    public ExportTrains(){
        // nothing to do
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
        } catch (IOException e) {
            log.error("Exception while writing the new CSV operations file, may not be complete: {}",
                    e.getLocalizedMessage());
        }
    }

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
            fileOut.printRecord(Bundle.getMessage("Name"), Bundle.getMessage("Description"), Bundle.getMessage("Time"),
                    Bundle.getMessage("Route"), Bundle.getMessage("Departs"), Bundle.getMessage("Terminates"),
                    Bundle.getMessage("Status"), Bundle.getMessage("Comment"), Bundle.getMessage("LocoTypes"),
                    Bundle.getMessage("CarTypes"), Bundle.getMessage("RoadOption"), Bundle.getMessage("RoadsCar"),
                    Bundle.getMessage("RoadOption"), Bundle.getMessage("RoadsCaboose"), Bundle.getMessage("RoadOption"),
                    Bundle.getMessage("RoadsLoco"),
                    Bundle.getMessage("LoadOption"), Bundle.getMessage("Loads"), Bundle.getMessage("OwnerOption"),
                    Bundle.getMessage("Owners"), Bundle.getMessage("Built"),
                    Bundle.getMessage("NormalModeWhenBuilding"), Bundle.getMessage("AllowCarsToReturn"),
                    Bundle.getMessage("AllowThroughCars"), Bundle.getMessage("SendCustomToStaging"),
                    Bundle.getMessage("SendToTerminal", ""),
                    Bundle.getMessage("AllowLocalMoves"), Bundle.getMessage("ServiceAllCars"),
                    Bundle.getMessage("BuildConsist"));

            int count = 0;

            for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByTimeList()) {
                if (!train.isBuildEnabled()) {
                    continue;
                }
                count++;
                String routeName = "";
                if (train.getRoute() != null) {
                    routeName = train.getRoute().getName();
                }
                fileOut.printRecord(train.getName(), train.getDescription(), train.getDepartureTime(), routeName,
                        train.getTrainDepartsName(), train.getTrainTerminatesName(), train.getStatus(),
                        train.getComment(), TrainCommon.formatStringToCommaSeparated(train.getLocoTypeNames()),
                        TrainCommon.formatStringToCommaSeparated(train.getCarTypeNames()), getCarRoadOption(train),
                        getCarRoads(train), getCabooseRoadOption(train), getCabooseRoads(train),
                        getLocoRoadOption(train), getLocoRoads(train), getLoadOption(train),
                        getLoads(train), getOwnerOption(train), getOwners(train), getBuilt(train),
                        train.isBuildTrainNormalEnabled() ? Bundle.getMessage("ButtonYes") : "",
                        train.isAllowReturnToStagingEnabled() ? Bundle.getMessage("ButtonYes") : "",
                        train.isAllowThroughCarsEnabled() ? Bundle.getMessage("ButtonYes") : "",
                        train.isSendCarsWithCustomLoadsToStagingEnabled() ? Bundle.getMessage("ButtonYes") : "",
                        train.isSendCarsToTerminalEnabled() ? Bundle.getMessage("ButtonYes") : "",
                        train.isAllowLocalMovesEnabled() ? Bundle.getMessage("ButtonYes") : "",
                        train.isServiceAllCarsWithFinalDestinationsEnabled() ? Bundle.getMessage("ButtonYes") : "",
                        train.isBuildConsistEnabled() ? Bundle.getMessage("ButtonYes") : "");
            }

            fileOut.println();
            // second create header for built trains
            fileOut.printRecord(Bundle.getMessage("Name"), Bundle.getMessage("csvParameters"),
                    Bundle.getMessage("Attributes"));

            for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByTimeList()) {
                if (!train.isBuildEnabled()) {
                    continue;
                }

                if (train.isBuilt() && train.getRoute() != null) {
                    ArrayList<Object> line = new ArrayList<>();
                    line.addAll(Arrays.asList(new Object[] { train.getName(), Bundle.getMessage("Route") }));
                    train.getRoute().getLocationsBySequenceList().forEach(rl -> line.add(rl.getName()));
                    fileOut.printRecord(line);

                    line.clear();
                    line.addAll(Arrays.asList(new Object[] { train.getName(), Bundle.getMessage("csvArrivalTime") }));
                    train.getRoute().getLocationsBySequenceList()
                            .forEach(rl -> line.add(train.getExpectedArrivalTime(rl)));
                    fileOut.printRecord(line);

                    line.clear();
                    line.addAll(Arrays.asList(new Object[] { train.getName(), Bundle.getMessage("csvDepartureTime") }));
                    train.getRoute().getLocationsBySequenceList()
                            .forEach(rl -> line.add(train.getExpectedDepartureTime(rl)));
                    fileOut.printRecord(line);

                    line.clear();
                    line.addAll(
                            Arrays.asList(new Object[] { train.getName(), Bundle.getMessage("csvTrainDirection") }));
                    train.getRoute().getLocationsBySequenceList().forEach(rl -> line.add(rl.getTrainDirectionString()));
                    fileOut.printRecord(line);

                    line.clear();
                    line.addAll(Arrays.asList(new Object[] { train.getName(), Bundle.getMessage("csvTrainWeight") }));
                    train.getRoute().getLocationsBySequenceList().forEach(rl -> line.add(train.getTrainWeight(rl)));
                    fileOut.printRecord(line);

                    line.clear();
                    line.addAll(Arrays.asList(new Object[] { train.getName(), Bundle.getMessage("csvTrainLength") }));
                    train.getRoute().getLocationsBySequenceList().forEach(rl -> line.add(train.getTrainLength(rl)));
                    fileOut.printRecord(line);

                    line.clear();
                    line.addAll(Arrays.asList(new Object[] { train.getName(), Bundle.getMessage("Engine") }));
                    train.getRoute().getLocationsBySequenceList().forEach(rl -> line.add(train.getLeadEngine(rl)));
                    fileOut.printRecord(line);

                    line.clear();
                    line.addAll(Arrays.asList(new Object[] { train.getName(), Bundle.getMessage("Cars") }));
                    train.getRoute().getLocationsBySequenceList()
                            .forEach(rl -> line.add(train.getNumberCarsInTrain(rl)));
                    fileOut.printRecord(line);

                    line.clear();
                    line.addAll(Arrays.asList(new Object[] { train.getName(), Bundle.getMessage("csvEmpties") }));
                    train.getRoute().getLocationsBySequenceList()
                            .forEach(rl -> line.add(train.getNumberEmptyCarsInTrain(rl)));
                    fileOut.printRecord(line);

                    line.clear();
                    line.addAll(Arrays.asList(new Object[] { train.getName(), Bundle.getMessage("Loads") }));
                    train.getRoute().getLocationsBySequenceList()
                            .forEach(rl -> line.add(train.getNumberLoadedCarsInTrain(rl)));
                    fileOut.printRecord(line);

                    fileOut.println();
                }
            }

            fileOut.flush();
            fileOut.close();
            log.info("Exported {} trains to file {}", count, defaultOperationsFilename());
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("ExportedTrainsToFile",
                            count, defaultOperationsFilename()),
                    Bundle.getMessage("ExportComplete"), JmriJOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            log.error("Can not open export trains CSV file: {}", e.getLocalizedMessage());
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("ExportedTrainsToFile",
                            0, defaultOperationsFilename()),
                    Bundle.getMessage("ExportFailed"), JmriJOptionPane.ERROR_MESSAGE);
        }
    }

    private String getCarRoadOption(Train train) {
        String roadOption = Bundle.getMessage("AcceptAll");
        if (train.getCarRoadOption().equals(Train.INCLUDE_ROADS)) {
            roadOption = Bundle.getMessage(
                    "AcceptOnly") + " " + train.getCarRoadNames().length + " " + Bundle.getMessage("Roads");
        } else if (train.getCarRoadOption().equals(Train.EXCLUDE_ROADS)) {
            roadOption = Bundle.getMessage(
                    "Exclude") + " " + train.getCarRoadNames().length + " " + Bundle.getMessage("Roads");
        }
        return roadOption;
    }

    private String getCarRoads(Train train) {
        if (train.getCarRoadOption().equals(Train.ALL_ROADS)) {
            return "";
        } else {
            return TrainCommon.formatStringToCommaSeparated(train.getCarRoadNames());
        }
    }
    
    private String getCabooseRoadOption(Train train) {
        String roadOption = Bundle.getMessage("AcceptAll");
        if (train.getCabooseRoadOption().equals(Train.INCLUDE_ROADS)) {
            roadOption = Bundle.getMessage(
                    "AcceptOnly") + " " + train.getCabooseRoadNames().length + " " + Bundle.getMessage("Roads");
        } else if (train.getCabooseRoadOption().equals(Train.EXCLUDE_ROADS)) {
            roadOption = Bundle.getMessage(
                    "Exclude") + " " + train.getCabooseRoadNames().length + " " + Bundle.getMessage("Roads");
        }
        return roadOption;
    }

    private String getCabooseRoads(Train train) {
        if (train.getCabooseRoadOption().equals(Train.ALL_ROADS)) {
            return "";
        } else {
            return TrainCommon.formatStringToCommaSeparated(train.getCabooseRoadNames());
        }
    }

    private String getLocoRoadOption(Train train) {
        String roadOption = Bundle.getMessage("AcceptAll");
        if (train.getLocoRoadOption().equals(Train.INCLUDE_ROADS)) {
            roadOption = Bundle.getMessage(
                    "AcceptOnly") + " " + train.getLocoRoadNames().length + " " + Bundle.getMessage("Roads");
        } else if (train.getLocoRoadOption().equals(Train.EXCLUDE_ROADS)) {
            roadOption = Bundle.getMessage(
                    "Exclude") + " " + train.getLocoRoadNames().length + " " + Bundle.getMessage("Roads");
        }
        return roadOption;
    }

    private String getLocoRoads(Train train) {
        if (train.getLocoRoadOption().equals(Train.ALL_ROADS)) {
            return "";
        } else {
            return TrainCommon.formatStringToCommaSeparated(train.getLocoRoadNames());
        }
    }

    private String getLoadOption(Train train) {
        String loadOption = Bundle.getMessage("AcceptAll");
        if (train.getLoadOption().equals(Train.INCLUDE_LOADS)) {
            loadOption = Bundle.getMessage(
                    "AcceptOnly") + " " + train.getLoadNames().length + " " + Bundle.getMessage("Loads");
        } else if (train.getLoadOption().equals(Train.EXCLUDE_LOADS)) {
            loadOption = Bundle.getMessage(
                    "Exclude") + " " + train.getLoadNames().length + " " + Bundle.getMessage("Loads");
        }
        return loadOption;
    }

    private String getLoads(Train train) {
        if (train.getLoadOption().equals(Train.ALL_LOADS)) {
            return "";
        } else {
            return TrainCommon.formatStringToCommaSeparated(train.getLoadNames());
        }
    }

    private String getOwnerOption(Train train) {
        String ownerOption = Bundle.getMessage("AcceptAll");
        if (train.getOwnerOption().equals(Train.INCLUDE_OWNERS)) {
            ownerOption = Bundle.getMessage(
                    "AcceptOnly") + " " + train.getOwnerNames().length + " " + Bundle.getMessage("Owners");
        } else if (train.getOwnerOption().equals(Train.EXCLUDE_OWNERS)) {
            ownerOption = Bundle.getMessage(
                    "Exclude") + " " + train.getOwnerNames().length + " " + Bundle.getMessage("Owners");
        }
        return ownerOption;
    }

    private String getOwners(Train train) {
        if (train.getOwnerOption().equals(Train.ALL_OWNERS)) {
            return "";
        } else {
            return TrainCommon.formatStringToCommaSeparated(train.getOwnerNames());
        }
    }

    private String getBuilt(Train train) {
        if (!train.getBuiltStartYear().equals(Train.NONE) && train.getBuiltEndYear().equals(Train.NONE)) {
            return Bundle.getMessage("After") + " " + train.getBuiltStartYear();
        }
        if (train.getBuiltStartYear().equals(Train.NONE) && !train.getBuiltEndYear().equals(Train.NONE)) {
            return Bundle.getMessage("Before") + " " + train.getBuiltEndYear();
        }
        if (!train.getBuiltStartYear().equals(Train.NONE) && !train.getBuiltEndYear().equals(Train.NONE)) {
            return Bundle.getMessage("Range") + " " + train.getBuiltStartYear() + ":" + train.getBuiltEndYear();
        }
        return "";
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExportTrains.class);

}
