package jmri.jmrit.operations.rollingstock.engines.tools;

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
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exports the Engine roster into a comma delimitated file (CSV). Order stored:
 * Number, Road, Model, Length, Owner, Built, Location, -, Track, Consist,
 * Moves, Last, Value, HP, Weight, Type, Comment, Misc.
 *
 * @author Daniel Boudreau Copyright (C) 2010
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
                    true);
        } catch (IOException e) {
            log.error("Can not open export engines CSV file: " + file.getName());
            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ExportedEnginesToFile"),
                    new Object[]{0, defaultOperationsFilename()}), Bundle.getMessage("ExportFailed"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);
        List<Engine> engineList = manager.getByNumberList();

        // create header
        String header = Bundle.getMessage("Number") +
                del +
                Bundle.getMessage("Road") +
                del +
                Bundle.getMessage("Model") +
                del +
                Bundle.getMessage("Length") +
                del +
                Bundle.getMessage("Owner") +
                del +
                Bundle.getMessage("Built") +
                del +
                Bundle.getMessage("Location") +
                del +
                "-" +
                del +
                Bundle.getMessage("Track") +
                del +
                Bundle.getMessage("Consist") +
                del +
                Bundle.getMessage("Moves") +
                del +
                Bundle.getMessage("Last") +
                del +
                Setup.getValueLabel() +
                del +
                Bundle.getMessage("HP") +
                del +
                Bundle.getMessage("WeightTons") +
                del +
                Bundle.getMessage("Type") +
                del +
                Bundle.getMessage("Comment") +
                del +
                Bundle.getMessage("Miscellaneous");
        fileOut.println(header);

        // store engine number, road, model, length, owner, built date, location - track
        for (Engine engine : engineList) {
            String line = ESC +
                    engine.getNumber() +
                    ESC +
                    del +
                    ESC +
                    engine.getRoadName() +
                    ESC +
                    del +
                    ESC +
                    engine.getModel() +
                    ESC +
                    del +
                    engine.getLength() +
                    del +
                    ESC +
                    engine.getOwner() +
                    ESC +
                    del +
                    ESC +
                    engine.getBuilt() +
                    ESC +
                    del +
                    ESC +
                    engine.getLocationName() +
                    ESC +
                    del +
                    "-" +
                    del +
                    ESC +
                    engine.getTrackName() +
                    ESC +
                    del +
                    ESC +
                    engine.getConsistName() +
                    ESC +
                    del +
                    engine.getMoves() +
                    del +
                    engine.getLastDate() +
                    del +
                    engine.getValue() +
                    del +
                    engine.getHp() +
                    del +
                    engine.getWeightTons() +
                    del +
                    ESC +
                    engine.getTypeName() +
                    ESC +
                    del +
                    ESC +
                    engine.getComment() +
                    ESC +
                    del +
                    (engine.isOutOfService() ? Bundle.getMessage("OutOfService") : "");
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

    private static String operationsFileName = "ExportOperationsEngineRoster.csv"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(ExportEngines.class);

}
