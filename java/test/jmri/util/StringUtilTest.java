package jmri.util;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.util.StringUtil class.
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class StringUtilTest extends TestCase {

    public void testFindMatch1() {
        String[] s = new String[]{"A", "B", "C"};
        int[] num = new int[]{20, 30, 40};
        int[] masks = new int[]{0xFF, 0xFF, 0xFF};

        Assert.assertEquals("A length", 1, StringUtil.getNamesFromStateMasked(20, num, masks, s).length);
        Assert.assertEquals("A value ", "A", StringUtil.getNamesFromStateMasked(20, num, masks, s)[0]);

        Assert.assertEquals("B length", 1, StringUtil.getNamesFromStateMasked(30, num, masks, s).length);
        Assert.assertEquals("B value ", "B", StringUtil.getNamesFromStateMasked(30, num, masks, s)[0]);

        Assert.assertEquals("C length", 1, StringUtil.getNamesFromStateMasked(40, num, masks, s).length);
        Assert.assertEquals("C value ", "C", StringUtil.getNamesFromStateMasked(40, num, masks, s)[0]);

        Assert.assertEquals("D null", 0, StringUtil.getNamesFromStateMasked(80, num, masks, s).length);

    }

    public void testFindMatch2() {
        String[] s = new String[]{"A", "B", "C"};
        int[] num = new int[]{0x20, 0x30, 0x40};
        int[] masks = new int[]{0xF0, 0xF0, 0xF0};

        Assert.assertEquals("A length", 1, StringUtil.getNamesFromStateMasked(0x21, num, masks, s).length);
        Assert.assertEquals("A value ", "A", StringUtil.getNamesFromStateMasked(0x21, num, masks, s)[0]);

        Assert.assertEquals("B length", 1, StringUtil.getNamesFromStateMasked(0x34, num, masks, s).length);
        Assert.assertEquals("B value ", "B", StringUtil.getNamesFromStateMasked(0x34, num, masks, s)[0]);

        Assert.assertEquals("C length", 1, StringUtil.getNamesFromStateMasked(0x4F, num, masks, s).length);
        Assert.assertEquals("C value ", "C", StringUtil.getNamesFromStateMasked(0x4F, num, masks, s)[0]);

        Assert.assertEquals("D null", 0, StringUtil.getNamesFromStateMasked(0x80, num, masks, s).length);

    }

    public void testFindMatch3() {
        String[] s = new String[]{"A", "B", "C"};
        int[] num = new int[]{0x20, 0x30, 0x40};
        int[] masks = new int[]{0x20, 0x30, 0x40};

        Assert.assertEquals("A length", 1, StringUtil.getNamesFromStateMasked(0x21, num, masks, s).length);
        Assert.assertEquals("A value ", "A", StringUtil.getNamesFromStateMasked(0x21, num, masks, s)[0]);

        Assert.assertEquals("B length", 2, StringUtil.getNamesFromStateMasked(0x34, num, masks, s).length);
        Assert.assertEquals("B value 1", "A", StringUtil.getNamesFromStateMasked(0x34, num, masks, s)[0]);
        Assert.assertEquals("B value 2", "B", StringUtil.getNamesFromStateMasked(0x34, num, masks, s)[1]);

        Assert.assertEquals("C length", 1, StringUtil.getNamesFromStateMasked(0x4F, num, masks, s).length);
        Assert.assertEquals("C value ", "C", StringUtil.getNamesFromStateMasked(0x4F, num, masks, s)[0]);

        Assert.assertEquals("D null", 0, StringUtil.getNamesFromStateMasked(0x80, num, masks, s).length);

    }

    public void testFindState() {
        String[] s = new String[]{"A", "B", "C"};
        int[] n = new int[]{20, 30, 40};

        Assert.assertEquals("A", 20, StringUtil.getStateFromName("A", n, s));
        Assert.assertEquals("B", 30, StringUtil.getStateFromName("B", n, s));
        Assert.assertEquals("C", 40, StringUtil.getStateFromName("C", n, s));
    }

    public void testFindName() {
        String[] s = new String[]{"A", "B", "C"};
        int[] n = new int[]{20, 30, 40};

        Assert.assertEquals("A", "A", StringUtil.getNameFromState(20, n, s));
        Assert.assertEquals("B", "B", StringUtil.getNameFromState(30, n, s));
        Assert.assertEquals("C", "C", StringUtil.getNameFromState(40, n, s));
    }

    public void testHexFromInt() {
        Assert.assertEquals("00", StringUtil.twoHexFromInt(0));
        Assert.assertEquals("01", StringUtil.twoHexFromInt(1));
        Assert.assertEquals("02", StringUtil.twoHexFromInt(2));
        Assert.assertEquals("03", StringUtil.twoHexFromInt(3));
        Assert.assertEquals("09", StringUtil.twoHexFromInt(9));
        Assert.assertEquals("0A", StringUtil.twoHexFromInt(10));
        Assert.assertEquals("0B", StringUtil.twoHexFromInt(11));
        Assert.assertEquals("0E", StringUtil.twoHexFromInt(14));
        Assert.assertEquals("0F", StringUtil.twoHexFromInt(15));
        Assert.assertEquals("10", StringUtil.twoHexFromInt(16));
        Assert.assertEquals("11", StringUtil.twoHexFromInt(17));
        Assert.assertEquals("80", StringUtil.twoHexFromInt(0x80));
        Assert.assertEquals("FF", StringUtil.twoHexFromInt(0xFF));
    }

    public void testHexFromIntFromByte() {
        Assert.assertEquals("00", StringUtil.twoHexFromInt((byte) 0));
        Assert.assertEquals("01", StringUtil.twoHexFromInt((byte) 1));
        Assert.assertEquals("02", StringUtil.twoHexFromInt((byte) 2));
        Assert.assertEquals("11", StringUtil.twoHexFromInt((byte) 17));
        Assert.assertEquals("80", StringUtil.twoHexFromInt((byte) 0x80));
        Assert.assertEquals("FF", StringUtil.twoHexFromInt((byte) 0xFF));
    }

    public void testAppHexFromInt() {
        Assert.assertEquals("00", StringUtil.appendTwoHexFromInt(0, ""));
        Assert.assertEquals("01", StringUtil.appendTwoHexFromInt(1, ""));
        Assert.assertEquals("02", StringUtil.appendTwoHexFromInt(2, ""));
        Assert.assertEquals("03", StringUtil.appendTwoHexFromInt(3, ""));
        Assert.assertEquals("09", StringUtil.appendTwoHexFromInt(9, ""));
        Assert.assertEquals("0A", StringUtil.appendTwoHexFromInt(10, ""));
        Assert.assertEquals("0B", StringUtil.appendTwoHexFromInt(11, ""));
        Assert.assertEquals("0E", StringUtil.appendTwoHexFromInt(14, ""));
        Assert.assertEquals("0F", StringUtil.appendTwoHexFromInt(15, ""));
        Assert.assertEquals("10", StringUtil.appendTwoHexFromInt(16, ""));
        Assert.assertEquals("11", StringUtil.appendTwoHexFromInt(17, ""));
        Assert.assertEquals("80", StringUtil.appendTwoHexFromInt(0x80, ""));
        Assert.assertEquals("FF", StringUtil.appendTwoHexFromInt(0xFF, ""));
    }

    public void testAppHexFromIntFromByte() {
        Assert.assertEquals("00", StringUtil.appendTwoHexFromInt((byte) 0, ""));
        Assert.assertEquals("01", StringUtil.appendTwoHexFromInt((byte) 1, ""));
        Assert.assertEquals("02", StringUtil.appendTwoHexFromInt((byte) 2, ""));
        Assert.assertEquals("11", StringUtil.appendTwoHexFromInt((byte) 17, ""));
        Assert.assertEquals("80", StringUtil.appendTwoHexFromInt((byte) 0x80, ""));
        Assert.assertEquals("FF", StringUtil.appendTwoHexFromInt((byte) 0xFF, ""));
    }

    public void testParseStringNull() {
        byte[] b = StringUtil.bytesFromHexString("");
        Assert.assertEquals("array length", 0, b.length);
    }

    public void testParseSingleDigit() {
        byte[] b = StringUtil.bytesFromHexString("A");
        Assert.assertEquals("array length", 1, b.length);
        Assert.assertEquals("0th byte", 0x0A, b[0]);
    }

    public void testParseDoubleDigit() {
        byte[] b = StringUtil.bytesFromHexString("AB");
        Assert.assertEquals("array length", 1, b.length);
        Assert.assertEquals("0th byte", 0xAB, b[0] & 0xFF);
    }

    public void testParseSeveral() {
        byte[] b = StringUtil.bytesFromHexString("12 34 AB 3 19 6 B B1");
        Assert.assertEquals("array length", 8, b.length);
        Assert.assertEquals("0th byte", 0x12, b[0] & 0xFF);
        Assert.assertEquals("1st byte", 0x34, b[1] & 0xFF);
        Assert.assertEquals("2nd byte", 0xAB, b[2] & 0xFF);
        Assert.assertEquals("3rd byte", 0x03, b[3] & 0xFF);
        Assert.assertEquals("4th byte", 0x19, b[4] & 0xFF);
        Assert.assertEquals("5th byte", 0x06, b[5] & 0xFF);
        Assert.assertEquals("6th byte", 0x0B, b[6] & 0xFF);
        Assert.assertEquals("7th byte", 0xB1, b[7] & 0xFF);
    }

    @SuppressWarnings("null")
    private boolean compareStringArray(String[] s1, String[] s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null && s2 != null) {
            return false;
        }
        if (s1 != null && s2 == null) {
            return false;
        }
        // s1 and s2 are not null
        if (s1.length != s2.length) {
            return false;
        }

        for (int i = 0; i < s1.length; i++) {
            if (!s1[i].equals(s2[i])) {
                return false;
            }
        }
        return true;
    }

    public void testJoinA1() {
        String input[] = new String[]{"A", "B", "C"};
        String results = StringUtil.join(input, ".");
        Assert.assertEquals("output", "A.B.C", results);
    }

    public void testSort1() {
        String input[] = new String[]{"A", "B", "C"};
        String output[] = new String[]{"A", "B", "C"};
        StringUtil.sort(input);
        Assert.assertTrue(compareStringArray(input, output));
    }

    public void testSort2() {
        String input[] = new String[]{"A", "b", "C"};
        String output[] = new String[]{"A", "C", "b"};
        StringUtil.sort(input);
        Assert.assertTrue(compareStringArray(input, output));
    }

    public void testSort3() {
        String input[] = new String[]{"B", "C", "A"};
        String output[] = new String[]{"A", "B", "C"};
        StringUtil.sort(input);
        Assert.assertTrue(compareStringArray(input, output));
    }

    public void testSort4() {
        String input[] = new String[]{"c", "b", "a"};
        String output[] = new String[]{"a", "b", "c"};
        StringUtil.sort(input);
        Assert.assertTrue(compareStringArray(input, output));
    }

    public void testSort5() {
        String input[] = new String[]{"A", "c", "b"};
        String output[] = new String[]{"A", "b", "c"};
        StringUtil.sort(input);
        Assert.assertTrue(compareStringArray(input, output));
    }

    public void testSort6() {
        String input[] = new String[]{"A", "A", "b"};
        String output[] = new String[]{"A", "A", "b"};
        StringUtil.sort(input);
        Assert.assertTrue(compareStringArray(input, output));
    }

    public void testSplit1() {
        String input = "abc.cdf";
        String[] result = jmri.util.StringUtil.split(input, ".");
        Assert.assertEquals("length", 2, result.length);
        Assert.assertEquals("item 0", "abc", result[0]);
        Assert.assertEquals("item 1", "cdf", result[1]);
    }

    public void testSplit2() {
        String input = "abcxcdf";
        String[] result = jmri.util.StringUtil.split(input, ".");
        Assert.assertEquals("length", 1, result.length);
        Assert.assertEquals("item 0", "abcxcdf", result[0]);
    }

    public void testSplit3() {
        String input = "abc.cdf.";
        String[] result = jmri.util.StringUtil.split(input, ".");
        Assert.assertEquals("length", 3, result.length);
        Assert.assertEquals("item 0", "abc", result[0]);
        Assert.assertEquals("item 1", "cdf", result[1]);
        Assert.assertEquals("item 2", "", result[2]);
    }

    public void testSplit4() {
        String input = "abc.cdf.ert";
        String[] result = jmri.util.StringUtil.split(input, ".");
        Assert.assertEquals("length", 3, result.length);
        Assert.assertEquals("item 0", "abc", result[0]);
        Assert.assertEquals("item 1", "cdf", result[1]);
        Assert.assertEquals("item 2", "ert", result[2]);
    }

    public void testSplit5() {
        String input = "abc..cdf";
        String[] result = jmri.util.StringUtil.split(input, ".");
        Assert.assertEquals("length", 3, result.length);
        Assert.assertEquals("item 0", "abc", result[0]);
        Assert.assertEquals("item 1", "", result[1]);
        Assert.assertEquals("item 2", "cdf", result[2]);
    }

    public void testSplit6() {
        String input = "abcxcdf.";
        String[] result = jmri.util.StringUtil.split(input, ".");
        Assert.assertEquals("length", 2, result.length);
        Assert.assertEquals("item 0", "abcxcdf", result[0]);
        Assert.assertEquals("item 1", "", result[1]);
    }

    public void testparenQuote() {
        String sample;

        sample = null;
        Assert.assertEquals(sample, sample, StringUtil.parenQuote(sample));
        
        sample = "";
        Assert.assertEquals(sample, sample, StringUtil.parenQuote(sample));
        
        sample = "abc";
        Assert.assertEquals(sample, sample, StringUtil.parenQuote(sample));

        sample = "123";
        Assert.assertEquals(sample, sample, StringUtil.parenQuote(sample));

        sample = "";
        Assert.assertEquals(sample, sample, StringUtil.parenQuote(sample));

        sample = "a\\b";
        Assert.assertEquals(sample, "a\\\\b", StringUtil.parenQuote(sample));

        sample = "a(v)c";
        Assert.assertEquals(sample, sample, StringUtil.parenQuote(sample));

        sample = "a(v(b)(n)K)";
        Assert.assertEquals(sample, sample, StringUtil.parenQuote(sample));

        sample = "()((()))";
        Assert.assertEquals(sample, sample, StringUtil.parenQuote(sample));

        sample = "a)b";
        Assert.assertEquals(sample, "a\\)b", StringUtil.parenQuote(sample));
    }

    public void testparenUnQuote() {
        String sample;

        sample = null;
        Assert.assertEquals(sample, sample, StringUtil.parenUnQuote(sample));

        sample = "";
        Assert.assertEquals(sample, sample, StringUtil.parenUnQuote(sample));

        sample = "abc";
        Assert.assertEquals(sample, sample, StringUtil.parenUnQuote(sample));

        sample = "123";
        Assert.assertEquals(sample, sample, StringUtil.parenUnQuote(sample));

        sample = "";
        Assert.assertEquals(sample, sample, StringUtil.parenUnQuote(sample));

        sample = "a\\\\b";
        Assert.assertEquals(sample, "a\\b", StringUtil.parenUnQuote(sample));

        sample = "a(v)c";
        Assert.assertEquals(sample, sample, StringUtil.parenUnQuote(sample));

        sample = "a(v(b)(n)K)";
        Assert.assertEquals(sample, sample, StringUtil.parenUnQuote(sample));

        sample = "()((()))";
        Assert.assertEquals(sample, sample, StringUtil.parenUnQuote(sample));

        sample = "a\\)b";
        Assert.assertEquals(sample, "a)b", StringUtil.parenUnQuote(sample));
    }

    public void testSplitParens() {
        String sample;
        java.util.List<String> list;

        sample = "(abc)";
        list = StringUtil.splitParens(sample);
        Assert.assertEquals(sample, 1, list.size());
        Assert.assertEquals(sample + " 1", "(abc)", list.get(0));

        sample = "(abc)(def)";
        list = StringUtil.splitParens(sample);
        Assert.assertEquals(sample, 2, list.size());
        Assert.assertEquals(sample + " 1", "(abc)", list.get(0));
        Assert.assertEquals(sample + " 1", "(def)", list.get(1));

    }

    public void testArrayToString() {
        Object[] a = new Object[]{"A", "B", "C"};
        Assert.assertEquals("Object", "[A],[B],[C]", StringUtil.arrayToString(a));

        String[] b = new String[]{"X", "Y", "Z"};
        Assert.assertEquals("Object", "[X],[Y],[Z]", StringUtil.arrayToString(b));

        byte[] c = new byte[]{1, 2, 3};
        Assert.assertEquals("Object", "[1],[2],[3]", StringUtil.arrayToString(c));

        int[] d = new int[]{1, 2, 3};
        Assert.assertEquals("Object", "[1],[2],[3]", StringUtil.arrayToString(d));
    }

    // from here down is testing infrastructure
    public StringUtilTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", StringUtilTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(StringUtilTest.class);
        return suite;
    }

}
