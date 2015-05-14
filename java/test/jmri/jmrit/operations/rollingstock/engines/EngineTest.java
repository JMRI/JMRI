// EngineTest.java
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
public class EngineTest extends TestCase {

     // test constroctors.
    public void test2ParmCtor() {
      // test the constructor with roadname and roadnumer as parameters.
      Engine e1 = new Engine("TESTROAD", "TESTNUMBER1");
      Assert.assertNotNull("Two parameter Constructor",e1);

      Assert.assertEquals("Engine Road", "TESTROAD", e1.getRoadName());
      Assert.assertEquals("Engine Number", "TESTNUMBER1", e1.getNumber());
      Assert.assertEquals("Engine ID", "TESTROAD" + "TESTNUMBER1", e1.getId());
    }
    public void testXmlConstructor(){
       // test the constructor loading this car from an XML element.

       // first, we need to build the XML element.
       org.jdom2.Element e = new org.jdom2.Element("engines");
       // set the rolling stock generic attributes.
        e.setAttribute("id", "TESTID");
        e.setAttribute("roadName", "TESTROAD1");
        e.setAttribute("roadNumber", "TESTNUMBER1");
        e.setAttribute("type", "TESTTYPE");
        e.setAttribute("length", "TESTLENGTH");
        e.setAttribute("color", "TESTCOLOR");
        e.setAttribute("weight", "TESTWEIGHT");
        e.setAttribute("weightTons", "TESTWEIGHTTONS");
        e.setAttribute("built", "TESTBUILT");
        e.setAttribute("locationId","TESTLOCATION");
        e.setAttribute("routeLocationId", "TESTROUTELOCATION");
        e.setAttribute("secLocationId", "TESTTRACK");
        e.setAttribute("destinationId", "TESTDESTINATION");
        e.setAttribute("routeDestinationId", "TESTROUTEDESTINATION");
        e.setAttribute("secDestionationId", "TESTDESTINATIONTRACK");
        e.setAttribute("lastRouteId", "SAVEDROUTE");
        e.setAttribute("moves","5");

        e.setAttribute("date", "2015/05/15 15:15:15");
        e.setAttribute("selected",Xml.FALSE);
        e.setAttribute("lastLocationId", "TESTLASTLOCATION");
        e.setAttribute("train", "TESTTRAIN");
        e.setAttribute("owner", "TESTOWNER");
        e.setAttribute("value", "TESTVALUE");
        e.setAttribute("rifd","12345");
        e.setAttribute("locUnknown",Xml.FALSE);
        e.setAttribute("outOfService", Xml.FALSE);
        e.setAttribute("blocking", "5");
        e.setAttribute("comment","Test Comment");

        // set the engine specific attributes

        try {
           Engine e1 = new Engine(e);
           Assert.assertNotNull("Xml Element Constructor",e1);
        } catch(java.lang.NullPointerException npe) {
           Assert.fail("Null Pointer Exception while executing Xml Element Constructor");
        }
    }

    // test Engine Class
    // test Engine creation
    public void testCreate() {
        Engine e1 = new Engine("TESTROAD", "TESTNUMBER1");
        e1.setModel("TESTMODEL");
        e1.setLength("TESTLENGTH");

        Assert.assertEquals("Engine Road", "TESTROAD", e1.getRoadName());
        Assert.assertEquals("Engine Number", "TESTNUMBER1", e1.getNumber());
        Assert.assertEquals("Engine Model", "TESTMODEL", e1.getModel());
        Assert.assertEquals("Engine Length", "TESTLENGTH", e1.getLength());
    }

    public void testSetLocation() {
        Engine e1 = new Engine("TESTROAD", "TESTNUMBER1");
        e1.setModel("TESTMODEL");
        e1.setLength("50");

        Location l1 = new Location("id1", "B");
        Track l1t1 = l1.addTrack("A", Track.SPUR);
        Location l2 = new Location("id2", "C");
        Track l2t1 = l2.addTrack("B", Track.SPUR);
        Location l3 = new Location("id3", "A");
        Track l3t1 = l3.addTrack("B", Track.SPUR);
 
       // add track lengths
        l1t1.setLength(100);
        l1t1.setLength(100);
        l3t1.setLength(100);

        l1.addTypeName("Diesel");
        l2.addTypeName("Diesel");
        l3.addTypeName("Diesel");
        l1t1.addTypeName("Diesel");
        l2t1.addTypeName("Diesel");
        l3t1.addTypeName("Diesel");

        EngineTypes et = EngineTypes.instance();
        et.addName("Diesel");

        e1.setTypeName("Diesel");

        // place engines on tracks
        Assert.assertEquals("place e1", Track.OKAY, e1.setLocation(l1, l1t1));
        // check for failure too.
        Assert.assertFalse("fail place e1", Track.OKAY==e1.setLocation(l3, l2t1));


    }

    public void testSetDestination() {
        Engine e1 = new Engine("TESTROAD", "TESTNUMBER1");
        e1.setModel("TESTMODEL");
        e1.setLength("50");

        Location l1 = new Location("id1", "B");
        Track l1t1 = l1.addTrack("A", Track.SPUR);
        Location l2 = new Location("id2", "C");
        Track l2t1 = l2.addTrack("B", Track.SPUR);
        Location l3 = new Location("id3", "A");
        Track l3t1 = l3.addTrack("B", Track.SPUR);

        // add track lengths
        l1t1.setLength(100);
        l1t1.setLength(100);
        l3t1.setLength(100);

        l1.addTypeName("Diesel");
        l2.addTypeName("Diesel");
        l3.addTypeName("Diesel");
        l1t1.addTypeName("Diesel");
        l2t1.addTypeName("Diesel");
        l3t1.addTypeName("Diesel");

        EngineTypes et = EngineTypes.instance();
        et.addName("Diesel");

        e1.setTypeName("Diesel");

        e1.setLocation(l2,l2t1);

        // set destination.
        Assert.assertEquals("destination set e1", Track.OKAY, e1.setDestination(l1, l1t1));
        // check for failure too.
        Assert.assertFalse("fail to set destination e1", Track.OKAY==e1.setDestination(l3, l1t1));
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

    public EngineTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EngineTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EngineTest.class);
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
