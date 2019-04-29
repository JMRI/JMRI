package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import jmri.Turnout;
import jmri.util.JUnitAppender;
import org.apache.log4j.Level;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XBeeTurnoutManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeTurnoutManager
 * class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class XBeeTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    XBeeTrafficController tc = null;

    @Override
    public String getSystemName(int i){
       return "AT2:" +i;
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testProvideName() {
        // create
        Turnout t = l.provide("" + (getSystemName(getNumToTest1())));
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("system name correct ", t,l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testProvideIdStringName() {
        // create
        Turnout t = l.provide("ATNode 1:2");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("correct object returned ", t ,l.getBySystemName("ATNode 1:2"));
    }

    @Test
    public void testProvide16BitAddress() {
        // create
        Turnout t = l.provide("AT00 02:2");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("system name correct ", t,l.getBySystemName("AT00 02:2"));
    }

    @Test
    public void testProvide64BitAddress() {
        // create
        Turnout t = l.provide("AT00 13 A2 00 40 A0 4D 2D:2");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("system name correct ", t , l.getBySystemName("AT00 13 A2 00 40 A0 4D 2D:2"));
    }


    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Turnout t = l.provideTurnout(getSystemName(getNumToTest1()));
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("system name correct ", t , l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Test
    public void testUpperLower() {
        Turnout t = l.provideTurnout(getSystemName(getNumToTest2()));

        Assert.assertNull(l.getTurnout(t.getSystemName().toLowerCase()));
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

       Turnout e1;
       Turnout e2;

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
        tc = new XBeeInterfaceScaffold();
        XBeeConnectionMemo m = new XBeeConnectionMemo();
        m.setSystemPrefix("A");
        tc.setAdapterMemo(m);
        l = new XBeeTurnoutManager(tc, "A");
        m.setTurnoutManager(l);
        byte pan[] = {(byte) 0x00, (byte) 0x42};
        byte uad[] = {(byte) 0x00, (byte) 0x02};
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        XBeeNode node = new XBeeNode(pan,uad,gad);
        RemoteXBeeDevice rd = new RemoteXBeeDevice(tc.getXBee(),
             new XBee64BitAddress("0013A20040A04D2D"),
             new XBee16BitAddress("0002"),
             "Node 1");
        node.setXBee(rd);
        tc.registerNode(node);
    }

    @After
    public void tearDown() {
        tc.terminate();
        jmri.util.JUnitUtil.tearDown();
    }


    private final static Logger log = LoggerFactory.getLogger(XBeeTurnoutManagerTest.class);

}
