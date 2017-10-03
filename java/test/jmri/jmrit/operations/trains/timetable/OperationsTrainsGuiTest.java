package jmri.jmrit.operations.trains.timetable;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Operations Trains GUI class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class OperationsTrainsGuiTest extends OperationsSwingTestCase {

    @Test
    public void testTrainsScheduleTableFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsScheduleTableFrame f = new TrainsScheduleTableFrame();
        f.setVisible(true);

        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testTrainsScheduleEditFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsScheduleEditFrame f = new TrainsScheduleEditFrame();
        TrainScheduleManager tsm = InstanceManager.getDefault(TrainScheduleManager.class);
        Assert.assertNotNull("frame exists", f);
        f.setVisible(true);

        f.addTextBox.setText("A New Day");
        enterClickAndLeave(f.addButton);

        Assert.assertNotNull("Train schedule manager exists", tsm);
        Assert.assertNotNull("A new Day schedule exists", tsm.getScheduleByName("A New Day"));

        enterClickAndLeave(f.deleteButton);

        Assert.assertNull("A new Day schedule does not exist", tsm.getScheduleByName("A New Day"));

        enterClickAndLeave(f.replaceButton);

        Assert.assertNotNull("A new Day schedule exists", tsm.getScheduleByName("A New Day"));

        JUnitUtil.dispose(f);
    }

    // Ensure minimal setup for log4J
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        loadTrains();
    }

    private void loadTrains() {
        // Add some cars for the various tests in this suite
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        // add caboose to the roster
        Car c = cm.newCar("NH", "687");
        c.setCaboose(true);
        c = cm.newCar("CP", "435");
        c.setCaboose(true);

        // load engines
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Engine e1 = emanager.newEngine("E", "1");
        e1.setModel("GP40");
        Engine e2 = emanager.newEngine("E", "2");
        e2.setModel("GP40");
        Engine e3 = emanager.newEngine("UP", "3");
        e3.setModel("GP40");
        Engine e4 = emanager.newEngine("UP", "4");
        e4.setModel("FT");

        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        // turn off build fail messages
        tmanager.setBuildMessagesEnabled(true);
        // turn off print preview
        tmanager.setPrintPreviewEnabled(false);

        // load 5 trains
        for (int i = 0; i < 5; i++) {
            tmanager.newTrain("Test_Train " + i);
        }

        // load 6 locations
        for (int i = 0; i < 6; i++) {
            InstanceManager.getDefault(LocationManager.class).newLocation("Test_Location " + i);
        }

        // load 5 routes
        InstanceManager.getDefault(RouteManager.class).newRoute("Test Route A");
        InstanceManager.getDefault(RouteManager.class).newRoute("Test Route B");
        InstanceManager.getDefault(RouteManager.class).newRoute("Test Route C");
        InstanceManager.getDefault(RouteManager.class).newRoute("Test Route D");
        InstanceManager.getDefault(RouteManager.class).newRoute("Test Route E");
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
