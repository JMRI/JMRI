/**
 * LnTurnoutTest.java
 *
 * Description:	    tests for the jmri.jmrix.loconet.LnTurnout class
 * @author			Bob Jacobsen
 * @version
 */

package jmri.jmrix.loconet;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.loconet.*;

public class LnSensorAddressTest extends TestCase {

	public void testLnSensorAddressCreate() {
		LnSensorAddress a1 = new LnSensorAddress("LS001");
		LnSensorAddress a2 = new LnSensorAddress("LS001A");
		LnSensorAddress a3 = new LnSensorAddress("LS001C3");
		LnSensorAddress a4 = new LnSensorAddress(0x15, 0x60); // LS043
	}

	public void testLnSensorInvalid() {
		LnSensorAddress a;
		log.error("expect next message: ERROR - Can't parse sensor address string: foo");
		a = new LnSensorAddress("foo");
		assertTrue(!a.isValid());
	}

	public void testLnSensorAddressASmode() {
		LnSensorAddress a;

		a = new LnSensorAddress("LS130A");
		assertTrue(a.getLowBits() == 2);
		assertTrue(a.getHighBits() == 1);
		assertTrue(a.getASBit() == 0x40);
		assertTrue(a.isValid());

		a = new LnSensorAddress("LS257S");
		assertTrue(a.getLowBits() == 1);
		assertTrue(a.getHighBits() == 2);
		assertTrue(a.getASBit() == 0x00);
		assertTrue(a.isValid());

	}

	public void testLnSensorAddressNumericMode() {
		LnSensorAddress a;

		a = new LnSensorAddress("LS130A2"); // 0x0822
		assertTrue(a.getLowBits() == 17);
		assertTrue(a.getHighBits() == 16);
		assertTrue(a.getASBit() == 0x00);
		assertTrue(a.isValid());

		a = new LnSensorAddress("LS257D3");  // 0x101F
		assert(a.getLowBits() == 15);
		assert(a.getHighBits() == 32);
		assert(a.getASBit() == 0x40);
		assert(a.isValid());

	}

	public void testLnSensorAddressBDL16Mode() {
		LnSensorAddress a;

		a = new LnSensorAddress("LS130");
		assert(a.getLowBits() == 65);
		assert(a.getHighBits() == 0);
		assert(a.getASBit() == 0x00);
		assert(a.isValid());

		a = new LnSensorAddress("LS257");
		assert(a.getLowBits() == 0);
		assert(a.getHighBits() == 1);
		assert(a.getASBit() == 0x40);
		assert(a.isValid());

	}

	public void testLnSensorAddressFromPacket() {
		LnSensorAddress a;

		a = new LnSensorAddress(0x15, 0x60); // LS043
		log.debug("0x15, 0x60 shows as "+a.getNumericAddress()+" "+
							a.getDS54Address()+" "+a.getBDL16Address());
		assert(a.getNumericAddress().equals("LS43"));
		assert(a.getDS54Address().equals("LS21A"));
		assert(a.getBDL16Address().equals("LS2C3"));

	}

	// from here down is testing infrastructure

	public LnSensorAddressTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {LnSensorAddressTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LnSensorAddressTest.class);
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnSensorAddressTest.class.getName());

}
