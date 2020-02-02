package jmri.jmrit.operations.routes.tools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import javax.swing.JOptionPane;

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

    static final String ESC = "\""; // escape character NOI18N
    private String del = ","; // delimiter

    public ExportRoutes() {

    }

    public void setDeliminter(String delimiter) {
        del = delimiter;
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

        int count = 0;
        try (PrintWriter fileOut = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)), true)) {

            loadHeader(fileOut);

            for (Route route : InstanceManager.getDefault(RouteManager.class).getRoutesByNameList()) {
                count++;
                String line = route.getName() +
                        del +
                        del +
                        del +
                        del +
                        del +
                        del +
                        del +
                        del +
                        del +
                        del +
                        del +
                        del +
                        del +
                        ESC +
                        route.getComment() +
                        ESC;
                fileOut.println(line);
                for (RouteLocation rl : route.getLocationsBySequenceList()) {
                    line = del +
                            rl.getLocation().getName() +
                            del +
                            rl.getTrainDirectionString() +
                            del +
                            rl.getMaxCarMoves() +
                            del +
                            rl.getRandomControl() +
                            del +
                            (rl.isPickUpAllowed() ? Bundle.getMessage("yes") : Bundle.getMessage("no")) +
                            del +
                            (rl.isDropAllowed() ? Bundle.getMessage("yes") : Bundle.getMessage("no")) +
                            del +
                            rl.getWait() +
                            del +
                            rl.getFormatedDepartureTime() +
                            del +
                            rl.getMaxTrainLength() +
                            del +
                            rl.getGrade() +
                            del +
                            rl.getTrainIconX() +
                            del +
                            rl.getTrainIconY() +
                            del +
                            ESC +
                            rl.getComment().replace("\n", "<LF>") +
                            ESC +
                            del +
                            rl.getCommentTextColor();
                    fileOut.println(line);
                }
            }

            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ExportedRoutesToFile"),
                            new Object[]{count, defaultOperationsFilename()}),
                    Bundle.getMessage("ExportComplete"), JOptionPane.INFORMATION_MESSAGE);

            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            log.error("Can not open export Routes CSV file: " + file.getName());
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ExportedRoutesToFile"),
                            new Object[]{0, defaultOperationsFilename()}),
                    Bundle.getMessage("ExportFailed"), JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    private void loadHeader(PrintWriter fileOut) {
        String line = Bundle.getMessage(
                "Route") +
                " " +
                Bundle.getMessage("Name") +
                del +
                Bundle.getMessage("Location") +
                del +
                Bundle.getMessage("TrainDirection") +
                del +
                Bundle.getMessage("Moves") +
                del +
                Bundle.getMessage("Random") +
                del +
                Bundle.getMessage("Pickups") +
                del +
                Bundle.getMessage("Drops") +
                del +
                Bundle.getMessage("Wait") +
                del +
                Bundle.getMessage("DepartTime") +
                del +
                Bundle.getMessage("MaxLength") +
                del +
                Bundle.getMessage("Grade") +
                del +
                Bundle.getMessage("X") +
                del +
                Bundle.getMessage("Y") +
                del +
                Bundle.getMessage("Comment") +
                del +
                Bundle.getMessage("Comment") +
                " " +
                Bundle.getMessage("TextColor");
        fileOut.println(line);

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

    private static String operationsFileName = "ExportOperationsRoutes.csv"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(ExportRoutes.class);

}
