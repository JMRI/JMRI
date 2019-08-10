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

    private XNetSimulatorAdapter a = null;
 
    @Test
    public void testCtor() {
        Assert.assertNotNull(a);
    }

    @Test
    public void testOkToSend() {
        Assert.assertTrue(a.okToSend());
    }

    @Test
    public void testStatus(){
        Assert.assertTrue(a.status());
        // if the status returns true, then we MUST have a connected
        // input and output stream.
        Assert.assertNotNull(a.getInputStream());
        Assert.assertNotNull(a.getOutputStream());
    }

    // tests of generation of specific replies.
    @Test
    public void testGenerateCSVersionReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("21 21 00"));
        Assert.assertEquals("CS Version Reply",new XNetReply("63 21 36 00 74"),r);
    }

    @Test
    public void testGenerateResumeOperationsReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("21 81 A0"));
        Assert.assertEquals("CS Resume Operations Reply",new XNetReply("61 01 60"),r);
    }

    @Test
    public void testGenerateEmergencyStopReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("21 80 A1"));
        Assert.assertEquals("CS Emergency Stop Reply",new XNetReply("61 00 61"),r);
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
    public void testSetCSModeReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("22 22 00 00"));
        // not currently supported
        Assert.assertEquals("set CS Power Up Mode Reply",new XNetReply("61 82 E3"),r);
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
    public void testGenerateRegisterModeReadReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("22 11 01 33"));
        // not currently supported
        Assert.assertEquals("Register Mode Read Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testGenerateRegisterModeWriteReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("23 12 01 30"));
        // not currently supported
        Assert.assertEquals("Register Mode Read Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testGeneratePagedModeReadReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("22 14 01 37"));
        // not currently supported
        Assert.assertEquals("Paged Mode Read Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testGeneratePagedModeWriteReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("23 17 01 36"));
        // not currently supported
        Assert.assertEquals("Paged Mode Read Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testGenerateDirectModeReadReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("22 15 01 36"));
        // not currently supported
        Assert.assertEquals("Direct Mode Read Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testGenerateDirectModeWriteReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("23 16 01 34"));
        // not currently supported
        Assert.assertEquals("Direct Mode Read Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testRequestServiceModeResultsReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("21 10 31"));
        // not currently supported
        Assert.assertEquals("Direct Mode Read Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testGenerateDoubleHeaderReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("E5 43 00 01 00 02 A5"));
        // not currently supported
        Assert.assertEquals("Establish Double Header Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testGenerateAccOperRequestReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("52 01 80 D3"));
        Assert.assertEquals("Accessory Decoder Info Reply",new XNetReply("42 01 10 53"),r);
    }

    @Test
    public void testGenerateAccInfoRequestReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("42 42 81 81"));
        Assert.assertEquals("Accessory Decoder Info Reply",new XNetReply("42 42 50 50"),r);
    }

    @Test
    public void testGenerateLocoInfoRequestReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("E3 00 01 01 E3"));
        Assert.assertEquals("LocoMotive Info Reply",new XNetReply("E4 13 00 00 00 F7"),r);
    }

    @Test
    public void testGenerateLocoInfoV1RequestReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("A1 01 A0"));
        // not currently supported
        Assert.assertEquals("LocoMotive Info V1 Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testGenerateLocoInfoV1V2RequestReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("A2 01 01 A2"));
        // not currently supported
        Assert.assertEquals("LocoMotive Info V1/V2 Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testGenerateLocoFunctionStatusRequestReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("E3 07 01 01 E4"));
        Assert.assertEquals("LocoMotive Function Status Reply",new XNetReply("E3 50 00 00 B3"),r);
    }

    @Test
    public void testSetLocoSpeedRequestReply(){
        // 14 speed step mode
        XNetReply r = getReplyForMessage(new XNetMessage("E4 10 01 11 01 E4"));
        Assert.assertEquals("LocoMotive set speed Reply",new XNetReply("01 04 05"),r);
        // loco status should change.
        r = getReplyForMessage(new XNetMessage("E3 00 01 01 E3"));
        Assert.assertEquals("LocoMotive Info Reply after speed set",new XNetReply("E4 10 01 00 00 F5"),r);
        // 27 speed step mode
        r = getReplyForMessage(new XNetMessage("E4 11 01 11 01 E4"));
        Assert.assertEquals("LocoMotive set speed Reply",new XNetReply("01 04 05"),r);
        // loco status should change.
        r = getReplyForMessage(new XNetMessage("E3 00 01 01 E3"));
        Assert.assertEquals("LocoMotive Info Reply after speed set",new XNetReply("E4 11 01 00 00 F4"),r);

        // 28 speed step mode
        r = getReplyForMessage(new XNetMessage("E4 12 01 11 01 E7"));
        Assert.assertEquals("LocoMotive set speed Reply",new XNetReply("01 04 05"),r);
        // loco status should change.
        r = getReplyForMessage(new XNetMessage("E3 00 01 01 E3"));
        Assert.assertEquals("LocoMotive Info Reply after speed set",new XNetReply("E4 12 01 00 00 F7"),r);
        // 128 speed step mode
        r = getReplyForMessage(new XNetMessage("E4 13 01 11 01 E6"));
        Assert.assertEquals("LocoMotive set speed Reply",new XNetReply("01 04 05"),r);
        // loco status should change.
        r = getReplyForMessage(new XNetMessage("E3 00 01 01 E3"));
        Assert.assertEquals("LocoMotive Info Reply after speed set",new XNetReply("E4 13 01 00 00 F6"),r);
    }

    @Test
    public void testSetLocoFunctionRequestReply(){
        // group 1
        XNetReply r = getReplyForMessage(new XNetMessage("E4 20 01 20 01 E4"));
        Assert.assertEquals("LocoMotive set function Reply",new XNetReply("01 04 05"),r);
        // loco status should change.
        r = getReplyForMessage(new XNetMessage("E3 00 01 01 E3"));
        Assert.assertEquals("LocoMotive Info Reply after function set",new XNetReply("E4 13 00 01 00 F6"),r);
        // group 2
        r = getReplyForMessage(new XNetMessage("E4 21 01 20 01 E5"));
        Assert.assertEquals("LocoMotive set function Reply",new XNetReply("01 04 05"),r);
        // loco status should change.
        r = getReplyForMessage(new XNetMessage("E3 00 01 01 E3"));
        Assert.assertEquals("LocoMotive Info Reply after function set",new XNetReply("E4 13 00 01 01 F7"),r);
        // group 3
        r = getReplyForMessage(new XNetMessage("E4 22 01 20 01 E5"));
        Assert.assertEquals("LocoMotive set function Reply",new XNetReply("01 04 05"),r);
        // loco status should change.
        r = getReplyForMessage(new XNetMessage("E3 00 01 01 E3"));
        Assert.assertEquals("LocoMotive Info Reply after function set",new XNetReply("E4 13 00 01 11 E7"),r);
    }

    @Test
    public void testSetLocoMomentaryFunctionRequestReply(){
        // group 1
        XNetReply r = getReplyForMessage(new XNetMessage("E4 24 01 24 01 E4"));
        Assert.assertEquals("LocoMotive set function Reply",new XNetReply("01 04 05"),r);
        // loco momentary function status should change.
        r = getReplyForMessage(new XNetMessage("E3 07 01 01 E4"));
        Assert.assertEquals("LocoMotive Function Status after set Reply",new XNetReply("E3 50 01 00 B2"),r);
        // group 2
        r = getReplyForMessage(new XNetMessage("E4 25 01 24 01 E5"));
        Assert.assertEquals("LocoMotive set function Reply",new XNetReply("01 04 05"),r);
        // loco momentary function status should change.
        r = getReplyForMessage(new XNetMessage("E3 07 01 01 E4"));
        Assert.assertEquals("LocoMotive Function Status after set Reply",new XNetReply("E3 50 01 01 B3"),r);
        // group 3
        r = getReplyForMessage(new XNetMessage("E4 26 01 24 01 E6"));
        Assert.assertEquals("LocoMotive set function Reply",new XNetReply("01 04 05"),r);
        // loco momentary function status should change.
        r = getReplyForMessage(new XNetMessage("E3 07 01 01 E4"));
        Assert.assertEquals("LocoMotive Function Status after set Reply",new XNetReply("E3 50 01 F1 43"),r);
    }

    @Test
    public void testLocoFunctionHighMomentryStatusReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("E3 08 01 08 E2"));
        Assert.assertEquals("LocoMotive High Momentary Function Status",new XNetReply("E4 51 00 00 B5"),r);
    }

    @Test
    public void testLocoFunctionHighStatusReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("E3 09 01 09 ED"));
        Assert.assertEquals("LocoMotive High Function Status",new XNetReply("E3 52 00 00 B1"),r);
    }

    @Test
    public void testSetLocoFunctionHighReply(){
        // set group 4
        XNetReply r = getReplyForMessage(new XNetMessage("E4 23 01 23 01 E4"));
        Assert.assertEquals("LocoMotive High Function Status set",new XNetReply("01 04 05"),r);
        // status should change
        r = getReplyForMessage(new XNetMessage("E3 09 01 09 E2"));
        Assert.assertEquals("LocoMotive group 4 Function Status after set",new XNetReply("E3 52 01 00 B0"),r);
        // set group 5
        r = getReplyForMessage(new XNetMessage("E4 28 01 23 01 EF"));
        Assert.assertEquals("LocoMotive High Function Status set",new XNetReply("01 04 05"),r);
        // status should change
        r = getReplyForMessage(new XNetMessage("E3 09 01 09 E2"));
        Assert.assertEquals("LocoMotive group 5 Function Status after set",new XNetReply("E3 52 01 01 B1"),r);
    }

    @Test
    public void testSetLocoFunctionHighMomentryReply(){
        // group 4
        XNetReply r = getReplyForMessage(new XNetMessage("E4 27 01 09 01 CD"));
        Assert.assertEquals("LocoMotive High Momentary Function Status set",new XNetReply("01 04 05"),r);
        r = getReplyForMessage(new XNetMessage("E3 08 01 08 E2"));
        Assert.assertEquals("LocoMotive group 4 Momentary Function Status",new XNetReply("E4 51 01 00 B4"),r);
        // group 5
        r = getReplyForMessage(new XNetMessage("E4 2C 01 09 01 C0"));
        Assert.assertEquals("LocoMotive High Momentary Function Status set",new XNetReply("01 04 05"),r);
        r = getReplyForMessage(new XNetMessage("E3 08 01 08 E2"));
        Assert.assertEquals("LocoMotive Group 5 Momentary Function Status",new XNetReply("E4 51 01 01 B5"),r);
    }

    @Test
    public void testAddToMURequestReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("E4 40 01 40 01 E4"));
        // not currently supported
        Assert.assertEquals("LocoMotive Add to MU Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testRemoveFromMURequestReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("E4 42 01 40 01 E6"));
        // not currently supported
        Assert.assertEquals("LocoMotive Remove from MU Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testMUMemberAddressInquiryReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("E4 01 01 01 01 E4"));
        // not currently supported
        Assert.assertEquals("LocoMotive MU Member inquiry Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testMUAddressInquiryReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("E2 03 01 E0"));
        // not currently supported
        Assert.assertEquals("LocoMotive MU Address inquiry Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testCSSearchStackReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("E3 05 01 03 E4"));
        // not currently supported
        Assert.assertEquals("LocoMotive Search Stack Reply",new XNetReply("61 82 E3"),r);
    }

    @Test
    public void testCSDeleteStackReply(){
        XNetReply r = getReplyForMessage(new XNetMessage("E3 44 01 44 E2"));
        // not currently supported
        Assert.assertEquals("LocoMotive Delete Stack Reply",new XNetReply("61 82 E3"),r);
    }

    private XNetReply getReplyForMessage(XNetMessage m){
        XNetReply r = null;
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
        a = new XNetSimulatorAdapter();
    }

    @After
    public void tearDown() {
        a.dispose();
        a = null;
        JUnitUtil.tearDown();
    }

}
