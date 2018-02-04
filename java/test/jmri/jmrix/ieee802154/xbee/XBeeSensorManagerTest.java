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
        return "ABCS2:" + i;
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", l);
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Sensor t = l.provideSensor("ABCS2:" + getNumToTest1());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Test
    public void testUpperLower() {
        Sensor t = l.provideSensor("ABCS2:" + getNumToTest2());
        String name = t.getSystemName();
        Assert.assertNull(l.getSensor(name.toLowerCase()));
    }

    @Test
    public void testMoveUserName() {
        Sensor t1 = l.provideSensor("ABCS2:" + getNumToTest1());
        Sensor t2 = l.provideSensor("ABCS2:" + getNumToTest2());
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
        m.setSystemPrefix("ABC");
        tc.setAdapterMemo(m);
        l = new XBeeSensorManager(tc, "ABC");
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


