package jmri.jmrit.operations.trains;

import java.io.*;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.util.JUnitOperationsUtil;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainManifestTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        JUnitOperationsUtil.initOperationsData();
        Train train1 = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        try {
            TrainManifest tm = new TrainManifest(train1);
            Assert.assertNotNull("exists", tm);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testAddCarsLocationUnknown() throws IOException {
        JUnitOperationsUtil.initOperationsData();
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        Car car = cmanager.getByRoadAndNumber("CP", "777");
        car.setLocationUnknown(true);
        Train train1 = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        try {
            TrainManifest tm = new TrainManifest(train1);
            Assert.assertNotNull("exists", tm);
        } catch (Exception e) {
            Assert.fail();
        }

        File file = InstanceManager.getDefault(TrainManagerXml.class).getTrainManifestFile(train1.getName());

        BufferedReader in = JUnitOperationsUtil.getBufferedReader(file);
        Assert.assertEquals("confirm number of lines in manifest", 18, in.lines().count());
        in.close();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainManifestTest.class);
}
