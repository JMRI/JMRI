//EngineSetFrameTest.java
package jmri.jmrit.operations.rollingstock.engines;

import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations EnginesSetFrame class
 *
 * @author	Dan Boudreau Copyright (C) 2010
 * @version $Revision: 28746 $
 */
public class EngineSetFrameTest extends OperationsSwingTestCase {

    public void testEngineSetFrame() {
        EngineSetFrame f = new EngineSetFrame();
        f.setTitle("Test Engine Set Frame");
        f.initComponents();
        EngineManager cManager = EngineManager.instance();
        Engine e3 = cManager.getByRoadAndNumber("AA", "3");
        f.loadEngine(e3);
        f.dispose();
    }

    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        loadEngines();
    }

    private void loadEngines() {

        // add Owner1 and Owner2
        CarOwners co = CarOwners.instance();
        co.addName("Owner1");
        co.addName("Owner2");
        // add road names
        CarRoads cr = CarRoads.instance();
        cr.addName("NH");
        cr.addName("UP");
        cr.addName("AA");
        cr.addName("SP");
        // add locations
        LocationManager lManager = LocationManager.instance();
        Location westford = lManager.newLocation("Westford");
        Track westfordYard = westford.addTrack("Yard", Track.YARD);
        westfordYard.setLength(300);
        Track westfordSiding = westford.addTrack("Siding", Track.SPUR);
        westfordSiding.setLength(300);
        Track westfordAble = westford.addTrack("Able", Track.SPUR);
        westfordAble.setLength(300);
        Location boxford = lManager.newLocation("Boxford");
        Track boxfordYard = boxford.addTrack("Yard", Track.YARD);
        boxfordYard.setLength(300);
        Track boxfordJacobson = boxford.addTrack("Jacobson", Track.SPUR);
        boxfordJacobson.setLength(300);
        Track boxfordHood = boxford.addTrack("Hood", Track.SPUR);
        boxfordHood.setLength(300);

        EngineManager cManager = EngineManager.instance();
        // add 5 Engines to table
        Engine e1 = cManager.newEngine("NH", "1");
        e1.setModel("RS1");
        e1.setBuilt("2009");
        e1.setMoves(55);
        e1.setOwner("Owner2");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 3");
        e1.setRfid("RFID 3");
        e1.setWeightTons("Tons of Weight");
        e1.setComment("Test Engine NH 1 Comment");
        Assert.assertEquals("e1 location", Track.OKAY, e1.setLocation(westford, westfordYard));
        Assert.assertEquals("e1 destination", Track.OKAY, e1.setDestination(boxford, boxfordJacobson));

        Engine e2 = cManager.newEngine("UP", "2");
        e2.setModel("FT");
        e2.setBuilt("2004");
        e2.setMoves(50);
        e2.setOwner("AT");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 2");
        e2.setRfid("RFID 2");

        Engine e3 = cManager.newEngine("AA", "3");
        e3.setModel("SW8");
        e3.setBuilt("2006");
        e3.setMoves(40);
        e3.setOwner("AB");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 5");
        e3.setRfid("RFID 5");
        Assert.assertEquals("e3 location", Track.OKAY, e3.setLocation(boxford, boxfordHood));
        Assert.assertEquals("e3 destination", Track.OKAY, e3.setDestination(boxford, boxfordYard));

        Engine e4 = cManager.newEngine("SP", "2");
        e4.setModel("GP35");
        e4.setBuilt("1990");
        e4.setMoves(30);
        e4.setOwner("AAA");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 4");
        e4.setRfid("RFID 4");
        Assert.assertEquals("e4 location", Track.OKAY, e4.setLocation(westford, westfordSiding));
        Assert.assertEquals("e4 destination", Track.OKAY, e4.setDestination(boxford, boxfordHood));

        Engine e5 = cManager.newEngine("NH", "5");
        e5.setModel("SW1200");
        e5.setBuilt("1956");
        e5.setMoves(25);
        e5.setOwner("DAB");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 1");
        e5.setRfid("RFID 1");
        Assert.assertEquals("e5 location", Track.OKAY, e5.setLocation(westford, westfordAble));
        Assert.assertEquals("e5 destination", Track.OKAY, e5.setDestination(westford, westfordAble));
    }

    public EngineSetFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EngineSetFrameTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EngineSetFrameTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
