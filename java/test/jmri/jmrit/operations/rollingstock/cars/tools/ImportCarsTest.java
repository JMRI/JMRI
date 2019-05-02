package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.junit.rules.*;
import jmri.util.swing.JemmyUtil;

import org.junit.*;
import org.junit.rules.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ImportCarsTest extends OperationsTestCase {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(60); // 60 second timeout for methods in this test class.

    @Rule
    public RetryRule retryRule = new RetryRule(2); // allow 2 retries

    @Test
    public void testCTor() {
        ImportCars t = new ImportCars();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testReadFile() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        CarManager emanager = InstanceManager.getDefault(CarManager.class);
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        Assert.assertEquals("cars", 9, emanager.getNumEntries());

        // export cars to create file
        JUnitOperationsUtil.initOperationsData();
        List<Car> carList = InstanceManager.getDefault(CarManager.class).getByIdList();
        ExportCars exportCars = new ExportCars(carList);
        Assert.assertNotNull("exists", exportCars);

        // should cause export complete dialog to appear
        Thread export = new Thread(new Runnable() {
            @Override
            public void run() {
                exportCars.writeOperationsCarFile();
            }
        });
        export.setName("Export Cars"); // NOI18N
        export.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return export.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), Bundle.getMessage("ButtonOK"));
        
        try {
            export.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        java.io.File file = new java.io.File(ExportCars.defaultOperationsFilename());
        Assert.assertTrue("Confirm file creation", file.exists());

        // delete all cars
        emanager.deleteAll();
        Assert.assertEquals("cars", 0, emanager.getNumEntries());

        // do import

        Thread mb = new ImportCars(){
            @Override
            protected File getFile() {
                // replace JFileChooser with fixed file to avoid threading issues
                return new File(OperationsXml.getFileLocation()+OperationsXml.getOperationsDirectoryName() + File.separator + ExportCars.getOperationsFileName());
            }
        };
        mb.setName("Test Import Cars"); // NOI18N
        mb.start();

        // wait for import complete and acknowledge
        JemmyUtil.pressDialogButton(Bundle.getMessage("SuccessfulImport"), Bundle.getMessage("ButtonOK"));

        try {
            mb.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        // confirm import successful
        Assert.assertEquals("cars", 9, emanager.getNumEntries());
    }

    // private final static Logger log = LoggerFactory.getLogger(ImportCarsTest.class);

}
