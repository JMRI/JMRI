package jmri.jmrit.operations.rollingstock.engines.tools;

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
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;

/**
 * Exports the Engine roster into a comma delimited file (CSV). Order stored:
 * Number, Road, Model, Length, Owner, Built, Location, -, Track, Consist,
 * Moves, Last, Value, HP, Weight, Type, Comment, Misc.
 *
 * @author Daniel Boudreau Copyright (C) 2010
 *
 */
public class ExportEngines extends XmlFile {

    protected static final String LOCATION_TRACK_SEPARATOR = "-";

    public ExportEngines() {
        // nothing to do
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
        } catch (IOException e) {
            log.error("Exception while writing the new CSV operations file, may not be complete", e);
        }
    }

    public void writeFile(String name) {
        log.debug("writeFile {}", name);
        // This is taken in large part from "Java and XML" page 368
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }

        try (CSVPrinter fileOut = new CSVPrinter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)),
                CSVFormat.DEFAULT)) {

            EngineManager manager = InstanceManager.getDefault(EngineManager.class);
            List<Engine> engineList = manager.getByNumberList();

            // create header
            fileOut.printRecord(Bundle.getMessage("Number"),
                    Bundle.getMessage("Road"),
                    Bundle.getMessage("Model"),
                    Bundle.getMessage("Length"),
                    Bundle.getMessage("Owner"),
                    Bundle.getMessage("Built"),
                    Bundle.getMessage("Location"),
                    LOCATION_TRACK_SEPARATOR,
                    Bundle.getMessage("Track"),
                    Bundle.getMessage("Consist"),
                    Bundle.getMessage("Moves"),
                    Bundle.getMessage("Last"),
                    Setup.getValueLabel(),
                    Bundle.getMessage("HP"),
                    Bundle.getMessage("WeightTons"),
                    Bundle.getMessage("Type"),
                    Bundle.getMessage("Comment"),
                    Bundle.getMessage("Miscellaneous"));

            // store engine number, road, model, length, owner, built date, location - track
            for (Engine engine : engineList) {
                fileOut.printRecord(engine.getNumber(),
                        engine.getRoadName(),
                        engine.getModel(),
                        engine.getLength(),
                        engine.getOwner(),
                        engine.getBuilt(),
                        engine.getLocationName(),
                        LOCATION_TRACK_SEPARATOR,
                        engine.getTrackName(),
                        engine.getConsistName(),
                        engine.getMoves(),
                        engine.getLastDate(),
                        engine.getValue(),
                        engine.getHp(),
                        engine.getWeightTons(),
                        engine.getTypeName(),
                        engine.getComment(),
                        engine.isOutOfService() ? Bundle.getMessage("OutOfService") : "");
            }
            fileOut.flush();
            fileOut.close();
            log.info("Exported {} engines to file {}", engineList.size(), defaultOperationsFilename());
            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ExportedEnginesToFile"),
                    new Object[]{engineList.size(), defaultOperationsFilename()}), Bundle.getMessage("ExportComplete"),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            log.error("Can not open export engines CSV file: {}", file.getName());
            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ExportedEnginesToFile"),
                    new Object[]{0, defaultOperationsFilename()}), Bundle.getMessage("ExportFailed"),
                    JOptionPane.ERROR_MESSAGE);
        }
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

    private static String operationsFileName = "ExportOperationsEngineRoster.csv"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(ExportEngines.class);

}
