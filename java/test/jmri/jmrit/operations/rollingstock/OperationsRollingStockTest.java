// OperationsRollingStockTest.java
package jmri.jmrit.operations.rollingstock;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Setup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations RollingStock class Last manually cross-checked on
 * 20090131
 *
 * Still to do: RollingStock: Location Length change (set) RollingStock:
 * Destination RollingStock: Train, Route
 *
 * Note: RollingStock: XML read/write is tested in OperationsEnginesTest and
 * OperationsCarsTest
 *
 * @author	Bob Coleman Copyright (C) 2009
 *
 */
public class OperationsRollingStockTest extends OperationsTestCase {

     // test constroctors.
     public void testCtor(){
       // test the default constructor.
       RollingStock rs1 = new RollingStock();
       Assert.assertNotNull("Default Constructor",rs1);
    }
    
    public void test2ParmCtor() {
      // test the constructor with roadname and roadnumer as parameters.
      RollingStock rs1 = new RollingStock("TESTROAD", "TESTNUMBER1");
      Assert.assertNotNull("Two parameter Constructor",rs1);

      Assert.assertEquals("Car Road", "TESTROAD", rs1.getRoadName());
      Assert.assertEquals("Car Number", "TESTNUMBER1", rs1.getNumber());
      Assert.assertEquals("Car ID", "TESTROAD" + "TESTNUMBER1", rs1.getId());
    }

    public void testXmlConstructor(){
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
        e.setAttribute(Xml.LOCATION_ID,"TESTLOCATION");
        e.setAttribute(Xml.ROUTE_LOCATION_ID, "TESTROUTELOCATION");
        e.setAttribute(Xml.SEC_LOCATION_ID, "TESTTRACK");
        e.setAttribute(Xml.DESTINATION_ID, "TESTDESTINATION");
        e.setAttribute(Xml.ROUTE_DESTINATION_ID, "TESTROUTEDESTINATION");
        e.setAttribute(Xml.SEC_DESTINATION_ID, "TESTDESTINATION");
        e.setAttribute(Xml.LAST_ROUTE_ID, "SAVEDROUTE");
        e.setAttribute(Xml.MOVES,"5");
        e.setAttribute(Xml.DATE, "2015/05/15 15:15:15");
        e.setAttribute(Xml.SELECTED,Xml.FALSE);
        e.setAttribute(Xml.LAST_LOCATION_ID, "TESTLASTLOCATION");
        e.setAttribute(Xml.TRAIN, "TESTTRAIN");
        e.setAttribute(Xml.OWNER, "TESTOWNER");
        e.setAttribute(Xml.VALUE, "TESTVALUE");
        e.setAttribute(Xml.RFID,"12345");
        e.setAttribute(Xml.LOC_UNKNOWN,Xml.FALSE);
        e.setAttribute(Xml.OUT_OF_SERVICE, Xml.FALSE);
        e.setAttribute(Xml.BLOCKING, "5");
        e.setAttribute(Xml.COMMENT,"Test Comment");
        try {
           RollingStock rs1 = new RollingStock(e);
           Assert.assertNotNull("Xml Element Constructor",rs1);
        } catch(java.lang.NullPointerException npe) {
           Assert.fail("Null Pointer Exception while executing Xml Element Constructor");
        }
        
        jmri.util.JUnitAppender.assertErrorMessage("Tag 12345 Not Found");
    }


    // test creation
    public void testCreate() {
        RollingStock rs1 = new RollingStock("TESTROAD", "TESTNUMBER1");

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

        Assert.assertEquals("RollingStock Type", "TESTTYPE", rs1.getTypeName());

        /* Also need to test location length */
        Assert.assertEquals("RollingStock Length", "TESTLENGTH", rs1.getLength());

        Assert.assertEquals("RollingStock Color", "TESTCOLOR", rs1.getColor());
        /* More appropriate Weight tests below */
        Assert.assertEquals("RollingStock Weight", "TESTWEIGHT", rs1.getWeight());
        Assert.assertEquals("RollingStock WeightTons", "TESTWEIGHTTONS", rs1.getWeightTons());

        Assert.assertEquals("RollingStock Built", "TESTBUILT", rs1.getBuilt());
        Assert.assertEquals("RollingStock Owner", "TESTOWNER", rs1.getOwner());
        Assert.assertEquals("RollingStock Comment", "TESTCOMMENT", rs1.getComment());
        Assert.assertEquals("RollingStock Rfid", "TESTRFID", rs1.getRfid());
        Assert.assertEquals("RollingStock Moves", 5, rs1.getMoves());
    }

    // test RollingStock weight and weighttons
    public void testRollingStockWeight() {
        RollingStock rs1 = new RollingStock("TESTROAD", "TESTNUMBER1");
        Assert.assertEquals("RollingStock Road", "TESTROAD", rs1.getRoadName());
        Assert.assertEquals("RollingStock Number", "TESTNUMBER1", rs1.getNumber());

        Setup.setScale(Setup.N_SCALE);
        rs1.setWeight("20");
        Assert.assertEquals("RollingStock Weight Real test", "20", rs1.getWeight());
        Assert.assertEquals("RollingStock WeightTons Real test", "1600", rs1.getWeightTons());
    }

    // test RollingStock public constants
    public void testRollingStockConstants() {
        RollingStock rs1 = new RollingStock("TESTROAD", "TESTNUMBER1");
        Assert.assertEquals("RollingStock Road", "TESTROAD", rs1.getRoadName());
        Assert.assertEquals("RollingStock Number", "TESTNUMBER1", rs1.getNumber());
        
        Assert.assertEquals("RollingStock Constant TRACK_CHANGED_PROPERTY", "rolling stock track location", RollingStock.TRACK_CHANGED_PROPERTY);
        Assert.assertEquals("RollingStock Constant DESTINATION_CHANGED_PROPERTY", "rolling stock destination", RollingStock.DESTINATION_CHANGED_PROPERTY);
        Assert.assertEquals("RollingStock Constant DESTINATIONTRACK_CHANGED_PROPERTY", "rolling stock track destination", RollingStock.DESTINATION_TRACK_CHANGED_PROPERTY);

        Assert.assertEquals("RollingStock Constant COUPLER", 4, RollingStock.COUPLER);
    }

    // test RollingStock location and track
    public void testRollingStockLocation() {
        RollingStock rs1 = new RollingStock("TESTROAD", "TESTNUMBER1");
        /* Rolling Stock needs a valid type */
        rs1.setTypeName("TESTTYPE");
        /* Type needs to be in CarTypes or EngineTypes */
        CarTypes.instance().addName("TESTTYPE");

        Assert.assertEquals("RollingStock Road", "TESTROAD", rs1.getRoadName());
        Assert.assertEquals("RollingStock Number", "TESTNUMBER1", rs1.getNumber());
        Assert.assertEquals("RollingStock Type", "TESTTYPE", rs1.getTypeName());

        /* Rolling Stock not placed on layout yet */
        Assert.assertEquals("RollingStock null Location Name", "", rs1.getLocationName());
        Assert.assertEquals("RollingStock null Location Id", "", rs1.getLocationId());
        Assert.assertEquals("RollingStock null Track Name", "", rs1.getTrackName());
        Assert.assertEquals("RollingStock null Track Id", "", rs1.getTrackId());
        Assert.assertEquals("RollingStock null car length", "", rs1.getLength());

        String testresult;

        /* Place Rolling Stock on layout */
        Location testlocation1 = new Location("Loc1", "Test Town");
        Track testtrack1 = testlocation1.addTrack("Testees Office", Track.SPUR);

        testtrack1.deleteTypeName("TESTTYPE");
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertEquals("RollingStock null Set Location", "type (TESTTYPE)", testresult);

        /* type needs to be valid for Track */
        testtrack1.addTypeName("TESTTYPE");
        testlocation1.deleteTypeName("TESTTYPE");
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertEquals("RollingStock null Set Location Track type", "type (TESTTYPE)", testresult);

        /* type needs to be valid for Location */
        testlocation1.addTypeName("TESTTYPE");
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertEquals("RollingStock null Set Location type", "rolling stock length ()", testresult);

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
        Assert.assertEquals("RollingStock null Set Length match", "okay", testresult);

        /* track needs to accept road */
        testtrack1.setRoadOption(Track.INCLUDE_ROADS);
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertEquals("RollingStock null Set includeroads", "road (TESTROAD)", testresult);

        /* track needs to accept road */
        testtrack1.setRoadOption(Track.INCLUDE_ROADS);
        testtrack1.addRoadName("TESTROAD");
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertEquals("RollingStock Set includeroads", "okay", testresult);

        /* track needs to accept road */
        testtrack1.setRoadOption(Track.EXCLUDE_ROADS);
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertEquals("RollingStock Set excluderoads", "road (TESTROAD)", testresult);

        /* track needs to accept road */
        testtrack1.setRoadOption(Track.ALL_ROADS);
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertEquals("RollingStock Set allroads", "okay", testresult);

        /* track needs to accept road */
        testtrack1.setRoadOption(Track.EXCLUDE_ROADS);
        testtrack1.deleteRoadName("TESTROAD");
        testresult = rs1.setLocation(testlocation1, testtrack1);
        Assert.assertEquals("RollingStock Set null excluderoads", "okay", testresult);
        
        // Normally logged message
        jmri.util.JUnitAppender.assertErrorMessage("Rolling stock (TESTROAD TESTNUMBER1) length () is not valid");
        
    }

    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    public OperationsRollingStockTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", OperationsRollingStockTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OperationsRollingStockTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }
}
