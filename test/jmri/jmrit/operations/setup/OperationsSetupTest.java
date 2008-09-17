// OperationsSetupTest.java

package jmri.jmrit.operations.setup;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.setup.OperationsXml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

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
 * Tests for the OperationsSetup class
 * @author	Bob Coleman
 * @version $Revision: 1.2 $
 */
public class OperationsSetupTest extends TestCase {

	// test creation
	public void testCreate() {
		Setup s = new Setup();
		s.setRailroadName("Test Railroad Name");
		Assert.assertEquals("Railroad Name", "Test Railroad Name", s.getRailroadName());
		s.setOwnerName("Test Owner Name");
		Assert.assertEquals("Owner Name", "Test Owner Name", s.getOwnerName());
	}

	// test public constants
	public void testConstants() {
		Setup s = new Setup();

		s.setRailroadName("Test Railroad Name");
		Assert.assertEquals("Railroad Name", "Test Railroad Name", s.getRailroadName());

		Assert.assertEquals("Operations Setup Constant Z_SCALE", 1, s.Z_SCALE);
		Assert.assertEquals("Operations Setup Constant N_SCALE", 2, s.N_SCALE);
		Assert.assertEquals("Operations Setup Constant TT_SCALE", 3, s.TT_SCALE);
		Assert.assertEquals("Operations Setup Constant HOn3_SCALE", 4, s.HOn3_SCALE);
		Assert.assertEquals("Operations Setup Constant OO_SCALE", 5, s.OO_SCALE);
		Assert.assertEquals("Operations Setup Constant HO_SCALE", 6, s.HO_SCALE);
		Assert.assertEquals("Operations Setup Constant Sn3_SCALE", 7, s.Sn3_SCALE);
		Assert.assertEquals("Operations Setup Constant S_SCALE", 8, s.S_SCALE);
		Assert.assertEquals("Operations Setup Constant On3_SCALE", 9, s.On3_SCALE);
		Assert.assertEquals("Operations Setup Constant O_SCALE", 10, s.O_SCALE);
		Assert.assertEquals("Operations Setup Constant G_SCALE", 11, s.G_SCALE);

		Assert.assertEquals("Operations Setup Constant EAST", 1, s.EAST);
		Assert.assertEquals("Operations Setup Constant WEST", 2, s.WEST);
		Assert.assertEquals("Operations Setup Constant NORTH", 4, s.NORTH);
		Assert.assertEquals("Operations Setup Constant SOUTH", 8, s.SOUTH);

		Assert.assertEquals("Operations Setup Constant MONOSPACED", "Monospaced", s.MONOSPACED);
		Assert.assertEquals("Operations Setup Constant SANSERIF", "SansSerif", s.SANSERIF);
	}
	
	// test scale attributes
	public void testScaleAttributes() {
		Setup s = new Setup();
		// Not really necessary
		s.setRailroadName("Test Railroad Name");
		Assert.assertEquals("Railroad Name", "Test Railroad Name", s.getRailroadName());
		s.setOwnerName("Test Owner Name");
		Assert.assertEquals("Owner Name", "Test Owner Name", s.getOwnerName());

		s.setScale(s.N_SCALE);
		Assert.assertEquals("Scale", 2, s.getScale());
		Assert.assertEquals("Scale Ratio", 160, s.getScaleRatio());
		Assert.assertEquals("Initial Weight", 500, s.getInitalWeight());
		Assert.assertEquals("Added Weight", 150, s.getAddWeight());
	}
	
	// test train attributes
	public void testTrainAttributes() {
		Setup s = new Setup();
		// Not really necessary
		s.setRailroadName("Test Railroad Name");
		Assert.assertEquals("Railroad Name", "Test Railroad Name", s.getRailroadName());
		s.setOwnerName("Test Owner Name");
		Assert.assertEquals("Owner Name", "Test Owner Name", s.getOwnerName());

		s.setTrainDirection(s.EAST);
		Assert.assertEquals("Direction East", 1, s.getTrainDirection());
		s.setTrainDirection(s.WEST);
		Assert.assertEquals("Direction West", 2, s.getTrainDirection());
		s.setTrainDirection(s.NORTH);
		Assert.assertEquals("Direction North", 4, s.getTrainDirection());
		s.setTrainDirection(s.SOUTH);
		Assert.assertEquals("Direction South", 8, s.getTrainDirection());

		s.setTrainLength(520);
		Assert.assertEquals("Train Length", 520, s.getTrainLength());

		s.setEngineSize(120);
		Assert.assertEquals("Engine Size", 120, s.getEngineSize());

		s.setCarMoves(12);
		Assert.assertEquals("Car Moves", 12, s.getCarMoves());
	}
	
	// test panel attributes
	public void testPanelAttributes() {
		Setup s = new Setup();
		// Not really necessary
		s.setRailroadName("Test Railroad Name");
		Assert.assertEquals("Railroad Name", "Test Railroad Name", s.getRailroadName());
		s.setOwnerName("Test Owner Name");
		Assert.assertEquals("Owner Name", "Test Owner Name", s.getOwnerName());

		s.setPanelName("Test Panel Name");
		Assert.assertEquals("Panel Name", "Test Panel Name", s.getPanelName());

		s.setFontName("Test Font Name");
		Assert.assertEquals("Font Name", "Test Font Name", s.getFontName());

		s.setTrainIconCordEnabled(true);
		Assert.assertEquals("Train Icon Cord Enabled True", true, s.isTrainIconCordEnabled());

		s.setTrainIconCordEnabled(false);
		Assert.assertEquals("Train Icon Cord Enabled False", false, s.isTrainIconCordEnabled());

		s.setTrainIconAppendEnabled(true);
		Assert.assertEquals("Train Icon Append Enabled True", true, s.isTrainIconAppendEnabled());

		s.setTrainIconAppendEnabled(false);
		Assert.assertEquals("Train Icon Append Enabled False", false, s.isTrainIconAppendEnabled());

		s.setTrainIconColorNorth("Red");
		Assert.assertEquals("Train Icon Color North", "Red", s.getTrainIconColorNorth());

		s.setTrainIconColorSouth("Blue");
		Assert.assertEquals("Train Icon Color South", "Blue", s.getTrainIconColorSouth());

		s.setTrainIconColorEast("Green");
		Assert.assertEquals("Train Icon Color East", "Green", s.getTrainIconColorEast());

		s.setTrainIconColorWest("Brown");
		Assert.assertEquals("Train Icon Color West", "Brown", s.getTrainIconColorWest());

		s.setTrainIconColorLocal("White");
		Assert.assertEquals("Train Icon Color Local", "White", s.getTrainIconColorLocal());

		s.setTrainIconColorTerminate("Black");
		Assert.assertEquals("Train Icon Color Terminate", "Black", s.getTrainIconColorTerminate());
	}

	// test xml file creation
	public void testXMLFileCreate() throws Exception {
		Setup s;
		s = createTestSetup();

		Assert.assertEquals("Create Railroad Name", "File Test Railroad Name", s.getRailroadName());
		Assert.assertEquals("Create Railroad Owner", "File Test Railroad Owner", s.getOwnerName());
		Assert.assertEquals("Create Panel Name", "File Test Panel Name", s.getPanelName());
		Assert.assertEquals("Create Font Name", "File Test Font Name", s.getFontName());

		Assert.assertEquals("Create Direction East", 1+2+4+8, s.getTrainDirection());
		Assert.assertEquals("Create Train Length", 1111, s.getTrainLength());
		Assert.assertEquals("Create Engine Size", 111, s.getEngineSize());
		Assert.assertEquals("Create Scale", 11, s.getScale());

		Assert.assertEquals("Create Train Icon Cord Enabled True", true, s.isTrainIconCordEnabled());
		Assert.assertEquals("Create Train Icon Append Enabled True", true, s.isTrainIconAppendEnabled());
		Assert.assertEquals("Create Train Icon Color North", "Blue", s.getTrainIconColorNorth());
		Assert.assertEquals("Create Train Icon Color South", "Red", s.getTrainIconColorSouth());
		Assert.assertEquals("Create Train Icon Color East", "Brown", s.getTrainIconColorEast());
		Assert.assertEquals("Create Train Icon Color West", "Green", s.getTrainIconColorWest());
		Assert.assertEquals("Create Train Icon Color Local", "Black", s.getTrainIconColorLocal());
		Assert.assertEquals("Create Train Icon Color Terminate", "White", s.getTrainIconColorTerminate());
	}

	// test xml file read
	public void testXMLFileRead() throws Exception {
		Setup s = new Setup();

		s.setRailroadName("Before Read Test Railroad Name");
		s.setOwnerName("Before Read Test Railroad Owner");
		s.setPanelName("Before Read Test Panel Name");
		s.setFontName("Before Read Test Font Name");

		s.setTrainDirection(s.EAST);
		s.setTrainLength(2222);
		s.setEngineSize(222);
		s.setScale(s.N_SCALE);

		s.setTrainIconCordEnabled(false);
		s.setTrainIconAppendEnabled(false);
		s.setTrainIconColorNorth("Red");
		s.setTrainIconColorSouth("Blue");
		s.setTrainIconColorEast("Green");
		s.setTrainIconColorWest("Brown");
		s.setTrainIconColorLocal("White");
		s.setTrainIconColorTerminate("Black");

		Assert.assertEquals("Before Read Railroad Name", "Before Read Test Railroad Name", s.getRailroadName());
		Assert.assertEquals("Before Read Railroad Owner", "Before Read Test Railroad Owner", s.getOwnerName());
		Assert.assertEquals("Before Read Panel Name", "Before Read Test Panel Name", s.getPanelName());
		Assert.assertEquals("Before Read Font Name", "Before Read Test Font Name", s.getFontName());

		Assert.assertEquals("Before Read Direction East", 1, s.getTrainDirection());
		Assert.assertEquals("Before Read Train Length", 2222, s.getTrainLength());
		Assert.assertEquals("Before Read Engine Size", 222, s.getEngineSize());
		Assert.assertEquals("Before Read Scale", 2, s.getScale());

		Assert.assertEquals("Before Read Train Icon Cord Enabled True", false, s.isTrainIconCordEnabled());
		Assert.assertEquals("Before Read Train Icon Append Enabled True", false, s.isTrainIconAppendEnabled());
		Assert.assertEquals("Before Read Train Icon Color North", "Red", s.getTrainIconColorNorth());
		Assert.assertEquals("Before Read Train Icon Color South", "Blue", s.getTrainIconColorSouth());
		Assert.assertEquals("Before Read Train Icon Color East", "Green", s.getTrainIconColorEast());
		Assert.assertEquals("Before Read Train Icon Color West", "Brown", s.getTrainIconColorWest());
		Assert.assertEquals("Before Read Train Icon Color Local", "White", s.getTrainIconColorLocal());
		Assert.assertEquals("Before Read Train Icon Color Terminate", "Black", s.getTrainIconColorTerminate());

		readTestSetup();

		Assert.assertEquals("After Read Railroad Name", "File Test Railroad Name", s.getRailroadName());
		Assert.assertEquals("After Read Railroad Owner", "File Test Railroad Owner", s.getOwnerName());
		Assert.assertEquals("After Read Panel Name", "File Test Panel Name", s.getPanelName());
		Assert.assertEquals("After Read Font Name", "File Test Font Name", s.getFontName());

		Assert.assertEquals("After Read Direction East", 1+2+4+8, s.getTrainDirection());
		Assert.assertEquals("After Read Train Length", 1111, s.getTrainLength());
		Assert.assertEquals("After Read Engine Size", 111, s.getEngineSize());
		Assert.assertEquals("After Read Scale", 11, s.getScale());

		Assert.assertEquals("After Read Train Icon Cord Enabled True", true, s.isTrainIconCordEnabled());
		Assert.assertEquals("After Read Train Icon Append Enabled True", true, s.isTrainIconAppendEnabled());
		Assert.assertEquals("After Read Train Icon Color North", "Blue", s.getTrainIconColorNorth());
		Assert.assertEquals("After Read Train Icon Color South", "Red", s.getTrainIconColorSouth());
		Assert.assertEquals("After Read Train Icon Color East", "Brown", s.getTrainIconColorEast());
		Assert.assertEquals("After Read Train Icon Color West", "Green", s.getTrainIconColorWest());
		Assert.assertEquals("After Read Train Icon Color Local", "Black", s.getTrainIconColorLocal());
		Assert.assertEquals("After Read Train Icon Color Terminate", "White", s.getTrainIconColorTerminate());
	}


	// TODO: Add test to create xml file

	// TODO: Add test to read xml file

	public static Setup createTestSetup() throws org.jdom.JDOMException, java.io.IOException, java.io.FileNotFoundException {
		// this uses explicit filenames intentionally, to ensure that
		// the resulting files go into the test tree area.

		OperationsXml ox = new OperationsXml();

		// store files in "temp"
		XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
		XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+"temp");

		// change file name to OperationsTest
		ox.setOperationsFileName("OperationsTest.xml");

		// remove existing Operations file if its there
		File f = new File(XmlFile.prefsDir()+"temp"+File.separator+"OperationsTest.xml");
		f.delete();

		// create a Operations file with known contents
		Setup s = new Setup();
		s.setRailroadName("File Test Railroad Name");
		s.setOwnerName("File Test Railroad Owner");
		s.setPanelName("File Test Panel Name");
		s.setFontName("File Test Font Name");

		s.setTrainDirection(s.EAST+s.WEST+s.NORTH+s.SOUTH);
		s.setTrainLength(1111);
		s.setEngineSize(111);
		s.setScale(s.G_SCALE);

		s.setTrainIconCordEnabled(true);
		s.setTrainIconAppendEnabled(true);
		s.setTrainIconColorNorth("Blue");
		s.setTrainIconColorSouth("Red");
		s.setTrainIconColorEast("Brown");
		s.setTrainIconColorWest("Green");
		s.setTrainIconColorLocal("Black");
		s.setTrainIconColorTerminate("White");

		// write it
		ox.writeFile(XmlFile.prefsDir()+"temp"+File.separator+"OperationsTest.xml");

		// Set filename back to Operations
		ox.setOperationsFileName("Operations.xml");

		return s;
	}

	public void readTestSetup() throws org.jdom.JDOMException, java.io.IOException, java.io.FileNotFoundException {
		// this uses explicit filenames intentionally, to ensure that
		// the resulting files go into the test tree area.

		OperationsXml ox = new OperationsXml();

		// store files in "temp"
		XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
		XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+"temp");

		// change file name to OperationsTest
		ox.setOperationsFileName("OperationsTest.xml");

		// create a Operations file with known contents
		Setup s = new Setup();

		// read it
		ox.readFile(XmlFile.prefsDir()+"temp"+File.separator+"OperationsTest.xml");

		// Set filename back to Operations
		ox.setOperationsFileName("Operations.xml");
	}

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
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();

        // create a new instance manager
        InstanceManager i = new InstanceManager(){
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

	public OperationsSetupTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsSetupTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsSetupTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
