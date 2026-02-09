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
        assertEquals(address.getNumber(), 1, "0x0001 number");


        m.setElement(4, 0xC0); // Long address 1
        m.setElement(5, 0x01);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_LONG,"0xC001 type");
        assertEquals(address.getNumber(), 1, "0xC001 number");

        m.setElement(4, 0xE6); // Long address 9876
        m.setElement(5, 0x94);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_LONG,"0xE694 type");
        assertEquals(address.getNumber(), 9876, "0xE694 number");


        m.setElement(4, 0x40); // short consist 1
        m.setElement(5, 0x01);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_CONSIST,"0x4001 type");
        assertEquals(address.getNumber(), 1,"0x4001 address");


        m.setElement(4, 0x80); // extended consist 1
        m.setElement(5, 0x01);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_EXTENDED_CONSIST,"0x8001 type");
        assertEquals(address.getNumber(), 1,"0x8001 address");

        m.setElement(4, 0x80); // extended consist 17
        m.setElement(5, 0x11);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_EXTENDED_CONSIST,"0x8011 type");
        assertEquals(address.getNumber(), 17,"0x8011 address");

        m.setElement(4, 0x80); // extended consist 99
        m.setElement(5, 0x63);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_EXTENDED_CONSIST,"0x8063 type");
        assertEquals(address.getNumber(), 99,"0x8063 address");

        m.setElement(4, 0x80); // extended consist 127
        m.setElement(5, 0x7F);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_EXTENDED_CONSIST,"0x807F type");
        assertEquals(address.getNumber(), 127,"0x807F address");

        m.setElement(4, 0x80); // extended consist 227
        m.setElement(5, 0xE3);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_EXTENDED_CONSIST,"0x80E3 type");
        assertEquals(address.getNumber(), 227,"0x80E3 address");

        m.setElement(4, 0xB1); // extended consist 9876
        m.setElement(5, 0xC3);
        address = r4.parseAddress(m);
        assertEquals(address.getProtocol(), jmri.DccLocoAddress.Protocol.DCC_EXTENDED_CONSIST,"0xB1C3 type");
        assertEquals(address.getNumber(), 9876,"0xB1C3 address");
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

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusReporterTest.class);

}
