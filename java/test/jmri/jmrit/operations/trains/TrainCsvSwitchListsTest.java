package jmri.jmrit.operations.trains;

import java.io.BufferedReader;
import java.io.File;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.util.JUnitOperationsUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainCsvSwitchListsTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        TrainCsvSwitchLists t = new TrainCsvSwitchLists();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCreateCsvSwtichList() {
        TrainCsvSwitchLists tcs = new TrainCsvSwitchLists();
        Assert.assertNotNull("exists",tcs);
        
        JUnitOperationsUtil.initOperationsData();
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        
        // test two switch lists, departure and terminate locations
        Location depart_location = lmanager.getLocationByName("North End Staging");
        Assert.assertNotNull(depart_location);
        Location terminate_location = lmanager.getLocationByName("South End Staging");
        Assert.assertNotNull(terminate_location);
        
        // create some work
        Train train1 = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        Assert.assertTrue(train1.build());
                       
        File file = tcs.buildSwitchList(depart_location);
        Assert.assertTrue(file.exists());
        
        file = tcs.buildSwitchList(terminate_location);
        Assert.assertTrue(file.exists());
        
        File switchListFileDepart = InstanceManager.getDefault(TrainManagerXml.class).getCsvSwitchListFile(depart_location.getName());
        Assert.assertTrue(switchListFileDepart.exists());

        BufferedReader in = JUnitOperationsUtil.getBufferedReader(switchListFileDepart);
        Assert.assertEquals("confirm number of lines in switch list", 26, in.lines().count());
        
        File switchListFileTerminate = InstanceManager.getDefault(TrainManagerXml.class).getCsvSwitchListFile(terminate_location.getName());
        Assert.assertTrue(switchListFileTerminate.exists());

        in = JUnitOperationsUtil.getBufferedReader(switchListFileTerminate);
        Assert.assertEquals("confirm number of lines in switch list", 22, in.lines().count());
        
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    /**
     * Creates a switch list for a train that visits a location twice
     */
    @Test
    public void testSwitchListTrainTurn() {

        loadLocationsEnginesAndCars();

        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.newTrain("Test switchlists train");
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        Route route = rmanager.newRoute("Test switchlists route");
        
        // disable build messages
        tmanager.setBuildMessagesEnabled(false);
        // disable build reports
        tmanager.setBuildReportEnabled(false);

        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location locationA = lmanager.getLocationByName("Test Location A");
        Location locationB = lmanager.newLocation("Test Location B");
        Track yardB = locationB.addTrack("Yard at B", Track.YARD);
        yardB.setLength(400);

        Location locationC = lmanager.newLocation("Test Location C");
        Track yardC = locationC.addTrack("Yard at C", Track.YARD);
        yardC.setLength(80); // only enough room for one car

        Location locationD = lmanager.newLocation("Test Location D");

        // create the turn
        route.addLocation(locationA);
        route.addLocation(locationB);
        RouteLocation rlc = route.addLocation(locationC);
        route.addLocation(locationD); // no tracks at D, so no work
        RouteLocation rld = route.addLocation(locationD); // change direction
        route.addLocation(locationC);
        route.addLocation(locationB); // no work at B at this time
        route.addLocation(locationA);

        rlc.setComment("Only one drop at location C");

        // confirm that the default train direction was north
        Assert.assertEquals("default train direction", RouteLocation.NORTH, rlc.getTrainDirection());
        rld.setTrainDirection(RouteLocation.SOUTH);

        train.setRoute(route);

        train.setNumberEngines("1"); // request an engine

        // place one car at location B for pick up
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        Car c6 = cmanager.getByRoadAndNumber("AA", "6");
        Assert.assertEquals("Place car on track", Track.OKAY, c6.setLocation(locationB, yardB));

        Assert.assertTrue(train.build());

        TrainCsvSwitchLists tsl = new TrainCsvSwitchLists();
        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB);
        tsl.buildSwitchList(locationC);
        // improve test coverage, switch list for a location without work
        tsl.buildSwitchList(locationD);

        File switchListFileA = InstanceManager.getDefault(TrainManagerXml.class).getCsvSwitchListFile(locationA.getName());
        Assert.assertTrue(switchListFileA.exists());
        File switchListFileB = InstanceManager.getDefault(TrainManagerXml.class).getCsvSwitchListFile(locationB.getName());
        Assert.assertTrue(switchListFileB.exists());
        File switchListFileC = InstanceManager.getDefault(TrainManagerXml.class).getCsvSwitchListFile(locationC.getName());
        Assert.assertTrue(switchListFileC.exists());
        File switchListFileD = InstanceManager.getDefault(TrainManagerXml.class).getCsvSwitchListFile(locationD.getName());
        Assert.assertTrue(switchListFileD.exists());

        BufferedReader inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 29, inA.lines().count());
        BufferedReader inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 28, inB.lines().count());
        BufferedReader inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 26, inC.lines().count());
        BufferedReader inD = JUnitOperationsUtil.getBufferedReader(switchListFileD);
        Assert.assertEquals("confirm number of lines in switch list", 23, inD.lines().count());

        train.move(); // move train to B

        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB);
        tsl.buildSwitchList(locationC);
        tsl.buildSwitchList(locationD);

        inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 23, inA.lines().count());
        inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 26, inB.lines().count());
        inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 24, inC.lines().count());
        inD = JUnitOperationsUtil.getBufferedReader(switchListFileD);
        Assert.assertEquals("confirm number of lines in switch list", 21, inD.lines().count());

        train.move(rlc); // move train to C
        Assert.assertEquals("current train location", "Test Location C", train.getCurrentLocation().getName());

        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB); // should report that already serviced train
        tsl.buildSwitchList(locationC);
        tsl.buildSwitchList(locationD);

        inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 23, inA.lines().count());
        inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 27, inB.lines().count());
        inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 24, inC.lines().count());
        inD = JUnitOperationsUtil.getBufferedReader(switchListFileD);
        Assert.assertEquals("confirm number of lines in switch list", 21, inD.lines().count());

        train.move(); // move train to D
        Assert.assertEquals("current train location", "Test Location D", train.getCurrentLocation().getName());
        train.move(); // move train to D reverse direction
        Assert.assertEquals("current train location", "Test Location D", train.getCurrentLocation().getName());

        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB);
        tsl.buildSwitchList(locationC);
        tsl.buildSwitchList(locationD);

        inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 23, inA.lines().count());
        inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 27, inB.lines().count());
        inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 23, inC.lines().count());
        inD = JUnitOperationsUtil.getBufferedReader(switchListFileD);
        Assert.assertEquals("confirm number of lines in switch list", 19, inD.lines().count());

        train.move(); // move train to C
        Assert.assertEquals("current train location", "Test Location C", train.getCurrentLocation().getName());

        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB);
        tsl.buildSwitchList(locationC);
        tsl.buildSwitchList(locationD);

        inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 23, inA.lines().count());
        inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 27, inB.lines().count());
        inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 23, inC.lines().count());
        inD = JUnitOperationsUtil.getBufferedReader(switchListFileD);
        Assert.assertEquals("confirm number of lines in switch list", 19, inD.lines().count());

        train.move(); // move train to B
        Assert.assertEquals("current train location", "Test Location B", train.getCurrentLocation().getName());

        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB);
        tsl.buildSwitchList(locationC);
        tsl.buildSwitchList(locationD);

        inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 23, inA.lines().count());
        inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 27, inB.lines().count());
        inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 23, inC.lines().count());
        inD = JUnitOperationsUtil.getBufferedReader(switchListFileD);
        Assert.assertEquals("confirm number of lines in switch list", 19, inD.lines().count());

        train.move(); // move train to A
        Assert.assertEquals("current train location", "Test Location A", train.getCurrentLocation().getName());

        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB);
        tsl.buildSwitchList(locationC);
        tsl.buildSwitchList(locationD);

        inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 23, inA.lines().count());
        inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 25, inB.lines().count());
        inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 23, inC.lines().count());
        inD = JUnitOperationsUtil.getBufferedReader(switchListFileD);
        Assert.assertEquals("confirm number of lines in switch list", 19, inD.lines().count());

        train.move(); // terminate train

        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB);
        tsl.buildSwitchList(locationC);
        tsl.buildSwitchList(locationD);

        inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 10, inA.lines().count());
        inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 13, inB.lines().count());
        inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 10, inC.lines().count());
        inD = JUnitOperationsUtil.getBufferedReader(switchListFileD);
        Assert.assertEquals("confirm number of lines in switch list", 9, inD.lines().count());

        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    /**
     * Unique data for this test. Any changes below can affect the number of
     * lines in the switch list file
     */
    private void loadLocationsEnginesAndCars() {

        CarTypes ct = InstanceManager.getDefault(CarTypes.class);

        // register the car and engine types used
        ct.addName("Boxcar");

        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location locationA = lmanager.newLocation("Test Location A");
        Track spurA = locationA.addTrack("Spur at A", Track.SPUR);
        spurA.setLength(400);
        Track yardA = locationA.addTrack("Yard at A", Track.YARD);
        yardA.setLength(400);

        // place 6 cars
        JUnitOperationsUtil.createAndPlaceCar("AA", "1", "Boxcar", "40", "DAB", "1984", spurA, 0);
        JUnitOperationsUtil.createAndPlaceCar("AA", "2", "Boxcar", "40", "DAB", "1984", spurA, 0);
        JUnitOperationsUtil.createAndPlaceCar("AA", "3", "Boxcar", "40", "DAB", "1984", spurA, 0);
        // 3 on yard tracks
        JUnitOperationsUtil.createAndPlaceCar("AA", "4", "Boxcar", "40", "DAB", "1984", yardA, 0);
        JUnitOperationsUtil.createAndPlaceCar("AA", "5", "Boxcar", "40", "DAB", "1984", yardA, 0);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("AA", "6", "Boxcar", "40", "DAB", "1984", yardA, 20);
        c6.setUtility(true); // make this car a utility car for better test coverage

        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Engine e1 = emanager.newRS("NYC", "1");
        e1.setModel("E8");
        Assert.assertEquals("Place engine on track", Track.OKAY, e1.setLocation(locationA, yardA));
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainCsvSwitchListsTest.class);

}
