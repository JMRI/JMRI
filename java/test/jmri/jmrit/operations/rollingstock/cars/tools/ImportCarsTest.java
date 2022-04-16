package jmri.jmrit.operations.rollingstock.cars.tools;

import java.io.File;
import java.util.List;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@Timeout(60)
public class ImportCarsTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ImportCars t = new ImportCars();
        Assert.assertNotNull("exists", t);
    }

    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testReadFile() {

        CarManager cm = InstanceManager.getDefault(CarManager.class);
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        // export cars to create file
        List<Car> carList = cm.getByIdList();
        ExportCars exportCars = new ExportCars(carList);
        Assert.assertNotNull("exists", exportCars);

        // should cause export complete dialog to appear
        Thread exportThread = new Thread(exportCars::writeOperationsCarFile);
        exportThread.setName("Export Cars"); // NOI18N
        exportThread.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return exportThread.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), Bundle.getMessage("ButtonOK"));

        try {
            exportThread.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        File file = new File(ExportCars.defaultOperationsFilename());
        Assert.assertTrue("Confirm file creation", file.exists());

        // delete all cars
        cm.deleteAll();
        Assert.assertEquals("cars", 0, cm.getNumEntries());

        // do import
        Thread importThread = new ImportCars() {
            @Override
            protected File getFile() {
                // replace JFileChooser with fixed file to avoid threading issues
                return new File(OperationsXml.getFileLocation() +
                        OperationsXml.getOperationsDirectoryName() +
                        File.separator +
                        ExportCars.getOperationsFileName());
            }
        };
        importThread.setName("Test Import Cars"); // NOI18N
        importThread.start();
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return importThread.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        // wait for import complete and acknowledge
        JemmyUtil.pressDialogButton(Bundle.getMessage("SuccessfulImport"), Bundle.getMessage("ButtonOK"));

        try {
            importThread.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        // confirm import successful
        Assert.assertEquals("cars", 9, cm.getNumEntries());

    }

    // private final static Logger log = LoggerFactory.getLogger(ImportCarsTest.class);

}
