package jmri.jmrix.lenz.xnetsimulator;

import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XNetSimulatorAdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter
 * class
 *
 * @author	Paul Bender
 */
public class XNetSimulatorAdapterTest {

    @Test
    public void testCtor() {
        XNetSimulatorAdapter a = new XNetSimulatorAdapter();
        Assert.assertNotNull(a);
    }

    @Test
    public void testOkToSend() {
        XNetSimulatorAdapter a = new XNetSimulatorAdapter();
        Assert.assertTrue(a.okToSend());
    }

    @Test
    public void testGenerateCSVersionReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("21 21 00"));
        Assert.assertEquals("CS Version Reply",new XNetReply("63 21 36 00 74"),r);
    }

    @Test
    public void testGenerateResumeOperationsReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("21 81 A0"));
        Assert.assertEquals("CS Resume Operations Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testGenerateEmergencyStopReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("21 80 A1"));
        Assert.assertEquals("CS Emergency Stop Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testEmergencyStopAllReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("80 80"));
        Assert.assertEquals("CS Emergency Stop All Reply ",new XNetReply("81 00 81"),r);
    }

    @Test
    public void testCSStatusReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("21 24 05"));
        Assert.assertEquals("CS Emergency Stop All Reply ",new XNetReply("62 22 00 40"),r);
    }

    @Test
    public void testGenerateLiVersionReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("F0 F0"));
        Assert.assertEquals("LI Version Reply",new XNetReply("02 00 00 02"),r);
    }

    @Test
    public void testGenerateLiAddressReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("F2 01 00 F3"));
        // not currently supported
        Assert.assertEquals("LI Address Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testGenerateLiBaudReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("F2 02 00 F0"));
        // not currently supported
        Assert.assertEquals("LI Baud Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testEmergencyStopLocoReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("92 00 02 90"));
        Assert.assertEquals("CS Emergency Specific Loco (XNetV4) Reply ",new XNetReply("01 04 05"),r);
    }

    @Test
    public void testEmergencyStopLocoReplyV1V2(){
        XNetReply r = getReplyForMessage(new XNetMessage("91 02 93"));
        Assert.assertEquals("CS Emergency Specific Loco (XNetV1,V2) Reply ",new XNetReply("01 04 05"),r);
    }

    @Test
    public void testGenerateOpsModeWriteCvReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("E6 30 00 42 EC 03 05 7D"));
        Assert.assertEquals("Ops Mode Write CV Reply",new XNetReply("01 04 05"),r);
    }

    @Test
    public void testGenerateOpsModeVerifyCvReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("E6 30 00 42 E4 03 05 7A"));
        Assert.assertEquals("Ops Mode Verify CV Reply",new XNetReply("01 04 05"),r);
    }

    @Test
    public void testGenerateOpsModeWriteBitReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("E6 30 00 32 E8 02 E9 E7"));
        Assert.assertEquals("Ops Mode Write Bit Reply",new XNetReply("01 04 05"),r);
    }

    @Test
    public void testGenerateOpsModeVerifyBitReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("E6 30 00 32 E8 02 F9 F7"));
        Assert.assertEquals("Ops Mode Verify Bit Reply",new XNetReply("01 04 05"),r);
    }

    @Test
    public void testGenerateDoubleHeaderReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("C5 04 00 01 00 02 C2"));
        // not currently supported
        Assert.assertEquals("Establish Double Header Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testGenerateAccOperRequetReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("52 01 80 D3"));
        Assert.assertEquals("Accessory Decoder Info Reply",new XNetReply("42 01 10 53"),r);
    }

    @Test
    public void testGenerateAccInfoRequetReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("42 42 81 81"));
        Assert.assertEquals("Accessory Decoder Info Reply",new XNetReply("42 42 50 50"),r);
    }

    private XNetReply getReplyForMessage(XNetMessage m){
        XNetReply r = null;
        XNetSimulatorAdapter a = new XNetSimulatorAdapter();
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method generateReplyMethod = null;
        try {
            generateReplyMethod = a.getClass().getDeclaredMethod("generateReply", XNetMessage.class);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method generateReply in XNetSimulatorAdapter class: ");
        }

        // override the default permissions.
        Assert.assertNotNull(generateReplyMethod);
        generateReplyMethod.setAccessible(true);

        try {
           r = (XNetReply) generateReplyMethod.invoke(a,m);
        } catch(java.lang.IllegalAccessException ite){
             Assert.fail("could not access method generateReply in XNetSimulatoradapter class");
        } catch(java.lang.reflect.InvocationTargetException ite){
             Throwable cause = ite.getCause();
             Assert.fail("generateReply execution failed reason: " + cause.getMessage());
        }
        return r;
    }


    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
