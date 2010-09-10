// OperationsRoutesTest.java

package jmri.jmrit.operations.routes;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainManagerXml;

import java.io.File;
import java.util.List;
import javax.swing.JComboBox;

/**
 * Tests for the Operations Route class
 * Last manually cross-checked on 20090131
 * 
 * Still to do:
 *   Route: Route Location <-- Need to verify
 *   Route: XML read/write
 *   RouteLocation: get/set Staging Track
 *   RouteLocation: location <--Need to verify
 *   RouteLocation: XML read/write
 * 
 * @author	Bob Coleman     Copyright (C) 2008, 2009
 * @version $Revision: 1.18 $
 */
public class OperationsRoutesTest extends TestCase {

	// test Route creation
	public void testCreate() {
		Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");
		r1.setComment("TESTCOMMENT");

		Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
		Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());
		Assert.assertEquals("Route Comment", "TESTCOMMENT", r1.getComment());
	}

	// test Route public constants
	public void testConstants() {
		Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

		Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
		Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());

		Assert.assertEquals("Route Constant EAST", 1, Route.EAST);
		Assert.assertEquals("Route Constant WEST", 2, Route.WEST);
		Assert.assertEquals("Route Constant NORTH", 4, Route.NORTH);
		Assert.assertEquals("Route Constant SOUTH", 8, Route.SOUTH);

		Assert.assertEquals("Route Constant LISTCHANGE_CHANGED_PROPERTY", "routeListChange", Route.LISTCHANGE_CHANGED_PROPERTY);
		Assert.assertEquals("Route Constant DISPOSE", "dispose", Route.DISPOSE);
	}

	// test Route attributes
	public void testAttributes() {
		Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

		Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
		Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());
		Assert.assertEquals("Route toString", "TESTROUTENAME", r1.toString());

		r1.setName("TESTNEWNAME");
		Assert.assertEquals("Route New Name", "TESTNEWNAME", r1.getName());
	}

	// test route location
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
	public void testRouteLocationConstants() {
		Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

		Location l1 = new Location("TESTLOCATIONID1", "TESTLOCATIONNAME1");

		RouteLocation rl1 = new RouteLocation("TESTROUTELOCATIONID", l1);
		Assert.assertNotNull("exists", rl1 );

		Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
		Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());

		Assert.assertEquals("RouteLocation Constant EAST", 1, RouteLocation.EAST);
		Assert.assertEquals("RouteLocation Constant WEST", 2, RouteLocation.WEST);
		Assert.assertEquals("RouteLocation Constant NORTH", 4, RouteLocation.NORTH);
		Assert.assertEquals("RouteLocation Constant SOUTH", 8, RouteLocation.SOUTH);

		Assert.assertEquals("RouteLocation Constant EAST_DIR", "East", RouteLocation.EAST_DIR);
		Assert.assertEquals("RouteLocation Constant WEST_DIR", "West", RouteLocation.WEST_DIR);
		Assert.assertEquals("RouteLocation Constant NORTH_DIR", "North", RouteLocation.NORTH_DIR);
		Assert.assertEquals("RouteLocation Constant SOUTH_DIR", "South", RouteLocation.SOUTH_DIR);

		Assert.assertEquals("RouteLocation Constant DROP_CHANGED_PROPERTY", "dropChange", RouteLocation.DROP_CHANGED_PROPERTY);
		Assert.assertEquals("RouteLocation Constant PICKUP_CHANGED_PROPERTY", "pickupChange", RouteLocation.PICKUP_CHANGED_PROPERTY);
		Assert.assertEquals("RouteLocation Constant MAXMOVES_CHANGED_PROPERTY", "maxMovesChange", RouteLocation.MAXMOVES_CHANGED_PROPERTY);
		Assert.assertEquals("RouteLocation Constant DISPOSE", "dispose", RouteLocation.DISPOSE);
	}
	
	// test RouteLocation attributes
	public void testRouteLocationAttributes() {
		Route r1 = new Route("TESTROUTEID", "TESTROUTENAME");

		Assert.assertEquals("Route Id", "TESTROUTEID", r1.getId());
		Assert.assertEquals("Route Name", "TESTROUTENAME", r1.getName());

		Location l1 = new Location("TESTLOCATIONID1", "TESTLOCATIONNAME1");

		RouteLocation rl1 = new RouteLocation("TESTROUTELOCATIONID", l1);
		rl1.setSequenceId(4);
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
		Assert.assertEquals("RouteLocation Sequence", 4, rl1.getSequenceId());

		Assert.assertEquals("RouteLocation Max Train Length", 320, rl1.getMaxTrainLength());
		Assert.assertEquals("RouteLocation Train Length", 220, rl1.getTrainLength());
		Assert.assertEquals("RouteLocation Train Weight", 240, rl1.getTrainWeight());
		Assert.assertEquals("RouteLocation Max Car Moves", 32, rl1.getMaxCarMoves());
		Assert.assertEquals("RouteLocation Car Moves", 10, rl1.getCarMoves());
		Assert.assertEquals("RouteLocation Grade", 2.0, rl1.getGrade());
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

//                rl1.setCanDrop(true);
		Assert.assertEquals("RouteLocation Train can drop initial", true, rl1.canDrop());

                rl1.setCanDrop(false);
		Assert.assertEquals("RouteLocation Train can drop false", false, rl1.canDrop());

                rl1.setCanDrop(true);
		Assert.assertEquals("RouteLocation Train can drop true", true, rl1.canDrop());

//                rl1.setCanPickup(true);
		Assert.assertEquals("RouteLocation Train can Pickup initial", true, rl1.canPickup());

                rl1.setCanPickup(false);
		Assert.assertEquals("RouteLocation Train can Pickup false", false, rl1.canPickup());

                rl1.setCanPickup(true);
		Assert.assertEquals("RouteLocation Train can Pickup true", true, rl1.canPickup());
	}

	// test route location management
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

		rl1test= r1.getLastLocationByName("TESTLOCATIONNAME1");
		Assert.assertEquals("Add Location 1", "TESTLOCATIONNAME1", rl1test.getName());

		rl1test= r1.getLastLocationByName("TESTLOCATIONNAME2");
		Assert.assertEquals("Add Location 2", "TESTLOCATIONNAME2", rl1test.getName());

		rl1test= r1.getLastLocationByName("TESTLOCATIONNAME3");
		Assert.assertEquals("Add Location 3", "TESTLOCATIONNAME3", rl1test.getName());

		//  Check that locations are in the expected order
		List<String> list = r1.getLocationsBySequenceList();
		for (int i = 0; i < list.size(); i++) {
			rl1test = r1.getLocationById(list.get(i));
			if (i == 0) {			
				Assert.assertEquals("List Location 1 before", "TESTLOCATIONNAME1", rl1test.getName());
				Assert.assertEquals("List Location 1 sequence id", 1, rl1test.getSequenceId());
			}
			if (i == 1) {			
				Assert.assertEquals("List Location 2 before", "TESTLOCATIONNAME2", rl1test.getName());
				Assert.assertEquals("List Location 2 sequence id", 2, rl1test.getSequenceId());
			}
			if (i == 2) {			
				Assert.assertEquals("List Location 3 before", "TESTLOCATIONNAME3", rl1test.getName());
				Assert.assertEquals("List Location 3 sequence id", 3, rl1test.getSequenceId());
			}
		}

		//  Add a fourth location but put it in the second spot and check that locations are in the expected order
		Location l4 = new Location("TESTLOCATIONID4", "TESTLOCATIONNAME4");
		rladd = r1.addLocation(l4,2);

		rl1test= r1.getLastLocationByName("TESTLOCATIONNAME4");
		Assert.assertEquals("Add Location 4", "TESTLOCATIONNAME4", rl1test.getName());

		list = r1.getLocationsBySequenceList();
		for (int i = 0; i < list.size(); i++) {
			rl1test = r1.getLocationById(list.get(i));
			if (i == 0) {			
				Assert.assertEquals("List Location 1 after", "TESTLOCATIONNAME1", rl1test.getName());
				Assert.assertEquals("List Location 1 sequence id", 1, rl1test.getSequenceId());
			}
			if (i == 1) {			
				Assert.assertEquals("List Location 2 after", "TESTLOCATIONNAME4", rl1test.getName());
				Assert.assertEquals("List Location 2 sequence id", 2, rl1test.getSequenceId());
			}
			if (i == 2) {			
				Assert.assertEquals("List Location 3 after", "TESTLOCATIONNAME2", rl1test.getName());
				Assert.assertEquals("List Location 3 sequence id", 3, rl1test.getSequenceId());
			}
			if (i == 3) {			
				Assert.assertEquals("List Location 4 after", "TESTLOCATIONNAME3", rl1test.getName());
				Assert.assertEquals("List Location 4 sequence id", 4, rl1test.getSequenceId());
			}
		}

		//  Move up the third location and check that locations are in the expected order
		rl1test= r1.getLastLocationByName("TESTLOCATIONNAME3");
		r1.moveLocationUp(rl1test);
		list = r1.getLocationsBySequenceList();
		for (int i = 0; i < list.size(); i++) {
			rl1test = r1.getLocationById(list.get(i));
			if (i == 0) {			
				Assert.assertEquals("List Location 1 after move up", "TESTLOCATIONNAME1", rl1test.getName());
				Assert.assertEquals("List Location 1 sequence id", 1, rl1test.getSequenceId());
			}
			if (i == 1) {			
				Assert.assertEquals("List Location 2 after move up", "TESTLOCATIONNAME4", rl1test.getName());
				Assert.assertEquals("List Location 2 sequence id", 2, rl1test.getSequenceId());
			}
			if (i == 2) {			
				Assert.assertEquals("List Location 3 after move up", "TESTLOCATIONNAME3", rl1test.getName());
				Assert.assertEquals("List Location 3 sequence id", 3, rl1test.getSequenceId());
			}
			if (i == 3) {			
				Assert.assertEquals("List Location 4 after move up", "TESTLOCATIONNAME2", rl1test.getName());
				Assert.assertEquals("List Location 4 sequence id", 4, rl1test.getSequenceId());
			}
		}

		//  Move down the first location down 2 and check that locations are in the expected order
		rl1test= r1.getLastLocationByName("TESTLOCATIONNAME1");
		r1.moveLocationDown(rl1test);
		r1.moveLocationDown(rl1test);
		list = r1.getLocationsBySequenceList();
		for (int i = 0; i < list.size(); i++) {
			rl1test = r1.getLocationById(list.get(i));
			if (i == 0) {			
				Assert.assertEquals("List Location 1 after move up", "TESTLOCATIONNAME4", rl1test.getName());
				Assert.assertEquals("List Location 1 sequence id", 1, rl1test.getSequenceId());
			}
			if (i == 1) {			
				Assert.assertEquals("List Location 2 after move up", "TESTLOCATIONNAME3", rl1test.getName());
				Assert.assertEquals("List Location 2 sequence id", 2, rl1test.getSequenceId());
			}
			if (i == 2) {			
				Assert.assertEquals("List Location 3 after move up", "TESTLOCATIONNAME1", rl1test.getName());
				Assert.assertEquals("List Location 3 sequence id", 3, rl1test.getSequenceId());
			}
			if (i == 3) {			
				Assert.assertEquals("List Location 4 after move up", "TESTLOCATIONNAME2", rl1test.getName());
				Assert.assertEquals("List Location 4 sequence id", 4, rl1test.getSequenceId());
			}
		}

		//  Delete the third location and check that locations are in the expected order
		rl1test= r1.getLastLocationByName("TESTLOCATIONNAME3");
		r1.deleteLocation(rl1test);
		list = r1.getLocationsBySequenceList();
		for (int i = 0; i < list.size(); i++) {
			rl1test = r1.getLocationById(list.get(i));
			if (i == 0) {			
				Assert.assertEquals("List Location 1 after move up", "TESTLOCATIONNAME4", rl1test.getName());
				Assert.assertEquals("List Location 1 sequence id", 1, rl1test.getSequenceId());
			}
			if (i == 1) {			
				Assert.assertEquals("List Location 2 after move up", "TESTLOCATIONNAME1", rl1test.getName());
				Assert.assertEquals("List Location 2 sequence id", 2, rl1test.getSequenceId());
			}
			if (i == 2) {			
				Assert.assertEquals("List Location 3 after move up", "TESTLOCATIONNAME2", rl1test.getName());
				Assert.assertEquals("List Location 3 sequence id", 3, rl1test.getSequenceId());
			}
		}
	}
	
	public void testRouteManager(){
		RouteManager rm = RouteManager.instance();
		List<String> listById = rm.getRoutesByIdList();
		List<String> listByName = rm.getRoutesByNameList();
		
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
		for (int i=0; i<listById.size(); i++){
			Route r = rm.getRouteById(listById.get(i));
			if (i == 0){
				Assert.assertEquals("First route name by id", "testRoute", r.getName());
			}
			if (i == 1){
				Assert.assertEquals("2nd route name by id", "atestRoute", r.getName());
			}
			if (i == 2){
				Assert.assertEquals("3rd route name by id", "ztestRoute", r.getName());
			}
			if (i == 3){
				Assert.assertEquals("4th route name by id", "dtestRoute", r.getName());
			}
		}
		
		// check the order
		for (int i=0; i<listByName.size(); i++){
			Route r = rm.getRouteById(listByName.get(i));
			if (i == 0){
				Assert.assertEquals("First route name by id", "atestRoute", r.getName());
			}
			if (i == 1){
				Assert.assertEquals("2nd route name by id", "dtestRoute", r.getName());
			}
			if (i == 2){
				Assert.assertEquals("3rd route name by id", "testRoute", r.getName());
			}
			if (i == 3){
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
		for (int i=0; i<listById.size(); i++){
			Route r = rm.getRouteById(listById.get(i));
			if (i == 0){
				Assert.assertEquals("First route name by id", "atestRoute", r.getName());
			}
			if (i == 1){
				Assert.assertEquals("2nd route name by id", "ztestRoute", r.getName());
			}
			if (i == 2){
				Assert.assertEquals("4th route name by id", "dtestRoute", r.getName());
			}
		}
		
		// check the order
		for (int i=0; i<listByName.size(); i++){
			Route r = rm.getRouteById(listByName.get(i));
			if (i == 0){
				Assert.assertEquals("First route name by id", "atestRoute", r.getName());
			}
			if (i == 1){
				Assert.assertEquals("2nd route name by id", "dtestRoute", r.getName());
			}
			if (i == 2){
				Assert.assertEquals("3rd route name by id", "ztestRoute", r.getName());
			}
		}
		
		// test the combo box
		JComboBox b = rm.getComboBox();
		Assert.assertEquals("ComboBox item count", 4, b.getItemCount());
		Assert.assertEquals("First combo item", "", b.getItemAt(0));
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
		for (int i=0; i<listById.size(); i++){
			Route r = rm.getRouteById(listById.get(i));
			if (i == 0){
				Assert.assertEquals("First route name by id", "atestRoute", r.getName());
			}
		}
		
		// check the order
		for (int i=0; i<listByName.size(); i++){
			Route r = rm.getRouteById(listByName.get(i));
			if (i == 0){
				Assert.assertEquals("First route name by id", "atestRoute", r.getName());
			}
		}
		
		// test combo box update
		rm.updateComboBox(b);
		Assert.assertEquals("ComboBox item count", 2, b.getItemCount());
		Assert.assertEquals("First combo item", "", b.getItemAt(0));
		Assert.assertEquals("First combo route", route2, b.getItemAt(1));

		// finish up by deleting the last route
		rm.deregister(route5);
		listById = rm.getRoutesByIdList();
		listByName = rm.getRoutesByNameList();
		Assert.assertEquals("Route id list is empty", true, listById.isEmpty());
		Assert.assertEquals("Route name list is empty", true, listByName.isEmpty());
	}
	
	// test route status
	public void testRouteStatus(){
		RouteManager rm = RouteManager.instance();
		Route r = rm.newRoute("TestRouteStatus");		
		// note that the status strings are defined in JmritOperationsRoutesBundle.properties
		Assert.assertEquals("Route status error", "Error", r.getStatus());
		
		// now add a location to the route
		Location l = LocationManager.instance().newLocation("TestRouteStatusLoc");
		r.addLocation(l);
		// note that the status strings are defined in JmritOperationsRoutesBundle.properties
		Assert.assertEquals("Route status ophan", "Orphan", r.getStatus());
		
		// now connect route to a train
		Train t = TrainManager.instance().newTrain("TestRouteStatusTrain");
		t.setRoute(r);
		// note that the status strings are defined in JmritOperationsRoutesBundle.properties
		Assert.assertEquals("Route status ophan", "Okay", r.getStatus());
	}

	// test location Xml create support
	public void testXMLCreate() throws Exception {

                RouteManager manager = RouteManager.instance();
                List<String> temprouteList = manager.getRoutesByIdList();

                Assert.assertEquals("Starting Number of Routes", 0, temprouteList.size());
                manager.newRoute("Test Number 1");
                manager.newRoute("Test Number 2");
                manager.newRoute("Test Number 3");

                temprouteList = manager.getRoutesByIdList();

                Assert.assertEquals("New Number of Routes", 3, temprouteList.size());
/*                
                Assert.assertEquals("New Engine by Id 1", "Test Number 1", manager.getById("CPTest Number 1").getNumber());
                Assert.assertEquals("New Engine by Id 2", "Test Number 2", manager.getById("ACLTest Number 2").getNumber());
                Assert.assertEquals("New Engine by Id 3", "Test Number 3", manager.getById("CPTest Number 3").getNumber());

                Assert.assertEquals("New Location by Road+Name 1", "Test Number 1", manager.getByRoadAndNumber("CP", "Test Number 1").getNumber());
                Assert.assertEquals("New Location by Road+Name 2", "Test Number 2", manager.getByRoadAndNumber("ACL", "Test Number 2").getNumber());
                Assert.assertEquals("New Location by Road+Name 3", "Test Number 3", manager.getByRoadAndNumber("CP", "Test Number 3").getNumber());

                manager.getByRoadAndNumber("CP", "Test Number 1").setBuilt("1923");
                manager.getByRoadAndNumber("CP", "Test Number 1").setColor("Black");
                manager.getByRoadAndNumber("CP", "Test Number 1").setComment("Nice runner");
//                manager.getByRoadAndNumber("CP", "Test Number 1").setConsist(consist);
//                manager.getByRoadAndNumber("CP", "Test Number 1").setDestination(destination, track);
                manager.getByRoadAndNumber("CP", "Test Number 1").setHp("23");
                manager.getByRoadAndNumber("CP", "Test Number 1").setLength("50");
//                manager.getByRoadAndNumber("CP", "Test Number 1").setLocation(location, track);
//                manager.getByRoadAndNumber("CP", "Test Number 1").setModel("E8");
                manager.getByRoadAndNumber("CP", "Test Number 1").setMoves(5);
                manager.getByRoadAndNumber("CP", "Test Number 1").setOwner("TestOwner");
//                manager.getByRoadAndNumber("CP", "Test Number 1").setRouteDestination(routeDestination);
//                manager.getByRoadAndNumber("CP", "Test Number 1").setRouteLocation(routeLocation);
//                manager.getByRoadAndNumber("CP", "Test Number 1").setSavedRouteId(id);
//                manager.getByRoadAndNumber("CP", "Test Number 1").setTrain(train);
                manager.getByRoadAndNumber("CP", "Test Number 1").setWeight("87");
                manager.getByRoadAndNumber("CP", "Test Number 1").setWeightTons("97");
                
                
                manager.getByRoadAndNumber("CP", "Test Number 1").setType("Gas Turbine");
                
                manager.getByRoadAndNumber("CP", "Test Number 1").setModel("E8");
*/                
/*
		manager.getLastLocationByName("Test Location 1").setLocationOps(Location.NORMAL);
		manager.getLastLocationByName("Test Location 1").setSwitchList(true);
		manager.getLastLocationByName("Test Location 1").setTrainDirections(Location.EAST+Location.WEST);
		manager.getLastLocationByName("Test Location 1").addTypeName("Baggage");
		manager.getLastLocationByName("Test Location 1").addTypeName("BoxCar");
		manager.getLastLocationByName("Test Location 1").addTypeName("Caboose");
		manager.getLastLocationByName("Test Location 1").addTypeName("Coal");
		manager.getLastLocationByName("Test Location 1").addTypeName("Engine");
		manager.getLastLocationByName("Test Location 1").addTypeName("Hopper");
                manager.getLastLocationByName("Test Location 2").setComment("Test Location 2 Comment");
		manager.getLastLocationByName("Test Location 2").setLocationOps(Location.NORMAL);
		manager.getLastLocationByName("Test Location 2").setSwitchList(true);
		manager.getLastLocationByName("Test Location 2").setTrainDirections(Location.EAST+Location.WEST);
		manager.getLastLocationByName("Test Location 2").addTypeName("Baggage");
		manager.getLastLocationByName("Test Location 2").addTypeName("BoxCar");
		manager.getLastLocationByName("Test Location 2").addTypeName("Caboose");
		manager.getLastLocationByName("Test Location 2").addTypeName("Coal");
		manager.getLastLocationByName("Test Location 2").addTypeName("Engine");
		manager.getLastLocationByName("Test Location 2").addTypeName("Hopper");
                manager.getLastLocationByName("Test Location 3").setComment("Test Location 3 Comment");
		manager.getLastLocationByName("Test Location 3").setLocationOps(Location.NORMAL);
		manager.getLastLocationByName("Test Location 3").setSwitchList(true);
		manager.getLastLocationByName("Test Location 3").setTrainDirections(Location.EAST+Location.WEST);
		manager.getLastLocationByName("Test Location 3").addTypeName("Baggage");
		manager.getLastLocationByName("Test Location 3").addTypeName("BoxCar");
		manager.getLastLocationByName("Test Location 3").addTypeName("Caboose");
		manager.getLastLocationByName("Test Location 3").addTypeName("Coal");
		manager.getLastLocationByName("Test Location 3").addTypeName("Engine");
		manager.getLastLocationByName("Test Location 3").addTypeName("Hopper");
*/
/*                
                locationList = manager.getLocationsByIdList();
                Assert.assertEquals("New Number of Locations", 3, locationList.size());

                for (int i = 0; i < locationList.size(); i++) {
                    String locationId = (String)locationList.get(i);
                    Location loc = manager.getLocationById(locationId);
                    String locname = loc.getName();
                    if (i == 0) {
                        Assert.assertEquals("New Location by Id List 1", "Test Location 2", locname);
                    }
                    if (i == 1) {
                        Assert.assertEquals("New Location by Id List 2", "Test Location 1", locname);
                    }
                    if (i == 2) {
                        Assert.assertEquals("New Location by Id List 3", "Test Location 3", locname);
                    }
                }

*/
/*                
                locationList = manager.getLocationsByNameList();
                Assert.assertEquals("New Number of Locations", 3, locationList.size());

                for (int i = 0; i < locationList.size(); i++) {
                    String locationId = (String)locationList.get(i);
                    Location loc = manager.getLocationById(locationId);
                    String locname = loc.getName();
                    if (i == 0) {
                        Assert.assertEquals("New Location by Name List 1", "Test Location 1", locname);
                    }
                    if (i == 1) {
                        Assert.assertEquals("New Location by Name List 2", "Test Location 2", locname);
                    }
                    if (i == 2) {
                        Assert.assertEquals("New Location by Name List 3", "Test Location 3", locname);
                    }
                }
*/
                

                RouteManagerXml.instance().writeOperationsFile();

                // Add some more engines and write file again
                // so we can test the backup facility
                manager.newRoute("Test Number 4");
                manager.newRoute("Test Number 5");
                manager.newRoute("Test Number 6");
//                manager.getRouteByRoadAndNumber("ACL", "Test Number 2").setComment("Test Engine 2 Changed Comment");
                
                RouteManagerXml.instance().writeOperationsFile();
        }

	// TODO: Add tests for Route location track location

	// TODO: Add test to create xml file

	// TODO: Add test to read xml file

	// from here down is testing infrastructure

    // Ensure minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        
		// Repoint OperationsSetupXml to JUnitTest subdirectory
		OperationsSetupXml.setOperationsDirectoryName("operations"+File.separator+"JUnitTest");
		// Change file names to ...Test.xml
		OperationsSetupXml.instance().setOperationsFileName("OperationsJUnitTest.xml"); 
		RouteManagerXml.instance().setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
		EngineManagerXml.instance().setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
		CarManagerXml.instance().setOperationsFileName("OperationsJUnitTestCarRoster.xml");
		LocationManagerXml.instance().setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
		TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml");

        // Need to clear out RouteManager global variables
        RouteManager manager = RouteManager.instance();
        manager.dispose();
    }

	public OperationsRoutesTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsRoutesTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsRoutesTest.class);
		return suite;
	}

    // The minimal setup for log4J
    @Override
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
