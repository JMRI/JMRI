package jmri.jmrit.operations.trains.tools;

import jmri.jmrit.operations.trains.timetable.TrainsScheduleTableFrame;

import java.awt.Component;
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
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations Trains GUI class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class OperationsTrainsGuiTest extends OperationsSwingTestCase {


    public void testTrainModifyFrame() {
        // confirm that train default accepts Boxcars
        TrainManager tmanager = TrainManager.instance();
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

        f.dispose();
    }

    public void testTrainByCarTypeFrame() {
        TrainManager tmanager = TrainManager.instance();
        Train train = tmanager.getTrainByName("Test Train Name");
        TrainByCarTypeFrame f = new TrainByCarTypeFrame();
        f.initComponents(train);

        Assert.assertNotNull("frame exists", f);
        f.dispose();
    }

    public void testTrainsScheduleTableFrame() {
        TrainsScheduleTableFrame f = new TrainsScheduleTableFrame();
        f.setVisible(true);

        Assert.assertNotNull("frame exists", f);
        f.dispose();
    }

    // test TrainIcon attributes
    public void testTrainIconAttributes() {
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



    private void enterClickAndLeave(Component comp) {
        getHelper().enterClickAndLeave(new MouseEventData(this, comp));
        jmri.util.JUnitUtil.releaseThread(comp.getTreeLock()); // compensate for race between GUI and test thread
    }

    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        loadTrains();
    }

    private void loadTrains() {
        // Add some cars for the various tests in this suite
        CarManager cm = CarManager.instance();
        // add caboose to the roster
        Car c = cm.newCar("NH", "687");
        c.setCaboose(true);
        c = cm.newCar("CP", "435");
        c.setCaboose(true);

        // load engines
        EngineManager emanager = EngineManager.instance();
        Engine e1 = emanager.newEngine("E", "1");
        e1.setModel("GP40");
        Engine e2 = emanager.newEngine("E", "2");
        e2.setModel("GP40");
        Engine e3 = emanager.newEngine("UP", "3");
        e3.setModel("GP40");
        Engine e4 = emanager.newEngine("UP", "4");
        e4.setModel("FT");

        TrainManager tmanager = TrainManager.instance();
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
            LocationManager.instance().newLocation("Test_Location " + i);
        }

        // load 5 routes
        RouteManager.instance().newRoute("Test Route A");
        RouteManager.instance().newRoute("Test Route B");
        RouteManager.instance().newRoute("Test Route C");
        RouteManager.instance().newRoute("Test Route D");
        RouteManager.instance().newRoute("Test Route E");
    }

    public OperationsTrainsGuiTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", OperationsTrainsGuiTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OperationsTrainsGuiTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
