package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.netbeans.jemmy.QueueTool;

import javax.swing.*;

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
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
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
        toggleCheckBoxThenClickSave(f,f.outOfServiceCheckBox);
        JUnitUtil.waitFor(() -> {
            return c3.isOutOfService();
            }, "Out of service");
        Assert.assertFalse("Location unknown", c3.isLocationUnknown());

        // change car's status
        toggleCheckBoxThenClickSave(f,f.outOfServiceCheckBox);
        JUnitUtil.waitFor(() -> {
            return !c3.isOutOfService();
        }, "Not Out of service");
        Assert.assertFalse("Location unknown", c3.isLocationUnknown());

        // change car's status
        toggleCheckBoxThenClickSave(f,f.locationUnknownCheckBox);
        // location unknown checkbox also causes the car to be out of service
        JUnitUtil.waitFor(() -> {
            return c3.isOutOfService();
        }, "Out of service Again");
        Assert.assertTrue("Location unknown", c3.isLocationUnknown());
        
        // change car's status
        toggleCheckBoxThenClickSave(f,f.locationUnknownCheckBox);
        // location unknown checkbox also causes the car to be out of service
        JUnitUtil.waitFor(() -> {
            return !c3.isOutOfService();
        }, "Not Out of service Again");
        Assert.assertFalse("Location unknown", c3.isLocationUnknown());

        JUnitUtil.dispose(f);

    }

    private void toggleCheckBoxThenClickSave(CarSetFrame frame, JCheckBox box){
        JemmyUtil.enterClickAndLeave(box);
        JemmyUtil.enterClickAndLeave(frame.saveButton);
        new QueueTool().waitEmpty(100);
    }
}
