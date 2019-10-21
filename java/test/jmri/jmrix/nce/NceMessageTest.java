package jmri.jmrix.nce;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * JUnit tests for the NceMessage class.
 *
 * @author	Bob Jacobsen Copyright 2002-2004
 */
public class NceMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    // ensure that the static useBinary value is left OK
    private int saveCommandOptions;
    private NceTrafficController tc; // don't init now, as there's logging in the ctor
    private NceMessage msg = null; 

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new NceTrafficController();
        saveCommandOptions = tc.getCommandOptions();
        m = msg = new NceMessage(1);
    }

    @After
    public void tearDown() {
	m = msg = null;
        tc.commandOptionSet = false;	// kill warning message
        tc.setCommandOptions(saveCommandOptions);
        Assert.assertTrue("Command has been set", tc.commandOptionSet);
        tc.commandOptionSet = false;	// kill warning message
        tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    @Test
    public void testToBinaryString() {
        msg = new NceMessage(4);
        msg.setOpCode(0x81);
        msg.setElement(1, 0x02);
        msg.setElement(2, 0xA2);
        msg.setElement(3, 0x00);
        msg.setBinary(true);
        Assert.assertEquals("string compare ", "81 02 A2 00", msg.toString());
    }

    @Test
    public void testToASCIIString() {
        msg = new NceMessage(5);
        msg.setOpCode(0x50);
        msg.setElement(1, 0x20);
        msg.setElement(2, 0x32);
        msg.setElement(3, 0x36);
        msg.setElement(4, 0x31);
        msg.setBinary(false);
        Assert.assertEquals("string compare ", "P 261", msg.toString());
    }

    @Test
    public void testGetEnableAscii() {
        tc.setCommandOptions(NceTrafficController.OPTION_FORCE_ASCII);
        msg = NceMessage.getEnableMain(tc);
        Assert.assertEquals("length", 1, msg.getNumDataElements());
        Assert.assertEquals("opCode", 'E', msg.getOpCode());
    }

    @Test
    public void testGetEnableBinary() {
        tc.setCommandOptions(NceTrafficController.OPTION_1999);
        msg = NceMessage.getEnableMain(tc);
        Assert.assertEquals("length", 1, msg.getNumDataElements());
        Assert.assertEquals("opCode", 0x89, msg.getOpCode());
    }

    @Test
    public void testRecognizeEnable() {
        msg = NceMessage.getEnableMain(tc);
        Assert.assertEquals("isEnableMain", true, msg.isEnableMain());
        Assert.assertEquals("isKillMain", false, msg.isKillMain());
    }

    @Test
    public void testReadPagedCVAscii() {
        tc.setCommandOptions(NceTrafficController.OPTION_2004);
        msg = NceMessage.getReadPagedCV(tc, 12);
        Assert.assertEquals("string compare ", "R012", msg.toString());
    }

    @Test
    public void testReadPagedCVBin() {
        tc.setCommandOptions(NceTrafficController.OPTION_2006);
        msg = NceMessage.getReadPagedCV(tc, 12);
        Assert.assertEquals("string compare ", "A1 00 0C", msg.toString());
    }

    @Test
    public void testReadPagedCVBinToMonitorString() {
        tc.setCommandOptions(NceTrafficController.OPTION_2006);
        msg = NceMessage.getReadPagedCV(tc, 12);
        Assert.assertEquals("monitor string compare ", "Read CV 12 in paged mode", msg.toMonitorString());
    }

    @Test
    public void testWritePagedCVAscii() {
        tc.setCommandOptions(NceTrafficController.OPTION_2004);
        msg = NceMessage.getWritePagedCV(tc, 12, 251);
        Assert.assertEquals("string compare ", "P012 251", msg.toString());
    }

    @Test
    public void testWritePagedCVAsciiToMonitorString() {
        tc.setCommandOptions(NceTrafficController.OPTION_2004);
        msg = NceMessage.getWritePagedCV(tc, 12, 251);
        Assert.assertEquals("monitor string compare ", "binary cmd: P012 251", msg.toMonitorString());
    }

    @Test
    public void testWritePagedCVBin() {
        tc.setCommandOptions(NceTrafficController.OPTION_2006);
        msg = NceMessage.getWritePagedCV(tc, 12, 251);
        Assert.assertEquals("string compare ", "A0 00 0C FB", msg.toString());
    }

    @Test
    public void testReadRegisterAscii() {
        tc.setCommandOptions(NceTrafficController.OPTION_2004);
        msg = NceMessage.getReadRegister(tc, 2);
        Assert.assertEquals("string compare ", "V2", msg.toString());
    }

    @Test
    public void testReadRegisterBin() {
        tc.setCommandOptions(NceTrafficController.OPTION_2006);
        msg = NceMessage.getReadRegister(tc, 2);
        Assert.assertEquals("string compare ", "A7 02", msg.toString());
    }

    @Test
    public void testWriteRegisterAscii() {
        tc.setCommandOptions(NceTrafficController.OPTION_2004);
        msg = NceMessage.getWriteRegister(tc, 2, 251);
        Assert.assertEquals("string compare ", "S2 251", msg.toString());
    }

    @Test
    public void testWriteRegisterBin() {
        tc.setCommandOptions(NceTrafficController.OPTION_2006);
        msg = NceMessage.getWriteRegister(tc, 2, 251);
        Assert.assertEquals("string compare ", "A6 02 FB", msg.toString());
    }

    @Test
    public void testCheckPacketMessage1Ascii() {
        tc.setCommandOptions(NceTrafficController.OPTION_FORCE_ASCII);
        msg = NceMessage.sendPacketMessage(tc, new byte[]{(byte) 0x81, (byte) 0xff, (byte) 0x7e});
        Assert.assertEquals("content", "S C02 81 FF 7E", msg.toString());
    }

    @Test
    public void testCheckPacketMessage1Bin() {
        tc.setCommandOptions(NceTrafficController.OPTION_1999);
        msg = NceMessage.sendPacketMessage(tc, new byte[]{(byte) 0x81, (byte) 0xff, (byte) 0x7e});
        Assert.assertEquals("content", "93 02 81 FF 7E", msg.toString());
    }

}
