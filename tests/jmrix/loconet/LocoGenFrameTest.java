/** 
 * LocoGenFrameTest.java
 *
 * Description:	    tests for the jmri.jmrix.loconet.locogen.LocoGenFrame class
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrix.loconet.locogen;

import java.io.*;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.loconet.*;
import jmri.jmrix.loconet.locogen.*;
import jmri.tests.jmrix.loconet.*;

public class LocoGenFrameTest extends TestCase {

	public void testFrameCreate() {
		new LocoGenFrame();		
	}


	public void testParseStringNull() {
		LocoGenFrame t = new LocoGenFrame();		
		int[] b = t.parseString("");
		Assert.assertEquals("array length",0,b.length);
	}


	public void testParseSingleDigit() {
		LocoGenFrame t = new LocoGenFrame();		
		int[] b = t.parseString("A");
		Assert.assertEquals("array length",1,b.length);
		Assert.assertEquals("0th byte",0x0A,b[0]);
	}

	public void testParseDoubleDigit() {
		LocoGenFrame t = new LocoGenFrame();		
		int[] b = t.parseString("AB");
		Assert.assertEquals("array length",1,b.length);
		Assert.assertEquals("0th byte",0xAB,b[0]);
	}

	public void testParseSeveral() {
		LocoGenFrame t = new LocoGenFrame();		
		int[] b = t.parseString("12 34 AB 3 19 6 B B1");
		Assert.assertEquals("array length",8,b.length);
		Assert.assertEquals("0th byte",0x12,b[0]);
		Assert.assertEquals("1st byte",0x34,b[1]);
		Assert.assertEquals("2nd byte",0xAB,b[2]);
		Assert.assertEquals("3rd byte",0x03,b[3]);
		Assert.assertEquals("4th byte",0x19,b[4]);
		Assert.assertEquals("5th byte",0x06,b[5]);
		Assert.assertEquals("6th byte",0x0B,b[6]);
		Assert.assertEquals("7th byte",0xB1,b[7]);
	}

	public void testPacketNull() {
		LocoGenFrame t = new LocoGenFrame();
		LocoNetMessage m = t.createPacket("");
		Assert.assertEquals("null pointer",null,m);
	}

	public void testPacketCreate() {
		LocoGenFrame t = new LocoGenFrame();
		LocoNetMessage m = t.createPacket("12 34 AB 3 19 6 B B1");
		Assert.assertEquals("length",8,m.getNumDataElements());
		Assert.assertEquals("0th byte",0x12,m.getElement(0));
		Assert.assertEquals("1st byte",0x34,m.getElement(1));
		Assert.assertEquals("2nd byte",0xAB,m.getElement(2));
		Assert.assertEquals("3rd byte",0x03,m.getElement(3));
		Assert.assertEquals("4th byte",0x19,m.getElement(4));
		Assert.assertEquals("5th byte",0x06,m.getElement(5));
		Assert.assertEquals("6th byte",0x0B,m.getElement(6));
		Assert.assertEquals("7th byte",0xB1,m.getElement(7));
	}
	
			
		
	// from here down is testing infrastructure
	
	public LocoGenFrameTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {LocoGenFrameTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LocoGenFrameTest.class);
		return suite;
	}
	 
	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoGenFrameTest.class.getName());

}
