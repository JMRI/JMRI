/** 
 * NmraPacketTest.java
 *
 * Description:	
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

import jmri.*;

public class NmraPacketTest extends TestCase {

	// create an accessory decoder packet
	public void testAccDecoderPacket1() {
		// test fixed bits
		byte[] ba = NmraPacket.accDecoderPkt(0, 0, 0);
		Assert.assertEquals("first byte ", 0x80, ba[0] & 0xFF);
		Assert.assertEquals("second byte ", 0x80, ba[1] & 0xFF);
		Assert.assertEquals("third byte ", 0x00, ba[2] & 0xFF);	
	}
	public void testAccDecoderPacket2() {
		// test C bit
		byte[] ba = NmraPacket.accDecoderPkt(0, 1, 0);
		Assert.assertEquals("first byte ", 0x80, ba[0] & 0xFF);
		Assert.assertEquals("second byte ", 0x88, ba[1] & 0xFF);
		Assert.assertEquals("third byte ", 0x08, ba[2] & 0xFF);	
	}
	public void testAccDecoderPacket3() {
		// test D bits
		byte[] ba = NmraPacket.accDecoderPkt(0, 0, 7);
		Assert.assertEquals("first byte ", 0x80, ba[0] & 0xFF);
		Assert.assertEquals("second byte ", 0x87, ba[1] & 0xFF);
		Assert.assertEquals("third byte ", 0x07, ba[2] & 0xFF);	
	}
	public void testAccDecoderPacket4() {
		// test short part of address
		byte[] ba = NmraPacket.accDecoderPkt(3, 0, 0);
		Assert.assertEquals("first byte ", 0x80, ba[0] & 0xFF);
		Assert.assertEquals("second byte ", 0xB0, ba[1] & 0xFF);
		Assert.assertEquals("third byte ", 0x30, ba[2] & 0xFF);	
	}
	public void testAccDecoderPacket5() {
		// test top part of address
		byte[] ba = NmraPacket.accDecoderPkt(128, 0, 0);
		Assert.assertEquals("first byte ", 0x90, ba[0] & 0xFF);
		Assert.assertEquals("second byte ", 0x80, ba[1] & 0xFF);
		Assert.assertEquals("third byte ", 0x10, ba[2] & 0xFF);	
	}
	public void testAccDecoderPacket6() {
		// "all bits set" test
		byte[] ba = NmraPacket.accDecoderPkt(511, 1, 15);
		Assert.assertEquals("first byte ", 0xBF, ba[0] & 0xFF);
		Assert.assertEquals("second byte ", 0xFF, ba[1] & 0xFF);
		Assert.assertEquals("third byte ", 0x40, ba[2] & 0xFF);	
	}
	public void testAccDecoderPacket7() {
		// "typical packet" test
		byte[] ba = NmraPacket.accDecoderPkt(33, 1, 5);
		Assert.assertEquals("first byte ", 0x84, ba[0] & 0xFF);
		Assert.assertEquals("second byte ", 0x9D, ba[1] & 0xFF);
		Assert.assertEquals("third byte ", 0x19, ba[2] & 0xFF);	
	}

	// from here down is testing infrastructure	
	public NmraPacketTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {NmraPacketTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		AllTest.initLogging();
		TestSuite suite = new TestSuite(NmraPacketTest.class);
		return suite;
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NmraPacketTest.class.getName());

}
