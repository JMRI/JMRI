package jmri.jmrix.srcp;

import java.io.StringReader;
import jmri.jmrix.srcp.parser.ParseException;
import jmri.jmrix.srcp.parser.SRCPClientParser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SRCPReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPReply class
 *
 * @author	Bob Jacobsen
 * @author  Paul Bender Copyright (C) 2017
 */
public class SRCPReplyTest {

    @Test
    public void testCtor() {
        SRCPReply m = new SRCPReply();
        Assert.assertNotNull(m);
    }

    // Test the string constructor.
    @Test
    public void testStringCtor() {
        String s = "100 OK REASON GOES HERE\n\r";
        SRCPReply m = new SRCPReply(s);
        Assert.assertNotNull(m);
        Assert.assertTrue("String Constructor Correct", s.equals(m.toString()));
    }

    // Test the parser constructor.
    @Test
    public void testParserCtor() {
        String s = "12345678910 400 ERROR Reason GOES HERE\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(s));
        SRCPReply m = null;
        try {
            m = new SRCPReply(p.commandresponse());
        } catch (ParseException pe) {
            // m is already null if there is an exception parsing the string
        }
        Assert.assertNotNull(m);
        Assert.assertEquals("Parser Constructor Correct", s, m.toString());
        //Assert.assertTrue("Parser Constructor Correct", s.equals(m.toString()));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
