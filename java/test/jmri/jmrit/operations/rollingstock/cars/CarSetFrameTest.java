package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * Tests for the Operations CarSetFrame class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class CarSetFrameTest extends OperationsTestCase {

    @Test
    public void testCarSetFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();

        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        f.initComponents();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        f.loadCar(c3);

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarSetFrameSaveButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();

        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        f.initComponents();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        f.loadCar(c3);

        // check defaults
        Assert.assertFalse("Out of service", c3.isOutOfService());
        Assert.assertFalse("Location unknown", c3.isLocationUnknown());

        // change car's status
        JemmyUtil.enterClickAndLeave(f.outOfServiceCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertTrue("Out of service", c3.isOutOfService());
        Assert.assertFalse("Location unknown", c3.isLocationUnknown());

        // change car's status
        JemmyUtil.enterClickAndLeave(f.outOfServiceCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertFalse("Out of service", c3.isOutOfService());
        Assert.assertFalse("Location unknown", c3.isLocationUnknown());

        // change car's status
        JemmyUtil.enterClickAndLeave(f.locationUnknownCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);
        // location unknown checkbox also causes the car to be out of service
        Assert.assertTrue("Out of service", c3.isOutOfService());
        Assert.assertTrue("Location unknown", c3.isLocationUnknown());
        
        // change car's status
        JemmyUtil.enterClickAndLeave(f.locationUnknownCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);
        // location unknown checkbox also causes the car to be out of service
        Assert.assertFalse("Out of service", c3.isOutOfService());
        Assert.assertFalse("Location unknown", c3.isLocationUnknown());

        JUnitUtil.dispose(f);
    }
}
