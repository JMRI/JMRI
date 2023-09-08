package jmri.jmrit.operations.locations.schedules.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class SchedulesAndStagingFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JUnitOperationsUtil.initOperationsData();
        SchedulesAndStagingFrame t = new SchedulesAndStagingFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);

    }

    @Test
    public void testSchedulesAndStagingFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // add schedules to a location
        JUnitOperationsUtil.initOperationsData();
        JUnitOperationsUtil.createSchedules();

        SchedulesAndStagingFrame f = new SchedulesAndStagingFrame();
        Assert.assertNotNull(f);
        
        // check loads
        Assert.assertEquals("Number of loads", 2, f.loadsComboBox.getItemCount());
        CarLoads carLoads = InstanceManager.getDefault(CarLoads.class);
        carLoads.addName("Boxcar", "Empty");
        Assert.assertEquals("Number of loads", 3, f.loadsComboBox.getItemCount());
        
        // Add a car type
        Assert.assertEquals("Number of types", 3, f.typesComboBox.getItemCount());
        CarTypes carTypes = InstanceManager.getDefault(CarTypes.class);
        carTypes.addName("NEWBOXCAR");
        Assert.assertEquals("Number of types", 4, f.typesComboBox.getItemCount());
        
        // enable custom load generation out of staging
        Location location = InstanceManager.getDefault(LocationManager.class).getLocationByName("North End Staging");
        Track staging = location.getTrackByName("North End 2", null);
        staging.setAddCustomLoadsAnySpurEnabled(true);
        
        JemmyUtil.enterClickAndLeave(f.allLoadsCheckBox);
        
        // TODO improve test
        JUnitUtil.dispose(f);
    }

    // private final static Logger log =
    // LoggerFactory.getLogger(SchedulesByLoadFrameTest.class);

}
