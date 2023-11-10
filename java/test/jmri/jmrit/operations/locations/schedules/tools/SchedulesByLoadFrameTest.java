package jmri.jmrit.operations.locations.schedules.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.locations.schedules.Schedule;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class SchedulesByLoadFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JUnitOperationsUtil.initOperationsData();
        SchedulesByLoadFrame t = new SchedulesByLoadFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);

    }

    @Test
    public void testSchedulesByLoadFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // add schedules to a location
        JUnitOperationsUtil.initOperationsData();
        JUnitOperationsUtil.createSchedules();

        SchedulesByLoadFrame f = new SchedulesByLoadFrame();
        Assert.assertNotNull(f);
        
        JemmyUtil.enterClickAndLeave(f.allTypesCheckBox);
        JemmyUtil.enterClickAndLeave(f.allLoadsCheckBox);
        
        // update car loads for Boxcar to eliminate warning
        CarLoads carLoads = InstanceManager.getDefault(CarLoads.class);
        carLoads.addName("Boxcar", "Empty");
        carLoads.addName("Boxcar", "Metal");
        carLoads.addName("Flat", "Metal");
        carLoads.addName("Flat", "Junk");
        
        // modify schedule
        ScheduleManager scheduleManager = InstanceManager.getDefault(ScheduleManager.class);
        Schedule schedule = scheduleManager.getScheduleByName("Test Schedule");
        schedule.addItem("Boxcar");
        JemmyUtil.enterClickAndLeave(f.allLoadsCheckBox);
        
        // don't allow the "E" load
        Location location = InstanceManager.getDefault(LocationManager.class).getLocationByName("North Industries");
        Track track = location.getTrackByName("Test Spur 2", null);
        track.addLoadName("E");
        track.setLoadOption(Track.EXCLUDE_LOADS);
        
        // Add a car type
        Assert.assertEquals("Number of types", 3, f.typesComboBox.getItemCount());
        CarTypes carTypes = InstanceManager.getDefault(CarTypes.class);
        carTypes.addName("NEWBOXCAR");
        Assert.assertEquals("Number of types", 4, f.typesComboBox.getItemCount());
        
        // TODO improve test
        JUnitUtil.dispose(f);
    }

    // private final static Logger log =
    // LoggerFactory.getLogger(SchedulesByLoadFrameTest.class);

}
