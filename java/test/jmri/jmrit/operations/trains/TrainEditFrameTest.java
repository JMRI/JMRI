package jmri.jmrit.operations.trains;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainEditFrameTest extends OperationsSwingTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        TrainEditFrame t = new TrainEditFrame(train1);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testTrainEditFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainEditFrame trainEditFrame = new TrainEditFrame(null);
        trainEditFrame.setTitle("Test Edit Train Frame");
        ThreadingUtil.runOnGUI(() -> {
            // fill in name and description fields
            trainEditFrame.trainNameTextField.setText("Test Train Name");
            trainEditFrame.trainDescriptionTextField.setText("Test Train Description");
            trainEditFrame.commentTextArea.setText("Test Train Comment");
        });
        enterClickAndLeave(trainEditFrame.addTrainButton);

        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train t = tmanager.getTrainByName("Test Train Name");

        // test defaults
        Assert.assertEquals("train name", "Test Train Name", t.getName());
        Assert.assertEquals("train description", "Test Train Description", t.getDescription());
        Assert.assertEquals("train comment", "Test Train Comment", t.getComment());
        Assert.assertEquals("train depart time", "00:00", t.getDepartureTime());
        Assert.assertEquals("train route", null, t.getRoute());
        Assert.assertTrue("train accepts car type Boxcar", t.acceptsTypeName("Boxcar"));
        Assert.assertEquals("train roads", Train.ALL_ROADS, t.getRoadOption());
        Assert.assertEquals("train requirements", Train.NO_CABOOSE_OR_FRED, t.getRequirements());

        // test departure time fields
        trainEditFrame.hourBox.setSelectedItem("15");
        trainEditFrame.minuteBox.setSelectedItem("45");
        // shouldn't change until Save
        Assert.assertEquals("train comment", "00:00", t.getDepartureTime());
        enterClickAndLeave(trainEditFrame.saveTrainButton);

        // clear no route dialogue box
        pressDialogButton(trainEditFrame, Bundle.getMessage("TrainNoRoute"), "OK");

        Assert.assertEquals("train comment", "15:45", t.getDepartureTime());

        // test route field, 5 routes and a blank
        Assert.assertEquals("Route Combobox item count", 6, trainEditFrame.routeBox.getItemCount());
        ThreadingUtil.runOnGUI(() -> {
            trainEditFrame.routeBox.setSelectedIndex(3); // the 3rd item should be "Test Route C"
        });
        Assert.assertEquals("train route 2", "Test Route C", t.getRoute().getName());
        // test route edit button
        enterClickAndLeave(trainEditFrame.editButton);

        // confirm panel creation
        JmriJFrame ref = JmriJFrame.getFrame(Bundle.getMessage("TitleRouteEdit"));
        Assert.assertNotNull("route add frame", ref);

        // increase screen size so clear and set buttons are shown
        ThreadingUtil.runOnGUI(() -> {
            trainEditFrame.setLocation(10, 0);
            trainEditFrame.setSize(trainEditFrame.getWidth(), trainEditFrame.getHeight() + 200);
        });

        // test car types using the clear and set buttons
        enterClickAndLeave(trainEditFrame.clearButton);

        Assert.assertFalse("train accepts car type Boxcar", t.acceptsTypeName("Boxcar"));
        enterClickAndLeave(trainEditFrame.setButton);

        Assert.assertTrue("train accepts car type Boxcar", t.acceptsTypeName("Boxcar"));

        // test engine fields
        Assert.assertEquals("number of engines", "0", t.getNumberEngines());
        Assert.assertEquals("engine model", "", t.getEngineModel());
        Assert.assertEquals("engine road", "", t.getEngineRoad());
        // now change them
        trainEditFrame.numEnginesBox.setSelectedItem("3");
        trainEditFrame.modelEngineBox.setSelectedItem("FT");
        trainEditFrame.roadEngineBox.setSelectedItem("UP");
        // shouldn't change until Save
        Assert.assertEquals("number of engines 1", "0", t.getNumberEngines());
        Assert.assertEquals("engine model 1", "", t.getEngineModel());
        Assert.assertEquals("engine road 1", "", t.getEngineRoad());
        enterClickAndLeave(trainEditFrame.saveTrainButton);

        Assert.assertEquals("number of engines 2", "3", t.getNumberEngines());
        Assert.assertEquals("engine model 2", "FT", t.getEngineModel());
        Assert.assertEquals("engine road 2", "UP", t.getEngineRoad());

        // test caboose and FRED buttons and fields
        // require a car with FRED
        enterClickAndLeave(trainEditFrame.fredRadioButton);

        // shouldn't change until Save
        Assert.assertEquals("train requirements 1", Train.NO_CABOOSE_OR_FRED, t.getRequirements());
        enterClickAndLeave(trainEditFrame.saveTrainButton);

        Assert.assertEquals("train requirements 2", Train.FRED, t.getRequirements());
        enterClickAndLeave(trainEditFrame.cabooseRadioButton);

        enterClickAndLeave(trainEditFrame.saveTrainButton);

        Assert.assertEquals("train requirements 3", Train.CABOOSE, t.getRequirements());
        Assert.assertEquals("caboose road 1", "", t.getCabooseRoad());
        // shouldn't change until Save
        String roadNames[] = Bundle.getMessage("carRoadNames").split(",");
        trainEditFrame.roadCabooseBox.setSelectedItem(roadNames[2]);
        Assert.assertEquals("caboose road 2", "", t.getCabooseRoad());
        enterClickAndLeave(trainEditFrame.saveTrainButton);

        Assert.assertEquals("caboose road 3", roadNames[2], t.getCabooseRoad());
        enterClickAndLeave(trainEditFrame.noneRadioButton);

        enterClickAndLeave(trainEditFrame.saveTrainButton);

        Assert.assertEquals("train requirements 4", Train.NO_CABOOSE_OR_FRED, t.getRequirements());

        // test frame size and location
        ThreadingUtil.runOnGUI(() -> {
            trainEditFrame.setSize(650, 600);
            trainEditFrame.setLocation(25, 30);
        });
        enterClickAndLeave(trainEditFrame.saveTrainButton);

        // test delete button
        // the delete opens a dialog window to confirm the delete
        enterClickAndLeave(trainEditFrame.deleteTrainButton);

        // don't delete, we need this train for the next two tests
        // testTrainBuildOptionFrame() and testTrainEditFrameRead()
        pressDialogButton(trainEditFrame, Bundle.getMessage("deleteTrain"), Bundle.getMessage("ButtonNo"));

        ThreadingUtil.runOnGUI(() -> {
            JUnitUtil.dispose(ref);
            JUnitUtil.dispose(trainEditFrame);
        });

        // now reload the window
        Train t2 = tmanager.getTrainByName("Test Train Name");
        Assert.assertNotNull(t);

        // change the train so it doesn't match the add test
        ThreadingUtil.runOnGUI(() -> {
            t2.setRequirements(Train.CABOOSE);
            t2.setCabooseRoad("CP");

        });

        TrainEditFrame f = new TrainEditFrame(t2);
        f.setTitle("Test Edit Train Frame");

        Assert.assertEquals("train name", "Test Train Name", f.trainNameTextField.getText());
        Assert.assertEquals("train description", "Test Train Description", f.trainDescriptionTextField
                .getText());
        Assert.assertEquals("train comment", "Test Train Comment", f.commentTextArea.getText());
        Assert.assertEquals("train depart hour", "15", f.hourBox.getSelectedItem());
        Assert.assertEquals("train depart minute", "45", f.minuteBox.getSelectedItem());
        Assert.assertEquals("train route", t.getRoute(), f.routeBox.getSelectedItem());
        Assert.assertEquals("number of engines", "3", f.numEnginesBox.getSelectedItem());
        Assert.assertEquals("engine model", "FT", f.modelEngineBox.getSelectedItem());
        Assert.assertEquals("engine road", "UP", f.roadEngineBox.getSelectedItem());
        Assert.assertEquals("caboose road", "CP", f.roadCabooseBox.getSelectedItem());
        // check radio buttons
        Assert.assertTrue("caboose selected", f.cabooseRadioButton.isSelected());
        Assert.assertFalse("none selected", f.noneRadioButton.isSelected());
        Assert.assertFalse("FRED selected", f.fredRadioButton.isSelected());

        ThreadingUtil.runOnGUI(() -> {
            JUnitUtil.dispose(f);
        });
    }
    
    /**
     * Test that delete train works
     */
    @Test
    public void testTrainEditFrameDelete() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train t = tmanager.getTrainByName("Test_Train 1");
        Assert.assertNotNull(t);

        TrainEditFrame trainEditFrame = new TrainEditFrame(t);
        trainEditFrame.setTitle("Test Delete Train Frame");

        enterClickAndLeave(trainEditFrame.deleteTrainButton);

        // And now press the confirmation button
        pressDialogButton(trainEditFrame, Bundle.getMessage("deleteTrain"), Bundle.getMessage("ButtonYes"));

        t = tmanager.getTrainByName("Test_Train 1");
        Assert.assertNull("train deleted", t);

        // Now add it back
        enterClickAndLeave(trainEditFrame.addTrainButton);

        t = tmanager.getTrainByName("Test_Train 1");
        Assert.assertNotNull("train added", t);

        JUnitUtil.dispose(trainEditFrame);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        loadTrains();
    }

    private void loadTrains() {
        // Add some cars for the various tests in this suite
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        String roadNames[] = Bundle.getMessage("carRoadNames").split(",");
        // add caboose to the roster
        Car c = cm.newCar(roadNames[2], "687");
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

    // private final static Logger log = LoggerFactory.getLogger(TrainEditFrameTest.class);
}
