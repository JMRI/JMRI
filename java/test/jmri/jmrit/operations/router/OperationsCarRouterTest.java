
package jmri.jmrit.operations.router;

import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.schedules.Schedule;
import jmri.jmrit.operations.locations.schedules.ScheduleItem;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.Kernel;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Operations Router class
 *
 * @author	Daniel Boudreau Copyright (C) 2010, 2011, 2013
 */
public class OperationsCarRouterTest extends OperationsTestCase {

    private final int DIRECTION_ALL = Location.EAST + Location.WEST + Location.NORTH + Location.SOUTH;
    
    @Test
    public void testCarRoutingDefaults() {
        
        Assert.assertTrue("Default car routing true", Setup.isCarRoutingEnabled());
        Assert.assertTrue("Default routing using yards", Setup.isCarRoutingViaYardsEnabled());
        Assert.assertFalse("Default routing through staging", Setup.isCarRoutingViaStagingEnabled());
        
        Assert.assertEquals("default build report level", Setup.BUILD_REPORT_VERY_DETAILED, Setup.getBuildReportLevel());                
    }

    /**
     * Original test, has been broken down into multiple shorter tests and
     * enhanced, see testCarRoutingA - E. Note that for this test the build
     * report level is set to normal, which increases test coverage, the main
     * reason this test still exists.
     * 
     * Test car routing. First set of tests confirm proper operation of just one
     * location. The next set of tests confirms operation using one train and
     * two locations. When this test was written, routing up to 5 trains and 6
     * locations was supported.
     * <p>
     */
    @Test
    public void testCarRouting() {
        // change report level to increase test coverage
        Setup.setRouterBuildReportLevel(Setup.BUILD_REPORT_NORMAL);
        // now load up the managers
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Router router = InstanceManager.getDefault(Router.class);

        // create 6 locations and tracks
        Location acton = lmanager.newLocation("Acton");
        Assert.assertEquals("Location 1 Name", "Acton", acton.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, acton.getLength());

        Track actonSpur1 = acton.addTrack("Acton Spur 1", Track.SPUR);
        actonSpur1.setLength(300);
        Assert.assertEquals("Location actonSpur1 Name", "Acton Spur 1", actonSpur1.getName());
        Assert.assertEquals("Location actonSpur1 Length", 300, actonSpur1.getLength());

        Track actonSpur2 = acton.addTrack("Acton Spur 2", Track.SPUR);
        actonSpur2.setLength(300);
        Assert.assertEquals("Location actonSpur2 Name", "Acton Spur 2", actonSpur2.getName());
        Assert.assertEquals("Location actonSpur2 Length", 300, actonSpur2.getLength());

        Track actonYard = acton.addTrack("Acton Yard 1", Track.YARD);
        actonYard.setLength(400);
        Assert.assertEquals("Location actonYard Name", "Acton Yard 1", actonYard.getName());
        Assert.assertEquals("Location actonYard Length", 400, actonYard.getLength());

        Track actonInterchange1 = acton.addTrack("Acton Interchange 1", Track.INTERCHANGE);
        actonInterchange1.setLength(500);
        Assert.assertEquals("Track actonInterchange Name", "Acton Interchange 1", actonInterchange1.getName());
        Assert.assertEquals("Track actonInterchange Length", 500, actonInterchange1.getLength());
        Assert.assertEquals("Track actonInterchange Train Directions", DIRECTION_ALL, actonInterchange1.getTrainDirections());

        // add a second interchange track
        Track actonInterchange2 = acton.addTrack("Acton Interchange 2", Track.INTERCHANGE);
        actonInterchange2.setLength(500);
        // bias tracks
        actonInterchange2.setMoves(100);
        Assert.assertEquals("Track actonInterchange2 Name", "Acton Interchange 2", actonInterchange2.getName());
        Assert.assertEquals("Track actonInterchange2 Length", 500, actonInterchange2.getLength());
        Assert.assertEquals("Track actonInterchange2 Train Directions", DIRECTION_ALL, actonInterchange2.getTrainDirections());

        Location boston = lmanager.newLocation("Boston");
        Assert.assertEquals("Location 1 Name", "Boston", boston.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, boston.getLength());

        Track bostonSpur1 = boston.addTrack("Boston Spur 1", Track.SPUR);
        bostonSpur1.setLength(300);
        Assert.assertEquals("Location bostonSpur1 Name", "Boston Spur 1", bostonSpur1.getName());
        Assert.assertEquals("Location bostonSpur1 Length", 300, bostonSpur1.getLength());

        Track bostonSpur2 = boston.addTrack("Boston Spur 2", Track.SPUR);
        bostonSpur2.setLength(300);
        Assert.assertEquals("Location bostonSpur2 Name", "Boston Spur 2", bostonSpur2.getName());
        Assert.assertEquals("Location bostonSpur2 Length", 300, bostonSpur2.getLength());

        Track bostonYard = boston.addTrack("Boston Yard 1", Track.YARD);
        bostonYard.setLength(400);
        Assert.assertEquals("Location bostonYard Name", "Boston Yard 1", bostonYard.getName());
        Assert.assertEquals("Location bostonYard Length", 400, bostonYard.getLength());

        Track bostonInterchange = boston.addTrack("Boston Interchange 1", Track.INTERCHANGE);
        bostonInterchange.setLength(500);
        Assert.assertEquals("Track bostonInterchange Name", "Boston Interchange 1", bostonInterchange.getName());
        Assert.assertEquals("Track bostonInterchange Length", 500, bostonInterchange.getLength());

        Location chelmsford = lmanager.newLocation("Chelmsford");
        Assert.assertEquals("Location 1 Name", "Chelmsford", chelmsford.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, chelmsford.getLength());

        Track chelmsfordSpur1 = chelmsford.addTrack("Chelmsford Spur 1", Track.SPUR);
        chelmsfordSpur1.setLength(300);
        Assert.assertEquals("Location chelmsfordSpur1 Name", "Chelmsford Spur 1", chelmsfordSpur1.getName());
        Assert.assertEquals("Location chelmsfordSpur1 Length", 300, chelmsfordSpur1.getLength());

        Track chelmsfordSpur2 = chelmsford.addTrack("Chelmsford Spur 2", Track.SPUR);
        chelmsfordSpur2.setLength(300);
        Assert.assertEquals("Location chelmsfordSpur2 Name", "Chelmsford Spur 2", chelmsfordSpur2.getName());
        Assert.assertEquals("Location chelmsfordSpur2 Length", 300, bostonSpur2.getLength());

        Track chelmsfordYard = chelmsford.addTrack("Chelmsford Yard 1", Track.YARD);
        chelmsfordYard.setLength(400);
        Assert.assertEquals("Location chelmsfordYard Name", "Chelmsford Yard 1", chelmsfordYard.getName());
        Assert.assertEquals("Location chelmsfordYard Length", 400, chelmsfordYard.getLength());

        Track chelmsfordInterchange = chelmsford.addTrack("Chelmsford Interchange 1", Track.INTERCHANGE);
        chelmsfordInterchange.setLength(500);
        Assert.assertEquals("Track chelmsfordInterchange Name", "Chelmsford Interchange 1", chelmsfordInterchange.getName());
        Assert.assertEquals("Track chelmsfordInterchange Length", 500, chelmsfordInterchange.getLength());

        Location danvers = lmanager.newLocation("essex MA");
        Track danversSpur1 = danvers.addTrack("Danvers Spur 1", Track.SPUR);
        danversSpur1.setLength(300);
        Track danversSpur2 = danvers.addTrack("Danvers Spur 2", Track.SPUR);
        danversSpur2.setLength(300);
        Track danversYard = danvers.addTrack("Danvers Yard 1", Track.YARD);
        danversYard.setLength(400);
        Track danversInterchange = danvers.addTrack("Danvers Interchange 1", Track.INTERCHANGE);
        danversInterchange.setLength(500);

        Location essex = lmanager.newLocation("Essex");
        Track essexSpur1 = essex.addTrack("Essex Spur 1", Track.SPUR);
        essexSpur1.setLength(300);
        Track essexSpur2 = essex.addTrack("Essex Spur 2", Track.SPUR);
        essexSpur2.setLength(300);
        Track essexYard = essex.addTrack("Essex Yard 1", Track.YARD);
        essexYard.setLength(400);
        Track essexInterchange = essex.addTrack("Essex Interchange 1", Track.INTERCHANGE);
        essexInterchange.setLength(500);

        Location foxboro = lmanager.newLocation("Foxboro");
        Track foxboroSpur1 = foxboro.addTrack("Foxboro Spur 1", Track.SPUR);
        foxboroSpur1.setLength(300);
        Track foxboroSpur2 = foxboro.addTrack("Foxboro Spur 2", Track.SPUR);
        foxboroSpur2.setLength(300);
        Track foxboroYard = foxboro.addTrack("Foxboro Yard 1", Track.YARD);
        foxboroYard.setLength(400);
        Track foxboroInterchange = foxboro.addTrack("Foxboro Interchange 1", Track.INTERCHANGE);
        foxboroInterchange.setLength(500);

        // create 2 cars        
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("BA", "3", "Boxcar", "40", "DAB", "1984", actonSpur1, 0);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("BB", "4", "Flat", "40", "AT", "1-86", actonSpur1, 0);
        
        Assert.assertFalse("Try routing no final destination", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        
        Assert.assertFalse("Try routing no final destination", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "", c4.getDestinationName());

        // test disable routing
        Setup.setCarRoutingEnabled(false);
        c3.setFinalDestination(boston);
        Assert.assertFalse("Test router disabled", router.setDestination(c3, null, null));
        Assert.assertEquals("Router status", Router.STATUS_ROUTER_DISABLED, router.getStatus());
        Setup.setCarRoutingEnabled(true);

        // first try car routing with just one location
        c3.setFinalDestination(acton);
        Assert.assertFalse("Try routing final destination equal to current", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // now try with next track not equal to current
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertFalse("Try routing final track not equal to current", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // now try with next track equal to current
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur1);
        Assert.assertFalse("Try routing final track equal to current", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // create a local train servicing Acton
        Train actonTrain = tmanager.newTrain("Acton Local");
        Route routeA = rmanager.newRoute("A");
        RouteLocation rlActon1 = routeA.addLocation(acton);
        rlActon1.setTrainIconX(25);	// set train icon coordinates
        rlActon1.setTrainIconY(250);
        actonTrain.setRoute(routeA);

        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Spur 2", c3.getDestinationTrackName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // specify the Acton train
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c3, actonTrain, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Spur 2", c3.getDestinationTrackName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // Set the track length to be less the length of c3
        actonSpur2.setLength(c3.getTotalLength() - 1);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);	// local move, alternate or yard track option should be ignored
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c3, actonTrain, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());
        Assert.assertTrue("Should report that the issue was track length", router.getStatus().startsWith(Track.CAPACITY));

        // restore track length
        actonSpur2.setLength(300);

        // don't allow train to service boxcars
        actonTrain.deleteTypeName("Boxcar");
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertFalse("Try routing with train that doesn't service Boxcar", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car type Flat
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(acton);
        c4.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing with train that service Flat", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c4.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // now allow Boxcar again
        actonTrain.addTypeName("Boxcar");
        Assert.assertTrue("Try routing with train that does service Boxcar", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // don't allow train to service boxcars with road name BA
        actonTrain.addRoadName("BA");
        actonTrain.setRoadOption(Train.EXCLUDE_ROADS);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertFalse("Try routing with train that doesn't service road name BA", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car road name BB
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(acton);
        c4.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing with train that services road BB", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c4.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // now try again but allow road name
        actonTrain.setRoadOption(Train.ALL_ROADS);
        Assert.assertTrue("Try routing with train that does service road name BA", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // don't service cars built before 1985
        actonTrain.setBuiltStartYear("1985");
        actonTrain.setBuiltEndYear("2010");
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertFalse("Try routing with train that doesn't service car built before 1985", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car built after 1985
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(acton);
        c4.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing with train that services car built after 1985", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c4.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // car was built in 1984 should work
        actonTrain.setBuiltStartYear("1983");
        Assert.assertTrue("Try routing with train that doesn't service car built before 1983", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // try car loads
        c3.setLoadName("Tools");
        actonTrain.addLoadName("Tools");
        actonTrain.setLoadOption(Train.EXCLUDE_LOADS);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertFalse("Try routing with train that doesn't service load Tools", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car load "E"
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(acton);
        c4.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing with train that services load E", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c4.getDestinationName());

        actonTrain.setLoadOption(Train.ALL_LOADS);
        Assert.assertTrue("Try routing with train that that does service load Tools", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        // now test by modifying the route
        rlActon1.setPickUpAllowed(false);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertFalse("Try routing with train that doesn't pickup cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        rlActon1.setPickUpAllowed(true);
        Assert.assertTrue("Try routing with train that that can pickup cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        rlActon1.setDropAllowed(false);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertFalse("Try routing with train that doesn't drop cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        rlActon1.setDropAllowed(true);
        Assert.assertTrue("Try routing with train that that can drop cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        rlActon1.setMaxCarMoves(0);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertFalse("Try routing with train that doesn't service location", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        rlActon1.setMaxCarMoves(10);
        Assert.assertTrue("Try routing with train that does service location", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        // test train depart direction
        Assert.assertEquals("check default direction", Track.NORTH, rlActon1.getTrainDirection());
        // set the depart location Acton to service by South bound trains only
        acton.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing with local train that departs north, location south", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        acton.setTrainDirections(Track.NORTH);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing with local train that departs north, location north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        // set the depart track Acton to service by local train only
        actonSpur1.setTrainDirections(0);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing with local only", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        actonSpur1.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with local train that departs north, track north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        // test arrival directions
        // set the arrival track Acton to service by local trains only
        actonSpur2.setTrainDirections(0);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);	// now specify the actual track
        Assert.assertTrue("Try routing with local train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        actonSpur2.setTrainDirections(Track.NORTH);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);	// now specify the actual track
        Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        // add a second local train
        // create a local train servicing Acton
        Route routeA2 = rmanager.newRoute("A2");
        RouteLocation rlA2 = routeA2.addLocation(acton);
        rlA2.setTrainIconX(25);	// set train icon coordinates
        rlA2.setTrainIconY(250);
        Train ActonTrain2 = tmanager.newTrain("Acton Local 2");
        ActonTrain2.setRoute(routeA2);

        // try routing with this train
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Spur 2", c3.getDestinationTrackName());

        // don't allow Acton Local 2 to service boxcars
        ActonTrain2.deleteTypeName("Boxcar");
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        // Should be able to route using Acton Local, but destination should not be set
        Assert.assertTrue("Try routing with train that doesn't service Boxcar", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_THIS_TRAIN, router.getStatus());

        // Two locations one train testing begins
        // set next destination Boston
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        c3.setFinalDestinationTrack(null);
        // should fail no train!
        Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));
        // create a train with a route from Acton to Boston
        Train ActonToBostonTrain = tmanager.newTrain("Acton to Boston");
        Route routeAB = rmanager.newRoute("AB");
        RouteLocation rlActon = routeAB.addLocation(acton);
        RouteLocation rlBoston = routeAB.addLocation(boston);
        rlBoston.setTrainIconX(100);	// set train icon coordinates
        rlBoston.setTrainIconY(250);
        ActonToBostonTrain.setRoute(routeAB);

        // should work
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // try specific train
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        c3.setFinalDestinationTrack(null);
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, ActonToBostonTrain, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // don't allow train to service boxcars
        ActonToBostonTrain.deleteTypeName("Boxcar");
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that doesn't service Boxcar", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car type Flat
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(boston);
        c4.setFinalDestinationTrack(null);
        Assert.assertTrue("Try routing with train that service Flat", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c4.getDestinationName());

        // now allow Boxcar again
        ActonToBostonTrain.addTypeName("Boxcar");
        Assert.assertTrue("Try routing with train that does service Boxcar", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // don't allow train to service boxcars with road name BA
        ActonToBostonTrain.addRoadName("BA");
        ActonToBostonTrain.setRoadOption(Train.EXCLUDE_ROADS);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that doesn't service road name BA", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car road name BB
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(boston);
        Assert.assertTrue("Try routing with train that services road BB", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c4.getDestinationName());

        // now try again but allow road name
        ActonToBostonTrain.setRoadOption(Train.ALL_ROADS);
        Assert.assertTrue("Try routing with train that does service road name BA", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // don't service cars built before 1985
        ActonToBostonTrain.setBuiltStartYear("1985");
        ActonToBostonTrain.setBuiltEndYear("2010");
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that doesn't service car built before 1985", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        // try the car built after 1985
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(boston);
        Assert.assertTrue("Try routing with train that services car built after 1985", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c4.getDestinationName());

        // car was built in 1984 should work
        ActonToBostonTrain.setBuiltStartYear("1983");
        Assert.assertTrue("Try routing with train that doesn't service car built before 1983", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // try car loads
        c3.setLoadName("Tools");
        ActonToBostonTrain.addLoadName("Tools");
        ActonToBostonTrain.setLoadOption(Train.EXCLUDE_LOADS);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that doesn't service load Tools", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        // try the car load "E"
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(boston);
        Assert.assertTrue("Try routing with train that services load E", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c4.getDestinationName());

        ActonToBostonTrain.setLoadOption(Train.ALL_LOADS);
        Assert.assertTrue("Try routing with train that that does service load Tools", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // don't allow Boston to service Flat
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(boston);
        boston.deleteTypeName("Flat");
        Assert.assertFalse("Try routing with Boston that does not service Flat", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "", c4.getDestinationName());
        Assert.assertTrue("Router status", router.getStatus().startsWith(Track.TYPE));

        // restore Boston can service Flat
        boston.addTypeName("Flat");

        // now test by modifying the route
        rlActon.setPickUpAllowed(false);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that doesn't pickup cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        rlActon.setPickUpAllowed(true);
        Assert.assertTrue("Try routing with train that that can pickup cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        rlBoston.setDropAllowed(false);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that doesn't drop cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        rlBoston.setDropAllowed(true);
        Assert.assertTrue("Try routing with train that that can drop cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        rlBoston.setMaxCarMoves(0);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that doesn't service destination", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        rlBoston.setMaxCarMoves(5);
        Assert.assertTrue("Try routing with train that does service destination", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        rlActon.setMaxCarMoves(0);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that doesn't service location", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        rlActon.setMaxCarMoves(5);
        Assert.assertTrue("Try routing with train that does service location", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // test train depart direction
        Assert.assertEquals("check default direction", Track.NORTH, rlActon.getTrainDirection());
        // set the depart location Acton to service by South bound trains only
        acton.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);

        // remove the Action local by not allowing train to service boxcars
        actonTrain.deleteTypeName("Boxcar");
        Assert.assertFalse("Try routing with train that departs north, location south", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        acton.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with train that departs north, location north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // set the depart track Acton to service by South bound trains only
        actonSpur1.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that departs north, track south", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        actonSpur1.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // test arrival directions
        // set the arrival location Boston to service by South bound trains only
        boston.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that arrives north, destination south", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        boston.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with train that arrives north, destination north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // set the depart track Acton to service by South bound trains only
        bostonSpur1.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertTrue("Try routing with train that arrives north, but no final track", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston); // the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);	// now specify the actual track
        Assert.assertFalse("Try routing with train that arrives north, now with track", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        bostonSpur1.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        Setup.setOnlyActiveTrainsEnabled(true);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertTrue("Try routing only active trains", router.setDestination(c3, null, null));

        // now deselect the Action to Boston train
        ActonToBostonTrain.setBuildEnabled(false);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing only active trains, Action to Beford deselected", router.setDestination(c3, null, null));
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        Setup.setOnlyActiveTrainsEnabled(false);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertTrue("Try routing, only active trains deselected", router.setDestination(c3, null, null));

        // test yard and alternate track options
        bostonSpur1.setLength(c3.getTotalLength());
        // c4 is the same length as c3, so the track is now full
        Assert.assertEquals("Use up all of the space for bostonSpur1", Track.OKAY, c4.setLocation(boston, bostonSpur1));
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        c3.setFinalDestinationTrack(bostonSpur1);
        Assert.assertTrue("Test search for yard", router.setDestination(c3, null, null));
        Assert.assertEquals("Destination", "Boston", c3.getDestinationName());
        Assert.assertEquals("Destination track should be yard", "Boston Yard 1", c3.getDestinationTrackName());
        // the car was sent to a yard track because the spur was full
        Assert.assertTrue("Should be reporting length issue", router.getStatus().startsWith(Track.LENGTH));

        // remove yard type
        bostonYard.setTrackType(Track.SPUR);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        c3.setFinalDestinationTrack(bostonSpur1);
        Assert.assertTrue("Test search for yard that doesn't exist", router.setDestination(c3, null, null));
        Assert.assertEquals("Destination", "", c3.getDestinationName());
        Assert.assertTrue("Should be reporting length issue", router.getStatus().startsWith(Track.LENGTH));

        // restore yard type
        bostonYard.setTrackType(Track.YARD);

        // test alternate track option
        bostonSpur1.setAlternateTrack(bostonSpur2);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);
        c3.setFinalDestinationTrack(bostonSpur1);
        Assert.assertTrue("Test use alternate", router.setDestination(c3, null, null));
        Assert.assertEquals("Destination", "Boston", c3.getDestinationName());
        Assert.assertEquals("Destination track should be spur", "Boston Spur 2", c3.getDestinationTrackName());
        // the car was sent to the alternate track because the spur was full
        Assert.assertTrue("Should be reporting length issue", router.getStatus().startsWith(Track.LENGTH));

        // restore track length and remove alternate
        bostonSpur1.setLength(300);
        bostonSpur1.setAlternateTrack(null);

        // One train tests complete. Start two train testing.
        // Force first move to be by local train
        actonSpur1.setTrainDirections(0);
        actonTrain.addTypeName("Boxcar");	// restore the local

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);	// the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);	// now specify the actual track

        Assert.assertTrue("Try routing two trains via interchange", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 1", c3.getDestinationTrackName());

        // don't allow use of interchange track
        actonInterchange1.setDropOption(Track.TRAINS);

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);	// the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);	// now specify the actual track

        Assert.assertTrue("Try routing two trains via interchange", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 2", c3.getDestinationTrackName());

        actonInterchange2.setDropOption(Track.TRAINS);

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);	// the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);	// now specify the actual track

        Assert.assertTrue("Try routing two trains via yard", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Yard 1", c3.getDestinationTrackName());

        // allow use of interchange track
        actonInterchange1.setDropOption(Track.ANY);
        actonInterchange2.setDropOption(Track.ANY);

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);	// the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);

        // allow Boxcars
        ActonTrain2.addTypeName("Boxcar");

        Assert.assertTrue("Try routing two trains", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 1", c3.getDestinationTrackName());

        // test to see if second interchange track used if first is full
        actonInterchange1.setLength(c3.getTotalLength() - 1);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);	// the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);
        Assert.assertTrue("Try routing two trains to interchange track 2", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 2", c3.getDestinationTrackName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // use yard track if interchange tracks are full
        actonInterchange2.setLength(c3.getTotalLength() - 1);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);	// the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);
        Assert.assertTrue("Try routing two trains to yard track", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Yard 1", c3.getDestinationTrackName());

        // disable using yard tracks for routing
        Setup.setCarRoutingViaYardsEnabled(false);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);	// the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);
        router.setDestination(c3, ActonTrain2, null);
        Assert.assertFalse("Try routing two trains to yard track", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());

        // restore track length
        actonInterchange1.setLength(500);
        actonInterchange2.setLength(500);
        Setup.setCarRoutingViaYardsEnabled(true);

        // don't allow train 2 to service boxcars with road name BA
        ActonTrain2.addRoadName("BA");
        ActonTrain2.setRoadOption(Train.EXCLUDE_ROADS);

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(boston);	// the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);

        // routing should work using train 1, destination and track should not be set
        Assert.assertTrue("Try routing two trains via yard", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());

        // two train testing done!
        // now try up to 5 trains to route car
        // set final destination Chelmsford
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(chelmsford);
        c3.setFinalDestinationTrack(null);
        // should fail no train!
        Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));

        // create a train with a route from Boston to Chelmsford
        Train bostonToChelmsfordTrain = tmanager.newTrain("Boston to Chelmsford");
        Route routeBC = rmanager.newRoute("BC");
        routeBC.addLocation(boston);
        RouteLocation rlchelmsford = routeBC.addLocation(chelmsford);
        rlchelmsford.setTrainIconX(175);	// set train icon coordinates
        rlchelmsford.setTrainIconY(250);
        bostonToChelmsfordTrain.setRoute(routeBC);

        // should work
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 1", c3.getDestinationTrackName());

        // allow train 2 to service boxcars with road name BA
        ActonTrain2.setRoadOption(Train.ALL_ROADS);

        // try with train 2
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(chelmsford);

        // routing should work using train 2
        Assert.assertTrue("Try routing three trains", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 1", c3.getDestinationTrackName());

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(chelmsford);

        // test to see if second interchange track used if first is full
        actonInterchange1.setLength(c3.getTotalLength() - 1);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(chelmsford);	// the final destination for the car
        Assert.assertTrue("Try routing three trains to interchange track 2", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 2", c3.getDestinationTrackName());

        // use yard track if interchange tracks are full
        actonInterchange2.setLength(c3.getTotalLength() - 1);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(chelmsford);	// the final destination for the car
        Assert.assertTrue("Try routing three trains to yard track", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Yard 1", c3.getDestinationTrackName());

        // disable the use of yard tracks for routing
        Setup.setCarRoutingViaYardsEnabled(false);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(chelmsford);	// the final destination for the car
        // both interchange tracks are too short, so there isn't a route
        Assert.assertFalse("Try routing three trains to yard track, option disabled", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());

        // Try again with an interchange track that is long enough for car c3.
        actonInterchange1.setLength(c3.getTotalLength());
        Assert.assertEquals("c4 consumes all of the interchange track", Track.OKAY, c4.setLocation(actonInterchange1.getLocation(), actonInterchange1));
        // Track AI is long enough, but c4 is consuming all of the track, but there is a route!
        Assert.assertTrue("Try routing three trains to yard track, option disabled", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());

        // restore track length
        actonInterchange1.setLength(500);
        actonInterchange2.setLength(500);
        Setup.setCarRoutingViaYardsEnabled(true);

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(chelmsford);	// the final destination for the car
        // don't allow train 2 to service cars built before 1985
        ActonTrain2.setBuiltStartYear("1985");
        ActonTrain2.setBuiltEndYear("2010");
        // routing should work using train 1, but destinations and track should not be set
        Assert.assertTrue("Try routing three trains", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());
        // allow car to be serviced
        // car was built in 1984 should work
        ActonTrain2.setBuiltStartYear("1983");

        // set final destination Danvers
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(danvers);
        // should fail no train!
        Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));

        // create a train with a route from Chelmsford to Danvers
        Train chelmsfordToDanversTrain = tmanager.newTrain("Chelmsford to Danvers");
        Route routeCD = rmanager.newRoute("CD");
        routeCD.addLocation(chelmsford);
        RouteLocation rlDanvers = routeCD.addLocation(danvers);
        rlDanvers.setTrainIconX(250);	// set train icon coordinates
        rlDanvers.setTrainIconY(250);
        chelmsfordToDanversTrain.setRoute(routeCD);

        // should work
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 1", c3.getDestinationTrackName());

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(danvers);	// the final destination for the car

        // routing should work using train 2
        Assert.assertTrue("Try routing four trains", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 1", c3.getDestinationTrackName());

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(danvers);	// the final destination for the car

        // don't allow train 2 to service cars with load Tools
        ActonTrain2.addLoadName("Tools");
        ActonTrain2.setLoadOption(Train.EXCLUDE_LOADS);
        // routing should work using train 1, but destinations and track should not be set
        Assert.assertTrue("Try routing four trains", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());
        // restore train 2
        ActonTrain2.setLoadOption(Train.ALL_LOADS);

        // set final destination Essex
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(essex);
        // should fail no train!
        Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));

        // create a train with a route from Danvers to Essex
        Train danversToEssexTrain = tmanager.newTrain("Danvers to Essex");
        Route routeDE = rmanager.newRoute("DE");
        RouteLocation rlDanvers2 = routeDE.addLocation(danvers);
        RouteLocation rlEssex = routeDE.addLocation(essex);
        // set the number of car moves to 8 for a later test
        rlDanvers2.setMaxCarMoves(8);
        rlEssex.setMaxCarMoves(8);
        rlEssex.setTrainIconX(25);	// set train icon coordinates
        rlEssex.setTrainIconY(275);
        danversToEssexTrain.setRoute(routeDE);

        // should work
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 1", c3.getDestinationTrackName());

        // routing should work using train 2
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(essex);
        Assert.assertTrue("Try routing five trains", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 1", c3.getDestinationTrackName());

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(essex);
        // don't allow train 2 to pickup
        rlA2.setPickUpAllowed(false);
        // routing should work using train 1, but destinations and track should not be set
        Assert.assertTrue("Try routing five trains", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("Check status", Router.STATUS_NOT_THIS_TRAIN, router.getStatus());

        // set final destination Foxboro
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(foxboro);
        // should fail no train!
        Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));

        // create a train with a route from Essex to Foxboro
        Train EssexToFoxboroTrain = tmanager.newTrain("Essex to Foxboro");
        Route routeEF = rmanager.newRoute("EF");
        routeEF.addLocation(essex);
        RouteLocation rlFoxboro = routeEF.addLocation(foxboro);
        rlFoxboro.setTrainIconX(100);	// set train icon coordinates
        rlFoxboro.setTrainIconY(275);
        EssexToFoxboroTrain.setRoute(routeEF);

        // 6th train should fail!  Only 5 trains supported
        Assert.assertFalse("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertFalse("Try routing with final destination and train", router.setDestination(c3, actonTrain, null));
        Assert.assertFalse("Try routing with final destination and train", router.setDestination(c3, ActonTrain2, null));

        // get rid of the local train
        actonSpur1.setTrainDirections(Track.NORTH);

        // now should work!
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Boston Interchange 1", c3.getDestinationTrackName());

        //TODO test restrict location by car type
        //TODO test restrict tracks by type road, load
    }

    /**
     * Test routing using one train and the train is a local. a local services
     * only one location
     */
    @Test
    public void testCarRoutingOneLocalTrain() {
        // load up the managers
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Router router = InstanceManager.getDefault(Router.class);
        
        // create 7 locations and tracks, use 2
        JUnitOperationsUtil.createSevenNormalLocations();
        
        Location acton = lmanager.getLocationByName("Acton");      
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);
        Track actonSpur2 = acton.getTrackByName("Acton Spur 2", Track.SPUR);
        Track actonYard = acton.getTrackByName("Acton Yard 1", Track.YARD);

        Location boston = lmanager.getLocationByName("Boston");
        
        // make the yard the alternate
        actonSpur1.setAlternateTrack(actonYard);

        // create 2 cars
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("BA", "3", "Boxcar", "40", "DAB", "1984", null, 0);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("BB", "4", "Flat", "40", "AT", "1-86", actonSpur1, 0);
        
        // test trying to route car that isn't on a track
        Assert.assertFalse("Car not on a track", router.setDestination(c3, null, null));
        
        // test trying to route car that only has a track
        c3.setLocation(null, actonSpur1);
        Assert.assertFalse("Car doesn't have a location", router.setDestination(c3, null, null));
        
        // give the car a destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Car doesn't have a location", router.setDestination(c3, null, null));

        Assert.assertEquals("place car at Acton", Track.OKAY, c3.setLocation(acton, actonSpur1));
        Assert.assertFalse("Try routing no final destination", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        Assert.assertFalse("Try routing no final destination", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "", c4.getDestinationName());

        // test disable routing
        Setup.setCarRoutingEnabled(false);
        
        Assert.assertFalse("Test router disabled", router.setDestination(c3, null, null));
        Assert.assertEquals("Router status", Router.STATUS_ROUTER_DISABLED, router.getStatus());
        Setup.setCarRoutingEnabled(true);

        // first try car routing with only destination, no track
        c3.setFinalDestination(acton);
        Assert.assertFalse("Try routing final destination equal to current", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // now try with next track not equal to current
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertFalse("Try routing final track not equal to current", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // now try with next track equal to current
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur1);
        Assert.assertFalse("Try routing final track equal to current", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // create a local train servicing Acton
        Train actonTrain = tmanager.newTrain("Acton Local");
        Route routeA = rmanager.newRoute("A");
        RouteLocation rlA = routeA.addLocation(acton);
        rlA.setTrainIconX(25);  // set train icon coordinates
        rlA.setTrainIconY(250);
        actonTrain.setRoute(routeA);

        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Spur 2", c3.getDestinationTrackName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // specify the Acton train
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c3, actonTrain, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Spur 2", c3.getDestinationTrackName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // Set the track length to be less the length of c3
        actonSpur2.setLength(c3.getTotalLength() - 1);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);  // local move, alternate or yard track option should be ignored
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c3, actonTrain, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());
        Assert.assertTrue("Should report that the issue was track length", router.getStatus().startsWith(Track.CAPACITY));

        // restore track length
        actonSpur2.setLength(300);

        // don't allow train to service boxcars
        actonTrain.deleteTypeName("Boxcar");
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertFalse("Try routing with train that doesn't service Boxcar", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car type Flat
        c4.setDestination(null, null);  // clear previous destination
        c4.setFinalDestination(acton);
        c4.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing with train that service Flat", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c4.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // now allow Boxcar again
        actonTrain.addTypeName("Boxcar");
        Assert.assertTrue("Try routing with train that does service Boxcar", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // don't allow train to service boxcars with road name BA
        actonTrain.addRoadName("BA");
        actonTrain.setRoadOption(Train.EXCLUDE_ROADS);
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertFalse("Try routing with train that doesn't service road name BA", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car road name BB
        c4.setDestination(null, null);  // clear previous destination
        c4.setFinalDestination(acton);
        c4.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing with train that services road BB", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c4.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // now try again but allow road name
        actonTrain.setRoadOption(Train.ALL_ROADS);
        Assert.assertTrue("Try routing with train that does service road name BA", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // don't service cars built before 1985
        actonTrain.setBuiltStartYear("1985");
        actonTrain.setBuiltEndYear("2010");
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertFalse("Try routing with train that doesn't service car built before 1985", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car built after 1985
        c4.setDestination(null, null);  // clear previous destination
        c4.setFinalDestination(acton);
        c4.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing with train that services car built after 1985", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c4.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // car was built in 1984 should work
        actonTrain.setBuiltStartYear("1983");
        Assert.assertTrue("Try routing with train that doesn't service car built before 1983", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // try car loads
        c3.setLoadName("Tools");
        actonTrain.addLoadName("Tools");
        actonTrain.setLoadOption(Train.EXCLUDE_LOADS);

        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertFalse("Try routing with train that doesn't service load Tools", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car load "E"
        c4.setDestination(null, null);  // clear previous destination
        c4.setFinalDestination(acton);
        c4.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing with train that services load E", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c4.getDestinationName());

        actonTrain.setLoadOption(Train.ALL_LOADS);
        Assert.assertTrue("Try routing with train that that does service load Tools", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        // now test by modifying the route
        rlA.setPickUpAllowed(false);
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertFalse("Try routing with train that doesn't pickup cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        rlA.setPickUpAllowed(true);
        Assert.assertTrue("Try routing with train that that can pickup cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        rlA.setDropAllowed(false);
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertFalse("Try routing with train that doesn't drop cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        rlA.setDropAllowed(true);
        Assert.assertTrue("Try routing with train that that can drop cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        rlA.setMaxCarMoves(0);
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertFalse("Try routing with train that doesn't service location", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        rlA.setMaxCarMoves(10);
        Assert.assertTrue("Try routing with train that does service location", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        // test train depart direction
        Assert.assertEquals("check default direction", Track.NORTH, rlA.getTrainDirection());
        // set the depart location Acton to service by South bound trains only
        acton.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing with local train that departs north, location south", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        acton.setTrainDirections(Track.NORTH);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing with local train that departs north, location north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        // set the depart track Acton to service by local train only
        actonSpur1.setTrainDirections(0);

        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing with local only", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        actonSpur1.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with local train that departs north, track north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        // test arrival directions
        // set the arrival track Acton to service by local trains only
        actonSpur2.setTrainDirections(0);

        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);  // now specify the actual track
        Assert.assertTrue("Try routing with local train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        actonSpur2.setTrainDirections(Track.NORTH);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);  // now specify the actual track
        Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());

        // add a second local train
        // create a local train servicing Acton
        Route routeA2 = rmanager.newRoute("A2");
        RouteLocation rlA2 = routeA2.addLocation(acton);
        rlA2.setTrainIconX(25); // set train icon coordinates
        rlA2.setTrainIconY(250);
        Train ActonTrain2 = tmanager.newTrain("Acton Local 2");
        ActonTrain2.setRoute(routeA2);

        // try routing with this train
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Spur 2", c3.getDestinationTrackName());

        // don't allow Acton Local 2 to service boxcars
        ActonTrain2.deleteTypeName("Boxcar");
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        // Should be able to route using Acton Local, but destination should not be set
        Assert.assertTrue("Try routing with train that doesn't service Boxcar", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_THIS_TRAIN, router.getStatus());
        
        // test alternate track
        ActonTrain2.addTypeName("Boxcar"); // restore
        c3.setDestination(null, null);  // clear previous destination
        
        // move car to acton spur 2 and route to spur 1
        Assert.assertEquals("place car at Acton", Track.OKAY, c3.setLocation(acton, actonSpur2));
        // reduce spur 1 length to only support 1 car
        actonSpur1.setLength(80); // each boxcar is 40' + couplers 4'
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur1);

        Assert.assertTrue("Test Alternate track", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", acton, c3.getDestination());
        Assert.assertEquals("Check car's destination track", actonYard, c3.getDestinationTrack());
                
        // now don't allow alternate to service car type Boxcar
        rlA.setMaxTrainLength(200); // restore
        actonYard.deleteTypeName("Boxcar");
        c3.setDestination(null, null);  // clear previous destination
 
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur1);

        Assert.assertTrue("Test Alternate track", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", null, c3.getDestination());
        Assert.assertEquals("Check car's destination track", null, c3.getDestinationTrack());
    }
    
    /**
     * Tests routing using one train and two locations.
     */
    @Test
    public void testCarRoutingOneTrain() {
        // now load up the managers
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Router router = InstanceManager.getDefault(Router.class);
        
        // create 7 locations and tracks, use 2
        JUnitOperationsUtil.createSevenNormalLocations();
        
        Location acton = lmanager.getLocationByName("Acton");      
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);

        Location boston = lmanager.getLocationByName("Boston");
        Track bostonSpur1 = boston.getTrackByName("Boston Spur 1", Track.SPUR);
        Track bostonSpur2 = boston.getTrackByName("Boston Spur 2", Track.SPUR);
        Track bostonYard1 = boston.getTrackByName("Boston Yard 1", Track.YARD);
        Track bostonYard2 = boston.getTrackByName("Boston Yard 2", Track.YARD);
        boston.deleteTrack(bostonYard2); // need to delete this track for this test

        // create 2 cars
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("BA", "3", "Boxcar", "40", "DAB", "1984", actonSpur1, 0);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("BB", "4", "Flat", "40", "AT", "1-86", actonSpur1, 0);

        Assert.assertFalse("Try routing no final destination", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        Assert.assertFalse("Try routing no final destination", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "", c4.getDestinationName());

        // create a local train servicing Acton
        Train tActon = tmanager.newTrain("Acton Local");
        Route routeA = rmanager.newRoute("A");
        RouteLocation rlActon1 = routeA.addLocation(acton);
        rlActon1.setTrainIconX(25); // set train icon coordinates
        rlActon1.setTrainIconY(250);
        tActon.setRoute(routeA);

        // add a second local train
        // create a local train servicing Acton
        Train tActon2 = tmanager.newTrain("Acton Local 2");
        Route routeA2 = rmanager.newRoute("A2");
        RouteLocation rlA2 = routeA2.addLocation(acton);
        rlA2.setTrainIconX(25); // set train icon coordinates
        rlA2.setTrainIconY(250);
        tActon2.setRoute(routeA2);

        // don't allow Acton Local 2 to service boxcars
        tActon2.deleteTypeName("Boxcar");

        // Two locations one train testing begins
        // set next destination Boston
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        c3.setFinalDestinationTrack(null);
        // should fail no train!
        Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));
        // create a train with a route from Acton to Boston
        Train tActonToBoston = tmanager.newTrain("Acton to Boston");
        Route routeAB = rmanager.newRoute("AB");
        RouteLocation rlActon = routeAB.addLocation(acton);
        RouteLocation rlBoston = routeAB.addLocation(boston);
        rlBoston.setTrainIconX(100);   // set train icon coordinates
        rlBoston.setTrainIconY(250);
        tActonToBoston.setRoute(routeAB);

        // should work
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // try specific train
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        c3.setFinalDestinationTrack(null);
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, tActonToBoston, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // don't allow train to service boxcars
        tActonToBoston.deleteTypeName("Boxcar");
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that doesn't service Boxcar", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car type Flat
        c4.setDestination(null, null);  // clear previous destination
        c4.setFinalDestination(boston);
        c4.setFinalDestinationTrack(null);
        Assert.assertTrue("Try routing with train that service Flat", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c4.getDestinationName());

        // now allow Boxcar again
        tActonToBoston.addTypeName("Boxcar");
        Assert.assertTrue("Try routing with train that does service Boxcar", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // don't allow train to service boxcars with road name BA
        tActonToBoston.addRoadName("BA");
        tActonToBoston.setRoadOption(Train.EXCLUDE_ROADS);
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that doesn't service road name BA", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car road name BB
        c4.setDestination(null, null);  // clear previous destination
        c4.setFinalDestination(boston);
        Assert.assertTrue("Try routing with train that services road BB", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c4.getDestinationName());

        // now try again but allow road name
        tActonToBoston.setRoadOption(Train.ALL_ROADS);
        Assert.assertTrue("Try routing with train that does service road name BA", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // don't service cars built before 1985
        tActonToBoston.setBuiltStartYear("1985");
        tActonToBoston.setBuiltEndYear("2010");
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that doesn't service car built before 1985", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        // try the car built after 1985
        c4.setDestination(null, null);  // clear previous destination
        c4.setFinalDestination(boston);
        Assert.assertTrue("Try routing with train that services car built after 1985", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c4.getDestinationName());

        // car was built in 1984 should work
        tActonToBoston.setBuiltStartYear("1983");
        Assert.assertTrue("Try routing with train that doesn't service car built before 1983", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // try car loads
        c3.setLoadName("Tools");
        tActonToBoston.addLoadName("Tools");
        tActonToBoston.setLoadOption(Train.EXCLUDE_LOADS);

        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that doesn't service load Tools", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        // try the car load "E"
        c4.setDestination(null, null);  // clear previous destination
        c4.setFinalDestination(boston);
        Assert.assertTrue("Try routing with train that services load E", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c4.getDestinationName());

        tActonToBoston.setLoadOption(Train.ALL_LOADS);
        Assert.assertTrue("Try routing with train that that does service load Tools", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // don't allow Boston to service Flat
        c4.setDestination(null, null);  // clear previous destination
        c4.setFinalDestination(boston);
        boston.deleteTypeName("Flat");
        Assert.assertFalse("Try routing with Boston that does not service Flat", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "", c4.getDestinationName());
        Assert.assertTrue("Router status", router.getStatus().startsWith(Track.TYPE));

        // restore Boston can service Flat
        boston.addTypeName("Flat");

        // now test by modifying the route
        rlActon.setPickUpAllowed(false);
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that doesn't pickup cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        rlActon.setPickUpAllowed(true);
        Assert.assertTrue("Try routing with train that that can pickup cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        rlBoston.setDropAllowed(false);
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that doesn't drop cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        rlBoston.setDropAllowed(true);
        Assert.assertTrue("Try routing with train that that can drop cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        rlBoston.setMaxCarMoves(0);
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that doesn't service destination", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        rlBoston.setMaxCarMoves(5);
        Assert.assertTrue("Try routing with train that does service destination", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        rlActon.setMaxCarMoves(0);
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that doesn't service location", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        rlActon.setMaxCarMoves(5);
        Assert.assertTrue("Try routing with train that does service location", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // test train depart direction
        Assert.assertEquals("check default direction", Track.NORTH, rlActon.getTrainDirection());
        // set the depart location Acton to service by South bound trains only
        acton.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);

        // remove the Action local by not allowing train to service boxcars
        tActon.deleteTypeName("Boxcar");
        Assert.assertFalse("Try routing with train that departs north, location south", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        acton.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with train that departs north, location north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // set the depart track Acton to service by South bound trains only
        actonSpur1.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that departs north, track south", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        actonSpur1.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // test arrival directions
        // set the arrival location Boston to service by South bound trains only
        boston.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing with train that arrives north, destination south", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        boston.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with train that arrives north, destination north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        // set the depart track Acton to service by South bound trains only
        bostonSpur1.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertTrue("Try routing with train that arrives north, but no final track", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston); // the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);  // now specify the actual track
        Assert.assertFalse("Try routing with train that arrives north, now with track", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        bostonSpur1.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());

        Setup.setOnlyActiveTrainsEnabled(true);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertTrue("Try routing only active trains", router.setDestination(c3, null, null));

        // now deselect the Action to Boston train
        tActonToBoston.setBuildEnabled(false);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertFalse("Try routing only active trains, Action to Beford deselected", router.setDestination(c3, null, null));
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        Setup.setOnlyActiveTrainsEnabled(false);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        Assert.assertTrue("Try routing, only active trains deselected", router.setDestination(c3, null, null));

        // test yard and alternate track options
        bostonSpur1.setLength(c3.getTotalLength());
        // c4 is the same length as c3, so the track is now full
        Assert.assertEquals("Use up all of the space for bostonSpur1", Track.OKAY, c4.setLocation(boston, bostonSpur1));
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        c3.setFinalDestinationTrack(bostonSpur1);
        Assert.assertTrue("Test search for yard", router.setDestination(c3, null, null));
        Assert.assertEquals("Destination", "Boston", c3.getDestinationName());
        Assert.assertEquals("Destination track should be yard", "Boston Yard 1", c3.getDestinationTrackName());
        // the car was sent to a yard track because the spur was full
        Assert.assertTrue("Should be reporting length issue", router.getStatus().startsWith(Track.LENGTH));

        // remove yard type
        bostonYard1.setTrackType(Track.SPUR);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        c3.setFinalDestinationTrack(bostonSpur1);
        Assert.assertTrue("Test search for yard that doesn't exist", router.setDestination(c3, null, null));
        Assert.assertEquals("Destination", "", c3.getDestinationName());
        Assert.assertTrue("Should be reporting length issue", router.getStatus().startsWith(Track.LENGTH));

        // restore yard type
        bostonYard1.setTrackType(Track.YARD);

        // test alternate track option
        bostonSpur1.setAlternateTrack(bostonSpur2);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);
        c3.setFinalDestinationTrack(bostonSpur1);
        Assert.assertTrue("Test use alternate", router.setDestination(c3, null, null));
        Assert.assertEquals("Destination", "Boston", c3.getDestinationName());
        Assert.assertEquals("Destination track should be spur", "Boston Spur 2", c3.getDestinationTrackName());
        Assert.assertTrue("Should be reporting length issue", router.getStatus().startsWith(Track.LENGTH));
    }
    
    /**
     * Two trains and two locations testing. A local performs the first move.
     */
    @Test
    public void testCarRoutingTwoTrains() {
        // now load up the managers
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Router router = InstanceManager.getDefault(Router.class);

        // create 7 locations and tracks, use 2
        JUnitOperationsUtil.createSevenNormalLocations();
        
        Location acton = lmanager.getLocationByName("Acton");      
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);
        Track actonInterchange1 = acton.getTrackByName("Acton Interchange 1", Track.INTERCHANGE);
        Track actonInterchange2 = acton.getTrackByName("Acton Interchange 2", Track.INTERCHANGE);

        Location boston = lmanager.getLocationByName("Boston");
        Track bostonSpur1 = boston.getTrackByName("Boston Spur 1", Track.SPUR);

        // create 3 cars
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("BA", "3", "Boxcar", "40", actonSpur1, 0);
        JUnitOperationsUtil.createAndPlaceCar("BB", "4", "Flat", "40", actonSpur1, 0);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("BC", "5", "Boxcar", "40", null, 0);

        // create a local train servicing Acton
        Train actonTrain = tmanager.newTrain("Acton Local");
        Route routeA = rmanager.newRoute("A");
        RouteLocation rlActon1 = routeA.addLocation(acton);
        rlActon1.setTrainIconX(25); // set train icon coordinates
        rlActon1.setTrainIconY(250);
        actonTrain.setRoute(routeA);

        // add a second local train
        // create a local train servicing Acton
        Train tActon2 = tmanager.newTrain("Acton Local 2");
        Route routeA2 = rmanager.newRoute("A2");
        RouteLocation rlA2 = routeA2.addLocation(acton);
        rlA2.setTrainIconX(25); // set train icon coordinates
        rlA2.setTrainIconY(250);
        tActon2.setRoute(routeA2);

        // don't allow Acton Local 2 to service boxcars
        tActon2.deleteTypeName("Boxcar");

        // create a train with a route from Acton to Boston
        Train tActonToBoston = tmanager.newTrain("Acton to Boston");
        Route routeAB = rmanager.newRoute("AB");
        RouteLocation rlActon = routeAB.addLocation(acton);
        Assert.assertNotNull(rlActon);
        RouteLocation rlBoston = routeAB.addLocation(boston);
        rlBoston.setTrainIconX(100);   // set train icon coordinates
        rlBoston.setTrainIconY(250);
        tActonToBoston.setRoute(routeAB);

        // Force first move to be by local train
        actonSpur1.setTrainDirections(0);
        
        // Start two train testing.

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);    // the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);  // now specify the actual track

        Assert.assertTrue("Try routing two trains via interchange", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 1", c3.getDestinationTrackName());

        // don't allow use of interchange track 1, forces the use of interchange track 2
        actonInterchange1.setDropOption(Track.TRAINS);

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);    // the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);  // now specify the actual track

        Assert.assertTrue("Try routing two trains via interchange", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 2", c3.getDestinationTrackName());

        actonInterchange2.setDropOption(Track.TRAINS);

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);    // the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);  // now specify the actual track

        Assert.assertTrue("Try routing two trains via yard", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Yard 1", c3.getDestinationTrackName());

        // allow use of interchange track
        actonInterchange1.setDropOption(Track.ANY);
        actonInterchange2.setDropOption(Track.ANY);

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);    // the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);

        // allow Boxcars
        tActon2.addTypeName("Boxcar");

        Assert.assertTrue("Try routing two trains", router.setDestination(c3, tActon2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 1", c3.getDestinationTrackName());

        // test to see if second interchange track used if first is full
        actonInterchange1.setLength(c3.getTotalLength() - 1); // track capacity issue
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);    // the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);
        Assert.assertTrue("Try routing two trains using interchange track 2", router.setDestination(c3, tActon2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 2", c3.getDestinationTrackName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // use yard track if interchange tracks are full
        actonInterchange2.setLength(c3.getTotalLength() - 1); // track capacity issue
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);    // the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);
        Assert.assertTrue("Try routing two trains to yard track", router.setDestination(c3, tActon2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Yard 1", c3.getDestinationTrackName());

        // disable using yard tracks for routing
        Setup.setCarRoutingViaYardsEnabled(false);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);    // the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);
        
        Assert.assertFalse("Try routing two trains to yard track", router.setDestination(c3, tActon2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());
        
        // make interim track issue length
        actonInterchange1.setLength(c3.getTotalLength() + c5.getTotalLength() - 1); // too short for two cars
        Assert.assertEquals("place car at bostonInterchange 1", Track.OKAY, c5.setLocation(acton, actonInterchange1));
        Assert.assertTrue("Try routing two trains", router.setDestination(c3, tActon2, null));

        // restore track length
        actonInterchange1.setLength(500);
        actonInterchange2.setLength(500);
        Setup.setCarRoutingViaYardsEnabled(true);

        // don't allow train 2 to service boxcars with road name BA
        tActon2.addRoadName("BA");
        tActon2.setRoadOption(Train.EXCLUDE_ROADS);

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(boston);    // the final destination for the car
        c3.setFinalDestinationTrack(bostonSpur1);

        // routing should work using train 1, destination and track should not be set
        Assert.assertTrue("Try routing two trains via yard", router.setDestination(c3, tActon2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());

    }
    
    /**
     * Five train routing test
     */
    @Test
    public void testCarRoutingFiveTrainsA() {

        // only use interchange tracks for this test
        Setup.setCarRoutingViaYardsEnabled(false);
        
        // now load up the managers
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Router router = InstanceManager.getDefault(Router.class);
        
        // create 7 locations and tracks, use 6
        JUnitOperationsUtil.createSevenNormalLocations();
        
        Location acton = lmanager.getLocationByName("Acton");      
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);

        Location boston = lmanager.getLocationByName("Boston");
        Track bostonInterchange1 = boston.getTrackByName("Boston Interchange 1", null);
        Track bostonInterchange2 = boston.getTrackByName("Boston Interchange 2", null);
        
        Location chelmsford = lmanager.getLocationByName("Chelmsford");
        Location danvers = lmanager.newLocation("Danvers");
        Location essex = lmanager.getLocationByName("Essex");
        Location foxboro = lmanager.getLocationByName("Foxboro");

        // create 2 cars       
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("BA", "3", "Boxcar", "40", "DAB", "1984", actonSpur1, 0);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("BB", "4", "Flat", "40", "AT", "1-86", actonSpur1, 20);

        // create a train with a route from Acton to Boston
        Train actonToBostonTrain = tmanager.newTrain("Acton to Boston");
        Route routeAB = rmanager.newRoute("AB");
        RouteLocation rlActon = routeAB.addLocation(acton);
        routeAB.addLocation(boston);
        actonToBostonTrain.setRoute(routeAB);

        // create a train with a route from Boston to Chelmsford
        Train bostonToChelmsfordTrain = tmanager.newTrain("Boston to Chelmsford");
        Route routeBC = rmanager.newRoute("BC");
        routeBC.addLocation(boston);
        routeBC.addLocation(chelmsford);
        bostonToChelmsfordTrain.setRoute(routeBC);

        // create a train with a route from Chelmsford to Danvers
        Train chelmsfordToDanversTrain = tmanager.newTrain("Chelmsford to Danvers");
        Route routeCD = rmanager.newRoute("CD");
        routeCD.addLocation(chelmsford);
        routeCD.addLocation(danvers);
        chelmsfordToDanversTrain.setRoute(routeCD);

        // create a train with a route from Danvers to Essex
        Train danversToEssexTrain = tmanager.newTrain("Danvers to Essex");
        Route routeDE = rmanager.newRoute("DE");
        routeDE.addLocation(danvers);
        routeDE.addLocation(essex);
        danversToEssexTrain.setRoute(routeDE);

        // create a train with a route from Essex to Foxboro
        Train EssexToFoxboroTrain = tmanager.newTrain("Essex to Foxboro");
        Route routeEF = rmanager.newRoute("EF");
        routeEF.addLocation(essex);
        routeEF.addLocation(foxboro);
        EssexToFoxboroTrain.setRoute(routeEF);
        
        // send cars to Foxboro
        c3.setFinalDestination(foxboro);
        c4.setFinalDestination(foxboro);
        
        // should work
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Boston Interchange 1", c3.getDestinationTrackName());
        
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c4.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Boston Interchange 1", c4.getDestinationTrackName());
        
        // use a train to route car3
        c3.setDestination(null, null); 
        c4.setDestination(null, null); 
        Assert.assertTrue(actonToBostonTrain.build());
        
        // should work
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Boston Interchange 1", c3.getDestinationTrackName());
        Assert.assertEquals("Check car's destination", "Boston", c4.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Boston Interchange 1", c4.getDestinationTrackName());
        
        // now limit the train length out of Acton
        rlActon.setMaxTrainLength(50); // only one car can be part of train
        
        Assert.assertTrue(actonToBostonTrain.build());
        Assert.assertEquals("Check car's destination", boston, c3.getDestination());
        Assert.assertEquals("Check car's destination track", bostonInterchange1, c3.getDestinationTrack());
        Assert.assertEquals("Check car's destination", null, c4.getDestination());
        Assert.assertEquals("Check car's destination track", null, c4.getDestinationTrack());
        
        rlActon.setMaxTrainLength(200); // restore
        bostonInterchange1.setLength(50); // force 2nd car to Boston interchange track 2
        
        Assert.assertTrue(actonToBostonTrain.build());
        Assert.assertEquals("Check car's destination", boston, c3.getDestination());
        Assert.assertEquals("Check car's destination track", bostonInterchange1, c3.getDestinationTrack());
        Assert.assertEquals("Check car's destination", boston, c4.getDestination());
        Assert.assertEquals("Check car's destination track", bostonInterchange2, c4.getDestinationTrack());
        
        bostonInterchange2.setLength(40); // no room for c4!
        
        Assert.assertTrue(actonToBostonTrain.build());
        Assert.assertEquals("Check car's destination", boston, c3.getDestination());
        Assert.assertEquals("Check car's destination track", bostonInterchange1, c3.getDestinationTrack());
        Assert.assertEquals("Check car's destination", null, c4.getDestination());
        Assert.assertEquals("Check car's destination track", null, c4.getDestinationTrack());
        
        // send cars to Essex, only requires 4 trains
        c3.setFinalDestination(essex);
        c4.setFinalDestination(essex);
        
        // same issue, different code path
        Assert.assertTrue(actonToBostonTrain.build());
        Assert.assertEquals("Check car's destination", boston, c3.getDestination());
        Assert.assertEquals("Check car's destination track", bostonInterchange1, c3.getDestinationTrack());
        Assert.assertEquals("Check car's destination", null, c4.getDestination());
        Assert.assertEquals("Check car's destination track", null, c4.getDestinationTrack());
    }
    
    /**
     * Five train routing test
     */
    @Test
    public void testCarRoutingFiveTrainsB() {

        // now load up the managers
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Router router = InstanceManager.getDefault(Router.class);
        
        // create 7 locations and tracks, use 6
        JUnitOperationsUtil.createSevenNormalLocations();
        
        Location acton = lmanager.getLocationByName("Acton");      
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);
        Track actonInterchange1 = acton.getTrackByName("Acton Interchange 1", Track.INTERCHANGE);
        Track actonInterchange2 = acton.getTrackByName("Acton Interchange 2", Track.INTERCHANGE);

        Location boston = lmanager.getLocationByName("Boston");
        Location chelmsford = lmanager.getLocationByName("Chelmsford");
        Location danvers = lmanager.newLocation("Danvers");
        Location essex = lmanager.getLocationByName("Essex");
        Location foxboro = lmanager.getLocationByName("Foxboro");

        // create 2 cars       
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("BA", "3", "Boxcar", "40", "DAB", "1984", actonSpur1, 0);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("BB", "4", "Flat", "40", "AT", "1-86", actonSpur1, 0);

        // create a local train servicing Acton
        Train actonTrain1 = tmanager.newTrain("Acton Local 1");
        Route routeA = rmanager.newRoute("A");
        routeA.addLocation(acton);
        actonTrain1.setRoute(routeA);

        // add a second local train
        // create a local train servicing Acton
        Train actonTrain2 = tmanager.newTrain("Acton Local 2");
        Route routeA2 = rmanager.newRoute("A2");
        RouteLocation rlA2 = routeA2.addLocation(acton);
        actonTrain2.setRoute(routeA2);

        // create a train with a route from Acton to Boston
        Train ActonToBostonTrain = tmanager.newTrain("Acton to Boston");
        Route routeAB = rmanager.newRoute("AB");
        RouteLocation rlActon = routeAB.addLocation(acton);
        Assert.assertNotNull(rlActon);
        RouteLocation rlBoston = routeAB.addLocation(boston);
        rlBoston.setTrainIconX(100);   // set train icon coordinates
        rlBoston.setTrainIconY(250);
        ActonToBostonTrain.setRoute(routeAB);

        // Force first move to be by local train
        actonSpur1.setTrainDirections(0);
        
        // try up to 5 trains to route car
        // set final destination Chelmsford
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(chelmsford);
        c3.setFinalDestinationTrack(null);
        // should fail no train!
        Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));

        // create a train with a route from Boston to Chelmsford
        Train bostonToChelmsfordTrain = tmanager.newTrain("Boston to Chelmsford");
        Route routeBC = rmanager.newRoute("BC");
        routeBC.addLocation(boston);
        RouteLocation rlchelmsford = routeBC.addLocation(chelmsford);
        rlchelmsford.setTrainIconX(175);    // set train icon coordinates
        rlchelmsford.setTrainIconY(250);
        bostonToChelmsfordTrain.setRoute(routeBC);

        // should work
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 1", c3.getDestinationTrackName());

        // allow train 2 to service boxcars with road name BA
        actonTrain2.setRoadOption(Train.ALL_ROADS);

        // try with train 2
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(chelmsford);

        // routing should work using train 2
        Assert.assertTrue("Try routing three trains", router.setDestination(c3, actonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 1", c3.getDestinationTrackName());

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(chelmsford);

        // test to see if second interchange track used if first is full
        actonInterchange1.setLength(c3.getTotalLength() - 1);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(chelmsford);    // the final destination for the car
        Assert.assertTrue("Try routing three trains to interchange track 2", router.setDestination(c3, actonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 2", c3.getDestinationTrackName());

        // use yard track if interchange tracks are full
        actonInterchange2.setLength(c3.getTotalLength() - 1);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(chelmsford);    // the final destination for the car
        Assert.assertTrue("Try routing three trains to yard track", router.setDestination(c3, actonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Yard 1", c3.getDestinationTrackName());

        // disable the use of yard tracks for routing
        Setup.setCarRoutingViaYardsEnabled(false);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(chelmsford);    // the final destination for the car
        // both interchange tracks are too short, so there isn't a route
        Assert.assertFalse("Try routing three trains to yard track, option disabled", router.setDestination(c3, actonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());

        // Try again with an interchange track that is long enough for car c3.
        actonInterchange1.setLength(c3.getTotalLength());
        Assert.assertEquals("c4 consumes all of the interchange track", Track.OKAY, c4.setLocation(actonInterchange1.getLocation(), actonInterchange1));
        // Track AI is long enough, but c4 is consuming all of the track, but there is a route!
        Assert.assertTrue("Try routing three trains to yard track, option disabled", router.setDestination(c3, actonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());

        // restore track length
        actonInterchange1.setLength(500);
        actonInterchange2.setLength(500);
        Setup.setCarRoutingViaYardsEnabled(true);

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(chelmsford);    // the final destination for the car
        // don't allow train 2 to service cars built before 1985
        actonTrain2.setBuiltStartYear("1985");
        actonTrain2.setBuiltEndYear("2010");
        // routing should work using train 1, but destinations and track should not be set
        Assert.assertTrue("Try routing three trains", router.setDestination(c3, actonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());
        // allow car to be serviced
        // car was built in 1984 should work
        actonTrain2.setBuiltStartYear("1983");

        // set final destination Danvers
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(danvers);
        // should fail no train!
        Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));

        // create a train with a route from Chelmsford to Danvers
        Train chelmsfordToDanversTrain = tmanager.newTrain("Chelmsford to Danvers");
        Route routeCD = rmanager.newRoute("CD");
        routeCD.addLocation(chelmsford);
        RouteLocation rlDanvers = routeCD.addLocation(danvers);
        rlDanvers.setTrainIconX(250);   // set train icon coordinates
        rlDanvers.setTrainIconY(250);
        chelmsfordToDanversTrain.setRoute(routeCD);

        // should work
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 1", c3.getDestinationTrackName());

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(danvers);    // the final destination for the car

        // routing should work using train 2
        Assert.assertTrue("Try routing four trains", router.setDestination(c3, actonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 1", c3.getDestinationTrackName());

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(danvers);    // the final destination for the car

        // don't allow train 2 to service cars with load Tools
        actonTrain2.addLoadName("Tools");
        actonTrain2.setLoadOption(Train.EXCLUDE_LOADS);
        c3.setLoadName("Tools");
        // routing should work using train 1, but destinations and track should not be set
        Assert.assertTrue("Try routing four trains", router.setDestination(c3, actonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());
        // restore train 2
        actonTrain2.setLoadOption(Train.ALL_LOADS);

        // set final destination Essex
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(essex);
        // should fail no train!
        Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));

        // create a train with a route from Danvers to Essex
        Train danversToEssexTrain = tmanager.newTrain("Danvers to Essex");
        Route routeDE = rmanager.newRoute("DE");
        RouteLocation rlDanvers2 = routeDE.addLocation(danvers);
        RouteLocation rlEssex = routeDE.addLocation(essex);
        // set the number of car moves to 8 for a later test
        rlDanvers2.setMaxCarMoves(8);
        rlEssex.setMaxCarMoves(8);
        rlEssex.setTrainIconX(25);  // set train icon coordinates
        rlEssex.setTrainIconY(275);
        danversToEssexTrain.setRoute(routeDE);

        // should work
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 1", c3.getDestinationTrackName());

        // routing should work using train 2
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(essex);
        Assert.assertTrue("Try routing five trains", router.setDestination(c3, actonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 1", c3.getDestinationTrackName());

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(essex);
        // don't allow train 2 to pickup
        rlA2.setPickUpAllowed(false);
        // routing should work using train 1, but destinations and track should not be set
        Assert.assertTrue("Try routing five trains", router.setDestination(c3, actonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("Check status", Router.STATUS_NOT_THIS_TRAIN, router.getStatus());

        // set final destination Foxboro
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(foxboro);
        // should fail no train!
        Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));

        // create a train with a route from Essex to Foxboro
        Train EssexToFoxboroTrain = tmanager.newTrain("Essex to Foxboro");
        Route routeEF = rmanager.newRoute("EF");
        routeEF.addLocation(essex);
        RouteLocation rlFoxboro = routeEF.addLocation(foxboro);
        rlFoxboro.setTrainIconX(100);   // set train icon coordinates
        rlFoxboro.setTrainIconY(275);
        EssexToFoxboroTrain.setRoute(routeEF);

        // 6th train should fail!  Only 5 trains supported
        Assert.assertFalse("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertFalse("Try routing with final destination and train", router.setDestination(c3, actonTrain1, null));
        Assert.assertFalse("Try routing with final destination and train", router.setDestination(c3, actonTrain2, null));

        // get rid of the local train
        actonSpur1.setTrainDirections(Track.NORTH);

        // now should work!
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Boston", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Boston Interchange 1", c3.getDestinationTrackName());

        //TODO test restrict location by car type
        //TODO test restrict tracks by type road, load
    }
    
    /**
     * Test routing through staging
     */
    @Test
    public void testCarRoutingThroughStaging() {
        // now load up the managers
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Router router = InstanceManager.getDefault(Router.class);

        // create 7 locations and tracks, use 5
        JUnitOperationsUtil.createSevenNormalLocations();
        
        Location acton = lmanager.getLocationByName("Acton");      
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);

        Location boston = lmanager.getLocationByName("Boston");
        Location chelmsford = lmanager.getLocationByName("Chelmsford");
        Location essex = lmanager.getLocationByName("Essex");
        Track essexInterchange1 = essex.getTrackByName("Essex Interchange 1", Track.INTERCHANGE);

        Location foxboro = lmanager.getLocationByName("Foxboro");
        Location gulf = lmanager.getLocationByName("Gulf");
        Track gulfInterchange1 = gulf.getTrackByName("Gulf Interchange 1", Track.INTERCHANGE);
        
        // create four staging tracks       
        Location staging = lmanager.newLocation("Staging MA");
        Track stagingTrack1 = staging.addTrack("Staging 1", Track.STAGING);
        stagingTrack1.setLength(500);
        Track stagingTrack2 = staging.addTrack("Staging 2", Track.STAGING);
        stagingTrack2.setLength(500);
        Track stagingTrack3 = staging.addTrack("Staging 3", Track.STAGING);
        stagingTrack3.setLength(500);      
        Track stagingTrack4 = staging.addTrack("Staging 4", Track.STAGING);
        stagingTrack4.setLength(500);
        
        stagingTrack2.setMoves(60);
        stagingTrack3.setMoves(80);
        stagingTrack4.setMoves(100); // the last track to try in staging
        
        // don't allow Boxcars staging on two tracks
        stagingTrack1.deleteTypeName("Boxcar");
        stagingTrack3.deleteTypeName("Boxcar");
        
        // create a 2nd staging location
        Location staging2 = lmanager.newLocation("Staging 2");
        Track stagingT2 = staging2.addTrack("Staging 2", Track.STAGING);
        stagingT2.setLength(500);

        // create 2 cars
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("BA", "3", "Boxcar", "40", actonSpur1, 0);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("BB", "4", "Flat", "40", actonSpur1, 0);
        
        // make staging track 2 unavailable by placing a car there
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("BC", "B", "Boxcar", "40", stagingTrack2, 0);

        // create two trains, one terminates into staging, the other departs
        Train trainActonToStagingTrain = tmanager.newTrain("Train Acton-Boston-Staging");
        Route routeA = rmanager.newRoute("Route Acton-Boston-Staging");
        routeA.addLocation(acton);
        routeA.addLocation(boston);
        routeA.addLocation(staging); // terminated into staging
        trainActonToStagingTrain.setRoute(routeA);
        
        Train trainStagingToFoxboro1 = tmanager.newTrain("Train Staging-Gulf-Essex-Foxboro 1");
        Route routeB = rmanager.newRoute("Route Staging-Gulf-Essex-Foxboro");
        routeB.addLocation(staging);
        RouteLocation rlgulf = routeB.addLocation(gulf);
        RouteLocation rlessex = routeB.addLocation(essex);
        routeB.addLocation(foxboro);
        trainStagingToFoxboro1.setRoute(routeB);
        
        // default is routing through staging is disabled
        Assert.assertFalse("Default routing through staging", Setup.isCarRoutingViaStagingEnabled());
        // try and route the car through staging to Essex
        c3.setFinalDestination(essex);
        Assert.assertFalse("Try routing two trains through staging", router.setDestination(c3, null, null));
        Assert.assertEquals("Check status", Router.STATUS_NOT_ABLE, router.getStatus());
        
        // enable routing through staging
        Setup.setCarRoutingViaStagingEnabled(true);
        Assert.assertTrue("Try routing two trains through staging", router.setDestination(c3, null, null));
        
        // confirm car's destination
        Assert.assertEquals("car's destination is staging", staging, c3.getDestination());
        // note that the router sets a car's track into staging when a "specific" train isn't provided
        Assert.assertEquals("car's destination track is staging", stagingTrack2, c3.getDestinationTrack());
        Assert.assertEquals("car's final destination", essex, c3.getFinalDestination());
        
        c3.setDestination(null, null);  // clear previous destination
        
        // try again, but specify a train
        trainActonToStagingTrain.build();
        Assert.assertEquals("car's destination track is staging", stagingTrack4, c3.getDestinationTrack());
        Assert.assertEquals("car's final destination", essex, c3.getFinalDestination());
        trainActonToStagingTrain.reset(); // release car c3
        
        // now test 3 trains
        // third train doesn't exist, should fail
        c3.setFinalDestination(chelmsford);
        c3.setDestination(null, null);  // clear previous destination
        Assert.assertFalse("Try routing three trains through staging", router.setDestination(c3, null, null));
        Assert.assertEquals("Check status", Router.STATUS_NOT_ABLE, router.getStatus());
        
        // new train departs Essex terminates Chelmsford        
        Train trainEssexToChelmsford = tmanager.newTrain("Train Essex-Chelmsford");
        Route routeC = rmanager.newRoute("Route Essex-Chelmsford");
        routeC.addLocation(essex);
        routeC.addLocation(chelmsford);
        trainEssexToChelmsford.setRoute(routeC);
        
        Assert.assertTrue("Try routing three trains through staging", router.setDestination(c3, null, null));
        
        // confirm car's destination
        Assert.assertEquals("car's destination is staging", staging, c3.getDestination());
        // note that the router sets a car's track into staging when a "specific" train isn't provided
        Assert.assertEquals("car's destination track is staging", stagingTrack2, c3.getDestinationTrack());
        Assert.assertEquals("car's final destination", chelmsford, c3.getFinalDestination());
        
        // try to route car through staging using two trains after staging
        c3.setDestination(null, null);  // clear previous destination
        trainActonToStagingTrain.build();
        Assert.assertEquals("car's destination track is staging", stagingTrack4, c3.getDestinationTrack());
        Assert.assertEquals("car's final destination", chelmsford, c3.getFinalDestination());
        trainActonToStagingTrain.reset(); // release car c3
        
        // place car in staging and try to route
        c5.setLocation(null, null); // remove c5 from staging, now only one track has cars
        c3.setDestination(null, null);  // clear previous destination
        Assert.assertEquals("place car in staging", Track.OKAY, c3.setLocation(staging, stagingTrack4));
        
        Assert.assertTrue("Try routing two trains from staging", router.setDestination(c3, null, null));
        Assert.assertEquals("car's destination is essex ", essexInterchange1, c3.getDestinationTrack());
        Assert.assertEquals("car's final destination", chelmsford, c3.getFinalDestination());
        
        // don't allow train out of staging to carry car type Boxcar
        trainStagingToFoxboro1.deleteTypeName("Boxcar");
        c3.setDestination(null, null);  // clear previous destination
        
        Assert.assertFalse("Try routing two trains from staging", router.setDestination(c3, trainStagingToFoxboro1, null));
        Assert.assertEquals("car's destination", null, c3.getDestinationTrack());
        Assert.assertEquals("car's final destination", chelmsford, c3.getFinalDestination());
        
        // create a second train out of staging
        Train trainStagingToFoxboro2 = tmanager.newTrain("Train Staging-Gulf-Essex-Foxboro 2");
        trainStagingToFoxboro2.setRoute(routeB); // use the same route
        
        Assert.assertTrue("Try routing two trains from staging", router.setDestination(c3, null, null));
        Assert.assertEquals("car's destination is essex ", essexInterchange1, c3.getDestinationTrack());
        Assert.assertEquals("car's final destination", chelmsford, c3.getFinalDestination());
        
        // now specify the first train that can't carry car type Boxcar
        c3.setDestination(null, null);  // clear previous destination
        Assert.assertFalse("Try routing two trains from staging", router.setDestination(c3, trainStagingToFoxboro1, null));
        Assert.assertEquals("car's destination", null, c3.getDestinationTrack());
        Assert.assertEquals("car's final destination", chelmsford, c3.getFinalDestination()); 
        
        // test train out staging length issues, use two cars
        Assert.assertEquals("place car in staging", Track.OKAY, c4.setLocation(staging, stagingTrack4));
        c4.setFinalDestination(chelmsford); // also send this car to Chelmsford
        c4.setMoves(100); // always process this car second
        trainStagingToFoxboro2.build();
        
        // confirm cars added to train and destination
        Assert.assertEquals("car added to train", trainStagingToFoxboro2, c3.getTrain());
        Assert.assertEquals("car added to train", trainStagingToFoxboro2, c4.getTrain());
        Assert.assertEquals("car's destination is essex ", essexInterchange1, c3.getDestinationTrack());
        Assert.assertEquals("car's final destination", chelmsford, c3.getFinalDestination());
        Assert.assertEquals("car's destination is essex ", essexInterchange1, c4.getDestinationTrack());
        Assert.assertEquals("car's final destination", chelmsford, c4.getFinalDestination());

        // now limit train length 
        rlgulf.setMaxTrainLength(80); // only one car can be carried
        
        trainStagingToFoxboro2.build();
        Assert.assertTrue(trainStagingToFoxboro2.isBuilt());
        
        // the Router found a two train route, but gave up using a three train route out of staging
        // TODO should the router use multiple trains just to get the car out of staging?
        // would this screw up the generation of custom loads? As an inefficient route could be selected.
       
        // change c4 final destination
        trainStagingToFoxboro2.reset();
        c4.setFinalDestination(foxboro); // one hop out staging provides report messages
        
        // TODO for the next build to succeed, the program needed trainStagingToFoxboro1 to pull car c4 from Gulf
        // the router should have ignored the trainStagingToFoxboro2 length restriction when pulling c4 from Gulf
        // Note the car c4 is a "Flat" car, if it was a "Boxcar" the build would have failed
        
        // interchange track at Gulf is available
        trainStagingToFoxboro2.build();
        Assert.assertTrue(trainStagingToFoxboro2.isBuilt());
        
        Assert.assertEquals("car's destination", gulfInterchange1, c4.getDestinationTrack());
        Assert.assertEquals("car's final destination", foxboro, c4.getFinalDestination());
        
        // test having train terminate into staging 2, build will try and send car there
        trainStagingToFoxboro2.reset();
        c4.setFinalDestination(chelmsford); // restore 
        rlgulf.setMaxTrainLength(1000); // restore
        routeB.addLocation(staging2); // terminate into staging 2
        rlessex.setMaxCarMoves(1); // only allow one drop, c4 has to go to staging
        
        trainStagingToFoxboro2.build();
        Assert.assertTrue(trainStagingToFoxboro2.isBuilt());
        
        Assert.assertEquals("car's destination", essexInterchange1, c3.getDestinationTrack());
        Assert.assertEquals("car's final destination", chelmsford, c3.getFinalDestination());
        Assert.assertEquals("car's destination", stagingT2, c4.getDestinationTrack()); // sent to staging rather than build failure
        Assert.assertEquals("car's final destination", chelmsford, c4.getFinalDestination());
    }

    // Use trains to move cars
    @Test
    public void testRoutingWithTrains() {
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);

        loadLocationsTrainsAndCars();

        List<Train> trains = tmanager.getTrainsByNameList();
        Assert.assertEquals("confirm number of trains", 7, trains.size());

        Train actonTrain = tmanager.getTrainByName("Acton Local");
        Train actonToBostonTrain = tmanager.getTrainByName("Acton to Boston");
        Train bostonToChelmsfordTrain = tmanager.getTrainByName("Boston to Chelmsford");
        Train chelmsfordToDanversTrain = tmanager.getTrainByName("Chelmsford to Danvers");
        Train danversToEssexTrain = tmanager.getTrainByName("Danvers to Essex");
        Train essexToFoxboroTrain = tmanager.getTrainByName("Essex to Foxboro");

        Car c3 = cmanager.getByRoadAndNumber("BA", "3");
        Car c4 = cmanager.getByRoadAndNumber("BB", "4");

        Location acton = lmanager.getLocationByName("Acton");
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", Track.SPUR);
        // set the depart track Acton to service by local train only
        actonSpur1.setTrainDirections(0);

        Location essex = lmanager.getLocationByName("Essex");
        Track essexSpur2 = essex.getTrackByName("Essex Spur 2", Track.SPUR);
        Location Foxboro = lmanager.getLocationByName("Foxboro");

        Location holden = lmanager.newLocation("Holden");

        // place cars
        Assert.assertEquals("Place car", Track.OKAY, c3.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c4.setLocation(acton, actonSpur1));

        // confirm cars are in Acton
        Assert.assertEquals("car's location Acton", "Acton", c3.getLocationName());
        Assert.assertEquals("car's location Acton", "Acton Spur 1", c3.getTrackName());

        Assert.assertEquals("car's location Acton", "Acton", c4.getLocationName());
        Assert.assertEquals("car's location Acton", "Acton Spur 1", c4.getTrackName());

        // set final destination Essex
        c3.setFinalDestination(essex);
        c3.setFinalDestinationTrack(essexSpur2);
        c3.setLoadName("L");
        c3.setReturnWhenEmptyDestination(Foxboro);

        // final destination Gulf is not reachable, so car must move
        c4.setFinalDestination(holden);

        actonTrain.build();
        Assert.assertTrue(actonTrain.isBuilt());

        Assert.assertEquals("car's destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("car's destinaton track", "Acton Interchange 1", c3.getDestinationTrackName());
        Assert.assertEquals("car's final destinaton", essex, c3.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", essexSpur2, c3.getFinalDestinationTrack());

        Assert.assertEquals("car's destination", "Acton", c4.getDestinationName());
        Assert.assertEquals("car's destinaton track", "Acton Yard 1", c4.getDestinationTrackName());
        Assert.assertEquals("car's final destinaton", holden, c4.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", null, c4.getFinalDestinationTrack());

        actonTrain.reset();

        // check car's destinations after reset
        Assert.assertEquals("car's destination", "", c3.getDestinationName());
        Assert.assertEquals("car's destinaton track", "", c3.getDestinationTrackName());
        Assert.assertEquals("car's final destinaton", essex, c3.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", essexSpur2, c3.getFinalDestinationTrack());
        Assert.assertEquals("car's load", "L", c3.getLoadName());

        Assert.assertEquals("car's final destinaton", holden, c4.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", null, c4.getFinalDestinationTrack());

        actonTrain.build();
        Assert.assertTrue(actonTrain.isBuilt());
        actonTrain.terminate();

        // confirm cars have moved
        Assert.assertEquals("car's location Acton", "Acton", c3.getLocationName());
        Assert.assertEquals("car's location Acton", "Acton Interchange 1", c3.getTrackName());
        // as of 5/4/2011 the car's destination is set to null, but the final destination continues to exist
        Assert.assertEquals("car's destination", "", c3.getDestinationName());
        Assert.assertEquals("car's destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("car's final destinaton", essex, c3.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", essexSpur2, c3.getFinalDestinationTrack());
        Assert.assertEquals("car's load", "L", c3.getLoadName());

        Assert.assertEquals("car's location Acton", "Acton", c4.getLocationName());
        Assert.assertEquals("car's location Acton", "Acton Yard 1", c4.getTrackName());
        Assert.assertEquals("car's destination", "", c4.getDestinationName());
        Assert.assertEquals("car's destination track", "", c4.getDestinationTrackName());

        // Place a maximum length restriction on the train
        Route route = actonToBostonTrain.getRoute();
        RouteLocation rlActon = route.getDepartsRouteLocation();
        rlActon.setMaxTrainLength(c3.getTotalLength());
        actonToBostonTrain.build();

        Assert.assertEquals("car's destination", "Boston", c3.getDestinationName());
        Assert.assertEquals("car's destinaton track", "Boston Interchange 1", c3.getDestinationTrackName());
        Assert.assertEquals("car's final destinaton", essex, c3.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", essexSpur2, c3.getFinalDestinationTrack());

        Assert.assertEquals("car's destination", "", c4.getDestinationName());
        Assert.assertEquals("car's destinaton track", "", c4.getDestinationTrackName());
        Assert.assertEquals("car's final destinaton", holden, c4.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", null, c4.getFinalDestinationTrack());

        actonToBostonTrain.reset();

        // restore
        rlActon.setMaxTrainLength(Setup.getMaxTrainLength());

        actonToBostonTrain.build();
        Assert.assertTrue(actonToBostonTrain.isBuilt());
        actonToBostonTrain.terminate();

        // confirm cars have moved
        Assert.assertEquals("car's location Boston", "Boston", c3.getLocationName());
        Assert.assertEquals("car's location Boston", "Boston Interchange 1", c3.getTrackName());
        Assert.assertEquals("car's destination", "", c3.getDestinationName());
        Assert.assertEquals("car's destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("car's load", "L", c3.getLoadName());
        Assert.assertEquals("car's final destinaton", essex, c3.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", essexSpur2, c3.getFinalDestinationTrack());

        Assert.assertEquals("car's location Boston", "Boston", c4.getLocationName());
        Assert.assertEquals("car's location Boston", "Boston Spur 1", c4.getTrackName());
        Assert.assertEquals("car's destination", "", c4.getDestinationName());
        Assert.assertEquals("car's destination track", "", c4.getDestinationTrackName());

        bostonToChelmsfordTrain.build();
        Assert.assertTrue(bostonToChelmsfordTrain.isBuilt());
        bostonToChelmsfordTrain.terminate();

        // confirm cars have moved
        Assert.assertEquals("car's location Chelmsford", "Chelmsford", c3.getLocationName());
        Assert.assertEquals("car's location Chelmsford", "Chelmsford Interchange 1", c3.getTrackName());
        Assert.assertEquals("car's destination", "", c3.getDestinationName());
        Assert.assertEquals("car's destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("car's load", "L", c3.getLoadName());
        Assert.assertEquals("car's final destinaton", essex, c3.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", essexSpur2, c3.getFinalDestinationTrack());

        Assert.assertEquals("car's location Chelmsford", "Chelmsford", c4.getLocationName());
        Assert.assertEquals("car's location Chelmsford", "Chelmsford Spur 1", c4.getTrackName());
        Assert.assertEquals("car's destination", "", c4.getDestinationName());
        Assert.assertEquals("car's destination track", "", c4.getDestinationTrackName());

        chelmsfordToDanversTrain.build();
        Assert.assertTrue(chelmsfordToDanversTrain.isBuilt());
        chelmsfordToDanversTrain.terminate();

        // confirm cars have moved
        Assert.assertEquals("car's location Danvers", "Danvers", c3.getLocationName());
        Assert.assertEquals("car's location Danvers", "Danvers Interchange 1", c3.getTrackName());
        Assert.assertEquals("car's destination", "", c3.getDestinationName());
        Assert.assertEquals("car's destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("car's load", "L", c3.getLoadName());
        Assert.assertEquals("car's final destinaton", essex, c3.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", essexSpur2, c3.getFinalDestinationTrack());

        Assert.assertEquals("car's location Danvers", "Danvers", c4.getLocationName());
        Assert.assertEquals("car's location Danvers", "Danvers Spur 1", c4.getTrackName());
        Assert.assertEquals("car's destination", "", c4.getDestinationName());
        Assert.assertEquals("car's destination track", "", c4.getDestinationTrackName());

        danversToEssexTrain.build();
        Assert.assertTrue(danversToEssexTrain.isBuilt());
        danversToEssexTrain.terminate();

        // confirm cars have moved car has arrived at final destination Essex
        Assert.assertEquals("car's location Essex", "Essex", c3.getLocationName());
        Assert.assertEquals("car's location Essex", "Essex Spur 2", c3.getTrackName());
        Assert.assertEquals("car's destination", "", c3.getDestinationName());
        Assert.assertEquals("car's destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("car's load", "E", c3.getLoadName());
        // car when empty must return to Foxboro
        Assert.assertEquals("car's final destinaton", Foxboro, c3.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", null, c3.getFinalDestinationTrack());

        Assert.assertEquals("car's location Essex", "Essex", c4.getLocationName());
        Assert.assertEquals("car's location Essex", "Essex Spur 1", c4.getTrackName());
        Assert.assertEquals("car's destination", "", c4.getDestinationName());
        Assert.assertEquals("car's destination track", "", c4.getDestinationTrackName());

        essexToFoxboroTrain.build();
        Assert.assertTrue(essexToFoxboroTrain.isBuilt());
        essexToFoxboroTrain.terminate();

        // confirm cars have moved
        Assert.assertEquals("car's location Foxboro", "Foxboro", c3.getLocationName());
        Assert.assertEquals("car's location Foxboro", "Foxboro Spur 1", c3.getTrackName());
        Assert.assertEquals("car's destination", "", c3.getDestinationName());
        Assert.assertEquals("car's destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("car's load", "L", c3.getLoadName());

        Assert.assertEquals("car's location Foxboro", "Foxboro", c4.getLocationName());
        Assert.assertEquals("car's location Foxboro", "Foxboro Spur 1", c4.getTrackName());
        Assert.assertEquals("car's destination", "", c4.getDestinationName());
        Assert.assertEquals("car's destination track", "", c4.getDestinationTrackName());
    }
    
    /**
     * Test alternate track when routing with trains
     */
    @Test
    public void testRoutingWithTrainsAlternateTrack() {
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);

        loadLocationsTrainsAndCars();

        List<Train> trains = tmanager.getTrainsByNameList();
        Assert.assertEquals("confirm number of trains", 7, trains.size());

        Train actonTrain = tmanager.getTrainByName("Acton Local");
        Train actonToBostonTrain = tmanager.getTrainByName("Acton to Boston");

        // cars are 40' long
        Car c3 = cmanager.getByRoadAndNumber("BA", "3");
        Car c4 = cmanager.getByRoadAndNumber("BB", "4");
        Car c5 = cmanager.getByRoadAndNumber("BC", "5");
        Car c6 = cmanager.getByRoadAndNumber("BD", "6");
        
        // adjust move counts so the car placement order is always the same
        c4.setMoves(100);
        c5.setMoves(200);
        c6.setMoves(300);

        Location acton = lmanager.getLocationByName("Acton");
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);
        Track actonSpur2 = acton.getTrackByName("Acton Spur 2", null);
        Track actonYard = acton.getTrackByName("Acton Yard 1", null);
        Track actonInterchange = acton.getTrackByName("Acton Interchange 1", null);
        
        Location boston = lmanager.getLocationByName("Boston");
        Track bostonSpur2 = boston.getTrackByName("Boston Spur 2", null);
        Track bostonYard1 = boston.getTrackByName("Boston Yard 1", null);
        Track bostonYard2 = boston.getTrackByName("Boston Yard 2", null);
        boston.deleteTrack(bostonYard2); // need to get rid of this one
        Track bostonInterchange1 = boston.getTrackByName("Boston Interchange 1", null);

        // place cars
        Assert.assertEquals("Place car", Track.OKAY, c3.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c4.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c5.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c6.setLocation(acton, actonSpur1));

        // confirm cars are in Acton
        Assert.assertEquals("car's location Acton spur 1", actonSpur1, c3.getTrack());
        Assert.assertEquals("car's location Acton spur 1", actonSpur1, c4.getTrack());
        Assert.assertEquals("car's location Acton spur 1", actonSpur1, c5.getTrack());
        Assert.assertEquals("car's location Acton spur 1", actonSpur1, c6.getTrack());

        // set final destination Acton spur 2
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        c4.setFinalDestination(acton);
        c4.setFinalDestinationTrack(actonSpur2);
        c5.setFinalDestination(acton);
        c5.setFinalDestinationTrack(actonSpur2);
        c6.setFinalDestination(acton);
        c6.setFinalDestinationTrack(actonSpur2);
        
        actonTrain.build();
        // confirm that car destination is Acton spur 2     
        Assert.assertEquals("car's destination track", actonSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonSpur2, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonSpur2, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonSpur2, c6.getDestinationTrack());

        actonTrain.reset();
        // confirm that car destination has been removed     
        Assert.assertEquals("car's destination track", null, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c6.getDestinationTrack());
        
        // now reduce spur 2 length
        actonSpur2.setLength(80); // only one 40' can fit
        
        actonTrain.build();
        // confirm that car destinations    
        Assert.assertEquals("car's destination track", actonSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c6.getDestinationTrack());
        
        // now provide an alternate
        actonSpur2.setAlternateTrack(actonYard);
        actonYard.setTrainDirections(0); // only a local can use this alternate yard        
        
        actonTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", actonSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonYard, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonYard, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonYard, c6.getDestinationTrack());
        
        // now disallow car type Boxcar at actonYard
        actonYard.deleteTypeName("Boxcar");
        
        actonTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", actonSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonYard, c4.getDestinationTrack()); // c4 is a Flatcar
        Assert.assertEquals("car's destination track", null, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c6.getDestinationTrack());
        
        actonYard.addTypeName("Boxcar"); // restore
        
        // now try to limit length of the train, even though the train is a local
        Route routeA = actonTrain.getRoute();
        RouteLocation rlA = routeA.getDepartsRouteLocation();
        rlA.setMaxTrainLength(100); // train length is ignored for a local 
        
        actonTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", actonSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonYard, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonYard, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonYard, c6.getDestinationTrack());
        
        actonTrain.reset(); // release cars
        
        // now try moving cars using the Acton to Boston train
        actonToBostonTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", actonSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c6.getDestinationTrack());
        
        // now allow regular trains to use the alternate yard track
        actonYard.setTrainDirections(Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH);
        
        actonToBostonTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", actonSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonYard, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonYard, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonYard, c6.getDestinationTrack());        
        
        actonToBostonTrain.reset();
        // send the cars to Boston
        c3.setFinalDestination(boston);
        c3.setFinalDestinationTrack(bostonSpur2);
        c4.setFinalDestination(boston);
        c4.setFinalDestinationTrack(bostonSpur2);
        c5.setFinalDestination(boston);
        c5.setFinalDestinationTrack(bostonSpur2);
        c6.setFinalDestination(boston);
        c6.setFinalDestinationTrack(bostonSpur2);
        
        actonToBostonTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", bostonSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", bostonSpur2, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", bostonSpur2, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", bostonSpur2, c6.getDestinationTrack());
        
        // now reduce spur 2 length
        bostonSpur2.setLength(80); // only one 40' can fit
        
        actonToBostonTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", bostonSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", bostonYard1, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", bostonYard1, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", bostonYard1, c6.getDestinationTrack());
        
        // now try to limit length of the train
        Route routeB = actonToBostonTrain.getRoute();
        RouteLocation rlB = routeB.getDepartsRouteLocation();
        rlB.setMaxTrainLength(100); // train length is ignored for a local
        
        actonToBostonTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", bostonSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", bostonYard1, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c6.getDestinationTrack());
        
        rlB.setMaxTrainLength(400); // restore
        actonToBostonTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", bostonSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", bostonYard1, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", bostonYard1, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", bostonYard1, c6.getDestinationTrack());
        
        // now disallow the use of the alternate yard track
        bostonYard1.setTrainDirections(0);
        
        actonToBostonTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", bostonSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c6.getDestinationTrack());
        
        // change departure track to staging, router will attempt to find a way to get car out of staging
        bostonYard1.setTrainDirections(Track.NORTH); 
        actonSpur1.setTrackType(Track.STAGING);
        
        actonToBostonTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", bostonSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", bostonYard1, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", bostonYard1, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", bostonYard1, c6.getDestinationTrack());
        
        // now disallow the use of the alternate yard track
        bostonYard1.setTrainDirections(0);
        
        actonToBostonTrain.build();
        // confirm that car destinations
        // note that the Router has two interchange tracks to choose from boston and acton, so results could change based on usage above
        Assert.assertEquals("car's destination track", bostonSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", bostonInterchange1, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonInterchange, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", bostonInterchange1, c6.getDestinationTrack());
             
    }

    /*
     * This test creates 4 schedules, and each schedule only has one item.
     * Two cars are used a boxcar and a flat. They both start with a load of
     * "Food". They should be routed to the correct schedule that is demanding
     * the car type and load.
     */
    @Test
    public void testRoutingWithSimpleSchedules() {
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);

        loadLocationsTrainsAndCars();

        List<Train> trains = tmanager.getTrainsByNameList();
        Assert.assertEquals("confirm number of trains", 7, trains.size());

        Train actonTrain = tmanager.getTrainByName("Acton Local");
        Train actonToBostonTrain = tmanager.getTrainByName("Acton to Boston");
        Train bostonToChelmsfordTrain = tmanager.getTrainByName("Boston to Chelmsford");
        Train chelmsfordToDanversTrain = tmanager.getTrainByName("Chelmsford to Danvers");
        Train danversToEssexTrain = tmanager.getTrainByName("Danvers to Essex");
        Train essexToFoxboroTrain = tmanager.getTrainByName("Essex to Foxboro");

        Car c3 = cmanager.getByRoadAndNumber("BA", "3");
        Car c4 = cmanager.getByRoadAndNumber("BB", "4");

        Location acton = lmanager.getLocationByName("Acton");
        Location Chelmsford = lmanager.getLocationByName("Chelmsford");
        Location danvers = lmanager.getLocationByName("Danvers");
        Location essex = lmanager.getLocationByName("Essex");
        Location Foxboro = lmanager.getLocationByName("Foxboro");

        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", Track.SPUR);
        Track chelmsfordSpur1 = Chelmsford.getTrackByName("Chelmsford Spur 1", Track.SPUR);
        Track danversSpur1 = danvers.getTrackByName("Danvers Spur 1", Track.SPUR);
        Track danversSpur2 = danvers.getTrackByName("Danvers Spur 2", Track.SPUR);
        Track essexSpur1 = essex.getTrackByName("Essex Spur 1", Track.SPUR);
        Track essexSpur2 = essex.getTrackByName("Essex Spur 2", Track.SPUR);
        Track foxboroSpur1 = Foxboro.getTrackByName("Foxboro Spur 1", Track.SPUR);

        // set the depart track Acton to service by local train only
        actonSpur1.setTrainDirections(0);

        // create schedules
        ScheduleManager scheduleManager = InstanceManager.getDefault(ScheduleManager.class);
        Schedule schA = scheduleManager.newSchedule("Schedule A");
        ScheduleItem schAItem1 = schA.addItem("Boxcar");
        schAItem1.setReceiveLoadName("Food");
        schAItem1.setShipLoadName("Metal");
        schAItem1.setDestination(danvers);
        schAItem1.setDestinationTrack(danversSpur2);

        Schedule schB = scheduleManager.newSchedule("Schedule B");
        ScheduleItem schBItem1 = schB.addItem("Flat");
        schBItem1.setReceiveLoadName("Food");
        schBItem1.setShipLoadName("Junk");
        schBItem1.setDestination(Foxboro);
        schBItem1.setDestinationTrack(foxboroSpur1);

        Schedule schC = scheduleManager.newSchedule("Schedule C");
        ScheduleItem schCItem1 = schC.addItem("Boxcar");
        schCItem1.setShipLoadName("Screws");
        schCItem1.setDestination(essex);

        Schedule schD = scheduleManager.newSchedule("Schedule D");
        ScheduleItem schDItem1 = schD.addItem("Boxcar");
        schDItem1.setReceiveLoadName("Screws");
        schDItem1.setShipLoadName("Nails");
        schDItem1.setWait(1);
        schDItem1.setDestination(Foxboro);
        schDItem1.setDestinationTrack(foxboroSpur1);

        // Add schedule to tracks
        danversSpur1.setSchedule(schB);
        danversSpur2.setSchedule(schC);
        essexSpur1.setSchedule(schD);
        essexSpur2.setSchedule(schA);
        chelmsfordSpur1.setSchedule(schA);

        // bias track
        essexSpur2.setMoves(0);
        chelmsfordSpur1.setMoves(1);
        danversSpur2.setMoves(50);

        // place cars
        Assert.assertEquals("Place car", Track.OKAY, c3.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c4.setLocation(acton, actonSpur1));

        // c3 (BA 3) is a Boxcar
        c3.setLoadName("Food");	// Track Essex Spur 2 schedule is demanding this car
        c3.setReturnWhenEmptyDestination(Foxboro);

        // c4 (BB 4) is a Flat
        c4.setLoadName("Food");	// Track Danvers Spur 1 schedule is demanding this car

        // build train
        // Car c3 should be routed to Essex using 5 trains
        actonTrain.build();
        Assert.assertTrue("Acton train built", actonTrain.isBuilt());

        // check car destinations
        Assert.assertEquals("Car BA 3 destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Acton Interchange 1", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Essex", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Essex Spur 2", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "Acton", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Acton Interchange 1", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Danvers", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Danvers Spur 1", c4.getFinalDestinationTrackName());

        actonTrain.reset();
        // check car destinations after reset
        Assert.assertEquals("Car BA 3 destination", "", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "", c4.getFinalDestinationTrackName());

        // bias track
        essexSpur2.setMoves(100);

        actonTrain.reset();

        // build train
        actonTrain.build();
        Assert.assertTrue("Acton train built", actonTrain.isBuilt());

        Assert.assertEquals("Car BA 3 destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Acton Interchange 1", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Chelmsford", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Chelmsford Spur 1", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "Acton", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Acton Interchange 1", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Danvers", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Danvers Spur 1", c4.getFinalDestinationTrackName());

        // check next loads
        //Assert.assertEquals("Car BA 3 load","Metal", c3.getNextLoad());
        //Assert.assertEquals("Car BB 4 load","Junk", c4.getNextLoad());
        actonTrain.terminate();

        // check destinations
        Assert.assertEquals("Car BA 3 destination", "", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Chelmsford", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Chelmsford Spur 1", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Danvers", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Danvers Spur 1", c4.getFinalDestinationTrackName());

        // check load
        Assert.assertEquals("Car BA 3 load", "Food", c3.getLoadName());
        Assert.assertEquals("Car BB 4 load", "Food", c4.getLoadName());

        // check next loads
        Assert.assertEquals("Car BA 3 load", "", c3.getNextLoadName());
        Assert.assertEquals("Car BB 4 load", "", c4.getNextLoadName());

        // check car's location
        Assert.assertEquals("Car BA 3 location", "Acton Interchange 1", c3.getTrackName());
        Assert.assertEquals("Car BB 4 location", "Acton Interchange 1", c4.getTrackName());

        actonToBostonTrain.build();
        actonToBostonTrain.terminate();

        // check destinations
        Assert.assertEquals("Car BA 3 destination", "", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "", c3.getDestinationTrackName());
        // schedule at Chelmsford (schedule A) forwards car BA 3 to Danvers, load Metal
        Assert.assertEquals("Car BA 3 final destination", "Chelmsford", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Chelmsford Spur 1", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Danvers", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Danvers Spur 1", c4.getFinalDestinationTrackName());

        // check load
        Assert.assertEquals("Car BA 3 load", "Food", c3.getLoadName());
        Assert.assertEquals("Car BB 4 load", "Food", c4.getLoadName());

        // check next loads
        Assert.assertEquals("Car BA 3 load", "", c3.getNextLoadName());
        Assert.assertEquals("Car BB 4 load", "", c4.getNextLoadName());

        bostonToChelmsfordTrain.build();
        bostonToChelmsfordTrain.terminate();

        // check destinations
        Assert.assertEquals("Car BA 3 destination", "", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "", c3.getDestinationTrackName());
        // schedule at Danvers (schedule C) forwards car BA 3 to Essex, no track specified, load Screws
        // schedule at Chelmsford (schedule A) forwards car BA 3 to Danvers, load Metal
        Assert.assertEquals("Car BA 3 final destination", "Danvers", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Danvers Spur 2", c3.getFinalDestinationTrackName());
        // schedule at Danvers (schedule B) forwards car BB 4 to Foxboro load Junk
        Assert.assertEquals("Car BB 4 destination", "", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Danvers", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Danvers Spur 1", c4.getFinalDestinationTrackName());

        // check load
        Assert.assertEquals("Car BA 3 load", "Metal", c3.getLoadName());
        Assert.assertEquals("Car BB 4 load", "Food", c4.getLoadName());

        // check next loads
        Assert.assertEquals("Car BA 3 load", "", c3.getNextLoadName());
        Assert.assertEquals("Car BB 4 load", "", c4.getNextLoadName());

        // check car's location
        Assert.assertEquals("Car BA 3 location", "Chelmsford Spur 1", c3.getTrackName());
        Assert.assertEquals("Car BB 4 location", "Chelmsford Interchange 1", c4.getTrackName());

        chelmsfordToDanversTrain.build();
        chelmsfordToDanversTrain.terminate();

        // Train has arrived at Danvers, check destinations
        // schedule at Danvers (schedule C) forwards car BA 3 to Essex, no track specified, load Screws
        Assert.assertEquals("Car BA 3 destination", "", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Essex", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());
        // schedule at Danvers (schedule B) forward car BB 4 to Foxboro Spur 1.
        Assert.assertEquals("Car BB 4 destination", "", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Foxboro", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Foxboro Spur 1", c4.getFinalDestinationTrackName());

        // check load
        Assert.assertEquals("Car BA 3 load", "Screws", c3.getLoadName());
        Assert.assertEquals("Car BB 4 load", "Junk", c4.getLoadName());

        // check next loads
        Assert.assertEquals("Car BA 3 load", "", c3.getNextLoadName());
        Assert.assertEquals("Car BB 4 load", "", c4.getNextLoadName());

        // check car's location
        Assert.assertEquals("Car BA 3 location", "Danvers Spur 2", c3.getTrackName());
        Assert.assertEquals("Car BB 4 location", "Danvers Spur 1", c4.getTrackName());

        danversToEssexTrain.build();

        // schedule D at Essex Spur 1 is requesting load Screws, ship Nails  then forward car to Foxboro Spur 1
        Assert.assertEquals("Car BA 3 destination track", "Essex Spur 1", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Foxboro", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Foxboro Spur 1", c3.getFinalDestinationTrackName());

        // check next loads
        Assert.assertEquals("Car BA 3 load", "Nails", c3.getNextLoadName());
        Assert.assertEquals("Car BB 4 load", "", c4.getNextLoadName());

        // check next wait
        Assert.assertEquals("Car BA 3 has wait", 1, c3.getNextWait());
        Assert.assertEquals("Car BB 4 has no wait", 0, c4.getNextWait());

        danversToEssexTrain.terminate();

        // Train has arrived at Essex, check destinations
        // schedule at Essex (schedule D) forwards car BA 3 to Foxboro Spur 1 load Nails, wait = 1
        Assert.assertEquals("Car BA 3 destination", "", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Foxboro", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Foxboro Spur 1", c3.getFinalDestinationTrackName());

        Assert.assertEquals("Car BB 4 destination", "", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Foxboro", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Foxboro Spur 1", c4.getFinalDestinationTrackName());

        // check load
        Assert.assertEquals("Car BA 3 load", "Screws", c3.getLoadName()); // wait of 1 delays load change
        Assert.assertEquals("Car BB 4 load", "Junk", c4.getLoadName());

        // check next loads
        Assert.assertEquals("Car BA 3 load", "Nails", c3.getNextLoadName()); // wait of 1 delays load change
        Assert.assertEquals("Car BB 4 load", "", c4.getNextLoadName());

        // check wait
        Assert.assertEquals("Car BA 3 has wait", 1, c3.getWait());
        Assert.assertEquals("Car BB 4 has no wait", 0, c4.getWait());

        // check next wait
        Assert.assertEquals("Car BA 3 has wait", 0, c3.getNextWait());
        Assert.assertEquals("Car BB 4 has no wait", 0, c4.getNextWait());

        // check car's location
        Assert.assertEquals("Car BA 3 location", "Essex Spur 1", c3.getTrackName());
        Assert.assertEquals("Car BB 4 location", "Essex Interchange 1", c4.getTrackName());

        essexToFoxboroTrain.build();

        // confirm that only BB 4 is in train, BA 3 has wait = 1
        Assert.assertEquals("Car BA 3 not in train", null, c3.getTrain());
        Assert.assertEquals("Car BB 4 in train", essexToFoxboroTrain, c4.getTrain());
        essexToFoxboroTrain.terminate();

        // Train has arrived at Foxboro, check destinations
        Assert.assertEquals("Car BA 3 destination", "", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Foxboro", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Foxboro Spur 1", c3.getFinalDestinationTrackName());

        Assert.assertEquals("Car BB 4 destination", "", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "", c4.getFinalDestinationTrackName());

        // check load
        Assert.assertEquals("Car BA 3 load", "Nails", c3.getLoadName());
        Assert.assertEquals("Car BB 4 load", "E", c4.getLoadName());

        // check next loads
        Assert.assertEquals("Car BA 3 load", "", c3.getNextLoadName());
        Assert.assertEquals("Car BB 4 load", "", c4.getNextLoadName());

        // check wait
        Assert.assertEquals("Car BA 3 has no wait", 0, c3.getWait());
        Assert.assertEquals("Car BB 4 has no wait", 0, c4.getWait());

        // check car's location
        Assert.assertEquals("Car BA 3 location", "Essex Spur 1", c3.getTrackName());
        Assert.assertEquals("Car BB 4 location", "Foxboro Spur 1", c4.getTrackName());

        essexToFoxboroTrain.build();
        // confirm that only BA 3 is in train
        Assert.assertEquals("Car BA 3 in train", essexToFoxboroTrain, c3.getTrain());
        Assert.assertEquals("Car BB 4 not in train", null, c4.getTrain());
        essexToFoxboroTrain.terminate();

        // Train has arrived again at Foxboro, check destinations
        Assert.assertEquals("Car BA 3 destination", "", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "", c3.getDestinationTrackName());
        // Car BA 3 has return when empty destination of Foxboro, no track
        Assert.assertEquals("Car BA 3 final destination", "Foxboro", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());

        Assert.assertEquals("Car BB 4 destination", "", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "", c4.getFinalDestinationTrackName());

        // check load
        Assert.assertEquals("Car BA 3 load", "E", c3.getLoadName());
        Assert.assertEquals("Car BB 4 load", "E", c4.getLoadName());

        // check next loads
        Assert.assertEquals("Car BA 3 load", "", c3.getNextLoadName());
        Assert.assertEquals("Car BB 4 load", "", c4.getNextLoadName());
    }

    /*
     * Using the same setup from the previous tests, use trains and schedules to move
     * cars. This test creates 1 schedule with multiple items. Four cars are
     * used, three boxcars and a flat. They should be routed to the correct
     * schedule that is demanding the car type and load.
     */
    @Test
    public void testRoutingWithSchedules() {
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);

        loadLocationsTrainsAndCars();

        List<Train> trains = tmanager.getTrainsByNameList();
        Assert.assertEquals("confirm number of trains", 7, trains.size());

        Train actonTrain = tmanager.getTrainByName("Acton Local");
        Train actonToBostonTrain = tmanager.getTrainByName("Acton to Boston");
        Train bostonToChelmsfordTrain = tmanager.getTrainByName("Boston to Chelmsford");

        Car c3 = cmanager.getByRoadAndNumber("BA", "3");
        Car c4 = cmanager.getByRoadAndNumber("BB", "4");
        Car c5 = cmanager.getByRoadAndNumber("BC", "5");
        Car c6 = cmanager.getByRoadAndNumber("BD", "6");

        Location acton = lmanager.getLocationByName("Acton");
        Location Chelmsford = lmanager.getLocationByName("Chelmsford");
        Location danvers = lmanager.getLocationByName("Danvers");
        Location essex = lmanager.getLocationByName("Essex");

        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", Track.SPUR);
        Track chelmsfordSpur1 = Chelmsford.getTrackByName("Chelmsford Spur 1", Track.SPUR);
        Track danversSpur1 = danvers.getTrackByName("Danvers Spur 1", Track.SPUR);
        Track danversSpur2 = danvers.getTrackByName("Danvers Spur 2", Track.SPUR);
        Track essexSpur1 = essex.getTrackByName("Essex Spur 1", Track.SPUR);
        Track essexSpur2 = essex.getTrackByName("Essex Spur 2", Track.SPUR);

        // set the depart track Acton to service by local train only
        actonSpur1.setTrainDirections(0);

        // create schedules
        ScheduleManager scheduleManager = InstanceManager.getDefault(ScheduleManager.class);
        Schedule schA = scheduleManager.newSchedule("Schedule AA");
        ScheduleItem schAItem1 = schA.addItem("Boxcar");
        schAItem1.setReceiveLoadName("Empty");
        schAItem1.setShipLoadName("Metal");
        schAItem1.setDestination(acton);
        schAItem1.setDestinationTrack(actonSpur1);
        ScheduleItem schAItem2 = schA.addItem("Flat");
        schAItem2.setReceiveLoadName("Junk");
        schAItem2.setShipLoadName("Metal");
        schAItem2.setDestination(danvers);
        schAItem2.setDestinationTrack(danversSpur2);
        ScheduleItem schAItem3 = schA.addItem("Boxcar");
        schAItem3.setReceiveLoadName("Boxes");
        schAItem3.setShipLoadName("Screws");
        schAItem3.setDestination(danvers);
        schAItem3.setDestinationTrack(danversSpur1);

        // Add schedule to tracks
        chelmsfordSpur1.setSchedule(schA);
        chelmsfordSpur1.setScheduleMode(Track.SEQUENTIAL);
        danversSpur1.setSchedule(null);
        danversSpur2.setSchedule(null);
        essexSpur1.setSchedule(null);
        essexSpur2.setSchedule(null);

        // c3 (BA 3) is a Boxcar
        c3.setLoadName("Empty");

        // c4 (BB 4) is a Flat
        c4.setLoadName("Junk");

        c5.setLoadName("Boxes");

        c6.setLoadName("Empty");

        // place cars
        Assert.assertEquals("Place car", Track.OKAY, c3.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c4.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c5.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c6.setLocation(acton, actonSpur1));

        // note car move count is exactly the same order as schedule
        // build train
        actonTrain.build();
        Assert.assertTrue("Acton train built", actonTrain.isBuilt());

        // check car destinations
        Assert.assertEquals("Car BA 3 destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Acton Interchange 1", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Chelmsford", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Chelmsford Spur 1", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "Acton", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Acton Interchange 1", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Chelmsford", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Chelmsford Spur 1", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 destination", "Acton", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Acton Interchange 1", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "Chelmsford", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "Chelmsford Spur 1", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 destination", "Acton", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Acton Interchange 1", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "Chelmsford", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "Chelmsford Spur 1", c6.getFinalDestinationTrackName());

        // check car schedule ids
        Assert.assertEquals("Car BA 3 schedule id", schAItem1.getId(), c3.getScheduleItemId());
        Assert.assertEquals("Car BB 4 schedule id", schAItem2.getId(), c4.getScheduleItemId());
        Assert.assertEquals("Car BC 5 schedule id", schAItem3.getId(), c5.getScheduleItemId());
        Assert.assertEquals("Car BD 6 schedule id", schAItem1.getId(), c6.getScheduleItemId());

        actonTrain.reset();

        // Next car in schedule is flat car
        // build train
        actonTrain.build();
        Assert.assertTrue("Acton train built", actonTrain.isBuilt());

        // check car destinations
        Assert.assertEquals("Car BA 3 destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Acton Yard 1", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "Acton", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Acton Interchange 1", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Chelmsford", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Chelmsford Spur 1", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 destination", "Acton", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Acton Interchange 1", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "Chelmsford", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "Chelmsford Spur 1", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 destination", "Acton", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Acton Interchange 1", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "Chelmsford", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "Chelmsford Spur 1", c6.getFinalDestinationTrackName());

        // check car schedule ids
        Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleItemId());
        Assert.assertEquals("Car BB 4 schedule id", schAItem2.getId(), c4.getScheduleItemId());
        Assert.assertEquals("Car BC 5 schedule id", schAItem3.getId(), c5.getScheduleItemId());
        Assert.assertEquals("Car BD 6 schedule id", schAItem1.getId(), c6.getScheduleItemId());

        actonTrain.terminate();
        // move the cars to Boston
        actonToBostonTrain.build();
        Assert.assertTrue("Boston train built", actonToBostonTrain.isBuilt());

        // check car destinations
        Assert.assertEquals("Car BA 3 destination", "Boston", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Boston Yard 1", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "Boston", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Boston Interchange 1", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Chelmsford", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Chelmsford Spur 1", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 destination", "Boston", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Boston Interchange 1", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "Chelmsford", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "Chelmsford Spur 1", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 destination", "Boston", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Boston Interchange 1", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "Chelmsford", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "Chelmsford Spur 1", c6.getFinalDestinationTrackName());

        // check car schedule ids
        Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleItemId());
        Assert.assertEquals("Car BB 4 schedule id", schAItem2.getId(), c4.getScheduleItemId());
        Assert.assertEquals("Car BC 5 schedule id", schAItem3.getId(), c5.getScheduleItemId());
        Assert.assertEquals("Car BD 6 schedule id", schAItem1.getId(), c6.getScheduleItemId());

        actonToBostonTrain.terminate();
        // move the cars to Boston
        bostonToChelmsfordTrain.build();
        Assert.assertTrue("Boston train built", bostonToChelmsfordTrain.isBuilt());

        // check car destinations
        Assert.assertEquals("Car BA 3 destination", "Chelmsford", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Chelmsford Yard 1", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "Chelmsford", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Chelmsford Spur 1", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Danvers", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Danvers Spur 2", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 destination", "Chelmsford", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Chelmsford Spur 1", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "Danvers", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "Danvers Spur 1", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 destination", "Chelmsford", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Chelmsford Spur 1", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "Acton", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "Acton Spur 1", c6.getFinalDestinationTrackName());

        // check car schedule ids
        Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleItemId());
        Assert.assertEquals("Car BB 4 schedule id", "", c4.getScheduleItemId());
        Assert.assertEquals("Car BC 5 schedule id", "", c5.getScheduleItemId());
        Assert.assertEquals("Car BD 6 schedule id", "", c6.getScheduleItemId());

        bostonToChelmsfordTrain.terminate();
    }

    /*
     * Using the setup from the previous tests, use trains and schedules to move
     * cars. This test creates 1 schedule in match mode with multiple items.
     * Test uses car loads to activate schedule.
     */
    @Test
    public void testRoutingWithSchedulesMatchMode() {
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);

        loadLocationsTrainsAndCars();

        List<Train> trains = tmanager.getTrainsByNameList();
        Assert.assertEquals("confirm number of trains", 7, trains.size());

        Car c3 = cmanager.getByRoadAndNumber("BA", "3");
        Car c4 = cmanager.getByRoadAndNumber("BB", "4");
        Car c5 = cmanager.getByRoadAndNumber("BC", "5");
        Car c6 = cmanager.getByRoadAndNumber("BD", "6");
        Car c7 = cmanager.getByRoadAndNumber("BA", "7");
        Car c8 = cmanager.getByRoadAndNumber("BB", "8");
        Car c9 = cmanager.getByRoadAndNumber("BC", "9");

        Location acton = lmanager.getLocationByName("Acton");
        Location Chelmsford = lmanager.getLocationByName("Chelmsford");
        Location danvers = lmanager.getLocationByName("Danvers");
        Location essex = lmanager.getLocationByName("Essex");

        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", Track.SPUR);
        Track chelmsfordSpur1 = Chelmsford.getTrackByName("Chelmsford Spur 1", Track.SPUR);
        Track danversSpur1 = danvers.getTrackByName("Danvers Spur 1", Track.SPUR);
        Track danversSpur2 = danvers.getTrackByName("Danvers Spur 2", Track.SPUR);
        Track essexSpur1 = essex.getTrackByName("Essex Spur 1", Track.SPUR);
        Track essexSpur2 = essex.getTrackByName("Essex Spur 2", Track.SPUR);

        // set the depart track Acton to service by local train only
        actonSpur1.setTrainDirections(0);

        // create schedules
        ScheduleManager scheduleManager = InstanceManager.getDefault(ScheduleManager.class);
        Schedule schA = scheduleManager.newSchedule("Schedule AAA");
        ScheduleItem schAItem1 = schA.addItem("Boxcar");
        schAItem1.setReceiveLoadName("Empty");
        schAItem1.setShipLoadName("Metal");
        schAItem1.setDestination(acton);
        schAItem1.setDestinationTrack(actonSpur1);
        ScheduleItem schAItem2 = schA.addItem("Flat");
        schAItem2.setReceiveLoadName("Junk");
        schAItem2.setShipLoadName("Metal");
        schAItem2.setDestination(danvers);
        schAItem2.setDestinationTrack(danversSpur2);
        ScheduleItem schAItem3 = schA.addItem("Boxcar");
        schAItem3.setReceiveLoadName("Boxes");
        schAItem3.setShipLoadName("Screws");
        schAItem3.setDestination(essex);
        schAItem3.setDestinationTrack(essexSpur1);
        ScheduleItem schAItem4 = schA.addItem("Boxcar");
        schAItem4.setReceiveLoadName("Boxes");
        schAItem4.setShipLoadName("Bolts");
        schAItem4.setDestination(danvers);
        schAItem4.setDestinationTrack(danversSpur1);
        ScheduleItem schAItem5 = schA.addItem("Boxcar");
        schAItem5.setReceiveLoadName("");
        schAItem5.setShipLoadName("Nuts");
        schAItem5.setDestination(essex);
        schAItem5.setDestinationTrack(essexSpur2);

        // Add schedule to tracks
        chelmsfordSpur1.setSchedule(null);
        essexSpur1.setSchedule(schA);
        essexSpur1.setScheduleMode(Track.MATCH);	// set schedule into match mode

        // c3 (BA 3) is a Boxcar
        c3.setLoadName("Boxes");

        // c4 (BB 4) is a Flat
        c4.setLoadName("Junk");

        // c5 (BC 5) is a Boxcar
        c5.setLoadName("Boxes");

        // c6 (BD 6) is a Boxcar
        c6.setLoadName("Boxes");

        // place cars
        Assert.assertEquals("Place car", Track.OKAY, c3.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c4.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c5.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c6.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c7.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c8.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c9.setLocation(acton, actonSpur1));

        // build train
        Train actonTrain = tmanager.getTrainByName("Acton Local");
        actonTrain.build();
        Assert.assertTrue("Acton train built", actonTrain.isBuilt());

        // check car destinations
        Assert.assertEquals("Car BA 3 destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Acton Interchange 1", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Essex", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Essex Spur 1", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "Acton", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Acton Interchange 1", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Essex", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Essex Spur 1", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 destination", "Acton", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Acton Interchange 1", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "Essex", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "Essex Spur 1", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 destination", "Acton", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Acton Interchange 1", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "Essex", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "Essex Spur 1", c6.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 7 destination", "Acton", c7.getDestinationName());
        Assert.assertEquals("Car BD 7 destination track", "Acton Interchange 1", c7.getDestinationTrackName());
        Assert.assertEquals("Car BD 7 final destination", "Essex", c7.getFinalDestinationName());
        Assert.assertEquals("Car BD 7 final destination track", "Essex Spur 1", c7.getFinalDestinationTrackName());

        // check car schedule ids
        Assert.assertEquals("Car BA 3 schedule id", schAItem3.getId(), c3.getScheduleItemId());
        Assert.assertEquals("Car BB 4 schedule id", schAItem2.getId(), c4.getScheduleItemId());
        Assert.assertEquals("Car BC 5 schedule id", schAItem3.getId(), c5.getScheduleItemId());
        Assert.assertEquals("Car BD 6 schedule id", schAItem4.getId(), c6.getScheduleItemId());
        Assert.assertEquals("Car BA 7 schedule id", schAItem5.getId(), c7.getScheduleItemId());
        Assert.assertEquals("Car BB 8 schedule id", schAItem1.getId(), c8.getScheduleItemId());
        Assert.assertEquals("Car BC 9 schedule id", schAItem5.getId(), c9.getScheduleItemId());

        actonTrain.reset();
    }

    /*
     * Using the setup from the previous tests, use trains and schedules to move
     * cars. This test creates 1 schedule in match mode with multiple items.
     * Cars use final destination to activate schedule
     */
    @Test
    public void testRoutingWithSchedulesMatchMode2() {
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);

        loadLocationsTrainsAndCars();

        List<Train> trains = tmanager.getTrainsByNameList();
        Assert.assertEquals("confirm number of trains", 7, trains.size());

        Car c3 = cmanager.getByRoadAndNumber("BA", "3");
        Car c4 = cmanager.getByRoadAndNumber("BB", "4");
        Car c5 = cmanager.getByRoadAndNumber("BC", "5");
        Car c6 = cmanager.getByRoadAndNumber("BD", "6");
        Car c7 = cmanager.getByRoadAndNumber("BA", "7");
        Car c8 = cmanager.getByRoadAndNumber("BB", "8");
        Car c9 = cmanager.getByRoadAndNumber("BC", "9");

        Assert.assertNotNull("confirm car not null", c3);
        Assert.assertNotNull("confirm car not null", c4);
        Assert.assertNotNull("confirm car not null", c5);
        Assert.assertNotNull("confirm car not null", c6);
        Assert.assertNotNull("confirm car not null", c7);
        Assert.assertNotNull("confirm car not null", c8);
        Assert.assertNotNull("confirm car not null", c9);

        Location acton = lmanager.getLocationByName("Acton");
        Location Boston = lmanager.getLocationByName("Boston");
        Location Chelmsford = lmanager.getLocationByName("Chelmsford");
        Location danvers = lmanager.getLocationByName("Danvers");
        Location essex = lmanager.getLocationByName("Essex");
        Location Foxboro = lmanager.getLocationByName("Foxboro");

        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", Track.SPUR);
        Track bostonSpur1 = Boston.getTrackByName("Boston Spur 1", Track.SPUR);
        Track chelmsfordSpur1 = Chelmsford.getTrackByName("Chelmsford Spur 1", Track.SPUR);
        Track danversSpur1 = danvers.getTrackByName("Danvers Spur 1", Track.SPUR);
        Track danversSpur2 = danvers.getTrackByName("Danvers Spur 2", Track.SPUR);
        Track essexSpur1 = essex.getTrackByName("Essex Spur 1", Track.SPUR);
        //Track essexSpur2 = essex.getTrackByName("Essex Spur 2", Track.SIDING);
        Track foxboroSpur1 = Foxboro.getTrackByName("Foxboro Spur 1", Track.SPUR);

        // set the depart track Acton to service by local train only
        actonSpur1.setTrainDirections(0);

        // create schedules
        ScheduleManager scheduleManager = InstanceManager.getDefault(ScheduleManager.class);
        Schedule schA = scheduleManager.newSchedule("Schedule ABC");

        ScheduleItem schAItem1 = schA.addItem("Boxcar");
        schAItem1.setReceiveLoadName("Cardboard");
        schAItem1.setShipLoadName("Metal");
        schAItem1.setDestination(acton);
        schAItem1.setDestinationTrack(actonSpur1);

        ScheduleItem schAItem2 = schA.addItem("Flat");
        //schAItem2.setLoad("Junk");
        schAItem2.setShipLoadName("Metal");
        schAItem2.setDestination(danvers);
        schAItem2.setDestinationTrack(danversSpur2);

        ScheduleItem schAItem3 = schA.addItem("Boxcar");
        schAItem3.setReceiveLoadName("Tools");
        schAItem3.setShipLoadName("Screws");
        schAItem3.setDestination(Boston);
        schAItem3.setDestinationTrack(bostonSpur1);

        ScheduleItem schAItem4 = schA.addItem("Boxcar");
        schAItem4.setReceiveLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName());
        schAItem4.setShipLoadName("Bolts");
        schAItem4.setDestination(danvers);
        schAItem4.setDestinationTrack(danversSpur1);

        ScheduleItem schAItem5 = schA.addItem("Boxcar");
        schAItem5.setReceiveLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultLoadName());
        schAItem5.setShipLoadName("Nuts");
        schAItem5.setDestination(Foxboro);
        schAItem5.setDestinationTrack(foxboroSpur1);

        // Add schedule to tracks
        chelmsfordSpur1.setSchedule(null);
        essexSpur1.setSchedule(schA);
        essexSpur1.setScheduleMode(Track.MATCH);	// set schedule into match mode

        // c3 (BA 3) is a Boxcar
        c3.setLoadName("Tools");
        c3.setFinalDestination(essex);

        // c4 (BB 4) is a Flat
        c4.setLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName());
        c4.setFinalDestination(essex);
        c4.setFinalDestinationTrack(essexSpur1);

        // c5 (BC 5) is a Boxcar
        c5.setLoadName("Tools");
        c5.setFinalDestination(essex);

        // c6 (BD 6) is a Boxcar
        c6.setLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName());
        c6.setFinalDestination(essex);

        // c7 (BA 7) is a Boxcar
        c7.setLoadName("Cardboard");
        c7.setFinalDestination(essex);
        c7.setFinalDestinationTrack(essexSpur1);

        // c8 (BB 8) is a Boxcar
        c8.setLoadName("Tools");
        c8.setMoves(20);	// serve BB 8 and BC 9 after the other cars

        // c9 (BC 9) is a Boxcar
        c9.setLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName());
        c9.setMoves(21);

        // place cars
        Assert.assertEquals("Place car", Track.OKAY, c3.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c4.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c5.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c6.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c7.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c8.setLocation(danvers, danversSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c9.setLocation(danvers, danversSpur1));

        // build train
        Train actonTrain = tmanager.getTrainByName("Acton Local");
        actonTrain.build();
        Assert.assertTrue("Acton train built", actonTrain.isBuilt());

        // check car destinations
        Assert.assertEquals("Car BA 3 destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Acton Interchange 1", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Essex", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "Acton", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Acton Interchange 1", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Essex", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Essex Spur 1", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 destination", "Acton", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Acton Interchange 1", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "Essex", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 destination", "Acton", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Acton Interchange 1", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "Essex", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "", c6.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 7 destination", "Acton", c7.getDestinationName());
        Assert.assertEquals("Car BA 7 destination track", "Acton Interchange 1", c7.getDestinationTrackName());
        Assert.assertEquals("Car BA 7 final destination", "Essex", c7.getFinalDestinationName());
        Assert.assertEquals("Car BA 7 final destination track", "Essex Spur 1", c7.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 8 destination", "", c8.getDestinationName());
        Assert.assertEquals("Car BB 8 destination track", "", c8.getDestinationTrackName());
        Assert.assertEquals("Car BB 8 final destination", "", c8.getFinalDestinationName());
        Assert.assertEquals("Car BB 8 final destination track", "", c8.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 9 destination", "", c9.getDestinationName());
        Assert.assertEquals("Car BC 9 destination track", "", c9.getDestinationTrackName());
        Assert.assertEquals("Car BC 9 final destination", "", c9.getFinalDestinationName());
        Assert.assertEquals("Car BC 9 final destination track", "", c9.getFinalDestinationTrackName());

        // check car schedule ids
        Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleItemId());	// no track assignment, schedule not tested
        Assert.assertEquals("Car BB 4 schedule id", schAItem2.getId(), c4.getScheduleItemId());	// has track assignment
        Assert.assertEquals("Car BC 5 schedule id", "", c5.getScheduleItemId());
        Assert.assertEquals("Car BD 6 schedule id", "", c6.getScheduleItemId());
        Assert.assertEquals("Car BA 7 schedule id", schAItem1.getId(), c7.getScheduleItemId());	// has track assignment
        Assert.assertEquals("Car BB 8 schedule id", "", c8.getScheduleItemId());
        Assert.assertEquals("Car BC 9 schedule id", "", c9.getScheduleItemId());

        actonTrain.terminate();

        // move the cars to Boston
        Train actonToBostonTrain = tmanager.getTrainByName("Acton to Boston");
        actonToBostonTrain.build();
        Assert.assertTrue("Acton train built", actonToBostonTrain.isBuilt());
        actonToBostonTrain.terminate();

        // move the cars to Chelmsford
        Train bostonToChelmsfordTrain = tmanager.getTrainByName("Boston to Chelmsford");
        bostonToChelmsfordTrain.build();
        Assert.assertTrue("Boston train built", bostonToChelmsfordTrain.isBuilt());
        bostonToChelmsfordTrain.terminate();

        // move the cars to Danvers
        Train chelmsfordToDanversTrain = tmanager.getTrainByName("Chelmsford to Danvers");
        chelmsfordToDanversTrain.build();
        Assert.assertTrue("Chelmsford train built", chelmsfordToDanversTrain.isBuilt());
        chelmsfordToDanversTrain.terminate();

        // move the cars to Essex (number of moves is 8)
        Train danversToEssexTrain = tmanager.getTrainByName("Danvers to Essex");
        danversToEssexTrain.build();
        Assert.assertTrue("Danvers train built", danversToEssexTrain.isBuilt());

        // check car destinations
        // BA 3 (Boxcar)
        Assert.assertEquals("Car BA 3 destination", "Essex", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Essex Spur 1", c3.getDestinationTrackName());
        // new final destination and load for car BA 3
        Assert.assertEquals("Car BA 3 final destination", "Boston", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Boston Spur 1", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 3 final load", "Screws", c3.getNextLoadName());
        Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleItemId());
        // BB 4 (Flat)
        Assert.assertEquals("Car BB 4 destination", "Essex", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Essex Spur 1", c4.getDestinationTrackName());
        // new final destination and load for car BB 4
        Assert.assertEquals("Car BB 4 final destination", "Danvers", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Danvers Spur 2", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 final load", "Metal", c4.getNextLoadName());
        Assert.assertEquals("Car BB 4 schedule id", "", c4.getScheduleItemId());
        // BC 5 (Boxcar)
        Assert.assertEquals("Car BC 5 destination", "Essex", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Essex Spur 1", c5.getDestinationTrackName());
        // new final destination and load for car BC 5, same as BA 3
        Assert.assertEquals("Car BC 5 final destination", "Boston", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "Boston Spur 1", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 final load", "Screws", c5.getNextLoadName());
        Assert.assertEquals("Car BC 5 schedule id", "", c5.getScheduleItemId());
        // BD 6 (Boxcar) note second Boxcar
        Assert.assertEquals("Car BD 6 destination", "Essex", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Essex Spur 1", c6.getDestinationTrackName());
        // new final destination and load for car BD 6
        Assert.assertEquals("Car BD 6 final destination", "Danvers", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "Danvers Spur 1", c6.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 6 final load", "Bolts", c6.getNextLoadName());
        Assert.assertEquals("Car BD 6 schedule id", "", c6.getScheduleItemId());
        // BA 7 (Boxcar) note 3rd Boxcar
        Assert.assertEquals("Car BA 7 destination", "Essex", c7.getDestinationName());
        Assert.assertEquals("Car BA 7 destination track", "Essex Spur 1", c7.getDestinationTrackName());
        // new final destination and load for car BA 7
        Assert.assertEquals("Car BA 7 final destination", "Acton", c7.getFinalDestinationName());
        Assert.assertEquals("Car BA 7 final destination track", "Acton Spur 1", c7.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 7 final load", "Metal", c7.getNextLoadName());
        Assert.assertEquals("Car BA 7 schedule id", "", c7.getScheduleItemId());
        // BB 8 (Boxcar) at Danvers to be added to train
        Assert.assertEquals("Car BB 8 destination", "Essex", c8.getDestinationName());
        Assert.assertEquals("Car BB 8 destination track", "Essex Spur 1", c8.getDestinationTrackName());
        // Should match schedule item 16c3
        Assert.assertEquals("Car BB 8 final destination", "Boston", c8.getFinalDestinationName());
        Assert.assertEquals("Car BB 8 final destination track", "Boston Spur 1", c8.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 8 final load", "Screws", c8.getNextLoadName());
        Assert.assertEquals("Car BB 8 schedule id", "", c8.getScheduleItemId());
        // BB 9 (Boxcar) at Danvers to be added to train
        Assert.assertEquals("Car BC 9 destination", "Essex", c9.getDestinationName());
        Assert.assertEquals("Car BC 9 destination track", "Essex Spur 1", c9.getDestinationTrackName());
        // Should match schedule item 16c4
        Assert.assertEquals("Car BC 9 final destination", "Danvers", c9.getFinalDestinationName());
        Assert.assertEquals("Car BC 9 final destination track", "Danvers Spur 1", c9.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 9 final load", "Bolts", c9.getNextLoadName());
        Assert.assertEquals("Car BC 9 schedule id", "", c9.getScheduleItemId());

        // test reset, car final destinations should revert.
        danversToEssexTrain.reset();

        Assert.assertEquals("Car BA 3 destination", "", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Essex", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 3 next load", "", c3.getNextLoadName());
        Assert.assertEquals("Car BB 4 destination", "", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Essex", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Essex Spur 1", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 destination", "", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "Essex", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 destination", "", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "Essex", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "", c6.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 7 destination", "", c7.getDestinationName());
        Assert.assertEquals("Car BA 7 destination track", "", c7.getDestinationTrackName());
        Assert.assertEquals("Car BA 7 final destination", "Essex", c7.getFinalDestinationName());
        Assert.assertEquals("Car BA 7 final destination track", "Essex Spur 1", c7.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 8 destination", "", c8.getDestinationName());
        Assert.assertEquals("Car BB 8 destination track", "", c8.getDestinationTrackName());
        Assert.assertEquals("Car BB 8 final destination", "", c8.getFinalDestinationName());
        Assert.assertEquals("Car BB 8 final destination track", "", c8.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 9 destination", "", c9.getDestinationName());
        Assert.assertEquals("Car BC 9 destination track", "", c9.getDestinationTrackName());
        Assert.assertEquals("Car BC 9 final destination", "", c9.getFinalDestinationName());
        Assert.assertEquals("Car BC 9 final destination track", "", c9.getFinalDestinationTrackName());
    }

    /* This test confirms that schedules can be linked together.
     * Note that there are schedules at Essex that are still active
     * but not reachable because the Chelmsford to Danvers train is
     * removed as part of this test.
     * has 5 tracks, 3 spurs, yard, and an interchange track.
     *
     */
    @Test
    public void testRoutingWithSchedulesLocal() {
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        CarLoads clm = InstanceManager.getDefault(CarLoads.class);

        loadLocationsTrainsAndCars();

        List<Train> trains = tmanager.getTrainsByNameList();
        Assert.assertEquals("confirm number of trains", 7, trains.size());

        Car c3 = cmanager.getByRoadAndNumber("BA", "3");
        Car c4 = cmanager.getByRoadAndNumber("BB", "4");
        Car c5 = cmanager.getByRoadAndNumber("BC", "5");
        Car c6 = cmanager.getByRoadAndNumber("BD", "6");
        Car c7 = cmanager.getByRoadAndNumber("BA", "7");
        Car c8 = cmanager.getByRoadAndNumber("BB", "8");
        Car c9 = cmanager.getByRoadAndNumber("BC", "9");

        // c3 (BA 3) is a Boxcar
        c3.setLoadName("Cardboard");

        // c4 (BB 4) is a Flat
        c4.setLoadName("Trucks");

        // c5 (BC 5) is a Boxcar
        c5.setLoadName(clm.getDefaultEmptyName());

        // c6 (BD 6) is a Boxcar
        c6.setLoadName(clm.getDefaultEmptyName());

        // c7 (BA 7) is a Boxcar
        c7.setLoadName(clm.getDefaultEmptyName());

        // c8 (BB 8) is a Boxcar
        c8.setLoadName("Trucks");

        // c9 (BC 9) is a Boxcar
        c9.setLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName());

        Location acton = lmanager.getLocationByName("Acton");
        Location Boston = lmanager.getLocationByName("Boston");
        Location Chelmsford = lmanager.getLocationByName("Chelmsford");
        Track chelmsfordSpur1 = Chelmsford.getTrackByName("Chelmsford Spur 1", Track.SPUR);

        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", Track.SPUR);
        actonSpur1.setTrainDirections(Track.NORTH + Track.SOUTH);
        Track actonSpur2 = acton.getTrackByName("Acton Spur 2", Track.SPUR);
        Track AS3 = acton.addTrack("Acton Spur 3", Track.SPUR);
        AS3.setLength(300);
        Track actonYard = acton.getTrackByName("Acton Yard 1", Track.YARD);
        Track actonInterchange = acton.getTrackByName("Acton Interchange 1", Track.INTERCHANGE);

        // create schedules
        ScheduleManager scheduleManager = InstanceManager.getDefault(ScheduleManager.class);
        Schedule schA = scheduleManager.newSchedule("Schedule Acton");
        ScheduleItem schAItem1 = schA.addItem("Boxcar");
        schAItem1.setReceiveLoadName("Cardboard");
        schAItem1.setShipLoadName("Scrap");
        ScheduleItem schAItem2 = schA.addItem("Gon");
        schAItem2.setReceiveLoadName("Trucks");
        schAItem2.setShipLoadName("Tires");
        schAItem2.setDestination(Boston);
        ScheduleItem schAItem3 = schA.addItem("Boxcar");
        schAItem3.setReceiveLoadName("Trucks");
        schAItem3.setShipLoadName("Wire");
        schAItem3.setDestination(Chelmsford);
        schAItem3.setDestinationTrack(chelmsfordSpur1);
        ScheduleItem schAItem4 = schA.addItem("Flat");
        schAItem4.setReceiveLoadName("Trucks");
        schAItem4.setShipLoadName("Coils");
        schAItem4.setDestination(Boston);
        ScheduleItem schAItem5 = schA.addItem("Flat");
        schAItem5.setReceiveLoadName("Coils");
        schAItem5.setShipLoadName("Trucks");
        schAItem5.setDestination(Boston);
        ScheduleItem schAItem6 = schA.addItem("Boxcar");
        schAItem6.setReceiveLoadName("Scrap");
        schAItem6.setShipLoadName("E");
        ScheduleItem schAItem7 = schA.addItem("Boxcar");
        schAItem7.setReceiveLoadName("Wire");
        schAItem7.setShipLoadName("L");

        // add schedules to tracks
        actonSpur1.setSchedule(schA);
        actonSpur1.setScheduleMode(Track.SEQUENTIAL);
        actonSpur2.setSchedule(schA);
        actonSpur2.setScheduleMode(Track.SEQUENTIAL);
        AS3.setSchedule(schA);
        // put Action Spur 3 into match mode
        AS3.setScheduleMode(Track.MATCH);

        // place cars
        Assert.assertEquals("Place car", Track.OKAY, c3.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c4.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c5.setLocation(acton, actonSpur2));
        Assert.assertEquals("Place car", Track.OKAY, c6.setLocation(acton, actonSpur2));
        Assert.assertEquals("Place car", Track.OKAY, c7.setLocation(acton, AS3));
        Assert.assertEquals("Place car", Track.OKAY, c8.setLocation(acton, actonYard));
        Assert.assertEquals("Place car", Track.OKAY, c9.setLocation(acton, actonInterchange));

        // Build train
        Train tActonToBostonTrain = tmanager.getTrainByName("Acton to Boston");
        Route rActonToBeford = tActonToBostonTrain.getRoute();
        RouteLocation rl = rActonToBeford.getDepartsRouteLocation();
        RouteLocation rd = rActonToBeford.getLastLocationByName("Boston");
        // increase the number of moves so all cars are used
        rl.setMaxCarMoves(10);
        rd.setMaxCarMoves(10);
        // kill the Chelmsford to Danvers train
        Train chelmsfordToDanversTrain = tmanager.getTrainByName("Chelmsford to Danvers");
        tmanager.deregister(chelmsfordToDanversTrain);

        tActonToBostonTrain.build();
        Assert.assertTrue("Acton train built", tActonToBostonTrain.isBuilt());

        // check cars
        Assert.assertEquals("Car BA 3 destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Acton Spur 2", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 3 load", "Cardboard", c3.getLoadName());
        Assert.assertEquals("Car BA 3 next load", "Scrap", c3.getNextLoadName());

        Assert.assertEquals("Car BB 4 destination", "Acton", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Acton Spur 3", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Boston", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 load", "Trucks", c4.getLoadName());
        Assert.assertEquals("Car BB 4 next load", "Coils", c4.getNextLoadName());

        Assert.assertEquals("Car BC 5 destination", "Boston", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Boston Spur 1", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 load", "E", c5.getLoadName());
        Assert.assertEquals("Car BC 5 next load", "", c5.getNextLoadName());

        Assert.assertEquals("Car BD 6 destination", "Boston", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Boston Spur 1", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "", c6.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 load", "E", c6.getLoadName());
        Assert.assertEquals("Car BD 6 next load", "", c6.getNextLoadName());

        Assert.assertEquals("Car BA 7 destination", "Boston", c7.getDestinationName());
        Assert.assertEquals("Car BA 7 destination track", "Boston Spur 1", c7.getDestinationTrackName());
        Assert.assertEquals("Car BA 7 final destination", "", c7.getFinalDestinationName());
        Assert.assertEquals("Car BA 7 final destination track", "", c7.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 7 load", "E", c7.getLoadName());
        Assert.assertEquals("Car BA 7 next load", "", c7.getNextLoadName());

        Assert.assertEquals("Car BB 8 destination", "Acton", c8.getDestinationName());
        Assert.assertEquals("Car BB 8 destination track", "Acton Spur 3", c8.getDestinationTrackName());
        Assert.assertEquals("Car BB 8 final destination", "Chelmsford", c8.getFinalDestinationName());
        Assert.assertEquals("Car BB 8 final destination track", "Chelmsford Spur 1", c8.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 8 load", "Trucks", c8.getLoadName());
        Assert.assertEquals("Car BB 8 next load", "Wire", c8.getNextLoadName());

        Assert.assertEquals("Car BC 9 destination", "Boston", c9.getDestinationName());
        Assert.assertEquals("Car BC 9 destination track", "Boston Spur 1", c9.getDestinationTrackName());
        Assert.assertEquals("Car BC 9 final destination", "", c9.getFinalDestinationName());
        Assert.assertEquals("Car BC 9 final destination track", "", c9.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 9 load", "E", c9.getLoadName());
        Assert.assertEquals("Car BC 9 next load", "", c9.getNextLoadName());

        tActonToBostonTrain.terminate();

        // Build train
        Train tBostonToActon = tmanager.newTrain("BostonToActonToBoston");
        Route rBostonToActon = InstanceManager.getDefault(RouteManager.class).newRoute("BostonToActonToBoston");
        RouteLocation rlB2 = rBostonToActon.addLocation(Boston);
        RouteLocation rlA2 = rBostonToActon.addLocation(acton);
        RouteLocation rlB3 = rBostonToActon.addLocation(Boston);
        // increase the number of moves so all cars are used
        rlB2.setMaxCarMoves(10);
        rlA2.setMaxCarMoves(10);
        rlB3.setMaxCarMoves(10);
        tBostonToActon.setRoute(rBostonToActon);

        tBostonToActon.build();

        // check cars
        Assert.assertEquals("Car BA 3 destination", "Acton", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Acton Spur 3", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 3 load", "Scrap", c3.getLoadName());
        Assert.assertEquals("Car BA 3 next load", "E", c3.getNextLoadName());

        Assert.assertEquals("Car BB 4 destination", "Boston", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Boston Yard 1", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 load", "Coils", c4.getLoadName());
        Assert.assertEquals("Car BB 4 next load", "", c4.getNextLoadName());

        Assert.assertEquals("Car BC 5 destination", "Acton", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Acton Yard 1", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 load", "L", c5.getLoadName());
        Assert.assertEquals("Car BC 5 next load", "", c5.getNextLoadName());

        Assert.assertEquals("Car BD 6 destination", "Acton", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Acton Yard 2", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "", c6.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 load", "L", c6.getLoadName());
        Assert.assertEquals("Car BD 6 next load", "", c6.getNextLoadName());

        Assert.assertEquals("Car BA 7 destination", "Acton", c7.getDestinationName());
        Assert.assertEquals("Car BA 7 destination track", "Acton Interchange 1", c7.getDestinationTrackName());
        Assert.assertEquals("Car BA 7 final destination", "", c7.getFinalDestinationName());
        Assert.assertEquals("Car BA 7 final destination track", "", c7.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 7 load", "L", c7.getLoadName());
        Assert.assertEquals("Car BA 7 next load", "", c7.getNextLoadName());

        Assert.assertEquals("Car BB 8 destination", "Boston", c8.getDestinationName());
        Assert.assertEquals("Car BB 8 destination track", "Boston Interchange 1", c8.getDestinationTrackName());
        Assert.assertEquals("Car BB 8 final destination", "Chelmsford", c8.getFinalDestinationName());
        Assert.assertEquals("Car BB 8 final destination track", "Chelmsford Spur 1", c8.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 8 load", "Wire", c8.getLoadName());
        Assert.assertEquals("Car BB 8 next load", "", c8.getNextLoadName());

        Assert.assertEquals("Car BC 9 destination", "Acton", c9.getDestinationName());
        Assert.assertEquals("Car BC 9 destination track", "Acton Interchange 2", c9.getDestinationTrackName());
        Assert.assertEquals("Car BC 9 final destination", "", c9.getFinalDestinationName());
        Assert.assertEquals("Car BC 9 final destination track", "", c9.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 9 load", "L", c9.getLoadName());
        Assert.assertEquals("Car BC 9 next load", "", c9.getNextLoadName());

        tBostonToActon.terminate();
    }
    
    /**
     * Test that only lead car in kernel is routed
     */
    @Test
    public void testRoutingCarKernel() {
        // load up the managers
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Router router = InstanceManager.getDefault(Router.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);

        // register the car and engine types used
        ct.addName("Boxcar");
        
        // create locations and tracks
        Location acton = lmanager.newLocation("Acton");
        Assert.assertEquals("Location 1 Name", "Acton", acton.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, acton.getLength());

        Track actonSpur1 = acton.addTrack("Acton Spur 1", Track.SPUR);
        actonSpur1.setLength(300);
        Assert.assertEquals("Location actonSpur1 Name", "Acton Spur 1", actonSpur1.getName());
        Assert.assertEquals("Location actonSpur1 Length", 300, actonSpur1.getLength());

        Track actonSpur2 = acton.addTrack("Acton Spur 2", Track.SPUR);
        actonSpur2.setLength(300);
        Assert.assertEquals("Location actonSpur2 Name", "Acton Spur 2", actonSpur2.getName());
        Assert.assertEquals("Location actonSpur2 Length", 300, actonSpur2.getLength());

        Track actonYard = acton.addTrack("Acton Yard 1", Track.YARD);
        actonYard.setLength(400);
        Assert.assertEquals("Location actonYard Name", "Acton Yard 1", actonYard.getName());
        Assert.assertEquals("Location actonYard Length", 400, actonYard.getLength());

        Track actonInterchange1 = acton.addTrack("Acton Interchange 1", Track.INTERCHANGE);
        actonInterchange1.setLength(500);
        Assert.assertEquals("Track actonInterchange Name", "Acton Interchange 1", actonInterchange1.getName());
        Assert.assertEquals("Track actonInterchange Length", 500, actonInterchange1.getLength());
        Assert.assertEquals("Track actonInterchange Train Directions", DIRECTION_ALL, actonInterchange1.getTrainDirections());

        // add a second interchange track
        Track actonInterchange2 = acton.addTrack("Acton Interchange 2", Track.INTERCHANGE);
        actonInterchange2.setLength(500);
        // bias tracks
        actonInterchange2.setMoves(100);

        // create 2 cars
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("BA", "1", "Boxcar", "40", actonSpur1, 0);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("BB", "2", "Boxcar", "40", actonSpur1, 0);

//        Car c1 = cmanager.newCar("BA", "1");
//        c1.setTypeName("Boxcar");
//        c1.setLength("40");
//        Assert.assertEquals("Box Car 3 Length", "40", c1.getLength());
//
//        Car c2 = cmanager.newCar("BB", "2");
//        c2.setTypeName("Boxcar");
//        c2.setLength("40");
//        Assert.assertEquals("Box Car 4 Length", "40", c2.getLength());
//
//        Assert.assertEquals("place car at Acton", Track.OKAY, c1.setLocation(acton, actonSpur1));
//        Assert.assertEquals("place car at Acton", Track.OKAY, c2.setLocation(acton, actonSpur1));

        // create a local train servicing Acton
        Train actonTrain = tmanager.newTrain("Acton Local");
        Route routeA = rmanager.newRoute("A");
        routeA.addLocation(acton);
        actonTrain.setRoute(routeA);

        c1.setFinalDestination(acton);
        c1.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c1, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c1.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Spur 2", c1.getDestinationTrackName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());
        
        c2.setFinalDestination(acton);
        c2.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c2, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c2.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Spur 2", c2.getDestinationTrackName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());
        
        // now place the two cars in a kernel
        Kernel k1 = cmanager.newKernel("group of two cars");
        c1.setKernel(k1); // lead car
        c2.setKernel(k1);
        
        // try routing them again
        c1.setFinalDestination(acton);
        c1.setFinalDestinationTrack(actonInterchange1);

        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c1, null, null));
        Assert.assertEquals("Check car's destination", "Acton", c1.getDestinationName());
        Assert.assertEquals("Check car's destination track", actonInterchange1, c1.getDestinationTrack());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());
        
        // c2 should fail part of a kernel and not lead
        c2.setFinalDestination(acton);
        c2.setFinalDestinationTrack(actonInterchange1);
        Assert.assertFalse("Try routing final track with Acton Local", router.setDestination(c2, null, null));
        // confirm that car destination didn't change
        Assert.assertEquals("Check car's destination", "Acton", c2.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Spur 2", c2.getDestinationTrackName());  
    }

    private void loadLocationsTrainsAndCars() {
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        
        // create 7 locations and tracks, use 6
        JUnitOperationsUtil.createSevenNormalLocations();
        
        Location acton = lmanager.getLocationByName("Acton");      
        Location boston = lmanager.getLocationByName("Boston");
        Location chelmsford = lmanager.getLocationByName("Chelmsford");
        Location danvers = lmanager.newLocation("Danvers");
        Location essex = lmanager.getLocationByName("Essex");
        Location foxboro = lmanager.getLocationByName("Foxboro");

        // create a local train servicing Acton
        Train actonTrain = tmanager.newTrain("Acton Local");
        Route routeA = rmanager.newRoute("A");
        RouteLocation rlA = routeA.addLocation(acton);
        rlA.setMaxCarMoves(10);
        rlA.setTrainIconX(25);	// set train icon coordinates
        rlA.setTrainIconY(250);
        actonTrain.setRoute(routeA);

        // create a train with a route from Acton to Boston
        Train actonToBostonTrain = tmanager.newTrain("Acton to Boston");
        Route routeAB = rmanager.newRoute("AB");
        routeAB.addLocation(acton);
        RouteLocation rlBoston = routeAB.addLocation(boston);
        rlBoston.setTrainIconX(100);	// set train icon coordinates
        rlBoston.setTrainIconY(250);
        actonToBostonTrain.setRoute(routeAB);

        // create a train with a route from Boston to Chelmsford
        Train bostonToChelmsfordTrain = tmanager.newTrain("Boston to Chelmsford");
        Route routeBC = rmanager.newRoute("BC");
        routeBC.addLocation(boston);
        RouteLocation rlchelmsford = routeBC.addLocation(chelmsford);
        rlchelmsford.setTrainIconX(175);	// set train icon coordinates
        rlchelmsford.setTrainIconY(250);
        bostonToChelmsfordTrain.setRoute(routeBC);

        // create a train with a route from Chelmsford to Danvers
        Train chelmsfordToDanversTrain = tmanager.newTrain("Chelmsford to Danvers");
        Route routeCD = rmanager.newRoute("CD");
        routeCD.addLocation(chelmsford);
        RouteLocation rlDanvers = routeCD.addLocation(danvers);
        rlDanvers.setTrainIconX(250);	// set train icon coordinates
        rlDanvers.setTrainIconY(250);
        chelmsfordToDanversTrain.setRoute(routeCD);

        // create a train with a route from Danvers to Essex
        Train danversToEssexTrain = tmanager.newTrain("Danvers to Essex");
        Route routeDE = rmanager.newRoute("DE");
        RouteLocation rlDanvers2 = routeDE.addLocation(danvers);
        RouteLocation rlEssex = routeDE.addLocation(essex);
        // set the number of car moves to 8 for a later test
        rlDanvers2.setMaxCarMoves(8);
        rlEssex.setMaxCarMoves(8);
        rlEssex.setTrainIconX(25);	// set train icon coordinates
        rlEssex.setTrainIconY(275);
        danversToEssexTrain.setRoute(routeDE);

        // create a train with a route from Essex to Foxboro
        Train essexToFoxboroTrain = tmanager.newTrain("Essex to Foxboro");
        Route routeEF = rmanager.newRoute("EF");
        routeEF.addLocation(essex);
        RouteLocation rlFoxboro = routeEF.addLocation(foxboro);
        rlFoxboro.setTrainIconX(100);	// set train icon coordinates
        rlFoxboro.setTrainIconY(275);
        essexToFoxboroTrain.setRoute(routeEF);

        Train bostonToActonTrain = tmanager.newTrain("BostonToActonToBoston");
        Route bostonToActon = InstanceManager.getDefault(RouteManager.class).newRoute("BostonToActonToBoston");
        RouteLocation rlB2 = bostonToActon.addLocation(boston);
        RouteLocation rlA3 = bostonToActon.addLocation(acton);
        RouteLocation rlB3 = bostonToActon.addLocation(boston);
        // increase the number of moves so all cars are used
        rlB2.setMaxCarMoves(10);
        rlA3.setMaxCarMoves(10);
        rlB3.setMaxCarMoves(10);
        bostonToActonTrain.setRoute(bostonToActon);

        // add cars
        JUnitOperationsUtil.createAndPlaceCar("BA", "3", "Boxcar", "40", "DAB", "1984", null, 0);
        JUnitOperationsUtil.createAndPlaceCar("BB", "4", "Flat", "40", "AT", "1-86", null, 2);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("BC", "5", "Boxcar", "40", "AT", "2000", null, 3);
        c5.setLoadName("Boxes");

        Car c6 = JUnitOperationsUtil.createAndPlaceCar("BD", "6", "Boxcar", "40", "", "2000", null, 4);
        c6.setLoadName("Empty");
        Car c7 = JUnitOperationsUtil.createAndPlaceCar("BA", "7", "Boxcar", "4", "", "2000", null, 5);
        c7.setLoadName("Boxes");
        Car c8 = JUnitOperationsUtil.createAndPlaceCar("BB", "8", "Boxcar", "4", "", "2000", null, 6);
        c8.setLoadName("Empty");
        Car c9 = JUnitOperationsUtil.createAndPlaceCar("BC", "9", "Boxcar", "4", "", "2000", null, 7);
        c9.setLoadName("Empty");
    }

    // Ensure minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        super.setUp();
        
        // change report level to increase test coverage
        Setup.setRouterBuildReportLevel(Setup.BUILD_REPORT_VERY_DETAILED);
        
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        // disable build messages
        tmanager.setBuildMessagesEnabled(false);
        // disable build reports
        tmanager.setBuildReportEnabled(false);
        
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);

        // register the car and engine types used
        ct.addName("Boxcar");
        ct.addName(Bundle.getMessage("Caboose"));
        ct.addName("Flat");   
    }

    // The minimal setup for log4J
    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }
}
