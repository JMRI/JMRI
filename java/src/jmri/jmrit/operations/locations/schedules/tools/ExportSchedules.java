package jmri.jmrit.operations.locations.schedules.tools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.locations.schedules.*;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.util.swing.JmriJOptionPane;

/**
 * Exports the Operation Schedules into a comma delimited file (CSV).
 *
 * @author Daniel Boudreau Copyright (C) 2018
 *
 */
public class ExportSchedules extends XmlFile {

    public void writeOperationsScheduleFile() {
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
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }

        try (CSVPrinter fileOut = new CSVPrinter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)),
                    CSVFormat.DEFAULT)) {

            // create header
            fileOut.printRecord(Bundle.getMessage("ScheduleName"),
                    Bundle.getMessage("Id"),
                    Bundle.getMessage("Type"),
                    Bundle.getMessage("Random"),
                    Bundle.getMessage("Delivery"),
                    Bundle.getMessage("Road"),
                    Bundle.getMessage("Receive"),
                    Bundle.getMessage("Ship"),
                    Bundle.getMessage("Destination"),
                    Bundle.getMessage("Track"),
                    Bundle.getMessage("Pickup"),
                    Bundle.getMessage("Count"),
                    Bundle.getMessage("Wait"),
                    Bundle.getMessage("Hits"),
                    Bundle.getMessage("Comment"));

            List<Schedule> schedules = InstanceManager.getDefault(ScheduleManager.class).getSchedulesByNameList();
            for (Schedule schedule : schedules) {
                for (ScheduleItem scheduleItem : schedule.getItemsBySequenceList()) {
                    fileOut.printRecord(schedule.getName(),
                            scheduleItem.getId(),
                            scheduleItem.getTypeName(),
                            scheduleItem.getRandom(),
                            scheduleItem.getSetoutTrainScheduleName(),
                            scheduleItem.getRoadName(),
                            scheduleItem.getReceiveLoadName(),
                            scheduleItem.getShipLoadName(),
                            scheduleItem.getDestinationName(),
                            scheduleItem.getDestinationTrackName(),
                            scheduleItem.getPickupTrainScheduleName(),
                            scheduleItem.getCount(),
                            scheduleItem.getWait(),
                            scheduleItem.getHits(),
                            schedule.getComment());
                }

            }
            fileOut.flush();
            fileOut.close();
            log.info("Exported {} schedules to file {}", schedules.size(), defaultOperationsFilename());
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("ExportedSchedulesToFile", schedules.size(), defaultOperationsFilename()),
                    Bundle.getMessage("ExportComplete"), JmriJOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            log.error("Can not open export schedules CSV file: {}", e.getLocalizedMessage());
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("ExportedSchedulesToFile", 0, defaultOperationsFilename()),
                    Bundle.getMessage("ExportFailed"), JmriJOptionPane.ERROR_MESSAGE);
        }
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

    private static String operationsFileName = "ExportOperationsSchedules.csv"; // NOI18N

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExportSchedules.class);

}
