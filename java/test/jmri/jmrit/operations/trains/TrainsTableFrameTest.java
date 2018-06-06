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
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainsTableFrameTest extends OperationsSwingTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsTableFrame t = new TrainsTableFrame();
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testTrainsTableFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);

        TrainsTableFrame f = new TrainsTableFrame();
        f.setLocation(10, 20);

        enterClickAndLeave(f.saveButton);

        Assert.assertEquals("sort by name", TrainsTableModel.TIMECOLUMNNAME, f.getSortBy());
        Assert.assertTrue("Build Messages", tmanager.isBuildMessagesEnabled());
        Assert.assertFalse("Build Report", tmanager.isBuildReportEnabled());
        Assert.assertFalse("Print Review", tmanager.isPrintPreviewEnabled());

        enterClickAndLeave(f.showTime);
        enterClickAndLeave(f.buildMsgBox);
        enterClickAndLeave(f.buildReportBox);
        enterClickAndLeave(f.saveButton);

        Assert.assertFalse("Build Messages 2", tmanager.isBuildMessagesEnabled());
        Assert.assertTrue("Build Report 2", tmanager.isBuildReportEnabled());
        Assert.assertFalse("Print Review 2", tmanager.isPrintPreviewEnabled());

        enterClickAndLeave(f.showId);
        enterClickAndLeave(f.buildMsgBox);
        enterClickAndLeave(f.printPreviewBox);
        enterClickAndLeave(f.saveButton);

        Assert.assertTrue("Build Messages 3", tmanager.isBuildMessagesEnabled());
        Assert.assertTrue("Build Report 3", tmanager.isBuildReportEnabled());
        Assert.assertTrue("Print Review 3", tmanager.isPrintPreviewEnabled());

        // create the TrainEditFrame
        enterClickAndLeave(f.addButton);

        // confirm panel creation
        JmriJFrame tef = JmriJFrame.getFrame(Bundle.getMessage("TitleTrainAdd"));
        Assert.assertNotNull("train edit frame", tef);

        // create the TrainSwichListEditFrame
        enterClickAndLeave(f.switchListsButton);

        // confirm panel creation
        JmriJFrame tsle = JmriJFrame.getFrame(Bundle.getMessage("TitleSwitchLists"));
        Assert.assertNotNull("train switchlist edit frame", tsle);

        // kill panels
        JUnitUtil.dispose(tef);
        JUnitUtil.dispose(tsle);
        JUnitUtil.dispose(f);
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

    // private final static Logger log = LoggerFactory.getLogger(TrainsTableFrameTest.class);

}
