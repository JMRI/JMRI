package jmri.util;

import java.util.Arrays;
import org.junit.*;

/**
 * Tests for the jmri.util.StringUtil class.
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class StringUtilTest {

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testFindState() {
        String[] s = new String[]{"A", "B", "C"};
        int[] n = new int[]{20, 30, 40};

        Assert.assertEquals("A", 20, StringUtil.getStateFromName("A", n, s));
        Assert.assertEquals("B", 30, StringUtil.getStateFromName("B", n, s));
        Assert.assertEquals("C", 40, StringUtil.getStateFromName("C", n, s));
    }

    @Test
    public void testFindName() {
        String[] s = new String[]{"A", "B", "C"};
        int[] n = new int[]{20, 30, 40};

        Assert.assertEquals("A", "A", StringUtil.getNameFromState(20, n, s));
        Assert.assertEquals("B", "B", StringUtil.getNameFromState(30, n, s));
        Assert.assertEquals("C", "C", StringUtil.getNameFromState(40, n, s));
    }

    @Test
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

    @Test
    public void testHexFromIntFromByte() {
        Assert.assertEquals("00", StringUtil.twoHexFromInt((byte) 0));
        Assert.assertEquals("01", StringUtil.twoHexFromInt((byte) 1));
        Assert.assertEquals("02", StringUtil.twoHexFromInt((byte) 2));
        Assert.assertEquals("11", StringUtil.twoHexFromInt((byte) 17));
        Assert.assertEquals("80", StringUtil.twoHexFromInt((byte) 0x80));
        Assert.assertEquals("FF", StringUtil.twoHexFromInt((byte) 0xFF));
    }

    @Test
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

    @Test
    public void testAppHexFromIntFromByte() {
        Assert.assertEquals("00", StringUtil.appendTwoHexFromInt((byte) 0, ""));
        Assert.assertEquals("01", StringUtil.appendTwoHexFromInt((byte) 1, ""));
        Assert.assertEquals("02", StringUtil.appendTwoHexFromInt((byte) 2, ""));
        Assert.assertEquals("11", StringUtil.appendTwoHexFromInt((byte) 17, ""));
        Assert.assertEquals("80", StringUtil.appendTwoHexFromInt((byte) 0x80, ""));
        Assert.assertEquals("FF", StringUtil.appendTwoHexFromInt((byte) 0xFF, ""));
    }
    
    @Test
    public void testParseStringNull() {
        byte[] b = StringUtil.bytesFromHexString("");
        Assert.assertEquals("array length", 0, b.length);
    }

    @Test
    public void testParseSingleDigit() {
        byte[] b = StringUtil.bytesFromHexString("A");
        Assert.assertEquals("array length", 1, b.length);
        Assert.assertEquals("0th byte", 0x0A, b[0]);
    }

    @Test
    public void testParseDoubleDigit() {
        byte[] b = StringUtil.bytesFromHexString("AB");
        Assert.assertEquals("array length", 1, b.length);
        Assert.assertEquals("0th byte", 0xAB, b[0] & 0xFF);
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testGetFirstIntFromString() {
        Assert.assertEquals("F aABC123DEFb", 123, StringUtil.getFirstIntFromString("aABC123DEFb"));
        Assert.assertEquals("F no val", -1, StringUtil.getFirstIntFromString(""));
        Assert.assertEquals("F 0", 0, StringUtil.getFirstIntFromString("0"));
        Assert.assertEquals("F +2", 2, StringUtil.getFirstIntFromString("+2"));
        Assert.assertEquals("F -5", 5, StringUtil.getFirstIntFromString("-5"));
        Assert.assertEquals("F ABC123DEF", 123, StringUtil.getFirstIntFromString("ABC123DEF"));
        Assert.assertEquals("F ABC123", 123, StringUtil.getFirstIntFromString("ABC123"));
        Assert.assertEquals("F 123", 123, StringUtil.getFirstIntFromString("123"));
        Assert.assertEquals("F 123ABC", 123, StringUtil.getFirstIntFromString("123ABC"));
        Assert.assertEquals("F AB12 34ABC", 12, StringUtil.getFirstIntFromString("AB12 34ABC"));
        Assert.assertEquals("F 123 ABC ", 123, StringUtil.getFirstIntFromString("123 ABC"));
        Assert.assertEquals("F 123ABC456", 123, StringUtil.getFirstIntFromString("123ABC456"));
        Assert.assertEquals("F 123A654BC456", 123, StringUtil.getFirstIntFromString("123A654BC456"));
        Assert.assertEquals("F XD+123ABC-456", 123, StringUtil.getFirstIntFromString("XD+123ABC-456"));
        Assert.assertEquals("F A c456fg123ABC789jh", 456, StringUtil.getFirstIntFromString("A c456fg123ABC789jh"));
    }

    @Test
    public void testGetLastIntFromString() {
        Assert.assertEquals("aABC123DEFb", 123, StringUtil.getLastIntFromString("aABC123DEFb"));
        Assert.assertEquals("no val", -1, StringUtil.getLastIntFromString(""));
        Assert.assertEquals("0", 0, StringUtil.getLastIntFromString("0"));
        Assert.assertEquals("+2", 2, StringUtil.getLastIntFromString("+2"));
        Assert.assertEquals("-5", 5, StringUtil.getLastIntFromString("-5"));
        Assert.assertEquals("ABC123DEF", 123, StringUtil.getLastIntFromString("ABC123DEF"));
        Assert.assertEquals("ABC123", 123, StringUtil.getLastIntFromString("ABC123"));
        Assert.assertEquals("123", 123, StringUtil.getLastIntFromString("123"));
        Assert.assertEquals("123ABC", 123, StringUtil.getLastIntFromString("123ABC"));
        Assert.assertEquals("AB12 34ABC", 34, StringUtil.getLastIntFromString("AB12 34ABC"));
        Assert.assertEquals("123 ABC ", 123, StringUtil.getLastIntFromString("123 ABC "));
        Assert.assertEquals("123ABC456", 456, StringUtil.getLastIntFromString("123ABC456"));
        Assert.assertEquals("123A654BC456", 456, StringUtil.getLastIntFromString("123A654BC456"));
        Assert.assertEquals("XD+123ABC-456", 456, StringUtil.getLastIntFromString("XD+123ABC-456"));
        Assert.assertEquals("Ac456fg123ABC789jh", 789, StringUtil.getLastIntFromString("Ac456fg123ABC789jh"));
    }

    @Test
    public void testReplaceLast() {
        Assert.assertEquals("no vals", "", StringUtil.replaceLast("", "", ""));
        Assert.assertEquals("D4F5gaz", "D4F5gaz", StringUtil.replaceLast("D4F5gaz", "", ""));
        Assert.assertEquals("D4F5gaz F5 S1", "D4S1gaz", StringUtil.replaceLast("D4F5gaz", "F5", "S1"));
        Assert.assertEquals("D4F5g1234", "D4F5g1234", StringUtil.replaceLast("D4F5g123", "123", "1234"));
        Assert.assertEquals("77YYYzz", "77YYYzz", StringUtil.replaceLast("xxYYYzz", "xx", "77"));
        Assert.assertEquals("xxAA77YYYzz", "xxAA77YYYzz", StringUtil.replaceLast("xxAAxxYYYzz", "xx", "77"));
        Assert.assertEquals("122", "122", StringUtil.replaceLast("121", "1", "2"));
        Assert.assertEquals("122 Z", "121", StringUtil.replaceLast("121", "Z", "2"));
    }

    /**
     * Test of concatTextHtmlAware method, of class StringUtil.
     */
    @Test
    public void testConcatTextHtmlAware() {
        String baseText = "Some text";
        String extraText = " and extra stuff";

        String baseTextHtml = "<html>" + baseText + "</html>";
        String extraTextHtml = "<html>" + extraText + "</html>";

        String baseTextHtmlUpper = "<HTML>" + baseText + "</HTML>";
        String extraTextHtmlUpper = "<HTML>" + extraText + "</HTML>";

        String expResultNoHtml = baseText + extraText;
        String expResultHtml = "<html>" + expResultNoHtml + "</html>";

        String result;

        // test null cases
        Assert.assertEquals(StringUtil.concatTextHtmlAware(null, null), null);
        Assert.assertEquals(StringUtil.concatTextHtmlAware(baseText, null), baseText);
        Assert.assertEquals(StringUtil.concatTextHtmlAware(null, extraText), extraText);
        Assert.assertEquals(StringUtil.concatTextHtmlAware(baseTextHtml, null), baseTextHtml);
        Assert.assertEquals(StringUtil.concatTextHtmlAware(null, extraTextHtml), extraTextHtml);

        // test with no HTML
        result = StringUtil.concatTextHtmlAware(baseText, extraText);
        Assert.assertEquals(expResultNoHtml, result);

        // test with baseText HTML
        result = StringUtil.concatTextHtmlAware(baseTextHtml, extraText);
        Assert.assertEquals(expResultHtml, result);

        // test with extraText HTML
        result = StringUtil.concatTextHtmlAware(baseText, extraTextHtml);
        Assert.assertEquals(expResultHtml, result);

        // test with both HTML
        result = StringUtil.concatTextHtmlAware(baseTextHtml, extraTextHtml);
        Assert.assertEquals(expResultHtml, result);

        // test with baseText uppercase HTML
        result = StringUtil.concatTextHtmlAware(baseTextHtmlUpper, extraTextHtml);
        Assert.assertEquals(expResultHtml, result);

        // test with extraText uppercase HTML
        result = StringUtil.concatTextHtmlAware(baseTextHtml, extraTextHtmlUpper);
        Assert.assertEquals(expResultHtml, result);

        // test with both uppercase HTML
        result = StringUtil.concatTextHtmlAware(baseTextHtmlUpper, extraTextHtmlUpper);
        Assert.assertEquals(expResultHtml, result);
    }
    
    @Test
    public void testFullTextToHexArray() {
        
        byte[] b = StringUtil.fullTextToHexArray("PAN",7);
        Assert.assertEquals("array length", 7, b.length);
        Assert.assertEquals("0th byte", 0x50, b[0] & 0xFF);
        Assert.assertEquals("1st byte", 0x41, b[1] & 0xFF);
        Assert.assertEquals("2nd byte", 0x4E, b[2] & 0xFF);
        Assert.assertEquals("3rd byte", 0x20, b[3] & 0xFF);
        Assert.assertEquals("4th byte", 0x20, b[4] & 0xFF);
        Assert.assertEquals("5th byte", 0x20, b[5] & 0xFF);
        Assert.assertEquals("6th byte", 0x20, b[6] & 0xFF);
        
        b = StringUtil.fullTextToHexArray("CATjhgkjhg jkhg kjhg kjhg jhg ",2);
        Assert.assertEquals("array length", 2, b.length);
        Assert.assertEquals("0th byte", 0x43, b[0] & 0xFF);
        Assert.assertEquals("1st byte", 0x41, b[1] & 0xFF);
        
        b = StringUtil.fullTextToHexArray("My FroG",8);
        Assert.assertEquals("array length", 8, b.length);
        Assert.assertEquals("0th byte", 0x4d, b[0] & 0xFF);
        Assert.assertEquals("1st byte", 0x79, b[1] & 0xFF);
        Assert.assertEquals("2st byte", 0x20, b[2] & 0xFF);
        Assert.assertEquals("3st byte", 0x46, b[3] & 0xFF);
        Assert.assertEquals("4st byte", 0x72, b[4] & 0xFF);
        Assert.assertEquals("5st byte", 0x6f, b[5] & 0xFF);
        Assert.assertEquals("6st byte", 0x47, b[6] & 0xFF);
        Assert.assertEquals("7th byte", 0x20, b[7] & 0xFF);
     
    }
    
    @Test
    public void testintBytesWithTotalFromNonSpacedHexString(){
        
        int[] b = StringUtil.intBytesWithTotalFromNonSpacedHexString("01020AB121",true);
        Assert.assertEquals("array length", 6, b.length);
        Assert.assertEquals("01020AB121 true","[5, 1, 2, 10, 177, 33]",Arrays.toString(b) );
        
        b = StringUtil.intBytesWithTotalFromNonSpacedHexString("01020ab121",true);
        Assert.assertEquals("01020ab121 true","[5, 1, 2, 10, 177, 33]",Arrays.toString(b) );
        
        b = StringUtil.intBytesWithTotalFromNonSpacedHexString("01020ab121",false);
        Assert.assertEquals("01020ab121 false","[1, 2, 10, 177, 33]",Arrays.toString(b) );
        
        b = StringUtil.intBytesWithTotalFromNonSpacedHexString("010",true);
        Assert.assertEquals("010 true","[0]",Arrays.toString(b) );
        
    }
    
    @Test
    public void testGetByte(){
        
        Assert.assertEquals("010203 2", 3, StringUtil.getByte(2,"010203") );
        Assert.assertEquals("010203 -1", 0, StringUtil.getByte(-1,"010203") );
        Assert.assertEquals("AbCdEf 1", 205, StringUtil.getByte(1,"AbCdEf") );
    }
    
    @Test
    public void testGetHexDigit(){
        
        Assert.assertEquals("010203 0", 0, StringUtil.getHexDigit(0,"010203") );
        Assert.assertEquals("010203 1", 1, StringUtil.getHexDigit(1,"010203") );
        Assert.assertEquals("010203 2", 0, StringUtil.getHexDigit(2,"010203") );
        Assert.assertEquals("010203 3", 2, StringUtil.getHexDigit(3,"010203") );
        Assert.assertEquals("010203 4", 0, StringUtil.getHexDigit(4,"010203") );
        Assert.assertEquals("010203 5", 3, StringUtil.getHexDigit(5,"010203") );
        Assert.assertEquals("010F03 3", 15, StringUtil.getHexDigit(3,"010F03") );
        
    }
    
    @Test
    public void testHexStringFromBytes(){
        Assert.assertEquals("Zero Length Array", "", StringUtil.hexStringFromBytes(new byte[] {}) );
        Assert.assertEquals("00010203", "00 01 02 03 ", StringUtil.hexStringFromBytes(new byte[]{0,1,2,3}) );
        Assert.assertEquals("0", "00 ", StringUtil.hexStringFromBytes(new byte[]{0}) );
        Assert.assertEquals("-1", "FF ", StringUtil.hexStringFromBytes(new byte[]{-1}) );
        Assert.assertEquals("-1", "AB CD ", StringUtil.hexStringFromBytes(new byte[]{(byte) 0xab,(byte) 0xcd}) );
        Assert.assertEquals("45,123,129,217", "2D 7B 81 D9 ", StringUtil.hexStringFromBytes(new byte[] {45,123,(byte)129,(byte)217}) );
        Assert.assertEquals("255,256,257", "FF 00 01 ", StringUtil.hexStringFromBytes(new byte[] {(byte)255,(byte)256,(byte)257}) );
    }
    
    @Test
    public void testHexStringFromInts(){
        Assert.assertEquals("Zero Length Array", "", StringUtil.hexStringFromInts(new int[] {}) );
        Assert.assertEquals("-1", "FF ", StringUtil.hexStringFromInts(new int[] {-1}) );
        Assert.assertEquals("0", "00 ", StringUtil.hexStringFromInts(new int[] {0}) );
        Assert.assertEquals("0,1", "00 01 ", StringUtil.hexStringFromInts(new int[] {0,1}) );
        Assert.assertEquals("45,123,129,217", "2D 7B 81 D9 ", StringUtil.hexStringFromInts(new int[] {45,123,129,217}) );
        Assert.assertEquals("255,256,257", "FF 00 01 ", StringUtil.hexStringFromInts(new int[] {255,256,257}) );
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }
    
}
