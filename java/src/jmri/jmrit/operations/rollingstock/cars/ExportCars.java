// ExportCars.java
package jmri.jmrit.operations.rollingstock.cars;

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
 * Exports the car roster into a comma delimitated file (CSV).
 * Only the cars shown in the CarsTableTrame window are exported.
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2011, 2016
 * @version $Revision$
 *
 */
public class ExportCars extends XmlFile {

    static final String ESC = "\""; // escape character NOI18N
    private String del = ","; // delimiter
    
    CarsTableFrame _carsTableFrame;

    public ExportCars(CarsTableFrame carsTableFrame) {
        _carsTableFrame = carsTableFrame;
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
                    true); // NOI18N
        } catch (IOException e) {
            log.error("Can not open export cars CSV file: {}", file.getName());
            return;
        }
        
        List<RollingStock> carList = _carsTableFrame.carsTableModel.getSelectedCarList();
//        List<RollingStock> carList = CarManager.instance().getByNumberList();
        
        String line = "";
        // check for delimiter in the following car fields
        String carType;
        String carLocationName;
        String carTrackName;
        // assume delimiter in the value field
        String value;
        String comment;
        // create header
        String header = Bundle.getMessage("Number") + del + Bundle.getMessage("Road") + del + Bundle.getMessage("Type")
                + del + Bundle.getMessage("Length") + del + Bundle.getMessage("Weight") + del
                + Bundle.getMessage("Color") + del + Bundle.getMessage("Owner") + del + Bundle.getMessage("Built")
                + del + Bundle.getMessage("Location") + del + "-" + del + Bundle.getMessage("Track") + del
                + Bundle.getMessage("Load") + del + Bundle.getMessage("Kernel") 
                + del + Bundle.getMessage("Moves") + del + Setup.getValueLabel() + del + Bundle.getMessage("Comment")
                + del + Bundle.getMessage("Miscellaneous");
        fileOut.println(header);

        // store car number, road, type, length, weight, color, owner, built date, location and track
        for (RollingStock rs : carList) {
            Car car = (Car) rs;
            carType = car.getTypeName();
            if (carType.contains(del)) {
//				log.debug("Car (" + car.getRoadName() + " " + car.getNumber() + ") has delimiter in type field: "
//						+ carType); // NOI18N
                carType = ESC + car.getTypeName() + ESC;
            }
            carLocationName = car.getLocationName();
            if (carLocationName.contains(del)) {
//				log.debug("Car (" + car.getRoadName() + " " + car.getNumber() + ") has delimiter in location field: "
//						+ carLocationName); // NOI18N
                carLocationName = ESC + car.getLocationName() + ESC;
            }
            carTrackName = car.getTrackName();
            if (carTrackName.contains(del)) {
//				log.debug("Car (" + car.getRoadName() + " " + car.getNumber() + ") has delimiter in track field: "
//						+ carTrackName); // NOI18N
                carTrackName = ESC + car.getTrackName() + ESC;
            }
            // only export value field if value has been set.
            value = "";
            if (!car.getValue().equals(Car.NONE)) {
                value = ESC + car.getValue() + ESC;
            }
            comment = "";
            if (!car.getComment().equals(Car.NONE)) {
                comment = ESC + car.getComment() + ESC;
            }
            line = car.getNumber() + del + car.getRoadName() + del + carType + del + car.getLength() + del
                    + car.getWeight() + del + car.getColor() + del + car.getOwner() + del + car.getBuilt() + del
                    + carLocationName + ",-," + carTrackName + del + car.getLoadName() + del + car.getKernelName()
                    + del + car.getMoves() + del + value + del + comment + del 
                    + (car.isOutOfService()?Bundle.getMessage("OutOfService"):""); // NOI18N
            fileOut.println(line);
        }
        fileOut.flush();
        fileOut.close();
        log.info("Exported " + carList.size() + " cars to file " + defaultOperationsFilename());
        JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ExportedCarsToFile"), new Object[]{
            carList.size(), defaultOperationsFilename()}), Bundle.getMessage("ExportComplete"),
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

    private static String OperationsFileName = "ExportOperationsCarRoster.csv"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(ExportCars.class.getName());

}
