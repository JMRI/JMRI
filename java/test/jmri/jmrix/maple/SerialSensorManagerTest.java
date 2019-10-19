package jmri.jmrix.maple;

import jmri.Sensor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the Maple SerialSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2008
 */
public class SerialSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private MapleSystemConnectionMemo memo = null;

    @Override
    public String getSystemName(int i) {
        return "KS" + i;
    }

    @Test
    public void testConstructor() {
        // create and register the manager object
        SerialSensorManager atm = new SerialSensorManager(new MapleSystemConnectionMemo());
        Assert.assertNotNull("Maple Sensor Manager creation", atm);
    }

    @Test
    public void testSensorCreationAndRegistration() {

        Sensor sensor = l.provideSensor("3");
        Assert.assertNotNull("found sensor", sensor);
        Assert.assertTrue("right name", sensor.getSystemName().equals("KS3"));
        Sensor s11 = l.provideSensor("11");
        Assert.assertNotNull("found s11", s11);
        Assert.assertTrue("right name s11", s11.getSystemName().equals("KS11"));
        InputBits.setNumInputBits(1000);
        Sensor s248 = l.provideSensor("KS248");
        Assert.assertNotNull("found s248", s248);
        Assert.assertTrue("right name s248", s248.getSystemName().equals("KS248"));
        Sensor s1000 = l.provideSensor("1000");
        Assert.assertNotNull("found s1000", s1000);
        Assert.assertTrue("right name s1000", s1000.getSystemName().equals("KS1000"));
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // replace SerialSensorManager to make sure nodes start
        // at the beginning
        SerialTrafficController tc = new SerialTrafficControlScaffold();
        memo = new MapleSystemConnectionMemo("K", "Maple");
        memo.setTrafficController(tc);
        // create and register the turnout manager object
        l = new SerialSensorManager(memo);
//        jmri.InstanceManager.setSensorManager(l);
//        SerialNode n1 = new SerialNode(1,0);
//        SerialNode n2 = new SerialNode(2,0);
    }

    @After
    public void tearDown() {
        memo.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
