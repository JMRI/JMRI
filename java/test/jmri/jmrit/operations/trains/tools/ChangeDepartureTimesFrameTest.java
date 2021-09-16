package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ChangeDepartureTimesFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ChangeDepartureTimesFrame t = new ChangeDepartureTimesFrame();
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testChangeTime() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        
        // confirm departure time default
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train2 = tmanager.getTrainById("2");
        Assert.assertEquals("departure time", "22", train2.getDepartureTimeHour());
        ChangeDepartureTimesFrame f = new ChangeDepartureTimesFrame();
        JemmyUtil.enterClickAndLeave(f.changeButton);
        Assert.assertEquals("departure time", "23", train2.getDepartureTimeHour());
        // test roll over at 24 hours
        JemmyUtil.enterClickAndLeave(f.changeButton);
        Assert.assertEquals("departure time", "00", train2.getDepartureTimeHour());      
    }
    
    @Test
    public void testChangeTimeRoute() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train2 = tmanager.getTrainById("2");
        Route route = train2.getRoute();
        // set depart time using the train's route
        RouteLocation rl1 = route.getRouteLocationBySequenceNumber(1);
        rl1.setDepartureTime("03", "43");
        RouteLocation rl2 = route.getRouteLocationBySequenceNumber(2);
        rl2.setDepartureTime("23", "02");
        
        Assert.assertEquals("departure time", "03", train2.getDepartureTimeHour());
        ChangeDepartureTimesFrame f = new ChangeDepartureTimesFrame();
        JemmyUtil.enterClickAndLeave(f.changeButton);
        Assert.assertEquals("departure time", "04", train2.getDepartureTimeHour());
        Assert.assertEquals("Route location departure time", "00", rl2.getDepartureTimeHour());
    }

    // private final static Logger log = LoggerFactory.getLogger(ChangeDepartureTimesFrameTest.class);

}
