package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.io.IOLine;
import com.digi.xbee.api.io.IOValue;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.exceptions.InterfaceNotOpenException;
import com.digi.xbee.api.exceptions.TimeoutException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import jmri.Sensor;
import jmri.util.JUnitAppender;
import org.apache.log4j.Level;
import org.junit.*;
import jmri.util.junit.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @Test
    public void testUpperLower() {
        Sensor t = l.provideSensor(getSystemName(getNumToTest2()));
        String name = t.getSystemName();
        
        int prefixLength = l.getSystemPrefix().length()+1;     // 1 for type letter
        String lowerName = name.substring(0,prefixLength)+name.substring(prefixLength, name.length()).toLowerCase();
        
        Assert.assertEquals(t, l.getSensor(lowerName));
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

    @Override
    @Test
    public void testRegisterDuplicateSystemName() throws PropertyVetoException, NoSuchFieldException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
       String s1 = l.makeSystemName("00 02:1");
       String s2 = l.makeSystemName("00 02:2");
       Assert.assertNotNull(s1);
       Assert.assertFalse(s1.isEmpty());
       Assert.assertNotNull(s2);
       Assert.assertFalse(s2.isEmpty());

       Sensor e1;
       Sensor e2;

       try {
          e1 = l.provide(s1);
          e2 = l.provide(s2);
       } catch (IllegalArgumentException | NullPointerException | ArrayIndexOutOfBoundsException ex) {
          // jmri.jmrix.openlcb.OlcbLightManagerTest gives a NullPointerException here.
          // jmri.jmrix.openlcb.OlcbSensorManagerTest gives a ArrayIndexOutOfBoundsException here.
          // Some other tests give an IllegalArgumentException here.

          // If the test is unable to provide a named bean, abort this test.
          JUnitAppender.clearBacklog(Level.WARN);
          log.debug("Cannot provide a named bean", ex);
          Assume.assumeTrue("We got no exception", false);
          return;
       }

       // Use reflection to change the systemName of e2
       // Try to find the field
       Field f1 = getField(e2.getClass(), "mSystemName");
       f1.setAccessible(true);
       f1.set(e2, e1.getSystemName());

       // Remove bean if it's already registered
       if (l.getBeanBySystemName(e1.getSystemName()) != null) {
          l.deregister(e1);
       }
       // Remove bean if it's already registered
       if (l.getBeanBySystemName(e2.getSystemName()) != null) {
          l.deregister(e2);
       }

       // Register the bean once. This should be OK.
       l.register(e1);

       // Register bean twice. This gives only a debug message.
       l.register(e1);

       String expectedMessage = "systemName is already registered: " + e1.getSystemName();
       boolean hasException = false;
       try {
          // Register different bean with existing systemName.
          // This should fail with an IllegalArgumentException.
          l.register(e2);
       } catch (IllegalArgumentException ex) {
          hasException = true;
          Assert.assertTrue("exception message is correct",
             expectedMessage.equals(ex.getMessage()));
          JUnitAppender.assertErrorMessage(expectedMessage);
       }
       Assert.assertTrue("exception is thrown", hasException);

       l.deregister(e1);
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

    private final static Logger log = LoggerFactory.getLogger(XBeeSensorManagerTest.class);

}


