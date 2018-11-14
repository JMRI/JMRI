package jmri.jmrit.operations.trains;

import java.io.File;
import java.io.IOException;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Setup;
import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Xml class Last manually cross-checked on 20090131
 * <p>
 * TrainBuilder: Everything. TrainSwitchLists: Everything.
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
 */
public class XmlTest extends OperationsTestCase {

    @Test
    public void testFilePathNames() {
        // test the build report path name
        Assert.assertEquals("buildstatus", TrainManagerXml.BUILD_STATUS);
        Assert.assertEquals(OperationsXml.getFileLocation()
                + "operations"
                + File.separator
                + "JUnitTest"
                + File.separator
                + "buildstatus"
                + File.separator
                + "train (TestReportName).txt",
                InstanceManager.getDefault(TrainManagerXml.class).defaultBuildReportFileName("TestReportName"));

        // test the manifest path name
        Assert.assertEquals("manifests", TrainManagerXml.MANIFESTS);
        Assert.assertEquals(OperationsXml.getFileLocation()
                + "operations"
                + File.separator
                + "JUnitTest"
                + File.separator
                + "manifests"
                + File.separator
                + "train (TestManifestName).txt",
                InstanceManager.getDefault(TrainManagerXml.class).getDefaultManifestFileName("TestManifestName"));

        // test the manifest CSV path name
        Assert.assertEquals("csvManifests", TrainManagerXml.CSV_MANIFESTS);
        Assert.assertEquals(OperationsXml.getFileLocation()
                + "operations"
                + File.separator
                + "JUnitTest"
                + File.separator
                + "csvManifests"
                + File.separator
                + "train (TestManifestName).csv",
                InstanceManager.getDefault(TrainManagerXml.class).getDefaultCsvManifestFileName("TestManifestName"));

        // test the switch list path name
        Assert.assertEquals("switchLists", TrainManagerXml.SWITCH_LISTS);
        Assert.assertEquals(OperationsXml.getFileLocation()
                + "operations"
                + File.separator
                + "JUnitTest"
                + File.separator
                + "switchLists"
                + File.separator
                + "location (TestSwitchListName).txt",
                InstanceManager.getDefault(TrainManagerXml.class).getDefaultSwitchListName("TestSwitchListName"));

        // test the CSV switch list path name
        Assert.assertEquals("csvSwitchLists", TrainManagerXml.CSV_SWITCH_LISTS);
        Assert.assertEquals(OperationsXml.getFileLocation()
                + "operations"
                + File.separator
                + "JUnitTest"
                + File.separator
                + "csvSwitchLists"
                + File.separator
                + "location (TestSwitchListName).csv",
                InstanceManager.getDefault(TrainManagerXml.class).getDefaultCsvSwitchListFileName("TestSwitchListName"));
    }

    /**
     * Test train XML create, read, and backup support. Originally written as
     * three separate tests, now one large test as of 8/29/2013
     *
     * @throws JDOMException exception
     * @throws IOException exception
     */
    @Test
    public void testXMLCreate() throws JDOMException, IOException {

        // confirm that file name has been modified
        Assert.assertEquals("test file name", "OperationsJUnitTestTrainRoster.xml", InstanceManager.getDefault(TrainManagerXml.class)
                .getOperationsFileName());

        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        Route A = rmanager.newRoute("A");
        Route B = rmanager.newRoute("B");
        Route C = rmanager.newRoute("C");

        Assert.assertEquals("no locations in route A", 0, A.getLocationsBySequenceList().size());
        Assert.assertEquals("no locations in route B", 0, B.getLocationsBySequenceList().size());
        Assert.assertEquals("no locations in route C", 0, C.getLocationsBySequenceList().size());

        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location Arlington = lmanager.newLocation("Arlington");
        Location Westford = lmanager.newLocation("Westford");
        Location Bedford = lmanager.newLocation("Bedford");

        RouteLocation startA = A.addLocation(Westford);
        startA.setTrainIconX(125); // set the train icon coordinates
        startA.setTrainIconY(175);
        RouteLocation startB = B.addLocation(Arlington);
        startB.setTrainIconX(175); // set the train icon coordinates
        startB.setTrainIconY(175);
        RouteLocation startC = C.addLocation(Bedford);
        startC.setTrainIconX(25); // set the train icon coordinates
        startC.setTrainIconY(200);

        RouteLocation midC = C.addLocation(Arlington);
        RouteLocation endC = C.addLocation(Westford);

        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        List<Train> temptrainList = manager.getTrainsByIdList();

        Assert.assertEquals("Starting Number of Trains", 0, temptrainList.size());
        Train t1 = manager.newTrain("Test Number 1");
        Train t2 = manager.newTrain("Test Number 2");
        Train t3 = manager.newTrain("Test Number 3");

        temptrainList = manager.getTrainsByIdList();
        Assert.assertEquals("New Number of Trains", 3, temptrainList.size());

        EngineManager eManager = InstanceManager.getDefault(EngineManager.class);
        Engine e1 = eManager.newRS("UP", "1");
        Engine e2 = eManager.newRS("UP", "2");
        Engine e3 = eManager.newRS("UP", "3");

        // save in backup file
        t3.setBuildEnabled(true);
        t3.setBuildFailed(false);
        t3.setBuildTrainNormalEnabled(false);
        t3.setBuilt(true);
        t3.setBuiltEndYear("1950");
        t3.setBuiltStartYear("1925");
        t3.setCabooseRoad("t3 X caboose road");
        t3.setComment("t3 X comment");
        t3.setDescription("t3 X description");
        t3.setEngineModel("t3 X engine model");
        t3.setEngineRoad("t3 X engine road");
        t3.setLeadEngine(e1);
        t3.setLoadOption("t3 X load option");
        t3.setManifestLogoPathName("t3 X pathName");
        t3.setNumberEngines("7");
        t3.setOwnerOption("t3 X owner option");
        t3.setRailroadName("t3 X railroad name");
        t3.setRequirements(Train.CABOOSE);
        t3.setRoadOption("t3 X raod option");
        t3.setRoute(B);
        t3.setStatusCode(Train.CODE_UNKNOWN);

        InstanceManager.getDefault(TrainManagerXml.class).writeOperationsFile();

        // Add some more trains and write file again
        // so we can test the backup facility
        Train t4 = manager.newTrain("Test Number 4");
        Train t5 = manager.newTrain("Test Number 5");
        Train t6 = manager.newTrain("Test Number 6");

        Assert.assertNotNull("train 1", t1);
        Assert.assertNotNull("train 2", t2);
        Assert.assertNotNull("train 3", t3);
        Assert.assertNotNull("train 4", t4);
        Assert.assertNotNull("train 5", t5);
        Assert.assertNotNull("train 6", t6);

        t1.setBuildEnabled(true);
        t1.setBuildFailed(true);
        t1.setBuildTrainNormalEnabled(true);
        t1.setBuilt(false);
        t1.setBuiltEndYear("1956");
        t1.setBuiltStartYear("1932");
        t1.setCabooseRoad("t1 caboose road");
        t1.setComment("t1 comment");
        t1.setCurrentLocation(startC);
        t1.setDepartureTime("1", "35");
        t1.setDescription("t1 description");
        t1.setEngineModel("t1 engine model");
        t1.setEngineRoad("t1 engine road");
        t1.setLeadEngine(e1);
        t1.setLoadOption("t1 load option");
        t1.setManifestLogoPathName("t1 pathName");
        t1.setNumberEngines("1");
        t1.setOwnerOption("t1 owner option");
        t1.setRailroadName("t1 railroad name");
        t1.setRequirements(Train.NO_CABOOSE_OR_FRED);
        t1.setRoadOption("t1 raod option");
        t1.setRoute(C);
        t1.setSecondLegCabooseRoad("t1 second leg caboose road");
        t1.setSecondLegEndLocation(midC);
        t1.setSecondLegEngineModel("t1 second leg engine model");
        t1.setSecondLegEngineRoad("t1 second leg engine road");
        t1.setSecondLegNumberEngines("5");
        t1.setSecondLegOptions(Train.ADD_CABOOSE);
        t1.setSecondLegStartLocation(endC);
        t1.setSendCarsToTerminalEnabled(true);
        t1.setStatusCode(Train.CODE_UNKNOWN);
        t1.setSwitchListStatus(Train.PRINTED);
        t1.setThirdLegCabooseRoad("t1 third leg caboose road");
        t1.setThirdLegEndLocation(startC);
        t1.setThirdLegEngineModel("t1 third leg engine model");
        t1.setThirdLegEngineRoad("t1 third leg engine road");
        t1.setThirdLegNumberEngines("3");
        t1.setThirdLegOptions(Train.HELPER_ENGINES);
        t1.setThirdLegStartLocation(midC);
        t1.addTrainSkipsLocation(midC.getId());

        t3.setBuildEnabled(false);
        t3.setBuildFailed(true);
        t3.setBuildTrainNormalEnabled(false);
        t3.setBuilt(false);
        t3.setBuiltEndYear("1955");
        t3.setBuiltStartYear("1931");
        t3.setCabooseRoad("t3 caboose road");
        t3.setComment("t3 comment");
        t3.setCurrentLocation(startA);
        t3.setDepartureTime("4", "55");
        t3.setDescription("t3 description");
        t3.setEngineModel("t3 engine model");
        t3.setEngineRoad("t3 engine road");
        t3.setLeadEngine(e2);
        t3.setLoadOption("t3 load option");
        t3.setManifestLogoPathName("t3 pathName");
        t3.setNumberEngines("1");
        t3.setOwnerOption("t3 owner option");
        t3.setRailroadName("t3 railroad name");
        t3.setRequirements(Train.NO_CABOOSE_OR_FRED);
        t3.setRoadOption("t3 raod option");
        t3.setRoute(A);
        t3.setStatusCode(Train.CODE_UNKNOWN);

        t5.setBuildEnabled(true);
        t5.setBuildFailed(false);
        t5.setBuildTrainNormalEnabled(false);
        t5.setBuilt(true);
        t5.setBuiltEndYear("1954");
        t5.setBuiltStartYear("1930");
        t5.setCabooseRoad("t5 caboose road");
        t5.setComment("t5 comment");
        t5.setCurrentLocation(startB);
        t5.setDepartureTime("23", "15");
        t5.setDescription("t5 description");
        t5.setEngineModel("t5 engine model");
        t5.setEngineRoad("t5 engine road");
        t5.setLeadEngine(e3);
        t5.setLoadOption("t5 load option");
        t5.setManifestLogoPathName("t5 pathName");
        t5.setNumberEngines("1");
        t5.setOwnerOption("t5 owner option");
        t5.setRailroadName("t5 railroad name");
        t5.setRequirements(Train.NO_CABOOSE_OR_FRED);
        t5.setRoadOption("t5 raod option");
        t5.setRoute(B);
        t5.setStatusCode(Train.CODE_UNKNOWN);

        InstanceManager.getDefault(TrainManagerXml.class).writeOperationsFile();

        // perform data check before dispose
        Assert.assertEquals("t1 build", true, t1.isBuildEnabled());
        Assert.assertEquals("t1 build failed", true, t1.getBuildFailed());
        Assert.assertEquals("t1 build normal", true, t1.isBuildTrainNormalEnabled());
        Assert.assertEquals("t1 built", false, t1.isBuilt());
        Assert.assertEquals("t1 built end year", "1956", t1.getBuiltEndYear());
        Assert.assertEquals("t1 built start year", "1932", t1.getBuiltStartYear());
        Assert.assertEquals("t1 caboose roadr", "t1 caboose road", t1.getCabooseRoad());
        Assert.assertEquals("t1 comment", "t1 comment", t1.getComment());
        Assert.assertEquals("t1 current location name", "Bedford", t1.getCurrentLocationName());
        Assert.assertEquals("t1 departure hour", "01", t1.getDepartureTimeHour());
        Assert.assertEquals("t1 departure minute", "35", t1.getDepartureTimeMinute());
        Assert.assertEquals("t1 engine model", "t1 engine model", t1.getEngineModel());
        Assert.assertEquals("t1 engine road", "t1 engine road", t1.getEngineRoad());
        Assert.assertNotNull("t1 has a lead engine", t1.getLeadEngine());
        Assert.assertEquals("t1 lead engine number", "1", t1.getLeadEngine().getNumber());
        Assert.assertEquals("t1 load option", "t1 load option", t1.getLoadOption());
        Assert.assertEquals("t1 path name", "t1 pathName", t1.getManifestLogoPathName());
        Assert.assertEquals("t1 number of engines", "1", t1.getNumberEngines());
        Assert.assertEquals("t1 Owner option", "t1 owner option", t1.getOwnerOption());
        Assert.assertEquals("t1 railroad name", "t1 railroad name", t1.getRailroadName());
        Assert.assertEquals("t1 requirements", Train.NO_CABOOSE_OR_FRED, t1.getRequirements());
        Assert.assertEquals("t1 road option", "t1 raod option", t1.getRoadOption());
        Assert.assertEquals("t1 route", C, t1.getRoute());
        Assert.assertEquals("t1 second leg caboose road", "t1 second leg caboose road", t1
                .getSecondLegCabooseRoad());
        Assert.assertEquals("t1 second leg end location", midC, t1.getSecondLegEndLocation());
        Assert.assertEquals("t1 second leg engine model", "t1 second leg engine model", t1
                .getSecondLegEngineModel());
        Assert.assertEquals("t1 second leg engine road", "t1 second leg engine road", t1
                .getSecondLegEngineRoad());
        Assert.assertEquals("t1 second leg number of engines", "5", t1.getSecondLegNumberEngines());
        Assert.assertEquals("t1 second leg options", Train.ADD_CABOOSE, t1.getSecondLegOptions());
        Assert.assertEquals("t1 second leg start location", endC, t1.getSecondLegStartLocation());
        Assert.assertEquals("t1 send cars to terminal", true, t1.isSendCarsToTerminalEnabled());
        Assert.assertEquals("t1 status", Train.UNKNOWN, t1.getStatus());
        Assert.assertEquals("t1 switch list status", Train.PRINTED, t1.getSwitchListStatus());
        Assert.assertEquals("t1 third leg caboose road", "t1 third leg caboose road", t1
                .getThirdLegCabooseRoad());
        Assert.assertEquals("t1 third leg end location", startC, t1.getThirdLegEndLocation());
        Assert.assertEquals("t1 third leg engine model", "t1 third leg engine model", t1
                .getThirdLegEngineModel());
        Assert.assertEquals("t1 third leg engine road", "t1 third leg engine road", t1
                .getThirdLegEngineRoad());
        Assert.assertEquals("t1 third leg number of engines", "3", t1.getThirdLegNumberEngines());
        Assert.assertEquals("t1 third leg options", Train.HELPER_ENGINES, t1.getThirdLegOptions());
        Assert.assertEquals("t1 third leg start location", midC, t1.getThirdLegStartLocation());
        Assert.assertEquals("t1 skips location", false, t1.skipsLocation(startC.getId()));
        Assert.assertEquals("t1 skips location", true, t1.skipsLocation(midC.getId()));
        Assert.assertEquals("t1 skips location", false, t1.skipsLocation(endC.getId()));

        Assert.assertEquals("t3 build", false, t3.isBuildEnabled());
        Assert.assertEquals("t3 build failed", true, t3.getBuildFailed());
        Assert.assertEquals("t3 build normal", false, t3.isBuildTrainNormalEnabled());
        Assert.assertEquals("t3 built", false, t3.isBuilt());
        Assert.assertEquals("t3 built end year", "1955", t3.getBuiltEndYear());
        Assert.assertEquals("t3 built start year", "1931", t3.getBuiltStartYear());
        Assert.assertEquals("t3 caboose roadr", "t3 caboose road", t3.getCabooseRoad());
        Assert.assertEquals("t3 comment", "t3 comment", t3.getComment());
        Assert.assertEquals("t3 current location name", "Westford", t3.getCurrentLocationName());
        Assert.assertEquals("t3 departure hour", "04", t3.getDepartureTimeHour());
        Assert.assertEquals("t3 departure minute", "55", t3.getDepartureTimeMinute());
        Assert.assertEquals("t3 engine model", "t3 engine model", t3.getEngineModel());
        Assert.assertEquals("t3 engine road", "t3 engine road", t3.getEngineRoad());
        Assert.assertEquals("t3 lead engine number", "2", t3.getLeadEngine().getNumber());
        Assert.assertEquals("t3 load option", "t3 load option", t3.getLoadOption());
        Assert.assertEquals("t3 path name", "t3 pathName", t3.getManifestLogoPathName());
        Assert.assertEquals("t3 number of engines", "1", t3.getNumberEngines());
        Assert.assertEquals("t3 Owner option", "t3 owner option", t3.getOwnerOption());
        Assert.assertEquals("t3 railroad name", "t3 railroad name", t3.getRailroadName());
        Assert.assertEquals("t3 requirements", Train.NO_CABOOSE_OR_FRED, t3.getRequirements());
        Assert.assertEquals("t3 road option", "t3 raod option", t3.getRoadOption());
        Assert.assertEquals("t3 route", A, t3.getRoute());
        // test second leg defaults
        Assert.assertEquals("t3 second leg caboose road", "", t3.getSecondLegCabooseRoad());
        Assert.assertEquals("t3 second leg end location", null, t3.getSecondLegEndLocation());
        Assert.assertEquals("t3 second leg engine model", "", t3.getSecondLegEngineModel());
        Assert.assertEquals("t3 second leg engine road", "", t3.getSecondLegEngineRoad());
        Assert.assertEquals("t3 second leg number of engines", "0", t3.getSecondLegNumberEngines());
        Assert.assertEquals("t3 second leg options", Train.NO_CABOOSE_OR_FRED, t3.getSecondLegOptions());
        Assert.assertEquals("t3 second leg start location", null, t3.getSecondLegStartLocation());
        Assert.assertEquals("t3 send cars to terminal", false, t3.isSendCarsToTerminalEnabled());
        Assert.assertEquals("t3 status", Train.UNKNOWN, t3.getStatus());
        Assert.assertEquals("t3 switch list status", Train.UNKNOWN, t3.getSwitchListStatus());
        // test third leg defaults
        Assert.assertEquals("t3 third leg caboose road", "", t3.getThirdLegCabooseRoad());
        Assert.assertEquals("t3 third leg end location", null, t3.getThirdLegEndLocation());
        Assert.assertEquals("t3 third leg engine model", "", t3.getThirdLegEngineModel());
        Assert.assertEquals("t3 third leg engine road", "", t3.getThirdLegEngineRoad());
        Assert.assertEquals("t3 third leg number of engines", "0", t3.getThirdLegNumberEngines());
        Assert.assertEquals("t3 third leg options", Train.NO_CABOOSE_OR_FRED, t3.getThirdLegOptions());
        Assert.assertEquals("t3 third leg start location", null, t3.getThirdLegStartLocation());

        Assert.assertEquals("t5 build", true, t5.isBuildEnabled());
        Assert.assertEquals("t5 build failed", false, t5.getBuildFailed());
        Assert.assertEquals("t5 built", true, t5.isBuilt());
        Assert.assertEquals("t5 built end year", "1954", t5.getBuiltEndYear());
        Assert.assertEquals("t5 built start year", "1930", t5.getBuiltStartYear());
        Assert.assertEquals("t5 caboose roadr", "t5 caboose road", t5.getCabooseRoad());
        Assert.assertEquals("t5 comment", "t5 comment", t5.getComment());
        Assert.assertEquals("t5 current location name", "Arlington", t5.getCurrentLocationName());
        Assert.assertEquals("t5 departure hour", "23", t5.getDepartureTimeHour());
        Assert.assertEquals("t5 departure minute", "15", t5.getDepartureTimeMinute());
        Assert.assertEquals("t5 engine model", "t5 engine model", t5.getEngineModel());
        Assert.assertEquals("t5 engine road", "t5 engine road", t5.getEngineRoad());
        Assert.assertEquals("t5 lead engine number", "3", t5.getLeadEngine().getNumber());
        Assert.assertEquals("t5 load option", "t5 load option", t5.getLoadOption());
        Assert.assertEquals("t5 path name", "t5 pathName", t5.getManifestLogoPathName());
        Assert.assertEquals("t5 number of engines", "1", t5.getNumberEngines());
        Assert.assertEquals("t5 Owner option", "t5 owner option", t5.getOwnerOption());
        Assert.assertEquals("t5 railroad name", "t5 railroad name", t5.getRailroadName());
        Assert.assertEquals("t5 requirements", Train.NO_CABOOSE_OR_FRED, t5.getRequirements());
        Assert.assertEquals("t5 road option", "t5 raod option", t5.getRoadOption());
        Assert.assertEquals("t5 route", B, t5.getRoute());
        Assert.assertEquals("t5 status", Train.UNKNOWN, t5.getStatus());

        // clear all trains
        manager.dispose();

        // prevent swing access when loading train icon
        Setup.setPanelName("");

        manager = InstanceManager.getDefault(TrainManager.class);
        temptrainList = manager.getTrainsByIdList();

        Assert.assertEquals("Starting Number of Trains", 0, temptrainList.size());

        // confirm that none of the trains exist
        t1 = manager.getTrainByName("Test Number 1");
        t2 = manager.getTrainByName("Test Number 2");
        t3 = manager.getTrainByName("Test Number 3");
        t4 = manager.getTrainByName("Test Number 4");
        t5 = manager.getTrainByName("Test Number 5");
        t6 = manager.getTrainByName("Test Number 6");

        Assert.assertNull("train 1", t1);
        Assert.assertNull("train 2", t2);
        Assert.assertNull("train 3", t3);
        Assert.assertNull("train 4", t4);
        Assert.assertNull("train 5", t5);
        Assert.assertNull("train 6", t6);

        // now reload train data from file
        InstanceManager.getDefault(TrainManagerXml.class).readFile(InstanceManager.getDefault(TrainManagerXml.class).getDefaultOperationsFilename());

        temptrainList = manager.getTrainsByIdList();

        Assert.assertEquals("Number of Trains", 6, temptrainList.size());

        t1 = manager.getTrainByName("Test Number 1");
        t2 = manager.getTrainByName("Test Number 2");
        t3 = manager.getTrainByName("Test Number 3");
        t4 = manager.getTrainByName("Test Number 4");
        t5 = manager.getTrainByName("Test Number 5");
        t6 = manager.getTrainByName("Test Number 6");

        Assert.assertNotNull("train 1", t1);
        Assert.assertNotNull("train 2", t2);
        Assert.assertNotNull("train 3", t3);
        Assert.assertNotNull("train 4", t4);
        Assert.assertNotNull("train 5", t5);
        Assert.assertNotNull("train 6", t6);

        Assert.assertEquals("t1 build", true, t1.isBuildEnabled());
        Assert.assertEquals("t1 build failed", true, t1.getBuildFailed());
        Assert.assertEquals("t1 build normal", true, t1.isBuildTrainNormalEnabled());
        Assert.assertEquals("t1 built", false, t1.isBuilt());
        Assert.assertEquals("t1 built end year", "1956", t1.getBuiltEndYear());
        Assert.assertEquals("t1 built start year", "1932", t1.getBuiltStartYear());
        Assert.assertEquals("t1 caboose roadr", "t1 caboose road", t1.getCabooseRoad());
        Assert.assertEquals("t1 comment", "t1 comment", t1.getComment());
        Assert.assertEquals("t1 current location name", "Bedford", t1.getCurrentLocationName());
        Assert.assertEquals("t1 departure hour", "01", t1.getDepartureTimeHour());
        Assert.assertEquals("t1 departure minute", "35", t1.getDepartureTimeMinute());
        Assert.assertEquals("t1 engine model", "t1 engine model", t1.getEngineModel());
        Assert.assertEquals("t1 engine road", "t1 engine road", t1.getEngineRoad());
        Assert.assertNotNull("t1 has a lead engine", t1.getLeadEngine());
        Assert.assertEquals("t1 lead engine number", "1", t1.getLeadEngine().getNumber());
        Assert.assertEquals("t1 load option", "t1 load option", t1.getLoadOption());
        Assert.assertEquals("t1 path name", "t1 pathName", t1.getManifestLogoPathName());
        Assert.assertEquals("t1 number of engines", "1", t1.getNumberEngines());
        Assert.assertEquals("t1 Owner option", "t1 owner option", t1.getOwnerOption());
        Assert.assertEquals("t1 railroad name", "t1 railroad name", t1.getRailroadName());
        Assert.assertEquals("t1 requirements", Train.NO_CABOOSE_OR_FRED, t1.getRequirements());
        Assert.assertEquals("t1 road option", "t1 raod option", t1.getRoadOption());
        Assert.assertEquals("t1 route", C, t1.getRoute());
        Assert.assertEquals("t1 second leg caboose road", "t1 second leg caboose road", t1
                .getSecondLegCabooseRoad());
        Assert.assertEquals("t1 second leg end location", midC, t1.getSecondLegEndLocation());
        Assert.assertEquals("t1 second leg engine model", "t1 second leg engine model", t1
                .getSecondLegEngineModel());
        Assert.assertEquals("t1 second leg engine road", "t1 second leg engine road", t1
                .getSecondLegEngineRoad());
        Assert.assertEquals("t1 second leg number of engines", "5", t1.getSecondLegNumberEngines());
        Assert.assertEquals("t1 second leg options", Train.ADD_CABOOSE, t1.getSecondLegOptions());
        Assert.assertEquals("t1 second leg start location", endC, t1.getSecondLegStartLocation());
        Assert.assertEquals("t1 send cars to terminal", true, t1.isSendCarsToTerminalEnabled());
        Assert.assertEquals("t1 status", Train.UNKNOWN, t1.getStatus());
        Assert.assertEquals("t1 switch list status", Train.PRINTED, t1.getSwitchListStatus());
        Assert.assertEquals("t1 third leg caboose road", "t1 third leg caboose road", t1
                .getThirdLegCabooseRoad());
        Assert.assertEquals("t1 third leg end location", startC, t1.getThirdLegEndLocation());
        Assert.assertEquals("t1 third leg engine model", "t1 third leg engine model", t1
                .getThirdLegEngineModel());
        Assert.assertEquals("t1 third leg engine road", "t1 third leg engine road", t1
                .getThirdLegEngineRoad());
        Assert.assertEquals("t1 third leg number of engines", "3", t1.getThirdLegNumberEngines());
        Assert.assertEquals("t1 third leg options", Train.HELPER_ENGINES, t1.getThirdLegOptions());
        Assert.assertEquals("t1 third leg start location", midC, t1.getThirdLegStartLocation());
        Assert.assertEquals("t1 skips location", false, t1.skipsLocation(startC.getId()));
        Assert.assertEquals("t1 skips location", true, t1.skipsLocation(midC.getId()));
        Assert.assertEquals("t1 skips location", false, t1.skipsLocation(endC.getId()));

        Assert.assertEquals("t3 build", false, t3.isBuildEnabled());
        Assert.assertEquals("t3 build failed", true, t3.getBuildFailed());
        Assert.assertEquals("t3 build normal", false, t3.isBuildTrainNormalEnabled());
        Assert.assertEquals("t3 built", false, t3.isBuilt());
        Assert.assertEquals("t3 built end year", "1955", t3.getBuiltEndYear());
        Assert.assertEquals("t3 built start year", "1931", t3.getBuiltStartYear());
        Assert.assertEquals("t3 caboose roadr", "t3 caboose road", t3.getCabooseRoad());
        Assert.assertEquals("t3 comment", "t3 comment", t3.getComment());
        Assert.assertEquals("t3 current location name", "Westford", t3.getCurrentLocationName());
        Assert.assertEquals("t3 departure hour", "04", t3.getDepartureTimeHour());
        Assert.assertEquals("t3 departure minute", "55", t3.getDepartureTimeMinute());
        Assert.assertEquals("t3 engine model", "t3 engine model", t3.getEngineModel());
        Assert.assertEquals("t3 engine road", "t3 engine road", t3.getEngineRoad());
        Assert.assertEquals("t3 lead engine number", "2", t3.getLeadEngine().getNumber());
        Assert.assertEquals("t3 load option", "t3 load option", t3.getLoadOption());
        Assert.assertEquals("t3 path name", "t3 pathName", t3.getManifestLogoPathName());
        Assert.assertEquals("t3 number of engines", "1", t3.getNumberEngines());
        Assert.assertEquals("t3 Owner option", "t3 owner option", t3.getOwnerOption());
        Assert.assertEquals("t3 railroad name", "t3 railroad name", t3.getRailroadName());
        Assert.assertEquals("t3 requirements", Train.NO_CABOOSE_OR_FRED, t3.getRequirements());
        Assert.assertEquals("t3 road option", "t3 raod option", t3.getRoadOption());
        Assert.assertEquals("t3 route", A, t3.getRoute());
        // test second leg defaults
        Assert.assertEquals("t3 second leg caboose road", "", t3.getSecondLegCabooseRoad());
        Assert.assertEquals("t3 second leg end location", null, t3.getSecondLegEndLocation());
        Assert.assertEquals("t3 second leg engine model", "", t3.getSecondLegEngineModel());
        Assert.assertEquals("t3 second leg engine road", "", t3.getSecondLegEngineRoad());
        Assert.assertEquals("t3 second leg number of engines", "0", t3.getSecondLegNumberEngines());
        Assert.assertEquals("t3 second leg options", Train.NO_CABOOSE_OR_FRED, t3.getSecondLegOptions());
        Assert.assertEquals("t3 second leg start location", null, t3.getSecondLegStartLocation());
        Assert.assertEquals("t3 send cars to terminal", false, t3.isSendCarsToTerminalEnabled());
        Assert.assertEquals("t3 status", Train.UNKNOWN, t3.getStatus());
        Assert.assertEquals("t3 switch list status", Train.UNKNOWN, t3.getSwitchListStatus());
        // test third leg defaults
        Assert.assertEquals("t3 third leg caboose road", "", t3.getThirdLegCabooseRoad());
        Assert.assertEquals("t3 third leg end location", null, t3.getThirdLegEndLocation());
        Assert.assertEquals("t3 third leg engine model", "", t3.getThirdLegEngineModel());
        Assert.assertEquals("t3 third leg engine road", "", t3.getThirdLegEngineRoad());
        Assert.assertEquals("t3 third leg number of engines", "0", t3.getThirdLegNumberEngines());
        Assert.assertEquals("t3 third leg options", Train.NO_CABOOSE_OR_FRED, t3.getThirdLegOptions());
        Assert.assertEquals("t3 third leg start location", null, t3.getThirdLegStartLocation());

        Assert.assertEquals("t5 build", true, t5.isBuildEnabled());
        Assert.assertEquals("t5 build failed", false, t5.getBuildFailed());
        Assert.assertEquals("t5 built", true, t5.isBuilt());
        Assert.assertEquals("t5 built end year", "1954", t5.getBuiltEndYear());
        Assert.assertEquals("t5 built start year", "1930", t5.getBuiltStartYear());
        Assert.assertEquals("t5 caboose roadr", "t5 caboose road", t5.getCabooseRoad());
        Assert.assertEquals("t5 comment", "t5 comment", t5.getComment());
        Assert.assertEquals("t5 current location name", "Arlington", t5.getCurrentLocationName());
        Assert.assertEquals("t5 departure hour", "23", t5.getDepartureTimeHour());
        Assert.assertEquals("t5 departure minute", "15", t5.getDepartureTimeMinute());
        Assert.assertEquals("t5 engine model", "t5 engine model", t5.getEngineModel());
        Assert.assertEquals("t5 engine road", "t5 engine road", t5.getEngineRoad());
        Assert.assertEquals("t5 lead engine number", "3", t5.getLeadEngine().getNumber());
        Assert.assertEquals("t5 load option", "t5 load option", t5.getLoadOption());
        Assert.assertEquals("t5 path name", "t5 pathName", t5.getManifestLogoPathName());
        Assert.assertEquals("t5 number of engines", "1", t5.getNumberEngines());
        Assert.assertEquals("t5 Owner option", "t5 owner option", t5.getOwnerOption());
        Assert.assertEquals("t5 railroad name", "t5 railroad name", t5.getRailroadName());
        Assert.assertEquals("t5 requirements", Train.NO_CABOOSE_OR_FRED, t5.getRequirements());
        Assert.assertEquals("t5 road option", "t5 raod option", t5.getRoadOption());
        Assert.assertEquals("t5 route", B, t5.getRoute());
        Assert.assertEquals("t5 status", Train.UNKNOWN, t5.getStatus());

        // now test the train backup file
        manager.dispose();
        manager = InstanceManager.getDefault(TrainManager.class);
        temptrainList = manager.getTrainsByIdList();

        Assert.assertEquals("Starting Number of Trains", 0, temptrainList.size());

        // set file name to backup
        InstanceManager.getDefault(TrainManagerXml.class).setOperationsFileName("OperationsJUnitTestTrainRoster.xml.bak");

        InstanceManager.getDefault(TrainManagerXml.class).readFile(InstanceManager.getDefault(TrainManagerXml.class).getDefaultOperationsFilename());

        // restore file name
        InstanceManager.getDefault(TrainManagerXml.class).setOperationsFileName("OperationsJUnitTestTrainRoster.xml");

        temptrainList = manager.getTrainsByIdList();

        Assert.assertEquals("Number of Trains", 3, temptrainList.size());

        t1 = manager.getTrainByName("Test Number 1");
        t2 = manager.getTrainByName("Test Number 2");
        t3 = manager.getTrainByName("Test Number 3");
        t4 = manager.getTrainByName("Test Number 4");
        t5 = manager.getTrainByName("Test Number 5");
        t6 = manager.getTrainByName("Test Number 6");

        Assert.assertNotNull("train 1", t1);
        Assert.assertNotNull("train 2", t2);
        Assert.assertNotNull("train 3", t3);
        Assert.assertNull("train 4", t4);
        Assert.assertNull("train 5", t5);
        Assert.assertNull("train 6", t6);

        Assert.assertEquals("t3 build", true, t3.isBuildEnabled());
        Assert.assertEquals("t3 build failed", false, t3.getBuildFailed());
        Assert.assertEquals("t3 built", true, t3.isBuilt());
        Assert.assertEquals("t3 built end year", "1950", t3.getBuiltEndYear());
        Assert.assertEquals("t3 built start year", "1925", t3.getBuiltStartYear());
        Assert.assertEquals("t3 caboose roadr", "t3 X caboose road", t3.getCabooseRoad());
        Assert.assertEquals("t3 comment", "t3 X comment", t3.getComment());
        Assert.assertEquals("t3 engine model", "t3 X engine model", t3.getEngineModel());
        Assert.assertEquals("t3 engine road", "t3 X engine road", t3.getEngineRoad());
        Assert.assertEquals("t3 load option", "t3 X load option", t3.getLoadOption());
        Assert.assertEquals("t3 path name", "t3 X pathName", t3.getManifestLogoPathName());
        Assert.assertEquals("t3 number of engines", "7", t3.getNumberEngines());
        Assert.assertEquals("t3 Owner option", "t3 X owner option", t3.getOwnerOption());
        Assert.assertEquals("t3 railroad name", "t3 X railroad name", t3.getRailroadName());
        Assert.assertEquals("t3 requirements", Train.CABOOSE, t3.getRequirements());
        Assert.assertEquals("t3 raod option", "t3 X raod option", t3.getRoadOption());
        Assert.assertEquals("t3 status", Train.UNKNOWN, t3.getStatus());
    }

    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();

        Setup.setBuildAggressive(false);
        Setup.setTrainIntoStagingCheckEnabled(true);
        Setup.setMaxTrainLength(1000);
        Setup.setRouterBuildReportLevel(Setup.BUILD_REPORT_VERY_DETAILED);
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

}
