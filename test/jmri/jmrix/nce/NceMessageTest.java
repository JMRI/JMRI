// NceMessageTest.java

package jmri.jmrix.nce;

import jmri.*;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the NceMessage class
 * @author			Bob Jacobsen Copyright 2002-2004
 * @version			$Revision: 1.5 $
 */

public class NceMessageTest extends TestCase {

    // ensure that the static useBinary value is left OK
    static boolean saveUseBinary;
    
    public void setUp() {
        saveUseBinary = NceMessage.useBinary;
    }
    
    public void tearDown() {
        NceMessage.useBinary = saveUseBinary;
    }
    
	public void testCreate() {
		NceMessage m = new NceMessage(1);
	}

	public void testToBinaryString() {
		NceMessage m = new NceMessage(4);
		m.setOpCode(0x81);
		m.setElement(1, 0x02);
		m.setElement(2, 0xA2);
		m.setElement(3, 0x00);
		m.setBinary(true);
		Assert.assertEquals("string compare ", "81 02 A2 00", m.toString());
	}

	public void testToASCIIString() {
		NceMessage m = new NceMessage(5);
		m.setOpCode(0x50);
		m.setElement(1, 0x20);
		m.setElement(2, 0x32);
		m.setElement(3, 0x36);
		m.setElement(4, 0x31);
		m.setBinary(false);
		Assert.assertEquals("string compare ", "P 261", m.toString());
	}

	public void testGetEnableAscii() {
	    NceMessage.useBinary = false;
		NceMessage m = NceMessage.getEnableMain();
		Assert.assertEquals("length", 1, m.getNumDataElements());
		Assert.assertEquals("opCode", 'E', m.getOpCode());
	}

	public void testGetEnableBinary() {
	    NceMessage.useBinary = true;
		NceMessage m = NceMessage.getEnableMain();
		Assert.assertEquals("length", 1, m.getNumDataElements());
		Assert.assertEquals("opCode", 0x89, m.getOpCode());
	}
	
	public void testRecognizeEnable() {
		NceMessage m = NceMessage.getEnableMain();
		Assert.assertEquals("isEnableMain", true, m.isEnableMain());
		Assert.assertEquals("isKillMain", false, m.isKillMain());
	}

	public void testReadPagedCV() {
		NceMessage m = NceMessage.getReadPagedCV(12);
		Assert.assertEquals("string compare ", "R012", m.toString());
	}

	public void testWritePagedCV() {
		NceMessage m = NceMessage.getWritePagedCV(12, 251);
		Assert.assertEquals("string compare ", "P012 251", m.toString());
	}

	public void testReadRegister() {
		NceMessage m = NceMessage.getReadRegister(2);
		Assert.assertEquals("string compare ", "V2", m.toString());
	}

	public void testWriteRegister() {
		NceMessage m = NceMessage.getWriteRegister(2, 251);
		Assert.assertEquals("string compare ", "S2 251", m.toString());
	}

	public void testCheckPacketMessage1Ascii() {
	    NceMessage.useBinary = false;
		NceMessage m = NceMessage.sendPacketMessage(new byte[]{(byte)0x81,(byte)0xff,(byte)0x7e});
		Assert.assertEquals("content", "S C02 81 FF 7E", m.toString());
	}

	public void testCheckPacketMessage1Bin() {
	    NceMessage.useBinary = true;
		NceMessage m = NceMessage.sendPacketMessage(new byte[]{(byte)0x81,(byte)0xff,(byte)0x7e});
		Assert.assertEquals("content", "93 02 81 FF 7E", m.toString());
	}

	// from here down is testing infrastructure

	public NceMessageTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {NceMessageTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(NceMessageTest.class);
		return suite;
	}

}
