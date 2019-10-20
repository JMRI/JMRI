package jmri.jmrix.srcp.parser;

import java.io.StringReader;
import jmri.jmrix.srcp.SRCPSystemConnectionMemo;
import jmri.jmrix.srcp.SRCPTrafficController;
import jmri.jmrix.srcp.SRCPListener;
import jmri.jmrix.srcp.SRCPMessage;
import jmri.jmrix.srcp.SRCPReply;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SRCPClientVisitorTest {

    jmri.jmrix.srcp.SRCPSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        SRCPClientVisitor t = new SRCPClientVisitor();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testInfoPowerOnResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 POWER ON hello world\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
        Assert.assertEquals("12345678910 100 INFO0 POWER",new SRCPReply(e).toString());
    }

    @Test
    public void testInfoPowerOffResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 POWER OFF goodye\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
        Assert.assertEquals("12345678910 100 INFO0 POWER",new SRCPReply(e).toString());
    }

    @Test
    public void testInfoPowerInitResponse() throws ParseException {
        String code = "12345678910 101 INFO 0 POWER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
        Assert.assertEquals("12345678910 101 INFO0 POWER",new SRCPReply(e).toString());
    }

    @Test
    public void testInfoPowerTermResponse() throws ParseException {
        String code = "12345678910 102 INFO 0 POWER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
        Assert.assertEquals("12345678910 102 INFO0 POWER",new SRCPReply(e).toString());
    }

    // test valid Feedback (FB) responses.
    @Test
    public void testFBFeedbackOffResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 FB 1234 0\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
        Assert.assertEquals("12345678910 100 INFO0 FB1234 0",new SRCPReply(e).toString());
    }

    @Test
    public void testFBFeedbackOnResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 FB 1234 0\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testFBInitResponse() throws ParseException {
        String code = "12345678910 101 INFO 0 FB\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testFBTermResponse() throws ParseException {
        String code = "12345678910 102 INFO 0 FB\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    // test valid General Accessory (GA) responses.
    @Test
    public void testGAClosedResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 GA 1234 1 0\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testGAThrownResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 GA 1234 0 0\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testGAInitResponse() throws ParseException {
        String code = "12345678910 101 INFO 0 GA 1234 N\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testGATermResponse() throws ParseException {
        String code = "12345678910 102 INFO 0 GA 1234\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    // valid Generic Locomoitve (GL) responses
    @Test
    public void testGLInfoResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 GL 1234 1 2 28 0 1 0 1 0 1 0 1\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testGLInitResponse() throws ParseException {
        String code = "12345678910 101 INFO 0 GL 1234 N\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testGLTermResponse() throws ParseException {
        String code = "12345678910 102 INFO 0 GL 1234\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    // valid Service Mode (SM) responses
    @Test
    public void testSMCVInfoResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 SM 1234 CV 2 28\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testSMCVBITInfoResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 SM 1234 CVBIT 2 0 1\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testSMREGInfoResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 SM 1234 REG 2 28\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testSMInitResponse() throws ParseException {
        String code = "12345678910 101 INFO 0 SM 1234 NMRA\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testSMTermResponse() throws ParseException {
        String code = "12345678910 102 INFO 0 SM 1234\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    // valid Lock (LOCK) responses
    @Test
    public void testLOCKInfoResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 LOCK GL 1234 2 28\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testLOCKInitResponse() throws ParseException {
        String code = "12345678910 101 INFO 0 LOCK GL 1234\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testLOCKTermResponse() throws ParseException {
        String code = "12345678910 102 INFO 0 LOCK GA 1234\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    // valid Lock (TIME) responses
    @Test
    public void testTIMEInfoResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 TIME 2456678 08 43 12\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testTIMEInitResponse() throws ParseException {
        String code = "12345678910 101 INFO 0 TIME 1 1\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testTIMETermResponse() throws ParseException {
        String code = "12345678910 102 INFO 0 TIME\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    // valid Session (SESSION) responses
    @Test
    public void testSESSIONInfoResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 SESSION 12345678\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testSESSIONInitResponse() throws ParseException {
        String code = "12345678910 101 INFO 0 SESSION 12345678\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testSESSIONTermResponse() throws ParseException {
        String code = "12345678910 102 INFO 0 SESSION 12345678\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    // valid Server (SERVER) responses
    @Test
    public void testSERVERInfoResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 SERVER RUNNING\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testSERVERInitResponse() throws ParseException {
        String code = "12345678910 101 INFO 0 SERVER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testSERVERTermResponse() throws ParseException {
        String code = "12345678910 102 INFO 0 SERVER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    // valid DESCRIPTION responses
    @Test
    public void testBus0DescriptionResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 DESCRIPTION SERVER SESSION TIME\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testBus1DescriptionResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 DESCRIPTION FB GA GL LOCK POWER SM\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testFBDescriptionResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 DESCRIPTION FB\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testGADescriptionResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 DESCRIPTION GA 42 N\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testGLDescriptionResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 DESCRIPTION GL 1 N 128 5\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testLockDescriptionResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 DESCRIPTION LOCK GA 1\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testPowerDescriptionResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 DESCRIPTION POWER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testServerDescriptionResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 DESCRIPTION SERVER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testSESSIONDescriptionResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 DESCRIPTION SESSION 12345678\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testSMDescriptionResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 DESCRIPTION SM NMRA\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testTIMEDescriptionResponse() throws ParseException {
        String code = "12345678910 100 INFO 0 DESCRIPTION TIME 1 2\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    // valid fixed format messages (defined in section 5.2 of the SRCP standard).
    @Test
    public void testResponse200() throws ParseException {
        String code = "12345678910 200 OK\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testResponse410() throws ParseException {
        String code = "12345678910 410 ERROR unknown command\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testResponse411() throws ParseException {
        String code = "12345678910 411 ERROR unknown value\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testResponse412() throws ParseException {
        String code = "12345678910 412 ERROR wrong value\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testResponse414() throws ParseException {
        String code = "12345678910 414 ERROR device locked\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testResponse415() throws ParseException {
        String code = "12345678910 415 ERROR forbidden\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testResponse416() throws ParseException {
        String code = "12345678910 416 ERROR no data\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testResponse417() throws ParseException {
        String code = "12345678910 417 ERROR timeout\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testResponse422() throws ParseException {
        String code = "12345678910 422 ERROR unsupported device group\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testResponse423() throws ParseException {
        String code = "12345678910 423 ERROR unsupported operation\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testResponse424() throws ParseException {
        String code = "12345678910 424 ERROR device reinitialized\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testResponse425() throws ParseException {
        String code = "12345678910 425 ERROR not supported\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testResponse499() throws ParseException {
        String code = "12345678910 499 ERROR unspecified error\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
    }

    // handshake mode responses (Defined in section 4.3 of the SRCP protocol)
    @Test
    public void testHandshakeResponse200() throws ParseException {
        String code = "12345678910 200 OK 12345678\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.handshakeresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testHandshakeResponse201() throws ParseException {
        String code = "12345678910 201 OK PROTOCOL SRCP\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.handshakeresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testHandshakeResponse202() throws ParseException {
        String code = "12345678910 202 OK CONNECTIONMODEOK\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.handshakeresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testHandshakeResponse400() throws ParseException {
        String code = "12345678910 400 ERROR unsupported protocol\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.handshakeresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testHandshakeResponse401() throws ParseException {
        String code = "12345678910 401 ERROR unsupported connection mode\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.handshakeresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testHandshakeResponse402() throws ParseException {
        String code = "12345678910 402 ERROR unsufficient data\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.handshakeresponse();
        e.jjtAccept(v, memo);
    }

    @Test
    public void testHandshakeResponse500() throws ParseException {
        String code = "12345678910 500 ERROR out of resources\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.handshakeresponse();
        e.jjtAccept(v, memo);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        SRCPTrafficController et = new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener l) {
                // we aren't actually sending anything to a layout.
            }
        };
        memo = new SRCPSystemConnectionMemo("D", "SRCP", et);
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SRCPClientVisitorTest.class);

}
