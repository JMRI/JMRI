// SerialAddressTest.java

package jmri.jmrix.grapevine;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.util.JUnitAppender;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * JUnit tests for the SerialAddress utility class.
 * @author	Dave Duchamp Copyright 2004
 * @author  Bob Jacobsen Copyright 2007, 2008
 * @version	$Revision: 1.4 $
 */
public class SerialAddressTest extends TestCase {

    // service routine for testing regular expressions
    Matcher checkRegex(String regex, String string, boolean OK) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(string);
        Assert.assertTrue((OK?"":"don't")+"match "+regex+" in "+string, !m.matches()^OK);
        return m;
    }

    // Checking a regular expression used for parsing;
    // This just takes the regex out of the code and tries it directly,
    // so if there's something wrong with it we know early
    public void testRegExTurnout() {
        Matcher m;
        String pattern = SerialAddress.turnoutRegex;
        
        // check turnout match in default format
        m = checkRegex(pattern, "GT12", true);
        Assert.assertEquals("7 groups", 7,   m.groupCount());
        Assert.assertEquals("group 1", "G",  m.group(1));
        Assert.assertEquals("group 2", "T",  m.group(2));
        Assert.assertEquals("group 7", "12", m.group(7));
        Assert.assertEquals("group 3", null,   m.group(3));

        // check turnout match in two-part
        m = checkRegex(pattern, "GT12B4", true);
        Assert.assertEquals("group 1", "G",  m.group(1));
        Assert.assertEquals("group 2", "T",  m.group(2));
        Assert.assertEquals("group 7", null, m.group(7));
        Assert.assertEquals("group 3", "12B4",   m.group(3));
        Assert.assertEquals("group 4", "12",   m.group(4));
        Assert.assertEquals("group 5", "B",   m.group(5));
        Assert.assertEquals("group 6", "4",   m.group(6));
        Assert.assertEquals("group 7", null,   m.group(7));

        // check doesn't match
        checkRegex(pattern, "LT1", false);
    }
    
    // Checking a regular expression used for parsing;
    // This just takes the regex out of the code and tries it directly,
    // so if there's something wrong with it we know early
    public void testRegExSensor() {
        Matcher m;
        String pattern = SerialAddress.sensorRegex;
        
        // check sensor match in default format
        m = checkRegex(pattern, "GS12", true);
        Assert.assertEquals("7 groups", 7,   m.groupCount());
        Assert.assertEquals("group 1", "G",  m.group(1));
        Assert.assertEquals("group 2", "S",  m.group(2));
        Assert.assertEquals("group 7", "12", m.group(7));
        Assert.assertEquals("group 3", null,   m.group(3));

        // check sensor match in two-part
        m = checkRegex(pattern, "GS12B4", true);
        Assert.assertEquals("group 1", "G",  m.group(1));
        Assert.assertEquals("group 2", "S",  m.group(2));
        Assert.assertEquals("group 7", null, m.group(7));
        Assert.assertEquals("group 3", "12B4",   m.group(3));
        Assert.assertEquals("group 4", "12",   m.group(4));
        Assert.assertEquals("group 5", "B",   m.group(5));
        Assert.assertEquals("group 6", "4",   m.group(6));
        Assert.assertEquals("group 7", null,   m.group(7));

        m = checkRegex(pattern, "GS12a4", true);
        Assert.assertEquals("group 1", "G",  m.group(1));
        Assert.assertEquals("group 2", "S",  m.group(2));
        Assert.assertEquals("group 7", null, m.group(7));
        Assert.assertEquals("group 3", "12a4",   m.group(3));
        Assert.assertEquals("group 4", "12",   m.group(4));
        Assert.assertEquals("group 5", "a",   m.group(5));
        Assert.assertEquals("group 6", "4",   m.group(6));
        Assert.assertEquals("group 7", null,   m.group(7));

    }
    
    // Checking a regular expression used for parsing;
    // This just takes the regex out of the code and tries it directly,
    // so if there's something wrong with it we know early
    public void testRegExHost() {
        Matcher m;
        String pattern = SerialAddress.allRegex;
        
        // check sensor match in default format
        m = checkRegex(pattern, "GS12", true);
        Assert.assertEquals("7 groups", 7,   m.groupCount());
        Assert.assertEquals("group 1", "G",  m.group(1));
        Assert.assertEquals("group 2", "S",  m.group(2));
        Assert.assertEquals("group 7", "12", m.group(7));
        Assert.assertEquals("group 3", null,   m.group(3));

        m = checkRegex(pattern, "GL14007", true);
        Assert.assertEquals("7 groups", 7,   m.groupCount());
        Assert.assertEquals("group 1", "G",  m.group(1));
        Assert.assertEquals("group 2", "L",  m.group(2));
        Assert.assertEquals("group 7", "14007", m.group(7));
        Assert.assertEquals("group 3", null,   m.group(3));

        // check sensor match in two-part
        m = checkRegex(pattern, "GS12B4", true);
        Assert.assertEquals("group 1", "G",  m.group(1));
        Assert.assertEquals("group 2", "S",  m.group(2));
        Assert.assertEquals("group 7", null, m.group(7));
        Assert.assertEquals("group 3", "12B4",   m.group(3));
        Assert.assertEquals("group 4", "12",   m.group(4));
        Assert.assertEquals("group 5", "B",   m.group(5));
        Assert.assertEquals("group 6", "4",   m.group(6));
        Assert.assertEquals("group 7", null,   m.group(7));

        m = checkRegex(pattern, "GS12a4", true);
        Assert.assertEquals("group 1", "G",  m.group(1));
        Assert.assertEquals("group 2", "S",  m.group(2));
        Assert.assertEquals("group 7", null, m.group(7));
        Assert.assertEquals("group 3", "12a4",   m.group(3));
        Assert.assertEquals("group 4", "12",   m.group(4));
        Assert.assertEquals("group 5", "a",   m.group(5));
        Assert.assertEquals("group 6", "4",   m.group(6));
        Assert.assertEquals("group 7", null,   m.group(7));

    }
    
	public void testGetNodeFromSystemName() {
            SerialNode d = new SerialNode(14,SerialNode.NODE2002V6);
            SerialNode c = new SerialNode(17,SerialNode.NODE2002V1);
            SerialNode b = new SerialNode(127,SerialNode.NODE2002V1);
            Assert.assertEquals("node of GL14007", d,  SerialAddress.getNodeFromSystemName("GL14007") );
            Assert.assertEquals("node of GL14B7", d,   SerialAddress.getNodeFromSystemName("GL14B7") );
            Assert.assertEquals("node of GL127007", b, SerialAddress.getNodeFromSystemName("GL127007") );
            Assert.assertEquals("node of GL127B7", b,  SerialAddress.getNodeFromSystemName("GL127B7") );
            Assert.assertEquals("node of GL17007", c,  SerialAddress.getNodeFromSystemName("GL17007") );
            Assert.assertEquals("node of GL17B7", c,   SerialAddress.getNodeFromSystemName("GL17B7") );
            Assert.assertEquals("node of GL11007", null, SerialAddress.getNodeFromSystemName("GL11007") );
            Assert.assertEquals("node of GL11B7", null,  SerialAddress.getNodeFromSystemName("GL11B7") );
        }

    
    //////////////////////////////////////////////
    
    // service routine for testing validSystemNameFormat
    void checkValidSystemNameFormatName(String name, char letter, boolean OK) {
        Assert.assertTrue( (OK?"":"in")+"valid format - "+name, !SerialAddress.validSystemNameFormat(name,letter)^OK );
    }
    
	public void testValidateSystemNameFormat() {
            checkValidSystemNameFormatName("GL1002",'L', true);
            checkValidSystemNameFormatName("GL1B2",'L', true);
            checkValidSystemNameFormatName("GL",'L', false);
            JUnitAppender.assertErrorMessage("illegal system name format: GL");
            

            checkValidSystemNameFormatName("GLB2",'L', false);
            JUnitAppender.assertErrorMessage("illegal system name format: GLB2");

            checkValidSystemNameFormatName("GL2005",'L', true);
            checkValidSystemNameFormatName("GL2B5",'L', true);
            checkValidSystemNameFormatName("GT2005",'T', true);
            checkValidSystemNameFormatName("GT2B5",'T', true);
            checkValidSystemNameFormatName("GS2005",'S', true);
            checkValidSystemNameFormatName("GS2B5",'S', true);

            checkValidSystemNameFormatName("GY2005",'L', false);
            JUnitAppender.assertErrorMessage("illegal system name format: GY2005");

            checkValidSystemNameFormatName("GY2B5",'L', false);
            JUnitAppender.assertErrorMessage("illegal system name format: GY2B5");

            checkValidSystemNameFormatName("GL22001",'L', true);
            checkValidSystemNameFormatName("GL22B1",'L', true);

            checkValidSystemNameFormatName("GL22000",'L', false);
            JUnitAppender.assertErrorMessage("invalid bit number 0 in GL22000");

            checkValidSystemNameFormatName("GL22B0",'L', false);
            JUnitAppender.assertErrorMessage("invalid bit number 0 in GL22B0");

            checkValidSystemNameFormatName("GS2201",'S', true);
            checkValidSystemNameFormatName("GL2B048",'L', true);

            checkValidSystemNameFormatName("GL2B2049",'L', false);
            JUnitAppender.assertErrorMessage("invalid bit number 2049 in GL2B2049");

            checkValidSystemNameFormatName("GL127019",'L', true);

            checkValidSystemNameFormatName("GL128000",'L', false);
            JUnitAppender.assertErrorMessage("invalid node number 128 in GL128000");

            checkValidSystemNameFormatName("GL127B7",'L', true);

            checkValidSystemNameFormatName("GL128B7",'L', false);
            JUnitAppender.assertErrorMessage("invalid node number 128 in GL128B7");

            checkValidSystemNameFormatName("GL2oo5",'L', false);
            JUnitAppender.assertErrorMessage("illegal system name format: GL2oo5");

            checkValidSystemNameFormatName("GL2aB5",'L', false);
            JUnitAppender.assertErrorMessage("illegal system name format: GL2aB5");

            checkValidSystemNameFormatName("GL2B5x",'L', false);
            JUnitAppender.assertErrorMessage("illegal system name format: GL2B5x");
	}

	public void testGetBitFromSystemName() {
            Assert.assertEquals("GL2", 2, SerialAddress.getBitFromSystemName("GL2") );
            Assert.assertEquals("GL2002", 2, SerialAddress.getBitFromSystemName("GL2002") );
            Assert.assertEquals("GL1", 1, SerialAddress.getBitFromSystemName("GL1") );
            Assert.assertEquals("GL2001", 1, SerialAddress.getBitFromSystemName("GL2001") );
            Assert.assertEquals("GL999", 999, SerialAddress.getBitFromSystemName("GL999") );
            Assert.assertEquals("GL2999", 999, SerialAddress.getBitFromSystemName("GL2999") );

            Assert.assertEquals("GL29O9", 0, SerialAddress.getBitFromSystemName("GL29O9") );
            JUnitAppender.assertErrorMessage("illegal system name format: GL29O9");

            Assert.assertEquals("GL1B7", 7, SerialAddress.getBitFromSystemName("GL1B7") );
            Assert.assertEquals("GL2B7", 7, SerialAddress.getBitFromSystemName("GL2B7") );
            Assert.assertEquals("GL1B1", 1, SerialAddress.getBitFromSystemName("GL1B1") );
            Assert.assertEquals("GL2B1", 1, SerialAddress.getBitFromSystemName("GL2B1") );
            Assert.assertEquals("GL1B2048", 2048, SerialAddress.getBitFromSystemName("GL1B2048") );
            Assert.assertEquals("GL11B2048", 2048, SerialAddress.getBitFromSystemName("GL11B2048") );
        }
        
	public void testValidSystemNameConfig() {
            SerialNode d = new SerialNode(4,SerialNode.NODE2002V6);
            SerialNode c = new SerialNode(10,SerialNode.NODE2002V1);
            Assert.assertTrue("valid config GL4007",  SerialAddress.validSystemNameConfig("GL4007",'L') );
            Assert.assertTrue("valid config GL4B7",   SerialAddress.validSystemNameConfig("GL4B7",'L') );
            Assert.assertTrue("valid config GS10007", SerialAddress.validSystemNameConfig("GS10007",'S') );
            Assert.assertTrue("valid config GS10B7",  SerialAddress.validSystemNameConfig("GS10B7",'S') );
            Assert.assertTrue("valid config GL10011", SerialAddress.validSystemNameConfig("GL10011",'L') );
            Assert.assertTrue("valid config GL10B06", SerialAddress.validSystemNameConfig("GL10B06",'L') );

            Assert.assertTrue("invalid config GL10133", !SerialAddress.validSystemNameConfig("GL10133",'L') );
            JUnitAppender.assertErrorMessage("invalid bit number 133 in GL10133");
            JUnitAppender.assertWarnMessage("GL10133 invalid");

            Assert.assertTrue("invalid config GL10B133", !SerialAddress.validSystemNameConfig("GL10B133",'L') );
            JUnitAppender.assertErrorMessage("invalid bit number 133 in GL10B133");
            JUnitAppender.assertWarnMessage("GL10B133 invalid");

            Assert.assertTrue("valid config GS10006", SerialAddress.validSystemNameConfig("GS10006",'S') );
            Assert.assertTrue("valid config GS10B06", SerialAddress.validSystemNameConfig("GS10B06",'S') );

            Assert.assertTrue("invalid config GS10517", !SerialAddress.validSystemNameConfig("GS10517",'S') );
            JUnitAppender.assertErrorMessage("invalid bit number 517 in GS10517");
            JUnitAppender.assertWarnMessage("GS10517 invalid");

            Assert.assertTrue("invalid config GS10B547", !SerialAddress.validSystemNameConfig("GS10B547",'S') );
            JUnitAppender.assertWarnMessage("GS10B547 invalid; bad input bit number 547 > 96");

            Assert.assertTrue("valid config GT4006", SerialAddress.validSystemNameConfig("GT4006",'T') );
            Assert.assertTrue("valid config GT4B6", SerialAddress.validSystemNameConfig("GT4B6",'T') );

            Assert.assertTrue("invalid config GT4317", !SerialAddress.validSystemNameConfig("GT4317",'T') );
            JUnitAppender.assertErrorMessage("invalid bit number 317 in GT4317");
            JUnitAppender.assertWarnMessage("GT4317 invalid");

            Assert.assertTrue("invalid config GT4317", !SerialAddress.validSystemNameConfig("GT4B317",'T') );
            JUnitAppender.assertErrorMessage("invalid bit number 317 in GT4B317");
            JUnitAppender.assertWarnMessage("GT4B317 invalid");

            Assert.assertTrue("valid config GS4008", SerialAddress.validSystemNameConfig("GS4008",'S') );
            Assert.assertTrue("valid config GS4B8", SerialAddress.validSystemNameConfig("GS4B8",'S') );

            Assert.assertTrue("invalid config GS4309", !SerialAddress.validSystemNameConfig("GS4309",'S') );
            JUnitAppender.assertErrorMessage("invalid bit number 309 in GS4309");
            JUnitAppender.assertWarnMessage("GS4309 invalid");

            Assert.assertTrue("invalid config GS4B309", !SerialAddress.validSystemNameConfig("GS4B309",'S') );
            JUnitAppender.assertWarnMessage("GS4B309 invalid; bad input bit number 309 > 96");

            Assert.assertTrue("invalid config GL11007", !SerialAddress.validSystemNameConfig("GL11007",'L') );
            JUnitAppender.assertWarnMessage("GL11007 invalid; no such node");

            Assert.assertTrue("invalid config GL11B7", !SerialAddress.validSystemNameConfig("GL11B7",'L') );
            JUnitAppender.assertWarnMessage("GL11B7 invalid; no such node");

        }        
        
	public void testConvertSystemNameToAlternate() {
            Assert.assertEquals("convert GL14007",  "GL14B7", SerialAddress.convertSystemNameToAlternate("GL14007") );
            Assert.assertEquals("convert GS1007",   "GS1B7", SerialAddress.convertSystemNameToAlternate("GS1B7") );
            Assert.assertEquals("convert GT4007",   "GT4B7", SerialAddress.convertSystemNameToAlternate("GT4007") );
            Assert.assertEquals("convert GL14B7",   "GL14B7", SerialAddress.convertSystemNameToAlternate("GL14B7") );
            Assert.assertEquals("convert GL1B7",    "GL1B7", SerialAddress.convertSystemNameToAlternate("GL1B7") );
            Assert.assertEquals("convert GS4B7",    "GS4B7", SerialAddress.convertSystemNameToAlternate("GS4B7") );
            Assert.assertEquals("convert GL14B8",   "GL14B8", SerialAddress.convertSystemNameToAlternate("GL14B8") );

            Assert.assertEquals("convert GL128B7", "", SerialAddress.convertSystemNameToAlternate("GL128B7") );
            JUnitAppender.assertErrorMessage("invalid node number 128 in GL128B7");
        }
        
	public void testNormalizeSystemName() {
            Assert.assertEquals("normalize GL14007",    "GL14007", SerialAddress.normalizeSystemName("GL14007") );
            Assert.assertEquals("normalize GL01007",    "GL1007", SerialAddress.normalizeSystemName("GL01007") );
            Assert.assertEquals("normalize GL004007",   "GL4007", SerialAddress.normalizeSystemName("GL004007") );
            Assert.assertEquals("normalize GL14B7",     "GL14007", SerialAddress.normalizeSystemName("GL14B7") );
            Assert.assertEquals("normalize GL001B7",    "GL1007", SerialAddress.normalizeSystemName("GL001B7") );
            Assert.assertEquals("normalize GL004B7",    "GL4007", SerialAddress.normalizeSystemName("GL004B7") );
            Assert.assertEquals("normalize GL014B0008", "GL14008", SerialAddress.normalizeSystemName("GL014B0008") );

            // alternate sensor names
            Assert.assertEquals("normalize GS1a1",   "GS1101",  SerialAddress.normalizeSystemName("GS1a1") );
            Assert.assertEquals("normalize GS1m1",   "GS1201",  SerialAddress.normalizeSystemName("GS1m1") );
            Assert.assertEquals("normalize GS1p1",   "GS1001",  SerialAddress.normalizeSystemName("GS1p1") );
            Assert.assertEquals("normalize GS1s1",   "GS1021",  SerialAddress.normalizeSystemName("GS1s1") );
            Assert.assertEquals("normalize GS98p1", "GS98001",  SerialAddress.normalizeSystemName("GS98p1") );
            
            Assert.assertEquals("normalize GL128B7", "", SerialAddress.normalizeSystemName("GL128B7") );
            JUnitAppender.assertErrorMessage("invalid node number 128 in GL128B7");
        }
        
	// from here down is testing infrastructure

	public SerialAddressTest(String s) {
            super(s);
	}

	// Main entry point
	static public void main(String[] args) {
            String[] testCaseName = {SerialAddressTest.class.getName()};
            junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
            TestSuite suite = new TestSuite(SerialAddressTest.class);
            return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
