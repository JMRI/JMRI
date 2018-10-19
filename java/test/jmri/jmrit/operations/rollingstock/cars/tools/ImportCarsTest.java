package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.List;
import java.util.regex.Pattern;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFileChooserOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ImportCarsTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ImportCars t = new ImportCars();
        Assert.assertNotNull("exists",t);
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

        java.io.File file = new java.io.File(ExportCars.defaultOperationsFilename());
        Assert.assertTrue("Confirm file creation", file.exists());

        // delete all cars
        emanager.deleteAll();
        Assert.assertEquals("cars", 0, emanager.getNumEntries());
        
        // do import
        
        Thread mb = new ImportCars();
        mb.setName("Test Import Cars"); // NOI18N
        mb.start();
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return mb.getState().equals(Thread.State.WAITING);
        }, "wait for file chooser");
        
        // opens file chooser path "operations" "JUnitTest"
        JFileChooserOperator fco = new JFileChooserOperator();
        String[] path = OperationsXml.getOperationsDirectoryName().split(Pattern.quote(File.separator));  
        fco.chooseFile(path[0]);
        fco.chooseFile(path[1]);
        fco.chooseFile(ExportCars.getOperationsFileName());
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return mb.getState().equals(Thread.State.WAITING);
        }, "wait for dialog");
        
        // import complete 
        JemmyUtil.pressDialogButton(Bundle.getMessage("SuccessfulImport"), Bundle.getMessage("ButtonOK"));
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return mb.getState().equals(Thread.State.TERMINATED);
        }, "wait for import complete");
        
        // confirm import successful
        Assert.assertEquals("cars", 9, emanager.getNumEntries());
    }


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ImportCarsTest.class);

}
