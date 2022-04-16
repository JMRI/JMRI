package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExportCarsTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = manager.getByIdList();
        ExportCars t = new ExportCars(carList);
        Assert.assertNotNull("exists", t);
    }
    
    @Test
    public void testCreateFile() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        
        // Improve test coverage by having an Out of Service car
        Car car = cm.newRS("SP", "1234");
        car.setOutOfService(true);
        List<Car> carList = cm.getByIdList();
        
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

    }

    // private final static Logger log = LoggerFactory.getLogger(ExportCarsTest.class);
}
