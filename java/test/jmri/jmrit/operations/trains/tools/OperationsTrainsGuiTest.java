package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainIcon;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.timetable.TrainsScheduleTableFrame;
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
    public void testTrainModifyFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // confirm that train default accepts Boxcars
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train t = tmanager.newTrain("Test Train Name 2");
        Assert.assertTrue("accepts Boxcar 1", t.acceptsTypeName("Boxcar"));

        TrainsByCarTypeFrame f = new TrainsByCarTypeFrame();
        f.initComponents("Boxcar");

        // remove Boxcar from trains
        enterClickAndLeave(f.clearButton);
        enterClickAndLeave(f.saveButton);

        Assert.assertFalse("accepts Boxcar 2", t.acceptsTypeName("Boxcar"));

        // now add Boxcar to trains
        enterClickAndLeave(f.setButton);
        enterClickAndLeave(f.saveButton);

        Assert.assertTrue("accepts Boxcar 3", t.acceptsTypeName("Boxcar"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testTrainByCarTypeFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.getTrainByName("Test Train Name");
        TrainByCarTypeFrame f = new TrainByCarTypeFrame();
        f.initComponents(train);

        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testTrainsScheduleTableFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsScheduleTableFrame f = new TrainsScheduleTableFrame();
        f.setVisible(true);

        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }

    // test TrainIcon attributes
    @Test
    public void testTrainIconAttributes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTNAME");

        Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
        Assert.assertEquals("Train Name", "TESTNAME", train1.getName());
        Assert.assertEquals("Train toString", "TESTNAME", train1.toString());

        jmri.jmrit.display.panelEditor.PanelEditor editor = new jmri.jmrit.display.panelEditor.PanelEditor(
                "Test Panel");
        Assert.assertNotNull("New editor", editor);
        TrainIcon trainicon1 = editor.addTrainIcon("TestName");
        trainicon1.setTrain(train1);
        Assert.assertEquals("TrainIcon set train", "TESTNAME", trainicon1.getTrain().getName());

        // test color change
        String[] colors = TrainIcon.getLocoColors();
        for (int i = 0; i < colors.length; i++) {
            trainicon1.setLocoColor(colors[i]);
        }
        editor.getTargetFrame().dispose();
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
