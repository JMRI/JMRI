package jmri.jmrix.grapevine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * JUnit tests for the SerialAddress utility class.
 *
 * @author Dave Duchamp Copyright 2004
 * @author Bob Jacobsen Copyright 2007, 2008
 */
public class SerialAddressTest {

    private SerialTrafficControlScaffold tcis = null;

    // service routine for testing regular expressions
    Matcher checkRegex(String regex, String string, boolean OK) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(string);
        Assert.assertTrue((OK ? "" : "don't") + "match " + regex + " in " + string, !m.matches() ^ OK);
        return m;
    }

    // Checking a regular expression used for parsing;
    // This just takes the regex out of the code and tries it directly,
    // so if there's something wrong with it we know early
    @Test
    public void testRegExTurnout() {
        Matcher m;
        String pattern = SerialAddress.turnoutRegex;

        // check turnout match in default format
        m = checkRegex(pattern, "GT12", true);
        Assert.assertEquals("6 groups", 6, m.groupCount());
        //Assert.assertEquals("group 1", "G", m.group(1));
        Assert.assertEquals("group 1", "T", m.group(1));
        Assert.assertEquals("group 6", "12", m.group(6));
        Assert.assertEquals("group 2", null, m.group(2));

        // check turnout match in two-part
        m = checkRegex(pattern, "GT12B4", true);
        //Assert.assertEquals("group 1", "G", m.group(1));
        Assert.assertEquals("group 1", "T", m.group(1));
        Assert.assertEquals("group 6", null, m.group(6));
        Assert.assertEquals("group 2", "12B4", m.group(2));
        Assert.assertEquals("group 3", "12", m.group(3));
        Assert.assertEquals("group 4", "B", m.group(4));
        Assert.assertEquals("group 5", "4", m.group(5));
        Assert.assertEquals("group 6", null, m.group(6));

        // check doesn't match
        checkRegex(pattern, "GLT1", false); // prefix G not included in regex
    }

    // Checking a regular expression used for parsing;
    // This just takes the regex out of the code and tries it directly,
    // so if there's something wrong with it we know early
    @Test
    public void testRegExSensor() {
        Matcher m;
        String pattern = SerialAddress.sensorRegex;

        // check sensor match in default format
        m = checkRegex(pattern, "GS12", true);
        Assert.assertEquals("6 groups", 6, m.groupCount());
        //Assert.assertEquals("group 1", "G", m.group(1));
        Assert.assertEquals("group 1", "S", m.group(1));
        Assert.assertEquals("group 6", "12", m.group(6));
        Assert.assertEquals("group 2", null, m.group(2));

        // check sensor match in two-part
        m = checkRegex(pattern, "GS12B4", true);
        //Assert.assertEquals("group 1", "G", m.group(1));
        Assert.assertEquals("group 1", "S", m.group(1));
        Assert.assertEquals("group 6", null, m.group(6));
        Assert.assertEquals("group 2", "12B4", m.group(2));
        Assert.assertEquals("group 3", "12", m.group(3));
        Assert.assertEquals("group 4", "B", m.group(4));
        Assert.assertEquals("group 5", "4", m.group(5));
        Assert.assertEquals("group 6", null, m.group(6));

        m = checkRegex(pattern, "GS12a4", true);
        //Assert.assertEquals("group 1", "G", m.group(1));
        Assert.assertEquals("group 1", "S", m.group(1));
        Assert.assertEquals("group 6", null, m.group(6));
        Assert.assertEquals("group 2", "12a4", m.group(2));
        Assert.assertEquals("group 3", "12", m.group(3));
        Assert.assertEquals("group 4", "a", m.group(4));
        Assert.assertEquals("group 5", "4", m.group(5));
        Assert.assertEquals("group 6", null, m.group(6));

    }

    // Checking a regular expression used for parsing;
    // This just takes the regex out of the code and tries it directly,
    // so if there's something wrong with it we know early
    @Test
    public void testRegExHost() {
        Matcher m;
        String pattern = SerialAddress.allRegex;

        // check sensor match in default format
        m = checkRegex(pattern, "GS12", true);
        Assert.assertEquals("6 groups", 6, m.groupCount());
        //Assert.assertEquals("group 1", "G", m.group(1));
        Assert.assertEquals("group 1", "S", m.group(1));
        Assert.assertEquals("group 6", "12", m.group(6));
        Assert.assertEquals("group 2", null, m.group(2));

        m = checkRegex(pattern, "GL14007", true);
        Assert.assertEquals("6 groups", 6, m.groupCount());
        //Assert.assertEquals("group 1", "G", m.group(1));
        Assert.assertEquals("group 1", "L", m.group(1));
        Assert.assertEquals("group 6", "14007", m.group(6));
        Assert.assertEquals("group 2", null, m.group(2));

        // check sensor match in two-part
        m = checkRegex(pattern, "GS12B4", true);
        //Assert.assertEquals("group 1", "G", m.group(1));
        Assert.assertEquals("group 1", "S", m.group(1));
        Assert.assertEquals("group 6", null, m.group(6));
        Assert.assertEquals("group 2", "12B4", m.group(2));
        Assert.assertEquals("group 3", "12", m.group(3));
        Assert.assertEquals("group 4", "B", m.group(4));
        Assert.assertEquals("group 5", "4", m.group(5));
        Assert.assertEquals("group 6", null, m.group(6));

        m = checkRegex(pattern, "GS12a4", true);
        //Assert.assertEquals("group 1", "G", m.group(1));
        Assert.assertEquals("group 1", "S", m.group(1));
        Assert.assertEquals("group 6", null, m.group(6));
        Assert.assertEquals("group 2", "12a4", m.group(2));
        Assert.assertEquals("group 3", "12", m.group(3));
        Assert.assertEquals("group 4", "a", m.group(4));
        Assert.assertEquals("group 5", "4", m.group(5));
        Assert.assertEquals("group 6", null, m.group(6));

    }

    @Test
    public void testGetNodeFromSystemName() {
        SerialNode d = new SerialNode(14, SerialNode.NODE2002V6, tcis);
        SerialNode c = new SerialNode(17, SerialNode.NODE2002V1, tcis);
        SerialNode b = new SerialNode(127, SerialNode.NODE2002V1, tcis);
        Assert.assertEquals("node of GL14107", d, SerialAddress.getNodeFromSystemName("GL14107", tcis));
        Assert.assertEquals("node of GL14B107", d, SerialAddress.getNodeFromSystemName("GL14B107", tcis));
        Assert.assertEquals("node of GL127107", b, SerialAddress.getNodeFromSystemName("GL127107", tcis));
        Assert.assertEquals("node of GL127B107", b, SerialAddress.getNodeFromSystemName("GL127B107", tcis));
        Assert.assertEquals("node of GL17107", c, SerialAddress.getNodeFromSystemName("GL17107", tcis));
        Assert.assertEquals("node of GL17B107", c, SerialAddress.getNodeFromSystemName("GL17B107", tcis));
        Assert.assertEquals("node of GL11107", null, SerialAddress.getNodeFromSystemName("GL11107", tcis));
        Assert.assertEquals("node of GL11B107", null, SerialAddress.getNodeFromSystemName("GL11B107", tcis));
    }

    //////////////////////////////////////////////
    // service routine for testing validSystemNameFormat, prefix = "G")
    void checkValidSystemNameFormatName(String name, char letter, boolean OK) {
        Assert.assertTrue((OK ? "" : "in") + "valid format - " + name,
                (SerialAddress.validSystemNameFormat(name, letter, "G") != jmri.Manager.NameValidity.VALID) ^ OK);
    }

    @Test
    public void testValidSystemNameFormat() {
        checkValidSystemNameFormatName("GL1302", 'L', true);
        checkValidSystemNameFormatName("GL1B302", 'L', true);
        checkValidSystemNameFormatName("GL", 'L', false);

        checkValidSystemNameFormatName("GLB2", 'L', false);

        checkValidSystemNameFormatName("GL2305", 'L', true);
        checkValidSystemNameFormatName("GL2B105", 'L', true);
        checkValidSystemNameFormatName("GT2105", 'T', true);
        checkValidSystemNameFormatName("GT2B205", 'T', true);
        checkValidSystemNameFormatName("GS2005", 'S', true);
        checkValidSystemNameFormatName("GS2B5", 'S', true);

        checkValidSystemNameFormatName("GY2005", 'L', false);

        checkValidSystemNameFormatName("GY2B5", 'L', false);

        checkValidSystemNameFormatName("GL22101", 'L', true);
        checkValidSystemNameFormatName("GL22B301", 'L', true);

        checkValidSystemNameFormatName("GL22000", 'L', false);
        //JUnitAppender.assertWarnMessage("invalid bit number 0 in GL22000"); // * reduced to debug level for add new item pane

        checkValidSystemNameFormatName("GL22B0", 'L', false);
        //JUnitAppender.assertWarnMessage("invalid bit number 0 in GL22B0"); // *

        checkValidSystemNameFormatName("GS2001", 'S', true);
        checkValidSystemNameFormatName("GL2B0118", 'L', true);

        checkValidSystemNameFormatName("GL2B2049", 'L', false);
        //JUnitAppender.assertWarnMessage("invalid bit number 2049 in GL2B2049"); // *

        checkValidSystemNameFormatName("GL127419", 'L', true);

        checkValidSystemNameFormatName("GL128000", 'L', false);
        //JUnitAppender.assertWarnMessage("invalid node number 128 in GL128000"); // *

        checkValidSystemNameFormatName("GL127B407", 'L', true);

        checkValidSystemNameFormatName("GL128B7", 'L', false);
        //JUnitAppender.assertWarnMessage("invalid node number 128 in GL128B7"); // *

        checkValidSystemNameFormatName("GL2oo5", 'L', false);

        checkValidSystemNameFormatName("GL2aB5", 'L', false);

        checkValidSystemNameFormatName("GL2B5x", 'L', false);
    }

    @Test
    public void testGetBitFromSystemName() {
        Assert.assertEquals("GL102", 102, SerialAddress.getBitFromSystemName("GL102", "G"));
        Assert.assertEquals("GS2002", 2, SerialAddress.getBitFromSystemName("GS2002", "G"));
        Assert.assertEquals("GS101", 101, SerialAddress.getBitFromSystemName("GS101", "G"));
        Assert.assertEquals("GS2001", 1, SerialAddress.getBitFromSystemName("GS2001", "G"));
        Assert.assertEquals("GL9109", 109, SerialAddress.getBitFromSystemName("GL9109", "G"));
        Assert.assertEquals("GS2009", 9, SerialAddress.getBitFromSystemName("GS2009", "G"));

        Assert.assertEquals("GL29O9", 0, SerialAddress.getBitFromSystemName("GL29O9", "G"));
        JUnitAppender.assertErrorMessage("illegal system name format in getBitFromSystemName: GL29O9 prefix: G");

        Assert.assertEquals("GL1B107", 107, SerialAddress.getBitFromSystemName("GL1B107", "G"));
        Assert.assertEquals("GL2B107", 107, SerialAddress.getBitFromSystemName("GL2B107", "G"));
        Assert.assertEquals("GL1B101", 101, SerialAddress.getBitFromSystemName("GL1B101", "G"));
        Assert.assertEquals("GL2B101", 101, SerialAddress.getBitFromSystemName("GL2B101", "G"));
        Assert.assertEquals("GL1B2048", 2048, SerialAddress.getBitFromSystemName("GL1B2048", "G"));
        Assert.assertEquals("GL11B2048", 2048, SerialAddress.getBitFromSystemName("GL11B2048", "G"));
    }

    @Test
    public void testValidSystemNameConfig() {
        SerialNode d = new SerialNode(4, SerialNode.NODE2002V6, tcis);
        SerialNode c = new SerialNode(10, SerialNode.NODE2002V1, tcis);
        Assert.assertNotNull("exists", d);
        Assert.assertNotNull("exists", c);
        Assert.assertTrue("valid config GL4107", SerialAddress.validSystemNameConfig("GL4107", 'L', tcis));
        Assert.assertTrue("valid config GL4B307", SerialAddress.validSystemNameConfig("GL4B307", 'L', tcis));
        Assert.assertTrue("valid config GS10007", SerialAddress.validSystemNameConfig("GS10007", 'S', tcis));
        Assert.assertTrue("valid config GS10B07", SerialAddress.validSystemNameConfig("GS10B07", 'S', tcis));
        Assert.assertTrue("valid config GL10311", SerialAddress.validSystemNameConfig("GL10311", 'L', tcis));
        Assert.assertTrue("valid config GL10B206", SerialAddress.validSystemNameConfig("GL10B206", 'L', tcis));

        Assert.assertTrue("invalid config GL10133", !SerialAddress.validSystemNameConfig("GL10133", 'L', tcis));
        //JUnitAppender.assertWarnMessage("invalid bit number 133 in GL10133"); // *
        //JUnitAppender.assertWarnMessage("invalid system name GL10133"); // *

        Assert.assertTrue("invalid config GL10B133", !SerialAddress.validSystemNameConfig("GL10B133", 'L', tcis));
        //JUnitAppender.assertWarnMessage("invalid bit number 133 in GL10B133"); // *
        //JUnitAppender.assertWarnMessage("invalid system name GL10B133"); // *

        Assert.assertTrue("valid config GS10006", SerialAddress.validSystemNameConfig("GS10006", 'S', tcis));
        Assert.assertTrue("valid config GS10B06", SerialAddress.validSystemNameConfig("GS10B06", 'S', tcis));

        Assert.assertTrue("invalid config GS10517", !SerialAddress.validSystemNameConfig("GS10517", 'S', tcis));
        //JUnitAppender.assertWarnMessage("invalid bit number 517 in GS10517"); // *
        //JUnitAppender.assertWarnMessage("invalid system name GS10517"); // *

        Assert.assertTrue("invalid config GS10B547", !SerialAddress.validSystemNameConfig("GS10B547", 'S', tcis));
        JUnitAppender.assertWarnMessage("invalid system name GS10B547; bad input bit number 547 > 224");

        Assert.assertTrue("valid config GT4106", SerialAddress.validSystemNameConfig("GT4106", 'T', tcis));
        Assert.assertTrue("valid config GT4B106", SerialAddress.validSystemNameConfig("GT4B106", 'T', tcis));

        Assert.assertTrue("invalid config GT4517", !SerialAddress.validSystemNameConfig("GT4517", 'T', tcis));
        //JUnitAppender.assertWarnMessage("invalid bit number 517 in GT4517"); // *
        //JUnitAppender.assertWarnMessage("invalid system name GT4517"); // *

        Assert.assertTrue("invalid config GT4299", !SerialAddress.validSystemNameConfig("GT4299", 'T', tcis));
        //JUnitAppender.assertWarnMessage("invalid bit number 299 in GT4299"); // *
        //JUnitAppender.assertWarnMessage("invalid system name GT4299"); // *

        Assert.assertTrue("valid config GS4008", SerialAddress.validSystemNameConfig("GS4008", 'S', tcis));
        Assert.assertTrue("valid config GS4B8", SerialAddress.validSystemNameConfig("GS4B8", 'S', tcis));

        Assert.assertTrue("invalid config GS4309", !SerialAddress.validSystemNameConfig("GS4309", 'S', tcis));
        //JUnitAppender.assertWarnMessage("invalid bit number 309 in GS4309"); // *
        //JUnitAppender.assertWarnMessage("invalid system name GS4309"); // *

        Assert.assertTrue("invalid config GS4B309", !SerialAddress.validSystemNameConfig("GS4B309", 'S', tcis));
        JUnitAppender.assertWarnMessage("invalid system name GS4B309; bad input bit number 309 > 224");

        Assert.assertTrue("invalid config GL11107", !SerialAddress.validSystemNameConfig("GL11107", 'L', tcis));
        JUnitAppender.assertWarnMessage("invalid system name  GL11107; no such node");

        Assert.assertTrue("invalid config GL11B107", !SerialAddress.validSystemNameConfig("GL11B107", 'L', tcis));
        JUnitAppender.assertWarnMessage("invalid system name GL11B107; no such node");
    }

    @Test
    public void testConvertSystemNameToAlternate() {
        Assert.assertEquals("convert GL14107", "GL14B107", SerialAddress.convertSystemNameToAlternate("GL14107", "G"));
        Assert.assertEquals("convert GS1107", "GS1B107", SerialAddress.convertSystemNameToAlternate("GS1B107", "G"));
        Assert.assertEquals("convert GT4107", "GT4B107", SerialAddress.convertSystemNameToAlternate("GT4107", "G"));
        Assert.assertEquals("convert GL14B307", "GL14B307",
                SerialAddress.convertSystemNameToAlternate("GL14B307", "G"));
        Assert.assertEquals("convert GL1B207", "GL1B207", SerialAddress.convertSystemNameToAlternate("GL1B207", "G"));
        Assert.assertEquals("convert GS4B207", "GS4B207", SerialAddress.convertSystemNameToAlternate("GS4B207", "G"));
        Assert.assertEquals("convert GL14B308", "GL14B308",
                SerialAddress.convertSystemNameToAlternate("GL14B308", "G"));

        Assert.assertEquals("convert GL128B7", "", SerialAddress.convertSystemNameToAlternate("GL128B7", "G"));
        //JUnitAppender.assertWarnMessage("invalid node number 128 in GL128B7"); // *
    }

    @Test
    public void testNormalizeSystemName() {
        Assert.assertEquals("normalize GL14107", "GL14107", SerialAddress.normalizeSystemName("GL14107", "G"));
        Assert.assertEquals("normalize GL01107", "GL1107", SerialAddress.normalizeSystemName("GL01107", "G"));
        Assert.assertEquals("normalize GL004107", "GL4107", SerialAddress.normalizeSystemName("GL004107", "G"));
        Assert.assertEquals("normalize GL14B207", "GL14207", SerialAddress.normalizeSystemName("GL14B207", "G"));
        Assert.assertEquals("normalize GL001B00307", "GL1307", SerialAddress.normalizeSystemName("GL001B00307", "G"));
        Assert.assertEquals("normalize GL004B107", "GL4107", SerialAddress.normalizeSystemName("GL004B107", "G"));
        Assert.assertEquals("normalize GL014B0108", "GL14108", SerialAddress.normalizeSystemName("GL014B0108", "G"));

        // alternate sensor names
        Assert.assertEquals("normalize GS1a1", "GS1101", SerialAddress.normalizeSystemName("GS1a1", "G"));
        Assert.assertEquals("normalize GS1m1", "GS1201", SerialAddress.normalizeSystemName("GS1m1", "G"));
        Assert.assertEquals("normalize GS1p1", "GS1001", SerialAddress.normalizeSystemName("GS1p1", "G"));
        Assert.assertEquals("normalize GS1s1", "GS1021", SerialAddress.normalizeSystemName("GS1s1", "G"));
        Assert.assertEquals("normalize GS98p1", "GS98001", SerialAddress.normalizeSystemName("GS98p1", "G"));

        Assert.assertEquals("normalize GL128B7", "", SerialAddress.normalizeSystemName("GL128B7", "G"));
        //JUnitAppender.assertWarnMessage("invalid node number 128 in GL128B7"); // *
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tcis = new SerialTrafficControlScaffold(new GrapevineSystemConnectionMemo());
    }

    @After
    public void tearDown() {
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
