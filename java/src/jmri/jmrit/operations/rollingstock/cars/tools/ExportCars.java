package jmri.jmrit.operations.rollingstock.cars.tools;

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
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exports the car roster into a comma delimitated file (CSV).
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2011, 2016
 *
 */
public class ExportCars extends XmlFile {

    static final String ESC = "\""; // escape character NOI18N
    private String del = ","; // delimiter

    List<Car> _carList;

    public ExportCars(List<Car> carList) {
        _carList = carList;
    }

    public void setDeliminter(String delimiter) {
        del = delimiter;
    }

    /**
     * Store the all of the operation car objects in the default place,
     * including making a backup if needed
     */
    public void writeOperationsCarFile() {
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
            log.error("Can not open export cars CSV file: {}", file.getName());
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ExportedCarsToFile"), new Object[]{
                            0, defaultOperationsFilename()}),
                    Bundle.getMessage("ExportFailed"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // create header
        String header = Bundle.getMessage("Number") +
                del +
                Bundle.getMessage("Road") +
                del +
                Bundle.getMessage("Type") +
                del +
                Bundle.getMessage("Length") +
                del +
                Bundle.getMessage("Weight") +
                del +
                Bundle.getMessage("Color") +
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
                Bundle.getMessage("Load") +
                del +
                Bundle.getMessage("Kernel") +
                del +
                Bundle.getMessage("Moves") +
                del +
                Setup.getValueLabel() +
                del +
                Bundle.getMessage("Comment") +
                del +
                Bundle.getMessage("Miscellaneous") +
                del +
                Bundle.getMessage("Extensions") +
                del +
                Bundle.getMessage("Wait") +
                del +
                Bundle.getMessage("Pickup") +
                del +
                Bundle.getMessage("Last") +
                del +
                Bundle.getMessage("RWELocation") +
                del +
                "-" +
                del +
                Bundle.getMessage("Track") +
                del +
                Bundle.getMessage("RWELoad") +
                del +
                Bundle.getMessage("Train") +
                del +
                Bundle.getMessage("Destination") +
                del +
                "-" +
                del +
                Bundle.getMessage("Track") +
                del +
                Bundle.getMessage("FinalDestination") +
                del +
                "-" +
                del +
                Bundle.getMessage("Track");
        fileOut.println(header);

        // store car number, road, type, length, weight, color, owner, built date, location and track name
        for (Car car : _carList) {
            String line = 
                    ESC +
                    car.getNumber() +
                    ESC +
                    del +
                    ESC +
                    car.getRoadName() +
                    ESC +
                    del +
                    ESC +
                    car.getTypeName() +
                    ESC +
                    del +
                    car.getLength() +
                    del +
                    car.getWeight() +
                    del +
                    ESC +
                    car.getColor() +
                    ESC +
                    del +
                    ESC +
                    car.getOwner() +
                    ESC +
                    del +
                    ESC +
                    car.getBuilt() +
                    ESC +
                    del +
                    ESC +
                    car.getLocationName() +
                    ESC +
                    del +
                    "-" +
                    del +
                    ESC +
                    car.getTrackName() +
                    ESC +
                    del +
                    ESC +
                    car.getLoadName() +
                    ESC +
                    del +
                    ESC +
                    car.getKernelName() +
                    ESC +
                    del +
                    car.getMoves() +
                    del +
                    ESC +
                    car.getValue() +
                    ESC +
                    del +
                    ESC +
                    car.getComment() +
                    ESC +
                    del +
                    (car.isOutOfService() ? Bundle.getMessage("OutOfService") : "") +
                    del +
                    car.getTypeExtensions() +
                    del +
                    car.getWait() +
                    del +
                    ESC +
                    car.getPickupScheduleName() +
                    ESC +
                    del +
                    car.getLastDate() +
                    del +
                    ESC +
                    car.getReturnWhenEmptyDestinationName() +
                    ESC +
                    del +
                    "-" +
                    del +
                    ESC +
                    car.getReturnWhenEmptyDestTrackName() +
                    ESC +
                    del +
                    ESC +
                    car.getReturnWhenEmptyLoadName() +
                    ESC +
                    del +
                    ESC +
                    car.getTrainName() +
                    ESC +
                    del +
                    ESC +
                    car.getDestinationName() +
                    ESC +
                    del +
                    "-" +
                    del +
                    ESC +
                    car.getDestinationTrackName() +
                    ESC +
                    del +
                    ESC +
                    car.getFinalDestinationName() +
                    ESC +
                    del +
                    "-" +
                    del +
                    ESC +
                    car.getFinalDestinationTrackName() +
                    ESC;

            fileOut.println(line);
        }
        fileOut.flush();
        fileOut.close();
        log.info("Exported " + _carList.size() + " cars to file " + defaultOperationsFilename());
        JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ExportedCarsToFile"), new Object[]{
                _carList.size(), defaultOperationsFilename()}), Bundle.getMessage("ExportComplete"),
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

    private static String operationsFileName = "ExportOperationsCarRoster.csv"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(ExportCars.class);

}
