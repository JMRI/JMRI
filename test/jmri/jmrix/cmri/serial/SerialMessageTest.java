/**
 * SerialMessageTest.java
 *
 * Description:	    JUnit tests for the SerialMessage class
 * @author			Bob Jacobsen
 * @version			$Revision: 1.1 $
 */

package jmri.jmrix.cmri.serial;

import jmri.*;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SerialMessageTest extends TestCase {

	public void testCreate() {
		SerialMessage m = new SerialMessage(1);
	}

	public void testToBinaryString() {
		SerialMessage m = new SerialMessage(4);
		m.setOpCode(0x81);
		m.setElement(1, 0x02);
		m.setElement(2, 0xA2);
		m.setElement(3, 0x00);
		m.setBinary(true);
		Assert.assertEquals("string compare ", "81 02 a2 00", m.toString());
	}

	public void testToASCIIString() {
		SerialMessage m = new SerialMessage(5);
		m.setOpCode(0x54);
		m.setElement(1, 0x20);
		m.setElement(2, 0x32);
		m.setElement(3, 0x04);
		m.setElement(4, 0x05);
		m.setBinary(false);
		Assert.assertEquals("string compare ", "54 20 32 04 05", m.toString());
	}

	// from here down is testing infrastructure

	public SerialMessageTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {SerialMessageTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(SerialMessageTest.class);
		return suite;
	}

}
