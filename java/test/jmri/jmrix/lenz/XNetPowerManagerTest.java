package jmri.jmrix.lenz;

import jmri.JmriException;
import jmri.PowerManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XNetPowerManagerTest.java
 * <p>
 * Description:	tests for the jmri.jmrix.lenz.XNetPowerManager class
 *
 * @author	Paul Bender
 */
public class XNetPowerManagerTest extends jmri.jmrix.AbstractPowerManagerTestBase {

    private XNetPowerManager pm = null;
    private XNetInterfaceScaffold tc = null;
    private int propertyChangeCount;
    private java.beans.PropertyChangeListener listener = null;

    // service routines to simulate receiving on, off from interface
    @Override
    protected void hearOn() {
        sendOnReply();
    }

    @Override
    protected void sendOnReply() {
        // send the reply.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x61);
        m.setElement(1, 0x01);
        m.setElement(2, 0x60);
        pm.message(m);
    }

    @Override
    protected void sendOffReply() {
        XNetReply m = new XNetReply();
        m.setElement(0, 0x61);
        m.setElement(1, 0x00);
        m.setElement(2, 0x61);
        pm.message(m);
    }

    @Override
    protected void sendIdleReply() {
        XNetReply m = new XNetReply();
        m.setElement(0, 0x81);
        m.setElement(1, 0x00);
        m.setElement(2, 0x81);
        pm.message(m);
    }

    @Override
    protected void hearOff() {
        sendOffReply();
    }

    @Override
    protected void hearIdle() {
        sendIdleReply();
    }

    @Override
    protected int numListeners() {
        return tc.numListeners();
    }

    @Override
    protected int outboundSize() {
        return tc.outbound.size();
    }

    @Override
    protected boolean outboundOnOK(int index) {
        XNetMessage m = XNetMessage.getResumeOperationsMsg();
        return tc.outbound.elementAt(index).equals(m);
    }

    @Override
    protected boolean outboundOffOK(int index) {
        XNetMessage m = XNetMessage.getEmergencyOffMsg();
        return tc.outbound.elementAt(index).equals(m);
    }

    @Override
    protected boolean outboundIdleOK(int index) {
        XNetMessage m = XNetMessage.getEmergencyStopMsg();
        return tc.outbound.elementAt(index).equals(m);
    }

    @Test
    public void testGetUserName() {
        Assert.assertEquals("User Name", "XpressNet", pm.getUserName());
    }

    @Test
    public void testGetPower() {
        Assert.assertEquals("Power", PowerManager.UNKNOWN, pm.getPower());
    }

    @Test
    public void testSetPowerON() {
        try {
            pm.setPower(PowerManager.ON);
        } catch (JmriException je) {
            Assert.fail("Failed to set Power ON");
        }
        // we should still see unknown, until a reply is received.
        Assert.assertEquals("Power", PowerManager.UNKNOWN, pm.getPower());
        // check that we actually sent a message.
        Assert.assertEquals("Message Sent", 2, tc.outbound.size());
        // send the reply.
        sendOnReply();
        // and now verify power is set the right way.
        Assert.assertEquals("Power", PowerManager.ON, pm.getPower());
    }

    @Test
    public void testSetPowerOFF() {
        try {
            pm.setPower(PowerManager.OFF);
        } catch (JmriException je) {
            Assert.fail("Failed to set Power OFF");
        }
        // we should still see unknown, until a reply is received.
        Assert.assertEquals("Power", PowerManager.UNKNOWN, pm.getPower());
        // check that we actually sent a message.
        Assert.assertEquals("Message Sent", 2, tc.outbound.size());
        // send the reply.
        sendOffReply();
        // and now verify power is set the right way.
        Assert.assertEquals("Power", PowerManager.OFF, pm.getPower());
    }

    @Test
    public void testReceiveEmergencyStop() {
        // we should still see unknown, until a reply is received.
        Assert.assertEquals("Power", PowerManager.UNKNOWN, pm.getPower());

        // send the reply.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x81);
        m.setElement(1, 0x00);
        m.setElement(2, 0x81);

        pm.message(m);
        // and now verify power is IDLE.
        Assert.assertEquals("Power", PowerManager.IDLE, pm.getPower());
    }

    @Test
    public void testReceiveServiceModeEntry() {
        // we should still see unknown, until a reply is received.
        Assert.assertEquals("Power", PowerManager.UNKNOWN, pm.getPower());

        // send the reply.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x61);
        m.setElement(1, 0x02);
        m.setElement(2, 0x63);

        pm.message(m);
        // and now verify power is off.
        Assert.assertEquals("Power", PowerManager.OFF, pm.getPower());
    }

    @Test
    public void testReceiveStatusResponse() {
        // we should still see unknown, until a reply is received.
        Assert.assertEquals("Power", PowerManager.UNKNOWN, pm.getPower());

        // send the reply.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x62);
        m.setElement(1, 0x22);
        m.setElement(2, 0x00);
        m.setElement(3, 0x40);

        pm.message(m);
        // and now verify power is on.
        Assert.assertEquals("Power", PowerManager.ON, pm.getPower());
    }

    @Test
    public void testReceiveStatusResponseInEmergencyOffMode() {
        // we should still see unknown, until a reply is received.
        Assert.assertEquals("Power", PowerManager.UNKNOWN, pm.getPower());

        // send the reply.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x62);
        m.setElement(1, 0x22);
        m.setElement(2, 0x01);
        m.setElement(3, 0x41);

        pm.message(m);
        // and now verify power is off.
        Assert.assertEquals("Power", PowerManager.OFF, pm.getPower());
    }

    @Test
    public void testReceiveStatusResponseInEstopMode() {
        // we should still see unknown, until a reply is received.
        Assert.assertEquals("Power", PowerManager.UNKNOWN, pm.getPower());

        // send the reply.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x62);
        m.setElement(1, 0x22);
        m.setElement(2, 0x02);
        m.setElement(3, 0x42);

        pm.message(m);
        // and now verify power is IDLE.
        Assert.assertEquals("Power", PowerManager.IDLE, pm.getPower());
    }

    @Test
    public void testReceiveStatusResponseInServiceMode() {
        // we should still see unknown, until a reply is received.
        Assert.assertEquals("Power", PowerManager.UNKNOWN, pm.getPower());

        // send the reply.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x62);
        m.setElement(1, 0x22);
        m.setElement(2, 0x08);
        m.setElement(3, 0x48);

        pm.message(m);
        // and now verify power is off.
        Assert.assertEquals("Power", PowerManager.OFF, pm.getPower());
    }

    @Test
    public void testReceiveStatusResponseInPowerUpMode() {
        // we should still see unknown, until a reply is received.
        Assert.assertEquals("Power", PowerManager.UNKNOWN, pm.getPower());

        // send the reply.
        XNetReply m = new XNetReply();
        m.setElement(0, 0x62);
        m.setElement(1, 0x22);
        m.setElement(2, 0x40);
        m.setElement(3, 0x00);

        pm.message(m);
        // and now verify power is off.
        Assert.assertEquals("Power", PowerManager.OFF, pm.getPower());
    }

    @Test
    public void testAddAndRemoveListener() {
        listener = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent event) {
                propertyChangeCount = propertyChangeCount + 1;
            }
        };
        pm.addPropertyChangeListener(listener);
        Assert.assertEquals("PropertyChangeCount", 0, propertyChangeCount);
        // trigger a property change, and make sure the count changes too.
        sendOnReply();
        Assert.assertEquals("PropertyChangeCount", 1, propertyChangeCount);
        pm.removePropertyChangeListener(listener);
        // now trigger another change, and make sure the count doesn't change.
        sendOnReply();
        Assert.assertEquals("PropertyChangeCount", 1, propertyChangeCount);
    }

    @Test
    @Override
    public void testImplementsIdle() {
        Assert.assertTrue(p.implementsIdle());
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        tc = new XNetInterfaceScaffold(new LenzCommandStation());
        p = pm = new XNetPowerManager(tc.getSystemConnectionMemo());
    }

    @After
    public void tearDown() {
        p = pm = null;
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
