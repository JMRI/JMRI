package jmri.jmrix.ieee802154.xbee;

import jmri.Sensor;
import jmri.SensorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import com.digi.xbee.api.connection.IConnectionInterface;
import com.digi.xbee.api.exceptions.OperationNotSupportedException;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.models.XBeeProtocol;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.RemoteXBeeDevice;


import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
@MockPolicy(Slf4jMockPolicy.class)

/**
 * XBeeSensorManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeSensorManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
@RunWith(PowerMockRunner.class)
public class XBeeSensorManagerTest extends jmri.managers.AbstractSensorMgrTest {

    private static final String NODE_ID = "id";
        
    private XBeeDevice localDevice;	
    private RemoteXBeeDevice remoteDevice1;

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


    // The minimal setup for log4J
    @Override
    @Before 
    public void setUp() {
        jmri.util.JUnitUtil.resetInstanceManager();

        // setup the mock XBee Connection.
        // Mock the local device.
        localDevice = PowerMockito.mock(XBeeDevice.class);
        Mockito.when(localDevice.getConnectionInterface()).thenReturn(Mockito.mock(IConnectionInterface.class));
        Mockito.when(localDevice.getXBeeProtocol()).thenReturn(XBeeProtocol.ZIGBEE);
        // Mock the remote device 1.
        remoteDevice1 = Mockito.mock(RemoteXBeeDevice.class);
        Mockito.when(remoteDevice1.getXBeeProtocol()).thenReturn(XBeeProtocol.UNKNOWN);
        Mockito.when(remoteDevice1.getNodeID()).thenReturn(NODE_ID);
        Mockito.when(remoteDevice1.get64BitAddress()).thenReturn(new XBee64BitAddress("0013A20040A04D2D"));
        Mockito.when(remoteDevice1.get16BitAddress()).thenReturn(new XBee16BitAddress("0002"));



        XBeeTrafficController tc = new XBeeTrafficController() {
            public void setInstance() {
            }
            public void sendXBeeMessage(XBeeMessage m,XBeeListener l){
            }
            public XBeeDevice getXBee() {
               return localDevice;
            }
        };
        XBeeConnectionMemo m = new XBeeConnectionMemo();
        m.setSystemPrefix("ABC");
        tc.setAdapterMemo(m);
        l = new XBeeSensorManager(tc, "ABC");
        m.setSensorManager(l);
        byte pan[] = {(byte) 0x00, (byte) 0x42};
        byte uad[] = {(byte) 0x00, (byte) 0x02};
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        XBeeNode node = new XBeeNode(pan,uad,gad);
        node.setXBee(remoteDevice1);
        tc.registerNode(node);

    }

    @After
    public void tearDown() {
        //l.dispose();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

}


