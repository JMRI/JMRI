package jmri.jmrit.operations.trains;

import java.text.MessageFormat;

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

import jmri.util.*;
import jmri.util.swing.JemmyUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Tests for the Operations Trains GUI class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class TrainBuilderGuiTest extends OperationsTestCase {

    private TrainManager tmanager;
    private LocationManager lmanager;
    private EngineManager emanager;
    private CarManager cmanager;

    /**
     * Test prompt for which track in staging a train should depart on.
     */
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testStagingPromptFrom() {

        JUnitOperationsUtil.initOperationsData();
        Setup.setStagingPromptFromEnabled(true);

        // two sets of cars in staging
        JUnitOperationsUtil.initOperationsData();

        Train train2 = tmanager.getTrainById("2");

        // should cause a prompt asking which track to use
        Thread build = new Thread(new Runnable() {
            @Override
            public void run() {
                new TrainBuilder().build(train2);
            }
        });
        build.setName("Build Train 2"); // NOI18N
        build.start();

        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("SelectDepartureTrack"), Bundle.getMessage("ButtonOK"));

        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.TERMINATED);
        }, "wait for build to complete");

        Assert.assertTrue("Train status", train2.isBuilt());

        train2.reset();

        // now try the cancel option
        Thread build2 = new Thread(new Runnable() {
            @Override
            public void run() {
                new TrainBuilder().build(train2);
            }
        });
        build2.setName("Build Train 2"); // NOI18N
        build2.start();

        JUnitUtil.waitFor(() -> {
            return build2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("SelectDepartureTrack"), "Cancel");

        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.TERMINATED);
        }, "wait for build to complete");

        Assert.assertFalse("Train status", train2.isBuilt());

        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    /**
     * Test prompt selecting which track to use in staging.
     */
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testStagingPromptTo() {

        JUnitOperationsUtil.initOperationsData();
        Setup.setStagingPromptToEnabled(true);
        
        Train train2 = tmanager.getTrainById("2");

        // should cause prompt for track into staging
        Thread build = new Thread(new Runnable() {
            @Override
            public void run() {
                new TrainBuilder().build(train2);
            }
        });
        build.setName("Build Train 2"); // NOI18N
        build.start();

        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("SelectArrivalTrack"), Bundle.getMessage("ButtonOK"));

        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.TERMINATED);
        }, "wait for build to complete");

        Assert.assertTrue("Train status", train2.isBuilt());

        train2.reset();

        // now try the cancel option
        Thread build2 = new Thread(new Runnable() {
            @Override
            public void run() {
                new TrainBuilder().build(train2);
            }
        });
        build2.setName("Build Train 2"); // NOI18N
        build2.start();

        JUnitUtil.waitFor(() -> {
            return build2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("SelectArrivalTrack"), "Cancel");

        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.TERMINATED);
        }, "wait for build to complete");

        Assert.assertFalse("Train status", train2.isBuilt());
        
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testBuildFailedMessage() {

        JUnitOperationsUtil.initOperationsData();
        // enable build failure messages
        tmanager.setBuildMessagesEnabled(true);
        
        Train train2 = tmanager.getTrainById("2");
        // make train build fail by removing train's route
        train2.setRoute(null);

        // should cause failure dialog to appear
        Thread build = new Thread(new Runnable() {
            @Override
            public void run() {
                new TrainBuilder().build(train2);
            }
        });
        build.setName("Build Train 2"); // NOI18N
        build.start();

        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("buildErrorMsg"),
                new Object[]{train2.getName(), train2.getDescription()}), Bundle.getMessage("ButtonOK"));
        
        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.TERMINATED);
        }, "wait for build to complete");
        
        Assert.assertFalse("Train status", train2.isBuilt());
    }
    
    /**
     * Test warning message.
     */
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testWarningMessage() {

        JUnitOperationsUtil.initOperationsData();
        tmanager.setBuildMessagesEnabled(true);
        
        // cause 1 warning message
        Setup.setCarRoutingEnabled(false);
        
        // Route Northend-NI-Southend
        Train train2 = tmanager.getTrainById("2");

        // should cause warning dialog to appear
        Thread build = new Thread(new Runnable() {
            @Override
            public void run() {
                new TrainBuilder().build(train2);
            }
        });
        build.setName("Build Train 2"); // NOI18N
        build.start();

        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        // dialog "Build report for train (SFF) has 1 warnings"
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("buildWarningMsg"),
                new Object[]{train2.getName(), 1}), Bundle.getMessage("ButtonOK"));
    }
    
    /**
     * Test failure message when cars in staging are stuck there.
     */
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
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

        // should cause failure dialog to appear
        Thread build = new Thread(new Runnable() {
            @Override
            public void run() {
                new TrainBuilder().build(train2);
            }
        });
        build.setName("Build Train 2"); // NOI18N
        build.start();

        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        // dialog "remove cars from staging" or continue by pressing "OK"
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("buildErrorMsg"),
                new Object[]{train2.getName(), train2.getDescription()}), Bundle.getMessage("ButtonOK"));
        
        // thread can go from RUNNABLE to WAITING to RUNNABLE to WAITING .....
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");
        
        // next prompt asks if cars are to be released from train by reset
        JemmyUtil.pressDialogButton(Bundle.getMessage("buildResetTrain"), Bundle.getMessage("ButtonNo"));
        
        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.TERMINATED);
        }, "wait for build to complete");
        
        Assert.assertFalse("Train status", train2.isBuilt());
        
        //confirm that the two cabooses are assigned to the train
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
     * Test failure message when cars in staging are stuck there.
     * Release cars and engines by train reset.
     */
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
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
            // dialog "remove cars from staging" or continue by pressing "OK", press OK.
            JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("buildErrorMsg"),
                new Object[]{train2.getName(), train2.getDescription()}), Bundle.getMessage("ButtonOK"));
        });
        t1.setName("Build Train 2 Dialog 1 Thread");
        t1.start();
        
        Thread t2 = new Thread(() -> {
            // prompt asks if cars are to be released from train by reset, press Yes.
            JemmyUtil.pressDialogButton(Bundle.getMessage("buildResetTrain"), Bundle.getMessage("ButtonYes"));
        });
        t2.setName("Build Train 2 Dialog 2 Thread");
        t2.start();
        
        // should cause failure dialog to appear
        Thread build = new Thread(() -> {
            new TrainBuilder().build(train2);
        });
        build.setName("Build Train 2"); // NOI18N
        build.start();
        
        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Build Train 2 Dialog 1 Thread did not close"); // dialog "remove cars from staging" or continue by pressing "OK"
        
        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        }, "Build Train 2 Dialog 2 Thread did not close"); // asks if cars are to be released from train by reset
        
        JUnitUtil.waitFor(() -> {
            return !build.isAlive();
        }, "wait for build to complete");
        
        Assert.assertFalse("Train status", train2.isBuilt());
        
        //confirm that engines and cars are released from train
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
     * Test failure message when cars in staging are stuck there.
     * Remove stuck cars from staging.
     */
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
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

        // should cause failure dialog to appear
        Thread build = new Thread(new Runnable() {
            @Override
            public void run() {
                new TrainBuilder().build(train2);
            }
        });
        build.setName("Build Train 2"); // NOI18N
        build.start();

        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        // dialog "remove cars from staging" or continue by pressing "OK"
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("buildErrorMsg"),
                new Object[]{train2.getName(), train2.getDescription()}), Bundle.getMessage("buttonRemoveCars"));
        
        // thread can go from RUNNABLE to WAITING to RUNNABLE to WAITING .....
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.WAITING);
        },"wait for prompt");
        
        // next prompt asks if cars are to be released from train by reset
        JemmyUtil.pressDialogButton(Bundle.getMessage("buildResetTrain"), Bundle.getMessage("ButtonYes"));
        
        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.TERMINATED);
        }, "wait for build to complete");
        
        Assert.assertFalse("Train status", train2.isBuilt());
        
        //confirm that engines and cars are released from train
        Assert.assertEquals("Train assignment", null, c1.getTrain());
        Assert.assertEquals("Train assignment", null, c2.getTrain());
        
        Assert.assertEquals("Train assignment", null, c3.getTrain());
        Assert.assertEquals("Train assignment", null, c4.getTrain());
        
        Assert.assertEquals("Track assignment", null, c3.getTrack());
        Assert.assertEquals("Track assignment", null, c4.getTrack());
        Assert.assertEquals("Track assignment", null, c11.getTrack());
    }
    
    /**
     * Test failure message when build fails, release engines
     * by reset. No cars in staging for this test.
     */
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
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

        // should cause failure dialog to appear
        Thread build = new Thread(new Runnable() {
            @Override
            public void run() {
                new TrainBuilder().build(train2);
            }
        });
        build.setName("Build Train 2"); // NOI18N
        build.start();

        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        // dialog remove engines from staging or continue by pressing OK
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("buildErrorMsg"),
                new Object[]{train2.getName(), train2.getDescription()}), Bundle.getMessage("ButtonOK"));
        
        // thread can go from RUNNABLE to WAITING to RUNNABLE to WAITING .....
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.WAITING);
        },"wait for prompt");
        
        // next prompt asks if cars are to be released from train by reset
        JemmyUtil.pressDialogButton(Bundle.getMessage("buildResetTrain"), Bundle.getMessage("ButtonYes"));
        
        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.TERMINATED);
        }, "wait for build to complete");
        
        Assert.assertFalse("Train status", train2.isBuilt());
        
        //confirm that the two cabooses are assigned to the train
        Assert.assertEquals("Train assignment", null, e1.getTrain());
        Assert.assertEquals("Train assignment", null, e2.getTrain());
        Assert.assertEquals("Train assignment", null, e3.getTrain());
        Assert.assertEquals("Train assignment", null, e4.getTrain());
    }
    
    /**
     * Test failure message when build fails, Don't release engines
     * by reset. No cars in staging for this test.
     */
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
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

        // should cause failure dialog to appear
        Thread build = new Thread(new Runnable() {
            @Override
            public void run() {
                new TrainBuilder().build(train2);
            }
        });
        build.setName("Build Train 2"); // NOI18N
        build.start();

        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        // dialog remove engines from staging or continue by pressing OK
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("buildErrorMsg"),
                new Object[]{train2.getName(), train2.getDescription()}), Bundle.getMessage("ButtonOK"));
        
        // thread can go from RUNNABLE to WAITING to RUNNABLE to WAITING .....
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.WAITING);
        },"wait for prompt");
        
        // next prompt asks if cars are to be released from train by reset
        JemmyUtil.pressDialogButton(Bundle.getMessage("buildResetTrain"), Bundle.getMessage("ButtonNo"));
        
        JUnitUtil.waitFor(() -> {
            return build.getState().equals(Thread.State.TERMINATED);
        }, "wait for build to complete");
        
        // only e3 and e4 have been assigned to train
        Assert.assertFalse("Train status", train2.isBuilt());
        
        //confirm that the two cabooses are assigned to the train
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
