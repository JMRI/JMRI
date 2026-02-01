package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the DefaultRailCom class
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class DefaultRailComTest {

    @Test
    public void testCreateRailCom() {
        RailCom r = new DefaultRailCom("ID1234");
        assertNotNull( r, "RailCom not null");
    }

    @Test
    public void testGetRailComUserName() {
        RailCom r = new DefaultRailCom("ID1234", "Test Tag");
        assertEquals( "Test Tag", r.getUserName(), "RailCom user name is 'Test Tag'");
    }

    @Test
    public void testGetRailComTagID() {
        RailCom r = new DefaultRailCom("ID1234");
        assertEquals( "1234", r.getTagID(), "RailCom TagID is 1234");
    }

    @Test
    public void testRailComGetLocoAddress() {
        RailCom r = new DefaultRailCom("ID1234");
        assertEquals( new jmri.DccLocoAddress(1234,true), r.getLocoAddress(), "Loco Address");
    }

    @Test
    public void testRailComToString() {
        RailCom r = new DefaultRailCom("ID1234");
        assertEquals( "ID1234", r.toString(), "RailCom toString ");
    }

    @Test
    public void testRailComToReportString() {
        DefaultRailCom r = new DefaultRailCom("ID1234");
        assertEquals( "Unknown Orientation Address 1234(L) ", r.toReportString(), "RailCom toReportString");
    }

    @Test
    public void testNotYetSeen() {
        RailCom r = new DefaultRailCom("ID0413276BC1");
        assertNull( r.getWhereLastSeen(), "At creation, Reporter where seen is null");
        assertNull( r.getWhenLastSeen(), "At creation, Date when seen is null");
        assertEquals( RailCom.UNSEEN, r.getState(), "At creation, RailCom status is UNSEEN");

        r.setWhereLastSeen(null);
        assertNull( r.getWhereLastSeen(), "After setWhereLastSeen(null), Reporter where seen is null");
        assertNull( r.getWhenLastSeen(), "After setWhereLastSeen(null), Date when seen is null");
        assertEquals( RailCom.UNSEEN, r.getState(), "After setWhereLastSeen(null), RailCom status is UNSEEN");
    }

    @Test
    public void testHasBeenSeen() throws InterruptedException {
        RailCom r = new DefaultRailCom("ID0413276BC1");
        Reporter rep = new AbstractReporter("IR1") {
            @Override
            public int getState() {
                return state;
            }

            @Override
            public void setState(int s) {
                state = s;
            }
            private int state = 0;
        };

        Date timeBefore = Calendar.getInstance().getTime();
        JUnitUtil.waitFor(5);
        r.setWhereLastSeen(rep);
        JUnitUtil.waitFor(5);
        Date timeAfter = Calendar.getInstance().getTime();

        assertEquals( rep, r.getWhereLastSeen(), "Where last seen is 'IR1'");
        
        Date date = r.getWhenLastSeen();
        assertNotNull( date, "When last seen is not null");
        assertEquals( RailCom.SEEN, r.getState(), "Status is SEEN");
        assertTrue( date.after(timeBefore), "Time when last seen is later than 'timeBefore'");
        assertTrue( date.before(timeAfter), "Time when last seen is earlier than 'timeAfter'");

        r.setWhereLastSeen(null);
        assertTrue( date.after(timeBefore), "Time when last seen is later than 'timeBefore'");
        assertTrue( date.before(timeAfter), "Time when last seen is earlier than 'timeAfter'");
        assertEquals( RailCom.UNSEEN, r.getState(), "After setWhereLastSeen(null), RailCom status is UNSEEN");

    }
    
    @Test
    public void testGetSetOrientation(){
        RailCom r = new DefaultRailCom("ID0415556BC1");
        assertEquals( RailCom.Orientation.UNKNOWN , r.getOrientation(), "getorientation is UNKNOWN at start");
        r.setOrientation(RailCom.Orientation.ORIENTA);
        assertEquals( RailCom.Orientation.ORIENTA , r.getOrientation(), "getorientation is RailCom.ORIENTA");
    }

    @Test
    public void testGetSetActualSpeed(){
        RailCom r = new DefaultRailCom("ID0415556BC2");
        assertEquals( -1 , r.getActualSpeed(), "ActualSpeed is UNKNOWN at start");
        r.setActualSpeed(44);
        assertEquals( 44 , r.getActualSpeed(), "ActualSpeed is 44");
    }

    @Test
    public void testGetSetActualLoad(){
        RailCom r = new DefaultRailCom("ID0415556BC3");
        assertEquals( -1 , r.getActualLoad(), "ActualLoad is UNKNOWN at start");
        r.setActualLoad(3);
        assertEquals( 3 , r.getActualLoad(), "ActualLoad is 3");
    }

    @Test
    public void testGetSetActualTemperature(){
        RailCom r = new DefaultRailCom("ID0415556BC4");
        assertEquals( -1 , r.getActualTemperature(), "ActualTemperature is UNKNOWN at start");
        r.setActualTemperature(4);
        assertEquals( 4 , r.getActualTemperature(), "ActualTemperature is 4");
    }

    @Test
    public void testGetSetWaterLevel(){
        RailCom r = new DefaultRailCom("ID0415556BC5");
        assertEquals( -1 , r.getWaterLevel(), "WaterLevel is UNKNOWN at start");
        r.setWaterLevel(5);
        assertEquals( 5 , r.getWaterLevel(), "WaterLevel is 5");
    }

    @Test
    public void testGetSetFuelLevel(){
        RailCom r = new DefaultRailCom("ID0415556BC6");
        assertEquals( -1 , r.getFuelLevel(), "FuelLevel is UNKNOWN at start");
        r.setFuelLevel(6);
        assertEquals( 6 , r.getFuelLevel(), "FuelLevel is 6");
    }

    @Test
    public void testGetSetLocation(){
        RailCom r = new DefaultRailCom("ID0415556BC7");
        assertEquals( -1 , r.getLocation(), "Location is UNKNOWN at start");
        r.setLocation(7);
        assertEquals( 7 , r.getLocation(), "Location is 7");
    }

    @Test
    public void testGetSetRoutingNo(){
        RailCom r = new DefaultRailCom("ID0415556BD1");
        assertEquals( -1 , r.getRoutingNo(), "RoutingNo is UNKNOWN at start");
        r.setRoutingNo(8);
        assertEquals( 8 , r.getRoutingNo(), "RoutingNo is 8");
    }

    @Test
    public void testGetSetExpectedCv(){
        RailCom r = new DefaultRailCom("ID0415556BD2");
        assertEquals( -1 , r.getExpectedCv(), "ExpectedCv is UNKNOWN at start");
        r.setExpectedCv(9);
        assertEquals( 9 , r.getExpectedCv(), "ExpectedCv is 9");
    }

    @Test
    public void testToReportString(){
        DefaultRailCom r = new DefaultRailCom("ID1234");
        assertEquals( "Unknown Orientation Address 1234(L) " , r.toReportString(), "Basic Report String");

        r.setOrientation(RailCom.Orientation.ORIENTA);
        assertEquals( "Orientation A Address 1234(L) " , r.toReportString(), "Report String ORIENTA");

        r.setOrientation(RailCom.Orientation.ORIENTB);
        assertEquals( "Orientation B Address 1234(L) " , r.toReportString(), "Report String ORIENTB");

        r.setWaterLevel(2);
        r.setFuelLevel(3);
        r.setLocation(4);
        r.setRoutingNo(5);
        r.setActualTemperature(6);
        r.setActualLoad(7);
        r.setActualSpeed(8);
        assertEquals( "Orientation B Address 1234(L) Water 2 Fuel 3 Location : 4 Routing No : 5 Temperature : 6 Load : 7 Speed : 8 "
            , r.toReportString(), "Report String ORIENTB");

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initRailComManager();
    }

    @AfterEach
    public void tearDown() {
        InstanceManager.getDefault( IdTagManager.class).dispose();
        JUnitUtil.tearDown();
    }

}
