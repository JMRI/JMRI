package jmri.jmrit.operations.rollingstock.cars.tools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;

/**
 * Exports the car roster into a comma delimited file (CSV).
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2011, 2016
 *
 */
public class ExportCars extends XmlFile {

    protected static final String LOCATION_TRACK_SEPARATOR = "-";
    List<Car> _carList;

    public ExportCars(List<Car> carList) {
        _carList = carList;
    }

    /**
     * Create CSV file based on the car list.
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
        } catch (IOException e) {
            log.error("Exception while writing the new CSV operations file, may not be complete: {}", e);
        }
    }

    /**
     * Any changes to the column order should also be made to the ImportCars.java
     * file.
     * 
     * @param name file name
     */
    private void writeFile(String name) {
        log.debug("writeFile {}", name);
        // This is taken in large part from "Java and XML" page 368
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }

        try (CSVPrinter fileOut = new CSVPrinter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)),
                CSVFormat.DEFAULT)) {

            // create header
            fileOut.printRecord(Bundle.getMessage("Number"),
                    Bundle.getMessage("Road"),
                    Bundle.getMessage("Type"),
                    Bundle.getMessage("Length"),
                    Bundle.getMessage("Weight"),
                    Bundle.getMessage("Color"),
                    Bundle.getMessage("Owner"),
                    Bundle.getMessage("Built"),
                    Bundle.getMessage("Location"),
                    LOCATION_TRACK_SEPARATOR,
                    Bundle.getMessage("Track"),
                    Bundle.getMessage("Load"),
                    Bundle.getMessage("Kernel"),
                    Bundle.getMessage("Moves"),
                    Setup.getValueLabel(),
                    Bundle.getMessage("Comment"),
                    Bundle.getMessage("Miscellaneous"),
                    Bundle.getMessage("Extensions"),
                    Bundle.getMessage("Wait"),
                    Bundle.getMessage("Pickup"),
                    Bundle.getMessage("Last"),
                    Bundle.getMessage("RWELocation"),
                    LOCATION_TRACK_SEPARATOR,
                    Bundle.getMessage("Track"),
                    Bundle.getMessage("RWELoad"),
                    Bundle.getMessage("RWLLocation"),
                    LOCATION_TRACK_SEPARATOR,
                    Bundle.getMessage("Track"),
                    Bundle.getMessage("RWLLoad"),
                    Bundle.getMessage("Division"),
                    Bundle.getMessage("Train"),
                    Bundle.getMessage("Destination"),
                    LOCATION_TRACK_SEPARATOR,
                    Bundle.getMessage("Track"),
                    Bundle.getMessage("FinalDestination"),
                    LOCATION_TRACK_SEPARATOR,
                    Bundle.getMessage("Track"),
                    Bundle.getMessage( "RFID_Tag"));

            // store car attributes
            for (Car car : _carList) {
                fileOut.printRecord(car.getNumber(),
                        car.getRoadName(),
                        car.getTypeName(),
                        car.getLength(),
                        car.getWeight(),
                        car.getColor(),
                        car.getOwner(),
                        car.getBuilt(),
                        car.getLocationName(),
                        LOCATION_TRACK_SEPARATOR,
                        car.getTrackName(),
                        car.getLoadName(),
                        car.getKernelName(),
                        car.getMoves(),
                        car.getValue(),
                        car.getComment(),
                        car.isOutOfService() ? Bundle.getMessage("OutOfService") : "",
                        car.getTypeExtensions(),
                        car.getWait(),
                        car.getPickupScheduleName(),
                        car.getLastDate(),
                        car.getReturnWhenEmptyDestinationName(),
                        LOCATION_TRACK_SEPARATOR,
                        car.getReturnWhenEmptyDestTrackName(),
                        car.getReturnWhenEmptyLoadName(),
                        car.getReturnWhenLoadedDestinationName(),
                        LOCATION_TRACK_SEPARATOR,
                        car.getReturnWhenLoadedDestTrackName(),
                        car.getReturnWhenLoadedLoadName(),
                        car.getDivision(),
                        car.getTrainName(),
                        car.getDestinationName(),
                        LOCATION_TRACK_SEPARATOR,
                        car.getDestinationTrackName(),
                        car.getFinalDestinationName(),
                        LOCATION_TRACK_SEPARATOR,
                        car.getFinalDestinationTrackName(),
                        car.getRfid());
            }
            fileOut.flush();
            fileOut.close();
            log.info("Exported {} cars to file {}", _carList.size(), defaultOperationsFilename());
            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ExportedCarsToFile"), new Object[]{
                _carList.size(), defaultOperationsFilename()}), Bundle.getMessage("ExportComplete"),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            log.error("Can not open export cars CSV file: {}", file.getName());
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ExportedCarsToFile"),
                            new Object[] { 0, defaultOperationsFilename() }),
                    Bundle.getMessage("ExportFailed"), JOptionPane.ERROR_MESSAGE);
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

    private static String operationsFileName = "ExportOperationsCarRoster.csv"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(ExportCars.class);

}
