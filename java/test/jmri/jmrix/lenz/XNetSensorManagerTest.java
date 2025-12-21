package jmri.jmrix.lenz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.lenz.XNetSensorManager class.
 *
 * @author Paul Bender Copyright (c) 2003
 */
public class XNetSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private XNetInterfaceScaffold xnis;

    @Override
    public String getSystemName(int i) {
        return "XS" + i;
    }

    @Test
    public void testXNetCTor() {
        assertNotNull(l);
    }

    @Test
    public void testByAddress() {
        // sample sensor object
        Sensor t = l.newSensor("XS22", "test");

        // test get
        assertSame(t, l.getByUserName("test"));
        assertSame(t, l.getBySystemName("XS22"));
    }

    @Test
    @Override
    public void testMisses() {
        // sample turnout object
        Sensor s = l.newSensor("XS22", "test");
        assertNotNull( s, "exists");

        // try to get nonexistant turnouts
        assertNull(l.getByUserName("foo"));
        assertNull(l.getBySystemName("bar"));
    }

    @Test
    public void testXNetMessages() {
        // send messages for feedbak encoder 22
        // notify the XpressNet that somebody else changed it...
        XNetReply m1 = new XNetReply();
        m1.setElement(0, 0x42);     // Opcode for feedback response
        m1.setElement(1, 0x02);     // The feedback encoder address
        m1.setElement(2, 0x51);     // A bit pattern telling which
        // bits of the upper nibble
        // are on in the message.
        m1.setElement(3, 0x11);     // The XOR of everything above
        xnis.sendTestMessage(m1);

        // see if sensor exists
        assertNotNull(l.getBySystemName("XS22"));
    }

    @Test
    public void testAsAbstractFactory() {
        jmri.InstanceManager.setSensorManager(l);

        // ask for a Sensor, and check type
        SensorManager t = jmri.InstanceManager.sensorManagerInstance();

        Sensor o = t.newSensor("XS21", "my name");

        assertNotNull(o);

        // make sure loaded into tables

        assertNotNull(t.getBySystemName("XS21"));
        assertNotNull(t.getByUserName("my name"));

    }

    @Test
    public void testGetSystemPrefix() {
        assertEquals( "X", l.getSystemPrefix(), "prefix");
    }

    @Test
    public void testAllowMultipleAdditions() {
        assertTrue(l.allowMultipleAdditions("foo"));
    }

    @Test
    public void testProvideAddressAndPin() {
        assertNotNull( l.provideSensor("XS99:3"), "Sensor XS99:3 provided");
    }

    // from here down is testing infrastructure

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        xnis = new XNetInterfaceScaffold(new LenzCommandStation());
        // create and register the manager object
        l = new XNetSensorManager(xnis.getSystemConnectionMemo());
    }

    @AfterEach
    public void tearDown() {
        l.dispose();
        xnis.terminateThreads();
        l = null;
        xnis = null;
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XNetSensorManagerTest.class);

}
