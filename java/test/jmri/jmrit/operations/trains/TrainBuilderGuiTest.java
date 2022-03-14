package jmri.jmrit.operations.trains;

import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Kernel;
import jmri.jmrit.operations.rollingstock.cars.KernelManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import jmri.util.ThreadingUtil;

/**
 * Tests for the Operations Trains GUI class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class TrainBuilderGuiTest extends OperationsTestCase {

    private TrainManager tmanager;
    private LocationManager lmanager;
    private EngineManager emanager;
    private CarManager cmanager;

    /**
     * Test prompt for which track in staging a train should depart on.
     */
    @Test
    public void testStagingPromptFrom() {

        JUnitOperationsUtil.initOperationsData();
        Setup.setStagingPromptFromEnabled(true);

        // two sets of cars in staging
        JUnitOperationsUtil.initOperationsData();

        Train train2 = tmanager.getTrainById("2");

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("SelectDepartureTrack"), Bundle.getMessage("ButtonOK"));
        });
        t1.setName("testStagingPromptFrom Thread 1");
        t1.start();

        // should cause a prompt asking which track to use
        boolean buildCompleteOk = ThreadingUtil.runOnGUIwithReturn(() -> {
            return new TrainBuilder().build(train2);
        });

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click OK in prompt asking which track to use did not happen");

        Assert.assertTrue("build Completed Ok", buildCompleteOk);

        Assert.assertTrue("Train status", train2.isBuilt());

        train2.reset();

        // now try the cancel option

        Thread t2 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("SelectDepartureTrack"), "Cancel");
        });
        t2.setName("testStagingPromptFrom Thread 2");
        t2.start();

        buildCompleteOk = ThreadingUtil.runOnGUIwithReturn(() -> {
            return new TrainBuilder().build(train2);
        });

        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        }, "Click Cancel in prompt asking which track to use did not happen");

        Assert.assertFalse("build not Complete Ok", buildCompleteOk);

        Assert.assertFalse("Train status", train2.isBuilt());

        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    /**
     * Test prompt selecting which track to use in staging.
     */
    @Test
    public void testStagingPromptTo() {

        JUnitOperationsUtil.initOperationsData();
        Setup.setStagingPromptToEnabled(true);

        Train train2 = tmanager.getTrainById("2");

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("SelectArrivalTrack"), Bundle.getMessage("ButtonOK"));
        });
        t1.setName("testStagingPromptTo Thread 1");
        t1.start();

        // should cause prompt for track into staging
        boolean buildCompleteOk = ThreadingUtil.runOnGUIwithReturn(() -> {
            return new TrainBuilder().build(train2);
        });

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "SelectArrivalTrack OK did not happen");

        Assert.assertTrue("build Completed Ok", buildCompleteOk);

        Assert.assertTrue("Train status", train2.isBuilt());

        train2.reset();

        // now try the cancel option

        Thread t2 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("SelectArrivalTrack"), "Cancel");
        });
        t2.setName("testStagingPromptTo Thread 2");
        t2.start();
        
        buildCompleteOk = ThreadingUtil.runOnGUIwithReturn(() -> {
            return new TrainBuilder().build(train2);
        });

        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        }, "SelectArrivalTrack Cancel did not happen");

        Assert.assertFalse("build not Complete Ok", buildCompleteOk);

        Assert.assertFalse("Train status", train2.isBuilt());

        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testBuildFailedMessage() {

        JUnitOperationsUtil.initOperationsData();
        // enable build failure messages
        tmanager.setBuildMessagesEnabled(true);

        Train train2 = tmanager.getTrainById("2");
        // make train build fail by removing train's route
        train2.setRoute(null);

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("buildErrorMsg"),
                new Object[] { train2.getName(), train2.getDescription() }), Bundle.getMessage("ButtonOK"));
        });
        t1.setName("testBuildFailedMessage Thread 1");
        t1.start();

        // should cause failure dialog to appear
        boolean buildCompleteOk = ThreadingUtil.runOnGUIwithReturn(() -> {
            return new TrainBuilder().build(train2);
        });

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "failure dialog click OK did not happen");

        Assert.assertFalse("build not Complete Ok", buildCompleteOk);

        Assert.assertFalse("Train status", train2.isBuilt());
    }

    /**
     * Test warning message.
     */
    @Test
    public void testWarningMessage() {

        JUnitOperationsUtil.initOperationsData();
        tmanager.setBuildMessagesEnabled(true);

        // cause 1 warning message
        Setup.setCarRoutingEnabled(false);

        // Route Northend-NI-Southend
        Train train2 = tmanager.getTrainById("2");

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(
                MessageFormat.format(Bundle.getMessage("buildWarningMsg"), new Object[] { train2.getName(), 1 }),
                Bundle.getMessage("ButtonOK"));
        });
        t1.setName("testWarningMessage Thread 1");
        t1.start();
        
        boolean buildCompleteOk = ThreadingUtil.runOnGUIwithReturn(() -> {
            return new TrainBuilder().build(train2);
        });

        // dialog "Build report for train (SFF) has 1 warnings"
        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "failure dialog click OK did not happen");
        
        Assert.assertTrue("build Completed Ok", buildCompleteOk);
        Assert.assertTrue("Train status", train2.isBuilt());
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    /**
     * Test failure message when cars in staging are stuck there.
     */
    @Test
    public void testBuildFailedMessageStagingA() {

        JUnitOperationsUtil.initOperationsData();
        tmanager.setBuildMessagesEnabled(true);

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");

        Location northend = lmanager.getLocationById("1");

        Track northendStaging1 = northend.getTrackById("1s1");

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(northend, northendStaging1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(northend, northendStaging1));

        // 4 cars in staging, 2 cabooses, and 2 Boxcars
        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");

        // Route Northend-NI-Southend
        Train train2 = tmanager.getTrainById("2");
        train2.setNumberEngines("2");
        Route route = train2.getRoute();

        // don't allow any drops in the train's route to cause build failure
        RouteLocation rlNI = route.getRouteLocationBySequenceNumber(2);
        rlNI.setDropAllowed(false);

        // note that Cabooses ignore the drop restriction
        RouteLocation rlSouthendStaging = route.getRouteLocationBySequenceNumber(3);
        rlSouthendStaging.setDropAllowed(false);

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("buildErrorMsg"),
                new Object[] { train2.getName(), train2.getDescription() }), Bundle.getMessage("ButtonOK"));
        });
        t1.setName("testBuildFailedMessageStagingA Dialog Clicker 1");
        t1.start();

        Thread t2 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("buildResetTrain"), Bundle.getMessage("ButtonNo"));
        });
        t2.setName("testBuildFailedMessageStagingA Dialog Clicker 2");
        t2.start();

        // should cause failure dialog to appear
        boolean buildCompleteOk = ThreadingUtil.runOnGUIwithReturn(() -> {
            return new TrainBuilder().build(train2);
        });

        // dialog "remove cars from staging" or continue by pressing "OK"
        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "remove cars from staging dialog click OK did not happen");

        // next prompt asks if cars are to be released from train by reset
        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        }, "released from train by reset dialog click no did not happen");

        Assert.assertFalse("build not Complete Ok", buildCompleteOk);

        Assert.assertFalse("Train status", train2.isBuilt());

        // confirm that the two cabooses are assigned to the train
        Assert.assertEquals("Train assignment", train2, e1.getTrain());
        Assert.assertEquals("Train assignment", train2, e2.getTrain());
        Assert.assertEquals("Train assignment", train2, c1.getTrain());
        Assert.assertEquals("Train assignment", train2, c2.getTrain());

        Assert.assertEquals("Train assignment", null, c3.getTrain());
        Assert.assertEquals("Train assignment", null, c4.getTrain());

        Assert.assertEquals("Track assignment", northendStaging1, c3.getTrack());
        Assert.assertEquals("Track assignment", northendStaging1, c4.getTrack());
    }

    /**
     * Test failure message when cars in staging are stuck there. Release cars and
     * engines by train reset.
     */
    @Test
    public void testBuildFailedMessageStagingB() {

        JUnitOperationsUtil.initOperationsData();
        tmanager.setBuildMessagesEnabled(true);

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");

        Location northend = lmanager.getLocationById("1");

        Track northendStaging1 = northend.getTrackById("1s1");

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(northend, northendStaging1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(northend, northendStaging1));

        // 4 cars in staging, 2 cabooses, and 2 Boxcars
        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");

        // increase test code coverage by placing cars in a kernel
        Kernel k2 = InstanceManager.getDefault(KernelManager.class).newKernel("2 Boxcars");
        c3.setKernel(k2);
        c4.setKernel(k2);

        // Route Northend-NI-Southend
        Train train2 = tmanager.getTrainById("2");
        train2.setNumberEngines("2");
        Route route = train2.getRoute();

        RouteLocation rlNI = route.getRouteLocationBySequenceNumber(2);
        rlNI.setDropAllowed(false);

        // note that Cabooses ignore the drop restriction
        RouteLocation rlSouthendStaging = route.getRouteLocationBySequenceNumber(3);
        rlSouthendStaging.setDropAllowed(false);

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("buildErrorMsg"),
                new Object[] { train2.getName(), train2.getDescription() }), Bundle.getMessage("ButtonOK"));
        });
        t1.setName("remove cars from staging, click OK Thread");
        t1.start();
        
        Thread t2 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("buildResetTrain"), Bundle.getMessage("ButtonYes"));
        });
        t2.setName("cars are to be released from train by reset, click Yes Thread");
        t2.start();
        
        // should cause failure dialogs to appear
        boolean buildCompleteOk = ThreadingUtil.runOnGUIwithReturn(() -> {
            return new TrainBuilder().build(train2);
        });

        // dialog "remove cars from staging" or continue by pressing "OK", press OK.
        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "remove cars from staging, click OK did not happen");

        // prompt asks if cars are to be released from train by reset, press Yes.
        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        }, "cars are to be released from train by reset, click Yes did not happen");

        Assert.assertFalse("build not Complete Ok", buildCompleteOk);

        Assert.assertFalse("Train status", train2.isBuilt());

        // confirm that engines and cars are released from train
        Assert.assertEquals("Train assignment", null, e1.getTrain());
        Assert.assertEquals("Train assignment", null, e2.getTrain());
        Assert.assertEquals("Train assignment", null, c1.getTrain());
        Assert.assertEquals("Train assignment", null, c2.getTrain());

        Assert.assertEquals("Train assignment", null, c3.getTrain());
        Assert.assertEquals("Train assignment", null, c4.getTrain());

        Assert.assertEquals("Track assignment", northendStaging1, c3.getTrack());
        Assert.assertEquals("Track assignment", northendStaging1, c4.getTrack());
    }

    /**
     * Test failure message when cars in staging are stuck there. Remove stuck cars
     * from staging.
     */
    @Test
    public void testBuildFailedMessageStagingC() {

        JUnitOperationsUtil.initOperationsData();
        tmanager.setBuildMessagesEnabled(true);

        Location northend = lmanager.getLocationById("1");

        Track northendStaging1 = northend.getTrackById("1s1");

        // 4 cars in staging, 2 cabooses, and 2 Boxcars
        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");
        Car c11 = JUnitOperationsUtil.createAndPlaceCar("A", "110", "Boxcar", "40", northendStaging1, 1);

        // increase test code coverage by placing cars in a kernel
        Kernel k2 = InstanceManager.getDefault(KernelManager.class).newKernel("2 Boxcars");
        c3.setKernel(k2);
        c4.setKernel(k2);

        // Route Northend-NI-Southend
        Train train2 = tmanager.getTrainById("2");
        Route route = train2.getRoute();

        RouteLocation rlNI = route.getRouteLocationBySequenceNumber(2);
        rlNI.setDropAllowed(false);

        // note that Cabooses ignore the drop restriction
        RouteLocation rlSouthendStaging = route.getRouteLocationBySequenceNumber(3);
        rlSouthendStaging.setDropAllowed(false);

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(
                MessageFormat.format(Bundle.getMessage("buildErrorMsg"),
                        new Object[] { train2.getName(), train2.getDescription() }),
                Bundle.getMessage("buttonRemoveCars"));
        });
        t1.setName("testBuildFailedMessageStagingC Dialog Clicker 1");
        t1.start();

        Thread t2 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("buildResetTrain"), Bundle.getMessage("ButtonYes"));
        });
        t2.setName("testBuildFailedMessageStagingC Dialog Clicker 2");
        t2.start();

        // should cause failure dialog to appear
        boolean buildCompleteOk = ThreadingUtil.runOnGUIwithReturn(() -> {
            return new TrainBuilder().build(train2);
        });

        // dialog "remove cars from staging" or continue by pressing "OK"
        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "remove cars from staging, click remove did not happen");

        // next prompt asks if cars are to be released from train by reset
        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        }, "cars are to be released from train by reset, click yes did not happen");

        Assert.assertFalse("build not Complete Ok", buildCompleteOk);

        Assert.assertFalse("Train status", train2.isBuilt());

        // confirm that engines and cars are released from train
        Assert.assertEquals("Train assignment", null, c1.getTrain());
        Assert.assertEquals("Train assignment", null, c2.getTrain());

        Assert.assertEquals("Train assignment", null, c3.getTrain());
        Assert.assertEquals("Train assignment", null, c4.getTrain());

        Assert.assertEquals("Track assignment", null, c3.getTrack());
        Assert.assertEquals("Track assignment", null, c4.getTrack());
        Assert.assertEquals("Track assignment", null, c11.getTrack());
    }

    /**
     * Test failure message when build fails, release engines by reset. No cars in
     * staging for this test.
     */
    @Test
    public void testBuildFailedMessageStagingD() {

        JUnitOperationsUtil.initOperationsData();
        tmanager.setBuildMessagesEnabled(true);

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

        Location northend = lmanager.getLocationById("1");
        Track northendStaging1 = northend.getTrackById("1s1");

        Location southend = lmanager.getLocationById("3");
        Track southendStaging1 = southend.getTrackById("3s1");

        // 4 cars in staging, 2 cabooses, and 2 Boxcars
        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");

        // remove cars from departure track
        c1.setLocation(null, null);
        c2.setLocation(null, null);
        c3.setLocation(null, null);
        c4.setLocation(null, null);

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(northend, northendStaging1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(northend, northendStaging1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(northend, northendStaging1));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(northend, northendStaging1));

        // Route Northend-NI-Southend
        Train train2 = tmanager.getTrainById("2");
        train2.setNumberEngines("4");

        southendStaging1.setLength(200); // make track too short for 4 locos

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("buildErrorMsg"),
                new Object[] { train2.getName(), train2.getDescription() }), Bundle.getMessage("ButtonOK"));
        });
        t1.setName("testBuildFailedMessageStagingD Dialog Clicker 1");
        t1.start();

        Thread t2 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("buildResetTrain"), Bundle.getMessage("ButtonYes"));
        });
        t2.setName("testBuildFailedMessageStagingD Dialog Clicker 2");
        t2.start();

        // should cause failure dialog to appear
        boolean buildCompleteOk = ThreadingUtil.runOnGUIwithReturn(() -> {
            return new TrainBuilder().build(train2);
        });

        // dialog remove engines from staging or continue by pressing OK
        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "remove cars from staging, click ok did not happen");

        // next prompt asks if cars are to be released from train by reset
        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        }, "cars are to be released from train by reset, click yes did not happen");

        Assert.assertFalse("build not Complete Ok", buildCompleteOk);

        Assert.assertFalse("Train status", train2.isBuilt());

        // confirm that the two cabooses are assigned to the train
        Assert.assertEquals("Train assignment", null, e1.getTrain());
        Assert.assertEquals("Train assignment", null, e2.getTrain());
        Assert.assertEquals("Train assignment", null, e3.getTrain());
        Assert.assertEquals("Train assignment", null, e4.getTrain());
    }

    /**
     * Test failure message when build fails, Don't release engines by reset. No
     * cars in staging for this test.
     */
    @Test
    public void testBuildFailedMessageStagingE() {

        JUnitOperationsUtil.initOperationsData();
        tmanager.setBuildMessagesEnabled(true);

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

        Location northend = lmanager.getLocationById("1");
        Track northendStaging1 = northend.getTrackById("1s1");

        Location southend = lmanager.getLocationById("3");
        Track southendStaging1 = southend.getTrackById("3s1");

        // 4 cars in staging, 2 cabooses, and 2 Boxcars
        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");

        // remove cars from departure track
        c1.setLocation(null, null);
        c2.setLocation(null, null);
        c3.setLocation(null, null);
        c4.setLocation(null, null);

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(northend, northendStaging1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(northend, northendStaging1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(northend, northendStaging1));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(northend, northendStaging1));

        // Route Northend-NI-Southend
        Train train2 = tmanager.getTrainById("2");
        train2.setNumberEngines("4");

        southendStaging1.setLength(200); // make track too short for 4 locos

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("buildErrorMsg"),
                new Object[] { train2.getName(), train2.getDescription() }), Bundle.getMessage("ButtonOK"));
        });
        t1.setName("testBuildFailedMessageStagingE Dialog Clicker 1");
        t1.start();

        Thread t2 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("buildResetTrain"), Bundle.getMessage("ButtonNo"));
        });
        t2.setName("testBuildFailedMessageStagingE Dialog Clicker 2");
        t2.start();

        // should cause failure dialog to appear
        boolean buildCompleteOk = ThreadingUtil.runOnGUIwithReturn(() -> {
            return new TrainBuilder().build(train2);
        });

        // dialog remove engines from staging or continue by pressing OK
        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "remove cars from staging, click ok did not happen");

        // next prompt asks if cars are to be released from train by reset
        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        }, "cars are to be released from train by reset, click no did not happen");

        Assert.assertFalse("build not Complete Ok", buildCompleteOk);

        // only e3 and e4 have been assigned to train
        Assert.assertFalse("Train status", train2.isBuilt());

        // confirm that the two cabooses are assigned to the train
        Assert.assertEquals("Train assignment", null, e1.getTrain());
        Assert.assertEquals("Train assignment", null, e2.getTrain());
        Assert.assertEquals("Train assignment", train2, e3.getTrain());
        Assert.assertEquals("Train assignment", train2, e4.getTrain());
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        // setup new managers
        tmanager = InstanceManager.getDefault(TrainManager.class);
        lmanager = InstanceManager.getDefault(LocationManager.class);
        emanager = InstanceManager.getDefault(EngineManager.class);
        cmanager = InstanceManager.getDefault(CarManager.class);

        // disable build messages
        tmanager.setBuildMessagesEnabled(false);
    }
}
