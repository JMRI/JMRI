/** 
 * NceMessageTest.java
 *
 * Description:	    JUnit tests for the NceMessage class
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrix.nce;

import jmri.*;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.nce.NceMessage;

public class NceMessageTest extends TestCase {

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
		Assert.assertEquals("string compare ", "81 02 a2 00", m.toString());
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
	
	public void testGetEnable() {
		NceMessage m = NceMessage.getEnableMain();
		Assert.assertEquals("length", 1, m.getNumDataElements());
		Assert.assertEquals("opCode", 'E', m.getOpCode());
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
