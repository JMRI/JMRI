package jmri.jmrix.loconet.hexfile;

import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.loconet.hexfile.LnSensorManagerTurnout class.
 *
 * @author	Bob Jacobsen Copyright 2001
 */
public class LnSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private LocoNetInterfaceScaffold lnis = null;

    @Override
    public String getSystemName(int i) {
        return "LS" + i;
    }

    @Test
    public void testLnSensorCreate() {
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testByAddress() {
        // sample turnout object
        Sensor t = l.newSensor("LS22", "test");

        // test get
        Assert.assertTrue(t == l.getByUserName("test"));
        Assert.assertTrue(t == l.getBySystemName("LS22"));
    }

    @Test
    @Override
    public void testMisses() {
        // sample turnout object
        Sensor t = l.newSensor("LS22", "test");
        Assert.assertNotNull("exists", t);

        // try to get nonexistant turnouts
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
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
        Assert.assertNotNull(l.getBySystemName("LS44"));
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Sensor, and check type
        SensorManager t = jmri.InstanceManager.sensorManagerInstance();

        Sensor o = t.newSensor("LS21", "my name");

        Assert.assertNotNull(o);

        Assert.assertNotNull(t.getBySystemName("LS21"));
        Assert.assertNotNull(t.getByUserName("my name"));

    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnSensorManagerTest.class);

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        Assert.assertNotNull("exists", lnis);

        // create and register the manager object
        l = new LnSensorManager(memo);
        jmri.InstanceManager.setSensorManager(l);
    }

    @After
    public void tearDown() {
        l.dispose();
        lnis = null;
        JUnitUtil.tearDown();
    }

}
