package jmri.jmrix.roco.z21;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.roco.z21.Z21XNetReply class
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class Z21XNetReplyTest extends jmri.jmrix.lenz.XNetReplyTest {

    // Test the string constructor.
    @Test
    @Override
    public void testStringCtor() {
        msg = new Z21XNetReply("12 34 AB 03 19 06 0B B1");
        assertEquals( 8, msg.getNumDataElements(), "length");
        assertEquals( 0x12, msg.getElement(0) & 0xFF, "0th byte");
        assertEquals( 0x34, msg.getElement(1) & 0xFF, "1st byte");
        assertEquals( 0xAB, msg.getElement(2) & 0xFF, "2nd byte");
        assertEquals( 0x03, msg.getElement(3) & 0xFF, "3rd byte");
        assertEquals( 0x19, msg.getElement(4) & 0xFF, "4th byte");
        assertEquals( 0x06, msg.getElement(5) & 0xFF, "5th byte");
        assertEquals( 0x0B, msg.getElement(6) & 0xFF, "6th byte");
        assertEquals( 0xB1, msg.getElement(7) & 0xFF, "7th byte");
    }

    // Test the string constructor with an empty string paramter.
    @Test
    @Override
    public void testStringCtorEmptyString() {
        msg = new Z21XNetReply("");
        assertEquals( 0, msg.getNumDataElements(), "length");
        assertEquals( "", msg.toString(), "empty reply");
    }

    // Test the copy constructor.
    @Test
    @Override
    public void testCopyCtor() {
        Z21XNetReply x = new Z21XNetReply("12 34 AB 03 19 06 0B B1");
        msg = new Z21XNetReply(x);
        assertEquals( x.getNumDataElements(), msg.getNumDataElements(), "length");
        assertEquals( x.getElement(0), msg.getElement(0), "0th byte");
        assertEquals( x.getElement(1), msg.getElement(1), "1st byte");
        assertEquals( x.getElement(2), msg.getElement(2), "2nd byte");
        assertEquals( x.getElement(3), msg.getElement(3), "3rd byte");
        assertEquals( x.getElement(4), msg.getElement(4), "4th byte");
        assertEquals( x.getElement(5), msg.getElement(5), "5th byte");
        assertEquals( x.getElement(6), msg.getElement(6), "6th byte");
        assertEquals( x.getElement(7), msg.getElement(7), "7th byte");
    }

    // Test the XNetMessage constructor.
    @Test
    @Override
    public void testXNetMessageCtor() {
        Z21XNetMessage x = new Z21XNetMessage("12 34 AB 03 19 06 0B B1");
        msg = new Z21XNetReply(x);
        assertEquals( x.getNumDataElements(), msg.getNumDataElements(), "length");
        assertEquals( x.getElement(0) & 0xFF, msg.getElement(0) & 0xFF, "0th byte");
        assertEquals( x.getElement(1) & 0xFF, msg.getElement(1) & 0xFF, "1st byte");
        assertEquals( x.getElement(2) & 0xFF, msg.getElement(2) & 0xFF, "2nd byte");
        assertEquals( x.getElement(3) & 0xFF, msg.getElement(3) & 0xFF, "3rd byte");
        assertEquals( x.getElement(4) & 0xFF, msg.getElement(4) & 0xFF, "4th byte");
        assertEquals( x.getElement(5) & 0xFF, msg.getElement(5) & 0xFF, "5th byte");
        assertEquals( x.getElement(6) & 0xFF, msg.getElement(6) & 0xFF, "6th byte");
        assertEquals( x.getElement(7) & 0xFF, msg.getElement(7) & 0xFF, "7th byte");
    }

    // get information from specific types of messages.
    // check is service mode response
    @Test
    @Override
    public void testIsServiceModeResponse() {
        // CV 1 in direct mode.
        Z21XNetReply r = new Z21XNetReply("64 14 00 14 05 61");
        assertTrue(r.isServiceModeResponse());
    }

    @Test
    @Override
    public void testToMonitorStringServiceModeDirectResponse() {
        Z21XNetReply r = new Z21XNetReply("64 14 00 14 05 61");
        assertEquals( Bundle.getMessage("Z21LAN_X_CV_RESULT", 21, 5), 
            r.toMonitorString(), "Monitor String");
    }

    @Test
    public void testToMonitorStringZ21_LAN_X_TURNOUT_INFO() {
        Z21XNetReply r = new Z21XNetReply("43 00 01 01 43");
        assertEquals( Bundle.getMessage("Z21LAN_X_TURNOUT_INFO", 2, "Closed"),
            r.toMonitorString(), "Monitor String");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        msg = new Z21XNetReply();
        m = msg;
    }

    @AfterEach
    @Override
    public void tearDown() {
        m = null;
        msg = null;
        JUnitUtil.tearDown();
    }

}
