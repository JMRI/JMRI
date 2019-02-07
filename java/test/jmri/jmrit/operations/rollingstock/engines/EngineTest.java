package jmri.jmrit.operations.rollingstock.engines;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Operations RollingStock Engine class Last manually
 * cross-checked on 20090131
 * <p>
 * Still to do: Engine: Destination Engine: Verify everything else EngineTypes:
 * get/set Names lists EngineModels: get/set Names lists EngineLengths:
 * Everything Consist: Everything Import: Everything EngineManager: Engine
 * register/deregister EngineManager: Consists
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 */
public class EngineTest extends OperationsTestCase {

    // test constroctors.
    @Test
    public void test2ParmCtor() {
        // test the constructor with roadname and roadnumer as parameters.
        Engine e1 = new Engine("TESTROAD", "TESTNUMBER1");
        Assert.assertNotNull("Two parameter Constructor", e1);

        Assert.assertEquals("Engine Road", "TESTROAD", e1.getRoadName());
        Assert.assertEquals("Engine Number", "TESTNUMBER1", e1.getNumber());
        Assert.assertEquals("Engine ID", "TESTROAD" + "TESTNUMBER1", e1.getId());
    }

    @Test
    public void testXmlConstructor() {
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
        e.setAttribute("locationId", "TESTLOCATION");
        e.setAttribute("routeLocationId", "TESTROUTELOCATION");
        e.setAttribute("secLocationId", "TESTTRACK");
        e.setAttribute("destinationId", "TESTDESTINATION");
        e.setAttribute("routeDestinationId", "TESTROUTEDESTINATION");
        e.setAttribute("secDestionationId", "TESTDESTINATIONTRACK");
        e.setAttribute("lastRouteId", "SAVEDROUTE");
        e.setAttribute("moves", "5");

        e.setAttribute("date", "2015/05/15 15:15:15");
        e.setAttribute("selected", Xml.FALSE);
        e.setAttribute("lastLocationId", "TESTLASTLOCATION");
        e.setAttribute("train", "TESTTRAIN");
        e.setAttribute("owner", "TESTOWNER");
        e.setAttribute("value", "TESTVALUE");
        e.setAttribute("rifd", "12345");
        e.setAttribute("locUnknown", Xml.FALSE);
        e.setAttribute("outOfService", Xml.FALSE);
        e.setAttribute("blocking", "5");
        e.setAttribute("comment", "Test Comment");

        // set the engine specific attributes
        try {
            Engine e1 = new Engine(e);
            Assert.assertNotNull("Xml Element Constructor", e1);
        } catch (java.lang.NullPointerException npe) {
            Assert.fail("Null Pointer Exception while executing Xml Element Constructor");
        }
    }

    // test Engine Class
    // test Engine creation
    @Test
    public void testCreate() {
        Engine e1 = new Engine("TESTROAD", "TESTNUMBER1");
        e1.setModel("TESTMODEL");
        e1.setLength("TESTLENGTH");

        Assert.assertEquals("Engine Road", "TESTROAD", e1.getRoadName());
        Assert.assertEquals("Engine Number", "TESTNUMBER1", e1.getNumber());
        Assert.assertEquals("Engine Model", "TESTMODEL", e1.getModel());
        Assert.assertEquals("Engine Length", "TESTLENGTH", e1.getLength());
    }

    @Test
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

        EngineTypes et = InstanceManager.getDefault(EngineTypes.class);
        et.addName("Diesel");

        e1.setTypeName("Diesel");

        // place engines on tracks
        Assert.assertEquals("place e1", Track.OKAY, e1.setLocation(l1, l1t1));
        // check for failure too.
        Assert.assertFalse("fail place e1", Track.OKAY == e1.setLocation(l3, l2t1));

    }

    @Test
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

        EngineTypes et = InstanceManager.getDefault(EngineTypes.class);
        et.addName("Diesel");

        e1.setTypeName("Diesel");

        e1.setLocation(l2, l2t1);

        // set destination.
        Assert.assertEquals("destination set e1", Track.OKAY, e1.setDestination(l1, l1t1));
        // check for failure too.
        Assert.assertFalse("fail to set destination e1", Track.OKAY == e1.setDestination(l3, l1t1));
    }
}
