package jmri.jmrix.nce;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * JUnit tests for the NceMessage class
 *
 * @author	Bob Jacobsen Copyright 2002-2004
  */
public class NceMessageUtilTest extends TestCase {

    // ensure that the static useBinary value is left OK
    int saveCommandOptions;
    NceTrafficController tc = new NceTrafficController();

    @Override
    public void setUp() {
        saveCommandOptions = tc.getCommandOptions();
    }

    @Override
    public void tearDown() {
        tc.commandOptionSet = false;	// kill warning message
        tc.setCommandOptions(saveCommandOptions);
        Assert.assertTrue("Command has been set", tc.commandOptionSet);
        tc.commandOptionSet = false;	// kill warning message
    }

    public void testCreate() {
        NceMessage m = new NceMessage(1);
        Assert.assertNotNull("exists", m);
    }

    public void testGetEnableAscii() {
        tc.setCommandOptions(NceTrafficController.OPTION_FORCE_ASCII);
        NceMessage m = NceMessageUtil.getEnableMain(tc);
        Assert.assertEquals("length", 1, m.getNumDataElements());
        Assert.assertEquals("opCode", 'E', m.getOpCode());
    }

    public void testGetEnableBinary() {
        tc.setCommandOptions(NceTrafficController.OPTION_1999);
        NceMessage m = NceMessageUtil.getEnableMain(tc);
        Assert.assertEquals("length", 1, m.getNumDataElements());
        Assert.assertEquals("opCode", 0x89, m.getOpCode());
    }

    public void testRecognizeEnable() {
        NceMessage m = NceMessageUtil.getEnableMain(tc);
        Assert.assertEquals("isEnableMain", true, m.isEnableMain());
        Assert.assertEquals("isKillMain", false, m.isKillMain());
    }

    public void testReadPagedCVAscii() {
        tc.setCommandOptions(NceTrafficController.OPTION_2004);
        NceMessage m = NceMessageUtil.getReadPagedCV(tc, 12);
        Assert.assertEquals("string compare ", "R012", m.toString());
    }

    public void testReadPagedCVBin() {
        tc.setCommandOptions(NceTrafficController.OPTION_2006);
        NceMessage m = NceMessageUtil.getReadPagedCV(tc, 12);
        Assert.assertEquals("string compare ", "A1 00 0C", m.toString());
    }

    public void testWritePagedCVAscii() {
        tc.setCommandOptions(NceTrafficController.OPTION_2004);
        NceMessage m = NceMessageUtil.getWritePagedCV(tc, 12, 251);
        Assert.assertEquals("string compare ", "P012 251", m.toString());
    }

    public void testWritePagedCVBin() {
        tc.setCommandOptions(NceTrafficController.OPTION_2006);
        NceMessage m = NceMessageUtil.getWritePagedCV(tc, 12, 251);
        Assert.assertEquals("string compare ", "A0 00 0C FB", m.toString());
    }

    public void testReadRegisterAscii() {
        tc.setCommandOptions(NceTrafficController.OPTION_2004);
        NceMessage m = NceMessageUtil.getReadRegister(tc, 2);
        Assert.assertEquals("string compare ", "V2", m.toString());
    }

    public void testReadRegisterBin() {
        tc.setCommandOptions(NceTrafficController.OPTION_2006);
        NceMessage m = NceMessageUtil.getReadRegister(tc, 2);
        Assert.assertEquals("string compare ", "A7 02", m.toString());
    }

    public void testWriteRegisterAscii() {
        tc.setCommandOptions(NceTrafficController.OPTION_2004);
        NceMessage m = NceMessageUtil.getWriteRegister(tc, 2, 251);
        Assert.assertEquals("string compare ", "S2 251", m.toString());
    }

    public void testWriteRegisterBin() {
        tc.setCommandOptions(NceTrafficController.OPTION_2006);
        NceMessage m = NceMessageUtil.getWriteRegister(tc, 2, 251);
        Assert.assertEquals("string compare ", "A6 02 FB", m.toString());
    }

    public void testCheckPacketMessage1Ascii() {
        tc.setCommandOptions(NceTrafficController.OPTION_FORCE_ASCII);
        NceMessage m = NceMessageUtil.sendPacketMessage(tc, new byte[]{(byte) 0x81, (byte) 0xff, (byte) 0x7e});
        Assert.assertEquals("content", "S C02 81 FF 7E", m.toString());
    }

    public void testCheckPacketMessage1Bin() {
        tc.setCommandOptions(NceTrafficController.OPTION_1999);
        NceMessage m = NceMessageUtil.sendPacketMessage(tc, new byte[]{(byte) 0x81, (byte) 0xff, (byte) 0x7e});
        Assert.assertEquals("content", "93 02 81 FF 7E", m.toString());
    }

    // from here down is testing infrastructure
    public NceMessageUtilTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NceMessageUtilTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NceMessageUtilTest.class);
        return suite;
    }

}
