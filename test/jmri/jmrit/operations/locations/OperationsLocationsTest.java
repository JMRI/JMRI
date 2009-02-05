// OperationsLocationsTest.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import java.util.List;
import java.io.File;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.InstanceManager;
import jmri.managers.InternalTurnoutManager;
import jmri.managers.InternalSensorManager;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;

/**
 * Tests for the Operations Locations class
 * Last manually cross-checked on 20090131
 * 
 * Still to do:
 *   ScheduleItem: XML read/write
 *   Schedule: Register, List, XML read/write
 *   Track: AcceptsDropTrain, AcceptsDropRoute
 *   Track: AcceptsPickupTrain, AcceptsPickupRoute
 *   Track: CheckScheduleValid
 *   Track: XML read/write
 *   Location: Track support  <-- I am here
 *   Location: XML read/write
 *  
 * @author	Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision: 1.8 $
 */
public class OperationsLocationsTest extends TestCase {

        // test Location Class (part one)
        // test Location creation
	public void testCreate() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());
		l.setName("New Test Name");
		Assert.assertEquals("New Location Name", "New Test Name", l.getName());
		l.setComment("Test Location Comment");
		Assert.assertEquals("Location Comment", "Test Location Comment", l.getComment());
	}

	// test Location public constants
	public void testLocationConstants() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		Assert.assertEquals("Location Constant NORMAL", 1, Location.NORMAL);
		Assert.assertEquals("Location Constant STAGING", 2, Location.STAGING);

		Assert.assertEquals("Location Constant EAST", 1, Location.EAST);
		Assert.assertEquals("Location Constant WEST", 2, Location.WEST);
		Assert.assertEquals("Location Constant NORTH", 4, Location.NORTH);
		Assert.assertEquals("Location Constant SOUTH", 8, Location.SOUTH);

		Assert.assertEquals("Location Constant YARDLISTLENGTH_CHANGED_PROPERTY", "yardListLength", Location.YARDLISTLENGTH_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant SIDINGLISTLENGTH_CHANGED_PROPERTY", "sidingListLength", Location.SIDINGLISTLENGTH_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant INTERCHANGELISTLENGTH_CHANGED_PROPERTY", "sidingListLength", Location.INTERCHANGELISTLENGTH_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant STAGINGLISTLENGTH_CHANGED_PROPERTY", "sidingListLength", Location.STAGINGLISTLENGTH_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant TYPES_CHANGED_PROPERTY", "types", Location.TYPES_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant TRAINDIRECTION_CHANGED_PROPERTY", "trainDirection", Location.TRAINDIRECTION_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant LENGTH_CHANGED_PROPERTY", "length", Location.LENGTH_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant USEDLENGTH_CHANGED_PROPERTY", "usedLength", Location.USEDLENGTH_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant NAME_CHANGED_PROPERTY", "name", Location.NAME_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant SWITCHLIST_CHANGED_PROPERTY", "switchList", Location.SWITCHLIST_CHANGED_PROPERTY);
		Assert.assertEquals("Location Constant DISPOSE_CHANGED_PROPERTY", "dispose", Location.DISPOSE_CHANGED_PROPERTY);
	}
        
        // test ScheduleItem class
        // test ScheduleItem public constants
	public void testScheduleItemConstants() {
		Assert.assertEquals("Location ScheduleItem Constant NUMBER_CHANGED_PROPERTY", "number", ScheduleItem.NUMBER_CHANGED_PROPERTY);
		Assert.assertEquals("Location ScheduleItem Constant TYPE_CHANGED_PROPERTY", "type", ScheduleItem.TYPE_CHANGED_PROPERTY);
		Assert.assertEquals("Location ScheduleItem Constant ROAD_CHANGED_PROPERTY", "road", ScheduleItem.ROAD_CHANGED_PROPERTY);
		Assert.assertEquals("Location ScheduleItem Constant LOAD_CHANGED_PROPERTY", "load", ScheduleItem.LOAD_CHANGED_PROPERTY);
		Assert.assertEquals("Location ScheduleItem Constant DISPOSE", "dispose", ScheduleItem.DISPOSE);
        }

	// test ScheduleItem attributes
	public void testScheduleItemAttributes() {
		ScheduleItem ltsi = new ScheduleItem("Test id", "Test Type");
		Assert.assertEquals("Location ScheduleItem id", "Test id", ltsi.getId());
		Assert.assertEquals("Location ScheduleItem Type", "Test Type", ltsi.getType());

                ltsi.setType("New Test Type");
		Assert.assertEquals("Location ScheduleItem set Type", "New Test Type", ltsi.getType());

                ltsi.setComment("New Test Comment");
		Assert.assertEquals("Location ScheduleItem set Comment", "New Test Comment", ltsi.getComment());

                ltsi.setRoad("New Test Road");
		Assert.assertEquals("Location ScheduleItem set Road", "New Test Road", ltsi.getRoad());

                ltsi.setLoad("New Test Load");
		Assert.assertEquals("Location ScheduleItem set Load", "New Test Load", ltsi.getLoad());

                ltsi.setShip("New Test Ship");
		Assert.assertEquals("Location ScheduleItem set Ship", "New Test Ship", ltsi.getShip());

                ltsi.setSequenceId(22);
		Assert.assertEquals("Location ScheduleItem set SequenceId", 22, ltsi.getSequenceId());

                ltsi.setCount(222);
		Assert.assertEquals("Location ScheduleItem set Count", 222, ltsi.getCount());
        }
        
        // test Schedule class
        // test schedule public constants
	public void testScheduleConstants() {
		Assert.assertEquals("Location Schedule Constant LISTCHANGE_CHANGED_PROPERTY", "listChange", Schedule.LISTCHANGE_CHANGED_PROPERTY);
		Assert.assertEquals("Location Schedule Constant DISPOSE", "dispose", Schedule.DISPOSE);
        }

	// test schedule attributes
	public void testScheduleAttributes() {
		Schedule lts = new Schedule("Test id", "Test Name");
		Assert.assertEquals("Location Schedule id", "Test id", lts.getId());
		Assert.assertEquals("Location Schedule Name", "Test Name", lts.getName());

                lts.setName("New Test Name");
		Assert.assertEquals("Location Schedule set Name", "New Test Name", lts.getName());
		Assert.assertEquals("Location Schedule toString", "New Test Name", lts.toString());

                lts.setComment("New Test Comment");
		Assert.assertEquals("Location Schedule set Comment", "New Test Comment", lts.getComment());
        }
        
	// test schedule scheduleitem
	public void testScheduleScheduleItems() {
		Schedule lts = new Schedule("Test id", "Test Name");
		Assert.assertEquals("Location Schedule ScheduleItem id", "Test id", lts.getId());
		Assert.assertEquals("Location Schedule ScheduleItem Name", "Test Name", lts.getName());

		ScheduleItem ltsi1, ltsi2, ltsi3, ltsi4;

                ltsi1 = lts.addItem("New Test Type");
		Assert.assertEquals("Location Schedule ScheduleItem Check Type", "New Test Type", ltsi1.getType());

                String testid = ltsi1.getId();
                ltsi2 = lts.getItemByType("New Test Type");
		Assert.assertEquals("Location Schedule ScheduleItem Check Ids", testid, ltsi2.getId());
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 0", 1, ltsi2.getSequenceId());

                ltsi3 = lts.addItem("New Second Test Type");
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 1", 1, ltsi1.getSequenceId());
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 2", 2, ltsi3.getSequenceId());
                
                lts.moveItemUp(ltsi3);
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 3", 2, ltsi1.getSequenceId());
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 4", 1, ltsi3.getSequenceId());

                ltsi4 = lts.addItem("New Third Test Type", 2);
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 5", 3, ltsi1.getSequenceId());
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 6", 1, ltsi3.getSequenceId());
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 7", 2, ltsi4.getSequenceId());

                lts.moveItemDown(ltsi3);
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 8", 3, ltsi1.getSequenceId());
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 9", 2, ltsi3.getSequenceId());
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 10", 1, ltsi4.getSequenceId());

                lts.deleteItem(ltsi3);
                /* This should be 2 not 3 */
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 8", 3, ltsi1.getSequenceId());
		Assert.assertEquals("Location Schedule ScheduleItem Check Seq 9", 1, ltsi4.getSequenceId());
        }

        // test Track class
	// test Track public constants
	public void testTrackConstants() {
		Assert.assertEquals("Location Track Constant ANY", "Any", Track.ANY);
		Assert.assertEquals("Location Track Constant TRAINS", "trains", Track.TRAINS);
		Assert.assertEquals("Location Track Constant ROUTES", "routes", Track.ROUTES);

		Assert.assertEquals("Location Track Constant STAGING", "Staging", Track.STAGING);
		Assert.assertEquals("Location Track Constant INTERCHANGE", "Interchange", Track.INTERCHANGE);
		Assert.assertEquals("Location track Constant YARD", "Yard", Track.YARD);
		Assert.assertEquals("Location Track Constant SIDING", "Siding", Track.SIDING);

		Assert.assertEquals("Location Track Constant EAST", 1, Track.EAST);
		Assert.assertEquals("Location Track Constant WEST", 2, Track.WEST);
		Assert.assertEquals("Location track Constant NORTH", 4, Track.NORTH);
		Assert.assertEquals("Location Track Constant SOUTH", 8, Track.SOUTH);

		Assert.assertEquals("Location Track Constant ALLROADS", "All", Track.ALLROADS);
		Assert.assertEquals("Location Track Constant INCLUDEROADS", "Include", Track.INCLUDEROADS);
		Assert.assertEquals("Location track Constant EXCLUDEROADS", "Exclude", Track.EXCLUDEROADS);

		Assert.assertEquals("Location Track Constant TYPES_CHANGED_PROPERTY", "types", Track.TYPES_CHANGED_PROPERTY);
		Assert.assertEquals("Location Track Constant ROADS_CHANGED_PROPERTY", "roads", Track.ROADS_CHANGED_PROPERTY);
		Assert.assertEquals("Location track Constant SCHEDULE_CHANGED_PROPERTY", "schedule change", Track.SCHEDULE_CHANGED_PROPERTY);
		Assert.assertEquals("Location track Constant DISPOSE_CHANGED_PROPERTY", "dispose", Track.DISPOSE_CHANGED_PROPERTY);
        }

	// test Track attributes
	public void testTrackAttributes() {
		Track t = new Track("Test id", "Test Name", "Test Type");
		Assert.assertEquals("Location Track id", "Test id", t.getId());
		Assert.assertEquals("Location Track Name", "Test Name", t.getName());
		Assert.assertEquals("Location Track Type", "Test Type", t.getLocType());

                t.setName("New Test Name");
		Assert.assertEquals("Location Track set Name", "New Test Name", t.getName());

                t.setComment("New Test Comment");
		Assert.assertEquals("Location Track set Comment", "New Test Comment", t.getComment());

                t.setMoves(40);
		Assert.assertEquals("Location Track Moves", 40, t.getMoves());

                t.setLength(400);
		Assert.assertEquals("Location Track Length", 400, t.getLength());

		t.setReserved(200);
		Assert.assertEquals("Location Track Reserved", 200, t.getReserved());

		t.setUsedLength(100);
		Assert.assertEquals("Location Track Used Length", 100, t.getUsedLength());

		t.setTrainDirections(Track.NORTH);
		Assert.assertEquals("Location Track Direction North", Track.NORTH, t.getTrainDirections());

		t.setTrainDirections(Track.SOUTH);
		Assert.assertEquals("Location Track Direction South", Track.SOUTH, t.getTrainDirections());

		t.setTrainDirections(Track.EAST);
		Assert.assertEquals("Location Track Direction East", Track.EAST, t.getTrainDirections());

		t.setTrainDirections(Track.WEST);
		Assert.assertEquals("Location Track Direction West", Track.WEST, t.getTrainDirections());

		t.setTrainDirections(Track.NORTH+Track.SOUTH);
		Assert.assertEquals("Location Track Direction North+South", Track.NORTH+Track.SOUTH, t.getTrainDirections());

		t.setTrainDirections(Track.EAST+Track.WEST);
		Assert.assertEquals("Location Track Direction East+West", Track.EAST+Track.WEST, t.getTrainDirections());

		t.setTrainDirections(Track.NORTH+Track.SOUTH+Track.EAST+Track.WEST);
		Assert.assertEquals("Location Track Direction North+South+East+West", Track.NORTH+Track.SOUTH+Track.EAST+Track.WEST, t.getTrainDirections());

                t.setRoadOption("New Test Road Option");
		Assert.assertEquals("Location Track set Road Option", "New Test Road Option", t.getRoadOption());

                t.setDropOption("New Test Drop Option");
		Assert.assertEquals("Location Track set Drop Option", "New Test Drop Option", t.getDropOption());

                t.setPickupOption("New Test Pickup Option");
		Assert.assertEquals("Location Track set Pickup Option", "New Test Pickup Option", t.getPickupOption());
        }

	// test Track car support
	public void testTrackCarSupport() {
		Track t = new Track("Test id", "Test Name", "Test Type");
		Assert.assertEquals("Location Track Car id", "Test id", t.getId());
		Assert.assertEquals("Location Track Car Name", "Test Name", t.getName());
		Assert.assertEquals("Location Track Car Type", "Test Type", t.getLocType());

		Assert.assertEquals("Location Track Car Start Used Length", 0, t.getUsedLength());
		Assert.assertEquals("Location Track Car Start Number of Rolling Stock", 0, t.getNumberRS());
		Assert.assertEquals("Location Track Car Start Number of Cars", 0, t.getNumberCars());
		Assert.assertEquals("Location Track Car Start Number of Engines", 0, t.getNumberEngines());

		jmri.jmrit.operations.rollingstock.cars.Car c1 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER1");
		c1.setLength("40");
		t.addRS(c1);

		Assert.assertEquals("Location Track Car First Number of Rolling Stock", 1, t.getNumberRS());
		Assert.assertEquals("Location Track Car First Number of Cars", 1, t.getNumberCars());
		Assert.assertEquals("Location Track Car First Number of Engines", 0, t.getNumberEngines());
		Assert.assertEquals("Location Track Car First Used Length", 40+4, t.getUsedLength()); // Drawbar length is 4

		jmri.jmrit.operations.rollingstock.cars.Car c2 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER2");
		c2.setLength("33");
		t.addRS(c2);

		Assert.assertEquals("Location Track Car 2nd Number of Rolling Stock", 2, t.getNumberRS());
		Assert.assertEquals("Location Track Car 2nd Number of Cars", 2, t.getNumberCars());
		Assert.assertEquals("Location Track Car 2nd Number of Engines", 0, t.getNumberEngines());
		Assert.assertEquals("Location Track Car 2nd Used Length", 40+4+33+4, t.getUsedLength()); // Drawbar length is 4

		jmri.jmrit.operations.rollingstock.engines.Engine e1 = new jmri.jmrit.operations.rollingstock.engines.Engine("TESTROAD", "TESTNUMBERE1");
		e1.setLength("80");
		t.addRS(e1);

		Assert.assertEquals("Location Track Car 3rd Number of Rolling Stock", 3, t.getNumberRS());
		Assert.assertEquals("Location Track Car 3rd Number of Cars", 2, t.getNumberCars());
		Assert.assertEquals("Location Track Car 3rd Number of Engines", 1, t.getNumberEngines());
		Assert.assertEquals("Location Track Car 3rd Used Length", 40+4+33+4+80+4, t.getUsedLength()); // Drawbar length is 4

		jmri.jmrit.operations.rollingstock.cars.Car c3 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER3");
		c3.setLength("50");
		t.addRS(c3);

		Assert.assertEquals("Location Track Car 4th Number of Rolling Stock", 4, t.getNumberRS());
		Assert.assertEquals("Location Track Car 4th Number of Cars", 3, t.getNumberCars());
		Assert.assertEquals("Location Track Car 4th Number of Engines", 1, t.getNumberEngines());
		Assert.assertEquals("Location Track Car 4th Used Length", 40+4+33+4+80+4+50+4, t.getUsedLength()); // Drawbar length is 4

		jmri.jmrit.operations.rollingstock.engines.Engine e2 = new jmri.jmrit.operations.rollingstock.engines.Engine("TESTROAD", "TESTNUMBERE2");
		e2.setLength("80");
		t.addRS(e2);

		Assert.assertEquals("Location Track Car 5th Number of Rolling Stock", 5, t.getNumberRS());
		Assert.assertEquals("Location Track Car 5th Number of Cars", 3, t.getNumberCars());
		Assert.assertEquals("Location Track Car 5th Number of Engines", 2, t.getNumberEngines());
		Assert.assertEquals("Location Track Car 5th Used Length", 40+4+33+4+80+4+50+4+80+4, t.getUsedLength()); // Drawbar length is 4

		t.deleteRS(c2);

		Assert.assertEquals("Location Track Car 6th Number of Rolling Stock", 4, t.getNumberRS());
		Assert.assertEquals("Location Track Car 6th Number of Cars", 2, t.getNumberCars());
		Assert.assertEquals("Location Track Car 6th Number of Engines", 2, t.getNumberEngines());
		Assert.assertEquals("Location Track Car 6th Used Length", 40+4+80+4+50+4+80+4, t.getUsedLength()); // Drawbar length is 4

		t.deleteRS(c1);

		Assert.assertEquals("Location Track Car 7th Number of Rolling Stock", 3, t.getNumberRS());
		Assert.assertEquals("Location Track Car 7th Number of Cars", 1, t.getNumberCars());
		Assert.assertEquals("Location Track Car 7th Number of Engines", 2, t.getNumberEngines());
		Assert.assertEquals("Location Track Car 7th Used Length", 80+4+50+4+80+4, t.getUsedLength()); // Drawbar length is 4

		t.deleteRS(e2);

		Assert.assertEquals("Location Track Car 8th Number of Rolling Stock", 2, t.getNumberRS());
		Assert.assertEquals("Location Track Car 8th Number of Cars", 1, t.getNumberCars());
		Assert.assertEquals("Location Track Car 8th Number of Engines", 1, t.getNumberEngines());
		Assert.assertEquals("Location Track Car 8th Used Length", 80+4+50+4, t.getUsedLength()); // Drawbar length is 4

		t.deleteRS(e1);

		Assert.assertEquals("Location Track Car 9th Number of Rolling Stock", 1, t.getNumberRS());
		Assert.assertEquals("Location Track Car 9th Number of Cars", 1, t.getNumberCars());
		Assert.assertEquals("Location Track Car 9th Number of Engines", 0, t.getNumberEngines());
		Assert.assertEquals("Location Track Car 9th Used Length", 50+4, t.getUsedLength()); // Drawbar length is 4

                t.deleteRS(c3);

		Assert.assertEquals("Location Track Car Last Number of Rolling Stock", 0, t.getNumberRS());
		Assert.assertEquals("Location Track Car Last Number of Cars", 0, t.getNumberCars());
		Assert.assertEquals("Location Track Car Last Number of Engines", 0, t.getNumberEngines());
		Assert.assertEquals("Location Track Car Last Used Length", 0, t.getUsedLength()); // Drawbar length is 4
	}

	// test Track pickup support
	public void testTrackPickUpSupport() {
		Track t = new Track("Test id", "Test Name", "Test Type");
		Assert.assertEquals("Location Track Car id", "Test id", t.getId());
		Assert.assertEquals("Location Track Car Name", "Test Name", t.getName());
		Assert.assertEquals("Location Track Car Type", "Test Type", t.getLocType());

		Assert.assertEquals("Location Track Pick Ups Start", 0, t.getPickupRS());

		t.addPickupRS();
		Assert.assertEquals("Location Track Pick Ups 1st", 1, t.getPickupRS());

		t.addPickupRS();
		Assert.assertEquals("Location Track Pick Ups 2nd", 2, t.getPickupRS());

		t.deletePickupRS();
		Assert.assertEquals("Location Track Pick Ups 3rd", 1, t.getPickupRS());

		t.deletePickupRS();
		Assert.assertEquals("Location Track Pick Ups 4th", 0, t.getPickupRS());
	}

	// test Track drop support
	public void testTrackDropSupport() {
		Track t = new Track("Test id", "Test Name", "Test Type");
		Assert.assertEquals("Location Track Car id", "Test id", t.getId());
		Assert.assertEquals("Location Track Car Name", "Test Name", t.getName());
		Assert.assertEquals("Location Track Car Type", "Test Type", t.getLocType());

		Assert.assertEquals("Location Track Drops Start", 0, t.getDropRS());
		Assert.assertEquals("Location Track Drops Start Reserved", 0, t.getReserved());

		jmri.jmrit.operations.rollingstock.cars.Car c1 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER1");
		c1.setLength("40");
		t.addDropRS(c1);
		Assert.assertEquals("Location Track Drops 1st", 1, t.getDropRS());
		Assert.assertEquals("Location Track Drops 1st Reserved", 40+4, t.getReserved());

		jmri.jmrit.operations.rollingstock.cars.Car c2 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER2");
		c2.setLength("50");
		t.addDropRS(c2);
		Assert.assertEquals("Location Track Drops 2nd", 2, t.getDropRS());
		Assert.assertEquals("Location Track Drops 2nd Reserved", 40+4+50+4, t.getReserved());

		t.deleteDropRS(c2);
		Assert.assertEquals("Location Track Drops 3rd", 1, t.getDropRS());
		Assert.assertEquals("Location Track Drops 3rd Reserved", 40+4, t.getReserved());

		t.deleteDropRS(c1);
		Assert.assertEquals("Location Track Drops 4th", 0, t.getDropRS());
       		Assert.assertEquals("Location Track Drops 4th Reserved", 0, t.getReserved());
	}

	// test Track typename support
	public void testTrackTypeNameSupport() {
		Track t = new Track("Test id", "Test Name", "Test Type");
		Assert.assertEquals("Location Track Car id", "Test id", t.getId());
		Assert.assertEquals("Location Track Car Name", "Test Name", t.getName());
		Assert.assertEquals("Location Track Car Type", "Test Type", t.getLocType());

                /* Test Type Name */
		Assert.assertEquals("Location Track Accepts Type Name undefined", false, t.acceptsTypeName("TestTypeName"));

		t.addTypeName("TestTypeName");
		Assert.assertEquals("Location Track Accepts Type Name defined", false, t.acceptsTypeName("TestTypeName"));

		// now add to car types
		CarTypes ct = CarTypes.instance();
		ct.addName("TestTypeName");
		t.addTypeName("TestTypeName");
		Assert.assertEquals("Location Track Accepts Type Name defined after ct", true, t.acceptsTypeName("TestTypeName"));

		t.deleteTypeName("TestTypeName");
		Assert.assertEquals("Location Track Accepts Type Name deleted", false, t.acceptsTypeName("TestTypeName"));

                /* Needed so later tests will behave correctly */
                ct.deleteName("TestTypeName");

		ct.addName("Baggager");
		
		t.addTypeName("Baggager");
		
		Assert.assertEquals("Location Track Accepts Type Name Baggager", true, t.acceptsTypeName("Baggager"));

                /* Test Road Name */
                t.setRoadOption(Track.INCLUDEROADS);
		Assert.assertEquals("Location Track set Road Option INCLUDEROADS", "Include", t.getRoadOption());

                Assert.assertEquals("Location Track Accepts Road Name undefined", false, t.acceptsRoadName("TestRoadName"));

		t.addRoadName("TestRoadName");
		Assert.assertEquals("Location Track Accepts Road Name defined", true, t.acceptsRoadName("TestRoadName"));

		t.addRoadName("TestOtherRoadName");
		Assert.assertEquals("Location Track Accepts Road Name other defined", true, t.acceptsRoadName("TestRoadName"));

                t.deleteRoadName("TestRoadName");
		Assert.assertEquals("Location Track Accepts Road Name deleted", false, t.acceptsRoadName("TestRoadName"));

                t.setRoadOption(Track.ALLROADS);
		Assert.assertEquals("Location Track set Road Option AllROADS", "All", t.getRoadOption());
		Assert.assertEquals("Location Track Accepts All Road Names", true, t.acceptsRoadName("TestRoadName"));

                t.setRoadOption(Track.EXCLUDEROADS);
		Assert.assertEquals("Location Track set Road Option EXCLUDEROADS", "Exclude", t.getRoadOption());
		Assert.assertEquals("Location Track Excludes Road Names", true, t.acceptsRoadName("TestRoadName"));

                t.addRoadName("TestRoadName");
		Assert.assertEquals("Location Track Excludes Road Names 2", false, t.acceptsRoadName("TestRoadName"));

                /* Test Drop IDs */
                Assert.assertEquals("Location Track Accepts Drop ID undefined", false, t.containsDropId("TestDropId"));

		t.addDropId("TestDropId");
                Assert.assertEquals("Location Track Accepts Drop ID defined", true, t.containsDropId("TestDropId"));

		t.addDropId("TestOtherDropId");
                Assert.assertEquals("Location Track Accepts Drop ID other defined", true, t.containsDropId("TestDropId"));

                t.deleteDropId("TestDropId");
                Assert.assertEquals("Location Track Accepts Drop ID deleted", false, t.containsDropId("TestDropId"));

                /* Test Pickup IDs */
                Assert.assertEquals("Location Track Accepts Pickup ID undefined", false, t.containsPickupId("TestPickupId"));

		t.addPickupId("TestPickupId");
                Assert.assertEquals("Location Track Accepts Pickup ID defined", true, t.containsPickupId("TestPickupId"));

		t.addPickupId("TestOtherPickupId");
                Assert.assertEquals("Location Track Accepts Pickup ID other defined", true, t.containsPickupId("TestPickupId"));

                t.deletePickupId("TestPickupId");
                Assert.assertEquals("Location Track Accepts Pickup ID deleted", false, t.containsPickupId("TestPickupId"));
	}

	// test Track schedule support
	public void testTrackScheduleSupport() {
		Track t = new Track("Test id", "Test Name", "Test Type");
		Assert.assertEquals("Location Track Car id", "Test id", t.getId());
		Assert.assertEquals("Location Track Car Name", "Test Name", t.getName());
		Assert.assertEquals("Location Track Car Type", "Test Type", t.getLocType());

                t.setScheduleName("Test Schedule Name");
		Assert.assertEquals("Location Track set Schedule Name", "Test Schedule Name", t.getScheduleName());
                t.setScheduleItemId("Test Schedule Item Id");
		Assert.assertEquals("Location Track set Schedule Item Id", "Test Schedule Item Id", t.getScheduleItemId());
                t.setScheduleCount(2);
		Assert.assertEquals("Location Track set Schedule Count", 2, t.getScheduleCount());
	}

	// test Track load support
	public void testTrackLoadSupport() {
		Track t = new Track("Test id", "Test Name", "Test Type");
		Assert.assertEquals("Location Track Car id", "Test id", t.getId());
		Assert.assertEquals("Location Track Car Name", "Test Name", t.getName());
		Assert.assertEquals("Location Track Car Type", "Test Type", t.getLocType());

                /* Test Load Swapable */
                Assert.assertEquals("Location Track Load Swapable default", false, t.isLoadSwapEnabled());
                t.enableLoadSwaps(true);
                Assert.assertEquals("Location Track Load Swapable true", true, t.isLoadSwapEnabled());
                t.enableLoadSwaps(false);
                Assert.assertEquals("Location Track Load Swapable false", false, t.isLoadSwapEnabled());

                /* Test Remove Loads */
                Assert.assertEquals("Location Track Remove Loads default", false, t.isRemoveLoadsEnabled());
                t.enableRemoveLoads(true);
                Assert.assertEquals("Location Track Remove Loads true", true, t.isRemoveLoadsEnabled());
                t.enableRemoveLoads(false);
                Assert.assertEquals("Location Track Remove Loads false", false, t.isRemoveLoadsEnabled());

                /* Test Add Loads */
                Assert.assertEquals("Location Track Add Loads default", false, t.isAddLoadsEnabled());
                t.enableAddLoads(true);
                Assert.assertEquals("Location Track Add Loads true", true, t.isAddLoadsEnabled());
                t.enableAddLoads(false);
                Assert.assertEquals("Location Track Add Loads false", false, t.isAddLoadsEnabled());
     	}

        // test Locations class (part two)
        // test length attributes
	public void testLengthAttributes() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		l.setLength(400);
		Assert.assertEquals("Location Length", 400, l.getLength());

		l.setUsedLength(200);
		Assert.assertEquals("Location Used Length", 200, l.getUsedLength());
	}

	// test operation attributes
	public void testOperationAttributes() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		l.setLocationOps(Location.STAGING);
		Assert.assertEquals("Location Ops Staging", Location.STAGING, l.getLocationOps());

		l.setLocationOps(Location.NORMAL);
		Assert.assertEquals("Location Ops Normal", Location.NORMAL, l.getLocationOps());
	}

	// test direction attributes
	public void testDirectionAttributes() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		l.setTrainDirections(Location.NORTH);
		Assert.assertEquals("Location Direction North", Location.NORTH, l.getTrainDirections());

		l.setTrainDirections(Location.SOUTH);
		Assert.assertEquals("Location Direction South", Location.SOUTH, l.getTrainDirections());

		l.setTrainDirections(Location.EAST);
		Assert.assertEquals("Location Direction East", Location.EAST, l.getTrainDirections());

		l.setTrainDirections(Location.WEST);
		Assert.assertEquals("Location Direction West", Location.WEST, l.getTrainDirections());

		l.setTrainDirections(Location.NORTH+Location.SOUTH);
		Assert.assertEquals("Location Direction North+South", Location.NORTH+Location.SOUTH, l.getTrainDirections());

		l.setTrainDirections(Location.EAST+Location.WEST);
		Assert.assertEquals("Location Direction East+West", Location.EAST+Location.WEST, l.getTrainDirections());

		l.setTrainDirections(Location.NORTH+Location.SOUTH+Location.EAST+Location.WEST);
		Assert.assertEquals("Location Direction North+South+East+West", Location.NORTH+Location.SOUTH+Location.EAST+Location.WEST, l.getTrainDirections());
	}

	// test car attributes
	public void testCarAttributes() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		l.setNumberRS(8);
		Assert.assertEquals("Location Number of Cars", 8, l.getNumberRS());
	}

	// test switchlist attributes
	public void testSwitchlistAttributes() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		l.setSwitchList(true);
		Assert.assertEquals("Location Switch List True", true, l.getSwitchList());

		l.setSwitchList(false);
		Assert.assertEquals("Location Switch List True", false, l.getSwitchList());
	}

	// test typename support
	public void testTypeNameSupport() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		Assert.assertEquals("Location Accepts Type Name undefined", false, l.acceptsTypeName("TestTypeName"));

		l.addTypeName("TestTypeName");
		Assert.assertEquals("Location Accepts Type Name defined", false, l.acceptsTypeName("TestTypeName"));

		// now add to car types
		CarTypes ct = CarTypes.instance();
		ct.addName("TestTypeName");
		l.addTypeName("TestTypeName");
		Assert.assertEquals("Location Accepts Type Name defined", true, l.acceptsTypeName("TestTypeName"));

		l.deleteTypeName("TestTypeName");
		Assert.assertEquals("Location Accepts Type Name undefined2", false, l.acceptsTypeName("TestTypeName"));

		ct.addName("Baggage");
		ct.addName("BoxCar");
		ct.addName("Caboose");
		ct.addName("Coal");
		ct.addName("Engine");
		ct.addName("Hopper");
		ct.addName("MOW");
		ct.addName("Passenger");
		ct.addName("Reefer");
		ct.addName("Stock");
		ct.addName("Tank Oil");
		
		l.addTypeName("Baggage");
		l.addTypeName("BoxCar");
		l.addTypeName("Caboose");
		l.addTypeName("Coal");
		l.addTypeName("Engine");
		l.addTypeName("Hopper");
		l.addTypeName("MOW");
		l.addTypeName("Passenger");
		l.addTypeName("Reefer");

		l.addTypeName("Stock");
		l.addTypeName("Tank Oil");
		
		Assert.assertEquals("Location Accepts Type Name BoxCar", true, l.acceptsTypeName("BoxCar"));
		Assert.assertEquals("Location Accepts Type Name Boxcar", false, l.acceptsTypeName("Boxcar"));
		Assert.assertEquals("Location Accepts Type Name MOW", true, l.acceptsTypeName("MOW"));
		Assert.assertEquals("Location Accepts Type Name Caboose", true, l.acceptsTypeName("Caboose"));
		Assert.assertEquals("Location Accepts Type Name BoxCar", true, l.acceptsTypeName("BoxCar"));
		Assert.assertEquals("Location Accepts Type Name undefined3", false, l.acceptsTypeName("TestTypeName"));
	}

	// test pickup support
	public void testPickUpSupport() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		Assert.assertEquals("Location Pick Ups Start Condition", 0 , l.getPickupRS());

		l.addPickupRS();
		Assert.assertEquals("Location Pick Up 1", 1, l.getPickupRS());

		l.addPickupRS();
		Assert.assertEquals("Location Pick Up second", 2, l.getPickupRS());

		l.deletePickupRS();
		Assert.assertEquals("Location Delete Pick Up", 1, l.getPickupRS());

		l.deletePickupRS();
		Assert.assertEquals("Location Delete Pick Up second", 0, l.getPickupRS());
	}

	// test drop support
	public void testDropSupport() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		Assert.assertEquals("Location Drop Start Condition", 0 , l.getPickupRS());

		l.addDropRS();
		Assert.assertEquals("Location Drop 1", 1, l.getDropRS());

		l.addDropRS();
		Assert.assertEquals("Location Drop second", 2, l.getDropRS());

		l.deleteDropRS();
		Assert.assertEquals("Location Delete Drop", 1, l.getDropRS());

		l.deleteDropRS();
		Assert.assertEquals("Location Delete Drop second", 0, l.getDropRS());
	}

	// test car support
	public void testCarSupport() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		Assert.assertEquals("Location Used Length", 0, l.getUsedLength());
		Assert.assertEquals("Location Number of Cars", 0, l.getNumberRS());

		jmri.jmrit.operations.rollingstock.cars.Car c1 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER1");
		c1.setLength("40");
		l.addRS(c1);

		Assert.assertEquals("Location Number of Cars", 1, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 44, l.getUsedLength()); // Drawbar length is 4

		jmri.jmrit.operations.rollingstock.cars.Car c2 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER2");
		c2.setLength("33");
		l.addRS(c2);

		Assert.assertEquals("Location Number of Cars", 2, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 40+4+33+4, l.getUsedLength()); // Drawbar length is 4

		jmri.jmrit.operations.rollingstock.cars.Car c3 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER3");
		c3.setLength("50");
		l.addRS(c3);

		Assert.assertEquals("Location Number of Cars", 3, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 40+4+33+4+50+4, l.getUsedLength()); // Drawbar length is 4

		l.deleteRS(c2);

		Assert.assertEquals("Location Number of Cars", 2, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 40+4+50+4, l.getUsedLength()); // Drawbar length is 4

		l.deleteRS(c1);

		Assert.assertEquals("Location Number of Cars", 1, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 50+4, l.getUsedLength()); // Drawbar length is 4

		l.deleteRS(c3);

		Assert.assertEquals("Location Number of Cars", 0, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 0, l.getUsedLength()); // Drawbar length is 4
	}

	// test car duplicates support
	public void testCarDuplicatesSupport() {
		Location l = new Location("Test id", "Test Name");
		Assert.assertEquals("Location id", "Test id", l.getId());
		Assert.assertEquals("Location Name", "Test Name", l.getName());

		Assert.assertEquals("Location Used Length", 0, l.getUsedLength());
		Assert.assertEquals("Location Number of Cars", 0, l.getNumberRS());

		jmri.jmrit.operations.rollingstock.cars.Car c1 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER1");
		c1.setLength("40");
		l.addRS(c1);

		Assert.assertEquals("Location Number of Cars", 1, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 44, l.getUsedLength()); // Drawbar length is 4

		jmri.jmrit.operations.rollingstock.cars.Car c2 = new jmri.jmrit.operations.rollingstock.cars.Car("TESTROAD", "TESTNUMBER2");
		c2.setLength("33");
		l.addRS(c2);

		Assert.assertEquals("Location Number of Cars", 2, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 40+4+33+4, l.getUsedLength()); // Drawbar length is 4

		l.addRS(c1);

		Assert.assertEquals("Location Number of Cars", 3, l.getNumberRS());
		Assert.assertEquals("Location Used Length one car", 40+4+33+4+40+4, l.getUsedLength()); // Drawbar length is 4

	}

	// test location Xml create support
	public void testXMLCreate() {

                LocationManager manager = LocationManager.instance();
                List locationList = manager.getLocationsByIdList();
                Assert.assertEquals("Starting Number of Locations", 0, locationList.size());
                manager.newLocation("Test Location 2");
                manager.newLocation("Test Location 1");
                manager.newLocation("Test Location 3");

                Assert.assertEquals("New Location by Id 1", "Test Location 2", manager.getLocationById("1").getName());
                Assert.assertEquals("New Location by Id 2", "Test Location 1", manager.getLocationById("2").getName());
                Assert.assertEquals("New Location by Id 3", "Test Location 3", manager.getLocationById("3").getName());

                Assert.assertEquals("New Location by Name 1", "Test Location 1", manager.getLocationByName("Test Location 1").getName());
                Assert.assertEquals("New Location by Name 2", "Test Location 2", manager.getLocationByName("Test Location 2").getName());
                Assert.assertEquals("New Location by Name 3", "Test Location 3", manager.getLocationByName("Test Location 3").getName());

                manager.getLocationByName("Test Location 1").setComment("Test Location 1 Comment");
		manager.getLocationByName("Test Location 1").setLocationOps(Location.NORMAL);
		manager.getLocationByName("Test Location 1").setSwitchList(true);
		manager.getLocationByName("Test Location 1").setTrainDirections(Location.EAST+Location.WEST);
		manager.getLocationByName("Test Location 1").addTypeName("Baggage");
		manager.getLocationByName("Test Location 1").addTypeName("BoxCar");
		manager.getLocationByName("Test Location 1").addTypeName("Caboose");
		manager.getLocationByName("Test Location 1").addTypeName("Coal");
		manager.getLocationByName("Test Location 1").addTypeName("Engine");
		manager.getLocationByName("Test Location 1").addTypeName("Hopper");
                manager.getLocationByName("Test Location 2").setComment("Test Location 2 Comment");
		manager.getLocationByName("Test Location 2").setLocationOps(Location.NORMAL);
		manager.getLocationByName("Test Location 2").setSwitchList(true);
		manager.getLocationByName("Test Location 2").setTrainDirections(Location.EAST+Location.WEST);
		manager.getLocationByName("Test Location 2").addTypeName("Baggage");
		manager.getLocationByName("Test Location 2").addTypeName("BoxCar");
		manager.getLocationByName("Test Location 2").addTypeName("Caboose");
		manager.getLocationByName("Test Location 2").addTypeName("Coal");
		manager.getLocationByName("Test Location 2").addTypeName("Engine");
		manager.getLocationByName("Test Location 2").addTypeName("Hopper");
                manager.getLocationByName("Test Location 3").setComment("Test Location 3 Comment");
		manager.getLocationByName("Test Location 3").setLocationOps(Location.NORMAL);
		manager.getLocationByName("Test Location 3").setSwitchList(true);
		manager.getLocationByName("Test Location 3").setTrainDirections(Location.EAST+Location.WEST);
		manager.getLocationByName("Test Location 3").addTypeName("Baggage");
		manager.getLocationByName("Test Location 3").addTypeName("BoxCar");
		manager.getLocationByName("Test Location 3").addTypeName("Caboose");
		manager.getLocationByName("Test Location 3").addTypeName("Coal");
		manager.getLocationByName("Test Location 3").addTypeName("Engine");
		manager.getLocationByName("Test Location 3").addTypeName("Hopper");

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

                LocationManagerXml.instance().writeOperationsLocationFile();

                manager.newLocation("Test Location 4");
                manager.newLocation("Test Location 5");
                manager.newLocation("Test Location 6");
                manager.getLocationByName("Test Location 2").setComment("Test Location 2 Changed Comment");
                
                LocationManagerXml.instance().writeOperationsLocationFile();
        }

	// test location Xml read support preparation
	public void testXMLReadPrep() {
                LocationManager manager = LocationManager.instance();
                List locationList = manager.getLocationsByIdList();
                Assert.assertEquals("Starting Number of Locations", 6, locationList.size());

                //  Revert the main xml file back to the backup file.
                LocationManagerXml.instance().revertBackupFile(XmlFile.prefsDir()+File.separator+LocationManagerXml.getOperationsDirectoryName()+File.separator+LocationManagerXml.getOperationsFileName());

                //  Need to dispose of the LocationManager's list and hash table
                manager.dispose();	
	}

	// test location Xml read support
	public void testXMLRead() throws Exception  {
                LocationManager manager = LocationManager.instance();
                List locationList = manager.getLocationsByNameList();

                // The dispose has removed all locations from the Manager.
                Assert.assertEquals("Starting Number of Locations", 0, locationList.size());

                // Need to force a re-read of the xml file.
                LocationManagerXml.instance().readFile(XmlFile.prefsDir()+File.separator+LocationManagerXml.getOperationsDirectoryName()+File.separator+LocationManagerXml.getOperationsFileName());

                locationList = manager.getLocationsByNameList();
                Assert.assertEquals("Starting Number of Locations", 3, locationList.size());

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

	}

        // TODO: Add tests for adding + deleting the same cars

	// TODO: Add tests for track locations

	// TODO: Add test to create xml file

	// TODO: Add test to read xml file
	// from here down is testing infrastructure

    // Ensure minimal setup for log4J

    Turnout t1, t2, t3;
    Sensor s1, s2, s3, s4, s5;
    SignalHead h1, h2, h3, h4;
    
    /**
    * Test-by test initialization.
    * Does log4j for standalone use, and then
    * creates a set of turnouts, sensors and signals
    * as common background for testing
    */
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();

        // Repoint ManagerXML to JUnitTest subdirectory
        new LocationManagerXml(){ {_instance = this;
        String tempstring;
        tempstring = getOperationsDirectoryName();
        if (!tempstring.contains(File.separator+"JUnitTest"))
            setOperationsDirectoryName(getOperationsDirectoryName()+File.separator+"JUnitTest");
        setOperationsFileName("OperationsJUnitTestLocationRoster.xml");}};
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+File.separator+LocationManagerXml.getOperationsDirectoryName());

        // create a new instance manager
        InstanceManager i = new InstanceManager(){
            @Override
            protected void init() {
                root = null;
                super.init();
                root = this;
            }
        };
        
        InstanceManager.setTurnoutManager(new InternalTurnoutManager());
        t1 = InstanceManager.turnoutManagerInstance().newTurnout("IT1", "1");
        t2 = InstanceManager.turnoutManagerInstance().newTurnout("IT2", "2");
        t3 = InstanceManager.turnoutManagerInstance().newTurnout("IT3", "3");

        InstanceManager.setSensorManager(new InternalSensorManager());
        s1 = InstanceManager.sensorManagerInstance().newSensor("IS1", "1");
        s2 = InstanceManager.sensorManagerInstance().newSensor("IS2", "2");
        s3 = InstanceManager.sensorManagerInstance().newSensor("IS3", "3");
        s4 = InstanceManager.sensorManagerInstance().newSensor("IS4", "4");
        s5 = InstanceManager.sensorManagerInstance().newSensor("IS5", "5");

        h1 = new jmri.VirtualSignalHead("IH1");
        InstanceManager.signalHeadManagerInstance().register(h1);
        h2 = new jmri.VirtualSignalHead("IH2");
        InstanceManager.signalHeadManagerInstance().register(h2);
        h3 = new jmri.VirtualSignalHead("IH3");
        InstanceManager.signalHeadManagerInstance().register(h3);
        h4 = new jmri.VirtualSignalHead("IH4");
        InstanceManager.signalHeadManagerInstance().register(h4);
    }

	public OperationsLocationsTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsLocationsTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsLocationsTest.class);
		return suite;
	}

    // The minimal setup for log4J
    @Override
    protected void tearDown() { 
        apps.tests.Log4JFixture.tearDown();
    }
}
