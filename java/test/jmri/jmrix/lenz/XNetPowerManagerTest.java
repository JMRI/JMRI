package jmri.jmrix.lenz;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XNetPowerManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetPowerManager class
 *
 * @author	Paul Bender
 */
public class XNetPowerManagerTest {

    private XNetPowerManager pm = null;
    private XNetInterfaceScaffold tc = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull(pm);
    }

    @Test
    public void testGetUserName() {
        Assert.assertEquals("User Name","XPressnet",pm.getUserName());
    }

    @Test
    public void testGetPower(){
       Assert.assertEquals("Power",jmri.PowerManager.UNKNOWN,pm.getPower());
    }

    @Test
    public void testSetPowerON(){
      try {
          pm.setPower(jmri.PowerManager.ON);
      } catch(jmri.JmriException je){
          Assert.fail("Failed to set Power ON");
      }
      // we should still see unknown, until a reply is received.
      Assert.assertEquals("Power",jmri.PowerManager.UNKNOWN,pm.getPower());
      // check that we actually sent a message.
      Assert.assertEquals("Message Sent",2,tc.outbound.size());
      // send the reply.
      XNetReply m = new XNetReply();
      m.setElement(0, 0x61);
      m.setElement(1, 0x01);
      m.setElement(2, 0x60);

      //tc.sendTestMessage(m);
      pm.message(m);
      // and now verify power is set the right way.
      Assert.assertEquals("Power",jmri.PowerManager.ON,pm.getPower());
    }

    @Test
    public void testSetPowerOFF(){
      try {
          pm.setPower(jmri.PowerManager.ON);
      } catch(jmri.JmriException je){
          Assert.fail("Failed to set Power ON");
      }
      // we should still see unknown, until a reply is received.
      Assert.assertEquals("Power",jmri.PowerManager.UNKNOWN,pm.getPower());
      // check that we actually sent a message.
      Assert.assertEquals("Message Sent",2,tc.outbound.size());
      // send the reply.
      XNetReply m = new XNetReply();
      m.setElement(0, 0x61);
      m.setElement(1, 0x00);
      m.setElement(2, 0x61);

      pm.message(m);
      // and now verify power is set the right way.
      Assert.assertEquals("Power",jmri.PowerManager.OFF,pm.getPower());
    }

    @Test
    public void testReceiveEmergencyStop(){
      // we should still see unknown, until a reply is received.
      Assert.assertEquals("Power",jmri.PowerManager.UNKNOWN,pm.getPower());

      // send the reply.
      XNetReply m = new XNetReply();
      m.setElement(0, 0x81);
      m.setElement(1, 0x00);
      m.setElement(2, 0x81);

      pm.message(m);
      // and now verify power is off.
      Assert.assertEquals("Power",jmri.PowerManager.OFF,pm.getPower());
    }

    @Test
    public void testReceiveServiceModeEntry(){
      // we should still see unknown, until a reply is received.
      Assert.assertEquals("Power",jmri.PowerManager.UNKNOWN,pm.getPower());

      // send the reply.
      XNetReply m = new XNetReply();
      m.setElement(0, 0x61);
      m.setElement(1, 0x02);
      m.setElement(2, 0x63);

      pm.message(m);
      // and now verify power is off.
      Assert.assertEquals("Power",jmri.PowerManager.OFF,pm.getPower());
    }

    @Test
    public void testReceiveStatusResponse(){
      // we should still see unknown, until a reply is received.
      Assert.assertEquals("Power",jmri.PowerManager.UNKNOWN,pm.getPower());

      // send the reply.
      XNetReply m = new XNetReply();
      m.setElement(0, 0x62);
      m.setElement(1, 0x22);
      m.setElement(2, 0x00);
      m.setElement(3, 0x40);

      pm.message(m);
      // and now verify power is on.
      Assert.assertEquals("Power",jmri.PowerManager.ON,pm.getPower());
    }

    @Test
    public void testReceiveStatusResponseInEmergencyOffMode(){
      // we should still see unknown, until a reply is received.
      Assert.assertEquals("Power",jmri.PowerManager.UNKNOWN,pm.getPower());

      // send the reply.
      XNetReply m = new XNetReply();
      m.setElement(0, 0x62);
      m.setElement(1, 0x22);
      m.setElement(2, 0x01);
      m.setElement(3, 0x41);

      pm.message(m);
      // and now verify power is off.
      Assert.assertEquals("Power",jmri.PowerManager.OFF,pm.getPower());
    }

    @Test
    public void testReceiveStatusResponseInEstopMode(){
      // we should still see unknown, until a reply is received.
      Assert.assertEquals("Power",jmri.PowerManager.UNKNOWN,pm.getPower());

      // send the reply.
      XNetReply m = new XNetReply();
      m.setElement(0, 0x62);
      m.setElement(1, 0x22);
      m.setElement(2, 0x02);
      m.setElement(3, 0x42);

      pm.message(m);
      // and now verify power is off.
      Assert.assertEquals("Power",jmri.PowerManager.OFF,pm.getPower());
    }

    @Test
    public void testReceiveStatusResponseInServiceMode(){
      // we should still see unknown, until a reply is received.
      Assert.assertEquals("Power",jmri.PowerManager.UNKNOWN,pm.getPower());

      // send the reply.
      XNetReply m = new XNetReply();
      m.setElement(0, 0x62);
      m.setElement(1, 0x22);
      m.setElement(2, 0x08);
      m.setElement(3, 0x48);

      pm.message(m);
      // and now verify power is off.
      Assert.assertEquals("Power",jmri.PowerManager.OFF,pm.getPower());
    }

    @Test
    public void testReceiveStatusResponseInPowerUpMode(){
      // we should still see unknown, until a reply is received.
      Assert.assertEquals("Power",jmri.PowerManager.UNKNOWN,pm.getPower());

      // send the reply.
      XNetReply m = new XNetReply();
      m.setElement(0, 0x62);
      m.setElement(1, 0x22);
      m.setElement(2, 0x40);
      m.setElement(3, 0x00);

      pm.message(m);
      // and now verify power is off.
      Assert.assertEquals("Power",jmri.PowerManager.OFF,pm.getPower());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        // infrastructure objects
        tc = new XNetInterfaceScaffold(new LenzCommandStation());
        pm = new XNetPowerManager(new XNetSystemConnectionMemo(tc));
    }

    @After
    public void tearDown() {
        pm = null;
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
