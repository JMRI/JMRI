package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.io.IOLine;
import com.digi.xbee.api.io.IOValue;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.exceptions.InterfaceNotOpenException;
import com.digi.xbee.api.exceptions.TimeoutException;
import jmri.Sensor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * XBeeSensorManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeSensorManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class XBeeSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private XBeeTrafficController tc = null;

    @Override
    public String getSystemName(int i) {
        return "AS2:" + i;
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", l);
    }

    @Override
    @Test
    public void testProvideName() {
        // create
        Sensor t = l.provide(getSystemName(getNumToTest1()));
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("system name correct ", t ,l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testProvideIdStringName() {
        // create
        Sensor t = l.provide("ASNode 1:2");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("correct object returned ", t ,l.getBySystemName("ASNODE 1:2"));
    }

    @Test
    public void testProvide16BitAddress() {
        // create
        Sensor t = l.provide("AS00 02:2");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("system name correct ", t,l.getBySystemName("AS00 02:2"));
    }

    @Test
    public void testProvide64BitAddress() {
        // create
        Sensor t = l.provide("AS00 13 A2 00 40 A0 4D 2D:2");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("system name correct ", t ,l.getBySystemName("AS00 13 A2 00 40 A0 4D 2D:2"));
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Sensor t = l.provideSensor(getSystemName(getNumToTest1()));
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("system name correct ", t, l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Ignore
    @Test
    public void testUpperLower() { // ignoring this test due to the system name format, needs to be properly coded
    }

    @Test
    public void testMoveUserName() {
        Sensor t1 = l.provideSensor(getSystemName(getNumToTest1()));
        Sensor t2 = l.provideSensor(getSystemName(getNumToTest2()));
        t1.setUserName("UserName");
        Assert.assertTrue(t1 == l.getByUserName("UserName"));
        
        t2.setUserName("UserName");
        Assert.assertTrue(t2 == l.getByUserName("UserName"));

        Assert.assertTrue(null == t1.getUserName());
    }

    @Override
    @Test
    public void testPullResistanceConfigurable(){
       Assert.assertTrue("Pull Resistance Configurable",l.isPullResistanceConfigurable());
    }



    // The minimal setup for log4J
    @Override
    @Before 
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        // setup the mock XBee Connection.
        tc = new XBeeInterfaceScaffold();

        XBeeConnectionMemo m = new XBeeConnectionMemo();
        m.setSystemPrefix("A");
        tc.setAdapterMemo(m);
        l = new XBeeSensorManager(tc, "A");
        m.setSensorManager(l);
        byte pan[] = {(byte) 0x00, (byte) 0x42};
        byte uad[] = {(byte) 0x00, (byte) 0x02};
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        XBeeNode node = new XBeeNode(pan,uad,gad);
        RemoteXBeeDevice rd = new RemoteXBeeDevice(tc.getXBee(),
             new XBee64BitAddress("0013A20040A04D2D"),
             new XBee16BitAddress("0002"),
             "Node 1"){
            @Override
            public IOValue getDIOValue(IOLine l) throws InterfaceNotOpenException,TimeoutException,XBeeException {
               return IOValue.LOW;
            }
        };
        node.setXBee(rd);
        tc.registerNode(node);
    }

    @After
    public void tearDown() {
        tc.terminate();
        jmri.util.JUnitUtil.tearDown();
    }

}


