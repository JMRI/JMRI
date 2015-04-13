// EngineManagerTest.java
package jmri.jmrit.operations.rollingstock.engines;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom2.JDOMException;

/**
 * Tests for the Operations RollingStock Engine class Last manually
 * cross-checked on 20090131
 *
 * Still to do: Engine: Destination Engine: Verify everything else EngineTypes:
 * get/set Names lists EngineModels: get/set Names lists EngineLengths:
 * Everything Consist: Everything Import: Everything EngineManager: Engine
 * register/deregister EngineManager: Consists
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision$
 */
public class EngineManagerTest extends TestCase {

    public void testEngineManager() {
        EngineManager manager = EngineManager.instance();
        List<RollingStock> engineList = manager.getByIdList();

        Assert.assertEquals("Starting Number of Engines", 0, engineList.size());
        Engine e1 = manager.newEngine("CP", "1");
        Engine e2 = manager.newEngine("ACL", "3");
        Engine e3 = manager.newEngine("CP", "3");
        Engine e4 = manager.newEngine("CP", "3-1");
        Engine e5 = manager.newEngine("PC", "2");
        Engine e6 = manager.newEngine("AA", "1");

        //setup the engines
        e1.setBuilt("2800");
        e2.setBuilt("1212");
        e3.setBuilt("100");
        e4.setBuilt("10");
        e5.setBuilt("1000");
        e6.setBuilt("1956");

        e1.setModel("GP356");
        e2.setModel("GP354");
        e3.setModel("GP351");
        e4.setModel("GP352");
        e5.setModel("GP353");
        e6.setModel("GP355");

        e1.setTypeName("Diesel");
        e2.setTypeName("Diesel");
        e3.setTypeName("Diesel");
        e4.setTypeName("Diesel");
        e5.setTypeName("Diesel");
        e6.setTypeName("Diesel");

        e1.setLength("13");
        e2.setLength("9");
        e3.setLength("12");
        e4.setLength("10");
        e5.setLength("11");
        e6.setLength("14");

        Location l1 = new Location("id1", "B");
        Track l1t1 = l1.addTrack("A", Track.SPUR);
        Track l1t2 = l1.addTrack("B", Track.SPUR);
        Location l2 = new Location("id2", "C");
        Track l2t1 = l2.addTrack("B", Track.SPUR);
        Track l2t2 = l2.addTrack("A", Track.SPUR);
        Location l3 = new Location("id3", "A");
        Track l3t1 = l3.addTrack("B", Track.SPUR);
        Track l3t2 = l3.addTrack("A", Track.SPUR);

        // add track lengths       
        l1t1.setLength(100);
        l1t2.setLength(100);
        l2t1.setLength(100);
        l2t2.setLength(100);
        l3t1.setLength(100);
        l3t2.setLength(100);

        l1.addTypeName("Diesel");
        l2.addTypeName("Diesel");
        l3.addTypeName("Diesel");
        l1t1.addTypeName("Diesel");
        l1t2.addTypeName("Diesel");
        l2t1.addTypeName("Diesel");
        l2t2.addTypeName("Diesel");
        l3t1.addTypeName("Diesel");
        l3t2.addTypeName("Diesel");

        EngineTypes et = EngineTypes.instance();
        et.addName("Diesel");

        // place engines on tracks
        Assert.assertEquals("place e1", Track.OKAY, e1.setLocation(l1, l1t1));
        Assert.assertEquals("place e2", Track.OKAY, e2.setLocation(l1, l1t2));
        Assert.assertEquals("place e3", Track.OKAY, e3.setLocation(l2, l2t1));
        Assert.assertEquals("place e4", Track.OKAY, e4.setLocation(l2, l2t2));
        Assert.assertEquals("place e5", Track.OKAY, e5.setLocation(l3, l3t1));
        Assert.assertEquals("place e6", Track.OKAY, e6.setLocation(l3, l3t2));

        // set engine destinations
        Assert.assertEquals("destination e1", Track.OKAY, e1.setDestination(l3, l3t1));
        Assert.assertEquals("destination e2", Track.OKAY, e2.setDestination(l3, l3t2));
        Assert.assertEquals("destination e3", Track.OKAY, e3.setDestination(l2, l2t2));
        Assert.assertEquals("destination e4", Track.OKAY, e4.setDestination(l2, l2t1));
        Assert.assertEquals("destination e5", Track.OKAY, e5.setDestination(l1, l1t1));
        Assert.assertEquals("destination e6", Track.OKAY, e6.setDestination(l1, l1t2));

        e1.setConsist(new Consist("F"));
        e2.setConsist(new Consist("D"));
        e3.setConsist(new Consist("B"));
        e4.setConsist(new Consist("A"));
        e5.setConsist(new Consist("C"));
        e6.setConsist(new Consist("E"));

        e1.setMoves(2);
        e2.setMoves(44);
        e3.setMoves(99999);
        e4.setMoves(33);
        e5.setMoves(4);
        e6.setMoves(9999);

        // make sure the ID tags exist before we
        // try to add it to an engine.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("SQ1");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("1Ab");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("Ase");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("asd");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("93F");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("B12");
        
        e1.setRfid("SQ1");
        e2.setRfid("1Ab");
        e3.setRfid("Ase");
        e4.setRfid("asd");
        e5.setRfid("93F");
        e6.setRfid("B12");

        e1.setOwner("LAST");
        e2.setOwner("FOOL");
        e3.setOwner("AAA");
        e4.setOwner("DAD");
        e5.setOwner("DAB");
        e6.setOwner("BOB");

        Route r = new Route("id", "Test");
        r.addLocation(l1);
        r.addLocation(l2);
        r.addLocation(l3);

        Train t1 = new Train("id1", "F");
        t1.setRoute(r);
        Train t3 = new Train("id3", "E");
        t3.setRoute(r);

        e1.setTrain(t1);
        e2.setTrain(t3);
        e3.setTrain(t3);
        e4.setTrain(new Train("id4", "B"));
        e5.setTrain(t3);
        e6.setTrain(new Train("id6", "A"));

        // now get engines by id
        engineList = manager.getByIdList();
        Assert.assertEquals("Number of Engines by id", 6, engineList.size());
        Assert.assertEquals("1st engine in list by id", e6, engineList.get(0));
        Assert.assertEquals("2nd engine in list by id", e2, engineList.get(1));
        Assert.assertEquals("3rd engine in list by id", e1, engineList.get(2));
        Assert.assertEquals("4th engine in list by id", e3, engineList.get(3));
        Assert.assertEquals("5th engine in list by id", e4, engineList.get(4));
        Assert.assertEquals("6th engine in list by id", e5, engineList.get(5));

        // now get engines by built
        engineList = manager.getByBuiltList();
        Assert.assertEquals("Number of Engines by built", 6, engineList.size());
        Assert.assertEquals("1st engine in list by built", e4, engineList.get(0));
        Assert.assertEquals("2nd engine in list by built", e3, engineList.get(1));
        Assert.assertEquals("3rd engine in list by built", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by built", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by built", e6, engineList.get(4));
        Assert.assertEquals("6th engine in list by built", e1, engineList.get(5));

        // now get engines by moves
        engineList = manager.getByMovesList();
        Assert.assertEquals("Number of Engines by move", 6, engineList.size());
        Assert.assertEquals("1st engine in list by move", e1, engineList.get(0));
        Assert.assertEquals("2nd engine in list by move", e5, engineList.get(1));
        Assert.assertEquals("3rd engine in list by move", e4, engineList.get(2));
        Assert.assertEquals("4th engine in list by move", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by move", e6, engineList.get(4));
        Assert.assertEquals("6th engine in list by move", e3, engineList.get(5));

        // now get engines by owner
        engineList = manager.getByOwnerList();
        Assert.assertEquals("Number of Engines by owner", 6, engineList.size());
        Assert.assertEquals("1st engine in list by owner", e3, engineList.get(0));
        Assert.assertEquals("2nd engine in list by owner", e6, engineList.get(1));
        Assert.assertEquals("3rd engine in list by owner", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by owner", e4, engineList.get(3));
        Assert.assertEquals("5th engine in list by owner", e2, engineList.get(4));
        Assert.assertEquals("6th engine in list by owner", e1, engineList.get(5));

        // now get engines by road name
        engineList = manager.getByRoadNameList();
        Assert.assertEquals("Number of Engines by road name", 6, engineList.size());
        Assert.assertEquals("1st engine in list by road name", e6, engineList.get(0));
        Assert.assertEquals("2nd engine in list by road name", e2, engineList.get(1));
        Assert.assertEquals("3rd engine in list by road name", e1, engineList.get(2));
        Assert.assertEquals("4th engine in list by road name", e3, engineList.get(3));
        Assert.assertEquals("5th engine in list by road name", e4, engineList.get(4));
        Assert.assertEquals("6th engine in list by road name", e5, engineList.get(5));

        // now get engines by consist
        engineList = manager.getByConsistList();
        Assert.assertEquals("Number of Engines by consist", 6, engineList.size());
        Assert.assertEquals("1st engine in list by consist", e4, engineList.get(0));
        Assert.assertEquals("2nd engine in list by consist", e3, engineList.get(1));
        Assert.assertEquals("3rd engine in list by consist", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by consist", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by consist", e6, engineList.get(4));
        Assert.assertEquals("6th engine in list by consist", e1, engineList.get(5));

        // now get engines by location
        engineList = manager.getByLocationList();
        Assert.assertEquals("Number of Engines by location", 6, engineList.size());
        Assert.assertEquals("1st engine in list by location", e6, engineList.get(0));
        Assert.assertEquals("2nd engine in list by location", e5, engineList.get(1));
        Assert.assertEquals("3rd engine in list by location", e1, engineList.get(2));
        Assert.assertEquals("4th engine in list by location", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by location", e4, engineList.get(4));
        Assert.assertEquals("6th engine in list by location", e3, engineList.get(5));

        // now get engines by destination
        engineList = manager.getByDestinationList();
        Assert.assertEquals("Number of Engines by destination", 6, engineList.size());
        Assert.assertEquals("1st engine in list by destination", e2, engineList.get(0));
        Assert.assertEquals("2nd engine in list by destination", e1, engineList.get(1));
        Assert.assertEquals("3rd engine in list by destination", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by destination", e6, engineList.get(3));
        Assert.assertEquals("5th engine in list by destination", e3, engineList.get(4));
        Assert.assertEquals("6th engine in list by destination", e4, engineList.get(5));

        // now get engines by train
        engineList = manager.getByTrainList();
        Assert.assertEquals("Number of Engines by train", 6, engineList.size());
        Assert.assertEquals("1st engine in list by train", e6, engineList.get(0));
        Assert.assertEquals("2nd engine in list by train", e4, engineList.get(1));
        Assert.assertEquals("3rd engine in list by train", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by train", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by train", e3, engineList.get(4));
        Assert.assertEquals("6th engine in list by train", e1, engineList.get(5));

        // now get engines by specific train
        List<Engine> engineList2 = manager.getByTrainBlockingList(t1);
        Assert.assertEquals("Number of Engines in t1", 1, engineList2.size());
        Assert.assertEquals("1st engine in list by t1", e1, engineList2.get(0));
        engineList2 = manager.getByTrainBlockingList(t3);
        Assert.assertEquals("Number of Engines in t3", 3, engineList2.size());
        Assert.assertEquals("1st engine in list by t3", e5, engineList2.get(0));
        Assert.assertEquals("2nd engine in list by t3", e2, engineList2.get(1));
        Assert.assertEquals("3rd engine in list by t3", e3, engineList2.get(2));

        // how many engines available?
        engineList2 = manager.getAvailableTrainList(t1);
        Assert.assertEquals("Number of Engines available for t1", 1, engineList2.size());
        Assert.assertEquals("1st engine in list available for t1", e1, engineList2.get(0));

        engineList2 = manager.getAvailableTrainList(t3);
        Assert.assertEquals("Number of Engines available for t3", 3, engineList2.size());
        Assert.assertEquals("1st engine in list available for t3", e5, engineList2.get(0));
        Assert.assertEquals("2nd engine in list available for t3", e2, engineList2.get(1));
        Assert.assertEquals("3rd engine in list available for t3", e3, engineList2.get(2));

        // release engines from trains
        e2.setTrain(null);
        e4.setTrain(null);	// e4 is located in the middle of the route, therefore not available
        e6.setTrain(null);	// e6 is located at the end of the route, therefore not available

        // there should be more engines now
        engineList2 = manager.getAvailableTrainList(t1);
        Assert.assertEquals("Number of Engines available t1 after release", 4, engineList2.size());
        // should be sorted by moves
        Assert.assertEquals("1st engine in list available for t1", e1, engineList2.get(0));
        Assert.assertEquals("2nd engine in list available for t1", e4, engineList2.get(1));

        engineList2 = manager.getAvailableTrainList(t3);
        Assert.assertEquals("Number of Engines available for t3 after release", 5, engineList2.size());
        Assert.assertEquals("1st engine in list available for t3", e5, engineList2.get(0));

        // now get engines by road number
        engineList = manager.getByNumberList();
        Assert.assertEquals("Number of Engines by number", 6, engineList.size());
        Assert.assertEquals("1st engine in list by number", e6, engineList.get(0));
        Assert.assertEquals("2nd engine in list by number", e1, engineList.get(1));
        Assert.assertEquals("3rd engine in list by number", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by number", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by number", e3, engineList.get(4));
        Assert.assertEquals("6th engine in list by number", e4, engineList.get(5));

        // find engine by road and number
        Assert.assertEquals("find e1 by road and number", e1, manager.getByRoadAndNumber("CP", "1"));
        Assert.assertEquals("find e2 by road and number", e2, manager.getByRoadAndNumber("ACL", "3"));
        Assert.assertEquals("find e3 by road and number", e3, manager.getByRoadAndNumber("CP", "3"));
        Assert.assertEquals("find e4 by road and number", e4, manager.getByRoadAndNumber("CP", "3-1"));
        Assert.assertEquals("find e5 by road and number", e5, manager.getByRoadAndNumber("PC", "2"));
        Assert.assertEquals("find e6 by road and number", e6, manager.getByRoadAndNumber("AA", "1"));

        // now get engines by RFID
        engineList = manager.getByRfidList();
        Assert.assertEquals("Number of Engines by rfid", 6, engineList.size());
        Assert.assertEquals("1st engine in list by rfid", e2, engineList.get(0));
        Assert.assertEquals("2nd engine in list by rfid", e5, engineList.get(1));
        Assert.assertEquals("3rd engine in list by rfid", e4, engineList.get(2));
        Assert.assertEquals("4th engine in list by rfid", e3, engineList.get(3));
        Assert.assertEquals("5th engine in list by rfid", e6, engineList.get(4));
        Assert.assertEquals("6th engine in list by rfid", e1, engineList.get(5));

        // find engine by RFID
        Assert.assertEquals("find e1 by rfid", e1, manager.getByRfid("SQ1"));
        Assert.assertEquals("find e2 by rfid", e2, manager.getByRfid("1Ab"));
        Assert.assertEquals("find e3 by rfid", e3, manager.getByRfid("Ase"));
        Assert.assertEquals("find e4 by rfid", e4, manager.getByRfid("asd"));
        Assert.assertEquals("find e5 by rfid", e5, manager.getByRfid("93F"));
        Assert.assertEquals("find e6 by rfid", e6, manager.getByRfid("B12"));

        // now get engines by model
        engineList = manager.getByModelList();
        Assert.assertEquals("Number of Engines by type", 6, engineList.size());
        Assert.assertEquals("1st engine in list by type", e3, engineList.get(0));
        Assert.assertEquals("2nd engine in list by type", e4, engineList.get(1));
        Assert.assertEquals("3rd engine in list by type", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by type", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by type", e6, engineList.get(4));
        Assert.assertEquals("6th engine in list by type", e1, engineList.get(5));

        manager.dispose();
        engineList = manager.getByIdList();
        Assert.assertEquals("After dispose Number of Engines", 0, engineList.size());
    }

    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception{
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initIdTagManager();
        jmri.InstanceManager.setShutDownManager( new
                 jmri.managers.DefaultShutDownManager() {
                    @Override
                    public void register(jmri.ShutDownTask s){
                       // do nothing with registered shutdown tasks for testing.
                    }
                 });

        // set the locale to US English
        Locale.setDefault(Locale.ENGLISH);

        // Repoint OperationsSetupXml to JUnitTest subdirectory
        OperationsSetupXml.setOperationsDirectoryName("operations" + File.separator + "JUnitTest");
        // Change file names to ...Test.xml
        OperationsSetupXml.instance().setOperationsFileName("OperationsJUnitTest.xml");
        RouteManagerXml.instance().setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
        CarManagerXml.instance().setOperationsFileName("OperationsJUnitTestCarRoster.xml");
        EngineManagerXml.instance().setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
        LocationManagerXml.instance().setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
        TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml");

        // Need to clear out EngineManager global variables
        EngineManager manager = EngineManager.instance();
        List<String> tempconsistList = manager.getConsistNameList();
        for (int i = 0; i < tempconsistList.size(); i++) {
            String consistId = tempconsistList.get(i);
            manager.deleteConsist(consistId);
        }
        EngineModels.instance().dispose();
        EngineLengths.instance().dispose();
        manager.dispose();
    }

    public EngineManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EngineManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EngineManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
       JUnitUtil.resetInstanceManager();
       apps.tests.Log4JFixture.tearDown();
       super.tearDown();
    }


}
