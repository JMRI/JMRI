package jmri.jmrix.dccpp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DCCppMessageTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppMessage class
 *
 * @author	Bob Jacobsen
 * @author	Mark Underwood
 */
public class DCCppMessageTest extends TestCase {

    public void testCtor() {
        DCCppMessage m = new DCCppMessage(3);
        //Assert.assertEquals("length", 3, m.getNumDataElements());
	Assert.assertNotNull(m);
    }

    // check opcode inclusion in message
    public void testOpCode() {
        DCCppMessage m = new DCCppMessage(5);
	Assert.assertNotNull(m);
        //m.setOpCode('i');
        //Assert.assertEquals("read=back op code", 'i', m.getOpCode());
        //Assert.assertEquals("stored op code", 'i', m.getElement(0));
    }

    // Test the string constructor.
    public void testStringCtor() {
        DCCppMessage m = DCCppMessage.parseDCCppMessage("T 42 1");
        Assert.assertEquals("length", 6, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'T', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '4', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '2', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '1', m.getElement(5) & 0xFF);
        //jmri.util.JUnitAppender.assertWarnMessage("Malformed ctor Command: T 42 1 Pattern: T\\s(\\d+)\\s(\\d+)\\s(\\d+)");
        //jmri.util.JUnitAppender.assertWarnMessage("Malformed ctor Command: T 42 1 Pattern: T\\s*(\\d+)");
        //jmri.util.JUnitAppender.assertWarnMessage("Malformed ctor Command: T 42 1 Pattern: T");
    }

    // Test the "Get" methods.
    public void testGetAccessoryDecoderMsg() {
	DCCppMessage m = DCCppMessage.makeAccessoryDecoderMsg(23, 2, true);
	log.debug("accessory decoder message = {}", m.toString());
        Assert.assertEquals("length", 8, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'a', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '3', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '2', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '1', m.getElement(7) & 0xFF);
    }

    public void testGetTurnoutCommandMsg() {
	DCCppMessage m = DCCppMessage.makeTurnoutCommandMsg(23, true);
	log.debug("turnout message = {}", m.toString());
        Assert.assertEquals("length", 6, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'T', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '3', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '1', m.getElement(5) & 0xFF);
    }

    public void testGetWriteDirectCVMsg() {
	DCCppMessage m = DCCppMessage.makeWriteDirectCVMsg(29, 12, 1, 2);
	log.debug("write cv message = {}", m.toString());
        Assert.assertEquals("length", 11, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'W', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '9', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '1', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", '2', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", ' ', m.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '1', m.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", ' ', m.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", '2', m.getElement(10) & 0xFF);
    }

    public void testGetBitWriteDirectCVMsg() {
	DCCppMessage m = DCCppMessage.makeBitWriteDirectCVMsg(17, 4, 1, 3, 4);
	log.debug("write cv bit message = {}", m.toString());
        Assert.assertEquals("length", 12, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'B', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '7', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '4', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '1', m.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", ' ', m.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", '3', m.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", ' ', m.getElement(10) & 0xFF);
        Assert.assertEquals("11th byte", '4', m.getElement(11) & 0xFF);
    }

    public void testGetReadDirectCVMsg() {
	DCCppMessage m = DCCppMessage.makeReadDirectCVMsg(17, 4, 3);
	log.debug("read cv message = {}", m.toString());
        Assert.assertEquals("length", 8, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'R', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '7', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '4', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '3', m.getElement(7) & 0xFF);
    }

    public void testGetWriteOpsModeCVMsg() {
	DCCppMessage m = DCCppMessage.makeWriteOpsModeCVMsg(17, 4, 3);
	log.debug("write ops cv message = {}", m.toString());
        Assert.assertEquals("length", 8, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'w', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '7', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '4', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '3', m.getElement(7) & 0xFF);
    }

    public void testGetBitWriteOpsModeCVMsg() {
	DCCppMessage m = DCCppMessage.makeBitWriteOpsModeCVMsg(17, 4, 3, 1);
	log.debug("write ops bit cv message = {}", m.toString());
        Assert.assertEquals("length", 10, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'b', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '1', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '7', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '4', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '3', m.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", ' ', m.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", '1', m.getElement(9) & 0xFF);
    }

    public void testSetTrackPowerMsg() {
	DCCppMessage m = DCCppMessage.makeSetTrackPowerMsg(true);
	log.debug("track power on message = {}", m.toString());
        Assert.assertEquals("length", 1, m.getNumDataElements());
        Assert.assertEquals("0th byte", '1', m.getElement(0) & 0xFF);

	DCCppMessage m2 = DCCppMessage.makeSetTrackPowerMsg(false);
	log.debug("track power off message = {}", m2.toString());
        Assert.assertEquals("length", 1, m2.getNumDataElements());
        Assert.assertEquals("0th byte", '0', m2.getElement(0) & 0xFF);
    }

    public void testReadTrackCurrentMsg() {
	DCCppMessage m = DCCppMessage.makeReadTrackCurrentMsg();
	log.debug("read track current message = {}", m.toString());
        Assert.assertEquals("length", 1, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'c', m.getElement(0) & 0xFF);
    }

    public void testGetCSStatusMsg() {
	DCCppMessage m = DCCppMessage.makeCSStatusMsg();
	log.debug("get status message = {}", m.toString());
        Assert.assertEquals("length", 1, m.getNumDataElements());
        Assert.assertEquals("0th byte", 's', m.getElement(0) & 0xFF);
    }

    public void testGetAddressedEmergencyStopMsg() {
	DCCppMessage m = DCCppMessage.makeAddressedEmergencyStop(5, 24);
	log.debug("emergency stop message = {}", m.toString());
        Assert.assertEquals("length", 11, m.getNumDataElements());
        Assert.assertEquals("0th byte", 't', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', m.getElement(2) & 0xFF);
        Assert.assertEquals("3st byte", ' ', m.getElement(3) & 0xFF);
        Assert.assertEquals("4rd byte", '2', m.getElement(4) & 0xFF);
        Assert.assertEquals("5rd byte", '4', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '-', m.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '1', m.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", ' ', m.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", '1', m.getElement(10) & 0xFF);
    }

    public void testGetSpeedAndDirectionMsg() {
	DCCppMessage m = DCCppMessage.makeSpeedAndDirectionMsg(5, 24, 0.5f, false);
	log.debug("Speed message 1 = {}", m.toString());
        Assert.assertEquals("length", 11, m.getNumDataElements());
        Assert.assertEquals("0th byte", 't', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', m.getElement(2) & 0xFF);
        Assert.assertEquals("3st byte", ' ', m.getElement(3) & 0xFF);
        Assert.assertEquals("4rd byte", '2', m.getElement(4) & 0xFF);
        Assert.assertEquals("5rd byte", '4', m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '6', m.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '3', m.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", ' ', m.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", '0', m.getElement(10) & 0xFF);

	DCCppMessage m2 = DCCppMessage.makeSpeedAndDirectionMsg(5, 24, 1.0f, true);
	log.debug("Speed message 2 = {}", m2.toString());
        Assert.assertEquals("length", 12, m2.getNumDataElements());
        Assert.assertEquals("0th byte", 't', m2.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m2.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', m2.getElement(2) & 0xFF);
        Assert.assertEquals("3st byte", ' ', m2.getElement(3) & 0xFF);
        Assert.assertEquals("4rd byte", '2', m2.getElement(4) & 0xFF);
        Assert.assertEquals("5rd byte", '4', m2.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m2.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '1', m2.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '2', m2.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", '6', m2.getElement(9) & 0xFF); // Speed steps capped at 126!
        Assert.assertEquals("10th byte", ' ', m2.getElement(10) & 0xFF);
        Assert.assertEquals("11th byte", '1', m2.getElement(11) & 0xFF);

	DCCppMessage m3 = DCCppMessage.makeSpeedAndDirectionMsg(5, 24, -1, true);
	log.debug("Speed message 3 = {}", m3.toString());
        Assert.assertEquals("length", 11, m3.getNumDataElements());
        Assert.assertEquals("0th byte", 't', m3.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m3.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '5', m3.getElement(2) & 0xFF);
        Assert.assertEquals("3st byte", ' ', m3.getElement(3) & 0xFF);
        Assert.assertEquals("4rd byte", '2', m3.getElement(4) & 0xFF);
        Assert.assertEquals("5rd byte", '4', m3.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", ' ', m3.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", '-', m3.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", '1', m3.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", ' ', m3.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", '1', m3.getElement(10) & 0xFF);
    }

    // from here down is testing infrastructure
    public DCCppMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DCCppMessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DCCppMessageTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppMessageTest.class.getName());

}
