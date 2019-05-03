package jmri.jmrit.operations.locations.schedules;

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
import jmri.jmrit.operations.locations.tools.ExportLocations;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exports the Operation Schedules into a comma delimitated file (CSV).
 *
 * @author Daniel Boudreau Copyright (C) 2018
 *
 */
public class ExportSchedules extends XmlFile {

    static final String ESC = "\""; // escape character NOI18N
    private String del = ","; // delimiter

    public void setDeliminter(String delimiter) {
        del = delimiter;
    }

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
            log.error("Can not open export schedules CSV file: {}", file.getName());
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ExportedSchedulesToFile"), new Object[]{
                            0, defaultOperationsFilename()}),
                    Bundle.getMessage("ExportFailed"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // create header
        String header = Bundle.getMessage("ScheduleName") +
                del +
                Bundle.getMessage("Id") +
                del +
                Bundle.getMessage("Type") +
                del +
                Bundle.getMessage("Random") +
                del +
                Bundle.getMessage("Delivery") +
                del +
                Bundle.getMessage("Road") +
                del +
                Bundle.getMessage("Receive") +
                del +
                Bundle.getMessage("Ship") +
                del +
                Bundle.getMessage("Destination") +
                del +
                Bundle.getMessage("Track") +
                del +
                Bundle.getMessage("Pickup") +
                del +
                Bundle.getMessage("Count") +
                del +
                Bundle.getMessage("Wait") +
                del +
                Bundle.getMessage("Hits") +
                del +
                Bundle.getMessage("Comment");

        fileOut.println(header);

        List<Schedule> schedules = InstanceManager.getDefault(ScheduleManager.class).getSchedulesByNameList();
        for (Schedule schedule : schedules) {
            for (ScheduleItem scheduleItem : schedule.getItemsBySequenceList()) {

                String line = ESC +
                        schedule.getName() +
                        ESC +
                        del +
                        scheduleItem.getId() +
                        del +
                        ESC +
                        scheduleItem.getTypeName() +
                        ESC +
                        del +
                        scheduleItem.getRandom() +
                        del +
                        ESC +
                        scheduleItem.getSetoutTrainScheduleName() +
                        ESC +
                        del +
                        ESC +
                        scheduleItem.getRoadName() +
                        ESC +
                        del +
                        ESC +
                        scheduleItem.getReceiveLoadName() +
                        ESC +
                        del +
                        ESC +
                        scheduleItem.getShipLoadName() +
                        ESC +
                        del +
                        ESC +
                        scheduleItem.getDestinationName() +
                        ESC +
                        del +
                        ESC +
                        scheduleItem.getDestinationTrackName() +
                        ESC +
                        del +
                        ESC +
                        scheduleItem.getPickupTrainScheduleName() +
                        ESC +
                        del +
                        scheduleItem.getCount() +
                        del +
                        scheduleItem.getWait() +
                        del +
                        scheduleItem.getHits() +
                        del +
                        ESC +
                        schedule.getComment() +
                        ESC;

                fileOut.println(line);
            }

        }
        fileOut.flush();
        fileOut.close();
        log.info("Exported " + schedules.size() + " schedules to file " + defaultOperationsFilename());
        JOptionPane.showMessageDialog(null,
                MessageFormat.format(Bundle.getMessage("ExportedSchedulesToFile"), new Object[]{
                        schedules.size(), defaultOperationsFilename()}),
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

    private static String operationsFileName = "ExportOperationsSchedules.csv"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(ExportLocations.class);

}
