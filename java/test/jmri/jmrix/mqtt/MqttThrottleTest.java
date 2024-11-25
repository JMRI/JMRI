package jmri.jmrix.mqtt;

import jmri.SpeedStepMode;
import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test for the jmri.jmrix.mqtt.MqttThrottle class
 *
 * @author Paul Bender
 * @author Mark Underwood
 * @author Egbert Broerse 2021
 * @author Dean Cording 2023
 */
public class MqttThrottleTest extends jmri.jmrix.AbstractThrottleTest {

    @Test
    public void testCtor() {
        MqttThrottle t = new MqttThrottle(memo);
        Assert.assertNotNull(t);
    }

    /**
     * Test of getSpeedIncrement method, of class AbstractThrottle.
     */
    @Override
    @Test
    public void testGetSpeedIncrement() {
        float expResult = 1.0F/126.0F;
        float result = instance.getSpeedIncrement();
        Assert.assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getSpeedStepMode method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testGetSpeedStepMode() {
        SpeedStepMode expResult = SpeedStepMode.NMRA_DCC_128;
        SpeedStepMode result = instance.getSpeedStepMode();
        Assert.assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    @Override
    public void testGetIsForward() {
        Assert.assertTrue(instance.getIsForward()); //new throttle defaults to Forward
        instance.setIsForward(true);
        Assert.assertTrue(instance.getIsForward());
        instance.setIsForward(false);
        Assert.assertFalse(instance.getIsForward());
    }

    /**
     * Test sending speed and directions for expected message formats, 
     *   various command station versions, fkeys, states. 
     */
    @Test
    public void testSpeedSettingAndDirection(){

        instance.setSpeedSetting(0.5f);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==5; }, "publish triggered");
        Assert.assertEquals("cab/3/throttle", a.getLastTopic());
        Assert.assertEquals("50", new String(a.getLastPayload()));
        instance.setIsForward(true);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==6; }, "publish triggered");
        Assert.assertEquals("cab/3/direction", a.getLastTopic());
        Assert.assertEquals("FORWARD", new String(a.getLastPayload()));
        Assert.assertEquals(instance.getSpeedSetting(), 0.5f, 0.0001);
        Assert.assertTrue(instance.getIsForward());
//        Assert.assertEquals( "t 3 63 1", lm.toString());

        instance.setSpeedSetting(0.0f);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==7; }, "publish triggered");
        Assert.assertEquals("cab/3/throttle", a.getLastTopic());
        Assert.assertEquals("0", new String(a.getLastPayload()));
        instance.setIsForward(false);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==8; }, "publish triggered");
        Assert.assertEquals("cab/3/direction", a.getLastTopic());
        Assert.assertEquals("REVERSE", new String(a.getLastPayload()));
        Assert.assertEquals(instance.getSpeedSetting(), 0.0f, 0.0001);
        Assert.assertFalse(instance.getIsForward());
//        Assert.assertEquals( "t 3 0 0", lm.toString());

        instance.setSpeedSetting(1.0f);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==9; }, "publish triggered");
        Assert.assertEquals("cab/3/throttle", a.getLastTopic());
        Assert.assertEquals("100", new String(a.getLastPayload()));
        instance.setIsForward(false);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==10; }, "publish triggered");
        Assert.assertEquals("cab/3/direction", a.getLastTopic());
        Assert.assertEquals("REVERSE", new String(a.getLastPayload()));
        Assert.assertEquals(instance.getSpeedSetting(), 1.0f, 0.0001);
        Assert.assertFalse(instance.getIsForward());


        instance.setSpeedSetting(0.5f);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==11; }, "publish triggered");
        Assert.assertEquals("cab/3/throttle", a.getLastTopic());
        Assert.assertEquals("50", new String(a.getLastPayload()));
        instance.setIsForward(true);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==12; }, "publish triggered");
        Assert.assertEquals("cab/3/direction", a.getLastTopic());
        Assert.assertEquals("FORWARD", new String(a.getLastPayload()));
        Assert.assertEquals(instance.getSpeedSetting(), 0.5f, 0.0001);
        Assert.assertTrue(instance.getIsForward());

        instance.setSpeedSetting(0.0f);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==13; }, "publish triggered");
        Assert.assertEquals("cab/3/throttle", a.getLastTopic());
        Assert.assertEquals("0", new String(a.getLastPayload()));
        instance.setIsForward(false);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==14; }, "publish triggered");
        Assert.assertEquals("cab/3/direction", a.getLastTopic());
        Assert.assertEquals("REVERSE", new String(a.getLastPayload()));
        Assert.assertEquals(instance.getSpeedSetting(), 0.0f, 0.0001);
        Assert.assertFalse(instance.getIsForward());

        instance.setSpeedSetting(1.0f);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==15; }, "publish triggered");
        Assert.assertEquals("cab/3/throttle", a.getLastTopic());
        Assert.assertEquals("100", new String(a.getLastPayload()));
        instance.setIsForward(false);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==16; }, "publish triggered");
        Assert.assertEquals("cab/3/direction", a.getLastTopic());
        Assert.assertEquals("REVERSE", new String(a.getLastPayload()));
        Assert.assertEquals(instance.getSpeedSetting(), 1.0f, 0.0001);
        Assert.assertFalse(instance.getIsForward());

    }

    /**
     * Test sending FKeys for expected message formats, various commandstation
     *   versions, fkeys, states. 
     */    
    @Test
    public void testFunctionFormats(){
        instance.setFunction(0, true);
        Assert.assertTrue(instance.getFunction(0));
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==5; }, "publish triggered");
        Assert.assertEquals("cab/3/function/0", a.getLastTopic());
        Assert.assertEquals("ON", new String(a.getLastPayload()));
        instance.setFunction(0, false);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==6; }, "publish triggered");
        Assert.assertEquals("cab/3/function/0", a.getLastTopic());
        Assert.assertEquals("OFF", new String(a.getLastPayload()));

        instance.setFunction(22, false);
        Assert.assertFalse(instance.getFunction(22));
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==7; }, "publish triggered");
        Assert.assertEquals("cab/3/function/22", a.getLastTopic());
        Assert.assertEquals("OFF", new String(a.getLastPayload()));

        instance.setFunction(28, true);
        Assert.assertTrue(instance.getFunction(28));
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==8; }, "publish triggered");
        Assert.assertEquals("cab/3/function/28", a.getLastTopic());
        Assert.assertEquals("ON", new String(a.getLastPayload()));
        instance.setFunction(28, false);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==9; }, "publish triggered");
        Assert.assertEquals("cab/3/function/28", a.getLastTopic());
        Assert.assertEquals("OFF", new String(a.getLastPayload()));

        instance.setFunction(16, true);
        Assert.assertTrue(instance.getFunction(16));
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==10; }, "publish triggered");
        Assert.assertEquals("cab/3/function/16", a.getLastTopic());
        Assert.assertEquals("ON", new String(a.getLastPayload()));
        instance.setFunction(16, false);
        Assert.assertFalse(instance.getFunction(16));
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==11; }, "publish triggered");
        Assert.assertEquals("cab/3/function/16", a.getLastTopic());
        Assert.assertEquals("OFF", new String(a.getLastPayload()));

        instance.setFunction(0, true);
        Assert.assertTrue(instance.getFunction(0));
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==12; }, "publish triggered");
        Assert.assertEquals("cab/3/function/0", a.getLastTopic());
        Assert.assertEquals("ON", new String(a.getLastPayload()));
        instance.setFunction(0, false);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==13; }, "publish triggered");
        Assert.assertEquals("cab/3/function/0", a.getLastTopic());
        Assert.assertEquals("OFF", new String(a.getLastPayload()));

        instance.setFunction(21, false);
        Assert.assertFalse(instance.getFunction(21));
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==14; }, "publish triggered");
        Assert.assertEquals("cab/3/function/21", a.getLastTopic());
        Assert.assertEquals("OFF", new String(a.getLastPayload()));

        instance.setFunction(4, true);
        Assert.assertTrue(instance.getFunction(4));
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==15; }, "publish triggered");
        Assert.assertEquals("cab/3/function/4", a.getLastTopic());
        Assert.assertEquals("ON", new String(a.getLastPayload()));
        instance.setFunction(4, false);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==16; }, "publish triggered");
        Assert.assertEquals("cab/3/function/4", a.getLastTopic());
        Assert.assertEquals("OFF", new String(a.getLastPayload()));

        instance.setFunction(28, true);
        Assert.assertTrue(instance.getFunction(28));
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==17; }, "publish triggered");
        Assert.assertEquals("cab/3/function/28", a.getLastTopic());
        Assert.assertEquals("ON", new String(a.getLastPayload()));
        instance.setFunction(28, false);
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==18; }, "publish triggered");
        Assert.assertEquals("cab/3/function/28", a.getLastTopic());
        Assert.assertEquals("OFF", new String(a.getLastPayload()));

    }

    /**
     * Test receiving messages for throttle, direction and functions
     */
    @Test
    public void testReceivingUpdates(){

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/throttle", "22");
        Assert.assertEquals(0.22f, instance.getSpeedSetting(), 0.0001);

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/throttle", "0");
        Assert.assertEquals(0.0f, instance.getSpeedSetting(), 0.0001);

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/throttle", "100");
        Assert.assertEquals(1.0f, instance.getSpeedSetting(), 0.0001);

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/throttle", "10");
        Assert.assertEquals(0.1f, instance.getSpeedSetting(), 0.0001);

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/throttle", "122");
        Assert.assertEquals(1.0f, instance.getSpeedSetting(), 0.0001);

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/direction", "FORWARD");
        Assert.assertTrue(instance.getIsForward());

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/direction", "REVERSE");
        Assert.assertFalse(instance.getIsForward());

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/direction", "FORWARD");
        Assert.assertTrue(instance.getIsForward());

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/direction", "XXXX");
        JUnitAppender.suppressErrorMessage("Invalid message XXXX");
        Assert.assertTrue(instance.getIsForward());

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/direction", "REVERSE");
        Assert.assertFalse(instance.getIsForward());

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/direction", "XXXX");
        JUnitAppender.suppressErrorMessage("Invalid message XXXX");
        Assert.assertFalse(instance.getIsForward());

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/direction", "STOP");
        Assert.assertEquals(-1.0f, instance.getSpeedSetting(), 0.0001);

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/function/1", "ON");
        Assert.assertTrue(instance.getFunction(1));

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/function/1", "OFF");
        Assert.assertFalse(instance.getFunction(1));

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/function/1", "XXX");
        JUnitAppender.suppressErrorMessage("Invalid message XXX");
        Assert.assertFalse(instance.getFunction(1));

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/function/1", "ON");
        Assert.assertTrue(instance.getFunction(1));

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/function/1", "XXX");
        JUnitAppender.suppressErrorMessage("Invalid message XXX");
        Assert.assertFalse(instance.getFunction(1));

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/function/23", "ON");
        Assert.assertTrue(instance.getFunction(23));
        Assert.assertFalse(instance.getFunction(1));

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/function/23", "OFF");
        Assert.assertFalse(instance.getFunction(23));

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/function/23", "XXX");
        JUnitAppender.suppressErrorMessage("Invalid message XXX");
        Assert.assertFalse(instance.getFunction(23));
        Assert.assertFalse(instance.getFunction(1));

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/function/23", "ON");
        Assert.assertTrue(instance.getFunction(23));
        Assert.assertFalse(instance.getFunction(1));

        ((MqttThrottle)instance).notifyMqttMessage("cab/3/function/23", "XXX");
        JUnitAppender.suppressErrorMessage("Invalid message XXX");
        Assert.assertFalse(instance.getFunction(23));
        Assert.assertFalse(instance.getFunction(1));




    }


    /**
     * Test of setF0 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF0() {
        boolean f0 = false;
        instance.setF0(f0);
    }

    /**
     * Test of setF1 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF1() {
        boolean f1 = false;
        instance.setF1(f1);
    }

    /**
     * Test of setF2 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF2() {
        boolean f2 = false;
        instance.setF2(f2);
    }

    /**
     * Test of setF3 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF3() {
        boolean f3 = false;
        instance.setF3(f3);
    }

    /**
     * Test of setF4 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF4() {
        boolean f4 = false;
        instance.setF4(f4);
    }

    /**
     * Test of setF5 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF5() {
        boolean f5 = false;
        instance.setF5(f5);
    }

    /**
     * Test of setF6 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF6() {
        boolean f6 = false;
        instance.setF6(f6);
    }

    /**
     * Test of setF7 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF7() {
        boolean f7 = false;
        instance.setF7(f7);
    }

    /**
     * Test of setF8 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF8() {
        boolean f8 = false;
        instance.setF8(f8);
    }

    /**
     * Test of setF9 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF9() {
        boolean f9 = false;
        instance.setF9(f9);
    }

    /**
     * Test of setF10 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF10() {
        boolean f10 = false;
        instance.setF10(f10);
    }

    /**
     * Test of setF11 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF11() {
        boolean f11 = false;
        instance.setF11(f11);
    }

    /**
     * Test of setF12 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF12() {
        boolean f12 = false;
        instance.setF12(f12);
    }

    /**
     * Test of setF13 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF13() {
        boolean f13 = false;
        instance.setF13(f13);
    }

    /**
     * Test of setF14 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF14() {
        boolean f14 = false;
        instance.setF14(f14);
    }

    /**
     * Test of setF15 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF15() {
        boolean f15 = false;
        instance.setF15(f15);
    }

    /**
     * Test of setF16 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF16() {
        boolean f16 = false;
        instance.setF16(f16);
    }

    /**
     * Test of setF17 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF17() {
        boolean f17 = false;
        instance.setF17(f17);
    }

    /**
     * Test of setF18 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF18() {
        boolean f18 = false;
        instance.setF18(f18);
    }

    /**
     * Test of setF19 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF19() {
        boolean f19 = false;
        instance.setF19(f19);
    }

    /**
     * Test of setF20 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF20() {
        boolean f20 = false;
        instance.setF20(f20);
    }

    /**
     * Test of setF21 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF21() {
        boolean f21 = false;
        instance.setF21(f21);
    }

    /**
     * Test of setF22 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF22() {
        boolean f22 = false;
        instance.setF22(f22);
    }

    /**
     * Test of setF23 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF23() {
        boolean f23 = false;
        instance.setF23(f23);
    }

    /**
     * Test of setF24 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF24() {
        boolean f24 = false;
        instance.setF24(f24);
    }

    /**
     * Test of setF25 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF25() {
        boolean f25 = false;
        instance.setF25(f25);
    }

    /**
     * Test of setF26 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF26() {
        boolean f26 = false;
        instance.setF26(f26);
    }

    /**
     * Test of setF27 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF27() {
        boolean f27 = false;
        instance.setF27(f27);
    }

    /**
     * Test of setF28 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF28() {
        boolean f28 = false;
        instance.setF28(f28);
    }

    /**
     * Test of sendFunctionGroup1 method, of class AbstractThrottle.
     */
    @Test
    @Override
    @Disabled("Test requires further development")
    public void testSendFunctionGroup1() {
    }

    /**
     * Test of sendFunctionGroup2 method, of class AbstractThrottle.
     */
    @Test
    @Override
    @Disabled("Test requires further development")
    public void testSendFunctionGroup2() {
    }

    /**
     * Test of sendFunctionGroup3 method, of class AbstractThrottle.
     */
    @Test
    @Override
    @Disabled("Test requires further development")
    public void testSendFunctionGroup3() {
    }

    /**
     * Test of sendFunctionGroup4 method, of class AbstractThrottle.
     */
    @Test
    @Override
    @Disabled("Test requires further development")
    public void testSendFunctionGroup4() {
    }

    /**
     * Test of sendFunctionGroup5 method, of class AbstractThrottle.
     */
    @Test
    @Override
    @Disabled("Test requires further development")
    public void testSendFunctionGroup5() {
    }

    // Test the constructor with an address specified.
    @Test
    public void testCtorWithArg() throws Exception {
        Assert.assertNotNull(instance);
    }


    private MqttSystemConnectionMemo memo = null;
    private MqttThrottleManager tm;
    private MqttAdapterScaffold a;

    public String sendThrottleTopic = "cab/{0}/throttle";
    public String rcvThrottleTopic ="cab/{0}/throttle";
    public String sendDirectionTopic = "cab/{0}/direction";
    public String rcvDirectionTopic = "cab/{0}/direction";
    public String sendFunctionTopic = "cab/{0}/function/{1}";
    public String rcvFunctionTopic = "cab/{0}/function/{1}";


    @Override
    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
       // prepare an interface
        a = new MqttAdapterScaffold(true);

        memo = new MqttSystemConnectionMemo();
        memo.setMqttAdapter(a);
        tm = new MqttThrottleManager(memo);
        jmri.InstanceManager.setDefault(jmri.ThrottleManager.class, tm);
        instance = new MqttThrottle(memo, sendThrottleTopic, rcvThrottleTopic,
        sendDirectionTopic, rcvDirectionTopic, sendFunctionTopic, rcvFunctionTopic, new jmri.DccLocoAddress(3, false));
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        a.dispose();
        a = null;
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

}
