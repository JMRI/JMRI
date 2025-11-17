package jmri.jmrix.loconet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.loconet.LnSensorManagerTurnout class.
 *
 * @author Bob Jacobsen Copyright 2001
 */
public class LnSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private LocoNetInterfaceScaffold lnis = null;

    @Override
    public String getSystemName(int i) {
        return "LS" + i;
    }

    @Test
    public void testLnSensorCreate() {
        assertNotNull( l, "exists");
    }

    @Test
    public void testByAddress() {
        // sample turnout object
        Sensor t = l.newSensor("LS22", "test");

        // test get
        assertSame( t, l.getByUserName("test"));
        assertSame( t, l.getBySystemName("LS22"));
    }

    @Test
    @Override
    public void testMisses() {
        // sample turnout object
        Sensor t = l.newSensor("LS22", "test");
        assertNotNull( t, "exists");

        // try to get nonexistant turnouts
        assertNull( l.getByUserName("foo"));
        assertNull( l.getBySystemName("bar"));
    }

    @Test
    public void testLocoNetMessages() {
        // send messages for 21, 22
        // notify the Ln that somebody else changed it...
        LocoNetMessage m1 = new LocoNetMessage(4);
        m1.setOpCode(0xb2);         // OPC_INPUT_REP
        m1.setElement(1, 0x15);     // all but lowest bit of address
        m1.setElement(2, 0x60);     // Aux (low addr bit high), sensor high
        m1.setElement(3, 0x38);
        lnis.sendTestMessage(m1);

        // see if sensor exists
        assertNotNull( l.getBySystemName("LS44"));
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Sensor, and check type
        SensorManager t = jmri.InstanceManager.sensorManagerInstance();

        Sensor o = t.newSensor("LS21", "my name");

        // log.debug("created sensor {}", o);
        
        assertNotNull(o);
        assertInstanceOf( LnSensor.class, o);

        // make sure loaded into tables

        assertNotNull( t.getBySystemName("LS21"));
        assertNotNull( t.getByUserName("my name"));

    }

    @Test
    public void testDeprecationWarningSensorNumberFormat() {
        String s = assertDoesNotThrow( () ->
            l.createSystemName("3:5", "L"),
            "no exception during createSystemName for arguments '3:5', 'L'");
        assertEquals( "LS37", s, "check createSystemName for arguments '3:5', 'L'");
        jmri.util.JUnitAppender.assertWarnMessage(
                "LnSensorManager.createSystemName(curAddress, prefix) support for curAddress using the '3:5' format is deprecated as of JMRI 4.17.4 and will be removed in a future JMRI release.  Use the curAddress format '37' instead.");
    }

    @Test
    public void testSetGetRestingTime() {
        assertEquals( 1250, ((LnSensorManager)l).getRestingTime(), "check default resting time");

        ((LnSensorManager)l).setRestingTime(600);
        assertEquals( 600, ((LnSensorManager)l).getRestingTime(), "check 1st set of resting time");

        ((LnSensorManager)l).setRestingTime(500);
        assertEquals( 500, ((LnSensorManager)l).getRestingTime(), "check 2nd set of resting time");

        ((LnSensorManager)l).setRestingTime(499);
        assertEquals( 500, ((LnSensorManager)l).getRestingTime(), "check 1st range check on set of resting time");

        ((LnSensorManager)l).setRestingTime(200001);
        assertEquals( 200000, ((LnSensorManager)l).getRestingTime(), "check 2nd range check on set of resting time");
        
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        assertNotNull( lnis, "exists");

        // create and register the manager object
        l = new LnSensorManager(memo, false);
        jmri.InstanceManager.setSensorManager(l);
    }
    
    @AfterEach
    public void tearDown() {
        l.dispose();
        lnis = null;
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnSensorManagerTest.class);

}
