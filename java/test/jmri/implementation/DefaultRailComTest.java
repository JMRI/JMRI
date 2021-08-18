package jmri.implementation;

import java.util.Calendar;
import java.util.Date;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for the DefaultRailCom class
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class DefaultRailComTest {

    @Test
    public void testCreateRailCom() {
        RailCom r = new DefaultRailCom("ID1234");
        Assert.assertNotNull("RailCom not null", r);
    }

    @Test
    public void testGetRailComUserName() {
        RailCom r = new DefaultRailCom("ID1234", "Test Tag");
        Assert.assertEquals("RailCom user name is 'Test Tag'", "Test Tag", r.getUserName());
    }

    @Test
    public void testGetRailComTagID() {
        RailCom r = new DefaultRailCom("ID1234");
        Assert.assertEquals("RailCom TagID is 1234", "1234", r.getTagID());
    }

    @Test
    public void testRailComGetLocoAddress() {
        RailCom r = new DefaultRailCom("ID1234");
        Assert.assertEquals("Loco Address ", new jmri.DccLocoAddress(1234,true), r.getLocoAddress());
    }

    @Test
    public void testRailComToString() {
        RailCom r = new DefaultRailCom("ID1234");
        Assert.assertEquals("RailCom toString ", "ID1234", r.toString());
    }

    @Test
    public void testRailComToReportString() {
        DefaultRailCom r = new DefaultRailCom("ID1234");
        Assert.assertEquals("RailCom toReportString ", "Unknown Orientation Address 1234(L) ", r.toReportString());
    }

    @Test
    public void testNotYetSeen() {
        RailCom r = new DefaultRailCom("ID0413276BC1");
        Assert.assertNull("At creation, Reporter where seen is null", r.getWhereLastSeen());
        Assert.assertNull("At creation, Date when seen is null", r.getWhenLastSeen());
        Assert.assertEquals("At creation, RailCom status is UNSEEN", RailCom.UNSEEN, r.getState());

        r.setWhereLastSeen(null);
        Assert.assertNull("After setWhereLastSeen(null), Reporter where seen is null", r.getWhereLastSeen());
        Assert.assertNull("After setWhereLastSeen(null), Date when seen is null", r.getWhenLastSeen());
        Assert.assertEquals("After setWhereLastSeen(null), RailCom status is UNSEEN", RailCom.UNSEEN, r.getState());
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
        Thread.sleep(5);
        r.setWhereLastSeen(rep);
        Thread.sleep(5);
        Date timeAfter = Calendar.getInstance().getTime();

        Assert.assertEquals("Where last seen is 'IR1'", rep, r.getWhereLastSeen());
        
        Date date = r.getWhenLastSeen();
        Assert.assertNotNull("When last seen is not null", date);
        Assert.assertEquals("Status is SEEN", RailCom.SEEN, r.getState());
        Assert.assertTrue("Time when last seen is later than 'timeBefore'", r.getWhenLastSeen().after(timeBefore));
        Assert.assertTrue("Time when last seen is earlier than 'timeAfter'", r.getWhenLastSeen().before(timeAfter));

        r.setWhereLastSeen(null);
        Assert.assertTrue("Time when last seen is later than 'timeBefore'", date.after(timeBefore));
        Assert.assertTrue("Time when last seen is earlier than 'timeAfter'", date.before(timeAfter));
        Assert.assertEquals("After setWhereLastSeen(null), RailCom status is UNSEEN", RailCom.UNSEEN, r.getState());

    }
    
    @Test
    public void testGetSetOrientation(){
        RailCom r = new DefaultRailCom("ID0415556BC1");
        Assert.assertEquals("getorientation is UNKNOWN at start", Sensor.UNKNOWN , r.getOrientation());
        r.setOrientation(RailCom.ORIENTA);
        Assert.assertEquals("getorientation is RailCom.ORIENTA", RailCom.ORIENTA , r.getOrientation());
    }

    @Test
    public void testGetSetActualSpeed(){
        RailCom r = new DefaultRailCom("ID0415556BC2");
        Assert.assertEquals("ActualSpeed is UNKNOWN at start", -1 , r.getActualSpeed());
        r.setActualSpeed(44);
        Assert.assertEquals("ActualSpeed is 44", 44 , r.getActualSpeed());
    }

    @Test
    public void testGetSetActualLoad(){
        RailCom r = new DefaultRailCom("ID0415556BC3");
        Assert.assertEquals("ActualLoad is UNKNOWN at start", -1 , r.getActualLoad());
        r.setActualLoad(3);
        Assert.assertEquals("ActualLoad is 3", 3 , r.getActualLoad());
    }

    @Test
    public void testGetSetActualTemperature(){
        RailCom r = new DefaultRailCom("ID0415556BC4");
        Assert.assertEquals("ActualTemperature is UNKNOWN at start", -1 , r.getActualTemperature());
        r.setActualTemperature(4);
        Assert.assertEquals("ActualTemperature is 4", 4 , r.getActualTemperature());
    }

    @Test
    public void testGetSetWaterLevel(){
        RailCom r = new DefaultRailCom("ID0415556BC5");
        Assert.assertEquals("WaterLevel is UNKNOWN at start", -1 , r.getWaterLevel());
        r.setWaterLevel(5);
        Assert.assertEquals("WaterLevel is 5", 5 , r.getWaterLevel());
    }

    @Test
    public void testGetSetFuelLevel(){
        RailCom r = new DefaultRailCom("ID0415556BC6");
        Assert.assertEquals("FuelLevel is UNKNOWN at start", -1 , r.getFuelLevel());
        r.setFuelLevel(6);
        Assert.assertEquals("FuelLevel is 6", 6 , r.getFuelLevel());
    }

    @Test
    public void testGetSetLocation(){
        RailCom r = new DefaultRailCom("ID0415556BC7");
        Assert.assertEquals("Location is UNKNOWN at start", -1 , r.getLocation());
        r.setLocation(7);
        Assert.assertEquals("Location is 7", 7 , r.getLocation());
    }

    @Test
    public void testGetSetRoutingNo(){
        RailCom r = new DefaultRailCom("ID0415556BD1");
        Assert.assertEquals("RoutingNo is UNKNOWN at start", -1 , r.getRoutingNo());
        r.setRoutingNo(8);
        Assert.assertEquals("RoutingNo is 8", 8 , r.getRoutingNo());
    }

    @Test
    public void testGetSetExpectedCv(){
        RailCom r = new DefaultRailCom("ID0415556BD2");
        Assert.assertEquals("ExpectedCv is UNKNOWN at start", -1 , r.getExpectedCv());
        r.setExpectedCv(9);
        Assert.assertEquals("ExpectedCv is 9", 9 , r.getExpectedCv());
    }

    @Test
    public void TestToReportString(){
        DefaultRailCom r = new DefaultRailCom("ID1234");
        Assert.assertEquals("Basic Report String", "Unknown Orientation Address 1234(L) " , r.toReportString());

        r.setOrientation(RailCom.ORIENTA);
        Assert.assertEquals("Report String ORIENTA", "Orientation A Address 1234(L) " , r.toReportString());

        r.setOrientation(RailCom.ORIENTB);
        Assert.assertEquals("Report String ORIENTB", "Orientation B Address 1234(L) " , r.toReportString());

        r.setWaterLevel(2);
        r.setFuelLevel(3);
        r.setLocation(4);
        r.setRoutingNo(5);
        r.setActualTemperature(6);
        r.setActualLoad(7);
        r.setActualSpeed(8);
        Assert.assertEquals("Report String ORIENTB", 
            "Orientation B Address 1234(L) Water 2 Fuel 3 Location : 4 Routing No : 5 Temperature : 6 Load : 7 Speed : 8 "
            , r.toReportString());

    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initRailComManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.clearShutDownManager(); // would be better to check and clean up specifics in tests
        JUnitUtil.tearDown();
    }

}
