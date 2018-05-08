//OperationsCarRouterTest.java
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
import org.junit.After;
import org.junit.Assert;
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
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);

        // register the car and engine types used
        ct.addName("Boxcar");
        ct.addName(Bundle.getMessage("Caboose"));
        ct.addName("Flat");

        // create 6 locations and tracks
        Location acton = lmanager.newLocation("Acton MA");
        Assert.assertEquals("Location 1 Name", "Acton MA", acton.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, acton.getLength());

        Track actonSiding1 = acton.addTrack("Acton Siding 1", Track.SPUR);
        actonSiding1.setLength(300);
        Assert.assertEquals("Location actonSpur1 Name", "Acton Siding 1", actonSiding1.getName());
        Assert.assertEquals("Location actonSpur1 Length", 300, actonSiding1.getLength());

        Track actonSiding2 = acton.addTrack("Acton Siding 2", Track.SPUR);
        actonSiding2.setLength(300);
        Assert.assertEquals("Location actonSpur2 Name", "Acton Siding 2", actonSiding2.getName());
        Assert.assertEquals("Location actonSpur2 Length", 300, actonSiding2.getLength());

        Track actonYard = acton.addTrack("Acton Yard", Track.YARD);
        actonYard.setLength(400);
        Assert.assertEquals("Location actonYard Name", "Acton Yard", actonYard.getName());
        Assert.assertEquals("Location actonYard Length", 400, actonYard.getLength());

        Track actonInterchange1 = acton.addTrack("Acton Interchange", Track.INTERCHANGE);
        actonInterchange1.setLength(500);
        Assert.assertEquals("Track actonInterchange Name", "Acton Interchange", actonInterchange1.getName());
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

        Location bedford = lmanager.newLocation("Bedford MA");
        Assert.assertEquals("Location 1 Name", "Bedford MA", bedford.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, bedford.getLength());

        Track bedfordSpur1 = bedford.addTrack("Bedford Siding 1", Track.SPUR);
        bedfordSpur1.setLength(300);
        Assert.assertEquals("Location bedfordSpur1 Name", "Bedford Siding 1", bedfordSpur1.getName());
        Assert.assertEquals("Location bedfordSpur1 Length", 300, bedfordSpur1.getLength());

        Track bedfordSpur2 = bedford.addTrack("Bedford Siding 2", Track.SPUR);
        bedfordSpur2.setLength(300);
        Assert.assertEquals("Location bedfordSpur2 Name", "Bedford Siding 2", bedfordSpur2.getName());
        Assert.assertEquals("Location bedfordSpur2 Length", 300, bedfordSpur2.getLength());

        Track bedfordYard = bedford.addTrack("Bedford Yard", Track.YARD);
        bedfordYard.setLength(400);
        Assert.assertEquals("Location bedfordYard Name", "Bedford Yard", bedfordYard.getName());
        Assert.assertEquals("Location bedfordYard Length", 400, bedfordYard.getLength());

        Track bedfordInterchange = bedford.addTrack("Bedford Interchange", Track.INTERCHANGE);
        bedfordInterchange.setLength(500);
        Assert.assertEquals("Track bedfordInterchange Name", "Bedford Interchange", bedfordInterchange.getName());
        Assert.assertEquals("Track bedfordInterchange Length", 500, bedfordInterchange.getLength());

        Location clinton = lmanager.newLocation("Clinton MA");
        Assert.assertEquals("Location 1 Name", "Clinton MA", clinton.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, clinton.getLength());

        Track clintonSpur1 = clinton.addTrack("Clinton Siding 1", Track.SPUR);
        clintonSpur1.setLength(300);
        Assert.assertEquals("Location clintonSpur1 Name", "Clinton Siding 1", clintonSpur1.getName());
        Assert.assertEquals("Location clintonSpur1 Length", 300, clintonSpur1.getLength());

        Track clintonSpur2 = clinton.addTrack("Clinton Siding 2", Track.SPUR);
        clintonSpur2.setLength(300);
        Assert.assertEquals("Location clintonSpur2 Name", "Clinton Siding 2", clintonSpur2.getName());
        Assert.assertEquals("Location clintonSpur2 Length", 300, bedfordSpur2.getLength());

        Track clintonYard = clinton.addTrack("Clinton Yard", Track.YARD);
        clintonYard.setLength(400);
        Assert.assertEquals("Location clintonYard Name", "Clinton Yard", clintonYard.getName());
        Assert.assertEquals("Location clintonYard Length", 400, clintonYard.getLength());

        Track clintonInterchange = clinton.addTrack("Clinton Interchange", Track.INTERCHANGE);
        clintonInterchange.setLength(500);
        Assert.assertEquals("Track clintonInterchange Name", "Clinton Interchange", clintonInterchange.getName());
        Assert.assertEquals("Track clintonInterchange Length", 500, clintonInterchange.getLength());

        Location danbury = lmanager.newLocation("essex MA");
        Track danburySpur1 = danbury.addTrack("Danbury Siding 1", Track.SPUR);
        danburySpur1.setLength(300);
        Track danburySpur2 = danbury.addTrack("Danbury Siding 2", Track.SPUR);
        danburySpur2.setLength(300);
        Track danburyYard = danbury.addTrack("Danbury Yard", Track.YARD);
        danburyYard.setLength(400);
        Track danburyInterchange = danbury.addTrack("Danbury Interchange", Track.INTERCHANGE);
        danburyInterchange.setLength(500);

        Location essex = lmanager.newLocation("Essex MA");
        Track essexSpur1 = essex.addTrack("Essex Siding 1", Track.SPUR);
        essexSpur1.setLength(300);
        Track essexSpur2 = essex.addTrack("Essex Siding 2", Track.SPUR);
        essexSpur2.setLength(300);
        Track essexYard = essex.addTrack("Essex Yard", Track.YARD);
        essexYard.setLength(400);
        Track essexInterchange = essex.addTrack("Essex Interchange", Track.INTERCHANGE);
        essexInterchange.setLength(500);

        Location foxboro = lmanager.newLocation("Foxboro MA");
        Track foxboroSpur1 = foxboro.addTrack("Foxboro Siding 1", Track.SPUR);
        foxboroSpur1.setLength(300);
        Track foxboroSpur2 = foxboro.addTrack("Foxboro Siding 2", Track.SPUR);
        foxboroSpur2.setLength(300);
        Track foxboroYard = foxboro.addTrack("Foxboro Yard", Track.YARD);
        foxboroYard.setLength(400);
        Track foxboroInterchange = foxboro.addTrack("Foxboro Interchange", Track.INTERCHANGE);
        foxboroInterchange.setLength(500);

        // create 2 cars
        Car c3 = cmanager.newCar("BA", "3");
        c3.setTypeName("Boxcar");
        c3.setLength("40");
        c3.setOwner("DAB");
        c3.setBuilt("1984");
        Assert.assertEquals("Box Car 3 Length", "40", c3.getLength());

        Car c4 = cmanager.newCar("BB", "4");
        c4.setTypeName("Flat");
        c4.setLength("40");
        c4.setOwner("AT");
        c4.setBuilt("1-86");
        Assert.assertEquals("Box Car 4 Length", "40", c4.getLength());

        Assert.assertEquals("place car at bedfordInterchange", Track.OKAY, c3.setLocation(acton, actonSiding1));
        Assert.assertFalse("Try routing no final destination", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        Assert.assertEquals("place car at Acton", Track.OKAY, c4.setLocation(acton, actonSiding1));
        Assert.assertFalse("Try routing no final destination", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "", c4.getDestinationName());

        // test disable routing
        Setup.setCarRoutingEnabled(false);
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Test router disabled", router.setDestination(c3, null, null));
        Assert.assertEquals("Router status", Router.STATUS_ROUTER_DISABLED, router.getStatus());
        Setup.setCarRoutingEnabled(true);

        // first try car routing with just one location
        c3.setFinalDestination(acton);
        Assert.assertFalse("Try routing final destination equal to current", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_CAR_AT_DESINATION, router.getStatus());

        // now try with next track not equal to current
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding2);
        Assert.assertFalse("Try routing final track not equal to current", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // now try with next track equal to current
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding1);
        Assert.assertFalse("Try routing final track equal to current", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_CAR_AT_DESINATION, router.getStatus());

        // create a local train servicing Acton
        Train actonTrain = tmanager.newTrain("Acton Local");
        Route routeA = rmanager.newRoute("A");
        RouteLocation rlActon1 = routeA.addLocation(acton);
        rlActon1.setTrainIconX(25);	// set train icon coordinates
        rlActon1.setTrainIconY(250);
        actonTrain.setRoute(routeA);

        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding2);
        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Siding 2", c3.getDestinationTrackName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // specify the Acton train
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding2);
        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c3, actonTrain, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Siding 2", c3.getDestinationTrackName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // Set the track length to be less the length of c3
        actonSiding2.setLength(c3.getTotalLength() - 1);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);	// local move, alternate or yard track option should be ignored
        c3.setFinalDestinationTrack(actonSiding2);
        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c3, actonTrain, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());
        Assert.assertTrue("Should report that the issue was track length", router.getStatus().startsWith(Track.CAPACITY));

        // restore track length
        actonSiding2.setLength(300);

        // don't allow train to service boxcars
        actonTrain.deleteTypeName("Boxcar");
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding2);
        Assert.assertFalse("Try routing with train that doesn't service Boxcar", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car type Flat
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(acton);
        c4.setFinalDestinationTrack(actonSiding2);
        Assert.assertTrue("Try routing with train that service Flat", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c4.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // now allow Boxcar again
        actonTrain.addTypeName("Boxcar");
        Assert.assertTrue("Try routing with train that does service Boxcar", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // don't allow train to service boxcars with road name BA
        actonTrain.addRoadName("BA");
        actonTrain.setRoadOption(Train.EXCLUDE_ROADS);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding2);
        Assert.assertFalse("Try routing with train that doesn't service road name BA", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car road name BB
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(acton);
        c4.setFinalDestinationTrack(actonSiding2);
        Assert.assertTrue("Try routing with train that services road BB", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c4.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // now try again but allow road name
        actonTrain.setRoadOption(Train.ALL_ROADS);
        Assert.assertTrue("Try routing with train that does service road name BA", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // don't service cars built before 1985
        actonTrain.setBuiltStartYear("1985");
        actonTrain.setBuiltEndYear("2010");
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding2);
        Assert.assertFalse("Try routing with train that doesn't service car built before 1985", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car built after 1985
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(acton);
        c4.setFinalDestinationTrack(actonSiding2);
        Assert.assertTrue("Try routing with train that services car built after 1985", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c4.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // car was built in 1984 should work
        actonTrain.setBuiltStartYear("1983");
        Assert.assertTrue("Try routing with train that doesn't service car built before 1983", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // try car loads
        c3.setLoadName("Tools");
        actonTrain.addLoadName("Tools");
        actonTrain.setLoadOption(Train.EXCLUDE_LOADS);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding2);
        Assert.assertFalse("Try routing with train that doesn't service load Tools", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car load "E"
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(acton);
        c4.setFinalDestinationTrack(actonSiding2);
        Assert.assertTrue("Try routing with train that services load E", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c4.getDestinationName());

        actonTrain.setLoadOption(Train.ALL_LOADS);
        Assert.assertTrue("Try routing with train that that does service load Tools", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

        // now test by modifying the route
        rlActon1.setPickUpAllowed(false);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding2);
        Assert.assertFalse("Try routing with train that doesn't pickup cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        rlActon1.setPickUpAllowed(true);
        Assert.assertTrue("Try routing with train that that can pickup cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

        rlActon1.setDropAllowed(false);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding2);
        Assert.assertFalse("Try routing with train that doesn't drop cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        rlActon1.setDropAllowed(true);
        Assert.assertTrue("Try routing with train that that can drop cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

        rlActon1.setMaxCarMoves(0);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding2);
        Assert.assertFalse("Try routing with train that doesn't service location", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        rlActon1.setMaxCarMoves(10);
        Assert.assertTrue("Try routing with train that does service location", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

        // test train depart direction
        Assert.assertEquals("check default direction", Track.NORTH, rlActon1.getTrainDirection());
        // set the depart location Acton to service by South bound trains only
        acton.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding2);
        Assert.assertTrue("Try routing with local train that departs north, location south", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

        acton.setTrainDirections(Track.NORTH);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding2);
        Assert.assertTrue("Try routing with local train that departs north, location north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

        // set the depart track Acton to service by local train only
        actonSiding1.setTrainDirections(0);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding2);
        Assert.assertTrue("Try routing with local only", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding2);
        actonSiding1.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with local train that departs north, track north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

        // test arrival directions
        // set the arrival track Acton to service by local trains only
        actonSiding2.setTrainDirections(0);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding2);	// now specify the actual track
        Assert.assertTrue("Try routing with local train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

        actonSiding2.setTrainDirections(Track.NORTH);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding2);	// now specify the actual track
        Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

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
        c3.setFinalDestinationTrack(actonSiding2);
        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Siding 2", c3.getDestinationTrackName());

        // don't allow Acton Local 2 to service boxcars
        ActonTrain2.deleteTypeName("Boxcar");
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSiding2);
        // Should be able to route using Acton Local, but destination should not be set
        Assert.assertTrue("Try routing with train that doesn't service Boxcar", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_THIS_TRAIN, router.getStatus());

        // Two locations one train testing begins
        // set next destination Bedford
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        c3.setFinalDestinationTrack(null);
        // should fail no train!
        Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));
        // create a train with a route from Acton to Bedford
        Train ActonToBedfordTrain = tmanager.newTrain("Acton to Bedford");
        Route routeAB = rmanager.newRoute("AB");
        RouteLocation rlActon = routeAB.addLocation(acton);
        RouteLocation rlBedford = routeAB.addLocation(bedford);
        rlBedford.setTrainIconX(100);	// set train icon coordinates
        rlBedford.setTrainIconY(250);
        ActonToBedfordTrain.setRoute(routeAB);

        // should work
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // try specific train
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        c3.setFinalDestinationTrack(null);
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, ActonToBedfordTrain, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // don't allow train to service boxcars
        ActonToBedfordTrain.deleteTypeName("Boxcar");
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that doesn't service Boxcar", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car type Flat
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(bedford);
        c4.setFinalDestinationTrack(null);
        Assert.assertTrue("Try routing with train that service Flat", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c4.getDestinationName());

        // now allow Boxcar again
        ActonToBedfordTrain.addTypeName("Boxcar");
        Assert.assertTrue("Try routing with train that does service Boxcar", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // don't allow train to service boxcars with road name BA
        ActonToBedfordTrain.addRoadName("BA");
        ActonToBedfordTrain.setRoadOption(Train.EXCLUDE_ROADS);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that doesn't service road name BA", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car road name BB
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(bedford);
        Assert.assertTrue("Try routing with train that services road BB", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c4.getDestinationName());

        // now try again but allow road name
        ActonToBedfordTrain.setRoadOption(Train.ALL_ROADS);
        Assert.assertTrue("Try routing with train that does service road name BA", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // don't service cars built before 1985
        ActonToBedfordTrain.setBuiltStartYear("1985");
        ActonToBedfordTrain.setBuiltEndYear("2010");
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that doesn't service car built before 1985", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        // try the car built after 1985
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(bedford);
        Assert.assertTrue("Try routing with train that services car built after 1985", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c4.getDestinationName());

        // car was built in 1984 should work
        ActonToBedfordTrain.setBuiltStartYear("1983");
        Assert.assertTrue("Try routing with train that doesn't service car built before 1983", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // try car loads
        c3.setLoadName("Tools");
        ActonToBedfordTrain.addLoadName("Tools");
        ActonToBedfordTrain.setLoadOption(Train.EXCLUDE_LOADS);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that doesn't service load Tools", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        // try the car load "E"
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(bedford);
        Assert.assertTrue("Try routing with train that services load E", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c4.getDestinationName());

        ActonToBedfordTrain.setLoadOption(Train.ALL_LOADS);
        Assert.assertTrue("Try routing with train that that does service load Tools", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // don't allow Bedford to service Flat
        c4.setDestination(null, null);	// clear previous destination
        c4.setFinalDestination(bedford);
        bedford.deleteTypeName("Flat");
        Assert.assertFalse("Try routing with Bedford that does not service Flat", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "", c4.getDestinationName());
        Assert.assertTrue("Router status", router.getStatus().startsWith(Track.TYPE));

        // restore Bedford can service Flat
        bedford.addTypeName("Flat");

        // now test by modifying the route
        rlActon.setPickUpAllowed(false);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that doesn't pickup cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        rlActon.setPickUpAllowed(true);
        Assert.assertTrue("Try routing with train that that can pickup cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        rlBedford.setDropAllowed(false);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that doesn't drop cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        rlBedford.setDropAllowed(true);
        Assert.assertTrue("Try routing with train that that can drop cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        rlBedford.setMaxCarMoves(0);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that doesn't service destination", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        rlBedford.setMaxCarMoves(5);
        Assert.assertTrue("Try routing with train that does service destination", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        rlActon.setMaxCarMoves(0);
        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that doesn't service location", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        rlActon.setMaxCarMoves(5);
        Assert.assertTrue("Try routing with train that does service location", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // test train depart direction
        Assert.assertEquals("check default direction", Track.NORTH, rlActon.getTrainDirection());
        // set the depart location Acton to service by South bound trains only
        acton.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);

        // remove the Action local by not allowing train to service boxcars
        actonTrain.deleteTypeName("Boxcar");
        Assert.assertFalse("Try routing with train that departs north, location south", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        acton.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with train that departs north, location north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // set the depart track Acton to service by South bound trains only
        actonSiding1.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that departs north, track south", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        actonSiding1.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // test arrival directions
        // set the arrival location Bedford to service by South bound trains only
        bedford.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that arrives north, destination south", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        bedford.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with train that arrives north, destination north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // set the depart track Acton to service by South bound trains only
        bedfordSpur1.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertTrue("Try routing with train that arrives north, but no final track", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford); // the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);	// now specify the actual track
        Assert.assertFalse("Try routing with train that arrives north, now with track", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        bedfordSpur1.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        Setup.setOnlyActiveTrainsEnabled(true);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertTrue("Try routing only active trains", router.setDestination(c3, null, null));

        // now deselect the Action to Bedford train
        ActonToBedfordTrain.setBuildEnabled(false);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing only active trains, Action to Beford deselected", router.setDestination(c3, null, null));
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        Setup.setOnlyActiveTrainsEnabled(false);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertTrue("Try routing, only active trains deselected", router.setDestination(c3, null, null));

        // test yard and alternate track options
        bedfordSpur1.setLength(c3.getTotalLength());
        // c4 is the same length as c3, so the track is now full
        Assert.assertEquals("Use up all of the space for bedfordSpur1", Track.OKAY, c4.setLocation(bedford, bedfordSpur1));
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        c3.setFinalDestinationTrack(bedfordSpur1);
        Assert.assertTrue("Test search for yard", router.setDestination(c3, null, null));
        Assert.assertEquals("Destination", "Bedford MA", c3.getDestinationName());
        Assert.assertEquals("Destination track should be yard", "Bedford Yard", c3.getDestinationTrackName());
        // the car was sent to a yard track because the spur was full
        Assert.assertTrue("Should be reporting length issue", router.getStatus().startsWith(Track.LENGTH));

        // remove yard type
        bedfordYard.setTrackType(Track.SPUR);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        c3.setFinalDestinationTrack(bedfordSpur1);
        Assert.assertTrue("Test search for yard that doesn't exist", router.setDestination(c3, null, null));
        Assert.assertEquals("Destination", "", c3.getDestinationName());
        Assert.assertTrue("Should be reporting length issue", router.getStatus().startsWith(Track.LENGTH));

        // restore yard type
        bedfordYard.setTrackType(Track.YARD);

        // test alternate track option
        bedfordSpur1.setAlternateTrack(bedfordSpur2);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);
        c3.setFinalDestinationTrack(bedfordSpur1);
        Assert.assertTrue("Test use alternate", router.setDestination(c3, null, null));
        Assert.assertEquals("Destination", "Bedford MA", c3.getDestinationName());
        Assert.assertEquals("Destination track should be siding", "Bedford Siding 2", c3.getDestinationTrackName());
        // the car was sent to the alternate track because the spur was full
        Assert.assertTrue("Should be reporting length issue", router.getStatus().startsWith(Track.LENGTH));

        // restore track length and remove alternate
        bedfordSpur1.setLength(300);
        bedfordSpur1.setAlternateTrack(null);

        // One train tests complete. Start two train testing.
        // Force first move to be by local train
        actonSiding1.setTrainDirections(0);
        actonTrain.addTypeName("Boxcar");	// restore the local

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);	// the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);	// now specify the actual track

        Assert.assertTrue("Try routing two trains via interchange", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());

        // don't allow use of interchange track
        actonInterchange1.setDropOption(Track.TRAINS);

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);	// the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);	// now specify the actual track

        Assert.assertTrue("Try routing two trains via interchange", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 2", c3.getDestinationTrackName());

        actonInterchange2.setDropOption(Track.TRAINS);

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);	// the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);	// now specify the actual track

        Assert.assertTrue("Try routing two trains via yard", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Yard", c3.getDestinationTrackName());

        // allow use of interchange track
        actonInterchange1.setDropOption(Track.ANY);
        actonInterchange2.setDropOption(Track.ANY);

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);	// the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);

        // allow Boxcars
        ActonTrain2.addTypeName("Boxcar");

        Assert.assertTrue("Try routing two trains", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());

        // test to see if second interchange track used if first is full
        actonInterchange1.setLength(c3.getTotalLength() - 1);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);	// the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);
        Assert.assertTrue("Try routing two trains to interchange track 2", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 2", c3.getDestinationTrackName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // use yard track if interchange tracks are full
        actonInterchange2.setLength(c3.getTotalLength() - 1);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);	// the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);
        Assert.assertTrue("Try routing two trains to yard track", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Yard", c3.getDestinationTrackName());

        // disable using yard tracks for routing
        Setup.setCarRoutingViaYardsEnabled(false);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(bedford);	// the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);
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
        c3.setFinalDestination(bedford);	// the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);

        // routing should work using train 1, destination and track should not be set
        Assert.assertTrue("Try routing two trains via yard", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());

        // two train testing done!
        // now try up to 5 trains to route car
        // set final destination Clinton
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(clinton);
        c3.setFinalDestinationTrack(null);
        // should fail no train!
        Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));

        // create a train with a route from Bedford to Clinton
        Train bedfordToClintonTrain = tmanager.newTrain("Bedford to Clinton");
        Route routeBC = rmanager.newRoute("BC");
        routeBC.addLocation(bedford);
        RouteLocation rlchelmsford = routeBC.addLocation(clinton);
        rlchelmsford.setTrainIconX(175);	// set train icon coordinates
        rlchelmsford.setTrainIconY(250);
        bedfordToClintonTrain.setRoute(routeBC);

        // should work
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());

        // allow train 2 to service boxcars with road name BA
        ActonTrain2.setRoadOption(Train.ALL_ROADS);

        // try with train 2
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(clinton);

        // routing should work using train 2
        Assert.assertTrue("Try routing three trains", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(clinton);

        // test to see if second interchange track used if first is full
        actonInterchange1.setLength(c3.getTotalLength() - 1);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(clinton);	// the final destination for the car
        Assert.assertTrue("Try routing three trains to interchange track 2", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 2", c3.getDestinationTrackName());

        // use yard track if interchange tracks are full
        actonInterchange2.setLength(c3.getTotalLength() - 1);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(clinton);	// the final destination for the car
        Assert.assertTrue("Try routing three trains to yard track", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Yard", c3.getDestinationTrackName());

        // disable the use of yard tracks for routing
        Setup.setCarRoutingViaYardsEnabled(false);
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(clinton);	// the final destination for the car
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
        c3.setFinalDestination(clinton);	// the final destination for the car
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

        // set final destination Danbury
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(danbury);
        // should fail no train!
        Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));

        // create a train with a route from Clinton to Danbury
        Train clintonToDanburyTrain = tmanager.newTrain("Clinton to Danbury");
        Route routeCD = rmanager.newRoute("CD");
        routeCD.addLocation(clinton);
        RouteLocation rlDanbury = routeCD.addLocation(danbury);
        rlDanbury.setTrainIconX(250);	// set train icon coordinates
        rlDanbury.setTrainIconY(250);
        clintonToDanburyTrain.setRoute(routeCD);

        // should work
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(danbury);	// the final destination for the car

        // routing should work using train 2
        Assert.assertTrue("Try routing four trains", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());

        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(danbury);	// the final destination for the car

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

        // create a train with a route from Danbury to Essex
        Train danburyToEssexTrain = tmanager.newTrain("Danbury to Essex");
        Route routeDE = rmanager.newRoute("DE");
        RouteLocation rlDanbury2 = routeDE.addLocation(danbury);
        RouteLocation rlEssex = routeDE.addLocation(essex);
        // set the number of car moves to 8 for a later test
        rlDanbury2.setMaxCarMoves(8);
        rlEssex.setMaxCarMoves(8);
        rlEssex.setTrainIconX(25);	// set train icon coordinates
        rlEssex.setTrainIconY(275);
        danburyToEssexTrain.setRoute(routeDE);

        // should work
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());

        // routing should work using train 2
        c3.setDestination(null, null);	// clear previous destination
        c3.setFinalDestination(essex);
        Assert.assertTrue("Try routing five trains", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());

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
        actonSiding1.setTrainDirections(Track.NORTH);

        // now should work!
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Bedford Interchange", c3.getDestinationTrackName());

        //TODO test restrict location by car type
        //TODO test restrict tracks by type road, load
    }

    /**
     * Test routing using one train and the train is a local. a local services
     * only one location
     */
    public void testCarRoutingA() {
        // load up the managers
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Router router = InstanceManager.getDefault(Router.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);

        // register the car and engine types used
        ct.addName("Boxcar");
        ct.addName(Bundle.getMessage("Caboose"));
        ct.addName("Flat");

        // create 2 locations and tracks
        Location acton = lmanager.newLocation("Acton MA");
        Assert.assertEquals("Location 1 Name", "Acton MA", acton.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, acton.getLength());

        Track actonSpur1 = acton.addTrack("Acton Siding 1", Track.SPUR);
        actonSpur1.setLength(300);
        Assert.assertEquals("Location actonSpur1 Name", "Acton Siding 1", actonSpur1.getName());
        Assert.assertEquals("Location actonSpur1 Length", 300, actonSpur1.getLength());

        Track actonSpur2 = acton.addTrack("Acton Siding 2", Track.SPUR);
        actonSpur2.setLength(300);
        Assert.assertEquals("Location actonSpur2 Name", "Acton Siding 2", actonSpur2.getName());
        Assert.assertEquals("Location actonSpur2 Length", 300, actonSpur2.getLength());

        Track actonYard = acton.addTrack("Acton Yard", Track.YARD);
        actonYard.setLength(400);
        Assert.assertEquals("Location actonYard Name", "Acton Yard", actonYard.getName());
        Assert.assertEquals("Location actonYard Length", 400, actonYard.getLength());
        
        // make the yard the alternate
        actonSpur1.setAlternateTrack(actonYard);

        Track actonInterchange1 = acton.addTrack("Acton Interchange", Track.INTERCHANGE);
        actonInterchange1.setLength(500);
        Assert.assertEquals("Track AI Name", "Acton Interchange", actonInterchange1.getName());
        Assert.assertEquals("Track AI Length", 500, actonInterchange1.getLength());
        Assert.assertEquals("Track AI Train Directions", DIRECTION_ALL, actonInterchange1.getTrainDirections());

        // add a second interchange track
        Track actonInterchange2 = acton.addTrack("Acton Interchange 2", Track.INTERCHANGE);
        actonInterchange2.setLength(500);
        // bias tracks
        actonInterchange2.setMoves(100);
        Assert.assertEquals("Track actonInterchange2 Name", "Acton Interchange 2", actonInterchange2.getName());
        Assert.assertEquals("Track actonInterchange2 Length", 500, actonInterchange2.getLength());
        Assert.assertEquals("Track actonInterchange2 Train Directions", DIRECTION_ALL, actonInterchange2.getTrainDirections());

        Location bedford = lmanager.newLocation("Bedford MA");
        Assert.assertEquals("Location 1 Name", "Bedford MA", bedford.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, bedford.getLength());

        Track bedfordSpur1 = bedford.addTrack("Bedford Siding 1", Track.SPUR);
        bedfordSpur1.setLength(300);
        Assert.assertEquals("Location bedfordSpur1 Name", "Bedford Siding 1", bedfordSpur1.getName());
        Assert.assertEquals("Location bedfordSpur1 Length", 300, bedfordSpur1.getLength());

        Track bedfordSpur2 = bedford.addTrack("Bedford Siding 2", Track.SPUR);
        bedfordSpur2.setLength(300);
        Assert.assertEquals("Location bedfordSpur2 Name", "Bedford Siding 2", bedfordSpur2.getName());
        Assert.assertEquals("Location bedfordSpur2 Length", 300, bedfordSpur2.getLength());

        Track bedfordYard = bedford.addTrack("Bedford Yard", Track.YARD);
        bedfordYard.setLength(400);
        Assert.assertEquals("Location bedfordYard Name", "Bedford Yard", bedfordYard.getName());
        Assert.assertEquals("Location bedfordYard Length", 400, bedfordYard.getLength());

        Track bedfordInterchange = bedford.addTrack("Bedford Interchange", Track.INTERCHANGE);
        bedfordInterchange.setLength(500);
        Assert.assertEquals("Track bedfordInterchange Name", "Bedford Interchange", bedfordInterchange.getName());
        Assert.assertEquals("Track bedfordInterchange Length", 500, bedfordInterchange.getLength());

        // create 2 cars
        Car c3 = cmanager.newCar("BA", "3");
        c3.setTypeName("Boxcar");
        c3.setLength("40");
        c3.setOwner("DAB");
        c3.setBuilt("1984");
        Assert.assertEquals("Box Car 3 Length", "40", c3.getLength());

        Car c4 = cmanager.newCar("BB", "4");
        c4.setTypeName("Flat");
        c4.setLength("40");
        c4.setOwner("AT");
        c4.setBuilt("1-86");
        Assert.assertEquals("Box Car 4 Length", "40", c4.getLength());

        Assert.assertEquals("place car at Acton", Track.OKAY, c3.setLocation(acton, actonSpur1));
        Assert.assertFalse("Try routing no final destination", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        Assert.assertEquals("place car at Acton", Track.OKAY, c4.setLocation(acton, actonSpur1));
        Assert.assertFalse("Try routing no final destination", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "", c4.getDestinationName());

        // test disable routing
        Setup.setCarRoutingEnabled(false);
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Test router disabled", router.setDestination(c3, null, null));
        Assert.assertEquals("Router status", Router.STATUS_ROUTER_DISABLED, router.getStatus());
        Setup.setCarRoutingEnabled(true);

        // first try car routing with just one location
        c3.setFinalDestination(acton);
        Assert.assertFalse("Try routing final destination equal to current", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_CAR_AT_DESINATION, router.getStatus());

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
        Assert.assertEquals("Router status", Router.STATUS_CAR_AT_DESINATION, router.getStatus());

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
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Siding 2", c3.getDestinationTrackName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // specify the Acton train
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c3, actonTrain, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Siding 2", c3.getDestinationTrackName());
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
        Assert.assertEquals("Check car's destination", "Acton MA", c4.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // now allow Boxcar again
        actonTrain.addTypeName("Boxcar");
        Assert.assertTrue("Try routing with train that does service Boxcar", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
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
        Assert.assertEquals("Check car's destination", "Acton MA", c4.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // now try again but allow road name
        actonTrain.setRoadOption(Train.ALL_ROADS);
        Assert.assertTrue("Try routing with train that does service road name BA", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
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
        Assert.assertEquals("Check car's destination", "Acton MA", c4.getDestinationName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // car was built in 1984 should work
        actonTrain.setBuiltStartYear("1983");
        Assert.assertTrue("Try routing with train that doesn't service car built before 1983", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
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
        Assert.assertEquals("Check car's destination", "Acton MA", c4.getDestinationName());

        actonTrain.setLoadOption(Train.ALL_LOADS);
        Assert.assertTrue("Try routing with train that that does service load Tools", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

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
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

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
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

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
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

        // test train depart direction
        Assert.assertEquals("check default direction", Track.NORTH, rlA.getTrainDirection());
        // set the depart location Acton to service by South bound trains only
        acton.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing with local train that departs north, location south", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

        acton.setTrainDirections(Track.NORTH);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing with local train that departs north, location north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

        // set the depart track Acton to service by local train only
        actonSpur1.setTrainDirections(0);

        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        Assert.assertTrue("Try routing with local only", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);
        actonSpur1.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with local train that departs north, track north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

        // test arrival directions
        // set the arrival track Acton to service by local trains only
        actonSpur2.setTrainDirections(0);

        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);  // now specify the actual track
        Assert.assertTrue("Try routing with local train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

        actonSpur2.setTrainDirections(Track.NORTH);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(acton);
        c3.setFinalDestinationTrack(actonSpur2);  // now specify the actual track
        Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());

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
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Siding 2", c3.getDestinationTrackName());

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
    public void testCarRoutingB() {
        // now load up the managers
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Router router = InstanceManager.getDefault(Router.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);

        // register the car and engine types used
        ct.addName("Boxcar");
        ct.addName(Bundle.getMessage("Caboose"));
        ct.addName("Flat");

        // create 2 locations and tracks
        Location acton = lmanager.newLocation("Acton MA");
        Assert.assertEquals("Location 1 Name", "Acton MA", acton.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, acton.getLength());

        Track actonSiding1 = acton.addTrack("Acton Siding 1", Track.SPUR);
        actonSiding1.setLength(300);
        Assert.assertEquals("Location actonSpur1 Name", "Acton Siding 1", actonSiding1.getName());
        Assert.assertEquals("Location actonSpur1 Length", 300, actonSiding1.getLength());

        Track actonSiding2 = acton.addTrack("Acton Siding 2", Track.SPUR);
        actonSiding2.setLength(300);
        Assert.assertEquals("Location actonSpur2 Name", "Acton Siding 2", actonSiding2.getName());
        Assert.assertEquals("Location actonSpur2 Length", 300, actonSiding2.getLength());

        Track actonYard = acton.addTrack("Acton Yard", Track.YARD);
        actonYard.setLength(400);
        Assert.assertEquals("Location actonYard Name", "Acton Yard", actonYard.getName());
        Assert.assertEquals("Location actonYard Length", 400, actonYard.getLength());

        Track actonInterchange1 = acton.addTrack("Acton Interchange", Track.INTERCHANGE);
        actonInterchange1.setLength(500);
        Assert.assertEquals("Track AI Name", "Acton Interchange", actonInterchange1.getName());
        Assert.assertEquals("Track AI Length", 500, actonInterchange1.getLength());
        Assert.assertEquals("Track AI Train Directions", DIRECTION_ALL, actonInterchange1.getTrainDirections());

        // add a second interchange track
        Track actonInterchange2 = acton.addTrack("Acton Interchange 2", Track.INTERCHANGE);
        actonInterchange2.setLength(500);
        // bias tracks
        actonInterchange2.setMoves(100);
        Assert.assertEquals("Track actonInterchange2 Name", "Acton Interchange 2", actonInterchange2.getName());
        Assert.assertEquals("Track actonInterchange2 Length", 500, actonInterchange2.getLength());
        Assert.assertEquals("Track actonInterchange2 Train Directions", DIRECTION_ALL, actonInterchange2.getTrainDirections());

        Location bedford = lmanager.newLocation("Bedford MA");
        Assert.assertEquals("Location 1 Name", "Bedford MA", bedford.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, bedford.getLength());

        Track bedfordSpur1 = bedford.addTrack("Bedford Siding 1", Track.SPUR);
        bedfordSpur1.setLength(300);
        Assert.assertEquals("Location bedfordSpur1 Name", "Bedford Siding 1", bedfordSpur1.getName());
        Assert.assertEquals("Location bedfordSpur1 Length", 300, bedfordSpur1.getLength());

        Track bedfordSpur2 = bedford.addTrack("Bedford Siding 2", Track.SPUR);
        bedfordSpur2.setLength(300);
        Assert.assertEquals("Location bedfordSpur2 Name", "Bedford Siding 2", bedfordSpur2.getName());
        Assert.assertEquals("Location bedfordSpur2 Length", 300, bedfordSpur2.getLength());

        Track bedfordYard = bedford.addTrack("Bedford Yard", Track.YARD);
        bedfordYard.setLength(400);
        Assert.assertEquals("Location bedfordYard Name", "Bedford Yard", bedfordYard.getName());
        Assert.assertEquals("Location bedfordYard Length", 400, bedfordYard.getLength());

        Track bedfordInterchange = bedford.addTrack("Bedford Interchange", Track.INTERCHANGE);
        bedfordInterchange.setLength(500);
        Assert.assertEquals("Track bedfordInterchange Name", "Bedford Interchange", bedfordInterchange.getName());
        Assert.assertEquals("Track bedfordInterchange Length", 500, bedfordInterchange.getLength());

        // create 2 cars
        Car c3 = cmanager.newCar("BA", "3");
        c3.setTypeName("Boxcar");
        c3.setLength("40");
        c3.setOwner("DAB");
        c3.setBuilt("1984");
        Assert.assertEquals("Box Car 3 Length", "40", c3.getLength());

        Car c4 = cmanager.newCar("BB", "4");
        c4.setTypeName("Flat");
        c4.setLength("40");
        c4.setOwner("AT");
        c4.setBuilt("1-86");
        Assert.assertEquals("Box Car 4 Length", "40", c4.getLength());

        Assert.assertEquals("place car at bedfordInterchange", Track.OKAY, c3.setLocation(acton, actonSiding1));
        Assert.assertFalse("Try routing no final destination", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        Assert.assertEquals("place car at Acton", Track.OKAY, c4.setLocation(acton, actonSiding1));
        Assert.assertFalse("Try routing no final destination", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "", c4.getDestinationName());


        // create a local train servicing Acton
        Train actonTrain = tmanager.newTrain("Acton Local");
        Route routeA = rmanager.newRoute("A");
        RouteLocation rlActon1 = routeA.addLocation(acton);
        rlActon1.setTrainIconX(25); // set train icon coordinates
        rlActon1.setTrainIconY(250);
        actonTrain.setRoute(routeA);

        // add a second local train
        // create a local train servicing Acton
        Train ActonTrain2 = tmanager.newTrain("Acton Local 2");
        Route routeA2 = rmanager.newRoute("A2");
        RouteLocation rlA2 = routeA2.addLocation(acton);
        rlA2.setTrainIconX(25); // set train icon coordinates
        rlA2.setTrainIconY(250);
        ActonTrain2.setRoute(routeA2);

        // don't allow Acton Local 2 to service boxcars
        ActonTrain2.deleteTypeName("Boxcar");

        // Two locations one train testing begins
        // set next destination Bedford
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        c3.setFinalDestinationTrack(null);
        // should fail no train!
        Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));
        // create a train with a route from Acton to Bedford
        Train ActonToBedfordTrain = tmanager.newTrain("Acton to Bedford");
        Route routeAB = rmanager.newRoute("AB");
        RouteLocation rlActon = routeAB.addLocation(acton);
        RouteLocation rlBedford = routeAB.addLocation(bedford);
        rlBedford.setTrainIconX(100);   // set train icon coordinates
        rlBedford.setTrainIconY(250);
        ActonToBedfordTrain.setRoute(routeAB);

        // should work
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // try specific train
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        c3.setFinalDestinationTrack(null);
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, ActonToBedfordTrain, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // don't allow train to service boxcars
        ActonToBedfordTrain.deleteTypeName("Boxcar");
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that doesn't service Boxcar", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car type Flat
        c4.setDestination(null, null);  // clear previous destination
        c4.setFinalDestination(bedford);
        c4.setFinalDestinationTrack(null);
        Assert.assertTrue("Try routing with train that service Flat", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c4.getDestinationName());

        // now allow Boxcar again
        ActonToBedfordTrain.addTypeName("Boxcar");
        Assert.assertTrue("Try routing with train that does service Boxcar", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // don't allow train to service boxcars with road name BA
        ActonToBedfordTrain.addRoadName("BA");
        ActonToBedfordTrain.setRoadOption(Train.EXCLUDE_ROADS);
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that doesn't service road name BA", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        // try the car road name BB
        c4.setDestination(null, null);  // clear previous destination
        c4.setFinalDestination(bedford);
        Assert.assertTrue("Try routing with train that services road BB", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c4.getDestinationName());

        // now try again but allow road name
        ActonToBedfordTrain.setRoadOption(Train.ALL_ROADS);
        Assert.assertTrue("Try routing with train that does service road name BA", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // don't service cars built before 1985
        ActonToBedfordTrain.setBuiltStartYear("1985");
        ActonToBedfordTrain.setBuiltEndYear("2010");
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that doesn't service car built before 1985", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        // try the car built after 1985
        c4.setDestination(null, null);  // clear previous destination
        c4.setFinalDestination(bedford);
        Assert.assertTrue("Try routing with train that services car built after 1985", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c4.getDestinationName());

        // car was built in 1984 should work
        ActonToBedfordTrain.setBuiltStartYear("1983");
        Assert.assertTrue("Try routing with train that doesn't service car built before 1983", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // try car loads
        c3.setLoadName("Tools");
        ActonToBedfordTrain.addLoadName("Tools");
        ActonToBedfordTrain.setLoadOption(Train.EXCLUDE_LOADS);

        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that doesn't service load Tools", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        // try the car load "E"
        c4.setDestination(null, null);  // clear previous destination
        c4.setFinalDestination(bedford);
        Assert.assertTrue("Try routing with train that services load E", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c4.getDestinationName());

        ActonToBedfordTrain.setLoadOption(Train.ALL_LOADS);
        Assert.assertTrue("Try routing with train that that does service load Tools", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // don't allow Bedford to service Flat
        c4.setDestination(null, null);  // clear previous destination
        c4.setFinalDestination(bedford);
        bedford.deleteTypeName("Flat");
        Assert.assertFalse("Try routing with Bedford that does not service Flat", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "", c4.getDestinationName());
        Assert.assertTrue("Router status", router.getStatus().startsWith(Track.TYPE));

        // restore Bedford can service Flat
        bedford.addTypeName("Flat");

        // now test by modifying the route
        rlActon.setPickUpAllowed(false);
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that doesn't pickup cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        rlActon.setPickUpAllowed(true);
        Assert.assertTrue("Try routing with train that that can pickup cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        rlBedford.setDropAllowed(false);
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that doesn't drop cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        rlBedford.setDropAllowed(true);
        Assert.assertTrue("Try routing with train that that can drop cars", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        rlBedford.setMaxCarMoves(0);
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that doesn't service destination", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        rlBedford.setMaxCarMoves(5);
        Assert.assertTrue("Try routing with train that does service destination", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        rlActon.setMaxCarMoves(0);
        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that doesn't service location", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        rlActon.setMaxCarMoves(5);
        Assert.assertTrue("Try routing with train that does service location", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // test train depart direction
        Assert.assertEquals("check default direction", Track.NORTH, rlActon.getTrainDirection());
        // set the depart location Acton to service by South bound trains only
        acton.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);

        // remove the Action local by not allowing train to service boxcars
        actonTrain.deleteTypeName("Boxcar");
        Assert.assertFalse("Try routing with train that departs north, location south", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        acton.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with train that departs north, location north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // set the depart track Acton to service by South bound trains only
        actonSiding1.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that departs north, track south", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        actonSiding1.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // test arrival directions
        // set the arrival location Bedford to service by South bound trains only
        bedford.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing with train that arrives north, destination south", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        bedford.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with train that arrives north, destination north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        // set the depart track Acton to service by South bound trains only
        bedfordSpur1.setTrainDirections(Track.SOUTH);

        // and the next destination for the car
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertTrue("Try routing with train that arrives north, but no final track", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford); // the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);  // now specify the actual track
        Assert.assertFalse("Try routing with train that arrives north, now with track", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        bedfordSpur1.setTrainDirections(Track.NORTH);
        Assert.assertTrue("Try routing with train that departs north, track north", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());

        Setup.setOnlyActiveTrainsEnabled(true);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertTrue("Try routing only active trains", router.setDestination(c3, null, null));

        // now deselect the Action to Bedford train
        ActonToBedfordTrain.setBuildEnabled(false);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertFalse("Try routing only active trains, Action to Beford deselected", router.setDestination(c3, null, null));
        Assert.assertEquals("Router status", Router.STATUS_NOT_ABLE, router.getStatus());

        Setup.setOnlyActiveTrainsEnabled(false);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        Assert.assertTrue("Try routing, only active trains deselected", router.setDestination(c3, null, null));

        // test yard and alternate track options
        bedfordSpur1.setLength(c3.getTotalLength());
        // c4 is the same length as c3, so the track is now full
        Assert.assertEquals("Use up all of the space for bedfordSpur1", Track.OKAY, c4.setLocation(bedford, bedfordSpur1));
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        c3.setFinalDestinationTrack(bedfordSpur1);
        Assert.assertTrue("Test search for yard", router.setDestination(c3, null, null));
        Assert.assertEquals("Destination", "Bedford MA", c3.getDestinationName());
        Assert.assertEquals("Destination track should be yard", "Bedford Yard", c3.getDestinationTrackName());
        // the car was sent to a yard track because the spur was full
        Assert.assertTrue("Should be reporting length issue", router.getStatus().startsWith(Track.LENGTH));

        // remove yard type
        bedfordYard.setTrackType(Track.SPUR);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        c3.setFinalDestinationTrack(bedfordSpur1);
        Assert.assertTrue("Test search for yard that doesn't exist", router.setDestination(c3, null, null));
        Assert.assertEquals("Destination", "", c3.getDestinationName());
        Assert.assertTrue("Should be reporting length issue", router.getStatus().startsWith(Track.LENGTH));

        // restore yard type
        bedfordYard.setTrackType(Track.YARD);

        // test alternate track option
        bedfordSpur1.setAlternateTrack(bedfordSpur2);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);
        c3.setFinalDestinationTrack(bedfordSpur1);
        Assert.assertTrue("Test use alternate", router.setDestination(c3, null, null));
        Assert.assertEquals("Destination", "Bedford MA", c3.getDestinationName());
        Assert.assertEquals("Destination track should be siding", "Bedford Siding 2", c3.getDestinationTrackName());
        Assert.assertTrue("Should be reporting length issue", router.getStatus().startsWith(Track.LENGTH));
    }
    
    /**
     * Two trains and two locations testing. A local performs the first move.
     */
    public void testCarRoutingC() {
        // now load up the managers
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Router router = InstanceManager.getDefault(Router.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);

        // register the car and engine types used
        ct.addName("Boxcar");
        ct.addName(Bundle.getMessage("Caboose"));
        ct.addName("Flat");

        // create 2 locations and tracks
        Location acton = lmanager.newLocation("Acton MA");
        Assert.assertEquals("Location 1 Name", "Acton MA", acton.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, acton.getLength());

        Track actonSiding1 = acton.addTrack("Acton Siding 1", Track.SPUR);
        actonSiding1.setLength(300);
        Assert.assertEquals("Location actonSpur1 Name", "Acton Siding 1", actonSiding1.getName());
        Assert.assertEquals("Location actonSpur1 Length", 300, actonSiding1.getLength());

        Track actonSiding2 = acton.addTrack("Acton Siding 2", Track.SPUR);
        actonSiding2.setLength(300);
        Assert.assertEquals("Location actonSpur2 Name", "Acton Siding 2", actonSiding2.getName());
        Assert.assertEquals("Location actonSpur2 Length", 300, actonSiding2.getLength());

        Track actonYard = acton.addTrack("Acton Yard", Track.YARD);
        actonYard.setLength(400);
        Assert.assertEquals("Location actonYard Name", "Acton Yard", actonYard.getName());
        Assert.assertEquals("Location actonYard Length", 400, actonYard.getLength());

        Track actonInterchange1 = acton.addTrack("Acton Interchange", Track.INTERCHANGE);
        actonInterchange1.setLength(500);
        Assert.assertEquals("Track AI Name", "Acton Interchange", actonInterchange1.getName());
        Assert.assertEquals("Track AI Length", 500, actonInterchange1.getLength());
        Assert.assertEquals("Track AI Train Directions", DIRECTION_ALL, actonInterchange1.getTrainDirections());

        // add a second interchange track
        Track actonInterchange2 = acton.addTrack("Acton Interchange 2", Track.INTERCHANGE);
        actonInterchange2.setLength(500);
        // bias tracks so interchange 2 is always checked after interchange 1
        actonInterchange2.setMoves(100);
        Assert.assertEquals("Track actonInterchange2 Name", "Acton Interchange 2", actonInterchange2.getName());
        Assert.assertEquals("Track actonInterchange2 Length", 500, actonInterchange2.getLength());
        Assert.assertEquals("Track actonInterchange2 Train Directions", DIRECTION_ALL, actonInterchange2.getTrainDirections());

        Location bedford = lmanager.newLocation("Bedford MA");
        Assert.assertEquals("Location 1 Name", "Bedford MA", bedford.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, bedford.getLength());

        Track bedfordSpur1 = bedford.addTrack("Bedford Siding 1", Track.SPUR);
        bedfordSpur1.setLength(300);
        Assert.assertEquals("Location bedfordSpur1 Name", "Bedford Siding 1", bedfordSpur1.getName());
        Assert.assertEquals("Location bedfordSpur1 Length", 300, bedfordSpur1.getLength());

        Track bedfordSpur2 = bedford.addTrack("Bedford Siding 2", Track.SPUR);
        bedfordSpur2.setLength(300);
        Assert.assertEquals("Location bedfordSpur2 Name", "Bedford Siding 2", bedfordSpur2.getName());
        Assert.assertEquals("Location bedfordSpur2 Length", 300, bedfordSpur2.getLength());

        Track bedfordYard = bedford.addTrack("Bedford Yard", Track.YARD);
        bedfordYard.setLength(400);
        Assert.assertEquals("Location bedfordYard Name", "Bedford Yard", bedfordYard.getName());
        Assert.assertEquals("Location bedfordYard Length", 400, bedfordYard.getLength());

        Track bedfordInterchange = bedford.addTrack("Bedford Interchange", Track.INTERCHANGE);
        bedfordInterchange.setLength(500);
        Assert.assertEquals("Track bedfordInterchange Name", "Bedford Interchange", bedfordInterchange.getName());
        Assert.assertEquals("Track bedfordInterchange Length", 500, bedfordInterchange.getLength());

        // create 3 cars
        Car c3 = cmanager.newCar("BA", "3");
        c3.setTypeName("Boxcar");
        c3.setLength("40");
        c3.setOwner("DAB");
        c3.setBuilt("1984");
        Assert.assertEquals("Box Car 3 Length", "40", c3.getLength());

        Car c4 = cmanager.newCar("BB", "4");
        c4.setTypeName("Flat");
        c4.setLength("40");
        c4.setOwner("AT");
        c4.setBuilt("1-86");
        Assert.assertEquals("Box Car 4 Length", "40", c4.getLength());
        
        Car c5 = cmanager.newCar("BC", "5");
        c5.setTypeName("Boxcar");
        c5.setLength("40");

        Assert.assertEquals("place car at bedfordInterchange", Track.OKAY, c3.setLocation(acton, actonSiding1));
        Assert.assertFalse("Try routing no final destination", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        Assert.assertEquals("place car at Acton", Track.OKAY, c4.setLocation(acton, actonSiding1));
        Assert.assertFalse("Try routing no final destination", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "", c4.getDestinationName());


        // create a local train servicing Acton
        Train actonTrain = tmanager.newTrain("Acton Local");
        Route routeA = rmanager.newRoute("A");
        RouteLocation rlActon1 = routeA.addLocation(acton);
        rlActon1.setTrainIconX(25); // set train icon coordinates
        rlActon1.setTrainIconY(250);
        actonTrain.setRoute(routeA);

        // add a second local train
        // create a local train servicing Acton
        Train ActonTrain2 = tmanager.newTrain("Acton Local 2");
        Route routeA2 = rmanager.newRoute("A2");
        RouteLocation rlA2 = routeA2.addLocation(acton);
        rlA2.setTrainIconX(25); // set train icon coordinates
        rlA2.setTrainIconY(250);
        ActonTrain2.setRoute(routeA2);

        // don't allow Acton Local 2 to service boxcars
        ActonTrain2.deleteTypeName("Boxcar");

        // create a train with a route from Acton to Bedford
        Train ActonToBedfordTrain = tmanager.newTrain("Acton to Bedford");
        Route routeAB = rmanager.newRoute("AB");
        RouteLocation rlActon = routeAB.addLocation(acton);
        Assert.assertNotNull(rlActon);
        RouteLocation rlBedford = routeAB.addLocation(bedford);
        rlBedford.setTrainIconX(100);   // set train icon coordinates
        rlBedford.setTrainIconY(250);
        ActonToBedfordTrain.setRoute(routeAB);

        // Force first move to be by local train
        actonSiding1.setTrainDirections(0);
        
        // Start two train testing.

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);    // the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);  // now specify the actual track

        Assert.assertTrue("Try routing two trains via interchange", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());

        // don't allow use of interchange track 1, forces the use of interchange track 2
        actonInterchange1.setDropOption(Track.TRAINS);

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);    // the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);  // now specify the actual track

        Assert.assertTrue("Try routing two trains via interchange", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 2", c3.getDestinationTrackName());

        actonInterchange2.setDropOption(Track.TRAINS);

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);    // the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);  // now specify the actual track

        Assert.assertTrue("Try routing two trains via yard", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Yard", c3.getDestinationTrackName());

        // allow use of interchange track
        actonInterchange1.setDropOption(Track.ANY);
        actonInterchange2.setDropOption(Track.ANY);

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);    // the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);

        // allow Boxcars
        ActonTrain2.addTypeName("Boxcar");

        Assert.assertTrue("Try routing two trains", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());

        // test to see if second interchange track used if first is full
        actonInterchange1.setLength(c3.getTotalLength() - 1); // track capacity issue
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);    // the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);
        Assert.assertTrue("Try routing two trains using interchange track 2", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 2", c3.getDestinationTrackName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());

        // use yard track if interchange tracks are full
        actonInterchange2.setLength(c3.getTotalLength() - 1); // track capacity issue
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);    // the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);
        Assert.assertTrue("Try routing two trains to yard track", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Yard", c3.getDestinationTrackName());

        // disable using yard tracks for routing
        Setup.setCarRoutingViaYardsEnabled(false);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);    // the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);
        
        Assert.assertFalse("Try routing two trains to yard track", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());
        
        // make interim track issue length
        actonInterchange1.setLength(c3.getTotalLength() + c5.getTotalLength() - 1); // too short for two cars
        Assert.assertEquals("place car at bedfordInterchange", Track.OKAY, c5.setLocation(acton, actonInterchange1));
        Assert.assertTrue("Try routing two trains", router.setDestination(c3, ActonTrain2, null));

        // restore track length
        actonInterchange1.setLength(500);
        actonInterchange2.setLength(500);
        Setup.setCarRoutingViaYardsEnabled(true);

        // don't allow train 2 to service boxcars with road name BA
        ActonTrain2.addRoadName("BA");
        ActonTrain2.setRoadOption(Train.EXCLUDE_ROADS);

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(bedford);    // the final destination for the car
        c3.setFinalDestinationTrack(bedfordSpur1);

        // routing should work using train 1, destination and track should not be set
        Assert.assertTrue("Try routing two trains via yard", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());

    }
    
    /**
     * Five train routing test
     */
    public void testCarRoutingD() {

        // now load up the managers
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Router router = InstanceManager.getDefault(Router.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);

        // register the car and engine types used
        ct.addName("Boxcar");
        ct.addName(Bundle.getMessage("Caboose"));
        ct.addName("Flat");

        // create 6 locations and tracks
        Location acton = lmanager.newLocation("Acton MA");
        Assert.assertEquals("Location 1 Name", "Acton MA", acton.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, acton.getLength());

        Track actonSiding1 = acton.addTrack("Acton Siding 1", Track.SPUR);
        actonSiding1.setLength(300);
        Assert.assertEquals("Location actonSpur1 Name", "Acton Siding 1", actonSiding1.getName());
        Assert.assertEquals("Location actonSpur1 Length", 300, actonSiding1.getLength());

        Track actonSiding2 = acton.addTrack("Acton Siding 2", Track.SPUR);
        actonSiding2.setLength(300);
        Assert.assertEquals("Location actonSpur2 Name", "Acton Siding 2", actonSiding2.getName());
        Assert.assertEquals("Location actonSpur2 Length", 300, actonSiding2.getLength());

        Track actonYard = acton.addTrack("Acton Yard", Track.YARD);
        actonYard.setLength(400);
        Assert.assertEquals("Location actonYard Name", "Acton Yard", actonYard.getName());
        Assert.assertEquals("Location actonYard Length", 400, actonYard.getLength());

        Track actonInterchange1 = acton.addTrack("Acton Interchange", Track.INTERCHANGE);
        actonInterchange1.setLength(500);
        Assert.assertEquals("Track AI Name", "Acton Interchange", actonInterchange1.getName());
        Assert.assertEquals("Track AI Length", 500, actonInterchange1.getLength());
        Assert.assertEquals("Track AI Train Directions", DIRECTION_ALL, actonInterchange1.getTrainDirections());

        // add a second interchange track
        Track actonInterchange2 = acton.addTrack("Acton Interchange 2", Track.INTERCHANGE);
        actonInterchange2.setLength(500);
        // bias tracks
        actonInterchange2.setMoves(100);
        Assert.assertEquals("Track actonInterchange2 Name", "Acton Interchange 2", actonInterchange2.getName());
        Assert.assertEquals("Track actonInterchange2 Length", 500, actonInterchange2.getLength());
        Assert.assertEquals("Track actonInterchange2 Train Directions", DIRECTION_ALL, actonInterchange2.getTrainDirections());

        Location bedford = lmanager.newLocation("Bedford MA");
        Assert.assertEquals("Location 1 Name", "Bedford MA", bedford.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, bedford.getLength());

        Track bedfordSpur1 = bedford.addTrack("Bedford Siding 1", Track.SPUR);
        bedfordSpur1.setLength(300);
        Assert.assertEquals("Location bedfordSpur1 Name", "Bedford Siding 1", bedfordSpur1.getName());
        Assert.assertEquals("Location bedfordSpur1 Length", 300, bedfordSpur1.getLength());

        Track bedfordSpur2 = bedford.addTrack("Bedford Siding 2", Track.SPUR);
        bedfordSpur2.setLength(300);
        Assert.assertEquals("Location bedfordSpur2 Name", "Bedford Siding 2", bedfordSpur2.getName());
        Assert.assertEquals("Location bedfordSpur2 Length", 300, bedfordSpur2.getLength());

        Track bedfordYard = bedford.addTrack("Bedford Yard", Track.YARD);
        bedfordYard.setLength(400);
        Assert.assertEquals("Location bedfordYard Name", "Bedford Yard", bedfordYard.getName());
        Assert.assertEquals("Location bedfordYard Length", 400, bedfordYard.getLength());

        Track bedfordInterchange = bedford.addTrack("Bedford Interchange", Track.INTERCHANGE);
        bedfordInterchange.setLength(500);
        Assert.assertEquals("Track bedfordInterchange Name", "Bedford Interchange", bedfordInterchange.getName());
        Assert.assertEquals("Track bedfordInterchange Length", 500, bedfordInterchange.getLength());

        Location clinton = lmanager.newLocation("Clinton MA");
        Assert.assertEquals("Location 1 Name", "Clinton MA", clinton.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, clinton.getLength());

        Track clintonSpur1 = clinton.addTrack("Clinton Siding 1", Track.SPUR);
        clintonSpur1.setLength(300);
        Assert.assertEquals("Location clintonSpur1 Name", "Clinton Siding 1", clintonSpur1.getName());
        Assert.assertEquals("Location clintonSpur1 Length", 300, clintonSpur1.getLength());

        Track clintonSpur2 = clinton.addTrack("Clinton Siding 2", Track.SPUR);
        clintonSpur2.setLength(300);
        Assert.assertEquals("Location clintonSpur2 Name", "Clinton Siding 2", clintonSpur2.getName());
        Assert.assertEquals("Location clintonSpur2 Length", 300, bedfordSpur2.getLength());

        Track clintonYard = clinton.addTrack("Clinton Yard", Track.YARD);
        clintonYard.setLength(400);
        Assert.assertEquals("Location clintonYard Name", "Clinton Yard", clintonYard.getName());
        Assert.assertEquals("Location clintonYard Length", 400, clintonYard.getLength());

        Track clintonInterchange = clinton.addTrack("Clinton Interchange", Track.INTERCHANGE);
        clintonInterchange.setLength(500);
        Assert.assertEquals("Track clintonInterchange Name", "Clinton Interchange", clintonInterchange.getName());
        Assert.assertEquals("Track clintonInterchange Length", 500, clintonInterchange.getLength());

        Location danbury = lmanager.newLocation("Danbury MA");
        Track danburySpur1 = danbury.addTrack("Danbury Siding 1", Track.SPUR);
        danburySpur1.setLength(300);
        Track danburySpur2 = danbury.addTrack("Danbury Siding 2", Track.SPUR);
        danburySpur2.setLength(300);
        Track danburyYard = danbury.addTrack("Danbury Yard", Track.YARD);
        danburyYard.setLength(400);
        Track danburyInterchange = danbury.addTrack("Danbury Interchange", Track.INTERCHANGE);
        danburyInterchange.setLength(500);

        Location essex = lmanager.newLocation("Essex MA");
        Track essexSpur1 = essex.addTrack("Essex Siding 1", Track.SPUR);
        essexSpur1.setLength(300);
        Track essexSpur2 = essex.addTrack("Essex Siding 2", Track.SPUR);
        essexSpur2.setLength(300);
        Track essexYard = essex.addTrack("Essex Yard", Track.YARD);
        essexYard.setLength(400);
        Track essexInterchange = essex.addTrack("Essex Interchange", Track.INTERCHANGE);
        essexInterchange.setLength(500);

        Location foxboro = lmanager.newLocation("Foxboro MA");
        Track foxboroSpur1 = foxboro.addTrack("Foxboro Siding 1", Track.SPUR);
        foxboroSpur1.setLength(300);
        Track foxboroSpur2 = foxboro.addTrack("Foxboro Siding 2", Track.SPUR);
        foxboroSpur2.setLength(300);
        Track foxboroYard = foxboro.addTrack("Foxboro Yard", Track.YARD);
        foxboroYard.setLength(400);
        Track foxboroInterchange = foxboro.addTrack("Foxboro Interchange", Track.INTERCHANGE);
        foxboroInterchange.setLength(500);

        // create 2 cars
        Car c3 = cmanager.newCar("BA", "3");
        c3.setTypeName("Boxcar");
        c3.setLength("40");
        c3.setOwner("DAB");
        c3.setBuilt("1984");
        Assert.assertEquals("Box Car 3 Length", "40", c3.getLength());

        Car c4 = cmanager.newCar("BB", "4");
        c4.setTypeName("Flat");
        c4.setLength("40");
        c4.setOwner("AT");
        c4.setBuilt("1-86");
        Assert.assertEquals("Box Car 4 Length", "40", c4.getLength());

        Assert.assertEquals("place car at bedfordInterchange", Track.OKAY, c3.setLocation(acton, actonSiding1));
        Assert.assertFalse("Try routing no final destination", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());

        Assert.assertEquals("place car at Acton", Track.OKAY, c4.setLocation(acton, actonSiding1));
        Assert.assertFalse("Try routing no final destination", router.setDestination(c4, null, null));
        Assert.assertEquals("Check car's destination", "", c4.getDestinationName());


        // create a local train servicing Acton
        Train actonTrain = tmanager.newTrain("Acton Local");
        Route routeA = rmanager.newRoute("A");
        RouteLocation rlActon1 = routeA.addLocation(acton);
        rlActon1.setTrainIconX(25); // set train icon coordinates
        rlActon1.setTrainIconY(250);
        actonTrain.setRoute(routeA);

        // add a second local train
        // create a local train servicing Acton
        Train ActonTrain2 = tmanager.newTrain("Acton Local 2");
        Route routeA2 = rmanager.newRoute("A2");
        RouteLocation rlA2 = routeA2.addLocation(acton);
        rlA2.setTrainIconX(25); // set train icon coordinates
        rlA2.setTrainIconY(250);
        ActonTrain2.setRoute(routeA2);

        // create a train with a route from Acton to Bedford
        Train ActonToBedfordTrain = tmanager.newTrain("Acton to Bedford");
        Route routeAB = rmanager.newRoute("AB");
        RouteLocation rlActon = routeAB.addLocation(acton);
        Assert.assertNotNull(rlActon);
        RouteLocation rlBedford = routeAB.addLocation(bedford);
        rlBedford.setTrainIconX(100);   // set train icon coordinates
        rlBedford.setTrainIconY(250);
        ActonToBedfordTrain.setRoute(routeAB);

        // Force first move to be by local train
        actonSiding1.setTrainDirections(0);
        

        // try up to 5 trains to route car
        // set final destination Clinton
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(clinton);
        c3.setFinalDestinationTrack(null);
        // should fail no train!
        Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));

        // create a train with a route from Bedford to Clinton
        Train bedfordToClintonTrain = tmanager.newTrain("Bedford to Clinton");
        Route routeBC = rmanager.newRoute("BC");
        routeBC.addLocation(bedford);
        RouteLocation rlchelmsford = routeBC.addLocation(clinton);
        rlchelmsford.setTrainIconX(175);    // set train icon coordinates
        rlchelmsford.setTrainIconY(250);
        bedfordToClintonTrain.setRoute(routeBC);

        // should work
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());

        // allow train 2 to service boxcars with road name BA
        ActonTrain2.setRoadOption(Train.ALL_ROADS);

        // try with train 2
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(clinton);

        // routing should work using train 2
        Assert.assertTrue("Try routing three trains", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(clinton);

        // test to see if second interchange track used if first is full
        actonInterchange1.setLength(c3.getTotalLength() - 1);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(clinton);    // the final destination for the car
        Assert.assertTrue("Try routing three trains to interchange track 2", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange 2", c3.getDestinationTrackName());

        // use yard track if interchange tracks are full
        actonInterchange2.setLength(c3.getTotalLength() - 1);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(clinton);    // the final destination for the car
        Assert.assertTrue("Try routing three trains to yard track", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Yard", c3.getDestinationTrackName());

        // disable the use of yard tracks for routing
        Setup.setCarRoutingViaYardsEnabled(false);
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(clinton);    // the final destination for the car
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

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(clinton);    // the final destination for the car
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

        // set final destination Danbury
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(danbury);
        // should fail no train!
        Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));

        // create a train with a route from Clinton to Danbury
        Train clintonToDanburyTrain = tmanager.newTrain("Clinton to Danbury");
        Route routeCD = rmanager.newRoute("CD");
        routeCD.addLocation(clinton);
        RouteLocation rlDanbury = routeCD.addLocation(danbury);
        rlDanbury.setTrainIconX(250);   // set train icon coordinates
        rlDanbury.setTrainIconY(250);
        clintonToDanburyTrain.setRoute(routeCD);

        // should work
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(danbury);    // the final destination for the car

        // routing should work using train 2
        Assert.assertTrue("Try routing four trains", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(danbury);    // the final destination for the car

        // don't allow train 2 to service cars with load Tools
        ActonTrain2.addLoadName("Tools");
        ActonTrain2.setLoadOption(Train.EXCLUDE_LOADS);
        c3.setLoadName("Tools");
        // routing should work using train 1, but destinations and track should not be set
        Assert.assertTrue("Try routing four trains", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "", c3.getDestinationTrackName());
        // restore train 2
        ActonTrain2.setLoadOption(Train.ALL_LOADS);

        // set final destination Essex
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(essex);
        // should fail no train!
        Assert.assertFalse("Try routing with final destination", router.setDestination(c3, null, null));

        // create a train with a route from Danbury to Essex
        Train danburyToEssexTrain = tmanager.newTrain("Danbury to Essex");
        Route routeDE = rmanager.newRoute("DE");
        RouteLocation rlDanbury2 = routeDE.addLocation(danbury);
        RouteLocation rlEssex = routeDE.addLocation(essex);
        // set the number of car moves to 8 for a later test
        rlDanbury2.setMaxCarMoves(8);
        rlEssex.setMaxCarMoves(8);
        rlEssex.setTrainIconX(25);  // set train icon coordinates
        rlEssex.setTrainIconY(275);
        danburyToEssexTrain.setRoute(routeDE);

        // should work
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());

        // routing should work using train 2
        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(essex);
        Assert.assertTrue("Try routing five trains", router.setDestination(c3, ActonTrain2, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Interchange", c3.getDestinationTrackName());

        c3.setDestination(null, null);  // clear previous destination
        c3.setFinalDestination(essex);
        // don't allow train 2 to pickup
        rlA2.setPickUpAllowed(false);
        // routing should work using train 1, but destinations and track should not be set
        Assert.assertTrue("Try routing five trains", router.setDestination(c3, ActonTrain2, null));
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
        Assert.assertFalse("Try routing with final destination and train", router.setDestination(c3, actonTrain, null));
        Assert.assertFalse("Try routing with final destination and train", router.setDestination(c3, ActonTrain2, null));

        // get rid of the local train
        actonSiding1.setTrainDirections(Track.NORTH);

        // now should work!
        Assert.assertTrue("Try routing with final destination and train", router.setDestination(c3, null, null));
        Assert.assertEquals("Check car's destination", "Bedford MA", c3.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Bedford Interchange", c3.getDestinationTrackName());

        //TODO test restrict location by car type
        //TODO test restrict tracks by type road, load
    }
    
    /**
     * Test routing through staging
     */
    public void testCarRoutingE() {
        // now load up the managers
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Router router = InstanceManager.getDefault(Router.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        
        // turn off error dialogue
        tmanager.setBuildMessagesEnabled(false);

        // register the car and engine types used
        ct.addName("Boxcar");
        ct.addName(Bundle.getMessage("Caboose"));
        ct.addName("Flat");

        // create 6 locations and tracks
        Location acton = lmanager.newLocation("Acton MA");
        Assert.assertEquals("Location 1 Name", "Acton MA", acton.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, acton.getLength());

        Track actonSiding1 = acton.addTrack("Acton Siding 1", Track.SPUR);
        actonSiding1.setLength(300);
        Assert.assertEquals("Location actonSpur1 Name", "Acton Siding 1", actonSiding1.getName());
        Assert.assertEquals("Location actonSpur1 Length", 300, actonSiding1.getLength());

        Track actonSiding2 = acton.addTrack("Acton Siding 2", Track.SPUR);
        actonSiding2.setLength(300);
        Assert.assertEquals("Location actonSpur2 Name", "Acton Siding 2", actonSiding2.getName());
        Assert.assertEquals("Location actonSpur2 Length", 300, actonSiding2.getLength());

        Track actonYard = acton.addTrack("Acton Yard", Track.YARD);
        actonYard.setLength(400);
        Assert.assertEquals("Location actonYard Name", "Acton Yard", actonYard.getName());
        Assert.assertEquals("Location actonYard Length", 400, actonYard.getLength());

        Track actonInterchange1 = acton.addTrack("Acton Interchange", Track.INTERCHANGE);
        actonInterchange1.setLength(500);
        Assert.assertEquals("Track AI Name", "Acton Interchange", actonInterchange1.getName());
        Assert.assertEquals("Track AI Length", 500, actonInterchange1.getLength());
        Assert.assertEquals("Track AI Train Directions", DIRECTION_ALL, actonInterchange1.getTrainDirections());

        // add a second interchange track
        Track actonInterchange2 = acton.addTrack("Acton Interchange 2", Track.INTERCHANGE);
        actonInterchange2.setLength(500);
        // bias tracks
        actonInterchange2.setMoves(100);
        Assert.assertEquals("Track actonInterchange2 Name", "Acton Interchange 2", actonInterchange2.getName());
        Assert.assertEquals("Track actonInterchange2 Length", 500, actonInterchange2.getLength());
        Assert.assertEquals("Track actonInterchange2 Train Directions", DIRECTION_ALL, actonInterchange2.getTrainDirections());

        Location bedford = lmanager.newLocation("Bedford MA");
        Assert.assertEquals("Location 1 Name", "Bedford MA", bedford.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, bedford.getLength());

        Track bedfordSpur1 = bedford.addTrack("Bedford Siding 1", Track.SPUR);
        bedfordSpur1.setLength(300);
        Assert.assertEquals("Location bedfordSpur1 Name", "Bedford Siding 1", bedfordSpur1.getName());
        Assert.assertEquals("Location bedfordSpur1 Length", 300, bedfordSpur1.getLength());

        Track bedfordSpur2 = bedford.addTrack("Bedford Siding 2", Track.SPUR);
        bedfordSpur2.setLength(300);
        Assert.assertEquals("Location bedfordSpur2 Name", "Bedford Siding 2", bedfordSpur2.getName());
        Assert.assertEquals("Location bedfordSpur2 Length", 300, bedfordSpur2.getLength());

        Track bedfordYard = bedford.addTrack("Bedford Yard", Track.YARD);
        bedfordYard.setLength(400);
        Assert.assertEquals("Location bedfordYard Name", "Bedford Yard", bedfordYard.getName());
        Assert.assertEquals("Location bedfordYard Length", 400, bedfordYard.getLength());

        Track bedfordInterchange = bedford.addTrack("Bedford Interchange", Track.INTERCHANGE);
        bedfordInterchange.setLength(500);
        Assert.assertEquals("Track bedfordInterchange Name", "Bedford Interchange", bedfordInterchange.getName());
        Assert.assertEquals("Track bedfordInterchange Length", 500, bedfordInterchange.getLength());

        Location clinton = lmanager.newLocation("Clinton MA");
        Assert.assertEquals("Location 1 Name", "Clinton MA", clinton.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, clinton.getLength());

        Track clintonSpur1 = clinton.addTrack("Clinton Siding 1", Track.SPUR);
        clintonSpur1.setLength(300);
        Assert.assertEquals("Location clintonSpur1 Name", "Clinton Siding 1", clintonSpur1.getName());
        Assert.assertEquals("Location clintonSpur1 Length", 300, clintonSpur1.getLength());

        Track clintonSpur2 = clinton.addTrack("Clinton Siding 2", Track.SPUR);
        clintonSpur2.setLength(300);
        Assert.assertEquals("Location clintonSpur2 Name", "Clinton Siding 2", clintonSpur2.getName());
        Assert.assertEquals("Location clintonSpur2 Length", 300, bedfordSpur2.getLength());

        Track clintonYard = clinton.addTrack("Clinton Yard", Track.YARD);
        clintonYard.setLength(400);
        Assert.assertEquals("Location clintonYard Name", "Clinton Yard", clintonYard.getName());
        Assert.assertEquals("Location clintonYard Length", 400, clintonYard.getLength());

        Track clintonInterchange = clinton.addTrack("Clinton Interchange", Track.INTERCHANGE);
        clintonInterchange.setLength(500);
        Assert.assertEquals("Track clintonInterchange Name", "Clinton Interchange", clintonInterchange.getName());
        Assert.assertEquals("Track clintonInterchange Length", 500, clintonInterchange.getLength());

        Location danbury = lmanager.newLocation("Danbury MA");
        Track danburySpur1 = danbury.addTrack("Danbury Siding 1", Track.SPUR);
        danburySpur1.setLength(300);
        Track danburySpur2 = danbury.addTrack("Danbury Siding 2", Track.SPUR);
        danburySpur2.setLength(300);
        Track danburyYard = danbury.addTrack("Danbury Yard", Track.YARD);
        danburyYard.setLength(400);
        Track danburyInterchange = danbury.addTrack("Danbury Interchange", Track.INTERCHANGE);
        danburyInterchange.setLength(500);

        Location essex = lmanager.newLocation("Essex MA");
        Track essexSpur1 = essex.addTrack("Essex Siding 1", Track.SPUR);
        essexSpur1.setLength(300);
        Track essexSpur2 = essex.addTrack("Essex Siding 2", Track.SPUR);
        essexSpur2.setLength(300);
        Track essexYard = essex.addTrack("Essex Yard", Track.YARD);
        essexYard.setLength(400);
        Track essexInterchange = essex.addTrack("Essex Interchange", Track.INTERCHANGE);
        essexInterchange.setLength(500);

        Location foxboro = lmanager.newLocation("Foxboro MA");
        Track foxboroSpur1 = foxboro.addTrack("Foxboro Siding 1", Track.SPUR);
        foxboroSpur1.setLength(300);
        Track foxboroSpur2 = foxboro.addTrack("Foxboro Siding 2", Track.SPUR);
        foxboroSpur2.setLength(300);
        Track foxboroYard = foxboro.addTrack("Foxboro Yard", Track.YARD);
        foxboroYard.setLength(400);
        Track foxboroInterchange = foxboro.addTrack("Foxboro Interchange", Track.INTERCHANGE);
        foxboroInterchange.setLength(500);
        
        Location gulf = lmanager.newLocation("Gulf MA");
        Track gulfYard = gulf.addTrack("Gulf Yard", Track.YARD);
        gulfYard.setLength(400);
        
        Location staging = lmanager.newLocation("Staging MA");
        Track stagingTrack1 = staging.addTrack("Staging 1", Track.STAGING);
        stagingTrack1.setLength(500);
        Track stagingTrack2 = staging.addTrack("Staging 2", Track.STAGING);
        stagingTrack2.setLength(500);
        stagingTrack2.setMoves(60);
        Track stagingTrack3 = staging.addTrack("Staging 3", Track.STAGING);
        stagingTrack3.setLength(500);
        stagingTrack3.setMoves(80);
        Track stagingTrack4 = staging.addTrack("Staging 4", Track.STAGING);
        stagingTrack4.setLength(500);
        stagingTrack4.setMoves(100); // the last track to try in staging
        
        // don't allow Boxcars staging first 3 tracks
        stagingTrack1.deleteTypeName("Boxcar");
        stagingTrack2.deleteTypeName("Boxcar");
        stagingTrack3.deleteTypeName("Boxcar");

        // create 2 cars
        Car c3 = cmanager.newCar("BA", "3");
        c3.setTypeName("Boxcar");
        c3.setLength("40");
        c3.setOwner("DAB");
        c3.setBuilt("1984");
        Assert.assertEquals("Box Car 3 Length", "40", c3.getLength());

        Car c4 = cmanager.newCar("BB", "4");
        c4.setTypeName("Flat");
        c4.setLength("40");
        c4.setOwner("AT");
        c4.setBuilt("1-86");
        Assert.assertEquals("Box Car 4 Length", "40", c4.getLength());

        Assert.assertEquals("place car at Acton", Track.OKAY, c3.setLocation(acton, actonSiding1));
        Assert.assertEquals("place car at Acton", Track.OKAY, c4.setLocation(acton, actonSiding1));

        // create two trains, one terminates into staging, the other departs
        Train actonToStagingTrain = tmanager.newTrain("Train Acton to Staging");
        Route routeA = rmanager.newRoute("A");
        routeA.addLocation(acton);
        routeA.addLocation(bedford);
        routeA.addLocation(staging); // terminated into staging
        actonToStagingTrain.setRoute(routeA);
        
        Train stagingToFoxboro1 = tmanager.newTrain("Train Staging to Foxboro");
        Route routeB = rmanager.newRoute("B");
        routeB.addLocation(staging);
        RouteLocation rlgulf = routeB.addLocation(gulf);
        routeB.addLocation(essex);
        routeB.addLocation(foxboro);
        stagingToFoxboro1.setRoute(routeB);
        
        // default is routing through staging is disabled
        Assert.assertFalse("Default routing through staging", Setup.isCarRoutingViaStagingEnabled());
        // try and route the car through staging to Foxboro
        c3.setFinalDestination(essex);
        Assert.assertFalse("Try routing two trains through staging", router.setDestination(c3, null, null));
        Assert.assertEquals("Check status", Router.STATUS_NOT_ABLE, router.getStatus());
        
        // enable routing through staging
        Setup.setCarRoutingViaStagingEnabled(true);
        Assert.assertTrue("Try routing two trains through staging", router.setDestination(c3, null, null));
        
        // confirm car's destination
        Assert.assertEquals("car's destination is staging", staging, c3.getDestination());
        // note that the router sets a car's track into staging when a "specific" train isn't provided
        Assert.assertEquals("car's destination track is staging", stagingTrack4, c3.getDestinationTrack());
        Assert.assertEquals("car's final destination", essex, c3.getFinalDestination());
                
        c3.setDestination(null, null);  // clear previous destination
        
        // now test 3 trains
        // third train doesn't exist, should fail
        c3.setFinalDestination(clinton);
        c3.setDestination(null, null);  // clear previous destination
        Assert.assertFalse("Try routing three trains through staging", router.setDestination(c3, null, null));
        Assert.assertEquals("Check status", Router.STATUS_NOT_ABLE, router.getStatus());
        
        // new train departs Essex terminates Clinton        
        Train EssexToClinton = tmanager.newTrain("Train Essex to Clinton");
        Route routeC = rmanager.newRoute("C");
        routeC.addLocation(essex);
        routeC.addLocation(clinton);
        EssexToClinton.setRoute(routeC);
        
        Assert.assertTrue("Try routing three trains through staging", router.setDestination(c3, null, null));
        
        // confirm car's destination
        Assert.assertEquals("car's destination is staging", staging, c3.getDestination());
        // note that the router sets a car's track into staging when a "specific" train isn't provided
        Assert.assertEquals("car's destination track is staging", stagingTrack4, c3.getDestinationTrack());
        Assert.assertEquals("car's final destination", clinton, c3.getFinalDestination());
        
        c3.setDestination(null, null);  // clear previous destination
        
//        // build train, now there's a "specific" train
//        actonToStagingTrain.build();
//        Assert.assertTrue(actonToStagingTrain.isBuilt());
//        Assert.assertEquals("car's destination track is staging", stagingTrack4, c3.getDestinationTrack());
//        Assert.assertEquals("car's final destination", clinton, c3.getFinalDestination());
//        actonToStagingTrain.reset();
        
        // place car in staging and try to route
        c3.setDestination(null, null);  // clear previous destination
        Assert.assertEquals("place car in staging", Track.OKAY, c3.setLocation(staging, stagingTrack4));
        
        Assert.assertTrue("Try routing two trains from staging", router.setDestination(c3, null, null));
        Assert.assertEquals("car's destination is essex ", essexInterchange, c3.getDestinationTrack());
        Assert.assertEquals("car's final destination", clinton, c3.getFinalDestination());
        
        // don't allow train out of staging to carry car type Boxcar
        stagingToFoxboro1.deleteTypeName("Boxcar");
        c3.setDestination(null, null);  // clear previous destination
        
        Assert.assertFalse("Try routing two trains from staging", router.setDestination(c3, stagingToFoxboro1, null));
        Assert.assertEquals("car's destination", null, c3.getDestinationTrack());
        Assert.assertEquals("car's final destination", clinton, c3.getFinalDestination());
        
        // create a second train out of staging
        Train stagingToFoxboro2 = tmanager.newTrain("Train Staging to Foxboro 2");
        stagingToFoxboro2.setRoute(routeB); // use the same route
        
        Assert.assertTrue("Try routing two trains from staging", router.setDestination(c3, null, null));
        Assert.assertEquals("car's destination is essex ", essexInterchange, c3.getDestinationTrack());
        Assert.assertEquals("car's final destination", clinton, c3.getFinalDestination());
        
        // now specify the first train that can't carry car type Boxcar
        c3.setDestination(null, null);  // clear previous destination
        Assert.assertFalse("Try routing two trains from staging", router.setDestination(c3, stagingToFoxboro1, null));
        Assert.assertEquals("car's destination", null, c3.getDestinationTrack());
        Assert.assertEquals("car's final destination", clinton, c3.getFinalDestination()); 
        
        // test train out staging length issues, use two cars
        Assert.assertEquals("place car in staging", Track.OKAY, c4.setLocation(staging, stagingTrack4));
        c4.setFinalDestination(clinton); // also send this car to Clinton
        c4.setMoves(100); // always process this car second
        stagingToFoxboro2.build();
        
        // confirm cars added to train and destination
        Assert.assertEquals("car added to train", stagingToFoxboro2, c3.getTrain());
        Assert.assertEquals("car added to train", stagingToFoxboro2, c4.getTrain());
        Assert.assertEquals("car's destination is essex ", essexInterchange, c3.getDestinationTrack());
        Assert.assertEquals("car's final destination", clinton, c3.getFinalDestination());
        Assert.assertEquals("car's destination is essex ", essexInterchange, c4.getDestinationTrack());
        Assert.assertEquals("car's final destination", clinton, c4.getFinalDestination());

        // now limit train length 
        rlgulf.setMaxTrainLength(80); // only one car can be carried
        
        stagingToFoxboro2.build();
        Assert.assertFalse(stagingToFoxboro2.isBuilt()); // build failed
        
        // the Router found a two train route, but gave up using a three train route out of staging
        // TODO should the router use multiple trains just to get the car out of staging?
        // would this screw up the generation of custom loads? As an inefficient route could be selected.
        stagingToFoxboro2.build();
        Assert.assertFalse(stagingToFoxboro2.isBuilt()); 
        
        // change c4 final destination
        c4.setFinalDestination(foxboro); // one hop out staging provides report messages
        
        // yard track at gulf is available
        stagingToFoxboro2.build();
        Assert.assertTrue(stagingToFoxboro2.isBuilt());
        
        Assert.assertEquals("car's destination", gulfYard, c4.getDestinationTrack());
        Assert.assertEquals("car's final destination", foxboro, c4.getFinalDestination());

    }

    // Use trains to move cars
    public void testRoutingWithTrains() {
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);

        loadLocationsTrainsAndCars();

        List<Train> trains = tmanager.getTrainsByNameList();
        Assert.assertEquals("confirm number of trains", 7, trains.size());

        Train actonTrain = tmanager.getTrainByName("Acton Local");
        Train actonToBedfordTrain = tmanager.getTrainByName("Acton to Bedford");
        Train bedfordToClintonTrain = tmanager.getTrainByName("Bedford to Clinton");
        Train clintonToDanburyTrain = tmanager.getTrainByName("Clinton to Danbury");
        Train danburyToEssexTrain = tmanager.getTrainByName("Danbury to Essex");
        Train essexToFoxboroTrain = tmanager.getTrainByName("Essex to Foxboro");

        Car c3 = cmanager.getByRoadAndNumber("BA", "3");
        Car c4 = cmanager.getByRoadAndNumber("BB", "4");

        Location acton = lmanager.getLocationByName("Acton MA");
        Track actonSpur1 = acton.getTrackByName("Acton Siding 1", Track.SPUR);
        // set the depart track Acton to service by local train only
        actonSpur1.setTrainDirections(0);

        Location essex = lmanager.getLocationByName("Essex MA");
        Track essexSpur2 = essex.getTrackByName("Essex Siding 2", Track.SPUR);
        Location Foxboro = lmanager.getLocationByName("Foxboro MA");

        Location holden = lmanager.newLocation("Holden");

        // place cars
        Assert.assertEquals("Place car", Track.OKAY, c3.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c4.setLocation(acton, actonSpur1));

        // confirm cars are in Acton
        Assert.assertEquals("car's location Acton", "Acton MA", c3.getLocationName());
        Assert.assertEquals("car's location Acton", "Acton Siding 1", c3.getTrackName());

        Assert.assertEquals("car's location Acton", "Acton MA", c4.getLocationName());
        Assert.assertEquals("car's location Acton", "Acton Siding 1", c4.getTrackName());

        // set final destination Essex
        c3.setFinalDestination(essex);
        c3.setFinalDestinationTrack(essexSpur2);
        c3.setLoadName("L");
        c3.setReturnWhenEmptyDestination(Foxboro);

        // final destination Gulf is not reachable, so car must move
        c4.setFinalDestination(holden);

        actonTrain.build();
        Assert.assertTrue(actonTrain.isBuilt());

        Assert.assertEquals("car's destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("car's destinaton track", "Acton Interchange", c3.getDestinationTrackName());
        Assert.assertEquals("car's final destinaton", essex, c3.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", essexSpur2, c3.getFinalDestinationTrack());

        Assert.assertEquals("car's destination", "Acton MA", c4.getDestinationName());
        Assert.assertEquals("car's destinaton track", "Acton Yard", c4.getDestinationTrackName());
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
        Assert.assertEquals("car's location Acton", "Acton MA", c3.getLocationName());
        Assert.assertEquals("car's location Acton", "Acton Interchange", c3.getTrackName());
        // as of 5/4/2011 the car's destination is set to null, but the final destination continues to exist
        Assert.assertEquals("car's destination", "", c3.getDestinationName());
        Assert.assertEquals("car's destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("car's final destinaton", essex, c3.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", essexSpur2, c3.getFinalDestinationTrack());
        Assert.assertEquals("car's load", "L", c3.getLoadName());

        Assert.assertEquals("car's location Acton", "Acton MA", c4.getLocationName());
        Assert.assertEquals("car's location Acton", "Acton Yard", c4.getTrackName());
        Assert.assertEquals("car's destination", "", c4.getDestinationName());
        Assert.assertEquals("car's destination track", "", c4.getDestinationTrackName());

        // Place a maximum length restriction on the train
        Route route = actonToBedfordTrain.getRoute();
        RouteLocation rlActon = route.getDepartsRouteLocation();
        rlActon.setMaxTrainLength(c3.getTotalLength());
        actonToBedfordTrain.build();

        Assert.assertEquals("car's destination", "Bedford MA", c3.getDestinationName());
        Assert.assertEquals("car's destinaton track", "Bedford Interchange", c3.getDestinationTrackName());
        Assert.assertEquals("car's final destinaton", essex, c3.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", essexSpur2, c3.getFinalDestinationTrack());

        Assert.assertEquals("car's destination", "", c4.getDestinationName());
        Assert.assertEquals("car's destinaton track", "", c4.getDestinationTrackName());
        Assert.assertEquals("car's final destinaton", holden, c4.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", null, c4.getFinalDestinationTrack());

        actonToBedfordTrain.reset();

        // restore
        rlActon.setMaxTrainLength(Setup.getMaxTrainLength());

        actonToBedfordTrain.build();
        Assert.assertTrue(actonToBedfordTrain.isBuilt());
        actonToBedfordTrain.terminate();

        // confirm cars have moved
        Assert.assertEquals("car's location Bedford", "Bedford MA", c3.getLocationName());
        Assert.assertEquals("car's location Bedford", "Bedford Interchange", c3.getTrackName());
        Assert.assertEquals("car's destination", "", c3.getDestinationName());
        Assert.assertEquals("car's destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("car's load", "L", c3.getLoadName());
        Assert.assertEquals("car's final destinaton", essex, c3.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", essexSpur2, c3.getFinalDestinationTrack());

        Assert.assertEquals("car's location Bedford", "Bedford MA", c4.getLocationName());
        Assert.assertEquals("car's location Bedford", "Bedford Siding 1", c4.getTrackName());
        Assert.assertEquals("car's destination", "", c4.getDestinationName());
        Assert.assertEquals("car's destination track", "", c4.getDestinationTrackName());

        bedfordToClintonTrain.build();
        Assert.assertTrue(bedfordToClintonTrain.isBuilt());
        bedfordToClintonTrain.terminate();

        // confirm cars have moved
        Assert.assertEquals("car's location Clinton", "Clinton MA", c3.getLocationName());
        Assert.assertEquals("car's location Clinton", "Clinton Interchange", c3.getTrackName());
        Assert.assertEquals("car's destination", "", c3.getDestinationName());
        Assert.assertEquals("car's destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("car's load", "L", c3.getLoadName());
        Assert.assertEquals("car's final destinaton", essex, c3.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", essexSpur2, c3.getFinalDestinationTrack());

        Assert.assertEquals("car's location Clinton", "Clinton MA", c4.getLocationName());
        Assert.assertEquals("car's location Clinton", "Clinton Siding 1", c4.getTrackName());
        Assert.assertEquals("car's destination", "", c4.getDestinationName());
        Assert.assertEquals("car's destination track", "", c4.getDestinationTrackName());

        clintonToDanburyTrain.build();
        Assert.assertTrue(clintonToDanburyTrain.isBuilt());
        clintonToDanburyTrain.terminate();

        // confirm cars have moved
        Assert.assertEquals("car's location Danbury", "Danbury MA", c3.getLocationName());
        Assert.assertEquals("car's location Danbury", "Danbury Interchange", c3.getTrackName());
        Assert.assertEquals("car's destination", "", c3.getDestinationName());
        Assert.assertEquals("car's destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("car's load", "L", c3.getLoadName());
        Assert.assertEquals("car's final destinaton", essex, c3.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", essexSpur2, c3.getFinalDestinationTrack());

        Assert.assertEquals("car's location Danbury", "Danbury MA", c4.getLocationName());
        Assert.assertEquals("car's location Danbury", "Danbury Siding 1", c4.getTrackName());
        Assert.assertEquals("car's destination", "", c4.getDestinationName());
        Assert.assertEquals("car's destination track", "", c4.getDestinationTrackName());

        danburyToEssexTrain.build();
        Assert.assertTrue(danburyToEssexTrain.isBuilt());
        danburyToEssexTrain.terminate();

        // confirm cars have moved car has arrived at final destination Essex
        Assert.assertEquals("car's location Essex", "Essex MA", c3.getLocationName());
        Assert.assertEquals("car's location Essex", "Essex Siding 2", c3.getTrackName());
        Assert.assertEquals("car's destination", "", c3.getDestinationName());
        Assert.assertEquals("car's destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("car's load", "E", c3.getLoadName());
        // car when empty must return to Foxboro
        Assert.assertEquals("car's final destinaton", Foxboro, c3.getFinalDestination());
        Assert.assertEquals("car's final destinaton track", null, c3.getFinalDestinationTrack());

        Assert.assertEquals("car's location Essex", "Essex MA", c4.getLocationName());
        Assert.assertEquals("car's location Essex", "Essex Siding 1", c4.getTrackName());
        Assert.assertEquals("car's destination", "", c4.getDestinationName());
        Assert.assertEquals("car's destination track", "", c4.getDestinationTrackName());

        essexToFoxboroTrain.build();
        Assert.assertTrue(essexToFoxboroTrain.isBuilt());
        essexToFoxboroTrain.terminate();

        // confirm cars have moved
        Assert.assertEquals("car's location Foxboro", "Foxboro MA", c3.getLocationName());
        Assert.assertEquals("car's location Foxboro", "Foxboro Siding 1", c3.getTrackName());
        Assert.assertEquals("car's destination", "", c3.getDestinationName());
        Assert.assertEquals("car's destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("car's load", "L", c3.getLoadName());

        Assert.assertEquals("car's location Foxboro", "Foxboro MA", c4.getLocationName());
        Assert.assertEquals("car's location Foxboro", "Foxboro Siding 2", c4.getTrackName());
        Assert.assertEquals("car's destination", "", c4.getDestinationName());
        Assert.assertEquals("car's destination track", "", c4.getDestinationTrackName());

    }
    
    /**
     * Test alternate track when routing with trains
     */
    public void testRoutingWithTrainsAlternateTrack() {
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);

        loadLocationsTrainsAndCars();

        List<Train> trains = tmanager.getTrainsByNameList();
        Assert.assertEquals("confirm number of trains", 7, trains.size());

        Train actonTrain = tmanager.getTrainByName("Acton Local");
        Train actonToBedfordTrain = tmanager.getTrainByName("Acton to Bedford");

        // cars are 40' long
        Car c3 = cmanager.getByRoadAndNumber("BA", "3");
        Car c4 = cmanager.getByRoadAndNumber("BB", "4");
        Car c5 = cmanager.getByRoadAndNumber("BC", "5");
        Car c6 = cmanager.getByRoadAndNumber("BD", "6");
        
        // adjust move counts so the car placement order is always the same
        c4.setMoves(100);
        c5.setMoves(200);
        c6.setMoves(300);

        Location acton = lmanager.getLocationByName("Acton MA");
        Track actonSpur1 = acton.getTrackByName("Acton Siding 1", null);
        Track actonSpur2 = acton.getTrackByName("Acton Siding 2", null);
        Track actonYard = acton.getTrackByName("Acton Yard", null);
        Track actonInterchange = acton.getTrackByName("Acton Interchange", null);
        
        Location bedford = lmanager.getLocationByName("Bedford MA");
        Track bedfordSpur2 = bedford.getTrackByName("Bedford Siding 2", null);
        Track bedfordYard = bedford.getTrackByName("Bedford Yard", null);
        Track bedfordInterchange = bedford.getTrackByName("Bedford Interchange", null);

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
        
        // now try moving cars using the Acton to Bedford train
        actonToBedfordTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", actonSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c6.getDestinationTrack());
        
        // now allow regular trains to use the alternate yard track
        actonYard.setTrainDirections(Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH);
        
        actonToBedfordTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", actonSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonYard, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonYard, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonYard, c6.getDestinationTrack());        
        
        actonToBedfordTrain.reset();
        // send the cars to Bedford
        c3.setFinalDestination(bedford);
        c3.setFinalDestinationTrack(bedfordSpur2);
        c4.setFinalDestination(bedford);
        c4.setFinalDestinationTrack(bedfordSpur2);
        c5.setFinalDestination(bedford);
        c5.setFinalDestinationTrack(bedfordSpur2);
        c6.setFinalDestination(bedford);
        c6.setFinalDestinationTrack(bedfordSpur2);
        
        actonToBedfordTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", bedfordSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", bedfordSpur2, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", bedfordSpur2, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", bedfordSpur2, c6.getDestinationTrack());
        
        // now reduce spur 2 length
        bedfordSpur2.setLength(80); // only one 40' can fit
        
        actonToBedfordTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", bedfordSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", bedfordYard, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", bedfordYard, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", bedfordYard, c6.getDestinationTrack());
        
        // now try to limit length of the train
        Route routeB = actonToBedfordTrain.getRoute();
        RouteLocation rlB = routeB.getDepartsRouteLocation();
        rlB.setMaxTrainLength(100); // train length is ignored for a local
        
        actonToBedfordTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", bedfordSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", bedfordYard, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c6.getDestinationTrack());
        
        rlB.setMaxTrainLength(400); // restore
        actonToBedfordTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", bedfordSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", bedfordYard, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", bedfordYard, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", bedfordYard, c6.getDestinationTrack());
        
        // now disallow the use of the alternate yard track
        bedfordYard.setTrainDirections(0);
        
        actonToBedfordTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", bedfordSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", null, c6.getDestinationTrack());
        
        // change departure track to staging, router will attempt to find a way to get car out of staging
        bedfordYard.setTrainDirections(Track.NORTH); 
        actonSpur1.setTrackType(Track.STAGING);
        
        actonToBedfordTrain.build();
        // confirm that car destinations     
        Assert.assertEquals("car's destination track", bedfordSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", bedfordYard, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", bedfordYard, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", bedfordYard, c6.getDestinationTrack());
        
        // now disallow the use of the alternate yard track
        bedfordYard.setTrainDirections(0);
        
        actonToBedfordTrain.build();
        // confirm that car destinations
        // note that the Router has two interchange tracks to choose from bedford and acton, so results could change based on usage above
        Assert.assertEquals("car's destination track", bedfordSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car's destination track", bedfordInterchange, c4.getDestinationTrack());
        Assert.assertEquals("car's destination track", actonInterchange, c5.getDestinationTrack());
        Assert.assertEquals("car's destination track", bedfordInterchange, c6.getDestinationTrack());
             
    }

    /*
     * This test creates 4 schedules, and each schedule only has one item.
     * Two cars are used a boxcar and a flat. They both start with a load of
     * "Food". They should be routed to the correct schedule that is demanding
     * the car type and load.
     */
    public void testRoutingWithSimpleSchedules() {
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);

        loadLocationsTrainsAndCars();

        List<Train> trains = tmanager.getTrainsByNameList();
        Assert.assertEquals("confirm number of trains", 7, trains.size());

        Train actonTrain = tmanager.getTrainByName("Acton Local");
        Train actonToBedfordTrain = tmanager.getTrainByName("Acton to Bedford");
        Train bedfordToClintonTrain = tmanager.getTrainByName("Bedford to Clinton");
        Train clintonToDanburyTrain = tmanager.getTrainByName("Clinton to Danbury");
        Train danburyToEssexTrain = tmanager.getTrainByName("Danbury to Essex");
        Train essexToFoxboroTrain = tmanager.getTrainByName("Essex to Foxboro");

        Car c3 = cmanager.getByRoadAndNumber("BA", "3");
        Car c4 = cmanager.getByRoadAndNumber("BB", "4");

        Location acton = lmanager.getLocationByName("Acton MA");
        Location Clinton = lmanager.getLocationByName("Clinton MA");
        Location danbury = lmanager.getLocationByName("Danbury MA");
        Location essex = lmanager.getLocationByName("Essex MA");
        Location Foxboro = lmanager.getLocationByName("Foxboro MA");

        Track actonSpur1 = acton.getTrackByName("Acton Siding 1", Track.SPUR);
        Track clintonSpur1 = Clinton.getTrackByName("Clinton Siding 1", Track.SPUR);
        Track danburySpur1 = danbury.getTrackByName("Danbury Siding 1", Track.SPUR);
        Track danburySpur2 = danbury.getTrackByName("Danbury Siding 2", Track.SPUR);
        Track essexSpur1 = essex.getTrackByName("Essex Siding 1", Track.SPUR);
        Track essexSpur2 = essex.getTrackByName("Essex Siding 2", Track.SPUR);
        Track foxboroSpur1 = Foxboro.getTrackByName("Foxboro Siding 1", Track.SPUR);

        // set the depart track Acton to service by local train only
        actonSpur1.setTrainDirections(0);

        // create schedules
        ScheduleManager scheduleManager = InstanceManager.getDefault(ScheduleManager.class);
        Schedule schA = scheduleManager.newSchedule("Schedule A");
        ScheduleItem schAItem1 = schA.addItem("Boxcar");
        schAItem1.setReceiveLoadName("Food");
        schAItem1.setShipLoadName("Metal");
        schAItem1.setDestination(danbury);
        schAItem1.setDestinationTrack(danburySpur2);

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
        danburySpur1.setScheduleId(schB.getId());
        danburySpur2.setScheduleId(schC.getId());
        essexSpur1.setScheduleId(schD.getId());
        essexSpur2.setScheduleId(schA.getId());
        clintonSpur1.setScheduleId(schA.getId());

        // bias track
        essexSpur2.setMoves(0);
        clintonSpur1.setMoves(1);
        danburySpur2.setMoves(50);

        // place cars
        Assert.assertEquals("Place car", Track.OKAY, c3.setLocation(acton, actonSpur1));
        Assert.assertEquals("Place car", Track.OKAY, c4.setLocation(acton, actonSpur1));

        // c3 (BA 3) is a Boxcar
        c3.setLoadName("Food");	// Track Essex Siding 2 schedule is demanding this car
        c3.setReturnWhenEmptyDestination(Foxboro);

        // c4 (BB 4) is a Flat
        c4.setLoadName("Food");	// Track Danbury Siding 1 schedule is demanding this car

        // build train
        // Car c3 should be routed to Essex using 5 trains
        actonTrain.build();
        Assert.assertTrue("Acton train built", actonTrain.isBuilt());

        // check car destinations
        Assert.assertEquals("Car BA 3 destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Acton Interchange", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Essex MA", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Essex Siding 2", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "Acton MA", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Acton Interchange", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Danbury MA", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Danbury Siding 1", c4.getFinalDestinationTrackName());

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

        Assert.assertEquals("Car BA 3 destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Acton Interchange", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Clinton MA", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Clinton Siding 1", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "Acton MA", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Acton Interchange", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Danbury MA", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Danbury Siding 1", c4.getFinalDestinationTrackName());

        // check next loads
        //Assert.assertEquals("Car BA 3 load","Metal", c3.getNextLoad());
        //Assert.assertEquals("Car BB 4 load","Junk", c4.getNextLoad());
        actonTrain.terminate();

        // check destinations
        Assert.assertEquals("Car BA 3 destination", "", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Clinton MA", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Clinton Siding 1", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Danbury MA", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Danbury Siding 1", c4.getFinalDestinationTrackName());

        // check load
        Assert.assertEquals("Car BA 3 load", "Food", c3.getLoadName());
        Assert.assertEquals("Car BB 4 load", "Food", c4.getLoadName());

        // check next loads
        Assert.assertEquals("Car BA 3 load", "", c3.getNextLoadName());
        Assert.assertEquals("Car BB 4 load", "", c4.getNextLoadName());

        // check car's location
        Assert.assertEquals("Car BA 3 location", "Acton Interchange", c3.getTrackName());
        Assert.assertEquals("Car BB 4 location", "Acton Interchange", c4.getTrackName());

        actonToBedfordTrain.build();
        actonToBedfordTrain.terminate();

        // check destinations
        Assert.assertEquals("Car BA 3 destination", "", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "", c3.getDestinationTrackName());
        // schedule at Clinton (schedule A) forwards car BA 3 to Danbury, load Metal
        Assert.assertEquals("Car BA 3 final destination", "Clinton MA", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Clinton Siding 1", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Danbury MA", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Danbury Siding 1", c4.getFinalDestinationTrackName());

        // check load
        Assert.assertEquals("Car BA 3 load", "Food", c3.getLoadName());
        Assert.assertEquals("Car BB 4 load", "Food", c4.getLoadName());

        // check next loads
        Assert.assertEquals("Car BA 3 load", "", c3.getNextLoadName());
        Assert.assertEquals("Car BB 4 load", "", c4.getNextLoadName());

        bedfordToClintonTrain.build();
        bedfordToClintonTrain.terminate();

        // check destinations
        Assert.assertEquals("Car BA 3 destination", "", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "", c3.getDestinationTrackName());
        // schedule at Danbury (schedule C) forwards car BA 3 to Essex, no track specified, load Screws
        // schedule at Clinton (schedule A) forwards car BA 3 to Danbury, load Metal
        Assert.assertEquals("Car BA 3 final destination", "Danbury MA", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Danbury Siding 2", c3.getFinalDestinationTrackName());
        // schedule at Danbury (schedule B) forwards car BB 4 to Foxboro load Junk
        Assert.assertEquals("Car BB 4 destination", "", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Danbury MA", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Danbury Siding 1", c4.getFinalDestinationTrackName());

        // check load
        Assert.assertEquals("Car BA 3 load", "Metal", c3.getLoadName());
        Assert.assertEquals("Car BB 4 load", "Food", c4.getLoadName());

        // check next loads
        Assert.assertEquals("Car BA 3 load", "", c3.getNextLoadName());
        Assert.assertEquals("Car BB 4 load", "", c4.getNextLoadName());

        // check car's location
        Assert.assertEquals("Car BA 3 location", "Clinton Siding 1", c3.getTrackName());
        Assert.assertEquals("Car BB 4 location", "Clinton Interchange", c4.getTrackName());

        clintonToDanburyTrain.build();
        clintonToDanburyTrain.terminate();

        // Train has arrived at Danbury, check destinations
        // schedule at Danbury (schedule C) forwards car BA 3 to Essex, no track specified, load Screws
        Assert.assertEquals("Car BA 3 destination", "", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Essex MA", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());
        // schedule at Danbury (schedule B) forward car BB 4 to Foxboro Siding 1.
        Assert.assertEquals("Car BB 4 destination", "", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Foxboro MA", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Foxboro Siding 1", c4.getFinalDestinationTrackName());

        // check load
        Assert.assertEquals("Car BA 3 load", "Screws", c3.getLoadName());
        Assert.assertEquals("Car BB 4 load", "Junk", c4.getLoadName());

        // check next loads
        Assert.assertEquals("Car BA 3 load", "", c3.getNextLoadName());
        Assert.assertEquals("Car BB 4 load", "", c4.getNextLoadName());

        // check car's location
        Assert.assertEquals("Car BA 3 location", "Danbury Siding 2", c3.getTrackName());
        Assert.assertEquals("Car BB 4 location", "Danbury Siding 1", c4.getTrackName());

        danburyToEssexTrain.build();

        // schedule D at Essex Siding 1 is requesting load Screws, ship Nails  then forward car to Foxboro Siding 1
        Assert.assertEquals("Car BA 3 destination track", "Essex Siding 1", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Foxboro MA", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Foxboro Siding 1", c3.getFinalDestinationTrackName());

        // check next loads
        Assert.assertEquals("Car BA 3 load", "Nails", c3.getNextLoadName());
        Assert.assertEquals("Car BB 4 load", "", c4.getNextLoadName());

        // check next wait
        Assert.assertEquals("Car BA 3 has wait", 1, c3.getNextWait());
        Assert.assertEquals("Car BB 4 has no wait", 0, c4.getNextWait());

        danburyToEssexTrain.terminate();

        // Train has arrived at Essex, check destinations
        // schedule at Essex (schedule D) forwards car BA 3 to Foxboro Siding 1 load Nails, wait = 1
        Assert.assertEquals("Car BA 3 destination", "", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Foxboro MA", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Foxboro Siding 1", c3.getFinalDestinationTrackName());

        Assert.assertEquals("Car BB 4 destination", "", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Foxboro MA", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Foxboro Siding 1", c4.getFinalDestinationTrackName());

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
        Assert.assertEquals("Car BA 3 location", "Essex Siding 1", c3.getTrackName());
        Assert.assertEquals("Car BB 4 location", "Essex Interchange", c4.getTrackName());

        essexToFoxboroTrain.build();

        // confirm that only BB 4 is in train, BA 3 has wait = 1
        Assert.assertEquals("Car BA 3 not in train", null, c3.getTrain());
        Assert.assertEquals("Car BB 4 in train", essexToFoxboroTrain, c4.getTrain());
        essexToFoxboroTrain.terminate();

        // Train has arrived at Foxboro, check destinations
        Assert.assertEquals("Car BA 3 destination", "", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Foxboro MA", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Foxboro Siding 1", c3.getFinalDestinationTrackName());

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
        Assert.assertEquals("Car BA 3 location", "Essex Siding 1", c3.getTrackName());
        Assert.assertEquals("Car BB 4 location", "Foxboro Siding 1", c4.getTrackName());

        essexToFoxboroTrain.build();
        // confirm that only BA 3 is in train
        Assert.assertEquals("Car BA 3 in train", essexToFoxboroTrain, c3.getTrain());
        Assert.assertEquals("Car BB 4 not in train", null, c4.getTrain());
        essexToFoxboroTrain.terminate();

        // Train has arrived again at Foxboro, check destinations
        Assert.assertEquals("Car BA 3 destination", "", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "", c3.getDestinationTrackName());
        // Car BA 3 has return when empty destination of Foxboro, no track
        Assert.assertEquals("Car BA 3 final destination", "Foxboro MA", c3.getFinalDestinationName());
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
    public void testRoutingWithSchedules() {
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);

        loadLocationsTrainsAndCars();

        List<Train> trains = tmanager.getTrainsByNameList();
        Assert.assertEquals("confirm number of trains", 7, trains.size());

        Train actonTrain = tmanager.getTrainByName("Acton Local");
        Train actonToBedfordTrain = tmanager.getTrainByName("Acton to Bedford");
        Train bedfordToClintonTrain = tmanager.getTrainByName("Bedford to Clinton");

        Car c3 = cmanager.getByRoadAndNumber("BA", "3");
        Car c4 = cmanager.getByRoadAndNumber("BB", "4");
        Car c5 = cmanager.getByRoadAndNumber("BC", "5");
        Car c6 = cmanager.getByRoadAndNumber("BD", "6");

        Location acton = lmanager.getLocationByName("Acton MA");
        Location Clinton = lmanager.getLocationByName("Clinton MA");
        Location danbury = lmanager.getLocationByName("Danbury MA");
        Location essex = lmanager.getLocationByName("Essex MA");

        Track actonSpur1 = acton.getTrackByName("Acton Siding 1", Track.SPUR);
        Track clintonSpur1 = Clinton.getTrackByName("Clinton Siding 1", Track.SPUR);
        Track danburySpur1 = danbury.getTrackByName("Danbury Siding 1", Track.SPUR);
        Track danburySpur2 = danbury.getTrackByName("Danbury Siding 2", Track.SPUR);
        Track essexSpur1 = essex.getTrackByName("Essex Siding 1", Track.SPUR);
        Track essexSpur2 = essex.getTrackByName("Essex Siding 2", Track.SPUR);

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
        schAItem2.setDestination(danbury);
        schAItem2.setDestinationTrack(danburySpur2);
        ScheduleItem schAItem3 = schA.addItem("Boxcar");
        schAItem3.setReceiveLoadName("Boxes");
        schAItem3.setShipLoadName("Screws");
        schAItem3.setDestination(danbury);
        schAItem3.setDestinationTrack(danburySpur1);

        // Add schedule to tracks
        clintonSpur1.setScheduleId(schA.getId());
        clintonSpur1.setScheduleMode(Track.SEQUENTIAL);
        danburySpur1.setScheduleId("");
        danburySpur2.setScheduleId("");
        essexSpur1.setScheduleId("");
        essexSpur2.setScheduleId("");

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
        Assert.assertEquals("Car BA 3 destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Acton Interchange", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Clinton MA", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Clinton Siding 1", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "Acton MA", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Acton Interchange", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Clinton MA", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Clinton Siding 1", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 destination", "Acton MA", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Acton Interchange", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "Clinton MA", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "Clinton Siding 1", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 destination", "Acton MA", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Acton Interchange", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "Clinton MA", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "Clinton Siding 1", c6.getFinalDestinationTrackName());

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
        Assert.assertEquals("Car BA 3 destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Acton Yard", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "Acton MA", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Acton Interchange", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Clinton MA", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Clinton Siding 1", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 destination", "Acton MA", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Acton Interchange", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "Clinton MA", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "Clinton Siding 1", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 destination", "Acton MA", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Acton Interchange", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "Clinton MA", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "Clinton Siding 1", c6.getFinalDestinationTrackName());

        // check car schedule ids
        Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleItemId());
        Assert.assertEquals("Car BB 4 schedule id", schAItem2.getId(), c4.getScheduleItemId());
        Assert.assertEquals("Car BC 5 schedule id", schAItem3.getId(), c5.getScheduleItemId());
        Assert.assertEquals("Car BD 6 schedule id", schAItem1.getId(), c6.getScheduleItemId());

        actonTrain.terminate();
        // move the cars to Bedford
        actonToBedfordTrain.build();
        Assert.assertTrue("Bedford train built", actonToBedfordTrain.isBuilt());

        // check car destinations
        Assert.assertEquals("Car BA 3 destination", "Bedford MA", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Bedford Yard", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "Bedford MA", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Bedford Interchange", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Clinton MA", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Clinton Siding 1", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 destination", "Bedford MA", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Bedford Interchange", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "Clinton MA", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "Clinton Siding 1", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 destination", "Bedford MA", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Bedford Interchange", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "Clinton MA", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "Clinton Siding 1", c6.getFinalDestinationTrackName());

        // check car schedule ids
        Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleItemId());
        Assert.assertEquals("Car BB 4 schedule id", schAItem2.getId(), c4.getScheduleItemId());
        Assert.assertEquals("Car BC 5 schedule id", schAItem3.getId(), c5.getScheduleItemId());
        Assert.assertEquals("Car BD 6 schedule id", schAItem1.getId(), c6.getScheduleItemId());

        actonToBedfordTrain.terminate();
        // move the cars to Bedford
        bedfordToClintonTrain.build();
        Assert.assertTrue("Bedford train built", bedfordToClintonTrain.isBuilt());

        // check car destinations
        Assert.assertEquals("Car BA 3 destination", "Clinton MA", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Clinton Yard", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "Clinton MA", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Clinton Siding 1", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Danbury MA", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Danbury Siding 2", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 destination", "Clinton MA", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Clinton Siding 1", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "Danbury MA", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "Danbury Siding 1", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 destination", "Clinton MA", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Clinton Siding 1", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "Acton MA", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "Acton Siding 1", c6.getFinalDestinationTrackName());

        // check car schedule ids
        Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleItemId());
        Assert.assertEquals("Car BB 4 schedule id", "", c4.getScheduleItemId());
        Assert.assertEquals("Car BC 5 schedule id", "", c5.getScheduleItemId());
        Assert.assertEquals("Car BD 6 schedule id", "", c6.getScheduleItemId());

        bedfordToClintonTrain.terminate();

    }

    /*
     * Using the setup from the previous tests, use trains and schedules to move
     * cars. This test creates 1 schedule in match mode with multiple items.
     * Test uses car loads to activate schedule.
     */
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

        Location acton = lmanager.getLocationByName("Acton MA");
        Location Clinton = lmanager.getLocationByName("Clinton MA");
        Location danbury = lmanager.getLocationByName("Danbury MA");
        Location essex = lmanager.getLocationByName("Essex MA");

        Track actonSpur1 = acton.getTrackByName("Acton Siding 1", Track.SPUR);
        Track clintonSpur1 = Clinton.getTrackByName("Clinton Siding 1", Track.SPUR);
        Track danburySpur1 = danbury.getTrackByName("Danbury Siding 1", Track.SPUR);
        Track danburySpur2 = danbury.getTrackByName("Danbury Siding 2", Track.SPUR);
        Track essexSpur1 = essex.getTrackByName("Essex Siding 1", Track.SPUR);
        Track essexSpur2 = essex.getTrackByName("Essex Siding 2", Track.SPUR);

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
        schAItem2.setDestination(danbury);
        schAItem2.setDestinationTrack(danburySpur2);
        ScheduleItem schAItem3 = schA.addItem("Boxcar");
        schAItem3.setReceiveLoadName("Boxes");
        schAItem3.setShipLoadName("Screws");
        schAItem3.setDestination(essex);
        schAItem3.setDestinationTrack(essexSpur1);
        ScheduleItem schAItem4 = schA.addItem("Boxcar");
        schAItem4.setReceiveLoadName("Boxes");
        schAItem4.setShipLoadName("Bolts");
        schAItem4.setDestination(danbury);
        schAItem4.setDestinationTrack(danburySpur1);
        ScheduleItem schAItem5 = schA.addItem("Boxcar");
        schAItem5.setReceiveLoadName("");
        schAItem5.setShipLoadName("Nuts");
        schAItem5.setDestination(essex);
        schAItem5.setDestinationTrack(essexSpur2);

        // Add schedule to tracks
        clintonSpur1.setScheduleId("");
        essexSpur1.setScheduleId(schA.getId());
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
        Assert.assertEquals("Car BA 3 destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Acton Interchange", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Essex MA", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Essex Siding 1", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "Acton MA", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Acton Interchange", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Essex MA", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Essex Siding 1", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 destination", "Acton MA", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Acton Interchange", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "Essex MA", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "Essex Siding 1", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 destination", "Acton MA", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Acton Interchange", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "Essex MA", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "Essex Siding 1", c6.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 7 destination", "Acton MA", c7.getDestinationName());
        Assert.assertEquals("Car BD 7 destination track", "Acton Interchange", c7.getDestinationTrackName());
        Assert.assertEquals("Car BD 7 final destination", "Essex MA", c7.getFinalDestinationName());
        Assert.assertEquals("Car BD 7 final destination track", "Essex Siding 1", c7.getFinalDestinationTrackName());

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

        Location acton = lmanager.getLocationByName("Acton MA");
        Location Bedford = lmanager.getLocationByName("Bedford MA");
        Location Clinton = lmanager.getLocationByName("Clinton MA");
        Location danbury = lmanager.getLocationByName("Danbury MA");
        Location essex = lmanager.getLocationByName("Essex MA");
        Location Foxboro = lmanager.getLocationByName("Foxboro MA");

        Track actonSpur1 = acton.getTrackByName("Acton Siding 1", Track.SPUR);
        Track bedfordSpur1 = Bedford.getTrackByName("Bedford Siding 1", Track.SPUR);
        Track clintonSpur1 = Clinton.getTrackByName("Clinton Siding 1", Track.SPUR);
        Track danburySpur1 = danbury.getTrackByName("Danbury Siding 1", Track.SPUR);
        Track danburySpur2 = danbury.getTrackByName("Danbury Siding 2", Track.SPUR);
        Track essexSpur1 = essex.getTrackByName("Essex Siding 1", Track.SPUR);
        //Track essexSpur2 = essex.getTrackByName("Essex Siding 2", Track.SIDING);
        Track foxboroSpur1 = Foxboro.getTrackByName("Foxboro Siding 1", Track.SPUR);

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
        schAItem2.setDestination(danbury);
        schAItem2.setDestinationTrack(danburySpur2);

        ScheduleItem schAItem3 = schA.addItem("Boxcar");
        schAItem3.setReceiveLoadName("Tools");
        schAItem3.setShipLoadName("Screws");
        schAItem3.setDestination(Bedford);
        schAItem3.setDestinationTrack(bedfordSpur1);

        ScheduleItem schAItem4 = schA.addItem("Boxcar");
        schAItem4.setReceiveLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName());
        schAItem4.setShipLoadName("Bolts");
        schAItem4.setDestination(danbury);
        schAItem4.setDestinationTrack(danburySpur1);

        ScheduleItem schAItem5 = schA.addItem("Boxcar");
        schAItem5.setReceiveLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultLoadName());
        schAItem5.setShipLoadName("Nuts");
        schAItem5.setDestination(Foxboro);
        schAItem5.setDestinationTrack(foxboroSpur1);

        // Add schedule to tracks
        clintonSpur1.setScheduleId("");
        essexSpur1.setScheduleId(schA.getId());
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
        Assert.assertEquals("Place car", Track.OKAY, c8.setLocation(danbury, danburySpur1));
        Assert.assertEquals("Place car", Track.OKAY, c9.setLocation(danbury, danburySpur1));

        // build train
        Train actonTrain = tmanager.getTrainByName("Acton Local");
        actonTrain.build();
        Assert.assertTrue("Acton train built", actonTrain.isBuilt());

        // check car destinations
        Assert.assertEquals("Car BA 3 destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Acton Interchange", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Essex MA", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 destination", "Acton MA", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Acton Interchange", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Essex MA", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Essex Siding 1", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 destination", "Acton MA", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Acton Interchange", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "Essex MA", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 destination", "Acton MA", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Acton Interchange", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "Essex MA", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "", c6.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 7 destination", "Acton MA", c7.getDestinationName());
        Assert.assertEquals("Car BA 7 destination track", "Acton Interchange", c7.getDestinationTrackName());
        Assert.assertEquals("Car BA 7 final destination", "Essex MA", c7.getFinalDestinationName());
        Assert.assertEquals("Car BA 7 final destination track", "Essex Siding 1", c7.getFinalDestinationTrackName());
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

        // move the cars to Bedford
        Train actonToBedfordTrain = tmanager.getTrainByName("Acton to Bedford");
        actonToBedfordTrain.build();
        Assert.assertTrue("Acton train built", actonToBedfordTrain.isBuilt());
        actonToBedfordTrain.terminate();

        // move the cars to Clinton
        Train bedfordToClintonTrain = tmanager.getTrainByName("Bedford to Clinton");
        bedfordToClintonTrain.build();
        Assert.assertTrue("Bedford train built", bedfordToClintonTrain.isBuilt());
        bedfordToClintonTrain.terminate();

        // move the cars to Danbury
        Train clintonToDanburyTrain = tmanager.getTrainByName("Clinton to Danbury");
        clintonToDanburyTrain.build();
        Assert.assertTrue("Clinton train built", clintonToDanburyTrain.isBuilt());
        clintonToDanburyTrain.terminate();

        // move the cars to Essex (number of moves is 8)
        Train danburyToEssexTrain = tmanager.getTrainByName("Danbury to Essex");
        danburyToEssexTrain.build();
        Assert.assertTrue("Danbury train built", danburyToEssexTrain.isBuilt());

        // check car destinations
        // BA 3 (Boxcar)
        Assert.assertEquals("Car BA 3 destination", "Essex MA", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Essex Siding 1", c3.getDestinationTrackName());
        // new final destination and load for car BA 3
        Assert.assertEquals("Car BA 3 final destination", "Bedford MA", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "Bedford Siding 1", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 3 final load", "Screws", c3.getNextLoadName());
        Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleItemId());
        // BB 4 (Flat)
        Assert.assertEquals("Car BB 4 destination", "Essex MA", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Essex Siding 1", c4.getDestinationTrackName());
        // new final destination and load for car BB 4
        Assert.assertEquals("Car BB 4 final destination", "Danbury MA", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Danbury Siding 2", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 final load", "Metal", c4.getNextLoadName());
        Assert.assertEquals("Car BB 4 schedule id", "", c4.getScheduleItemId());
        // BC 5 (Boxcar)
        Assert.assertEquals("Car BC 5 destination", "Essex MA", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Essex Siding 1", c5.getDestinationTrackName());
        // new final destination and load for car BC 5, same as BA 3
        Assert.assertEquals("Car BC 5 final destination", "Bedford MA", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "Bedford Siding 1", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 final load", "Screws", c5.getNextLoadName());
        Assert.assertEquals("Car BC 5 schedule id", "", c5.getScheduleItemId());
        // BD 6 (Boxcar) note second Boxcar
        Assert.assertEquals("Car BD 6 destination", "Essex MA", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Essex Siding 1", c6.getDestinationTrackName());
        // new final destination and load for car BD 6
        Assert.assertEquals("Car BD 6 final destination", "Danbury MA", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "Danbury Siding 1", c6.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 6 final load", "Bolts", c6.getNextLoadName());
        Assert.assertEquals("Car BD 6 schedule id", "", c6.getScheduleItemId());
        // BA 7 (Boxcar) note 3rd Boxcar
        Assert.assertEquals("Car BA 7 destination", "Essex MA", c7.getDestinationName());
        Assert.assertEquals("Car BA 7 destination track", "Essex Siding 1", c7.getDestinationTrackName());
        // new final destination and load for car BA 7
        Assert.assertEquals("Car BA 7 final destination", "Acton MA", c7.getFinalDestinationName());
        Assert.assertEquals("Car BA 7 final destination track", "Acton Siding 1", c7.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 7 final load", "Metal", c7.getNextLoadName());
        Assert.assertEquals("Car BA 7 schedule id", "", c7.getScheduleItemId());
        // BB 8 (Boxcar) at Danbury to be added to train
        Assert.assertEquals("Car BB 8 destination", "Essex MA", c8.getDestinationName());
        Assert.assertEquals("Car BB 8 destination track", "Essex Siding 1", c8.getDestinationTrackName());
        // Should match schedule item 16c3
        Assert.assertEquals("Car BB 8 final destination", "Bedford MA", c8.getFinalDestinationName());
        Assert.assertEquals("Car BB 8 final destination track", "Bedford Siding 1", c8.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 8 final load", "Screws", c8.getNextLoadName());
        Assert.assertEquals("Car BB 8 schedule id", "", c8.getScheduleItemId());
        // BB 9 (Boxcar) at Danbury to be added to train
        Assert.assertEquals("Car BC 9 destination", "Essex MA", c9.getDestinationName());
        Assert.assertEquals("Car BC 9 destination track", "Essex Siding 1", c9.getDestinationTrackName());
        // Should match schedule item 16c4
        Assert.assertEquals("Car BC 9 final destination", "Danbury MA", c9.getFinalDestinationName());
        Assert.assertEquals("Car BC 9 final destination track", "Danbury Siding 1", c9.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 9 final load", "Bolts", c9.getNextLoadName());
        Assert.assertEquals("Car BC 9 schedule id", "", c9.getScheduleItemId());

        // test reset, car final destinations should revert.
        danburyToEssexTrain.reset();

        Assert.assertEquals("Car BA 3 destination", "", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "Essex MA", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 3 next load", "", c3.getNextLoadName());
        Assert.assertEquals("Car BB 4 destination", "", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Essex MA", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "Essex Siding 1", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 destination", "", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "Essex MA", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 destination", "", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "Essex MA", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "", c6.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 7 destination", "", c7.getDestinationName());
        Assert.assertEquals("Car BA 7 destination track", "", c7.getDestinationTrackName());
        Assert.assertEquals("Car BA 7 final destination", "Essex MA", c7.getFinalDestinationName());
        Assert.assertEquals("Car BA 7 final destination track", "Essex Siding 1", c7.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 8 destination", "", c8.getDestinationName());
        Assert.assertEquals("Car BB 8 destination track", "", c8.getDestinationTrackName());
        Assert.assertEquals("Car BB 8 final destination", "", c8.getFinalDestinationName());
        Assert.assertEquals("Car BB 8 final destination track", "", c8.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 9 destination", "", c9.getDestinationName());
        Assert.assertEquals("Car BC 9 destination track", "", c9.getDestinationTrackName());
        Assert.assertEquals("Car BC 9 final destination", "", c9.getFinalDestinationName());
        Assert.assertEquals("Car BC 9 final destination track", "", c9.getFinalDestinationTrackName());

//		// try again
//		danburyToEssexTrain.build();
//		Assert.assertTrue("Bedford train built", danburyToEssexTrain.isBuilt());
//
//		// check car destinations
//		// BA 3 (Boxcar) this car's load and final destination is now different
//		Assert.assertEquals("Car BA 3 destination","Essex MA", c3.getDestinationName());
//		Assert.assertEquals("Car BA 3 destination track","Essex Siding 1", c3.getDestinationTrackName());
//		// new final destination and load for car BA 3
//		Assert.assertEquals("Car BA 3 final destination","Acton MA", c3.getFinalDestinationName());
//		Assert.assertEquals("Car BA 3 final destination track","Acton Siding 1", c3.getFinalDestinationTrackName());
//		Assert.assertEquals("Car BA 3 next load","Metal", c3.getNextLoadName());
//		Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleId());
//		// BB 4 (Flat) resets the match pointer so car BC 5 final destination and load is the same as last time
//		Assert.assertEquals("Car BB 4 destination","Essex MA", c4.getDestinationName());
//		Assert.assertEquals("Car BB 4 destination track","Essex Siding 1", c4.getDestinationTrackName());
//		// new final destination and load for car BB 4
//		Assert.assertEquals("Car BB 4 final destination","Danbury MA", c4.getFinalDestinationName());
//		Assert.assertEquals("Car BB 4 final destination track","Danbury Siding 2", c4.getFinalDestinationTrackName());
//		Assert.assertEquals("Car BB 4 next load","Metal", c4.getNextLoadName());
//		Assert.assertEquals("Car BB 4 schedule id", "", c4.getScheduleId());
//		// BC 5 (Boxcar)
//		Assert.assertEquals("Car BC 5 destination","Essex MA", c5.getDestinationName());
//		Assert.assertEquals("Car BC 5 destination track","Essex Siding 1", c5.getDestinationTrackName());
//		// new final destination and load for car BC 5, same as BA 3
//		Assert.assertEquals("Car BC 5 final destination","Bedford MA", c5.getFinalDestinationName());
//		Assert.assertEquals("Car BC 5 final destination track","Bedford Siding 1", c5.getFinalDestinationTrackName());
//		Assert.assertEquals("Car BC 5 next load","Screws", c5.getNextLoadName());
//		Assert.assertEquals("Car BC 5 schedule id", "", c5.getScheduleId());
//		// BD 6 (Boxcar) note second Boxcar
//		Assert.assertEquals("Car BD 6 destination","Essex MA", c6.getDestinationName());
//		Assert.assertEquals("Car BD 6 destination track","Essex Siding 1", c6.getDestinationTrackName());
//		// new final destination and load for car BD 6
//		Assert.assertEquals("Car BD 6 final destination","Danbury MA", c6.getFinalDestinationName());
//		Assert.assertEquals("Car BD 6 final destination track","Danbury Siding 1", c6.getFinalDestinationTrackName());
//		Assert.assertEquals("Car BC 6 next load", "Bolts", c6.getNextLoadName());
//		Assert.assertEquals("Car BD 6 schedule id", "", c6.getScheduleId());
//		// BA 7 (Boxcar) note 3rd Boxcar
//		Assert.assertEquals("Car BA 7 destination","Essex MA", c7.getDestinationName());
//		Assert.assertEquals("Car BA 7 destination track","Essex Siding 1", c7.getDestinationTrackName());
//		// new final destination and load for car BA 7
//		Assert.assertEquals("Car BA 7 final destination","Acton MA", c7.getFinalDestinationName());
//		Assert.assertEquals("Car BA 7 final destination track","Acton Siding 1", c7.getFinalDestinationTrackName());
//		Assert.assertEquals("Car BA 7 next load", "Metal", c7.getNextLoadName());
//		Assert.assertEquals("Car BA 7 schedule id", "", c7.getScheduleId());
//		// BB 8 (Boxcar) at Danbury to be added to train
//		Assert.assertEquals("Car BB 8 destination","Essex MA", c8.getDestinationName());
//		Assert.assertEquals("Car BB 8 destination track","Essex Siding 1", c8.getDestinationTrackName());
//		// Should match schedule item 16c3
//		Assert.assertEquals("Car BB 8 final destination","Bedford MA", c8.getFinalDestinationName());
//		Assert.assertEquals("Car BB 8 final destination track","Bedford Siding 1", c8.getFinalDestinationTrackName());
//		Assert.assertEquals("Car BB 8 next load", "Screws", c8.getNextLoadName());
//		Assert.assertEquals("Car BB 8 schedule id", "", c8.getScheduleId());
//		// BB 9 (Boxcar) at Danbury to be added to train
//		Assert.assertEquals("Car BC 9 destination","Essex MA", c9.getDestinationName());
//		Assert.assertEquals("Car BC 9 destination track","Essex Siding 1", c9.getDestinationTrackName());
//		// Should match schedule item 16c4
//		Assert.assertEquals("Car BC 9 final destination","Danbury MA", c9.getFinalDestinationName());
//		Assert.assertEquals("Car BC 9 final destination track","Danbury Siding 1", c9.getFinalDestinationTrackName());
//		Assert.assertEquals("Car BC 9 next load", "Bolts", c9.getNextLoadName());
//		Assert.assertEquals("Car BC 9 schedule id", "", c9.getScheduleId());
//
//		danburyToEssexTrain.terminate();
//
//		// check car destinations
//		// BA 3 (Boxcar)
//		Assert.assertEquals("Car BA 3 destination","", c3.getDestinationName());
//		Assert.assertEquals("Car BA 3 destination track","", c3.getDestinationTrackName());
//		// new final destination and load for car BA 3
//		Assert.assertEquals("Car BA 3 final destination","Acton MA", c3.getFinalDestinationName());
//		Assert.assertEquals("Car BA 3 final destination track","Acton Siding 1", c3.getFinalDestinationTrackName());
//		Assert.assertEquals("Car BA 3 load","Metal", c3.getLoadName());
//		Assert.assertEquals("Car BA 3 next load","", c3.getNextLoadName());
//		Assert.assertEquals("Car BA 3 schedule id", "", c3.getScheduleId());
//		// BB 4 (Flat)
//		Assert.assertEquals("Car BB 4 destination","", c4.getDestinationName());
//		Assert.assertEquals("Car BB 4 destination track","", c4.getDestinationTrackName());
//		// new final destination and load for car BB 4
//		Assert.assertEquals("Car BB 4 final destination","Danbury MA", c4.getFinalDestinationName());
//		Assert.assertEquals("Car BB 4 final destination track","Danbury Siding 2", c4.getFinalDestinationTrackName());
//		Assert.assertEquals("Car BB 4 load","Metal", c4.getLoadName());
//		Assert.assertEquals("Car BB 4 next load","", c4.getNextLoadName());
//		Assert.assertEquals("Car BB 4 schedule id", "", c4.getScheduleId());
//		// BC 5 (Boxcar)
//		Assert.assertEquals("Car BC 5 destination","", c5.getDestinationName());
//		Assert.assertEquals("Car BC 5 destination track","", c5.getDestinationTrackName());
//		// new final destination and load for car BC 5, same as BA 3
//		Assert.assertEquals("Car BC 5 final destination","Bedford MA", c5.getFinalDestinationName());
//		Assert.assertEquals("Car BC 5 final destination track","Bedford Siding 1", c5.getFinalDestinationTrackName());
//		Assert.assertEquals("Car BC 5 load","Screws", c5.getLoadName());
//		Assert.assertEquals("Car BC 5 next load","", c5.getNextLoadName());
//		Assert.assertEquals("Car BC 5 schedule id", "", c5.getScheduleId());
//		// BD 6 (Boxcar) note second Boxcar
//		Assert.assertEquals("Car BD 6 destination","", c6.getDestinationName());
//		Assert.assertEquals("Car BD 6 destination track","", c6.getDestinationTrackName());
//		// new final destination and load for car BD 6
//		Assert.assertEquals("Car BD 6 final destination","Danbury MA", c6.getFinalDestinationName());
//		Assert.assertEquals("Car BD 6 final destination track","Danbury Siding 1", c6.getFinalDestinationTrackName());
//		Assert.assertEquals("Car BC 6 load", "Bolts", c6.getLoadName());
//		Assert.assertEquals("Car BC 6 next load", "", c6.getNextLoadName());
//		Assert.assertEquals("Car BD 6 schedule id", "", c6.getScheduleId());
//		// BA 7 (Boxcar) note 3rd Boxcar
//		Assert.assertEquals("Car BA 7 destination","", c7.getDestinationName());
//		Assert.assertEquals("Car BA 7 destination track","", c7.getDestinationTrackName());
//		// new final destination and load for car BA 7
//		Assert.assertEquals("Car BA 7 final destination","Acton MA", c7.getFinalDestinationName());
//		Assert.assertEquals("Car BA 7 final destination track","Acton Siding 1", c7.getFinalDestinationTrackName());
//		Assert.assertEquals("Car BA 7 load", "Metal", c7.getLoadName());
//		Assert.assertEquals("Car BA 7 next load", "", c7.getNextLoadName());
//		Assert.assertEquals("Car BA 7 schedule id", "", c7.getScheduleId());
//		// BB 8 (Boxcar)
//		Assert.assertEquals("Car BB 8 destination","", c8.getDestinationName());
//		Assert.assertEquals("Car BB 8 destination track","", c8.getDestinationTrackName());
//		// Should match schedule item 16c3
//		Assert.assertEquals("Car BB 8 final destination","Bedford MA", c8.getFinalDestinationName());
//		Assert.assertEquals("Car BB 8 final destination track","Bedford Siding 1", c8.getFinalDestinationTrackName());
//		Assert.assertEquals("Car BB 8 load", "Screws", c8.getLoadName());
//		Assert.assertEquals("Car BB 8 next load", "", c8.getNextLoadName());
//		Assert.assertEquals("Car BB 8 schedule id", "", c8.getScheduleId());
//		// BB 9 (Boxcar)
//		Assert.assertEquals("Car BC 9 destination","", c9.getDestinationName());
//		Assert.assertEquals("Car BC 9 destination track","", c9.getDestinationTrackName());
//		// Should match schedule item 16c4
//		Assert.assertEquals("Car BC 9 final destination","Danbury MA", c9.getFinalDestinationName());
//		Assert.assertEquals("Car BC 9 final destination track","Danbury Siding 1", c9.getFinalDestinationTrackName());
//		Assert.assertEquals("Car BC 9 load", "Bolts", c9.getLoadName());
//		Assert.assertEquals("Car BC 9 next load", "", c9.getNextLoadName());
//		Assert.assertEquals("Car BC 9 schedule id", "", c9.getScheduleId());
    }

    /* This test confirms that schedules can be linked together.
     * Note that there are schedules at Essex that are still active
     * but not reachable because the Clinton to Danbury train is
     * removed as part of this test.
     * has 5 tracks, 3 sidings, yard, and an interchange track.
     *
     */
    public void testRoutingWithSchedulesLocal() {
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

        // c3 (BA 3) is a Boxcar
        c3.setLoadName("Cardboard");

        // c4 (BB 4) is a Flat
        c4.setLoadName("Trucks");

        // c5 (BC 5) is a Boxcar
        c5.setLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName());

        // c6 (BD 6) is a Boxcar
        c6.setLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName());

        // c7 (7) is a Boxcar
        c7.setLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName());

        // c8 (8) is a Boxcar
        c8.setLoadName("Trucks");

        // c9 (9) is a Boxcar
        c9.setLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName());

        Location acton = lmanager.getLocationByName("Acton MA");
        Location Bedford = lmanager.getLocationByName("Bedford MA");
        Location Clinton = lmanager.getLocationByName("Clinton MA");
        Track clintonSpur1 = Clinton.getTrackByName("Clinton Siding 1", Track.SPUR);

        Track actonSpur1 = acton.getTrackByName("Acton Siding 1", Track.SPUR);
        actonSpur1.setTrainDirections(Track.NORTH + Track.SOUTH);
        Track actonSpur2 = acton.getTrackByName("Acton Siding 2", Track.SPUR);
        Track AS3 = acton.addTrack("Acton Siding 3", Track.SPUR);
        AS3.setLength(300);
        Track actonYard = acton.getTrackByName("Acton Yard", Track.YARD);
        Track actonInterchange = acton.getTrackByName("Acton Interchange", Track.INTERCHANGE);

        // create schedules
        ScheduleManager scheduleManager = InstanceManager.getDefault(ScheduleManager.class);
        Schedule schA = scheduleManager.newSchedule("Schedule Action");
        ScheduleItem schAItem1 = schA.addItem("Boxcar");
        schAItem1.setReceiveLoadName("Cardboard");
        schAItem1.setShipLoadName("Scrap");
        ScheduleItem schAItem2 = schA.addItem("Gon");
        schAItem2.setReceiveLoadName("Trucks");
        schAItem2.setShipLoadName("Tires");
        schAItem2.setDestination(Bedford);
        ScheduleItem schAItem3 = schA.addItem("Boxcar");
        schAItem3.setReceiveLoadName("Trucks");
        schAItem3.setShipLoadName("Wire");
        schAItem3.setDestination(Clinton);
        schAItem3.setDestinationTrack(clintonSpur1);
        ScheduleItem schAItem4 = schA.addItem("Flat");
        schAItem4.setReceiveLoadName("Trucks");
        schAItem4.setShipLoadName("Coils");
        schAItem4.setDestination(Bedford);
        ScheduleItem schAItem5 = schA.addItem("Flat");
        schAItem5.setReceiveLoadName("Coils");
        schAItem5.setShipLoadName("Trucks");
        schAItem5.setDestination(Bedford);
        ScheduleItem schAItem6 = schA.addItem("Boxcar");
        schAItem6.setReceiveLoadName("Scrap");
        schAItem6.setShipLoadName("E");
        ScheduleItem schAItem7 = schA.addItem("Boxcar");
        schAItem7.setReceiveLoadName("Wire");
        schAItem7.setShipLoadName("L");

        // add schedules to tracks
        actonSpur1.setScheduleId(schA.getId());
        actonSpur1.setScheduleMode(Track.SEQUENTIAL);
        actonSpur2.setScheduleId(schA.getId());
        actonSpur2.setScheduleMode(Track.SEQUENTIAL);
        AS3.setScheduleId(schA.getId());
        // put Action Siding 3 into match mode
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
        Train actonToBedfordTrain = tmanager.getTrainByName("Acton to Bedford");
        Route actonToBeford = actonToBedfordTrain.getRoute();
        RouteLocation rl = actonToBeford.getDepartsRouteLocation();
        RouteLocation rd = actonToBeford.getLastLocationByName("Bedford MA");
        // increase the number of moves so all cars are used
        rl.setMaxCarMoves(10);
        rd.setMaxCarMoves(10);
        // kill the Clinton to Danbury train
        Train clintonToDanburyTrain = tmanager.getTrainByName("Clinton to Danbury");
        tmanager.deregister(clintonToDanburyTrain);

        actonToBedfordTrain.build();
        Assert.assertTrue("Acton train built", actonToBedfordTrain.isBuilt());

        // check cars
        Assert.assertEquals("Car BA 3 destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Acton Siding 2", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 3 load", "Cardboard", c3.getLoadName());
        Assert.assertEquals("Car BA 3 next load", "Scrap", c3.getNextLoadName());

        Assert.assertEquals("Car BB 4 destination", "Acton MA", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Acton Siding 3", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "Bedford MA", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 load", "Trucks", c4.getLoadName());
        Assert.assertEquals("Car BB 4 next load", "Coils", c4.getNextLoadName());

        Assert.assertEquals("Car BC 5 destination", "Bedford MA", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Bedford Siding 1", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 load", "E", c5.getLoadName());
        Assert.assertEquals("Car BC 5 next load", "", c5.getNextLoadName());

        Assert.assertEquals("Car BD 6 destination", "Bedford MA", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Bedford Siding 2", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "", c6.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 load", "E", c6.getLoadName());
        Assert.assertEquals("Car BD 6 next load", "", c6.getNextLoadName());

        Assert.assertEquals("Car BA 7 destination", "Bedford MA", c7.getDestinationName());
        Assert.assertEquals("Car BA 7 destination track", "Bedford Yard", c7.getDestinationTrackName());
        Assert.assertEquals("Car BA 7 final destination", "", c7.getFinalDestinationName());
        Assert.assertEquals("Car BA 7 final destination track", "", c7.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 7 load", "E", c7.getLoadName());
        Assert.assertEquals("Car BA 7 next load", "", c7.getNextLoadName());

        Assert.assertEquals("Car BB 8 destination", "Acton MA", c8.getDestinationName());
        Assert.assertEquals("Car BB 8 destination track", "Acton Siding 3", c8.getDestinationTrackName());
        Assert.assertEquals("Car BB 8 final destination", "Clinton MA", c8.getFinalDestinationName());
        Assert.assertEquals("Car BB 8 final destination track", "Clinton Siding 1", c8.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 8 load", "Trucks", c8.getLoadName());
        Assert.assertEquals("Car BB 8 next load", "Wire", c8.getNextLoadName());

        Assert.assertEquals("Car BC 9 destination", "Bedford MA", c9.getDestinationName());
        Assert.assertEquals("Car BC 9 destination track", "Bedford Interchange", c9.getDestinationTrackName());
        Assert.assertEquals("Car BC 9 final destination", "", c9.getFinalDestinationName());
        Assert.assertEquals("Car BC 9 final destination track", "", c9.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 9 load", "E", c9.getLoadName());
        Assert.assertEquals("Car BC 9 next load", "", c9.getNextLoadName());

        actonToBedfordTrain.terminate();

        // Build train
        Train BedfordToActonTrain = tmanager.newTrain("BedfordToActonToBedford");
        Route BedfordToActon = InstanceManager.getDefault(RouteManager.class).newRoute("BedfordToActonToBedford");
        RouteLocation rlB2 = BedfordToActon.addLocation(Bedford);
        RouteLocation rlA2 = BedfordToActon.addLocation(acton);
        RouteLocation rlB3 = BedfordToActon.addLocation(Bedford);
        // increase the number of moves so all cars are used
        rlB2.setMaxCarMoves(10);
        rlA2.setMaxCarMoves(10);
        rlB3.setMaxCarMoves(10);
        BedfordToActonTrain.setRoute(BedfordToActon);

        BedfordToActonTrain.build();

        // check cars
        Assert.assertEquals("Car BA 3 destination", "Acton MA", c3.getDestinationName());
        Assert.assertEquals("Car BA 3 destination track", "Acton Siding 3", c3.getDestinationTrackName());
        Assert.assertEquals("Car BA 3 final destination", "", c3.getFinalDestinationName());
        Assert.assertEquals("Car BA 3 final destination track", "", c3.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 3 load", "Scrap", c3.getLoadName());
        Assert.assertEquals("Car BA 3 next load", "E", c3.getNextLoadName());

        Assert.assertEquals("Car BB 4 destination", "Bedford MA", c4.getDestinationName());
        Assert.assertEquals("Car BB 4 destination track", "Bedford Yard", c4.getDestinationTrackName());
        Assert.assertEquals("Car BB 4 final destination", "", c4.getFinalDestinationName());
        Assert.assertEquals("Car BB 4 final destination track", "", c4.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 4 load", "Coils", c4.getLoadName());
        Assert.assertEquals("Car BB 4 next load", "", c4.getNextLoadName());

        Assert.assertEquals("Car BC 5 destination", "Acton MA", c5.getDestinationName());
        Assert.assertEquals("Car BC 5 destination track", "Acton Yard", c5.getDestinationTrackName());
        Assert.assertEquals("Car BC 5 final destination", "", c5.getFinalDestinationName());
        Assert.assertEquals("Car BC 5 final destination track", "", c5.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 5 load", "L", c5.getLoadName());
        Assert.assertEquals("Car BC 5 next load", "", c5.getNextLoadName());

        Assert.assertEquals("Car BD 6 destination", "Acton MA", c6.getDestinationName());
        Assert.assertEquals("Car BD 6 destination track", "Acton Interchange", c6.getDestinationTrackName());
        Assert.assertEquals("Car BD 6 final destination", "", c6.getFinalDestinationName());
        Assert.assertEquals("Car BD 6 final destination track", "", c6.getFinalDestinationTrackName());
        Assert.assertEquals("Car BD 6 load", "L", c6.getLoadName());
        Assert.assertEquals("Car BD 6 next load", "", c6.getNextLoadName());

        Assert.assertEquals("Car BA 7 destination", "Acton MA", c7.getDestinationName());
        Assert.assertEquals("Car BA 7 destination track", "Acton Yard", c7.getDestinationTrackName());
        Assert.assertEquals("Car BA 7 final destination", "", c7.getFinalDestinationName());
        Assert.assertEquals("Car BA 7 final destination track", "", c7.getFinalDestinationTrackName());
        Assert.assertEquals("Car BA 7 load", "E", c7.getLoadName());
        Assert.assertEquals("Car BA 7 next load", "", c7.getNextLoadName());

        Assert.assertEquals("Car BB 8 destination", "Bedford MA", c8.getDestinationName());
        Assert.assertEquals("Car BB 8 destination track", "Bedford Interchange", c8.getDestinationTrackName());
        Assert.assertEquals("Car BB 8 final destination", "Clinton MA", c8.getFinalDestinationName());
        Assert.assertEquals("Car BB 8 final destination track", "Clinton Siding 1", c8.getFinalDestinationTrackName());
        Assert.assertEquals("Car BB 8 load", "Wire", c8.getLoadName());
        Assert.assertEquals("Car BB 8 next load", "", c8.getNextLoadName());

        Assert.assertEquals("Car BC 9 destination", "Acton MA", c9.getDestinationName());
        Assert.assertEquals("Car BC 9 destination track", "Acton Interchange", c9.getDestinationTrackName());
        Assert.assertEquals("Car BC 9 final destination", "", c9.getFinalDestinationName());
        Assert.assertEquals("Car BC 9 final destination track", "", c9.getFinalDestinationTrackName());
        Assert.assertEquals("Car BC 9 load", "E", c9.getLoadName());
        Assert.assertEquals("Car BC 9 next load", "", c9.getNextLoadName());

        BedfordToActonTrain.terminate();
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
        Location acton = lmanager.newLocation("Acton MA");
        Assert.assertEquals("Location 1 Name", "Acton MA", acton.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, acton.getLength());

        Track actonSiding1 = acton.addTrack("Acton Siding 1", Track.SPUR);
        actonSiding1.setLength(300);
        Assert.assertEquals("Location actonSpur1 Name", "Acton Siding 1", actonSiding1.getName());
        Assert.assertEquals("Location actonSpur1 Length", 300, actonSiding1.getLength());

        Track actonSiding2 = acton.addTrack("Acton Siding 2", Track.SPUR);
        actonSiding2.setLength(300);
        Assert.assertEquals("Location actonSpur2 Name", "Acton Siding 2", actonSiding2.getName());
        Assert.assertEquals("Location actonSpur2 Length", 300, actonSiding2.getLength());

        Track actonYard = acton.addTrack("Acton Yard", Track.YARD);
        actonYard.setLength(400);
        Assert.assertEquals("Location actonYard Name", "Acton Yard", actonYard.getName());
        Assert.assertEquals("Location actonYard Length", 400, actonYard.getLength());

        Track actonInterchange1 = acton.addTrack("Acton Interchange", Track.INTERCHANGE);
        actonInterchange1.setLength(500);
        Assert.assertEquals("Track actonInterchange Name", "Acton Interchange", actonInterchange1.getName());
        Assert.assertEquals("Track actonInterchange Length", 500, actonInterchange1.getLength());
        Assert.assertEquals("Track actonInterchange Train Directions", DIRECTION_ALL, actonInterchange1.getTrainDirections());

        // add a second interchange track
        Track actonInterchange2 = acton.addTrack("Acton Interchange 2", Track.INTERCHANGE);
        actonInterchange2.setLength(500);
        // bias tracks
        actonInterchange2.setMoves(100);

        // create 2 cars
        Car c1 = cmanager.newCar("BA", "1");
        c1.setTypeName("Boxcar");
        c1.setLength("40");
        Assert.assertEquals("Box Car 3 Length", "40", c1.getLength());

        Car c2 = cmanager.newCar("BB", "2");
        c2.setTypeName("Boxcar");
        c2.setLength("40");
        Assert.assertEquals("Box Car 4 Length", "40", c2.getLength());

        Assert.assertEquals("place car at Acton", Track.OKAY, c1.setLocation(acton, actonSiding1));
        Assert.assertEquals("place car at Acton", Track.OKAY, c2.setLocation(acton, actonSiding1));

        // create a local train servicing Acton
        Train actonTrain = tmanager.newTrain("Acton Local");
        Route routeA = rmanager.newRoute("A");
        routeA.addLocation(acton);
        actonTrain.setRoute(routeA);

        c1.setFinalDestination(acton);
        c1.setFinalDestinationTrack(actonSiding2);
        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c1, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c1.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Siding 2", c1.getDestinationTrackName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());
        
        c2.setFinalDestination(acton);
        c2.setFinalDestinationTrack(actonSiding2);
        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c2, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c2.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Siding 2", c2.getDestinationTrackName());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());
        
        // now place the two cars in a kernel
        Kernel k1 = cmanager.newKernel("group of two cars");
        c1.setKernel(k1); // lead car
        c2.setKernel(k1);
        
        // try routing them again
        c1.setFinalDestination(acton);
        c1.setFinalDestinationTrack(actonInterchange1);

        Assert.assertTrue("Try routing final track with Acton Local", router.setDestination(c1, null, null));
        Assert.assertEquals("Check car's destination", "Acton MA", c1.getDestinationName());
        Assert.assertEquals("Check car's destination track", actonInterchange1, c1.getDestinationTrack());
        Assert.assertEquals("Router status", Track.OKAY, router.getStatus());
        
        // c2 should fail part of a kernel and not lead
        c2.setFinalDestination(acton);
        c2.setFinalDestinationTrack(actonInterchange1);
        Assert.assertFalse("Try routing final track with Acton Local", router.setDestination(c2, null, null));
        // confirm that car destination didn't change
        Assert.assertEquals("Check car's destination", "Acton MA", c2.getDestinationName());
        Assert.assertEquals("Check car's destination track", "Acton Siding 2", c2.getDestinationTrackName());
        
    }   
    

    private void loadLocationsTrainsAndCars() {
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);

        CarTypes ct = InstanceManager.getDefault(CarTypes.class);

        // register the car and engine types used
        ct.addName("Boxcar");
        ct.addName(Bundle.getMessage("Caboose"));
        ct.addName("Flat");

        // create 6 locations and tracks
        Location acton = lmanager.newLocation("Acton MA");
        Assert.assertEquals("Location 1 Name", "Acton MA", acton.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, acton.getLength());

        Track actonSpur1 = acton.addTrack("Acton Siding 1", Track.SPUR);
        actonSpur1.setLength(300);
        Assert.assertEquals("Location actonSpur1 Name", "Acton Siding 1", actonSpur1.getName());
        Assert.assertEquals("Location actonSpur1 Length", 300, actonSpur1.getLength());

        Track actonSpur2 = acton.addTrack("Acton Siding 2", Track.SPUR);
        actonSpur2.setLength(300);
        Assert.assertEquals("Location actonSpur2 Name", "Acton Siding 2", actonSpur2.getName());
        Assert.assertEquals("Location actonSpur2 Length", 300, actonSpur2.getLength());

        Track actonYard = acton.addTrack("Acton Yard", Track.YARD);
        actonYard.setLength(400);
        Assert.assertEquals("Location actonYard Name", "Acton Yard", actonYard.getName());
        Assert.assertEquals("Location actonYard Length", 400, actonYard.getLength());

        Track actonInterchange = acton.addTrack("Acton Interchange", Track.INTERCHANGE);
        actonInterchange.setLength(500);
        Assert.assertEquals("Track actonInterchange Name", "Acton Interchange", actonInterchange.getName());
        Assert.assertEquals("Track actonInterchange Length", 500, actonInterchange.getLength());
        Assert.assertEquals("Track actonInterchange Train Directions", DIRECTION_ALL, actonInterchange.getTrainDirections());

        Location bedford = lmanager.newLocation("Bedford MA");
        Assert.assertEquals("Location 1 Name", "Bedford MA", bedford.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, bedford.getLength());

        Track bedfordSpur1 = bedford.addTrack("Bedford Siding 1", Track.SPUR);
        bedfordSpur1.setLength(300);
        Assert.assertEquals("Location bedfordSpur1 Name", "Bedford Siding 1", bedfordSpur1.getName());
        Assert.assertEquals("Location bedfordSpur1 Length", 300, bedfordSpur1.getLength());

        Track bedfordSpur2 = bedford.addTrack("Bedford Siding 2", Track.SPUR);
        bedfordSpur2.setLength(300);
        Assert.assertEquals("Location bedfordSpur2 Name", "Bedford Siding 2", bedfordSpur2.getName());
        Assert.assertEquals("Location bedfordSpur2 Length", 300, bedfordSpur2.getLength());

        Track bedfordYard = bedford.addTrack("Bedford Yard", Track.YARD);
        bedfordYard.setLength(400);
        Assert.assertEquals("Location bedfordYard Name", "Bedford Yard", bedfordYard.getName());
        Assert.assertEquals("Location bedfordYard Length", 400, bedfordYard.getLength());

        Track bedfordInterchange = bedford.addTrack("Bedford Interchange", Track.INTERCHANGE);
        bedfordInterchange.setLength(500);
        Assert.assertEquals("Track bedfordInterchange Name", "Bedford Interchange", bedfordInterchange.getName());
        Assert.assertEquals("Track bedfordInterchange Length", 500, bedfordInterchange.getLength());

        Location clinton = lmanager.newLocation("Clinton MA");
        Assert.assertEquals("Location 1 Name", "Clinton MA", clinton.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, clinton.getLength());

        Track clintonSpur1 = clinton.addTrack("Clinton Siding 1", Track.SPUR);
        clintonSpur1.setLength(300);
        Assert.assertEquals("Location clintonSpur1 Name", "Clinton Siding 1", clintonSpur1.getName());
        Assert.assertEquals("Location clintonSpur1 Length", 300, clintonSpur1.getLength());

        Track clintonSpur2 = clinton.addTrack("Clinton Siding 2", Track.SPUR);
        clintonSpur2.setLength(300);
        Assert.assertEquals("Location clintonSpur2 Name", "Clinton Siding 2", clintonSpur2.getName());
        Assert.assertEquals("Location clintonSpur2 Length", 300, bedfordSpur2.getLength());

        Track clintonYard = clinton.addTrack("Clinton Yard", Track.YARD);
        clintonYard.setLength(400);
        Assert.assertEquals("Location clintonYard Name", "Clinton Yard", clintonYard.getName());
        Assert.assertEquals("Location clintonYard Length", 400, clintonYard.getLength());

        Track clintonInterchange = clinton.addTrack("Clinton Interchange", Track.INTERCHANGE);
        clintonInterchange.setLength(500);
        Assert.assertEquals("Track clintonInterchange Name", "Clinton Interchange", clintonInterchange.getName());
        Assert.assertEquals("Track clintonInterchange Length", 500, clintonInterchange.getLength());

        Location danbury = lmanager.newLocation("Danbury MA");
        Track danburySpur1 = danbury.addTrack("Danbury Siding 1", Track.SPUR);
        danburySpur1.setLength(300);
        Track danburySpur2 = danbury.addTrack("Danbury Siding 2", Track.SPUR);
        danburySpur2.setLength(300);
        Track danburyYard = danbury.addTrack("Danbury Yard", Track.YARD);
        danburyYard.setLength(400);
        Track danburyInterchange = danbury.addTrack("Danbury Interchange", Track.INTERCHANGE);
        danburyInterchange.setLength(500);

        Location essex = lmanager.newLocation("Essex MA");
        Track essexSpur1 = essex.addTrack("Essex Siding 1", Track.SPUR);
        essexSpur1.setLength(300);
        Track essexSpur2 = essex.addTrack("Essex Siding 2", Track.SPUR);
        essexSpur2.setLength(300);
        Track essexYard = essex.addTrack("Essex Yard", Track.YARD);
        essexYard.setLength(400);
        Track essexInterchange = essex.addTrack("Essex Interchange", Track.INTERCHANGE);
        essexInterchange.setLength(500);

        Location foxboro = lmanager.newLocation("Foxboro MA");
        Track foxboroSpur1 = foxboro.addTrack("Foxboro Siding 1", Track.SPUR);
        foxboroSpur1.setLength(300);
        Track foxboroSpur2 = foxboro.addTrack("Foxboro Siding 2", Track.SPUR);
        foxboroSpur2.setLength(300);
        Track foxboroYard = foxboro.addTrack("Foxboro Yard", Track.YARD);
        foxboroYard.setLength(400);
        Track foxboroInterchange = foxboro.addTrack("Foxboro Interchange", Track.INTERCHANGE);
        foxboroInterchange.setLength(500);

        // create a local train servicing Acton
        Train actonTrain = tmanager.newTrain("Acton Local");
        Route routeA = rmanager.newRoute("A");
        RouteLocation rlA = routeA.addLocation(acton);
        rlA.setMaxCarMoves(10);
        rlA.setTrainIconX(25);	// set train icon coordinates
        rlA.setTrainIconY(250);
        actonTrain.setRoute(routeA);

        // create a train with a route from Acton to Bedford
        Train actonToBedfordTrain = tmanager.newTrain("Acton to Bedford");
        Route routeAB = rmanager.newRoute("AB");
        routeAB.addLocation(acton);
        RouteLocation rlBedford = routeAB.addLocation(bedford);
        rlBedford.setTrainIconX(100);	// set train icon coordinates
        rlBedford.setTrainIconY(250);
        actonToBedfordTrain.setRoute(routeAB);

        // create a train with a route from Bedford to Clinton
        Train bedfordToClintonTrain = tmanager.newTrain("Bedford to Clinton");
        Route routeBC = rmanager.newRoute("BC");
        routeBC.addLocation(bedford);
        RouteLocation rlchelmsford = routeBC.addLocation(clinton);
        rlchelmsford.setTrainIconX(175);	// set train icon coordinates
        rlchelmsford.setTrainIconY(250);
        bedfordToClintonTrain.setRoute(routeBC);

        // create a train with a route from Clinton to Danbury
        Train clintonToDanburyTrain = tmanager.newTrain("Clinton to Danbury");
        Route routeCD = rmanager.newRoute("CD");
        routeCD.addLocation(clinton);
        RouteLocation rlDanbury = routeCD.addLocation(danbury);
        rlDanbury.setTrainIconX(250);	// set train icon coordinates
        rlDanbury.setTrainIconY(250);
        clintonToDanburyTrain.setRoute(routeCD);

        // create a train with a route from Danbury to Essex
        Train danburyToEssexTrain = tmanager.newTrain("Danbury to Essex");
        Route routeDE = rmanager.newRoute("DE");
        RouteLocation rlDanbury2 = routeDE.addLocation(danbury);
        RouteLocation rlEssex = routeDE.addLocation(essex);
        // set the number of car moves to 8 for a later test
        rlDanbury2.setMaxCarMoves(8);
        rlEssex.setMaxCarMoves(8);
        rlEssex.setTrainIconX(25);	// set train icon coordinates
        rlEssex.setTrainIconY(275);
        danburyToEssexTrain.setRoute(routeDE);

        // create a train with a route from Essex to Foxboro
        Train essexToFoxboroTrain = tmanager.newTrain("Essex to Foxboro");
        Route routeEF = rmanager.newRoute("EF");
        routeEF.addLocation(essex);
        RouteLocation rlFoxboro = routeEF.addLocation(foxboro);
        rlFoxboro.setTrainIconX(100);	// set train icon coordinates
        rlFoxboro.setTrainIconY(275);
        essexToFoxboroTrain.setRoute(routeEF);

        Train bedfordToActonTrain = tmanager.newTrain("BedfordToActonToBedford");
        Route bedfordToActon = InstanceManager.getDefault(RouteManager.class).newRoute("BedfordToActonToBedford");
        RouteLocation rlB2 = bedfordToActon.addLocation(bedford);
        RouteLocation rlA3 = bedfordToActon.addLocation(acton);
        RouteLocation rlB3 = bedfordToActon.addLocation(bedford);
        // increase the number of moves so all cars are used
        rlB2.setMaxCarMoves(10);
        rlA3.setMaxCarMoves(10);
        rlB3.setMaxCarMoves(10);
        bedfordToActonTrain.setRoute(bedfordToActon);

        // add cars
        Car c3 = cmanager.newCar("BA", "3");
        c3.setTypeName("Boxcar");
        c3.setLength("40");
        c3.setOwner("DAB");
        c3.setBuilt("1984");

        Car c4 = cmanager.newCar("BB", "4");
        c4.setTypeName("Flat");
        c4.setLength("40");
        c4.setOwner("AT");
        c4.setBuilt("1-86");
        c4.setMoves(2);

        Car c5 = cmanager.newCar("BC", "5");
        c5.setTypeName("Boxcar");
        c5.setLoadName("Boxes");
        c5.setLength("40");
        c5.setBuilt("2000");
        c5.setMoves(3);

        Car c6 = cmanager.newCar("BD", "6");
        c6.setTypeName("Boxcar");
        c6.setLoadName("Empty");
        c6.setLength("40");
        c6.setBuilt("2000");
        c6.setMoves(4);

        Car c7 = cmanager.newCar("BA", "7");
        c7.setTypeName("Boxcar");
        c7.setLoadName("Boxes");
        c7.setLength("4");
        c7.setBuilt("2000");
        c7.setMoves(5);

        Car c8 = cmanager.newCar("BB", "8");
        c8.setTypeName("Boxcar");
        c8.setLoadName("Empty");
        c8.setLength("4");
        c8.setBuilt("2000");
        c8.setMoves(6);

        Car c9 = cmanager.newCar("BC", "9");
        c9.setTypeName("Boxcar");
        c9.setLoadName("Empty");
        c9.setLength("4");
        c9.setBuilt("2000");
        c9.setMoves(7);
    }

    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        Setup.setRouterBuildReportLevel(Setup.BUILD_REPORT_VERY_DETAILED);
    }

    public OperationsCarRouterTest(String s) {
        super(s);
    }

    // The minimal setup for log4J
    @Override
    @After
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
