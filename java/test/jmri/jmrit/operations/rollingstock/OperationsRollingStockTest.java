package jmri.jmrit.operations.rollingstock;

import org.junit.Assert;
import org.junit.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Setup;

/**
 * Tests for the Operations Car class Last manually cross-checked on
 * 20090131
 * <p>
 * Still to do: Car: Location Length change (set) Car:
 * Destination Car: Train, Route
 * <p>
 * Note: Car: XML read/write is tested in OperationsEnginesTest and
 * OperationsCarsTest
 *
 * @author	Bob Coleman Copyright (C) 2009
 *
 */
public class OperationsRollingStockTest extends OperationsTestCase {

    // test constructors
    @Test
    public void testCtor() {
        // test the default constructor.
        Car rs1 = new Car();
        Assert.assertNotNull("Default Constructor", rs1);
    }

    @Test
    public void test2ParmCtor() {
        // test the constructor with roadname and roadnumer as parameters.
        Car rs1 = new Car("TESTROAD", "TESTNUMBER1");
        Assert.assertNotNull("Two parameter Constructor", rs1);

        Assert.assertEquals("Car Road", "TESTROAD", rs1.getRoadName());
        Assert.assertEquals("Car Number", "TESTNUMBER1", rs1.getNumber());
        Assert.assertEquals("Car ID", "TESTROAD" + "TESTNUMBER1", rs1.getId());
    }

    @Test
    public void testXmlConstructor() {
        // test the constructor loading this car from an XML element.

        // first, we need to build the XML element.
        org.jdom2.Element e = new org.jdom2.Element("cars");
        e.setAttribute(Xml.ID, "TESTID");
        e.setAttribute(Xml.ROAD_NAME, "TESTROAD1");
        e.setAttribute(Xml.ROAD_NUMBER, "TESTNUMBER1");
        e.setAttribute(Xml.TYPE, "TESTTYPE");
        e.setAttribute(Xml.LENGTH, "TESTLENGTH");
        e.setAttribute(Xml.COLOR, "TESTCOLOR");
        e.setAttribute(Xml.WEIGHT, "TESTWEIGHT");
        e.setAttribute(Xml.WEIGHT_TONS, "TESTWEIGHTTONS");
        e.setAttribute(Xml.BUILT, "TESTBUILT");
        e.setAttribute(Xml.LOCATION_ID, "TESTLOCATION");
        e.setAttribute(Xml.ROUTE_LOCATION_ID, "TESTROUTELOCATION");
        e.setAttribute(Xml.SEC_LOCATION_ID, "TESTTRACK");
        e.setAttribute(Xml.DESTINATION_ID, "TESTDESTINATION");
        e.setAttribute(Xml.ROUTE_DESTINATION_ID, "TESTROUTEDESTINATION");
        e.setAttribute(Xml.SEC_DESTINATION_ID, "TESTDESTINATION");
        e.setAttribute(Xml.LAST_ROUTE_ID, "SAVEDROUTE");
        e.setAttribute(Xml.MOVES, "5");
        e.setAttribute(Xml.DATE, "2015/05/15 15:15:15");
        e.setAttribute(Xml.SELECTED, Xml.FALSE);
        e.setAttribute(Xml.LAST_LOCATION_ID, "TESTLASTLOCATION");
        e.setAttribute(Xml.TRAIN, "TESTTRAIN");
        e.setAttribute(Xml.OWNER, "TESTOWNER");
        e.setAttribute(Xml.VALUE, "TESTVALUE");
        e.setAttribute(Xml.RFID, "12345");
        e.setAttribute(Xml.LOC_UNKNOWN, Xml.FALSE);
        e.setAttribute(Xml.OUT_OF_SERVICE, Xml.FALSE);
        e.setAttribute(Xml.BLOCKING, "5");
        e.setAttribute(Xml.COMMENT, "Test Comment");
        try {
            Car rs1 = new Car(e);
            Assert.assertNotNull("Xml Element Constructor", rs1);
        } catch (java.lang.NullPointerException npe) {
            Assert.fail("Null Pointer Exception while executing Xml Element Constructor");
        }

        jmri.util.JUnitAppender.assertErrorMessage("Tag 12345 Not Found");
    }

    // test creation
    @Test
    public void testCreate() {
        Car rs1 = new Car("TESTROAD", "TESTNUMBER1");

        Assert.assertEquals("Car Road", "TESTROAD", rs1.getRoadName());
        Assert.assertEquals("Car Number", "TESTNUMBER1", rs1.getNumber());
        Assert.assertEquals("Car ID", "TESTROAD" + "TESTNUMBER1", rs1.getId());

        rs1.setTypeName("TESTTYPE");
        rs1.setLength("TESTLENGTH");
        rs1.setColor("TESTCOLOR");
        rs1.setWeight("TESTWEIGHT");
        rs1.setWeightTons("TESTWEIGHTTONS");
        rs1.setBuilt("TESTBUILT");
        rs1.setOwner("TESTOWNER");
        rs1.setComment("TESTCOMMENT");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("TESTRFID");
        rs1.setRfid("TESTRFID");
        rs1.setMoves(5);

        Assert.assertEquals("Car Type", "TESTTYPE", rs1.getTypeName());

        /* Also need to test location length */
        Assert.assertEquals("Car Length", "TESTLENGTH", rs1.getLength());

        Assert.assertEquals("Car Color", "TESTCOLOR", rs1.getColor());
        /* More appropriate Weight tests below */
        Assert.assertEquals("Car Weight", "TESTWEIGHT", rs1.getWeight());
        Assert.assertEquals("Car WeightTons", "TESTWEIGHTTONS", rs1.getWeightTons());

        Assert.assertEquals("Car Built", "TESTBUILT", rs1.getBuilt());
        Assert.assertEquals("Car Owner", "TESTOWNER", rs1.getOwner());
        Assert.assertEquals("Car Comment", "TESTCOMMENT", rs1.getComment());
        Assert.assertEquals("Car Rfid", "TESTRFID", rs1.getRfid());
        Assert.assertEquals("Car Moves", 5, rs1.getMoves());
    }

    // test Car weight and weighttons
    @Test
    public void testCarWeight() {
        Car rs1 = new Car("TESTROAD", "TESTNUMBER1");
        Assert.assertEquals("Car Road", "TESTROAD", rs1.getRoadName());
        Assert.assertEquals("Car Number", "TESTNUMBER1", rs1.getNumber());

        Setup.setScale(Setup.N_SCALE);
        rs1.setWeight("20");
        Assert.assertEquals("Car Weight Real test", "20", rs1.getWeight());
        Assert.assertEquals("Car WeightTons Real test", "1600", rs1.getWeightTons());
    }

    // test Car public constants
    @Test
    public void testCarConstants() {
        Car rs1 = new Car("TESTROAD", "TESTNUMBER1");
        Assert.assertEquals("Car Road", "TESTROAD", rs1.getRoadName());
        Assert.assertEquals("Car Number", "TESTNUMBER1", rs1.getNumber());

        Assert.assertEquals("Car Constant TRACK_CHANGED_PROPERTY", "rolling stock track location", Car.TRACK_CHANGED_PROPERTY);
        Assert.assertEquals("Car Constant DESTINATION_CHANGED_PROPERTY", "rolling stock destination", Car.DESTINATION_CHANGED_PROPERTY);
        Assert.assertEquals("Car Constant DESTINATIONTRACK_CHANGED_PROPERTY", "rolling stock track destination", Car.DESTINATION_TRACK_CHANGED_PROPERTY);

        Assert.assertEquals("Car Constant COUPLERS", 4, Car.COUPLERS);
    }

    // test Car location and track
    @Test
    public void testCarLocation() {
        Car rs1 = new Car("TESTROAD", "TESTNUMBER1");
        /* Rolling Stock needs a valid type */
        rs1.setTypeName("TESTTYPE");
        /* Type needs to be in CarTypes or EngineTypes */
        InstanceManager.getDefault(CarTypes.class).addName("TESTTYPE");

        Assert.assertEquals("Car Road", "TESTROAD", rs1.getRoadName());
        Assert.assertEquals("Car Number", "TESTNUMBER1", rs1.getNumber());
        Assert.assertEquals("Car Type", "TESTTYPE", rs1.getTypeName());

        /* Rolling Stock not placed on layout yet */
        Assert.assertEquals("Car null Location Name", "", rs1.getLocationName());
        Assert.assertEquals("Car null Location Id", "", rs1.getLocationId());
        Assert.assertEquals("Car null Track Name", "", rs1.getTrackName());
        Assert.assertEquals("Car null Track Id", "", rs1.getTrackId());
        Assert.assertEquals("Car car length", "0", rs1.getLength());

        String testresult;

        /* Place Rolling Stock on layout */
        Location testlocation1 = new Location("Loc1", "Test Town");
        Track testtrack1 = testlocation1.addTrack("Testees Office", Track.SPUR);

        testtrack1.deleteTypeName("TESTTYPE");
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertEquals("Car null Set Location", "type (TESTTYPE)", testresult);

        /* type needs to be valid for Track */
        testtrack1.addTypeName("TESTTYPE");
        testlocation1.deleteTypeName("TESTTYPE");
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertEquals("Car null Set Location Track type", "type (TESTTYPE)", testresult);

        /* type needs to be valid for Location */
        testlocation1.addTypeName("TESTTYPE");
        rs1.setLength("");
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertEquals("Car null Set Location type", "rolling stock length ()", testresult);

        /* track needs to have a defined length */
        rs1.setLength("41");
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertTrue("status message starts with capacity", testresult.startsWith(Track.CAPACITY));

        /* track needs to be long enough */
        testtrack1.setLength(40);
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertTrue("status message starts with capacity", testresult.startsWith(Track.CAPACITY));

        /* track needs to be long enough */
        testtrack1.setLength(44);  // rs length + Coupler == 4
        rs1.setLength("40");
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertEquals("Car null Set Length match", "okay", testresult);
        
        // car is now on track, need to remove to continue testing
        rs1.setLocation(null, null);

        /* track needs to accept road */
        testtrack1.setRoadOption(Track.INCLUDE_ROADS);
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertEquals("Car null Set includeroads", "road (TESTROAD)", testresult);

        /* track needs to accept road */
        testtrack1.setRoadOption(Track.INCLUDE_ROADS);
        testtrack1.addRoadName("TESTROAD");
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertEquals("Car Set includeroads", "okay", testresult);
        
        // car is now on track, need to remove to continue testing
        rs1.setLocation(null, null);

        /* track needs to accept road */
        testtrack1.setRoadOption(Track.EXCLUDE_ROADS);
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertEquals("Car Set excluderoads", "road (TESTROAD)", testresult);

        /* track needs to accept road */
        testtrack1.setRoadOption(Track.ALL_ROADS);
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertEquals("Car Set allroads", "okay", testresult);
        
        // car is now on track, need to remove to continue testing
        rs1.setLocation(null, null);

        /* track needs to accept road */
        testtrack1.setRoadOption(Track.EXCLUDE_ROADS);
        testtrack1.deleteRoadName("TESTROAD");
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertEquals("Car Set null excluderoads", "okay", testresult);

        // Normally logged message
        jmri.util.JUnitAppender.assertErrorMessage("Rolling stock (TESTROAD TESTNUMBER1) length () is not valid");
    }
}
