// StringUtilTest.java

package jmri.util;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.util.StringUtil class.
 * @author	Bob Jacobsen  Copyright 2003
 * @version	$Revision: 1.6 $
 */
public class StringUtilTest extends TestCase {


    public void testHexFromInt() {
        Assert.assertEquals("00",StringUtil.twoHexFromInt(0));
        Assert.assertEquals("01",StringUtil.twoHexFromInt(1));
        Assert.assertEquals("02",StringUtil.twoHexFromInt(2));
        Assert.assertEquals("03",StringUtil.twoHexFromInt(3));
        Assert.assertEquals("09",StringUtil.twoHexFromInt(9));
        Assert.assertEquals("0A",StringUtil.twoHexFromInt(10));
        Assert.assertEquals("0B",StringUtil.twoHexFromInt(11));
        Assert.assertEquals("0E",StringUtil.twoHexFromInt(14));
        Assert.assertEquals("0F",StringUtil.twoHexFromInt(15));
        Assert.assertEquals("10",StringUtil.twoHexFromInt(16));
        Assert.assertEquals("11",StringUtil.twoHexFromInt(17));
        Assert.assertEquals("80",StringUtil.twoHexFromInt(0x80));
        Assert.assertEquals("FF",StringUtil.twoHexFromInt(0xFF));
    }

    public void testHexFromIntFromByte() {
        Assert.assertEquals("00",StringUtil.twoHexFromInt((byte)0));
        Assert.assertEquals("01",StringUtil.twoHexFromInt((byte)1));
        Assert.assertEquals("02",StringUtil.twoHexFromInt((byte)2));
        Assert.assertEquals("11",StringUtil.twoHexFromInt((byte)17));
        Assert.assertEquals("80",StringUtil.twoHexFromInt((byte)0x80));
        Assert.assertEquals("FF",StringUtil.twoHexFromInt((byte)0xFF));
    }

    public void testAppHexFromInt() {
        Assert.assertEquals("00",StringUtil.appendTwoHexFromInt(0,""));
        Assert.assertEquals("01",StringUtil.appendTwoHexFromInt(1,""));
        Assert.assertEquals("02",StringUtil.appendTwoHexFromInt(2,""));
        Assert.assertEquals("03",StringUtil.appendTwoHexFromInt(3,""));
        Assert.assertEquals("09",StringUtil.appendTwoHexFromInt(9,""));
        Assert.assertEquals("0A",StringUtil.appendTwoHexFromInt(10,""));
        Assert.assertEquals("0B",StringUtil.appendTwoHexFromInt(11,""));
        Assert.assertEquals("0E",StringUtil.appendTwoHexFromInt(14,""));
        Assert.assertEquals("0F",StringUtil.appendTwoHexFromInt(15,""));
        Assert.assertEquals("10",StringUtil.appendTwoHexFromInt(16,""));
        Assert.assertEquals("11",StringUtil.appendTwoHexFromInt(17,""));
        Assert.assertEquals("80",StringUtil.appendTwoHexFromInt(0x80,""));
        Assert.assertEquals("FF",StringUtil.appendTwoHexFromInt(0xFF,""));
    }

    public void testAppHexFromIntFromByte() {
        Assert.assertEquals("00",StringUtil.appendTwoHexFromInt((byte)0,""));
        Assert.assertEquals("01",StringUtil.appendTwoHexFromInt((byte)1,""));
        Assert.assertEquals("02",StringUtil.appendTwoHexFromInt((byte)2,""));
        Assert.assertEquals("11",StringUtil.appendTwoHexFromInt((byte)17,""));
        Assert.assertEquals("80",StringUtil.appendTwoHexFromInt((byte)0x80,""));
        Assert.assertEquals("FF",StringUtil.appendTwoHexFromInt((byte)0xFF,""));
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

        private boolean compareStringArray(String[] s1, String[] s2) {
            if (s1 == null && s2 == null) return true;
            if (s1 == null && s2 != null) return false;
            if (s1 != null && s2 == null) return false;
            if (s1.length != s2.length) return false;

            for (int i = 0; i<s1.length; i++) {
                if (! s1[i].equals(s2[i]) ) return false;
            }
            return true;
        }

        public void testSort1() {
            String input[] = new String[]{ "A", "B", "C" };
            String output[] = new String[]{ "A", "B", "C" };
            StringUtil.sort(input);
            Assert.assertTrue(compareStringArray(input, output));
        }

        public void testSort2() {
            String input[] = new String[]{ "A", "b", "C" };
            String output[] = new String[]{ "A", "C", "b" };
            StringUtil.sort(input);
            Assert.assertTrue(compareStringArray(input, output));
        }

        public void testSort3() {
            String input[] = new String[]{ "B", "C", "A" };
            String output[] = new String[]{ "A", "B", "C" };
            StringUtil.sort(input);
            Assert.assertTrue(compareStringArray(input, output));
        }

        public void testSort4() {
            String input[] = new String[]{ "c", "b", "a" };
            String output[] = new String[]{ "a", "b", "c" };
            StringUtil.sort(input);
            Assert.assertTrue(compareStringArray(input, output));
        }

        public void testSort5() {
            String input[] = new String[]{ "A", "c", "b" };
            String output[] = new String[]{ "A", "b", "c" };
            StringUtil.sort(input);
            Assert.assertTrue(compareStringArray(input, output));
        }

        public void testSort6() {
            String input[] = new String[]{ "A", "A", "b" };
            String output[] = new String[]{ "A", "A", "b" };
            StringUtil.sort(input);
            Assert.assertTrue(compareStringArray(input, output));
        }

        public void testReplace1() {
            String input = "123456";
            String output = StringUtil.localReplaceAll(input, "1", "X");
            Assert.assertTrue(output.equals("X23456"));
        }

        public void testReplace2() {
            String input = "123456";
            String output = StringUtil.localReplaceAll(input, "6", "X");
            Assert.assertTrue(output.equals("12345X"));
        }

        public void testReplace3() {
            String input = "123\n456";
            String output = StringUtil.localReplaceAll(input, "\n", "\n...>");
            Assert.assertTrue(output.equals("123\n...>456"));
        }

        public void testReplace4() {
            String input = "123\n456\n";
            String output = StringUtil.localReplaceAll(input, "\n", "\n...>");
            Assert.assertTrue(output.equals("123\n...>456\n...>"));
        }

        public void testReplace5() {
            String input = "123\n\n456";
            String output = StringUtil.localReplaceAll(input, "\n", "\n...>");
            Assert.assertTrue(output.equals("123\n...>\n...>456"));
        }

        public void testReplace6() {
            String input = "\n123\n456\n";
            String output = StringUtil.localReplaceAll(input, "\n", "\n...>");
            Assert.assertTrue(output.equals("\n...>123\n...>456\n...>"));
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
