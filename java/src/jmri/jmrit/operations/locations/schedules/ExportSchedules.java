package jmri.jmrit.operations.locations.schedules;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.setup.OperationsSetupXml;

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
            log.error("Exception while writing the new CSV operations file, may not be complete: {}", e);
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
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ExportedSchedulesToFile"), new Object[]{
                schedules.size(), defaultOperationsFilename()}),
                    Bundle.getMessage("ExportComplete"),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            log.error("Can not open export schedules CSV file: {}", file.getName());
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ExportedSchedulesToFile"), new Object[]{
                0, defaultOperationsFilename()}),
                    Bundle.getMessage("ExportFailed"),
                    JOptionPane.ERROR_MESSAGE);
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

    private final static Logger log = LoggerFactory.getLogger(ExportSchedules.class);

}
