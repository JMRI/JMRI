// OperationsSetupTest.java

package jmri.jmrit.operations.setup;

import jmri.jmrit.XmlFile;

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
 * Tests for the Operations Setup class
 * Last manually cross-checked on 20090131
 * 
 * Still to do:
 *   Backup, Control, Demo
 *  
 * @author	Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision: 1.3 $
 */
public class OperationsSetupTest extends TestCase {

	// test creation
	public void testCreate() {
		Setup s = new Setup();
		Setup.setRailroadName("Test Railroad Name");
		Assert.assertEquals("Railroad Name", "Test Railroad Name", Setup.getRailroadName());
		Setup.setOwnerName("Test Owner Name");
		Assert.assertEquals("Owner Name", "Test Owner Name", Setup.getOwnerName());
	}

	// test public constants
	public void testConstants() {
		Setup s = new Setup();

		Setup.setRailroadName("Test Railroad Name");
		Assert.assertEquals("Railroad Name", "Test Railroad Name", Setup.getRailroadName());

                Assert.assertEquals("Operations Setup Constant Z_SCALE", 1, Setup.Z_SCALE);
		Assert.assertEquals("Operations Setup Constant N_SCALE", 2, Setup.N_SCALE);
		Assert.assertEquals("Operations Setup Constant TT_SCALE", 3, Setup.TT_SCALE);
		Assert.assertEquals("Operations Setup Constant HOn3_SCALE", 4, Setup.HOn3_SCALE);
		Assert.assertEquals("Operations Setup Constant OO_SCALE", 5, Setup.OO_SCALE);
		Assert.assertEquals("Operations Setup Constant HO_SCALE", 6, Setup.HO_SCALE);
		Assert.assertEquals("Operations Setup Constant Sn3_SCALE", 7, Setup.Sn3_SCALE);
		Assert.assertEquals("Operations Setup Constant S_SCALE", 8, Setup.S_SCALE);
		Assert.assertEquals("Operations Setup Constant On3_SCALE", 9, Setup.On3_SCALE);
		Assert.assertEquals("Operations Setup Constant O_SCALE", 10, Setup.O_SCALE);
		Assert.assertEquals("Operations Setup Constant G_SCALE", 11, Setup.G_SCALE);

		Assert.assertEquals("Operations Setup Constant EAST", 1, Setup.EAST);
		Assert.assertEquals("Operations Setup Constant WEST", 2, Setup.WEST);
		Assert.assertEquals("Operations Setup Constant NORTH", 4, Setup.NORTH);
		Assert.assertEquals("Operations Setup Constant SOUTH", 8, Setup.SOUTH);

		Assert.assertEquals("Operations Setup Constant EAST_DIR", "East", Setup.EAST_DIR);
		Assert.assertEquals("Operations Setup Constant WEST_DIR", "West", Setup.WEST_DIR);
		Assert.assertEquals("Operations Setup Constant NORTH_DIR", "North", Setup.NORTH_DIR);
		Assert.assertEquals("Operations Setup Constant SOUTH_DIR", "South", Setup.SOUTH_DIR);

		Assert.assertEquals("Operations Setup Constant DESCRIPTIVE", "Descriptive", Setup.DESCRIPTIVE);
                /* Should be fixed in setup to AAR Codes */
                Assert.assertEquals("Operations Setup Constant AAR", "ARR Codes", Setup.AAR);
	        
		Assert.assertEquals("Operations Setup Constant MONOSPACED", "Monospaced", Setup.MONOSPACED);
		Assert.assertEquals("Operations Setup Constant SANSERIF", "SansSerif", Setup.SANSERIF);
		Assert.assertEquals("Operations Setup Constant LENGTHABV", "'", Setup.LENGTHABV);

                Assert.assertEquals("Operations Setup Constant BUILD_REPORT_MINIMAL", "1", Setup.BUILD_REPORT_MINIMAL);
                Assert.assertEquals("Operations Setup Constant BUILD_REPORT_NORMAL", "3", Setup.BUILD_REPORT_NORMAL);
                Assert.assertEquals("Operations Setup Constant BUILD_REPORT_DETAILED", "5", Setup.BUILD_REPORT_DETAILED);
                Assert.assertEquals("Operations Setup Constant BUILD_REPORT_VERY_DETAILED", "7", Setup.BUILD_REPORT_VERY_DETAILED);
	}
	
	// test menu attributes
	public void testMenuAttributes() {
		Setup s = new Setup();
		Setup.setMainMenuEnabled(true);
		Assert.assertTrue(Setup.isMainMenuEnabled());
		Setup.setMainMenuEnabled(false);
		Assert.assertFalse(Setup.isMainMenuEnabled());
	}
	
	
	// test scale attributes
	public void testScaleAttributes() {
		Setup s = new Setup();
		// Not really necessary
		Setup.setRailroadName("Test Railroad Name");
		Assert.assertEquals("Railroad Name", "Test Railroad Name", Setup.getRailroadName());
		Setup.setOwnerName("Test Owner Name");
		Assert.assertEquals("Owner Name", "Test Owner Name", Setup.getOwnerName());

		Setup.setScale(Setup.Z_SCALE);
		Assert.assertEquals("Z Scale", 1, Setup.getScale());
		Assert.assertEquals("Z Scale Ratio", 220, Setup.getScaleRatio());
		Assert.assertEquals("Z Scale Ton Ratio", 130, Setup.getScaleTonRatio());
		Assert.assertEquals("Z Initial Weight", 364, Setup.getInitalWeight());
		Assert.assertEquals("Z Added Weight", 100, Setup.getAddWeight());

		Setup.setScale(Setup.N_SCALE);
		Assert.assertEquals("N Scale", 2, Setup.getScale());
		Assert.assertEquals("N Scale Ratio", 160, Setup.getScaleRatio());
		Assert.assertEquals("N Scale Ton Ratio", 80, Setup.getScaleTonRatio());
		Assert.assertEquals("N Initial Weight", 500, Setup.getInitalWeight());
		Assert.assertEquals("N Added Weight", 150, Setup.getAddWeight());

		Setup.setScale(Setup.TT_SCALE);
		Assert.assertEquals("TT Scale", 3, Setup.getScale());
		Assert.assertEquals("TT Scale Ratio", 120, Setup.getScaleRatio());
		Assert.assertEquals("TT Scale Ton Ratio", 36, Setup.getScaleTonRatio());
		Assert.assertEquals("TT Initial Weight", 750, Setup.getInitalWeight());
		Assert.assertEquals("TT Added Weight", 375, Setup.getAddWeight());

		Setup.setScale(Setup.HOn3_SCALE);
		Assert.assertEquals("HOn3 Scale", 4, Setup.getScale());
		Assert.assertEquals("HOn3 Scale Ratio", 87, Setup.getScaleRatio());
		Assert.assertEquals("HOn3 Scale Ton Ratio", 20, Setup.getScaleTonRatio());
		Assert.assertEquals("HOn3 Initial Weight", 750, Setup.getInitalWeight());
		Assert.assertEquals("HOn3 Added Weight", 375, Setup.getAddWeight());

		Setup.setScale(Setup.OO_SCALE);
		Assert.assertEquals("OO Scale", 5, Setup.getScale());
		Assert.assertEquals("OO Scale Ratio", 76, Setup.getScaleRatio());
		Assert.assertEquals("OO Scale Ton Ratio", 20, Setup.getScaleTonRatio());
		Assert.assertEquals("OO Initial Weight", 750, Setup.getInitalWeight());
		Assert.assertEquals("OO Added Weight", 500, Setup.getAddWeight());

		Setup.setScale(Setup.HO_SCALE);
		Assert.assertEquals("HO Scale", 6, Setup.getScale());
		Assert.assertEquals("HO Scale Ratio", 87, Setup.getScaleRatio());
		Assert.assertEquals("HO Scale Ton Ratio", 20, Setup.getScaleTonRatio());
		Assert.assertEquals("HO Initial Weight", 1000, Setup.getInitalWeight());
		Assert.assertEquals("HO Added Weight", 500, Setup.getAddWeight());

		Setup.setScale(Setup.Sn3_SCALE);
		Assert.assertEquals("Sn3 Scale", 7, Setup.getScale());
		Assert.assertEquals("Sn3 Scale Ratio", 64, Setup.getScaleRatio());
		Assert.assertEquals("Sn3 Scale Ton Ratio", 16, Setup.getScaleTonRatio());
		Assert.assertEquals("Sn3 Initial Weight", 1000, Setup.getInitalWeight());
		Assert.assertEquals("Sn3 Added Weight", 500, Setup.getAddWeight());

		Setup.setScale(Setup.S_SCALE);
		Assert.assertEquals("S Scale", 8, Setup.getScale());
		Assert.assertEquals("S Scale Ratio", 64, Setup.getScaleRatio());
		Assert.assertEquals("S Scale Ton Ratio", 14, Setup.getScaleTonRatio());
		Assert.assertEquals("S Initial Weight", 2000, Setup.getInitalWeight());
		Assert.assertEquals("S Added Weight", 500, Setup.getAddWeight());

		Setup.setScale(Setup.On3_SCALE);
		Assert.assertEquals("On3 Scale", 9, Setup.getScale());
		Assert.assertEquals("On3 Scale Ratio", 48, Setup.getScaleRatio());
		Assert.assertEquals("On3 Scale Ton Ratio", 8, Setup.getScaleTonRatio());
		Assert.assertEquals("On3 Initial Weight", 1500, Setup.getInitalWeight());
		Assert.assertEquals("On3 Added Weight", 750, Setup.getAddWeight());

		Setup.setScale(Setup.O_SCALE);
		Assert.assertEquals("O Scale", 10, Setup.getScale());
		Assert.assertEquals("O Scale Ratio", 48, Setup.getScaleRatio());
		Assert.assertEquals("O Scale Ton Ratio", 5, Setup.getScaleTonRatio());
		Assert.assertEquals("O Initial Weight", 5000, Setup.getInitalWeight());
		Assert.assertEquals("O Added Weight", 1000, Setup.getAddWeight());

		Setup.setScale(Setup.G_SCALE);
		Assert.assertEquals("G Scale", 11, Setup.getScale());
		Assert.assertEquals("G Scale Ratio", 32, Setup.getScaleRatio());
		Assert.assertEquals("G Scale Ton Ratio", 2, Setup.getScaleTonRatio());
		Assert.assertEquals("G Initial Weight", 10000, Setup.getInitalWeight());
		Assert.assertEquals("G Added Weight", 2000, Setup.getAddWeight());
	}

        // test train attributes
	public void testTrainAttributes() {
		Setup s = new Setup();
		// Not really necessary
		Setup.setRailroadName("Test Railroad Name");
		Assert.assertEquals("Railroad Name", "Test Railroad Name", Setup.getRailroadName());
		Setup.setOwnerName("Test Owner Name");
		Assert.assertEquals("Owner Name", "Test Owner Name", Setup.getOwnerName());

		Setup.setTrainDirection(Setup.EAST);
		Assert.assertEquals("Direction East", 1, Setup.getTrainDirection());
		Setup.setTrainDirection(Setup.WEST);
		Assert.assertEquals("Direction West", 2, Setup.getTrainDirection());
		Setup.setTrainDirection(Setup.NORTH);
		Assert.assertEquals("Direction North", 4, Setup.getTrainDirection());
		Setup.setTrainDirection(Setup.SOUTH);
		Assert.assertEquals("Direction South", 8, Setup.getTrainDirection());

		Setup.setTrainLength(520);
		Assert.assertEquals("Train Length", 520, Setup.getTrainLength());

		Setup.setEngineSize(120);
		Assert.assertEquals("Engine Size", 120, Setup.getEngineSize());

		Setup.setCarMoves(12);
		Assert.assertEquals("Car Moves", 12, Setup.getCarMoves());

		Setup.setCarTypes("Test Car Types");
		Assert.assertEquals("Car Types", "Test Car Types", Setup.getCarTypes());

                Setup.setAppendCarCommentEnabled(true);
		Assert.assertTrue(Setup.isAppendCarCommentEnabled());
		Setup.setAppendCarCommentEnabled(false);
		Assert.assertFalse(Setup.isAppendCarCommentEnabled());

                Setup.setShowCarLoadEnabled(true);
		Assert.assertTrue(Setup.isShowCarLoadEnabled());
		Setup.setShowCarLoadEnabled(false);
		Assert.assertFalse(Setup.isShowCarLoadEnabled());

		Setup.setBuildReportLevel("Test Build Report Level");
		Assert.assertEquals("Build Report Level", "Test Build Report Level", Setup.getBuildReportLevel());

		Setup.setSwitchTime(4);
		Assert.assertEquals("Switch Time", 4, Setup.getSwitchTime());

		Setup.setTravelTime(8);
		Assert.assertEquals("Travel Time", 8, Setup.getTravelTime());
	}
	
	// test panel attributes
	public void testPanelAttributes() {
		Setup s = new Setup();
		// Not really necessary
		Setup.setRailroadName("Test Railroad Name");
		Assert.assertEquals("Railroad Name", "Test Railroad Name", Setup.getRailroadName());
		Setup.setOwnerName("Test Owner Name");
		Assert.assertEquals("Owner Name", "Test Owner Name", Setup.getOwnerName());

		Setup.setPanelName("Test Panel Name");
		Assert.assertEquals("Panel Name", "Test Panel Name", Setup.getPanelName());

		Setup.setFontName("Test Font Name");
		Assert.assertEquals("Font Name", "Test Font Name", Setup.getFontName());

		Setup.setTrainIconCordEnabled(true);
		Assert.assertEquals("Train Icon Cord Enabled True", true, Setup.isTrainIconCordEnabled());

		Setup.setTrainIconCordEnabled(false);
		Assert.assertEquals("Train Icon Cord Enabled False", false, Setup.isTrainIconCordEnabled());

		Setup.setTrainIconAppendEnabled(true);
		Assert.assertEquals("Train Icon Append Enabled True", true, Setup.isTrainIconAppendEnabled());

		Setup.setTrainIconAppendEnabled(false);
		Assert.assertEquals("Train Icon Append Enabled False", false, Setup.isTrainIconAppendEnabled());

		Setup.setTrainIconColorNorth("Red");
		Assert.assertEquals("Train Icon Color North", "Red", Setup.getTrainIconColorNorth());

		Setup.setTrainIconColorSouth("Blue");
		Assert.assertEquals("Train Icon Color South", "Blue", Setup.getTrainIconColorSouth());

		Setup.setTrainIconColorEast("Green");
		Assert.assertEquals("Train Icon Color East", "Green", Setup.getTrainIconColorEast());

		Setup.setTrainIconColorWest("Brown");
		Assert.assertEquals("Train Icon Color West", "Brown", Setup.getTrainIconColorWest());

		Setup.setTrainIconColorLocal("White");
		Assert.assertEquals("Train Icon Color Local", "White", Setup.getTrainIconColorLocal());

		Setup.setTrainIconColorTerminate("Black");
		Assert.assertEquals("Train Icon Color Terminate", "Black", Setup.getTrainIconColorTerminate());
	}

	// test xml file creation
	public void testXMLFileCreate() throws Exception {
		Setup s;
		s = createTestSetup();

		Assert.assertEquals("Create Railroad Name", "File Test Railroad Name", Setup.getRailroadName());
		Assert.assertEquals("Create Railroad Owner", "File Test Railroad Owner", Setup.getOwnerName());
		Assert.assertEquals("Create Panel Name", "File Test Panel Name", Setup.getPanelName());
		Assert.assertEquals("Create Font Name", "File Test Font Name", Setup.getFontName());

		Assert.assertEquals("Create Direction East", 1+2+4+8, Setup.getTrainDirection());
		Assert.assertEquals("Create Train Length", 1111, Setup.getTrainLength());
		Assert.assertEquals("Create Engine Size", 111, Setup.getEngineSize());
		Assert.assertEquals("Create Scale", 11, Setup.getScale());

		Assert.assertEquals("Create Train Icon Cord Enabled True", true, Setup.isTrainIconCordEnabled());
		Assert.assertEquals("Create Train Icon Append Enabled True", true, Setup.isTrainIconAppendEnabled());
		Assert.assertEquals("Create Train Icon Color North", "Blue", Setup.getTrainIconColorNorth());
		Assert.assertEquals("Create Train Icon Color South", "Red", Setup.getTrainIconColorSouth());
		Assert.assertEquals("Create Train Icon Color East", "Brown", Setup.getTrainIconColorEast());
		Assert.assertEquals("Create Train Icon Color West", "Green", Setup.getTrainIconColorWest());
		Assert.assertEquals("Create Train Icon Color Local", "Black", Setup.getTrainIconColorLocal());
		Assert.assertEquals("Create Train Icon Color Terminate", "White", Setup.getTrainIconColorTerminate());
	}

	// test xml file read
	public void testXMLFileRead() throws Exception {
		Setup s = new Setup();

		Setup.setRailroadName("Before Read Test Railroad Name");
		Setup.setOwnerName("Before Read Test Railroad Owner");
		Setup.setPanelName("Before Read Test Panel Name");
		Setup.setFontName("Before Read Test Font Name");

                Setup.setMainMenuEnabled(false);

		Setup.setTrainDirection(Setup.EAST);
		Setup.setTrainLength(2222);
		Setup.setEngineSize(222);
		Setup.setScale(Setup.N_SCALE);

                Setup.setCarTypes("Before Read Test Car Types");
		Setup.setSwitchTime(22);
		Setup.setTravelTime(222);
		Setup.setShowCarLoadEnabled(false);
		Setup.setAppendCarCommentEnabled(false);
		Setup.setBuildReportLevel("22");

		Setup.setTrainIconCordEnabled(false);
		Setup.setTrainIconAppendEnabled(false);
		Setup.setTrainIconColorNorth("Red");
		Setup.setTrainIconColorSouth("Blue");
		Setup.setTrainIconColorEast("Green");
		Setup.setTrainIconColorWest("Brown");
		Setup.setTrainIconColorLocal("White");
		Setup.setTrainIconColorTerminate("Black");

		Assert.assertEquals("Before Read Railroad Name", "Before Read Test Railroad Name", Setup.getRailroadName());
		Assert.assertEquals("Before Read Railroad Owner", "Before Read Test Railroad Owner", Setup.getOwnerName());
		Assert.assertEquals("Before Read Panel Name", "Before Read Test Panel Name", Setup.getPanelName());
		Assert.assertEquals("Before Read Font Name", "Before Read Test Font Name", Setup.getFontName());

                Assert.assertEquals("Before Read Main Menu Enabled", false, Setup.isMainMenuEnabled());

		Assert.assertEquals("Before Read Direction East", 1, Setup.getTrainDirection());
		Assert.assertEquals("Before Read Train Length", 2222, Setup.getTrainLength());
		Assert.assertEquals("Before Read Engine Size", 222, Setup.getEngineSize());
		Assert.assertEquals("Before Read Scale", 2, Setup.getScale());

                Assert.assertEquals("Before Read Test Car Types", "Before Read Test Car Types", Setup.getCarTypes());
		Assert.assertEquals("Before Read Switch Time", 22, Setup.getSwitchTime());
		Assert.assertEquals("Before Read Travel Time", 222, Setup.getTravelTime());
                Assert.assertEquals("Before Read Show Car Load Enabled", false, Setup.isShowCarLoadEnabled());
                Assert.assertEquals("Before Read Append Car Comment Enabled", false, Setup.isAppendCarCommentEnabled());
                Assert.assertEquals("Before Read Build Report Level", "22", Setup.getBuildReportLevel());

		Assert.assertEquals("Before Read Train Icon Cord Enabled True", false, Setup.isTrainIconCordEnabled());
		Assert.assertEquals("Before Read Train Icon Append Enabled True", false, Setup.isTrainIconAppendEnabled());
		Assert.assertEquals("Before Read Train Icon Color North", "Red", Setup.getTrainIconColorNorth());
		Assert.assertEquals("Before Read Train Icon Color South", "Blue", Setup.getTrainIconColorSouth());
		Assert.assertEquals("Before Read Train Icon Color East", "Green", Setup.getTrainIconColorEast());
		Assert.assertEquals("Before Read Train Icon Color West", "Brown", Setup.getTrainIconColorWest());
		Assert.assertEquals("Before Read Train Icon Color Local", "White", Setup.getTrainIconColorLocal());
		Assert.assertEquals("Before Read Train Icon Color Terminate", "Black", Setup.getTrainIconColorTerminate());

		readTestSetup();

		Assert.assertEquals("After Read Railroad Name", "File Test Railroad Name", Setup.getRailroadName());
		Assert.assertEquals("After Read Railroad Owner", "File Test Railroad Owner", Setup.getOwnerName());
		Assert.assertEquals("After Read Panel Name", "File Test Panel Name", Setup.getPanelName());
		Assert.assertEquals("After Read Font Name", "File Test Font Name", Setup.getFontName());

                Assert.assertEquals("After Read Main Menu Enabled", true, Setup.isMainMenuEnabled());

		Assert.assertEquals("After Read Direction East", 1+2+4+8, Setup.getTrainDirection());
		Assert.assertEquals("After Read Train Length", 1111, Setup.getTrainLength());
		Assert.assertEquals("After Read Engine Size", 111, Setup.getEngineSize());
		Assert.assertEquals("After Read Scale", 11, Setup.getScale());

                Assert.assertEquals("After Read Test Car Types", "File Test Car Types", Setup.getCarTypes());
		Assert.assertEquals("After Read Switch Time", 11, Setup.getSwitchTime());
		Assert.assertEquals("After Read Travel Time", 111, Setup.getTravelTime());
                Assert.assertEquals("After Read Show Car Load Enabled", true, Setup.isShowCarLoadEnabled());
                Assert.assertEquals("After Read Append Car Comment Enabled", true, Setup.isAppendCarCommentEnabled());
                Assert.assertEquals("After Read Build Report Level", "11", Setup.getBuildReportLevel());

		Assert.assertEquals("After Read Train Icon Cord Enabled True", true, Setup.isTrainIconCordEnabled());
		Assert.assertEquals("After Read Train Icon Append Enabled True", true, Setup.isTrainIconAppendEnabled());
		Assert.assertEquals("After Read Train Icon Color North", "Blue", Setup.getTrainIconColorNorth());
		Assert.assertEquals("After Read Train Icon Color South", "Red", Setup.getTrainIconColorSouth());
		Assert.assertEquals("After Read Train Icon Color East", "Brown", Setup.getTrainIconColorEast());
		Assert.assertEquals("After Read Train Icon Color West", "Green", Setup.getTrainIconColorWest());
		Assert.assertEquals("After Read Train Icon Color Local", "Black", Setup.getTrainIconColorLocal());
		Assert.assertEquals("After Read Train Icon Color Terminate", "White", Setup.getTrainIconColorTerminate());
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
		OperationsXml.setOperationsFileName("OperationsTest.xml");

		// remove existing Operations file if its there
		File f = new File(XmlFile.prefsDir()+"temp"+File.separator+"OperationsTest.xml");
		f.delete();

		// create a Operations file with known contents
		Setup s = new Setup();
		Setup.setRailroadName("File Test Railroad Name");
		Setup.setOwnerName("File Test Railroad Owner");
		Setup.setPanelName("File Test Panel Name");
		Setup.setFontName("File Test Font Name");
                
                Setup.setMainMenuEnabled(true);
                
		Setup.setTrainDirection(Setup.EAST+Setup.WEST+Setup.NORTH+Setup.SOUTH);
		Setup.setTrainLength(1111);
		Setup.setEngineSize(111);
		Setup.setScale(Setup.G_SCALE);
		Setup.setCarTypes("File Test Car Types");
		Setup.setSwitchTime(11);
		Setup.setTravelTime(111);
		Setup.setShowCarLoadEnabled(true);
		Setup.setAppendCarCommentEnabled(true);
		Setup.setBuildReportLevel("11");
                        
		Setup.setTrainIconCordEnabled(true);
		Setup.setTrainIconAppendEnabled(true);
		Setup.setTrainIconColorNorth("Blue");
		Setup.setTrainIconColorSouth("Red");
		Setup.setTrainIconColorEast("Brown");
		Setup.setTrainIconColorWest("Green");
		Setup.setTrainIconColorLocal("Black");
		Setup.setTrainIconColorTerminate("White");

		// write it
		ox.writeFile(XmlFile.prefsDir()+"temp"+File.separator+"OperationsTest.xml");

		// Set filename back to Operations
		OperationsXml.setOperationsFileName("Operations.xml");

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
		OperationsXml.setOperationsFileName("OperationsTest.xml");

		// create a Operations file with known contents
		Setup s = new Setup();

		// read it
		ox.readFile(XmlFile.prefsDir()+"temp"+File.separator+"OperationsTest.xml");

		// Set filename back to Operations
		OperationsXml.setOperationsFileName("Operations.xml");
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
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();

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
    @Override
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
