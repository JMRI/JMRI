package jmri.jmrix.mqtt;

import jmri.jmrix.AbstractPowerManagerTestBase;
import jmri.util.JUnitUtil;
import jmri.JmriException;
import jmri.PowerManager;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * MqttPowerManagerTest.java
 *
 * Test for the jmri.jmrix.mqtt.MqttPowerManager class
 *
 * @author Dean Cording (C) 2023
 *
 */
public class MqttPowerManagerTest extends AbstractPowerManagerTestBase {

    private MqttSystemConnectionMemo memo = null;
    private MqttAdapterScaffold a;

    public String sendPowerTopic = "track/power";
    public String rcvPowerTopic ="track/power";

    @Override
    protected void hearOn(){
        ((MqttPowerManager)p).notifyMqttMessage(rcvPowerTopic, "ON");
    }

    @Override
    protected void hearOff(){
        ((MqttPowerManager)p).notifyMqttMessage(rcvPowerTopic, "OFF");
    }

    @Override
    protected void hearIdle(){}

    @Override
    protected void sendOnReply(){
        ((MqttPowerManager)p).notifyMqttMessage(rcvPowerTopic, "ON");
    }  // get a reply to On command from layout

    @Override
    protected void sendOffReply(){
        ((MqttPowerManager)p).notifyMqttMessage(rcvPowerTopic, "OFF");
    } // get a reply to Off command from layout

    @Override
    protected void sendIdleReply(){}

    @Override
    protected int numListeners(){return 0;}

    @Override
    protected int outboundSize(){return 0;}

    @Override
    protected boolean outboundOnOK(int index){return false;}

    @Override
    protected boolean outboundOffOK(int index){return false;}

    @Override
    protected boolean outboundIdleOK(int index){return false;}


        // test setting power on, off, then getting reply from system
    @Override
    @Test
    public void testSetPowerOn() throws JmriException {
        Assert.assertEquals(PowerManager.UNKNOWN, p.getPower());
        p.setPower(PowerManager.ON);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==2; }, "publish triggered");
        Assert.assertEquals(rcvPowerTopic, a.getLastTopic());
        Assert.assertEquals("ON", new String(a.getLastPayload()));
        Assert.assertEquals(PowerManager.UNKNOWN, p.getPower());
        hearOn();
        Assert.assertEquals(PowerManager.ON, p.getPower());

    }

    @Override
    @Test
    public void testSetPowerOff() throws JmriException {
        Assert.assertEquals(PowerManager.UNKNOWN, p.getPower());
        p.setPower(PowerManager.OFF);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==2; }, "publish triggered");
        Assert.assertEquals(rcvPowerTopic, a.getLastTopic());
        Assert.assertEquals("OFF", new String(a.getLastPayload()));
        Assert.assertEquals(PowerManager.UNKNOWN, p.getPower());
        hearOff();
        Assert.assertEquals(PowerManager.OFF, p.getPower());

    }


    @Override
    @Test
    @Disabled("Listeners not relevant")
    public void testDispose1() throws JmriException {}

    @Override
    @Test
    @Disabled("Listeners not relevant")
    public void testDispose2() throws JmriException {}

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        a = new MqttAdapterScaffold(true);

        memo = new MqttSystemConnectionMemo();
        memo.setMqttAdapter(a);

        p = new MqttPowerManager(memo);
        ((MqttPowerManager)p).setSendTopic(sendPowerTopic);
        ((MqttPowerManager)p).setRcvTopic(rcvPowerTopic);

    }

    @AfterEach
    public void tearDown() {
        a.dispose();
        a = null;
        memo.dispose();
        memo = null;
        p = null;
        JUnitUtil.tearDown();

    }

}
