package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class YardmasterFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        YardmasterFrame t = new YardmasterFrame(null);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testLocationWithWork() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // improve test coverage
        Setup.setPrintHeadersEnabled(true);

        // build a train to create data for the Yardmaster window
        Train train = InstanceManager.getDefault(TrainManager.class).getTrainByName("STF");
        Assert.assertNotNull("train must exist", train);
        Assert.assertTrue("successful build", train.build());

        Location l = InstanceManager.getDefault(LocationManager.class).getLocationByName("North Industries");
        Assert.assertNotNull("location must exist", l);

        YardmasterFrame ymFrame = new YardmasterFrame(l);
        Assert.assertNotNull("exists", ymFrame);

        YardmasterPanel ymPanel = (YardmasterPanel) ymFrame.getContentPane();

        // press "Next" button to load window
        JemmyUtil.enterClickAndLeave(ymPanel.nextButton);

        // exercise buttons
        JemmyUtil.enterClickAndLeave(ymPanel.selectButton);

        JemmyUtil.enterClickAndLeave(ymPanel.clearButton);

        // press "Modify"
        JemmyUtil.enterClickAndLeaveThreadSafe(ymPanel.modifyButton);
        // clear dialog window
        JemmyUtil.pressDialogButton(ymFrame, Bundle.getMessage("AddCarsToTrain?"), Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(ymFrame);
        // press "Done"
        JemmyUtil.enterClickAndLeave(ymPanel.modifyButton);

        JUnitUtil.dispose(ymFrame);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
//  @Test
//  public void testLoop2() {
//      for (int i = 0; i < 1000; i++) {
//          setUp();
//          testLocationWithWork();
//          tearDown();
//      }
//  }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        // disable build messages
        InstanceManager.getDefault(TrainManager.class).setBuildMessagesEnabled(false);

        JUnitOperationsUtil.initOperationsData();
        // need to disable all references to JTextPane() so test dead locks don't occur
        // on Windows CI tests
        Setup.setPrintRouteCommentsEnabled(false);
        Setup.setPrintLocationCommentsEnabled(false);
        Train train2 = InstanceManager.getDefault(TrainManager.class).getTrainByName("STF");
        train2.setComment(Train.NONE);
        Route route = train2.getRoute();
        RouteLocation rl = route.getDepartsRouteLocation();
        rl.setComment(RouteLocation.NONE);
    }
}
