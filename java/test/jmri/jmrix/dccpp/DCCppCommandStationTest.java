package jmri.jmrix.dccpp;

import jmri.NmraPacket;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DCCppCommandStationTest.java
 * <p>
 * Test for the jmri.jmrix.dccpp.DCCppCommandStation class
 *
 * @author Paul Bender
 * @author Mark Underwood
 *
 * Based on LenzCommandStationTest
 */
public class DCCppCommandStationTest {

    @Test
    public void testCtor() {

        DCCppCommandStation c = new DCCppCommandStation();
        Assert.assertNotNull(c);
    }

    @Test
    public void testVersion() {
        // test setting the command station version from an DCCppReply
        DCCppCommandStation c = new DCCppCommandStation();
        // V1.0 Status Message
        //DCCppReply r = new DCCppReply("iDCC++BASE STATION vUNO_1.0: BUILD 23 Feb 2015 09:23:57");
        // V1.1 Status Message
        DCCppReply r = DCCppReply.parseDCCppReply(
                "iDCC++ BASE STATION FOR ARDUINO MEGA / ARDUINO MOTOR SHIELD: BUILD 23 Feb 2015 09:23:57");
        Assert.assertNotNull(r);
        log.debug("Status Reply: {}", r.toString());
        c.setCommandStationInfo(r);
        // Assert.assertTrue(c.getBaseStationType().equals("UNO_1.0"));
        //Assert.assertTrue(c.getBaseStationType().equals("MEGA / ARDUINO MOTOR SHIELD"));
        log.debug("Base Station: {}", c.getBaseStationType());
        log.debug("Code Date: {}", c.getCodeBuildDate());
        Assert.assertTrue(c.getBaseStationType().equals("DCC++ BASE STATION FOR ARDUINO MEGA / ARDUINO MOTOR SHIELD"));
        Assert.assertTrue(c.getCodeBuildDate().equals("23 Feb 2015 09:23:57"));
    }

    @Test
    public void testSetBaseStationTypeString() {
        DCCppCommandStation c = new DCCppCommandStation();
        c.setBaseStationType("MEGA_4.3");
        Assert.assertTrue(c.getBaseStationType().equals("MEGA_4.3"));
        c.setBaseStationType("UNO_1.7");
        Assert.assertTrue(c.getBaseStationType().equals("UNO_1.7"));
    }

    @Test
    public void testSetCodeBuildDateString() {
        DCCppCommandStation c = new DCCppCommandStation();
        c.setCodeBuildDate("17 May 2007 10:15:07");
        Assert.assertTrue(c.getCodeBuildDate().equals("17 May 2007 10:15:07"));
        c.setCodeBuildDate("03 Jan 1993 23:59:59");
        Assert.assertTrue(c.getCodeBuildDate().equals("03 Jan 1993 23:59:59"));
    }

    @Test
    public void testSendAccDecoderPktOpsMode1234() {
        int address = 1234;
        int cv = 28;
        int data = 11;
        byte[] ba = NmraPacket.accDecoderPktOpsMode(address, cv, data);
        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0xB5, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0xBB, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x1B, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x0B, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0xF2, ba[5] & 0xFF);

        String expectedResult = "M 0 B5 BB EC 1B 0B";

        DCCppCommandStation c = new DCCppCommandStation();
        DCCppTrafficController tc = new DCCppTrafficController(c) {
            private String expectedString;

            private DCCppTrafficController init(String var) {
                expectedString = var;
                return this;
            }

            @Override
            public void sendDCCppMessage(DCCppMessage m, DCCppListener reply) {
                StringBuilder sb = new StringBuilder();
                sb.append(" expected:<").append(expectedString)
                        .append("> but was:<").append(m.toString()).append(">");

                Assert.assertTrue(sb.toString(), expectedString.equals(m.toString()));
            }
        }.init(expectedResult);
        c.setTrafficController(tc);
        c.sendPacket(ba, 1);

    }

    @Test
    public void testSendAccSignalDecoderPktOpsMode2037() {
        int address = 2037;
        int cv = 556;
        int data = 175;
        byte[] ba = NmraPacket.accSignalDecoderPktOpsMode(address, cv, data);
        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0xBE, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x01, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEE, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x2B, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xAF, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0xD5, ba[5] & 0xFF);

        String expectedResult = "M 0 BE 01 EE 2B AF";

        DCCppCommandStation c = new DCCppCommandStation();
        DCCppTrafficController tc = new DCCppTrafficController(c) {
            private String expectedString;

            private DCCppTrafficController init(String var) {
                expectedString = var;
                return this;
            }

            @Override
            public void sendDCCppMessage(DCCppMessage m, DCCppListener reply) {
                StringBuilder sb = new StringBuilder();
                sb.append(" expected:<").append(expectedString)
                        .append("> but was:<").append(m.toString()).append(">");

                Assert.assertTrue(sb.toString(), expectedString.equals(m.toString()));
            }
        }.init(expectedResult);
        c.setTrafficController(tc);
        c.sendPacket(ba, 1);

    }

    @Test
    public void testSendAccSignalDecoderPkt256Aspect7() {
        int address = 256;
        int aspect = 7;
        byte[] ba = NmraPacket.accSignalDecoderPkt(address, aspect);
        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 4, ba.length);
        Assert.assertEquals("byte 0", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x67, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x07, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xE0, ba[3] & 0xFF);

        String expectedResult = "M 0 80 67 07";

        DCCppCommandStation c = new DCCppCommandStation();
        DCCppTrafficController tc = new DCCppTrafficController(c) {
            private String expectedString;

            private DCCppTrafficController init(String var) {
                expectedString = var;
                return this;
            }

            @Override
            public void sendDCCppMessage(DCCppMessage m, DCCppListener reply) {
                StringBuilder sb = new StringBuilder();
                sb.append(" expected:<").append(expectedString)
                        .append("> but was:<").append(m.toString()).append(">");

                Assert.assertTrue(sb.toString(), expectedString.equals(m.toString()));
            }
        }.init(expectedResult);
        c.setTrafficController(tc);
        c.sendPacket(ba, 1);

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppCommandStationTest.class);

}
