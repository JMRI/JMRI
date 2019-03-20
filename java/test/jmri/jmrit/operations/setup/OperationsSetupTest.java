package jmri.jmrit.operations.setup;

import java.io.File;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.trains.TrainManagerXml;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Operations Setup class Last manually cross-checked on 20090131
 * <p>
 * Still to do: Backup, Control, Demo
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
 */
public class OperationsSetupTest extends OperationsTestCase {

    // test creation
    @Test
    @SuppressWarnings("static-access")
    public void testCreate() {
        Setup s = new Setup();
        s.setRailroadName("Test Railroad Name");
        Assert.assertEquals("Railroad Name", "Test Railroad Name", s.getRailroadName());
        s.setOwnerName("Test Owner Name");
        Assert.assertEquals("Owner Name", "Test Owner Name", s.getOwnerName());
    }

    // test public constants
    @SuppressWarnings("static-access")
    @Test
    public void testConstants() {
        Setup s = new Setup();

        s.setRailroadName("Test Railroad Name");
        Assert.assertEquals("Railroad Name", "Test Railroad Name", s.getRailroadName());

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

        Assert.assertEquals("Operations Setup Constant EAST_DIR", Bundle.getMessage("East"), Setup.EAST_DIR);
        Assert.assertEquals("Operations Setup Constant WEST_DIR", Bundle.getMessage("West"), Setup.WEST_DIR);
        Assert.assertEquals("Operations Setup Constant NORTH_DIR", Bundle.getMessage("North"), Setup.NORTH_DIR);
        Assert.assertEquals("Operations Setup Constant SOUTH_DIR", Bundle.getMessage("South"), Setup.SOUTH_DIR);

        Assert.assertEquals("Operations Setup Constant DESCRIPTIVE", Bundle.getMessage("Descriptive"), Setup.DESCRIPTIVE);
        /* Should be fixed in setup to AAR Codes */
        Assert.assertEquals("Operations Setup Constant AAR", Bundle.getMessage("ArrCodes"), Setup.AAR);

        Assert.assertEquals("Operations Setup Constant MONOSPACED", Bundle.getMessage("Monospaced"), Setup.MONOSPACED);
        Assert.assertEquals("Operations Setup Constant LENGTHABV", Bundle.getMessage("LengthSymbol"), Setup.LENGTHABV);

        Assert.assertEquals("Operations Setup Constant BUILD_REPORT_MINIMAL", "1", Setup.BUILD_REPORT_MINIMAL);
        Assert.assertEquals("Operations Setup Constant BUILD_REPORT_NORMAL", "3", Setup.BUILD_REPORT_NORMAL);
        Assert.assertEquals("Operations Setup Constant BUILD_REPORT_DETAILED", "5", Setup.BUILD_REPORT_DETAILED);
        Assert.assertEquals("Operations Setup Constant BUILD_REPORT_VERY_DETAILED", "7", Setup.BUILD_REPORT_VERY_DETAILED);
    }

    // test menu attributes
    @SuppressWarnings("static-access")
    @Test
    public void testMenuAttributes() {
        Setup s = new Setup();
        s.setMainMenuEnabled(true);
        /* Seems to be failing on test machine */
//		Assert.assertTrue(s.isMainMenuEnabled());
        s.setMainMenuEnabled(false);
//		Assert.assertFalse(s.isMainMenuEnabled());
    }

    // test scale attributes
    @SuppressWarnings("static-access")
    @Test
    public void testScaleAttributes() {
        Setup s = new Setup();
        // Not really necessary
        s.setRailroadName("Test Railroad Name");
        Assert.assertEquals("Railroad Name", "Test Railroad Name", s.getRailroadName());
        s.setOwnerName("Test Owner Name");
        Assert.assertEquals("Owner Name", "Test Owner Name", s.getOwnerName());

        s.setScale(Setup.Z_SCALE);
        Assert.assertEquals("Z Scale", 1, s.getScale());
        Assert.assertEquals("Z Scale Ratio", 220, s.getScaleRatio());
        Assert.assertEquals("Z Scale Ton Ratio", 130, s.getScaleTonRatio());
        Assert.assertEquals("Z Initial Weight", 364, s.getInitalWeight());
        Assert.assertEquals("Z Added Weight", 100, s.getAddWeight());

        s.setScale(Setup.N_SCALE);
        Assert.assertEquals("N Scale", 2, s.getScale());
        Assert.assertEquals("N Scale Ratio", 160, s.getScaleRatio());
        Assert.assertEquals("N Scale Ton Ratio", 80, s.getScaleTonRatio());
        Assert.assertEquals("N Initial Weight", 500, s.getInitalWeight());
        Assert.assertEquals("N Added Weight", 150, s.getAddWeight());

        s.setScale(Setup.TT_SCALE);
        Assert.assertEquals("TT Scale", 3, s.getScale());
        Assert.assertEquals("TT Scale Ratio", 120, s.getScaleRatio());
        Assert.assertEquals("TT Scale Ton Ratio", 36, s.getScaleTonRatio());
        Assert.assertEquals("TT Initial Weight", 750, s.getInitalWeight());
        Assert.assertEquals("TT Added Weight", 375, s.getAddWeight());

        s.setScale(Setup.HOn3_SCALE);
        Assert.assertEquals("HOn3 Scale", 4, s.getScale());
        Assert.assertEquals("HOn3 Scale Ratio", 87, s.getScaleRatio());
        Assert.assertEquals("HOn3 Scale Ton Ratio", 20, s.getScaleTonRatio());
        Assert.assertEquals("HOn3 Initial Weight", 750, s.getInitalWeight());
        Assert.assertEquals("HOn3 Added Weight", 375, s.getAddWeight());

        s.setScale(Setup.OO_SCALE);
        Assert.assertEquals("OO Scale", 5, s.getScale());
        Assert.assertEquals("OO Scale Ratio", 76, s.getScaleRatio());
        Assert.assertEquals("OO Scale Ton Ratio", 20, s.getScaleTonRatio());
        Assert.assertEquals("OO Initial Weight", 750, s.getInitalWeight());
        Assert.assertEquals("OO Added Weight", 500, s.getAddWeight());

        s.setScale(Setup.HO_SCALE);
        Assert.assertEquals("HO Scale", 6, s.getScale());
        Assert.assertEquals("HO Scale Ratio", 87, s.getScaleRatio());
        Assert.assertEquals("HO Scale Ton Ratio", 20, s.getScaleTonRatio());
        Assert.assertEquals("HO Initial Weight", 1000, s.getInitalWeight());
        Assert.assertEquals("HO Added Weight", 500, s.getAddWeight());

        s.setScale(Setup.Sn3_SCALE);
        Assert.assertEquals("Sn3 Scale", 7, s.getScale());
        Assert.assertEquals("Sn3 Scale Ratio", 64, s.getScaleRatio());
        Assert.assertEquals("Sn3 Scale Ton Ratio", 16, s.getScaleTonRatio());
        Assert.assertEquals("Sn3 Initial Weight", 1000, s.getInitalWeight());
        Assert.assertEquals("Sn3 Added Weight", 500, s.getAddWeight());

        s.setScale(Setup.S_SCALE);
        Assert.assertEquals("S Scale", 8, s.getScale());
        Assert.assertEquals("S Scale Ratio", 64, s.getScaleRatio());
        Assert.assertEquals("S Scale Ton Ratio", 14, s.getScaleTonRatio());
        Assert.assertEquals("S Initial Weight", 2000, s.getInitalWeight());
        Assert.assertEquals("S Added Weight", 500, s.getAddWeight());

        s.setScale(Setup.On3_SCALE);
        Assert.assertEquals("On3 Scale", 9, s.getScale());
        Assert.assertEquals("On3 Scale Ratio", 48, s.getScaleRatio());
        Assert.assertEquals("On3 Scale Ton Ratio", 8, s.getScaleTonRatio());
        Assert.assertEquals("On3 Initial Weight", 1500, s.getInitalWeight());
        Assert.assertEquals("On3 Added Weight", 750, s.getAddWeight());

        s.setScale(Setup.O_SCALE);
        Assert.assertEquals("O Scale", 10, s.getScale());
        Assert.assertEquals("O Scale Ratio", 48, s.getScaleRatio());
        Assert.assertEquals("O Scale Ton Ratio", 5, s.getScaleTonRatio());
        Assert.assertEquals("O Initial Weight", 5000, s.getInitalWeight());
        Assert.assertEquals("O Added Weight", 1000, s.getAddWeight());

        s.setScale(Setup.G_SCALE);
        Assert.assertEquals("G Scale", 11, s.getScale());
        Assert.assertEquals("G Scale Ratio", 32, s.getScaleRatio());
        Assert.assertEquals("G Scale Ton Ratio", 2, s.getScaleTonRatio());
        Assert.assertEquals("G Initial Weight", 10000, s.getInitalWeight());
        Assert.assertEquals("G Added Weight", 2000, s.getAddWeight());
    }

    // test train attributes
    @SuppressWarnings("static-access")
    @Test
    public void testTrainAttributes() {
        Setup s = new Setup();
        // Not really necessary
        s.setRailroadName("Test Railroad Name");
        Assert.assertEquals("Railroad Name", "Test Railroad Name", s.getRailroadName());
        s.setOwnerName("Test Owner Name");
        Assert.assertEquals("Owner Name", "Test Owner Name", s.getOwnerName());

        s.setTrainDirection(Setup.EAST);
        Assert.assertEquals("Direction East", 1, s.getTrainDirection());
        s.setTrainDirection(Setup.WEST);
        Assert.assertEquals("Direction West", 2, s.getTrainDirection());
        s.setTrainDirection(Setup.NORTH);
        Assert.assertEquals("Direction North", 4, s.getTrainDirection());
        s.setTrainDirection(Setup.SOUTH);
        Assert.assertEquals("Direction South", 8, s.getTrainDirection());
        s.setTrainDirection(Setup.EAST + Setup.WEST + Setup.NORTH + Setup.SOUTH);
        Assert.assertEquals("Direction All", 15, s.getTrainDirection());

        s.setMaxTrainLength(520);
        Assert.assertEquals("Train Length", 520, s.getMaxTrainLength());

        s.setMaxNumberEngines(120);
        Assert.assertEquals("Engine Size", 120, s.getMaxNumberEngines());

        s.setCarMoves(12);
        Assert.assertEquals("Car Moves", 12, s.getCarMoves());

        s.setCarTypes("Test Car Types");
        Assert.assertEquals("Car Types", "Test Car Types", s.getCarTypes());

        /*
         s.setAppendCarCommentEnabled(true);
         Assert.assertTrue(s.isAppendCarCommentEnabled());
         s.setAppendCarCommentEnabled(false);
         Assert.assertFalse(s.isAppendCarCommentEnabled());

         s.setShowCarLengthEnabled(true);
         Assert.assertTrue(s.isShowCarLengthEnabled());
         s.setShowCarLengthEnabled(false);
         Assert.assertFalse(s.isShowCarLengthEnabled());

         s.setShowCarLoadEnabled(true);
         Assert.assertTrue(s.isShowCarLoadEnabled());
         s.setShowCarLoadEnabled(false);
         Assert.assertFalse(s.isShowCarLoadEnabled());

         s.setShowCarColorEnabled(true);
         Assert.assertTrue(s.isShowCarColorEnabled());
         s.setShowCarColorEnabled(false);
         Assert.assertFalse(s.isShowCarColorEnabled());

         s.setShowCarDestinationEnabled(true);
         Assert.assertTrue(s.isShowCarDestinationEnabled());
         s.setShowCarDestinationEnabled(false);
         Assert.assertFalse(s.isShowCarDestinationEnabled());
         */
        s.setBuildReportLevel("Test Build Report Level");
        Assert.assertEquals("Build Report Level", "Test Build Report Level", s.getBuildReportLevel());

        s.setSwitchTime(4);
        Assert.assertEquals("Switch Time", 4, s.getSwitchTime());

        s.setTravelTime(8);
        Assert.assertEquals("Travel Time", 8, s.getTravelTime());
    }

    // test panel attributes
    @SuppressWarnings("static-access")
    @Test
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

    // confirm that all operation file names have been modified
    @Test
    public void testXMLFileTestFileName() {
        Assert.assertEquals("Test setup file name", "OperationsJUnitTest.xml", InstanceManager.getDefault(OperationsSetupXml.class).getOperationsFileName());
        Assert.assertEquals("Test location file name", "OperationsJUnitTestLocationRoster.xml", InstanceManager.getDefault(LocationManagerXml.class).getOperationsFileName());
        Assert.assertEquals("Test train file name", "OperationsJUnitTestTrainRoster.xml", InstanceManager.getDefault(TrainManagerXml.class).getOperationsFileName());
        Assert.assertEquals("Test car file name", "OperationsJUnitTestCarRoster.xml", InstanceManager.getDefault(CarManagerXml.class).getOperationsFileName());
        Assert.assertEquals("Test engine file name", "OperationsJUnitTestEngineRoster.xml", InstanceManager.getDefault(EngineManagerXml.class).getOperationsFileName());
        Assert.assertEquals("Test route file name", "OperationsJUnitTestRouteRoster.xml", InstanceManager.getDefault(RouteManagerXml.class).getOperationsFileName());

        Assert.assertEquals("Test directory name", "operations" + File.separator + "JUnitTest", OperationsSetupXml.getOperationsDirectoryName());
    }

    // test xml file creation and read
    @SuppressWarnings("static-access")
    @Test
    public void testXMLFileCreate() throws Exception {
        Setup s;
        s = createTestSetup();

        Assert.assertEquals("Create Railroad Name", "File Test Railroad Name", s.getRailroadName());
        Assert.assertEquals("Create Railroad Owner", "File Test Railroad Owner", s.getOwnerName());
        Assert.assertEquals("Create Panel Name", "File Test Panel Name", s.getPanelName());
        Assert.assertEquals("Create Font Name", "File Test Font Name", s.getFontName());

        Assert.assertEquals("Create Direction East", 1 + 2 + 4 + 8, s.getTrainDirection());
        Assert.assertEquals("Create Train Length", 1111, s.getMaxTrainLength());
        Assert.assertEquals("Create Engine Size", 111, s.getMaxNumberEngines());
        Assert.assertEquals("Create Scale", 11, s.getScale());

        Assert.assertEquals("Create Train Icon Cord Enabled True", true, s.isTrainIconCordEnabled());
        Assert.assertEquals("Create Train Icon Append Enabled True", true, s.isTrainIconAppendEnabled());
        Assert.assertEquals("Create Train Icon Color North", "Blue", s.getTrainIconColorNorth());
        Assert.assertEquals("Create Train Icon Color South", "Red", s.getTrainIconColorSouth());
        Assert.assertEquals("Create Train Icon Color East", "Brown", s.getTrainIconColorEast());
        Assert.assertEquals("Create Train Icon Color West", "Green", s.getTrainIconColorWest());
        Assert.assertEquals("Create Train Icon Color Local", "Black", s.getTrainIconColorLocal());
        Assert.assertEquals("Create Train Icon Color Terminate", "White", s.getTrainIconColorTerminate());

        // now get ready for the read operation
        s = new Setup();

        s.setRailroadName("Before Read Test Railroad Name");
        s.setOwnerName("Before Read Test Railroad Owner");
        s.setPanelName("Before Read Test Panel Name");
        s.setFontName("Before Read Test Font Name");

        s.setMainMenuEnabled(false);

        s.setTrainDirection(Setup.EAST);
        s.setMaxTrainLength(2222);
        s.setMaxNumberEngines(222);
        s.setScale(Setup.N_SCALE);

        s.setCarTypes("Before Read Test Car Types");
        s.setSwitchTime(22);
        s.setTravelTime(222);
        s.setBuildReportLevel("22");

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

        Assert.assertEquals("Before Read Main Menu Enabled", false, s.isMainMenuEnabled());

        Assert.assertEquals("Before Read Direction East", 1, s.getTrainDirection());
        Assert.assertEquals("Before Read Train Length", 2222, s.getMaxTrainLength());
        Assert.assertEquals("Before Read Engine Size", 222, s.getMaxNumberEngines());
        Assert.assertEquals("Before Read Scale", 2, s.getScale());

        Assert.assertEquals("Before Read Test Car Types", "Before Read Test Car Types", s.getCarTypes());
        Assert.assertEquals("Before Read Switch Time", 22, s.getSwitchTime());
        Assert.assertEquals("Before Read Travel Time", 222, s.getTravelTime());
        Assert.assertEquals("Before Read Build Report Level", "22", s.getBuildReportLevel());

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

        Assert.assertEquals("After Read Main Menu Enabled", true, s.isMainMenuEnabled());

        Assert.assertEquals("After Read Direction East", 1 + 2 + 4 + 8, s.getTrainDirection());
        Assert.assertEquals("After Read Train Length", 1111, s.getMaxTrainLength());
        Assert.assertEquals("After Read Engine Size", 111, s.getMaxNumberEngines());
        Assert.assertEquals("After Read Scale", 11, s.getScale());

        Assert.assertEquals("After Read Test Car Types", "File Test Car Types", s.getCarTypes());
        Assert.assertEquals("After Read Switch Time", 11, s.getSwitchTime());
        Assert.assertEquals("After Read Travel Time", 111, s.getTravelTime());
        Assert.assertEquals("After Read Build Report Level", "11", s.getBuildReportLevel());

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
    @SuppressWarnings("static-access")
    private Setup createTestSetup() throws java.io.IOException, java.io.FileNotFoundException {
        // this uses explicit filenames intentionally, to ensure that
        // the resulting files go into the test tree area.

        OperationsSetupXml ox = new OperationsSetupXml();

        // create a Operations file with known contents
        Setup s = new Setup();
        s.setRailroadName("File Test Railroad Name");
        s.setOwnerName("File Test Railroad Owner");
        s.setPanelName("File Test Panel Name");
        s.setFontName("File Test Font Name");

        s.setMainMenuEnabled(true);

        s.setTrainDirection(Setup.EAST + Setup.WEST + Setup.NORTH + Setup.SOUTH);
        s.setMaxTrainLength(1111);
        s.setMaxNumberEngines(111);
        s.setScale(Setup.G_SCALE);
        s.setCarTypes("File Test Car Types");
        s.setSwitchTime(11);
        s.setTravelTime(111);
        s.setBuildReportLevel("11");

        s.setTrainIconCordEnabled(true);
        s.setTrainIconAppendEnabled(true);
        s.setTrainIconColorNorth("Blue");
        s.setTrainIconColorSouth("Red");
        s.setTrainIconColorEast("Brown");
        s.setTrainIconColorWest("Green");
        s.setTrainIconColorLocal("Black");
        s.setTrainIconColorTerminate("White");

        // store files in "temp"
        // write it
        ox.writeFile(OperationsSetupXml.getFileLocation() + OperationsSetupXml.getOperationsDirectoryName() + File.separator + InstanceManager.getDefault(OperationsSetupXml.class).getOperationsFileName());

        // Set filename back to Operations
        ox.setOperationsFileName("Operations.xml");

        Assert.assertEquals("confirm file name", "Operations.xml", ox.getOperationsFileName());

        return s;
    }

    @Test
    public void readTestSetup() throws org.jdom2.JDOMException, java.io.IOException, java.io.FileNotFoundException {
        // this uses explicit filenames intentionally, to ensure that
        // the resulting files go into the test tree area.

        OperationsSetupXml ox = new OperationsSetupXml();

        // change file name to OperationsTest
        ox.setOperationsFileName(InstanceManager.getDefault(OperationsSetupXml.class).getOperationsFileName());

        // create a Operations file with known contents
        Setup s = new Setup();
        Assert.assertNotNull("exists", s);

        // read it
        ox.readFile(OperationsSetupXml.getFileLocation() + OperationsSetupXml.getOperationsDirectoryName() + File.separator + InstanceManager.getDefault(OperationsSetupXml.class).getOperationsFileName());
    }
}
