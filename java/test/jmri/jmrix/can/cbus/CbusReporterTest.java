package jmri.jmrix.can.cbus;

import jmri.IdTag;
import jmri.jmrix.can.*;
import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;

import org.junit.Assert;
import org.junit.jupiter.api.*;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

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
        CbusReporter r4 = new CbusReporter("4",memo);
        CbusReporter r5 = new CbusReporter("5",memo);

        Assert.assertNotEquals("messages should be different",
   r.describeState(IdTag.UNSEEN), r.describeState(IdTag.SEEN));

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
        r4.reply(m);
        r5.reply(m);

        // tag unseen = 2
        // tag seen = 3

        Assert.assertEquals("r4 state set",r4.describeState(IdTag.SEEN),r4.describeState(r4.getState()));
        Assert.assertNotNull("r4 report set",r4.getCurrentReport());
        Assert.assertEquals("r5 state unset",r5.describeState(IdTag.UNKNOWN),r5.describeState(r5.getState()));
        Assert.assertEquals("r5 report unset",null,r5.getCurrentReport());

        m.setElement(2, 0x05); // ev lo
        r4.reply(m);
        r5.reply(m);

        Assert.assertEquals("r5 tag seen",r5.describeState(IdTag.SEEN),r5.describeState(r5.getState()));
        Assert.assertNotNull("r5 report set",r5.getCurrentReport());
        Assert.assertEquals("r4 tag gone",r4.describeState(IdTag.UNSEEN),r4.describeState(r4.getState()));
        Assert.assertEquals("r4 report unset",null,r4.getCurrentReport());

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

        r4.reply(m2);
        r5.reply(m2);

        Assert.assertEquals("r4 state set CBUS_ACDAT",r4.describeState(IdTag.SEEN),r4.describeState(r4.getState()));
        Assert.assertNotNull("r4 report set CBUS_ACDAT",r4.getCurrentReport());
        Assert.assertEquals("r5 state unset CBUS_ACDAT",r5.describeState(IdTag.UNSEEN),r5.describeState(r5.getState()));
        Assert.assertEquals("r5 report unset CBUS_ACDAT",null,r5.getCurrentReport());

        m2.setElement(2, 0x05); // ev lo

        m2.setExtended(true);
        r5.reply(m2);
        Assert.assertEquals("r5 state unset extended",r5.describeState(IdTag.UNSEEN),r5.describeState(r5.getState()));

        m2.setExtended(false);
        m2.setRtr(true);
        r5.reply(m2);
        Assert.assertEquals("r5 state unset rtr",r5.describeState(IdTag.UNSEEN),r5.describeState(r5.getState()));

        m2.setRtr(false);
        m2.setElement(0, 0x05); // random OPC not related to reporters
        r5.reply(m2);
        Assert.assertEquals("r5 state unset random opc",r5.describeState(IdTag.UNSEEN),r5.describeState(r5.getState()));

        m2.setElement(0, CbusConstants.CBUS_DDES); // put it back
        r5.reply(m2);
        Assert.assertEquals("r5 state set ok after incorrect msgs",r5.describeState(IdTag.SEEN),r5.describeState(r5.getState()));

        Assert.assertEquals("r4 state unseen",r4.describeState(IdTag.UNSEEN),r4.describeState(r4.getState()));

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

        r4.message(m3);

        Assert.assertEquals("r4 seen after CBUS_ACDAT outgoing message",IdTag.SEEN,r4.getState());

        r4.dispose();
        r5.dispose();
    }

    @Test
    public void testGetCbusReporterType(){
        Assert.assertEquals("Classic RfID default type",CbusReporterManager.CBUS_DEFAULT_REPORTER_TYPE,((CbusReporter)r).getCbusReporterType());

        r.setProperty(CbusReporterManager.CBUS_REPORTER_DESCRIPTOR_KEY, CbusReporterManager.CBUS_REPORTER_TYPES[1]);
        Assert.assertEquals("type changed",CbusReporterManager.CBUS_REPORTER_TYPES[1],((CbusReporter)r).getCbusReporterType());
    }

    @Test
    public void testMaintainSensorDefaultSetGet(){
        Assert.assertFalse("maintain sensor default",((CbusReporter)r).getMaintainSensor());
        r.setProperty(CbusReporterManager.CBUS_MAINTAIN_SENSOR_DESCRIPTOR_KEY, true);
        Assert.assertTrue("sensor maintained flag set true",((CbusReporter)r).getMaintainSensor());
    }

    @Test
    public void testRespondToDdesRc522CanReply(){

        // a new tag provided by Reporter4 then moves to Reporter 5
        CbusReporter r4 = new CbusReporter("4",memo);
        r4.setProperty(CbusReporterManager.CBUS_REPORTER_DESCRIPTOR_KEY, CbusReporterManager.CBUS_REPORTER_TYPES[1]);
        CbusReporter r5 = new CbusReporter("65534",memo);
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
        r4.reply(m);
        r5.reply(m);

        Assert.assertEquals("r4 state set",r4.describeState(IdTag.SEEN),r4.describeState(r4.getState()));
        Assert.assertNotNull("r4 report set",r4.getCurrentReport());
        Assert.assertEquals("r5 state unset",r5.describeState(IdTag.UNKNOWN),r5.describeState(r5.getState()));
        Assert.assertNull("r5 report unset",r5.getCurrentReport());

        IdTag tag = jmri.InstanceManager.getDefault(jmri.IdTagManager.class).getByTagID("1");
        Assert.assertNotNull("tag created",tag);

        m.setElement(1, 0xff); // ev hi
        m.setElement(2, 0xfe); // ev lo
        r4.reply(m);
        r5.reply(m);

        Assert.assertEquals("r5 tag seen",r5.describeState(IdTag.SEEN),r5.describeState(r5.getState()));
        Assert.assertNotNull("r5 report set",r5.getCurrentReport());
        Assert.assertEquals("r4 tag gone",r4.describeState(IdTag.UNSEEN),r4.describeState(r4.getState()));
        Assert.assertNull("r4 report unset",r4.getCurrentReport());

        r5.reply(m); // same tag
        Assert.assertEquals("r5 tag seen",r5.describeState(IdTag.SEEN),r5.describeState(r5.getState()));
        Assert.assertNotNull("r5 report set",r5.getCurrentReport());


        m.setElement(4, 0xff); // tag ID hi
        m.setElement(5, 0xff); // tag ID lo
        r5.reply(m); // same tag
        Assert.assertEquals("r5 tag seen",r5.describeState(IdTag.SEEN),r5.describeState(r5.getState()));
        Assert.assertNotNull("r5 report set",r5.getCurrentReport());

        tag = jmri.InstanceManager.getDefault(jmri.IdTagManager.class).getByTagID("65535");
        Assert.assertNotNull("tag 65535 created",tag);
        Assert.assertEquals("r5 tag seen 65535",tag,r5.getCurrentReport());
        Assert.assertEquals("r5 tag seen 65535",r5,tag.getWhereLastSeen());


        r4.dispose();
        r5.dispose();

    }


    @Test
    public void testSensorFollowing() throws jmri.JmriException {

        CbusReporter r2 = new CbusReporter("2",memo);

        r.setProperty(CbusReporterManager.CBUS_MAINTAIN_SENSOR_DESCRIPTOR_KEY, true);

        jmri.SensorManager sm = memo.get(jmri.SensorManager.class);
        jmri.Sensor followerSensor = sm.getBySystemName(sm.createSystemName("+1",sm.getSystemPrefix()));
        Assert.assertNull("No sensor at start",followerSensor);

        CbusReporterManager rm = (CbusReporterManager) memo.get(jmri.ReporterManager.class);
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
        r2.reply(m);

        followerSensor = sm.getBySystemName(sm.createSystemName("+1",sm.getSystemPrefix()));
        Assert.assertNotNull("Sensor created by reporter",followerSensor);
        Assert.assertEquals("sensor active", jmri.Sensor.ACTIVE, followerSensor.getState());

        m.setElement(2, 0x02); // ev lo
        ((CbusReporter)r).reply(m);
        r2.reply(m);

        final int status = followerSensor.getState();
        JUnitUtil.waitFor(() -> {
            return (status==jmri.Sensor.INACTIVE);
        }, "sensor triggered to inactive when spot report complete");

    }

    private TrafficControllerScaffold tcis;
    private CanSystemConnectionMemo memo;

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
        r = new CbusReporter("1", memo);
    }

    @AfterEach
    @Override
    public void tearDown() {
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).dispose();
        if (r!=null) {
            r.dispose();
        }
        memo.dispose();
        memo = null;
        tcis.terminateThreads();
        tcis = null;
        r = null;
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusReporterTest.class);

}
