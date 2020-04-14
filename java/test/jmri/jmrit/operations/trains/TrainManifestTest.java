package jmri.jmrit.operations.trains;

import java.io.BufferedReader;
import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.util.JUnitOperationsUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainManifestTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        JUnitOperationsUtil.initOperationsData();
        Train train1 = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        TrainManifest tm = new TrainManifest(train1);
        Assert.assertNotNull("exists", tm);
    }
    
    @Test
    public void testAddCarsLocationUnknown() {
        JUnitOperationsUtil.initOperationsData();
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        Car car = cmanager.getByRoadAndNumber("CP", "777");
        car.setLocationUnknown(true);
        Train train1 = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        TrainManifest tm = new TrainManifest(train1);
        Assert.assertNotNull("exists", tm);
        
        File file = InstanceManager.getDefault(TrainManagerXml.class).getTrainManifestFile(train1.getName());
        
        BufferedReader in = JUnitOperationsUtil.getBufferedReader(file);
        Assert.assertEquals("confirm number of lines in manifest", 15, in.lines().count());
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainManifestTest.class);
}
