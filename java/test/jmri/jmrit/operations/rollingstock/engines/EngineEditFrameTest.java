//EngineEditFrameTest.java
package jmri.jmrit.operations.rollingstock.engines;

import java.util.List;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations EngineEditFrame class
 *
 * @author	Dan Boudreau Copyright (C) 2010
 * @version $Revision: 28746 $
 */
public class EngineEditFrameTest  extends OperationsSwingTestCase {

    List<String> tempEngines;

    public void testEngineEditFrame() {

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();
        f.setTitle("Test Add Engine Frame");

        // add a new Engine
        f.roadComboBox.setSelectedItem("SP");
        f.roadNumberTextField.setText("6");
        f.modelComboBox.setSelectedItem("SW8");
        f.builtTextField.setText("1999");
        f.ownerComboBox.setSelectedItem("Owner1");
        f.commentTextField.setText("test Engine comment field");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addButton));

        EngineManager cManager = EngineManager.instance();
        // should have 6 Engines
        Assert.assertEquals("number of Engines", 6, cManager.getNumEntries());

        Engine c6 = cManager.getByRoadAndNumber("SP", "6");

        Assert.assertNotNull("Engine did not create", c6);
        Assert.assertEquals("Engine type", "SW8", c6.getModel());
        Assert.assertEquals("Engine type", "Diesel", c6.getTypeName());
        Assert.assertEquals("Engine length", "44", c6.getLength()); //default for SW8 is 44
        Assert.assertEquals("Engine built", "1999", c6.getBuilt());
        Assert.assertEquals("Engine owner", "Owner1", c6.getOwner());
        Assert.assertEquals("Engine comment", "test Engine comment field", c6.getComment());

        getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));
        // should have 6 Engines now
        Assert.assertEquals("number of Engines", 6, cManager.getNumEntries());

        f.dispose();
    }

    public void testEngineEditFrameRead() {
        EngineManager cManager = EngineManager.instance();
        Engine e1 = cManager.getByRoadAndNumber("NH", "1");
        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();
        f.loadEngine(e1);
        f.setTitle("Test Edit Engine Frame");

        Assert.assertEquals("Engine road", "NH", f.roadComboBox.getSelectedItem());
        Assert.assertEquals("Engine number", "1", f.roadNumberTextField.getText());
        Assert.assertEquals("Engine type", "RS1", f.modelComboBox.getSelectedItem());
        Assert.assertEquals("Engine type", "Diesel", f.typeComboBox.getSelectedItem());
        Assert.assertEquals("Engine length", "51", f.lengthComboBox.getSelectedItem());
        Assert.assertEquals("Engine weight", "Tons of Weight", f.weightTextField.getText());
        Assert.assertEquals("Engine built", "2009", f.builtTextField.getText());
        Assert.assertEquals("Engine owner", "Owner2", f.ownerComboBox.getSelectedItem());
        Assert.assertEquals("Engine comment", "Test Engine NH 1 Comment", f.commentTextField.getText());

        // test delete button
        getHelper().enterClickAndLeave(new MouseEventData(this, f.deleteButton));

        // should have 5 Engines now
        Assert.assertEquals("number of Engines", 4, cManager.getNumEntries());

        f.dispose();
    }

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

        EngineManager eManager = EngineManager.instance();
        // add 5 Engines to table
        Engine e1 = eManager.newEngine("NH", "1");
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

        Engine e2 = eManager.newEngine("UP", "2");
        e2.setModel("FT");
        e2.setBuilt("2004");
        e2.setMoves(50);
        e2.setOwner("AT");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 2");
        e2.setRfid("RFID 2");

        Engine e3 = eManager.newEngine("AA", "3");
        e3.setModel("SW8");
        e3.setBuilt("2006");
        e3.setMoves(40);
        e3.setOwner("AB");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 5");
        e3.setRfid("RFID 5");
        Assert.assertEquals("e3 location", Track.OKAY, e3.setLocation(boxford, boxfordHood));
        Assert.assertEquals("e3 destination", Track.OKAY, e3.setDestination(boxford, boxfordYard));

        Engine e4 = eManager.newEngine("SP", "2");
        e4.setModel("GP35");
        e4.setBuilt("1990");
        e4.setMoves(30);
        e4.setOwner("AAA");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 4");
        e4.setRfid("RFID 4");
        Assert.assertEquals("e4 location", Track.OKAY, e4.setLocation(westford, westfordSiding));
        Assert.assertEquals("e4 destination", Track.OKAY, e4.setDestination(boxford, boxfordHood));

        Engine e5 = eManager.newEngine("NH", "5");
        e5.setModel("SW1200");
        e5.setBuilt("1956");
        e5.setMoves(25);
        e5.setOwner("DAB");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 1");
        e5.setRfid("RFID 1");
        Assert.assertEquals("e5 location", Track.OKAY, e5.setLocation(westford, westfordAble));
        Assert.assertEquals("e5 destination", Track.OKAY, e5.setDestination(westford, westfordAble));
    }

    public EngineEditFrameTest (String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EngineEditFrameTest .class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EngineEditFrameTest .class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
