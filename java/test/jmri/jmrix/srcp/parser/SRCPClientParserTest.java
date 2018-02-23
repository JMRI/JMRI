package jmri.jmrix.srcp.parser;

import java.io.StringReader;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the {@link jmri.jmrix.srcp.parser.SRCPClientParser} class.
 *
 * @author Paul Bender Copyright (C) 2012,2017
 */
public class SRCPClientParserTest {

    @Test
    public void testParseFailure() {
        boolean exceptionOccured = false;
        String code = "12345678910 POWER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertTrue(exceptionOccured);
    }

    // test valid power responses.
    @Test
    public void testPowerOnResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 2 POWER ON hello world\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testPowerOffResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 2 POWER ON goodbye\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testPowerInitResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 101 INFO 2 POWER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testPowerTermResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 102 INFO 1 POWER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // test valid Feedback (FB) responses.
    @Test
    public void testFBFeedbackOffResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 1 FB 1234 0\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testFBFeedbackOnResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 1 FB 1234 0\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testFBInitResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 101 INFO 2 FB\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testFBTermResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 102 INFO 3 FB\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // test valid General Accessory (GA) responses.
    @Test
    public void testGAClosedResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 1 GA 1234 1 0\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGAThrownResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 1 GA 1234 0 0\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGAInitResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 101 INFO 2 GA 1234 N\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGATermResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 102 INFO 3 GA 1234\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // valid Generic Locomoitve (GL) responses
    @Test
    public void testGLInfoResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 1 GL 1234 1 2 28 0 1 0 1 0 1 0 1\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGLInitResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 101 INFO 2 GL 1234 N\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGLTermResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 102 INFO 3 GL 1234\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // valid Service Mode (SM) responses
    @Test
    public void testSMCVInfoResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 1 SM 1234 CV 2 28\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSMCVBITInfoResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 SM 1234 CVBIT 2 0 1\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        new SRCPClientVisitor();
        p.commandresponse();
    }

    @Test
    public void testSMREGInfoResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 SM 1234 REG 2 28\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        new SRCPClientVisitor();
        p.commandresponse();
    }

    @Test
    public void testSMInitResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 101 INFO 2 SM 1234 NMRA\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSMTermResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 102 INFO 3 SM 1234\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // valid Lock (LOCK) responses
    @Test
    public void testLOCKInfoResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 1 LOCK GL 1234 2 28\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testLOCKInitResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 101 INFO 2 LOCK GL 1234\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testLOCKTermResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 102 INFO 3 LOCK GA 1234\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // valid Lock (TIME) responses
    @Test
    public void testTIMEInfoResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 0 TIME 2456678 08 43 12\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testTIMEInitResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 101 INFO 0 TIME 1 1\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testTIMETermResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 102 INFO 0 TIME\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // valid Session (SESSION) responses
    @Test
    public void testSESSIONInfoResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 0 SESSION 12345678\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSESSIONInitResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 101 INFO 0 SESSION 12345678\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSESSIONTermResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 102 INFO 0 SESSION 12345678\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // valid Server (SERVER) responses
    @Test
    public void testSERVERInfoResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 0 SERVER RUNNING\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSERVERInitResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 101 INFO 0 SERVER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSERVERTermResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 102 INFO 0 SERVER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // valid DESCRIPTION responses
    @Test
    public void testBus0DescriptionResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 0 DESCRIPTION SERVER SESSION TIME\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testBus1DescriptionResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 1 DESCRIPTION FB GA GL LOCK POWER SM\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testFBDescriptionResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 1 DESCRIPTION FB\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGADescriptionResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 1 DESCRIPTION GA 42 N\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGLDescriptionResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 1 DESCRIPTION GL 1 N 128 5\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testLockDescriptionResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 1 DESCRIPTION LOCK GA 1\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testPowerDescriptionResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 1 DESCRIPTION POWER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testServerDescriptionResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 1 DESCRIPTION SERVER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSESSIONDescriptionResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 1 DESCRIPTION SESSION 12345678\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSMDescriptionResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 1 DESCRIPTION SM NMRA\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testTIMEDescriptionResponse() {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 1 DESCRIPTION TIME 1 2\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // valid fixed format messages (defined in section 5.2 of the SRCP standard).
    @Test
    public void testResponse200() {
        boolean exceptionOccured = false;
        String code = "12345678910 200 OK\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResponse410() {
        boolean exceptionOccured = false;
        String code = "12345678910 410 ERROR unknown command\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResponse411() {
        boolean exceptionOccured = false;
        String code = "12345678910 411 ERROR unknown value\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResponse412() {
        boolean exceptionOccured = false;
        String code = "12345678910 412 ERROR wrong value\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResponse414() {
        boolean exceptionOccured = false;
        String code = "12345678910 414 ERROR device locked\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResponse415() {
        boolean exceptionOccured = false;
        String code = "12345678910 415 ERROR forbidden\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResponse416() {
        boolean exceptionOccured = false;
        String code = "12345678910 416 ERROR no data\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResponse417() {
        boolean exceptionOccured = false;
        String code = "12345678910 417 ERROR timeout\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResponse418() {
        boolean exceptionOccured = false;
        String code = "12345678910 418 ERROR list too long\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResponse419() {
        boolean exceptionOccured = false;
        String code = "12345678910 419 ERROR list too short\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResponse420() {
        boolean exceptionOccured = false;
        String code = "12345678910 420 ERROR unsupported device protocol\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResponse421() {
        boolean exceptionOccured = false;
        String code = "12345678910 421 ERROR unsupported device\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResponse422() {
        boolean exceptionOccured = false;
        String code = "12345678910 422 ERROR unsupported device group\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResponse423() {
        boolean exceptionOccured = false;
        String code = "12345678910 423 ERROR unsupported operation\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResponse424() {
        boolean exceptionOccured = false;
        String code = "12345678910 424 ERROR device reinitialized\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResponse425() {
        boolean exceptionOccured = false;
        String code = "12345678910 425 ERROR not supported\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResponse499() {
        boolean exceptionOccured = false;
        String code = "12345678910 499 ERROR unspecified error\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.commandresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // handshake mode responses (Defined in section 4.3 of the SRCP protocol)
    @Test
    public void testHandshakeResponseServiceVersion() throws ParseException {
        String code = "12345678910 SRCP 0.8.3\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        p.handshakeresponse();
    }

    @Test
    public void testHandshakeResponse200() {
        boolean exceptionOccured = false;
        String code = "12345678910 200 OK 12345678\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.handshakeresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testHandshakeResponse201() {
        boolean exceptionOccured = false;
        String code = "12345678910 201 OK PROTOCOL SRCP\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.handshakeresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testHandshakeResponse202() {
        boolean exceptionOccured = false;
        String code = "12345678910 202 OK CONNECTIONMODEOK\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.handshakeresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testHandshakeResponse400() {
        boolean exceptionOccured = false;
        String code = "12345678910 400 ERROR unsupported protocol\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.handshakeresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testHandshakeResponse401() {
        boolean exceptionOccured = false;
        String code = "12345678910 401 ERROR unsupported connection mode\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.handshakeresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testHandshakeResponse402() {
        boolean exceptionOccured = false;
        String code = "12345678910 402 ERROR unsufficient data\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.handshakeresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testHandshakeResponse500() {
        boolean exceptionOccured = false;
        String code = "12345678910 500 ERROR out of resources\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        try {
            p.handshakeresponse();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
