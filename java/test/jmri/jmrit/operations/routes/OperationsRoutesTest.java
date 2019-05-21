package jmri.jmrit.operations.routes;

import java.io.IOException;
import java.util.List;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import org.jdom2.JDOMException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Operations Route class. Last manually cross-checked on 20090131.
 * <p>
 * Still to do: Route: Route Location <-- Need to verify Route: XML read/write
 * RouteLocation: get/set Staging Track RouteLocation: location <--Need to
 * verify RouteLocation: XML read/write
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
 */
public class OperationsRoutesTest extends OperationsTestCase {

    // test Route creation
    @Test
    public void testCreate() {
        Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");
        r1.setComment("TESTCOMMENT");

        Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
        Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());
        Assert.assertEquals("Route Comment", "TESTCOMMENT", r1.getComment());
    }

    // test Route public constants
    @Test
    public void testConstants() {
        Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

        Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
        Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());

        Assert.assertEquals("Route Constant EAST", 1, Route.EAST);
        Assert.assertEquals("Route Constant WEST", 2, Route.WEST);
        Assert.assertEquals("Route Constant NORTH", 4, Route.NORTH);
        Assert.assertEquals("Route Constant SOUTH", 8, Route.SOUTH);

        Assert.assertEquals("Route Constant LISTCHANGE_CHANGED_PROPERTY", "routeListChange",
                Route.LISTCHANGE_CHANGED_PROPERTY);
        Assert.assertEquals("Route Constant DISPOSE", "routeDispose", Route.DISPOSE);
    }

    // test Route attributes
    @Test
    public void testAttributes() {
        Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

        Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
        Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());
        Assert.assertEquals("Route toString", "TESTROUTENAME", r1.toString());

        r1.setName("TESTNEWNAME");
        Assert.assertEquals("Route New Name", "TESTNEWNAME", r1.getName());
    }

    // test route location
    @Test
    public void testRouteLocation() {
        Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

        Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
        Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());

        Location l1 = new Location("TESTLOCATIONID1", "TESTLOCATIONNAME1");

        RouteLocation rl1 = new RouteLocation("TESTROUTELOCATIONID", l1);

        Assert.assertEquals("Route Location Id", "TESTROUTELOCATIONID", rl1.getId());
        Assert.assertEquals("Route Location Name", "TESTLOCATIONNAME1", rl1.getName());
    }

    // test public RouteLocation constants
    @Test
    public void testRouteLocationConstants() {
        Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

        Location l1 = new Location("TESTLOCATIONID1", "TESTLOCATIONNAME1");

        RouteLocation rl1 = new RouteLocation("TESTROUTELOCATIONID", l1);
        Assert.assertNotNull("exists", rl1);

        Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
        Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());

        Assert.assertEquals("RouteLocation Constant EAST", 1, RouteLocation.EAST);
        Assert.assertEquals("RouteLocation Constant WEST", 2, RouteLocation.WEST);
        Assert.assertEquals("RouteLocation Constant NORTH", 4, RouteLocation.NORTH);
        Assert.assertEquals("RouteLocation Constant SOUTH", 8, RouteLocation.SOUTH);

        Assert.assertEquals("RouteLocation Constant EAST_DIR", Bundle.getMessage("East"), RouteLocation.EAST_DIR);
        Assert.assertEquals("RouteLocation Constant WEST_DIR", Bundle.getMessage("West"), RouteLocation.WEST_DIR);
        Assert.assertEquals("RouteLocation Constant NORTH_DIR", Bundle.getMessage("North"), RouteLocation.NORTH_DIR);
        Assert.assertEquals("RouteLocation Constant SOUTH_DIR", Bundle.getMessage("South"), RouteLocation.SOUTH_DIR);

        Assert.assertEquals("RouteLocation Constant DROP_CHANGED_PROPERTY", "dropChange",
                RouteLocation.DROP_CHANGED_PROPERTY);
        Assert.assertEquals("RouteLocation Constant PICKUP_CHANGED_PROPERTY", "pickupChange",
                RouteLocation.PICKUP_CHANGED_PROPERTY);
        Assert.assertEquals("RouteLocation Constant MAXMOVES_CHANGED_PROPERTY", "maxMovesChange",
                RouteLocation.MAX_MOVES_CHANGED_PROPERTY);
        Assert.assertEquals("RouteLocation Constant DISPOSE", "routeLocationDispose", RouteLocation.DISPOSE);
    }

    // test RouteLocation attributes
    @Test
    public void testRouteLocationAttributes() {
        Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

        Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
        Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());

        Location l1 = new Location("TESTLOCATIONID1", "TESTLOCATIONNAME1");

        RouteLocation rl1 = new RouteLocation("TESTROUTELOCATIONID", l1);
        rl1.setSequenceNumber(4);
        rl1.setComment("TESTROUTELOCATIONCOMMENT");
        rl1.setMaxTrainLength(320);
        rl1.setTrainLength(220);
        rl1.setTrainWeight(240);
        rl1.setMaxCarMoves(32);
        rl1.setCarMoves(10);
        rl1.setGrade(2.0);
        rl1.setTrainIconX(12);
        rl1.setTrainIconY(8);

        Assert.assertEquals("RouteLocation Id", "TESTROUTELOCATIONID", rl1.getId());
        Assert.assertEquals("RouteLocation Name", "TESTLOCATIONNAME1", rl1.getName());
        Assert.assertEquals("RouteLocation toString", "TESTLOCATIONNAME1", rl1.toString());

        Assert.assertEquals("RouteLocation Comment", "TESTROUTELOCATIONCOMMENT", rl1.getComment());
        Assert.assertEquals("RouteLocation Sequence", 4, rl1.getSequenceNumber());

        Assert.assertEquals("RouteLocation Max Train Length", 320, rl1.getMaxTrainLength());
        Assert.assertEquals("RouteLocation Train Length", 220, rl1.getTrainLength());
        Assert.assertEquals("RouteLocation Train Weight", 240, rl1.getTrainWeight());
        Assert.assertEquals("RouteLocation Max Car Moves", 32, rl1.getMaxCarMoves());
        Assert.assertEquals("RouteLocation Car Moves", 10, rl1.getCarMoves());
        Assert.assertEquals("RouteLocation Grade", "2.0", Double.toString(rl1.getGrade()));
        Assert.assertEquals("RouteLocation Icon X", 12, rl1.getTrainIconX());
        Assert.assertEquals("RouteLocation Icon Y", 8, rl1.getTrainIconY());

        rl1.setTrainDirection(RouteLocation.EAST);
        Assert.assertEquals("RouteLocation Train Direction East", 1, rl1.getTrainDirection());

        rl1.setTrainDirection(RouteLocation.WEST);
        Assert.assertEquals("RouteLocation Train Direction West", 2, rl1.getTrainDirection());

        rl1.setTrainDirection(RouteLocation.NORTH);
        Assert.assertEquals("RouteLocation Train Direction North", 4, rl1.getTrainDirection());

        rl1.setTrainDirection(RouteLocation.SOUTH);
        Assert.assertEquals("RouteLocation Train Direction South", 8, rl1.getTrainDirection());

        // rl1.setCanDrop(true);
        Assert.assertEquals("RouteLocation Train can drop initial", true, rl1.isDropAllowed());

        rl1.setDropAllowed(false);
        Assert.assertEquals("RouteLocation Train can drop false", false, rl1.isDropAllowed());

        rl1.setDropAllowed(true);
        Assert.assertEquals("RouteLocation Train can drop true", true, rl1.isDropAllowed());

        // rl1.setCanPickup(true);
        Assert.assertEquals("RouteLocation Train can Pickup initial", true, rl1.isPickUpAllowed());

        rl1.setPickUpAllowed(false);
        Assert.assertEquals("RouteLocation Train can Pickup false", false, rl1.isPickUpAllowed());

        rl1.setPickUpAllowed(true);
        Assert.assertEquals("RouteLocation Train can Pickup true", true, rl1.isPickUpAllowed());
    }

    // test route location management
    @Test
    public void testRouteLocationManagement() {
        Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

        Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
        Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());

        RouteLocation rladd;

        Location l1 = new Location("TESTLOCATIONID1", "TESTLOCATIONNAME1");
        rladd = r1.addLocation(l1);

        Location l2 = new Location("TESTLOCATIONID2", "TESTLOCATIONNAME2");
        rladd = r1.addLocation(l2);

        Location l3 = new Location("TESTLOCATIONID3", "TESTLOCATIONNAME3");
        rladd = r1.addLocation(l3);

        Assert.assertNotNull("exists", rladd);

        RouteLocation rl1test;

        rl1test = r1.getLastLocationByName("TESTLOCATIONNAME1");
        Assert.assertEquals("Add Location 1", "TESTLOCATIONNAME1", rl1test.getName());

        rl1test = r1.getLastLocationByName("TESTLOCATIONNAME2");
        Assert.assertEquals("Add Location 2", "TESTLOCATIONNAME2", rl1test.getName());

        rl1test = r1.getLastLocationByName("TESTLOCATIONNAME3");
        Assert.assertEquals("Add Location 3", "TESTLOCATIONNAME3", rl1test.getName());

        // Check that locations are in the expected order
        List<RouteLocation> list = r1.getLocationsBySequenceList();
        for (int i = 0; i < list.size(); i++) {
            rl1test = list.get(i);
            if (i == 0) {
                Assert.assertEquals("List Location 1 before", "TESTLOCATIONNAME1", rl1test.getName());
                Assert.assertEquals("List Location 1 sequence id", 1, rl1test.getSequenceNumber());
            }
            if (i == 1) {
                Assert.assertEquals("List Location 2 before", "TESTLOCATIONNAME2", rl1test.getName());
                Assert.assertEquals("List Location 2 sequence id", 2, rl1test.getSequenceNumber());
            }
            if (i == 2) {
                Assert.assertEquals("List Location 3 before", "TESTLOCATIONNAME3", rl1test.getName());
                Assert.assertEquals("List Location 3 sequence id", 3, rl1test.getSequenceNumber());
            }
        }

        // Add a fourth location but put it in the second spot and check that locations are in the expected order
        Location l4 = new Location("TESTLOCATIONID4", "TESTLOCATIONNAME4");
        rladd = r1.addLocation(l4, 2);

        rl1test = r1.getLastLocationByName("TESTLOCATIONNAME4");
        Assert.assertEquals("Add Location 4", "TESTLOCATIONNAME4", rl1test.getName());

        list = r1.getLocationsBySequenceList();
        for (int i = 0; i < list.size(); i++) {
            rl1test = list.get(i);
            if (i == 0) {
                Assert.assertEquals("List Location 1 after", "TESTLOCATIONNAME1", rl1test.getName());
                Assert.assertEquals("List Location 1 sequence id", 1, rl1test.getSequenceNumber());
            }
            if (i == 1) {
                Assert.assertEquals("List Location 2 after", "TESTLOCATIONNAME4", rl1test.getName());
                Assert.assertEquals("List Location 2 sequence id", 2, rl1test.getSequenceNumber());
            }
            if (i == 2) {
                Assert.assertEquals("List Location 3 after", "TESTLOCATIONNAME2", rl1test.getName());
                Assert.assertEquals("List Location 3 sequence id", 3, rl1test.getSequenceNumber());
            }
            if (i == 3) {
                Assert.assertEquals("List Location 4 after", "TESTLOCATIONNAME3", rl1test.getName());
                Assert.assertEquals("List Location 4 sequence id", 4, rl1test.getSequenceNumber());
            }
        }

        // Move up the third location and check that locations are in the expected order
        rl1test = r1.getLastLocationByName("TESTLOCATIONNAME3");
        r1.moveLocationUp(rl1test);
        list = r1.getLocationsBySequenceList();
        for (int i = 0; i < list.size(); i++) {
            rl1test = list.get(i);
            if (i == 0) {
                Assert.assertEquals("List Location 1 after move up", "TESTLOCATIONNAME1", rl1test.getName());
                Assert.assertEquals("List Location 1 sequence id", 1, rl1test.getSequenceNumber());
            }
            if (i == 1) {
                Assert.assertEquals("List Location 2 after move up", "TESTLOCATIONNAME4", rl1test.getName());
                Assert.assertEquals("List Location 2 sequence id", 2, rl1test.getSequenceNumber());
            }
            if (i == 2) {
                Assert.assertEquals("List Location 3 after move up", "TESTLOCATIONNAME3", rl1test.getName());
                Assert.assertEquals("List Location 3 sequence id", 3, rl1test.getSequenceNumber());
            }
            if (i == 3) {
                Assert.assertEquals("List Location 4 after move up", "TESTLOCATIONNAME2", rl1test.getName());
                Assert.assertEquals("List Location 4 sequence id", 4, rl1test.getSequenceNumber());
            }
        }

        // Move down the first location down 2 and check that locations are in the expected order
        rl1test = r1.getLastLocationByName("TESTLOCATIONNAME1");
        r1.moveLocationDown(rl1test);
        r1.moveLocationDown(rl1test);
        list = r1.getLocationsBySequenceList();
        for (int i = 0; i < list.size(); i++) {
            rl1test = list.get(i);
            if (i == 0) {
                Assert.assertEquals("List Location 1 after move up", "TESTLOCATIONNAME4", rl1test.getName());
                Assert.assertEquals("List Location 1 sequence id", 1, rl1test.getSequenceNumber());
            }
            if (i == 1) {
                Assert.assertEquals("List Location 2 after move up", "TESTLOCATIONNAME3", rl1test.getName());
                Assert.assertEquals("List Location 2 sequence id", 2, rl1test.getSequenceNumber());
            }
            if (i == 2) {
                Assert.assertEquals("List Location 3 after move up", "TESTLOCATIONNAME1", rl1test.getName());
                Assert.assertEquals("List Location 3 sequence id", 3, rl1test.getSequenceNumber());
            }
            if (i == 3) {
                Assert.assertEquals("List Location 4 after move up", "TESTLOCATIONNAME2", rl1test.getName());
                Assert.assertEquals("List Location 4 sequence id", 4, rl1test.getSequenceNumber());
            }
        }

        // Delete the third location and check that locations are in the expected order
        rl1test = r1.getLastLocationByName("TESTLOCATIONNAME3");
        r1.deleteLocation(rl1test);
        list = r1.getLocationsBySequenceList();
        for (int i = 0; i < list.size(); i++) {
            rl1test = list.get(i);
            if (i == 0) {
                Assert.assertEquals("List Location 1 after move up", "TESTLOCATIONNAME4", rl1test.getName());
                Assert.assertEquals("List Location 1 sequence id", 1, rl1test.getSequenceNumber());
            }
            if (i == 1) {
                Assert.assertEquals("List Location 2 after move up", "TESTLOCATIONNAME1", rl1test.getName());
                Assert.assertEquals("List Location 2 sequence id", 2, rl1test.getSequenceNumber());
            }
            if (i == 2) {
                Assert.assertEquals("List Location 3 after move up", "TESTLOCATIONNAME2", rl1test.getName());
                Assert.assertEquals("List Location 3 sequence id", 3, rl1test.getSequenceNumber());
            }
        }
    }

    @Test
    public void testRouteManager() {
        RouteManager rm = InstanceManager.getDefault(RouteManager.class);
        List<Route> listById = rm.getRoutesByIdList();
        List<Route> listByName = rm.getRoutesByNameList();

        Assert.assertEquals("Route id list is empty", true, listById.isEmpty());
        Assert.assertEquals("Route name list is empty", true, listByName.isEmpty());

        Route route1 = rm.newRoute("testRoute");

        listById = rm.getRoutesByIdList();
        listByName = rm.getRoutesByNameList();

        Assert.assertEquals("Route id list should not be empty", false, listById.isEmpty());
        Assert.assertEquals("Route name list should not be empty", false, listByName.isEmpty());

        // now see if list sort works properly
        Route route2 = rm.newRoute("atestRoute");
        Route route3 = rm.newRoute("ztestRoute");
        Route route4 = rm.newRoute("dtestRoute");

        // create the same named route
        Route route5 = rm.newRoute("atestRoute");
        rm.register(route2);

        // also try and re-register the same route
        rm.register(route4);

        listById = rm.getRoutesByIdList();
        listByName = rm.getRoutesByNameList();
        Assert.assertEquals("Route id list should have 4 routes", 4, listById.size());
        Assert.assertEquals("Route name list should have 4 routes", 4, listByName.size());

        // check the order
        for (int i = 0; i < listById.size(); i++) {
            Route r = listById.get(i);
            if (i == 0) {
                Assert.assertEquals("First route name by id", "testRoute", r.getName());
            }
            if (i == 1) {
                Assert.assertEquals("2nd route name by id", "atestRoute", r.getName());
            }
            if (i == 2) {
                Assert.assertEquals("3rd route name by id", "ztestRoute", r.getName());
            }
            if (i == 3) {
                Assert.assertEquals("4th route name by id", "dtestRoute", r.getName());
            }
        }

        // check the order
        for (int i = 0; i < listByName.size(); i++) {
            Route r = listByName.get(i);
            if (i == 0) {
                Assert.assertEquals("First route name by id", "atestRoute", r.getName());
            }
            if (i == 1) {
                Assert.assertEquals("2nd route name by id", "dtestRoute", r.getName());
            }
            if (i == 2) {
                Assert.assertEquals("3rd route name by id", "testRoute", r.getName());
            }
            if (i == 3) {
                Assert.assertEquals("4th route name by id", "ztestRoute", r.getName());
            }
        }

        // test the get routeByName
        Route route6 = rm.getRouteByName("dtestRoute");

        Assert.assertEquals("Route name should be", "dtestRoute", route6.getName());
        Assert.assertEquals("Route names should match", route6.getName(), route4.getName());
        Assert.assertEquals("Routes should match", route6, route4);

        // now remove a route
        rm.deregister(route1); // remove testRoute

        listById = rm.getRoutesByIdList();
        listByName = rm.getRoutesByNameList();
        Assert.assertEquals("Route id list should have 3 routes", 3, listById.size());
        Assert.assertEquals("Route name list should have 3 routes", 3, listByName.size());

        // check the order
        for (int i = 0; i < listById.size(); i++) {
            Route r = listById.get(i);
            if (i == 0) {
                Assert.assertEquals("First route name by id", "atestRoute", r.getName());
            }
            if (i == 1) {
                Assert.assertEquals("2nd route name by id", "ztestRoute", r.getName());
            }
            if (i == 2) {
                Assert.assertEquals("4th route name by id", "dtestRoute", r.getName());
            }
        }

        // check the order
        for (int i = 0; i < listByName.size(); i++) {
            Route r = listByName.get(i);
            if (i == 0) {
                Assert.assertEquals("First route name by id", "atestRoute", r.getName());
            }
            if (i == 1) {
                Assert.assertEquals("2nd route name by id", "dtestRoute", r.getName());
            }
            if (i == 2) {
                Assert.assertEquals("3rd route name by id", "ztestRoute", r.getName());
            }
        }

        // test the combo box
        JComboBox<Route> b = rm.getComboBox();
        Assert.assertEquals("ComboBox item count", 4, b.getItemCount());
        Assert.assertEquals("First combo item", null, b.getItemAt(0));
        Assert.assertEquals("First combo route", route2, b.getItemAt(1));
        Assert.assertEquals("2nd combo route", route4, b.getItemAt(2));
        Assert.assertEquals("3rd combo route", route3, b.getItemAt(3));

        // now remove two routes
        rm.deregister(route4); // remove dtestRoute
        rm.deregister(route3); // remove ztestRoute

        listById = rm.getRoutesByIdList();
        listByName = rm.getRoutesByNameList();
        Assert.assertEquals("Route id list should have 1 route", 1, listById.size());
        Assert.assertEquals("Route name list should have 1 route", 1, listByName.size());

        // check the order
        for (int i = 0; i < listById.size(); i++) {
            Route r = listById.get(i);
            if (i == 0) {
                Assert.assertEquals("First route name by id", "atestRoute", r.getName());
            }
        }

        // check the order
        for (int i = 0; i < listByName.size(); i++) {
            Route r = listByName.get(i);
            if (i == 0) {
                Assert.assertEquals("First route name by id", "atestRoute", r.getName());
            }
        }

        // test combo box update
        rm.updateComboBox(b);
        Assert.assertEquals("ComboBox item count", 2, b.getItemCount());
        Assert.assertEquals("First combo item", null, b.getItemAt(0));
        Assert.assertEquals("First combo route", route2, b.getItemAt(1));

        // finish up by deleting the last route
        rm.deregister(route5);
        listById = rm.getRoutesByIdList();
        listByName = rm.getRoutesByNameList();
        Assert.assertEquals("Route id list is empty", true, listById.isEmpty());
        Assert.assertEquals("Route name list is empty", true, listByName.isEmpty());
    }

    // test route status
    @Test
    public void testRouteStatus() {
        RouteManager rm = InstanceManager.getDefault(RouteManager.class);
        Route r = rm.newRoute("TestRouteStatus");
        // note that the status strings are defined in JmritOperationsRoutesBundle.properties
        Assert.assertEquals("Route status error", "Error", r.getStatus());

        // now add a location to the route
        Location l = InstanceManager.getDefault(LocationManager.class).newLocation("TestRouteStatusLoc");
        r.addLocation(l);
        // note that the status strings are defined in JmritOperationsRoutesBundle.properties
        Assert.assertEquals("Route status ophan", "Orphan", r.getStatus());

        // now connect route to a train
        Train t = InstanceManager.getDefault(TrainManager.class).newTrain("TestRouteStatusTrain");
        t.setRoute(r);
        // note that the status strings are defined in JmritOperationsRoutesBundle.properties
        Assert.assertEquals("Route status okay", "OK", r.getStatus());
    }

    /**
     * Test route Xml create, read, and backup support. Originally written as
     * three separate tests, now combined into one as of 8/29/2013
     *
     * @throws JDOMException exception
     * @throws IOException exception
     */
    @Test
    public void testXMLCreate() throws JDOMException, IOException {

        RouteManager manager = InstanceManager.getDefault(RouteManager.class);
        List<Route> temprouteList = manager.getRoutesByIdList();
        Assert.assertEquals("Starting Number of Routes", 0, temprouteList.size());

        Route r1 = manager.newRoute("Test Number 1");
        Route r2 = manager.newRoute("Test Number 2");
        Route r3 = manager.newRoute("Test Number 3");

        temprouteList = manager.getRoutesByIdList();
        Assert.assertEquals("New Number of Routes", 3, temprouteList.size());

        InstanceManager.getDefault(RouteManagerXml.class).writeOperationsFile();

        // Add some more routes and write file again
        // so we can test the backup facility
        Route r4 = manager.newRoute("Test Number 4");
        Route r5 = manager.newRoute("Test Number 5");
        Route r6 = manager.newRoute("Test Number 6");

        Assert.assertNotNull("route r1 exists", r1);
        Assert.assertNotNull("route r2 exists", r2);
        Assert.assertNotNull("route r3 exists", r3);
        Assert.assertNotNull("route r4 exists", r4);
        Assert.assertNotNull("route r5 exists", r5);
        Assert.assertNotNull("route r6 exists", r6);

        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location Acton = lmanager.newLocation("Acton");
        Location Bedford = lmanager.newLocation("Bedford");
        Location Chelmsford = lmanager.newLocation("Chelmsford");

        r1.setComment("r1 comment");
        RouteLocation r1l1 = r1.addLocation(Acton);
        r1.addLocation(Bedford);
        r1.addLocation(Chelmsford);
        r1.addLocation(Bedford);
        r1.addLocation(Acton);

        r1l1.setDropAllowed(false);
        r1l1.setPickUpAllowed(false);
        r1l1.setCarMoves(3); // this value isn't saved
        r1l1.setComment("rl1 comment");
        r1l1.setGrade(Double.valueOf("5"));
        r1l1.setMaxCarMoves(8);
        r1l1.setMaxTrainLength(345);
        r1l1.setTrainDirection(Location.SOUTH);
        r1l1.setTrainIconX(56);
        r1l1.setTrainIconY(78);
        r1l1.setTrainLength(234); // this value isn't saved
        r1l1.setTrainWeight(987); // this value isn't saved

        r2.setComment("r2 comment");
        r2.addLocation(Chelmsford);
        RouteLocation r2l2 = r2.addLocation(Bedford);
        r2.addLocation(Chelmsford);
        RouteLocation r2l4 = r2.addLocation(Bedford);

        r2l2.setDropAllowed(false);
        r2l2.setPickUpAllowed(true);
        r2l2.setCarMoves(3); // this value isn't saved
        r2l2.setComment("r2l2 comment");
        r2l2.setGrade(Double.valueOf("1"));
        r2l2.setMaxCarMoves(181);
        r2l2.setMaxTrainLength(4561);
        r2l2.setTrainDirection(Location.EAST);
        r2l2.setTrainIconX(651);
        r2l2.setTrainIconY(871);
        r2l2.setTrainLength(234); // this value isn't saved
        r2l2.setTrainWeight(987); // this value isn't saved

        r2l4.setDropAllowed(true);
        r2l4.setPickUpAllowed(false);
        r2l4.setCarMoves(3); // this value isn't saved
        r2l4.setComment("r2l4 comment");
        r2l4.setGrade(Double.valueOf("2"));
        r2l4.setMaxCarMoves(18);
        r2l4.setMaxTrainLength(456);
        r2l4.setTrainDirection(Location.NORTH);
        r2l4.setTrainIconX(65);
        r2l4.setTrainIconY(87);
        r2l4.setTrainLength(234); // this value isn't saved
        r2l4.setTrainWeight(987); // this value isn't saved

        r3.setComment("r3 comment");
        r4.setComment("r4 comment");
        r5.setComment("r5 comment");
        r6.setComment("r6 comment");

        InstanceManager.getDefault(RouteManagerXml.class).writeOperationsFile();

        // now perform read operation
        manager.dispose();
        manager = InstanceManager.getDefault(RouteManager.class);
        temprouteList = manager.getRoutesByIdList();
        Assert.assertEquals("Starting Number of Routes", 0, temprouteList.size());

        InstanceManager.getDefault(RouteManagerXml.class).readFile(InstanceManager.getDefault(RouteManagerXml.class).getDefaultOperationsFilename());
        temprouteList = manager.getRoutesByIdList();
        Assert.assertEquals("Number of Routes", 6, temprouteList.size());

        r1 = manager.getRouteByName("Test Number 1");
        r2 = manager.getRouteByName("Test Number 2");
        r3 = manager.getRouteByName("Test Number 3");
        r4 = manager.getRouteByName("Test Number 4");
        r5 = manager.getRouteByName("Test Number 5");
        r6 = manager.getRouteByName("Test Number 6");

        Assert.assertNotNull("route r1 exists", r1);
        Assert.assertNotNull("route r2 exists", r2);
        Assert.assertNotNull("route r3 exists", r3);
        Assert.assertNotNull("route r4 exists", r4);
        Assert.assertNotNull("route r5 exists", r5);
        Assert.assertNotNull("route r6 exists", r6);

        Assert.assertEquals("r1 comment", "r1 comment", r1.getComment());
        List<RouteLocation> locs = r1.getLocationsBySequenceList();
        Assert.assertEquals("number of locations in route r1", 5, locs.size());

        RouteLocation rl1 = locs.get(0);
        Assert.assertEquals("rl1 can drop", false, rl1.isDropAllowed());
        Assert.assertEquals("rl1 can pickup", false, rl1.isPickUpAllowed());
        Assert.assertEquals("rl1 car moves", 0, rl1.getCarMoves()); // default
        Assert.assertEquals("rl1 comment", "rl1 comment", rl1.getComment());
        Assert.assertEquals("rl1 grade", "5.0", Double.toString(rl1.getGrade()));
        Assert.assertEquals("rl1 max car moves", 8, rl1.getMaxCarMoves());
        Assert.assertEquals("rl1 max train length", 345, rl1.getMaxTrainLength());
        Assert.assertEquals("rl1 train direction", Location.SOUTH, rl1.getTrainDirection());
        Assert.assertEquals("rl1 IconX", 56, rl1.getTrainIconX());
        Assert.assertEquals("rl1 IconY", 78, rl1.getTrainIconY());
        Assert.assertEquals("rl1 train length", 0, rl1.getTrainLength()); // default
        Assert.assertEquals("rl1 train weight", 0, rl1.getTrainWeight()); // default

        Assert.assertEquals("r2 comment", "r2 comment", r2.getComment());
        locs = r2.getLocationsBySequenceList();
        Assert.assertEquals("number of locations in route r2", 4, locs.size());

        RouteLocation rl2 = locs.get(1);
        Assert.assertEquals("rl2 can drop", false, rl2.isDropAllowed());
        Assert.assertEquals("rl2 can pickup", true, rl2.isPickUpAllowed());
        Assert.assertEquals("rl2 car moves", 0, rl2.getCarMoves()); // default
        Assert.assertEquals("rl2 comment", "r2l2 comment", rl2.getComment());
        Assert.assertEquals("rl2 grade", "1.0", Double.toString(rl2.getGrade()));
        Assert.assertEquals("rl2 max car moves", 181, rl2.getMaxCarMoves());
        Assert.assertEquals("rl2 max train length", 4561, rl2.getMaxTrainLength());
        Assert.assertEquals("rl2 train direction", Location.EAST, rl2.getTrainDirection());
        Assert.assertEquals("rl2 IconX", 651, rl2.getTrainIconX());
        Assert.assertEquals("rl2 IconY", 871, rl2.getTrainIconY());
        Assert.assertEquals("rl2 train length", 0, rl2.getTrainLength()); // default
        Assert.assertEquals("rl2 train weight", 0, rl2.getTrainWeight()); // default

        RouteLocation rl4 = locs.get(3);
        Assert.assertEquals("rl4 can drop", true, rl4.isDropAllowed());
        Assert.assertEquals("rl4 can pickup", false, rl4.isPickUpAllowed());
        Assert.assertEquals("rl4 car moves", 0, rl4.getCarMoves()); // default
        Assert.assertEquals("rl4 comment", "r2l4 comment", rl4.getComment());
        Assert.assertEquals("rl4 grade", "2.0", Double.toString(rl4.getGrade()));
        Assert.assertEquals("rl4 max car moves", 18, rl4.getMaxCarMoves());
        Assert.assertEquals("rl4 max train length", 456, rl4.getMaxTrainLength());
        Assert.assertEquals("rl4 train direction", Location.NORTH, rl4.getTrainDirection());
        Assert.assertEquals("rl4 IconX", 65, rl4.getTrainIconX());
        Assert.assertEquals("rl4 IconY", 87, rl4.getTrainIconY());
        Assert.assertEquals("rl4 train length", 0, rl4.getTrainLength()); // default
        Assert.assertEquals("rl4 train weight", 0, rl4.getTrainWeight()); // default

        Assert.assertEquals("r3 comment", "r3 comment", r3.getComment());
        Assert.assertEquals("r4 comment", "r4 comment", r4.getComment());
        Assert.assertEquals("r5 comment", "r5 comment", r5.getComment());
        Assert.assertEquals("r6 comment", "r6 comment", r6.getComment());

        // now test backup file
        manager.dispose();
        // change default file name to backup
        InstanceManager.getDefault(RouteManagerXml.class).setOperationsFileName("OperationsJUnitTestRouteRoster.xml.bak");

        manager = InstanceManager.getDefault(RouteManager.class);
        temprouteList = manager.getRoutesByIdList();
        Assert.assertEquals("Starting Number of Routes", 0, temprouteList.size());

        InstanceManager.getDefault(RouteManagerXml.class).readFile(InstanceManager.getDefault(RouteManagerXml.class).getDefaultOperationsFilename());
        temprouteList = manager.getRoutesByIdList();
        Assert.assertEquals("Number of Routes", 3, temprouteList.size());

        r1 = manager.getRouteByName("Test Number 1");
        r2 = manager.getRouteByName("Test Number 2");
        r3 = manager.getRouteByName("Test Number 3");
        r4 = manager.getRouteByName("Test Number 4");
        r5 = manager.getRouteByName("Test Number 5");
        r6 = manager.getRouteByName("Test Number 6");

        Assert.assertNotNull("route r1 exists", r1);
        Assert.assertNotNull("route r2 exists", r2);
        Assert.assertNotNull("route r3 exists", r3);
        Assert.assertNull("route r4 exists", r4);
        Assert.assertNull("route r5 exists", r5);
        Assert.assertNull("route r6 exists", r6);

    }

    // TODO: Add tests for Route location track location
    // TODO: Add test to create xml file
    // TODO: Add test to read xml file

}
