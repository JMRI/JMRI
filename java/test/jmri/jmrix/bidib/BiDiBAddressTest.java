package jmri.jmrix.bidib;

import static org.junit.Assert.*;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.bidib.jbidibc.messages.enums.LcOutputType;
import org.junit.jupiter.api.*;

/**
 * Tests for the BiDiBAddress class
 *
 * @author  Eckart Meyer  Copyright (C) 2020
 */

public class BiDiBAddressTest {

    BiDiBSystemConnectionMemo memo;
    String p;
    char t;
    char s;
    char l;
    char r;

    private void checkAddr(String aString, char typeLetter, char expectedAddressType) {
        BiDiBAddress addr = new BiDiBAddress(p + typeLetter + aString, typeLetter, memo);
        assertTrue("invalid address", addr.isValid());
        if (expectedAddressType == 't') assertTrue("not a DCC address", addr.isTrackAddr());
        if (expectedAddressType == 'a') assertTrue("not a BiDiB accessory address", addr.isAccessoryAddr());
        if (expectedAddressType == 'p') assertTrue("not a port address", addr.isPortAddr());
        if (expectedAddressType == 'f') assertTrue("not a feedback address", addr.isFeedbackAddr());
    }

    private void checkPortAddr(String aString, char typeLetter, LcOutputType expectedPortType) {
        BiDiBAddress addr = new BiDiBAddress(p + typeLetter + aString, typeLetter, memo);
        assertTrue("invalid address", addr.isValid());
        if (expectedPortType == LcOutputType.SWITCHPORT) assertTrue("not a SWITCHPORT", addr.getPortType() == LcOutputType.SWITCHPORT);

    }

    @Test
    public void testAddressOK() {
        checkAddr("20", t, 't'); // no node - assume root node which is a command station -> is DCC address
        checkAddr("x0:20", t, 't'); // node UID = 0 also is the root node
        checkAddr("X0D68001234:7", t, 't'); // node UID specified as hex number
        checkAddr("Test0:7", t, 't'); // node identified by user name. Node is a command station
        checkAddr("Test1:7", t, 'a'); // node identified by user name. Node is not a command station, but has BiDiB accessories -> is accessory address
        checkAddr("X0DE8004321:42", l, 'p'); // Light type. Node is not a command station, but has ports -> is port address
        checkAddr("Test0:42", s, 'f'); // Sensor type. Node has feedbacks -> is feedback address
        checkAddr("Test1:42", s, 'f'); // Sensor type. Node has feedbacks -> is feedback address
        checkAddr("Test2:42", s, 'p'); // Sensor type. Node has no feedbacks, but ports -> is port address
        checkAddr("42", r, 'f'); // Reporter type -> must be feedback type
        // explicit address types
        //   Sensors
        checkAddr("Test0:f42", s, 'f');
        checkAddr("Test1:p42", s, 'p');
        //   Turnouts
        checkAddr("Test0:t7", t, 't');
        checkAddr("Test1:a7", t, 'a');
        checkAddr("Test1:p7", t, 'p');
        checkAddr("01.02-03_04:a7", t, 'a'); //username starts with a number and contains all allowed special characters

        //   Lights
        checkAddr("Test0:t7", l, 't');
        checkAddr("Test1:p7", l, 'p');
        //   Reporters
        checkAddr("f42", r, 'f'); // Reporter type -> must be feedback type
        // port types - only with port address type
        //   type based address model
        checkPortAddr("Test1:p5", l, LcOutputType.SWITCHPORT);
        checkPortAddr("Test1:p5S", l, LcOutputType.SWITCHPORT);
        checkPortAddr("Test1:p5L", l, LcOutputType.LIGHTPORT);
        checkPortAddr("Test1:p5V", l, LcOutputType.SERVOPORT);
        checkPortAddr("Test1:p5U", l, LcOutputType.SOUNDPORT);
        checkPortAddr("Test1:p5M", l, LcOutputType.MOTORPORT);
        checkPortAddr("Test1:p5A", l, LcOutputType.ANALOGPORT);
        checkPortAddr("Test1:p5B", l, LcOutputType.BACKLIGHTPORT);
        checkPortAddr("Test1:p5P", l, LcOutputType.SWITCHPAIRPORT);
        checkPortAddr("Test1:p7I", s, LcOutputType.INPUTPORT);
        checkPortAddr("Test1:p7", s, LcOutputType.INPUTPORT);
        //   flat address model
        checkPortAddr("Test2:p3", l, null); //no default for flat model addresses
        checkPortAddr("Test2:p3S", l, LcOutputType.SWITCHPORT);
        checkPortAddr("Test2:p1I", s, LcOutputType.INPUTPORT);
        checkPortAddr("Test2:p1", s, LcOutputType.INPUTPORT);
    }

    @Test
    public void testAddressNotOK() {
        // syntax
        //assertFalse("UID not a hex number", new BiDiBAddress(p + "T" + "0:20", t, memo).isValid()); //node 0 is not given as a hex number, it is interpreted as the username
        assertFalse("colon without node", new BiDiBAddress(p + "T" + ":20", t, memo).isValid()); //if node is omitted, the colon must omitted too
        assertFalse("no blanks allowed", new BiDiBAddress(p + "T" + " 20", t, memo).isValid()); //no blanks allowed
        assertFalse("no blanks allowed", new BiDiBAddress(p + "T" + "x0 :20", t, memo).isValid()); //no blanks allowed
        assertFalse("no blanks allowed", new BiDiBAddress(p + "T" + "x0: 20", t, memo).isValid()); //no blanks allowed
        assertFalse("illegal address type", new BiDiBAddress(p + "T" + "y20", t, memo).isValid());
        assertFalse("illegal port type", new BiDiBAddress(p + "T" + "Test1:p20Y", t, memo).isValid());
        // address type
        assertFalse("Sensor as DCC address", new BiDiBAddress(p + "S" + "t20", t, memo).isValid()); //sensors can't be a DCC address
        assertFalse("Sensor as accessory", new BiDiBAddress(p + "S" + "a20", t, memo).isValid()); //sensors can't be a DCC address
        assertFalse("Light as accessory", new BiDiBAddress(p + "L" + "a20", l, memo).isValid()); //lights can't be a BiDiB accessory
        assertFalse("Light as feedback", new BiDiBAddress(p + "L" + "f20", l, memo).isValid()); //lights can't be a BiDiB feedback
        assertFalse("Turnout as feedback", new BiDiBAddress(p + "T" + "f20", t, memo).isValid()); //turnouts can't be a BiDiB feedback
        assertFalse("Reporter as DCC address", new BiDiBAddress(p + "R" + "t20", t, memo).isValid()); //reporters can't be a DCC address
        assertFalse("Reporter as accessory", new BiDiBAddress(p + "R" + "a20", t, memo).isValid()); //reporters can't be a BiDiB accessory
        assertFalse("Reporter as port", new BiDiBAddress(p + "R" + "p20", t, memo).isValid()); //reporters can't be a port
        // port type
        assertFalse("turnout port type not allowed for DCC address", new BiDiBAddress(p + "T" + "Test0:t20L", t, memo).isValid());
        assertFalse("turnout port type not allowed for BiDiB accessory", new BiDiBAddress(p + "T" + "Test1:a20L", t, memo).isValid());
        assertFalse("light port type not allowed for DCC address", new BiDiBAddress(p + "L" + "Test0:t20L", t, memo).isValid());
        assertFalse("Turnout as input port", new BiDiBAddress(p + "T" + "Test1:p20I", t, memo).isValid());
        assertFalse("Light as input port", new BiDiBAddress(p + "L" + "Test1:p20I", t, memo).isValid());
        assertFalse("Sensor as non input port", new BiDiBAddress(p + "S" + "Test1:p20S", t, memo).isValid());

        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BT:20\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BT 20\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BTx0 :20\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BTx0: 20\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BTy20\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BTTest1:p20Y\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BSt20\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BSa20\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BLa20\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BLf20\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BTf20\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BRt20\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BRa20\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BRp20\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BTTest0:t20L\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BTTest1:a20L\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BLTest0:t20L\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BTTest1:p20I\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BLTest1:p20I\" is invalid");
        JUnitAppender.assertWarnMessage("*** BiDiB system name \"BSTest1:p20S\" is invalid");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        p = memo.getSystemPrefix();
        t = new BiDiBTurnoutManager(memo).typeLetter();
        s = new BiDiBSensorManager(memo).typeLetter();
        l = new BiDiBLightManager(memo).typeLetter();
        r = new BiDiBReporterManager(memo).typeLetter();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }


}
