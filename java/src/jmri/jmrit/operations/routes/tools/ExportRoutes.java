package jmri.jmrit.operations.routes.tools;

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
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.OperationsSetupXml;

/**
 * Export Routes to CSV file
 */
public class ExportRoutes extends XmlFile {

    public ExportRoutes() {
        // nothing to do
    }

    public void writeOperationsRoutesFile() {
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
        // This is taken in large part from "Java and XML" page 368
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }

        int count = 0;
        try (CSVPrinter fileOut = new CSVPrinter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)),
                CSVFormat.DEFAULT)) {

            loadHeader(fileOut);

            for (Route route : InstanceManager.getDefault(RouteManager.class).getRoutesByNameList()) {
                count++;
                fileOut.printRecord(route.getName(),
                        "", // NOI18N
                        route.getComment());
                for (RouteLocation rl : route.getLocationsBySequenceList()) {
                    fileOut.printRecord("", // NOI18N
                            rl.getLocation().getName(),
                            rl.getTrainDirectionString(),
                            rl.getMaxCarMoves(),
                            rl.getRandomControl(),
                            rl.isPickUpAllowed() ? Bundle.getMessage("yes") : Bundle.getMessage("no"),
                            rl.isDropAllowed() ? Bundle.getMessage("yes") : Bundle.getMessage("no"),
                            rl.getWait(),
                            rl.getFormatedDepartureTime(),
                            rl.getMaxTrainLength(),
                            rl.getGrade(),
                            rl.getTrainIconX(),
                            rl.getTrainIconY(),
                            rl.getComment().replace("\n", "<LF>"),
                            rl.getCommentTextColor());
                }
            }

            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ExportedRoutesToFile"),
                            new Object[]{count, defaultOperationsFilename()}),
                    Bundle.getMessage("ExportComplete"), JOptionPane.INFORMATION_MESSAGE);

            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            log.error("Can not open export Routes CSV file: {}", file.getName());
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ExportedRoutesToFile"),
                            new Object[]{0, defaultOperationsFilename()}),
                    Bundle.getMessage("ExportFailed"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadHeader(CSVPrinter fileOut) throws IOException {
        fileOut.printRecord(Bundle.getMessage("Route"),
                Bundle.getMessage("Location"),
                Bundle.getMessage("TrainDirection"),
                Bundle.getMessage("Moves"),
                Bundle.getMessage("Random"),
                Bundle.getMessage("Pickups"),
                Bundle.getMessage("Drops"),
                Bundle.getMessage("Wait"),
                Bundle.getMessage("DepartTime"),
                Bundle.getMessage("MaxLength"),
                Bundle.getMessage("Grade"),
                Bundle.getMessage("X"),
                Bundle.getMessage("Y"),
                Bundle.getMessage("Comment"),
                Bundle.getMessage("TextColor"));
    }

    public File getExportFile() {
        return findFile(defaultOperationsFilename());
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

    private static String operationsFileName = "ExportOperationsRoutes.csv"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(ExportRoutes.class);

}
