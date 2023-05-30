package jmri.jmrix.srcp.parser;

import java.io.StringReader;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;


/**
 * Tests for the {@link jmri.jmrix.srcp.parser.SRCPClientParser} class.
 *
 * @author Paul Bender Copyright (C) 2012,2017
 */
public class SRCPClientParserTest {

    @Test
    public void testParseFailure() {
        String code = "12345678910 POWER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertNotNull(Assertions.assertThrows(ParseException.class, () ->
            p.commandresponse() ));
    }

    // test valid power responses.
    @Test
    public void testPowerOnResponse() {
        String code = "12345678910 100 INFO 2 POWER ON hello world\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testPowerOffResponse() {
        String code = "12345678910 100 INFO 2 POWER ON goodbye\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testPowerInitResponse() {
        String code = "12345678910 101 INFO 2 POWER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    @Test
    public void testPowerTermResponse() {
        String code = "12345678910 102 INFO 1 POWER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    // test valid Feedback (FB) responses.
    @Test
    public void testFBFeedbackOffResponse() {
        String code = "12345678910 100 INFO 1 FB 1234 0\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    @Test
    public void testFBFeedbackOnResponse() {
        String code = "12345678910 100 INFO 1 FB 1234 0\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testFBInitResponse() {
        String code = "12345678910 101 INFO 2 FB\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    @Test
    public void testFBTermResponse() {
        String code = "12345678910 102 INFO 3 FB\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    // test valid General Accessory (GA) responses.
    @Test
    public void testGAClosedResponse() {
        String code = "12345678910 100 INFO 1 GA 1234 1 0\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    @Test
    public void testGAThrownResponse() {
        String code = "12345678910 100 INFO 1 GA 1234 0 0\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    @Test
    public void testGAInitResponse() {
        String code = "12345678910 101 INFO 2 GA 1234 N\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    @Test
    public void testGATermResponse() {
        String code = "12345678910 102 INFO 3 GA 1234\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    // valid Generic Locomoitve (GL) responses
    @Test
    public void testGLInfoResponse() {
        String code = "12345678910 100 INFO 1 GL 1234 1 2 28 0 1 0 1 0 1 0 1\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    @Test
    public void testGLInitResponse() {
        String code = "12345678910 101 INFO 2 GL 1234 N\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    @Test
    public void testGLTermResponse() {
        String code = "12345678910 102 INFO 3 GL 1234\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    // valid Service Mode (SM) responses
    @Test
    public void testSMCVInfoResponse() {
        String code = "12345678910 100 INFO 1 SM 1234 CV 2 28\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    @Test
    public void testSMCVBITInfoResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 SM 1234 CVBIT 2 0 1\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertNotNull(new SRCPClientVisitor());
        p.commandresponse();
    }

    @Test
    public void testSMREGInfoResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 SM 1234 REG 2 28\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertNotNull(new SRCPClientVisitor());
        p.commandresponse();
    }

    @Test
    public void testSMInitResponse() {
        String code = "12345678910 101 INFO 2 SM 1234 NMRA\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testSMTermResponse() {
        String code = "12345678910 102 INFO 3 SM 1234\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    // valid Lock (LOCK) responses
    @Test
    public void testLOCKInfoResponse() {
        String code = "12345678910 100 INFO 1 LOCK GL 1234 2 28\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    @Test
    public void testLOCKInitResponse() {
        String code = "12345678910 101 INFO 2 LOCK GL 1234\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    @Test
    public void testLOCKTermResponse() {
        String code = "12345678910 102 INFO 3 LOCK GA 1234\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    // valid Lock (TIME) responses
    @Test
    public void testTIMEInfoResponse() {
        String code = "12345678910 100 INFO 0 TIME 2456678 08 43 12\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    @Test
    public void testTIMEInitResponse() {
        String code = "12345678910 101 INFO 0 TIME 1 1\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.commandresponse() );
    }

    @Test
    public void testTIMETermResponse() {
        String code = "12345678910 102 INFO 0 TIME\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    // valid Session (SESSION) responses
    @Test
    public void testSESSIONInfoResponse() {
        String code = "12345678910 100 INFO 0 SESSION 12345678\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testSESSIONInitResponse() {
        String code = "12345678910 101 INFO 0 SESSION 12345678\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testSESSIONTermResponse() {
        String code = "12345678910 102 INFO 0 SESSION 12345678\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    // valid Server (SERVER) responses
    @Test
    public void testSERVERInfoResponse() {
        String code = "12345678910 100 INFO 0 SERVER RUNNING\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testSERVERInitResponse() {
        String code = "12345678910 101 INFO 0 SERVER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testSERVERTermResponse() {
        String code = "12345678910 102 INFO 0 SERVER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    // valid DESCRIPTION responses
    @Test
    public void testBus0DescriptionResponse() {
        String code = "12345678910 100 INFO 0 DESCRIPTION SERVER SESSION TIME\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testBus1DescriptionResponse() {
        String code = "12345678910 100 INFO 1 DESCRIPTION FB GA GL LOCK POWER SM\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testFBDescriptionResponse() {
        String code = "12345678910 100 INFO 1 DESCRIPTION FB\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testGADescriptionResponse() {
        String code = "12345678910 100 INFO 1 DESCRIPTION GA 42 N\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testGLDescriptionResponse() {
        String code = "12345678910 100 INFO 1 DESCRIPTION GL 1 N 128 5\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testLockDescriptionResponse() {
        String code = "12345678910 100 INFO 1 DESCRIPTION LOCK GA 1\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testPowerDescriptionResponse() {
        String code = "12345678910 100 INFO 1 DESCRIPTION POWER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testServerDescriptionResponse() {
        String code = "12345678910 100 INFO 1 DESCRIPTION SERVER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testSESSIONDescriptionResponse() {
        String code = "12345678910 100 INFO 1 DESCRIPTION SESSION 12345678\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testSMDescriptionResponse() {
        String code = "12345678910 100 INFO 1 DESCRIPTION SM NMRA\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testTIMEDescriptionResponse() {
        String code = "12345678910 100 INFO 1 DESCRIPTION TIME 1 2\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    // valid fixed format messages (defined in section 5.2 of the SRCP standard).
    @Test
    public void testResponse200() {
        String code = "12345678910 200 OK\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testResponse410() {
        String code = "12345678910 410 ERROR unknown command\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testResponse411() {
        String code = "12345678910 411 ERROR unknown value\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testResponse412() {
        String code = "12345678910 412 ERROR wrong value\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testResponse414() {
        String code = "12345678910 414 ERROR device locked\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testResponse415() {
        String code = "12345678910 415 ERROR forbidden\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testResponse416() {
        String code = "12345678910 416 ERROR no data\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testResponse417() {
        String code = "12345678910 417 ERROR timeout\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testResponse418() {
        String code = "12345678910 418 ERROR list too long\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testResponse419() {
        String code = "12345678910 419 ERROR list too short\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testResponse420() {
        String code = "12345678910 420 ERROR unsupported device protocol\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testResponse421() {
        String code = "12345678910 421 ERROR unsupported device\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testResponse422() {
        String code = "12345678910 422 ERROR unsupported device group\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testResponse423() {
        String code = "12345678910 423 ERROR unsupported operation\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testResponse424() {
        String code = "12345678910 424 ERROR device reinitialized\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testResponse425() {
        String code = "12345678910 425 ERROR not supported\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
    }

    @Test
    public void testResponse499() {
        String code = "12345678910 499 ERROR unspecified error\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.commandresponse() );
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
        String code = "12345678910 200 OK 12345678\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.handshakeresponse() );
    }

    @Test
    public void testHandshakeResponse201() {
        String code = "12345678910 201 OK PROTOCOL SRCP\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.handshakeresponse() );
    }

    @Test
    public void testHandshakeResponse202() {
        String code = "12345678910 202 OK CONNECTIONMODEOK\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.handshakeresponse() );
    }

    @Test
    public void testHandshakeResponse400() {
        String code = "12345678910 400 ERROR unsupported protocol\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.handshakeresponse() );
    }

    @Test
    public void testHandshakeResponse401() {
        String code = "12345678910 401 ERROR unsupported connection mode\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.handshakeresponse() );
    }

    @Test
    public void testHandshakeResponse402() {
        String code = "12345678910 402 ERROR unsufficient data\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () -> p.handshakeresponse() );
    }

    @Test
    public void testHandshakeResponse500() {
        String code = "12345678910 500 ERROR out of resources\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.handshakeresponse() );
    }

    @Test
    public void testParseWithNewLineOnly(){
        String code = "12345678910 500 ERROR out of resources\n";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        Assertions.assertDoesNotThrow( () ->  p.handshakeresponse() );
    }

    @Test
    public void testHandshakeResponseServiceVersionNoTimeStamp() throws ParseException {
        String code = "srcpd V2.1.6; SRCP 0.8.4; SRCPOTHER 0.8.3\n";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        p.handshakeresponse();
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
