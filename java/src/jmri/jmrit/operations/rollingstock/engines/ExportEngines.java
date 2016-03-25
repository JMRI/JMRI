// ExportEngines.java
package jmri.jmrit.operations.rollingstock.engines;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.JOptionPane;
import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exports the Engine roster into a comma delimitated file (CSV).
 *
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 *
 */
public class ExportEngines extends XmlFile {

    static final String ESC = "\""; // escape character NOI18N
    private String del = ","; // delimiter

    public ExportEngines() {

    }

    public void setDeliminter(String delimiter) {
        del = delimiter;
    }

    /**
     * Store the all of the operation Engine objects in the default place,
     * including making a backup if needed
     */
    public void writeOperationsEngineFile() {
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

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "EngineManager only provides Engine Objects")
    public void writeFile(String name) {
        if (log.isDebugEnabled()) {
            log.debug("writeFile {}", name);
        }
        // This is taken in large part from "Java and XML" page 368
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }

        PrintWriter fileOut = null;

        try {
            fileOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")), // NOI18N
                    true);
        } catch (IOException e) {
            log.error("Can not open export engines CSV file: " + file.getName());
            return;
        }

        EngineManager manager = EngineManager.instance();
        List<RollingStock> engineList = manager.getByNumberList();
        String line = "";
        // check for delimiter in the following Engine fields
        String engineModel;
        String engineLocationName;
        String engineTrackName;
        // assume delimiter in the value field
        String value;
        String comment;

        // create header
        String header = Bundle.getMessage("Number") + del + Bundle.getMessage("Road") + del
                + Bundle.getMessage("Model") + del + Bundle.getMessage("Length") + del + Bundle.getMessage("Owner")
                + del + Bundle.getMessage("Built") + del + Bundle.getMessage("Location") + del + "-" + del
                + Bundle.getMessage("Track") + del + Bundle.getMessage("Moves") + del + Setup.getValueLabel()
                 + del + Bundle.getMessage("Comment") + del  + Bundle.getMessage("Miscellaneous");
        fileOut.println(header);

        // store engine number, road, model, length, owner, built date, location and track
        for (RollingStock rs : engineList) {
            Engine engine = (Engine) rs;
            engineModel = engine.getModel();
            if (engineModel.contains(del)) {
                engineModel = ESC + engine.getModel() + ESC;
            }
            engineLocationName = engine.getLocationName();
            if (engineLocationName.contains(del)) {
                engineLocationName = ESC + engine.getLocationName() + ESC;
            }
            engineTrackName = engine.getTrackName();
            if (engineTrackName.contains(del)) {
                engineTrackName = ESC + engine.getTrackName() + ESC;
            }
            value = engine.getValue();
            if (value.contains(del)) {
                value = ESC + engine.getValue() + ESC;
            }
            comment = engine.getComment();
            if (comment.contains(del)) {
                comment = ESC + engine.getComment() + ESC;
            }
            line = engine.getNumber() + del + engine.getRoadName() + del + engineModel + del + engine.getLength() + del
                    + engine.getOwner() + del + engine.getBuilt() + del + engineLocationName + ",-," + engineTrackName // NOI18N
                    + del + engine.getMoves() + del + value + del + comment + del + (engine.isOutOfService()?Bundle.getMessage("OutOfService"):"");
            fileOut.println(line);
        }
        fileOut.flush();
        fileOut.close();
        log.info("Exported " + engineList.size() + " engines to file " + defaultOperationsFilename());
        JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ExportedEnginesToFile"),
                new Object[]{engineList.size(), defaultOperationsFilename()}), Bundle.getMessage("ExportComplete"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Operation files always use the same directory
    public static String defaultOperationsFilename() {
        return OperationsSetupXml.getFileLocation() + OperationsSetupXml.getOperationsDirectoryName() + File.separator
                + getOperationsFileName();
    }

    public static void setOperationsFileName(String name) {
        OperationsFileName = name;
    }

    public static String getOperationsFileName() {
        return OperationsFileName;
    }

    private static String OperationsFileName = "ExportOperationsEngineRoster.csv"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(ExportEngines.class.getName());

}
