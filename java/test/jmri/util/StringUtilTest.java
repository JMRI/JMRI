package jmri.util;

import java.util.Arrays;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for the jmri.util.StringUtil class.
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class StringUtilTest {

    @Test
    public void testFindMatch1() {
        String[] s = new String[]{"A", "B", "C"};
        int[] num = new int[]{20, 30, 40};
        int[] masks = new int[]{0xFF, 0xFF, 0xFF};

        assertEquals( 1, StringUtil.getNamesFromStateMasked(20, num, masks, s).length, "A length");
        assertEquals( "A", StringUtil.getNamesFromStateMasked(20, num, masks, s)[0], "A value ");

        assertEquals( 1, StringUtil.getNamesFromStateMasked(30, num, masks, s).length, "B length");
        assertEquals( "B", StringUtil.getNamesFromStateMasked(30, num, masks, s)[0], "B value ");

        assertEquals( 1, StringUtil.getNamesFromStateMasked(40, num, masks, s).length, "C length");
        assertEquals( "C", StringUtil.getNamesFromStateMasked(40, num, masks, s)[0], "C value ");

        assertEquals( 0, StringUtil.getNamesFromStateMasked(80, num, masks, s).length, "D null");

    }

    @Test
    public void testFindMatch2() {
        String[] s = new String[]{"A", "B", "C"};
        int[] num = new int[]{0x20, 0x30, 0x40};
        int[] masks = new int[]{0xF0, 0xF0, 0xF0};

        assertEquals( 1, StringUtil.getNamesFromStateMasked(0x21, num, masks, s).length, "A length");
        assertEquals( "A", StringUtil.getNamesFromStateMasked(0x21, num, masks, s)[0], "A value ");

        assertEquals( 1, StringUtil.getNamesFromStateMasked(0x34, num, masks, s).length, "B length");
        assertEquals( "B", StringUtil.getNamesFromStateMasked(0x34, num, masks, s)[0], "B value ");

        assertEquals( 1, StringUtil.getNamesFromStateMasked(0x4F, num, masks, s).length, "C length");
        assertEquals( "C", StringUtil.getNamesFromStateMasked(0x4F, num, masks, s)[0], "C value ");

        assertEquals( 0, StringUtil.getNamesFromStateMasked(0x80, num, masks, s).length, "D null");

    }

    @Test
    public void testFindMatch3() {
        String[] s = new String[]{"A", "B", "C"};
        int[] num = new int[]{0x20, 0x30, 0x40};
        int[] masks = new int[]{0x20, 0x30, 0x40};

        assertEquals( 1, StringUtil.getNamesFromStateMasked(0x21, num, masks, s).length, "A length");
        assertEquals( "A", StringUtil.getNamesFromStateMasked(0x21, num, masks, s)[0], "A value ");

        assertEquals( 2, StringUtil.getNamesFromStateMasked(0x34, num, masks, s).length, "B length");
        assertEquals( "A", StringUtil.getNamesFromStateMasked(0x34, num, masks, s)[0], "B value 1");
        assertEquals( "B", StringUtil.getNamesFromStateMasked(0x34, num, masks, s)[1], "B value 2");

        assertEquals( 1, StringUtil.getNamesFromStateMasked(0x4F, num, masks, s).length, "C length");
        assertEquals( "C", StringUtil.getNamesFromStateMasked(0x4F, num, masks, s)[0], "C value ");

        assertEquals( 0, StringUtil.getNamesFromStateMasked(0x80, num, masks, s).length, "D null");

    }

    @Test
    public void testGetNamesFromState() {

        String[] s = new String[]{"A", "B", "C", "D"};
        int[] num = new int[]{0, 1, 2, 4};

        assertEquals( 1, StringUtil.getNamesFromState(0, num, s).length);
        assertEquals( "A", StringUtil.getNamesFromState(0, num, s)[0]);

        assertEquals( 1, StringUtil.getNamesFromState(1, num, s).length);
        assertEquals( "B", StringUtil.getNamesFromState(1, num, s)[0]);

        assertEquals( 1, StringUtil.getNamesFromState(2, num, s).length);
        assertEquals( "C", StringUtil.getNamesFromState(2, num, s)[0]);

        assertEquals( 2, StringUtil.getNamesFromState(3, num, s).length);
        assertEquals( "B", StringUtil.getNamesFromState(3, num, s)[0]);
        assertEquals( "C", StringUtil.getNamesFromState(3, num, s)[1]);

    }

    @Test
    public void testFindState() {
        String[] s = new String[]{"A", "B", "C"};
        int[] n = new int[]{20, 30, 40};

        assertEquals( 20, StringUtil.getStateFromName("A", n, s), "A");
        assertEquals( 30, StringUtil.getStateFromName("B", n, s), "B");
        assertEquals( 40, StringUtil.getStateFromName("C", n, s), "C");
    }

    @Test
    public void testFindName() {
        String[] s = new String[]{"A", "B", "C"};
        int[] n = new int[]{20, 30, 40};

        assertEquals( "A", StringUtil.getNameFromState(20, n, s), "A");
        assertEquals( "B", StringUtil.getNameFromState(30, n, s), "B");
        assertEquals( "C", StringUtil.getNameFromState(40, n, s), "C");
    }

    @Test
    public void testHexFromInt() {
        assertEquals("00", StringUtil.twoHexFromInt(0));
        assertEquals("01", StringUtil.twoHexFromInt(1));
        assertEquals("02", StringUtil.twoHexFromInt(2));
        assertEquals("03", StringUtil.twoHexFromInt(3));
        assertEquals("09", StringUtil.twoHexFromInt(9));
        assertEquals("0A", StringUtil.twoHexFromInt(10));
        assertEquals("0B", StringUtil.twoHexFromInt(11));
        assertEquals("0E", StringUtil.twoHexFromInt(14));
        assertEquals("0F", StringUtil.twoHexFromInt(15));
        assertEquals("10", StringUtil.twoHexFromInt(16));
        assertEquals("11", StringUtil.twoHexFromInt(17));
        assertEquals("80", StringUtil.twoHexFromInt(0x80));
        assertEquals("FF", StringUtil.twoHexFromInt(0xFF));
    }

    @Test
    public void testHexFromIntFromByte() {
        assertEquals("00", StringUtil.twoHexFromInt((byte) 0));
        assertEquals("01", StringUtil.twoHexFromInt((byte) 1));
        assertEquals("02", StringUtil.twoHexFromInt((byte) 2));
        assertEquals("11", StringUtil.twoHexFromInt((byte) 17));
        assertEquals("80", StringUtil.twoHexFromInt((byte) 0x80));
        assertEquals("FF", StringUtil.twoHexFromInt((byte) 0xFF));
    }

    @Test
    public void testAppHexFromInt() {
        assertEquals("00", StringUtil.appendTwoHexFromInt(0, ""));
        assertEquals("01", StringUtil.appendTwoHexFromInt(1, ""));
        assertEquals("02", StringUtil.appendTwoHexFromInt(2, ""));
        assertEquals("03", StringUtil.appendTwoHexFromInt(3, ""));
        assertEquals("09", StringUtil.appendTwoHexFromInt(9, ""));
        assertEquals("0A", StringUtil.appendTwoHexFromInt(10, ""));
        assertEquals("0B", StringUtil.appendTwoHexFromInt(11, ""));
        assertEquals("0E", StringUtil.appendTwoHexFromInt(14, ""));
        assertEquals("0F", StringUtil.appendTwoHexFromInt(15, ""));
        assertEquals("10", StringUtil.appendTwoHexFromInt(16, ""));
        assertEquals("11", StringUtil.appendTwoHexFromInt(17, ""));
        assertEquals("80", StringUtil.appendTwoHexFromInt(0x80, ""));
        assertEquals("FF", StringUtil.appendTwoHexFromInt(0xFF, ""));
    }

    @Test
    public void testAppHexFromIntFromByte() {
        assertEquals("00", StringUtil.appendTwoHexFromInt((byte) 0, ""));
        assertEquals("01", StringUtil.appendTwoHexFromInt((byte) 1, ""));
        assertEquals("02", StringUtil.appendTwoHexFromInt((byte) 2, ""));
        assertEquals("11", StringUtil.appendTwoHexFromInt((byte) 17, ""));
        assertEquals("80", StringUtil.appendTwoHexFromInt((byte) 0x80, ""));
        assertEquals("FF", StringUtil.appendTwoHexFromInt((byte) 0xFF, ""));
    }
    
    @Test
    public void testParseStringNull() {
        byte[] b = StringUtil.bytesFromHexString("");
        assertEquals( 0, b.length, "array length");
    }

    @Test
    public void testParseSingleDigit() {
        byte[] b = StringUtil.bytesFromHexString("A");
        assertEquals( 1, b.length, "array length");
        assertEquals( 0x0A, b[0], "0th byte");
    }

    @Test
    public void testParseDoubleDigit() {
        byte[] b = StringUtil.bytesFromHexString("AB");
        assertEquals( 1, b.length, "array length");
        assertEquals( 0xAB, b[0] & 0xFF, "0th byte");
    }

    @Test
    public void testParseSeveral() {
        byte[] b = StringUtil.bytesFromHexString("12 34 AB 3 19 6 B B1");
        assertEquals( 8, b.length, "array length");
        assertEquals( 0x12, b[0] & 0xFF, "0th byte");
        assertEquals( 0x34, b[1] & 0xFF, "1st byte");
        assertEquals( 0xAB, b[2] & 0xFF, "2nd byte");
        assertEquals( 0x03, b[3] & 0xFF, "3rd byte");
        assertEquals( 0x19, b[4] & 0xFF, "4th byte");
        assertEquals( 0x06, b[5] & 0xFF, "5th byte");
        assertEquals( 0x0B, b[6] & 0xFF, "6th byte");
        assertEquals( 0xB1, b[7] & 0xFF, "7th byte");
    }

    @Test
    public void testparenQuote() {
        String sample;

        assertNull( StringUtil.parenQuote(null), "null String");

        sample = "";
        assertEquals( sample, StringUtil.parenQuote(sample), sample);

        sample = "abc";
        assertEquals( sample, StringUtil.parenQuote(sample), sample);

        sample = "123";
        assertEquals( sample, StringUtil.parenQuote(sample), sample);

        sample = "";
        assertEquals( sample, StringUtil.parenQuote(sample), sample);

        sample = "a\\b";
        assertEquals( "a\\\\b", StringUtil.parenQuote(sample), sample);

        sample = "a(v)c";
        assertEquals( sample, StringUtil.parenQuote(sample), sample);

        sample = "a(v(b)(n)K)";
        assertEquals( sample, StringUtil.parenQuote(sample), sample);

        sample = "()((()))";
        assertEquals( sample, StringUtil.parenQuote(sample), sample);

        sample = "a)b";
        assertEquals( "a\\)b", StringUtil.parenQuote(sample), sample);
    }

    @Test
    public void testparenUnQuote() {
        String sample;

        assertNull( StringUtil.parenUnQuote(null), "Null String");

        sample = "";
        assertEquals( sample, StringUtil.parenUnQuote(sample), sample);

        sample = "abc";
        assertEquals( sample, StringUtil.parenUnQuote(sample), sample);

        sample = "123";
        assertEquals( sample, StringUtil.parenUnQuote(sample), sample);

        sample = "";
        assertEquals( sample, StringUtil.parenUnQuote(sample), sample);

        sample = "a\\\\b";
        assertEquals( "a\\b", StringUtil.parenUnQuote(sample), sample);

        sample = "a(v)c";
        assertEquals( sample, StringUtil.parenUnQuote(sample), sample);

        sample = "a(v(b)(n)K)";
        assertEquals( sample, StringUtil.parenUnQuote(sample), sample);

        sample = "()((()))";
        assertEquals( sample, StringUtil.parenUnQuote(sample), sample);

        sample = "a\\)b";
        assertEquals( "a)b", StringUtil.parenUnQuote(sample), sample);
    }

    @Test
    public void testSplitParens() {
        String sample;
        java.util.List<String> list;

        sample = "(abc)";
        list = StringUtil.splitParens(sample);
        assertEquals( 1, list.size(), sample);
        assertEquals( "(abc)", list.get(0), sample + " 1");

        sample = "(abc)(def)";
        list = StringUtil.splitParens(sample);
        assertEquals( 2, list.size(), sample);
        assertEquals( "(abc)", list.get(0), sample + " 1");
        assertEquals( "(def)", list.get(1), sample + " 1");

    }

    @Test
    public void testArrayToString() {
        Object[] a = new Object[]{"A", "B", "C"};
        assertEquals( "[A],[B],[C]", StringUtil.arrayToString(a), "Object");

        String[] b = new String[]{"X", "Y", "Z"};
        assertEquals( "[X],[Y],[Z]", StringUtil.arrayToString(b), "Object");

        byte[] c = new byte[]{1, 2, 3};
        assertEquals( "[1],[2],[3]", StringUtil.arrayToString(c), "Object");

        int[] d = new int[]{1, 2, 3};
        assertEquals( "[1],[2],[3]", StringUtil.arrayToString(d), "Object");
    }

    @Test
    public void testGetFirstIntFromString() {
        assertEquals( 123, StringUtil.getFirstIntFromString("aABC123DEFb"), "F aABC123DEFb");
        assertEquals( -1, StringUtil.getFirstIntFromString(""), "F no val");
        assertEquals( 0, StringUtil.getFirstIntFromString("0"), "F 0");
        assertEquals( 2, StringUtil.getFirstIntFromString("+2"), "F +2");
        assertEquals( 5, StringUtil.getFirstIntFromString("-5"), "F -5");
        assertEquals( 123, StringUtil.getFirstIntFromString("ABC123DEF"), "F ABC123DEF");
        assertEquals( 123, StringUtil.getFirstIntFromString("ABC123"), "F ABC123");
        assertEquals( 123, StringUtil.getFirstIntFromString("123"), "F 123");
        assertEquals( 123, StringUtil.getFirstIntFromString("123ABC"), "F 123ABC");
        assertEquals( 12, StringUtil.getFirstIntFromString("AB12 34ABC"), "F AB12 34ABC");
        assertEquals( 123, StringUtil.getFirstIntFromString("123 ABC"), "F 123 ABC ");
        assertEquals( 123, StringUtil.getFirstIntFromString("123ABC456"), "F 123ABC456");
        assertEquals( 123, StringUtil.getFirstIntFromString("123A654BC456"), "F 123A654BC456");
        assertEquals( 123, StringUtil.getFirstIntFromString("XD+123ABC-456"), "F XD+123ABC-456");
        assertEquals( 456, StringUtil.getFirstIntFromString("A c456fg123ABC789jh"), "F A c456fg123ABC789jh");
    }

    @Test
    public void testGetLastIntFromString() {
        assertEquals( 123, StringUtil.getLastIntFromString("aABC123DEFb"), "aABC123DEFb");
        assertEquals( -1, StringUtil.getLastIntFromString(""), "no val");
        assertEquals( 0, StringUtil.getLastIntFromString("0"), "0");
        assertEquals( 2, StringUtil.getLastIntFromString("+2"), "+2");
        assertEquals( 5, StringUtil.getLastIntFromString("-5"), "-5");
        assertEquals( 123, StringUtil.getLastIntFromString("ABC123DEF"), "ABC123DEF");
        assertEquals( 123, StringUtil.getLastIntFromString("ABC123"), "ABC123");
        assertEquals( 123, StringUtil.getLastIntFromString("123"), "123");
        assertEquals( 123, StringUtil.getLastIntFromString("123ABC"), "123ABC");
        assertEquals( 34, StringUtil.getLastIntFromString("AB12 34ABC"), "AB12 34ABC");
        assertEquals( 123, StringUtil.getLastIntFromString("123 ABC "), "123 ABC ");
        assertEquals( 456, StringUtil.getLastIntFromString("123ABC456"), "123ABC456");
        assertEquals( 456, StringUtil.getLastIntFromString("123A654BC456"), "123A654BC456");
        assertEquals( 456, StringUtil.getLastIntFromString("XD+123ABC-456"), "XD+123ABC-456");
        assertEquals( 789, StringUtil.getLastIntFromString("Ac456fg123ABC789jh"), "Ac456fg123ABC789jh");
    }

    @Test
    public void testReplaceLast() {
        assertEquals( "", StringUtil.replaceLast("", "", ""), "no vals");
        assertEquals( "D4F5gaz", StringUtil.replaceLast("D4F5gaz", "", ""), "D4F5gaz");
        assertEquals( "D4S1gaz", StringUtil.replaceLast("D4F5gaz", "F5", "S1"), "D4F5gaz F5 S1");
        assertEquals( "D4F5g1234", StringUtil.replaceLast("D4F5g123", "123", "1234"), "D4F5g1234");
        assertEquals( "77YYYzz", StringUtil.replaceLast("xxYYYzz", "xx", "77"), "77YYYzz");
        assertEquals( "xxAA77YYYzz", StringUtil.replaceLast("xxAAxxYYYzz", "xx", "77"), "xxAA77YYYzz");
        assertEquals( "122", StringUtil.replaceLast("121", "1", "2"), "122");
        assertEquals( "121", StringUtil.replaceLast("121", "Z", "2"), "122 Z");
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

        // test null cases
        assertNull( StringUtil.concatTextHtmlAware(null, null));
        assertEquals(baseText, StringUtil.concatTextHtmlAware(baseText, null));
        assertEquals(extraText, StringUtil.concatTextHtmlAware(null, extraText));
        assertEquals(baseTextHtml, StringUtil.concatTextHtmlAware(baseTextHtml, null));
        assertEquals(extraTextHtml, StringUtil.concatTextHtmlAware(null, extraTextHtml));

        // test with no HTML
        String result = StringUtil.concatTextHtmlAware(baseText, extraText);
        assertEquals(expResultNoHtml, result);

        // test with baseText HTML
        result = StringUtil.concatTextHtmlAware(baseTextHtml, extraText);
        assertEquals(expResultHtml, result);

        // test with extraText HTML
        result = StringUtil.concatTextHtmlAware(baseText, extraTextHtml);
        assertEquals(expResultHtml, result);

        // test with both HTML
        result = StringUtil.concatTextHtmlAware(baseTextHtml, extraTextHtml);
        assertEquals(expResultHtml, result);

        // test with baseText uppercase HTML
        result = StringUtil.concatTextHtmlAware(baseTextHtmlUpper, extraTextHtml);
        assertEquals(expResultHtml, result);

        // test with extraText uppercase HTML
        result = StringUtil.concatTextHtmlAware(baseTextHtml, extraTextHtmlUpper);
        assertEquals(expResultHtml, result);

        // test with both uppercase HTML
        result = StringUtil.concatTextHtmlAware(baseTextHtmlUpper, extraTextHtmlUpper);
        assertEquals(expResultHtml, result);
    }

    @Test
    public void testFullTextToHexArray() {
        
        byte[] b = StringUtil.fullTextToHexArray("PAN",7);
        assertEquals( 7, b.length, "array length");
        assertEquals( 0x50, b[0] & 0xFF, "0th byte");
        assertEquals( 0x41, b[1] & 0xFF, "1st byte");
        assertEquals( 0x4E, b[2] & 0xFF, "2nd byte");
        assertEquals( 0x20, b[3] & 0xFF, "3rd byte");
        assertEquals( 0x20, b[4] & 0xFF, "4th byte");
        assertEquals( 0x20, b[5] & 0xFF, "5th byte");
        assertEquals( 0x20, b[6] & 0xFF, "6th byte");
        
        b = StringUtil.fullTextToHexArray("CATjhgkjhg jkhg kjhg kjhg jhg ",2);
        assertEquals( 2, b.length, "array length");
        assertEquals( 0x43, b[0] & 0xFF, "0th byte");
        assertEquals( 0x41, b[1] & 0xFF, "1st byte");

        b = StringUtil.fullTextToHexArray("My FroG",8);
        assertEquals( 8, b.length, "array length");
        assertEquals( 0x4d, b[0] & 0xFF, "0th byte");
        assertEquals( 0x79, b[1] & 0xFF, "1st byte");
        assertEquals( 0x20, b[2] & 0xFF, "2nd byte");
        assertEquals( 0x46, b[3] & 0xFF, "3rd byte");
        assertEquals( 0x72, b[4] & 0xFF, "4th byte");
        assertEquals( 0x6f, b[5] & 0xFF, "5th byte");
        assertEquals( 0x47, b[6] & 0xFF, "6th byte");
        assertEquals( 0x20, b[7] & 0xFF, "7th byte");

    }

    @Test
    public void testintBytesWithTotalFromNonSpacedHexString(){

        int[] b = StringUtil.intBytesWithTotalFromNonSpacedHexString("01020AB121",true);
        assertEquals( 6, b.length, "array length");
        assertEquals( "[5, 1, 2, 10, 177, 33]",Arrays.toString(b), "01020AB121 true");

        b = StringUtil.intBytesWithTotalFromNonSpacedHexString("01020ab121",true);
        assertEquals( "[5, 1, 2, 10, 177, 33]",Arrays.toString(b), "01020ab121 true");

        b = StringUtil.intBytesWithTotalFromNonSpacedHexString("01020ab121",false);
        assertEquals( "[1, 2, 10, 177, 33]",Arrays.toString(b), "01020ab121 false");

        b = StringUtil.intBytesWithTotalFromNonSpacedHexString("010",true);
        assertEquals( "[0]",Arrays.toString(b), "010 true");

    }

    @Test
    public void testGetByte(){

        assertEquals( 3, StringUtil.getByte(2,"010203"), "010203 2");
        assertEquals( 0, StringUtil.getByte(-1,"010203"), "010203 -1");
        assertEquals( 205, StringUtil.getByte(1,"AbCdEf"), "AbCdEf 1");
    }

    @Test
    public void testGetHexDigit(){

        assertEquals( 0, StringUtil.getHexDigit(0,"010203"), "010203 0");
        assertEquals( 1, StringUtil.getHexDigit(1,"010203"), "010203 1");
        assertEquals( 0, StringUtil.getHexDigit(2,"010203"), "010203 2");
        assertEquals( 2, StringUtil.getHexDigit(3,"010203"), "010203 3");
        assertEquals( 0, StringUtil.getHexDigit(4,"010203"), "010203 4");
        assertEquals( 3, StringUtil.getHexDigit(5,"010203"), "010203 5");
        assertEquals( 15, StringUtil.getHexDigit(3,"010F03"), "010F03 3");

    }

    @Test
    public void testHexStringFromBytes(){
        assertEquals( "", StringUtil.hexStringFromBytes(new byte[] {}), "Zero Length Array");
        assertEquals( "00 01 02 03 ", StringUtil.hexStringFromBytes(new byte[]{0,1,2,3}), "00010203");
        assertEquals( "00 ", StringUtil.hexStringFromBytes(new byte[]{0}), "0");
        assertEquals( "FF ", StringUtil.hexStringFromBytes(new byte[]{-1}), "-1");
        assertEquals( "AB CD ", StringUtil.hexStringFromBytes(new byte[]{(byte) 0xab,(byte) 0xcd}), "-1");
        assertEquals( "2D 7B 81 D9 ", StringUtil.hexStringFromBytes(new byte[] {45,123,(byte)129,(byte)217}), "45,123,129,217");
        assertEquals( "FF 00 01 ", StringUtil.hexStringFromBytes(new byte[] {(byte)255,(byte)256,(byte)257}), "255,256,257");
    }

    @Test
    public void testHexStringFromInts(){
        assertEquals( "", StringUtil.hexStringFromInts(new int[] {}), "Zero Length Array");
        assertEquals( "FF ", StringUtil.hexStringFromInts(new int[] {-1}), "-1");
        assertEquals( "00 ", StringUtil.hexStringFromInts(new int[] {0}), "0");
        assertEquals( "00 01 ", StringUtil.hexStringFromInts(new int[] {0,1}), "0,1");
        assertEquals( "2D 7B 81 D9 ", StringUtil.hexStringFromInts(new int[] {45,123,129,217}), "45,123,129,217");
        assertEquals( "FF 00 01 ", StringUtil.hexStringFromInts(new int[] {255,256,257}), "255,256,257");
    }

    @Test
    public void testIncrementLastNumberInString(){
        assertEquals( null, StringUtil.incrementLastNumberInString("",7), "zero length str");
        assertEquals( null, StringUtil.incrementLastNumberInString("NoNumberInString",1), "no number in str");
        assertEquals( "123", StringUtil.incrementLastNumberInString("123",0 ), "123 0");
        assertEquals( "ABC125DEF", StringUtil.incrementLastNumberInString("ABC123DEF",2), "ABC123DEF 2");
        assertEquals( "ABC100DEF", StringUtil.incrementLastNumberInString("ABC99DEF",1), "ABC99DEF 1");
        assertEquals( "123ABC457", StringUtil.incrementLastNumberInString("123ABC456",1), "123ABC456 1");
        assertEquals( "123ABC0002", StringUtil.incrementLastNumberInString("123ABC0001",1), "123ABC0001 1");
    }

    @Test
    public void testto8Bits(){
        assertEquals("00000000",StringUtil.to8Bits(0b00000000, true));
        assertEquals("10000000",StringUtil.to8Bits(0b10000000, true));
        assertEquals("01000000",StringUtil.to8Bits(0b01000000, true));
        assertEquals("00100000",StringUtil.to8Bits(0b00100000, true));
        assertEquals("00010000",StringUtil.to8Bits(0b00010000, true));
        assertEquals("00001000",StringUtil.to8Bits(0b00001000, true));
        assertEquals("00000100",StringUtil.to8Bits(0b00000100, true));
        assertEquals("00000010",StringUtil.to8Bits(0b00000010, true));
        assertEquals("00000001",StringUtil.to8Bits(0b00000001, true));
        assertEquals("11111111",StringUtil.to8Bits(0b11111111, true));

        assertEquals("00000000",StringUtil.to8Bits(0b00000000, false));
        assertEquals("00000001",StringUtil.to8Bits(0b10000000, false));
        assertEquals("00000010",StringUtil.to8Bits(0b01000000, false));
        assertEquals("00000100",StringUtil.to8Bits(0b00100000, false));
        assertEquals("00001000",StringUtil.to8Bits(0b00010000, false));
        assertEquals("00010000",StringUtil.to8Bits(0b00001000, false));
        assertEquals("00100000",StringUtil.to8Bits(0b00000100, false));
        assertEquals("01000000",StringUtil.to8Bits(0b00000010, false));
        assertEquals("10000000",StringUtil.to8Bits(0b00000001, false));
        assertEquals("11111111",StringUtil.to8Bits(0b11111111, false));
    }

    @Test
    public void testStripHtmlTags() {
        assertEquals("My String",StringUtil.stripHtmlTags("<html>My String</html>"));
        assertEquals("Line1"+System.lineSeparator()+"Line2",StringUtil.stripHtmlTags("<html>Line1<br>Line2</html>"));
        assertEquals("NoTags",StringUtil.stripHtmlTags("NoTags"));
        assertEquals("abc < def",StringUtil.stripHtmlTags("abc < def"));
        assertEquals("ghi > jkl",StringUtil.stripHtmlTags("ghi > jkl"));
        assertEquals("Line3"+System.lineSeparator()+"Line4",StringUtil.stripHtmlTags("<html>Line3<br/>Line4</html>"));
        assertEquals("Line5"+System.lineSeparator()+"Line6",StringUtil.stripHtmlTags("<html>Line5<br />Line6</html>"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
