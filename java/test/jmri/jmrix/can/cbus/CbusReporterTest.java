package jmri.jmrix.can.cbus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.IdTag;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.Sensor;
import jmri.jmrix.can.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CbusReporterTest extends jmri.implementation.AbstractReporterTestBase {

    @Override
    protected Object generateObjectToReport(){
        return new jmri.implementation.DefaultIdTag("ID0413276BC1", "Test Tag");
    }

    @Test
    public void testRespondToClassicRfidCanReply(){

        // a new tag provided by Reporter4 then moves to Reporter 5
        Reporter r4 = memo.get(ReporterManager.class).provideReporter("4");
        Reporter r5 = memo.get(ReporterManager.class).provideReporter("5");

        assertNotEquals(r.describeState(IdTag.UNSEEN), r.describeState(IdTag.SEEN),
            "messages should be different");

        CanReply m = new CanReply(tcis.getCanid());
        m.setNumDataElements(8);
        m.setElement(0, CbusConstants.CBUS_DDES);
        m.setElement(1, 0x00); // ev hi
        m.setElement(2, 0x04); // ev lo
        m.setElement(3, 0x30); // tag1
        m.setElement(4, 0x39); // tag2
        m.setElement(5, 0x31); // tag3
        m.setElement(6, 0x30); // tag4
        m.setElement(7, 0xAB); // tag5
        tcis.sendToListeners(m);

        // tag unseen = 2
        // tag seen = 3

        assertEquals(r4.describeState(IdTag.SEEN),r4.describeState(r4.getState()), "r4 state set");
        assertNotNull(r4.getCurrentReport(), "r4 report set");
        assertEquals(r5.describeState(IdTag.UNKNOWN),r5.describeState(r5.getState()), "r5 state unset");
        assertNull(r5.getCurrentReport(), "r5 report unset");

        m.setElement(2, 0x05); // ev lo
        tcis.sendToListeners(m);

        assertEquals(r5.describeState(IdTag.SEEN),r5.describeState(r5.getState()), "r5 tag seen");
        assertNotNull(r5.getCurrentReport(), "r5 report set");
        assertEquals(r4.describeState(IdTag.UNSEEN),r4.describeState(r4.getState()), "r4 tag gone");
        assertNull(r4.getCurrentReport(), "r4 report unset");

        CanReply m2 = new CanReply(tcis.getCanid());
        m2.setNumDataElements(8);
        m2.setElement(0, CbusConstants.CBUS_ACDAT);
        m2.setElement(1, 0x00); // ev hi
        m2.setElement(2, 0x04); // ev lo
        m2.setElement(3, 0x30); // tag1
        m2.setElement(4, 0x39); // tag2
        m2.setElement(5, 0x31); // tag3
        m2.setElement(6, 0x30); // tag4
        m2.setElement(7, 0xAB); // tag5

        tcis.sendToListeners(m2);

        assertEquals(r4.describeState(IdTag.SEEN),r4.describeState(r4.getState()), "r4 state set CBUS_ACDAT");
        assertNotNull(r4.getCurrentReport(), "r4 report set CBUS_ACDAT");
        assertEquals(r5.describeState(IdTag.UNSEEN),r5.describeState(r5.getState()), "r5 state unset CBUS_ACDAT");
        assertNull(r5.getCurrentReport(), "r5 report unset CBUS_ACDAT");

        m2.setElement(2, 0x05); // ev lo

        m2.setExtended(true);
        tcis.sendToListeners(m2);
        assertEquals(r5.describeState(IdTag.UNSEEN),r5.describeState(r5.getState()), "r5 state unset extended");

        m2.setExtended(false);
        m2.setRtr(true);
        tcis.sendToListeners(m2);
        assertEquals(r5.describeState(IdTag.UNSEEN),r5.describeState(r5.getState()), "r5 state unset rtr");

        m2.setRtr(false);
        m2.setElement(0, 0x05); // random OPC not related to reporters
        tcis.sendToListeners(m2);
        assertEquals(r5.describeState(IdTag.UNSEEN),r5.describeState(r5.getState()), "r5 state unset random opc");

        m2.setElement(0, CbusConstants.CBUS_DDES); // put it back
        tcis.sendToListeners(m2);
        assertEquals(r5.describeState(IdTag.SEEN),r5.describeState(r5.getState()), "r5 state set ok after incorrect msgs");

        assertEquals(r4.describeState(IdTag.UNSEEN),r4.describeState(r4.getState()), "r4 state unseen");

        CanMessage m3 = new CanMessage(tcis.getCanid());
        m3.setNumDataElements(8);
        m3.setElement(0, CbusConstants.CBUS_ACDAT);
        m3.setElement(1, 0x00); // ev hi
        m3.setElement(2, 0x04); // ev lo
        m3.setElement(3, 0x30); // tag1
        m3.setElement(4, 0x39); // tag2
        m3.setElement(5, 0x31); // tag3
        m3.setElement(6, 0x30); // tag4
        m3.setElement(7, 0xAB); // tag5

        tcis.sendToListeners(m3);

        assertEquals(IdTag.SEEN,r4.getState(), "r4 seen after CBUS_ACDAT outgoing message");

        r4.dispose();
        r5.dispose();
    }

    // A Classic RFID reporter must report the 5 byte tag whatever the value of the
    // first tag byte, the tag data must not be inspected as a self-describing DDES type,
    // and the reporter type must not be changed by the manager on frame content.
    @Test
    public void testClassicRfidTagWithOddFirstByte(){

        Reporter r4 = memo.get(ReporterManager.class).provideReporter("4");
        assertEquals(CbusReporterManager.CBUS_REPORTER_TYPE_CLASSIC,
            ((CbusReporter)r4).getCbusReporterType(), "default type Classic RFID");

        CanReply m = new CanReply(tcis.getCanid());
        m.setNumDataElements(8);
        m.setElement(0, CbusConstants.CBUS_DDES);
        m.setElement(1, 0x00); // ev hi
        m.setElement(2, 0x04); // ev lo
        m.setElement(3, 0x41); // tag1, least significant bit set
        m.setElement(4, 0x39); // tag2
        m.setElement(5, 0x31); // tag3
        m.setElement(6, 0x30); // tag4
        m.setElement(7, 0xAB); // tag5
        tcis.sendToListeners(m); // via CbusReporterManager

        assertEquals(IdTag.SEEN, r4.getState(), "r4 tag seen DDES");
        Object report = r4.getCurrentReport();
        assertNotNull(report, "r4 report set DDES");
        assertTrue(report instanceof IdTag, "report is an IdTag");
        assertFalse(report instanceof jmri.RailCom, "report is not a RailCom tag");
        assertEquals("41393130AB", ((IdTag)report).getTagID(), "10 hex digit classic tag ID");
        assertEquals(CbusReporterManager.CBUS_REPORTER_TYPE_CLASSIC,
            ((CbusReporter)r4).getCbusReporterType(), "type not changed by frame content");

        // same tag on ACDAT, reported by reporter 5
        Reporter r5 = memo.get(ReporterManager.class).provideReporter("5");
        m.setElement(0, CbusConstants.CBUS_ACDAT);
        m.setElement(2, 0x05); // ev lo
        tcis.sendToListeners(m);

        assertEquals(IdTag.SEEN, r5.getState(), "r5 tag seen ACDAT");
        report = r5.getCurrentReport();
        assertNotNull(report, "r5 report set ACDAT");
        assertTrue(report instanceof IdTag, "ACDAT report is an IdTag");
        assertEquals("41393130AB", ((IdTag)report).getTagID(), "ACDAT 10 hex digit classic tag ID");
        assertEquals(IdTag.UNSEEN, r4.getState(), "r4 tag gone");
        assertEquals(CbusReporterManager.CBUS_REPORTER_TYPE_CLASSIC,
            ((CbusReporter)r5).getCbusReporterType(), "r5 type not changed by frame content");

        // a self-describing reporter still uses the DDES format bit, tag1 0x01 is RailCom
        Reporter r6 = memo.get(ReporterManager.class).provideReporter("6");
        r6.setProperty(CbusReporterManager.CBUS_REPORTER_DESCRIPTOR_KEY, CbusReporterManager.CBUS_REPORTER_TYPE_DDES_DESCRIBING);
        m.setElement(0, CbusConstants.CBUS_DDES);
        m.setElement(2, 0x06); // ev lo
        m.setElement(3, 0x01); // railcom
        m.setElement(4, 0xC0); // long address 1
        m.setElement(5, 0x01);
        m.setElement(6, 0x00);
        m.setElement(7, 0x00);
        tcis.sendToListeners(m);
        assertEquals(IdTag.SEEN, r6.getState(), "r6 tag seen");
        report = r6.getCurrentReport();
        assertTrue(report instanceof jmri.RailCom, "self-describing reporter report is a RailCom tag");
        assertEquals(new jmri.DccLocoAddress(1, true), ((jmri.RailCom)report).getDccAddress(), "RailCom loco 1");

        r4.dispose();
        r5.dispose();
        r6.dispose();
    }

    // A Reporter created by a CANRCOM style DDES is set to the self-describing type,
    // a Reporter created by a Classic RFID ACDAT is not, whatever the tag data.
    @Test
    public void testNewReporterTypeFromFirstFrame(){

        CanReply m = new CanReply(tcis.getCanid());
        m.setNumDataElements(8);
        m.setElement(0, CbusConstants.CBUS_DDES);
        m.setElement(1, 0x00); // dd hi
        m.setElement(2, 0x07); // dd lo, reporter 7 does not exist yet
        m.setElement(3, 0x01); // railcom
        m.setElement(4, 0x00); // short address 3
        m.setElement(5, 0x03);
        m.setElement(6, 0x00);
        m.setElement(7, 0x00);
        tcis.sendToListeners(m);

        CbusReporter r7 = (CbusReporter)memo.get(ReporterManager.class).getBySystemName("MR7");
        assertNotNull(r7, "reporter 7 created");
        assertEquals(CbusReporterManager.CBUS_REPORTER_TYPE_DDES_DESCRIBING, r7.getCbusReporterType(),
            "new reporter from RailCom DDES is self-describing");
        assertTrue(r7.getCurrentReport() instanceof jmri.RailCom, "RailCom report");

        // user sets the type back to Classic, a later frame must not change it
        r7.setProperty(CbusReporterManager.CBUS_REPORTER_DESCRIPTOR_KEY, CbusReporterManager.CBUS_REPORTER_TYPE_CLASSIC);
        tcis.sendToListeners(m);
        assertEquals(CbusReporterManager.CBUS_REPORTER_TYPE_CLASSIC, r7.getCbusReporterType(),
            "existing reporter type not changed by frame content");

        // new reporter from a Classic RFID ACDAT, tag first byte low nibble 1
        m.setElement(0, CbusConstants.CBUS_ACDAT);
        m.setElement(2, 0x08); // reporter 8 does not exist yet
        m.setElement(3, 0x41);
        tcis.sendToListeners(m);
        CbusReporter r8 = (CbusReporter)memo.get(ReporterManager.class).getBySystemName("MR8");
        assertNotNull(r8, "reporter 8 created");
        assertEquals(CbusReporterManager.CBUS_REPORTER_TYPE_CLASSIC, r8.getCbusReporterType(),
            "new reporter from ACDAT stays Classic");

        r7.dispose();
        r8.dispose();
    }

    @Test
    public void testGetCbusReporterType(){
        assertEquals(CbusReporterManager.CBUS_DEFAULT_REPORTER_TYPE,((CbusReporter)r).getCbusReporterType(),
            "Classic RfID default type");

        r.setProperty(CbusReporterManager.CBUS_REPORTER_DESCRIPTOR_KEY, CbusReporterManager.CBUS_REPORTER_TYPES[1]);
        assertEquals(CbusReporterManager.CBUS_REPORTER_TYPES[1],((CbusReporter)r).getCbusReporterType(),
            "type changed");
    }

    @Test
    public void testMaintainSensorDefaultSetGet(){
        assertFalse(((CbusReporter)r).getMaintainSensor(), "maintain sensor default");
        r.setProperty(CbusReporterManager.CBUS_MAINTAIN_SENSOR_DESCRIPTOR_KEY, true);
        assertTrue(((CbusReporter)r).getMaintainSensor(), "sensor maintained flag set true");
    }

    @Test
    public void testRespondToDdesRc522CanReply(){

        // a new tag provided by Reporter4 then moves to Reporter 5
        Reporter r4 = memo.get(ReporterManager.class).provideReporter("4");
        r4.setProperty(CbusReporterManager.CBUS_REPORTER_DESCRIPTOR_KEY, CbusReporterManager.CBUS_REPORTER_TYPES[1]);
        Reporter r5 = memo.get(ReporterManager.class).provideReporter("65534");
        r5.setProperty(CbusReporterManager.CBUS_REPORTER_DESCRIPTOR_KEY, CbusReporterManager.CBUS_REPORTER_TYPES[1]);

        CanReply m = new CanReply(tcis.getCanid());
        m.setNumDataElements(8);
        m.setElement(0, CbusConstants.CBUS_DDES);
        m.setElement(1, 0x00); // ev hi
        m.setElement(2, 0x04); // ev lo
        m.setElement(3, 0x00); // rc522
        m.setElement(4, 0x00); // tag ID hi
        m.setElement(5, 0x01); // tag ID lo
        m.setElement(6, 0xff); // ddes3
        m.setElement(7, 0xAB); // ddes4
        tcis.sendToListeners(m);

        assertEquals(r4.describeState(IdTag.SEEN),r4.describeState(r4.getState()), "r4 state set");
        assertNotNull(r4.getCurrentReport(), "r4 report set");
        assertEquals(r5.describeState(IdTag.UNKNOWN),r5.describeState(r5.getState()), "r5 state unset");
        assertNull(r5.getCurrentReport(), "r5 report unset");

        IdTag tag = jmri.InstanceManager.getDefault(jmri.IdTagManager.class).getByTagID("1");
        assertNotNull(tag, "tag created");

        m.setElement(1, 0xff); // ev hi
        m.setElement(2, 0xfe); // ev lo
        tcis.sendToListeners(m);

        assertEquals(r5.describeState(IdTag.SEEN),r5.describeState(r5.getState()), "r5 tag seen");
        assertNotNull(r5.getCurrentReport(), "r5 report set");
        assertEquals(r4.describeState(IdTag.UNSEEN),r4.describeState(r4.getState()), "r4 tag gone");
        assertNull(r4.getCurrentReport(), "r4 report unset");

        tcis.sendToListeners(m);
        assertEquals(r5.describeState(IdTag.SEEN),r5.describeState(r5.getState()), "r5 tag seen");
        assertNotNull(r5.getCurrentReport(), "r5 report set");


        m.setElement(4, 0xff); // tag ID hi
        m.setElement(5, 0xff); // tag ID lo
        tcis.sendToListeners(m);
        assertEquals(r5.describeState(IdTag.SEEN),r5.describeState(r5.getState()), "r5 tag seen");
        assertNotNull(r5.getCurrentReport(), "r5 report set");

        tag = jmri.InstanceManager.getDefault(jmri.IdTagManager.class).getByTagID("65535");
        assertNotNull(tag, "tag 65535 created");
        assertEquals(tag,r5.getCurrentReport(), "r5 tag seen 65535");
        assertEquals(r5,tag.getWhereLastSeen(), "r5 tag seen 65535");


        r4.dispose();
        r5.dispose();

    }

    @Test
    public void testRespondToDdesRailComCanReply(){

        // a new tag provided by Reporter4 then moves to Reporter 5
        CbusReporter r4 = new CbusReporter("4",memo);
        r4.setProperty(CbusReporterManager.CBUS_REPORTER_DESCRIPTOR_KEY, CbusReporterManager.CBUS_REPORTER_TYPE_DDES_DESCRIBING);
 
        CanReply m = new CanReply(tcis.getCanid());
        
        m.setNumDataElements(8);
        m.setElement(0, CbusConstants.CBUS_DDES);
        m.setElement(1, 0x00); // dd hi
        m.setElement(2, 0x04); // dd lo
        m.setElement(3, 0x01); // railcom

        m.setElement(6, 0x99); // ddes3
        m.setElement(7, 0x15); // ddes4

        jmri.DccLocoAddress address;
        
        m.setElement(4, 0x00); // short address 1
        m.setElement(5, 0x01);
        address = r4.parseAddress(m); 
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_SHORT, "0x0001 type");
        assertEquals(address.isConsistAddress(), false, "0x0001 consist");
        assertEquals(address.getNumber(), 1, "0x0001 number");


        m.setElement(4, 0xC0); // Long address 1
        m.setElement(5, 0x01);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_LONG,"0xC001 type");
        assertEquals(address.isConsistAddress(), false, "0x8C01 consist");
        assertEquals(address.getNumber(), 1, "0xC001 number");

        m.setElement(4, 0xE6); // Long address 9876
        m.setElement(5, 0x94);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_LONG,"0xE694 type");
        assertEquals(address.isConsistAddress(), false, "0xE694 consist");
        assertEquals(address.getNumber(), 9876, "0xE694 number");


        m.setElement(4, 0x40); // short consist 1
        m.setElement(5, 0x01);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_SHORT,"0x4001 type");
        assertEquals(address.isConsistAddress(), true, "0x4001 consist");
        assertEquals(address.getNumber(), 1,"0x4001 address");


        m.setElement(4, 0x80); // extended consist 1
        m.setElement(5, 0x01);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_LONG,"0x8001 type");
        assertEquals(address.isConsistAddress(), true, "0x8001 consist");
        assertEquals(address.getNumber(), 1,"0x8001 address");

        m.setElement(4, 0x80); // extended consist 17
        m.setElement(5, 0x11);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_LONG,"0x8011 type");
        assertEquals(address.isConsistAddress(), true, "0x8011 consist");
        assertEquals(address.getNumber(), 17,"0x8011 address");

        m.setElement(4, 0x80); // extended consist 99
        m.setElement(5, 0x63);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_LONG,"0x8063 type");
        assertEquals(address.isConsistAddress(), true, "0x8063 consist");
        assertEquals(address.getNumber(), 99,"0x8063 address");

        m.setElement(4, 0x80); // extended consist 127
        m.setElement(5, 0x7F);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_LONG,"0x807F type");
        assertEquals(address.isConsistAddress(), true, "0x807F consist");
        assertEquals(address.getNumber(), 127,"0x807F address");

        m.setElement(4, 0x81); // extended consist 227
        m.setElement(5, 0x1B);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_LONG,"0x80E3 type");
        assertEquals(address.isConsistAddress(), true, "0x80E3 consist");
        assertEquals(address.getNumber(), 227,"0x80E3 address");

        m.setElement(4, 0x81); // extended consist 363
        m.setElement(5, 0xBF);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_LONG,"0x881BF type");
        assertEquals(address.isConsistAddress(), true, "0x881BF consist");
        assertEquals(address.getNumber(), 363,"0x881BF address");

        m.setElement(4, 0xB1); // extended consist 9876
        m.setElement(5, 0x4C);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_LONG,"0x81BF type");
        assertEquals(address.isConsistAddress(), true, "0x81BF consist");
        assertEquals(address.getNumber(), 9876,"0x81BF address");
    }


    @Test
    public void testSensorFollowing() throws jmri.JmriException {

        CbusReporter r2 = new CbusReporter("2",memo);

        r.setProperty(CbusReporterManager.CBUS_MAINTAIN_SENSOR_DESCRIPTOR_KEY, true);

        assertNotNull(memo);
        jmri.SensorManager sm = memo.get(jmri.SensorManager.class);
        Sensor followerSensor = sm.getBySystemName(sm.createSystemName("+1",sm.getSystemPrefix()));
        assertNull(followerSensor, "No sensor at start");

        CbusReporterManager rm = (CbusReporterManager) memo.get(ReporterManager.class);
        rm.setTimeout(30); // ms for testing


        CanReply m = new CanReply(tcis.getCanid());
        m.setNumDataElements(8);
        m.setElement(0, CbusConstants.CBUS_DDES);
        m.setElement(1, 0x00); // ev hi
        m.setElement(2, 0x01); // ev lo
        m.setElement(3, 0x30); // tag1
        m.setElement(4, 0x39); // tag2
        m.setElement(5, 0x31); // tag3
        m.setElement(6, 0x30); // tag4
        m.setElement(7, 0xAB); // tag5
        ((CbusReporter)r).reply(m);
        tcis.sendToListeners(m);

        followerSensor = sm.getBySystemName(sm.createSystemName("+1",sm.getSystemPrefix()));
        assertNotNull(followerSensor, "Sensor created by reporter");
        assertEquals(Sensor.ACTIVE, followerSensor.getState(), "sensor active");

        m.setElement(2, 0x02); // ev lo
        tcis.sendToListeners(m);
        r2.reply(m);

        final int status = followerSensor.getState();
        JUnitUtil.waitFor(() -> {
            return (status==Sensor.INACTIVE);
        }, "sensor triggered to inactive when spot report complete");

    }

    private TrafficControllerScaffold tcis;
    private CanSystemConnectionMemo memo = null;

    // ((CbusReporterManager)memo.get(jmri.ReporterManager.class));

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcis);

        memo.setProtocol(ConfigurationManager.MERGCBUS);
        memo.configureManagers();
        r = memo.get(ReporterManager.class).provide("1");
    }

    @AfterEach
    @Override
    public void tearDown() {
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).dispose();
        jmri.InstanceManager.getDefault(jmri.RailComManager.class).dispose();
        if (r!=null) {
            r.dispose();
        }
        assertNotNull(memo);
        memo.dispose();
        memo = null;
        tcis.terminateThreads();
        tcis = null;
        r = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();

    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusReporterTest.class);

}
