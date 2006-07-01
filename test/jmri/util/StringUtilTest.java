// StringUtilTest.java

package jmri.util;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.util.StringUtil class
 * @author	Bob Jacobsen  Copyright 2003
 * @version	$Revision: 1.2 $
 */
public class StringUtilTest extends TestCase {

    public void testHexFromInt() {
        Assert.assertEquals("00",StringUtil.twoHexFromInt(0));
        Assert.assertEquals("01",StringUtil.twoHexFromInt(1));
        Assert.assertEquals("02",StringUtil.twoHexFromInt(2));
        Assert.assertEquals("03",StringUtil.twoHexFromInt(3));
        Assert.assertEquals("09",StringUtil.twoHexFromInt(9));
        Assert.assertEquals("0a",StringUtil.twoHexFromInt(10));
        Assert.assertEquals("0b",StringUtil.twoHexFromInt(11));
        Assert.assertEquals("0e",StringUtil.twoHexFromInt(14));
        Assert.assertEquals("0f",StringUtil.twoHexFromInt(15));
        Assert.assertEquals("10",StringUtil.twoHexFromInt(16));
        Assert.assertEquals("11",StringUtil.twoHexFromInt(17));
        Assert.assertEquals("80",StringUtil.twoHexFromInt(0x80));
        Assert.assertEquals("ff",StringUtil.twoHexFromInt(0xFF));
    }

    public void testHexFromIntFromByte() {
        Assert.assertEquals("00",StringUtil.twoHexFromInt((byte)0));
        Assert.assertEquals("01",StringUtil.twoHexFromInt((byte)1));
        Assert.assertEquals("02",StringUtil.twoHexFromInt((byte)2));
        Assert.assertEquals("11",StringUtil.twoHexFromInt((byte)17));
        Assert.assertEquals("80",StringUtil.twoHexFromInt((byte)0x80));
        Assert.assertEquals("ff",StringUtil.twoHexFromInt((byte)0xFF));
    }

	public void testParseStringNull() {
		byte[] b = StringUtil.bytesFromHexString("");
		Assert.assertEquals("array length",0,b.length);
	}


	public void testParseSingleDigit() {
		byte[] b = StringUtil.bytesFromHexString("A");
		Assert.assertEquals("array length",1,b.length);
		Assert.assertEquals("0th byte",0x0A,b[0]);
	}

	public void testParseDoubleDigit() {
		byte[] b = StringUtil.bytesFromHexString("AB");
		Assert.assertEquals("array length",1,b.length);
		Assert.assertEquals("0th byte",0xAB,b[0]&0xFF);
	}

	public void testParseSeveral() {
		byte[] b = StringUtil.bytesFromHexString("12 34 AB 3 19 6 B B1");
		Assert.assertEquals("array length",8,b.length);
		Assert.assertEquals("0th byte",0x12,b[0]&0xFF);
		Assert.assertEquals("1st byte",0x34,b[1]&0xFF);
		Assert.assertEquals("2nd byte",0xAB,b[2]&0xFF);
		Assert.assertEquals("3rd byte",0x03,b[3]&0xFF);
		Assert.assertEquals("4th byte",0x19,b[4]&0xFF);
		Assert.assertEquals("5th byte",0x06,b[5]&0xFF);
		Assert.assertEquals("6th byte",0x0B,b[6]&0xFF);
		Assert.assertEquals("7th byte",0xB1,b[7]&0xFF);
	}


	// from here down is testing infrastructure

	public StringUtilTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {StringUtilTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(StringUtilTest.class);
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(StringUtilTest.class.getName());

}
