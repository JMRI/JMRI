// OperationsRollingStockTest.java

package jmri.jmrit.operations.rollingstock;

import java.util.Locale;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Setup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Operations RollingStock class
 * Last manually cross-checked on 20090131
 * 
 * Still to do:
 *   RollingStock: Location Length change (set)
 *   RollingStock: Destination
 *   RollingStock: Train, Route
 * 
 * Note:
 *   RollingStock: XML read/write is tested in OperationsEnginesTest 
 *                                         and OperationsCarsTest
 * 
 * @author	Bob Coleman Copyright (C) 2009
 * 
 */

public class OperationsRollingStockTest extends TestCase {

	// test creation
	public void testCreate() {
		RollingStock rs1 = new RollingStock("TESTROAD", "TESTNUMBER1");
                
                Assert.assertEquals("Car Road", "TESTROAD", rs1.getRoadName());
		Assert.assertEquals("Car Number", "TESTNUMBER1", rs1.getNumber());
		Assert.assertEquals("Car ID", "TESTROAD"+"TESTNUMBER1", rs1.getId());
                
		rs1.setTypeName("TESTTYPE");
		rs1.setLength("TESTLENGTH");
		rs1.setColor("TESTCOLOR");
		rs1.setWeight("TESTWEIGHT");
		rs1.setWeightTons("TESTWEIGHTTONS");
		rs1.setBuilt("TESTBUILT");
		rs1.setOwner("TESTOWNER");
		rs1.setComment("TESTCOMMENT");
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
                
		Assert.assertEquals("RollingStock Constant LOCATION_CHANGED_PROPERTY", "rolling stock location", RollingStock.LOCATION_CHANGED_PROPERTY);
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
		Assert.assertEquals("RollingStock null Set Location type", "length ()", testresult);

		/* track needs to have a defined length */
		rs1.setLength("41");
		testresult = rs1.setLocation(testlocation1, testtrack1);
		Assert.assertEquals("RollingStock null Set Length null", "length (45)", testresult);

		/* track needs to be long enough */
		testtrack1.setLength(40);
		testresult = rs1.setLocation(testlocation1, testtrack1);
		Assert.assertEquals("RollingStock null Set Length short", "length (45)", testresult);

		/* track needs to be long enough */
		testtrack1.setLength(44);  // rs length + Coupler == 4
		rs1.setLength("40");
		testresult = rs1.setLocation(testlocation1, testtrack1);
		Assert.assertEquals("RollingStock null Set Length match", "okay", testresult);

		/* track needs to accept road */
		testtrack1.setRoadOption(Track.INCLUDEROADS);
		testresult = rs1.setLocation(testlocation1, testtrack1);
		Assert.assertEquals("RollingStock null Set includeroads", "road (TESTROAD)", testresult);

		/* track needs to accept road */
		testtrack1.setRoadOption(Track.INCLUDEROADS);
		testtrack1.addRoadName("TESTROAD");
		testresult = rs1.setLocation(testlocation1, testtrack1);
		Assert.assertEquals("RollingStock Set includeroads", "okay", testresult);

		/* track needs to accept road */
		testtrack1.setRoadOption(Track.EXCLUDEROADS);
		testresult = rs1.setLocation(testlocation1, testtrack1);
		Assert.assertEquals("RollingStock Set excluderoads", "road (TESTROAD)", testresult);

		/* track needs to accept road */
		testtrack1.setRoadOption(Track.ALLROADS);
		testresult = rs1.setLocation(testlocation1, testtrack1);
		Assert.assertEquals("RollingStock Set allroads", "okay", testresult);

		/* track needs to accept road */
		testtrack1.setRoadOption(Track.EXCLUDEROADS);
		testtrack1.deleteRoadName("TESTROAD");
		testresult = rs1.setLocation(testlocation1, testtrack1);
		Assert.assertEquals("RollingStock Set null excluderoads", "okay", testresult);
	}


    // Ensure minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        
		// set the locale to US English
		Locale.setDefault(Locale.ENGLISH);
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
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
