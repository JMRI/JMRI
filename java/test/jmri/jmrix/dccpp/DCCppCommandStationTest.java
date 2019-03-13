package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DCCppCommandStationTest.java
 *
 * Description: tests for the jmri.jmrix.dccpp.DCCppCommandStation class
 *
 * @author Paul Bender
 * @author Mark Underwood
 *
 *         Based on LenzCommandStationTest
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppCommandStationTest.class);

}
