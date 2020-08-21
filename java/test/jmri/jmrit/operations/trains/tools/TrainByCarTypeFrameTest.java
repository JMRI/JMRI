package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainByCarTypeFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainByCarTypeFrame t = new TrainByCarTypeFrame((Train) null);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testSelection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        Train train = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        Assert.assertNotNull("exists", train);

        TrainByCarTypeFrame f = new TrainByCarTypeFrame(train);
        Assert.assertNotNull("exists", f);

        f.typeComboBox.setSelectedIndex(1);
        f.carsComboBox.setSelectedIndex(1);

        JUnitUtil.dispose(f);

    }
    
    @Test
    public void testCarSelection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        Train train1 = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        Assert.assertTrue(train1.build());
        Assert.assertNotNull("exists", train1);
        Assert.assertTrue(train1.isBuilt());
        
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        List<Car> cars = cmanager.getByTrainList(train1);

        TrainByCarTypeFrame f = new TrainByCarTypeFrame(cars.get(0));
        Assert.assertNotNull("exists", f);
        
        // 1st car in list is a Boxcar
        Assert.assertEquals("car selected", cars.get(0), f.carsComboBox.getSelectedItem());
        Assert.assertEquals("car type", cars.get(0).getTypeName(), f.typeComboBox.getSelectedItem());

        JUnitUtil.dispose(f);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainByCarTypeFrameTest.class);

}
