package jmri.jmrix.openlcb;

import jmri.Sensor;
import jmri.SensorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010
 */
public class OlcbSensorManagerTest extends jmri.managers.AbstractSensorMgrTest {

    @Override
    public String getSystemName(int i) {
        return "MSX00000" + i;
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists",l);
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        // olcb addresses are hex values requirng 6 digits.
        Sensor t = l.provideSensor("MSx00000" + getNumToTest1());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct " + t.getSystemName(), t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Test
    public void testUpperLower() {
        // olcb addresses are hex values requirng 6 digits.
        Sensor t = l.provideSensor("MSx00000" + getNumToTest2());
        String name = t.getSystemName();
        Assert.assertNull(l.getSensor(name.toLowerCase()));
    }



    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();

        OlcbSystemConnectionMemo m = new OlcbSystemConnectionMemo();
        m.setTrafficController(new jmri.jmrix.can.TestTrafficController());
        l = new OlcbSensorManager(m);

    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    


}
