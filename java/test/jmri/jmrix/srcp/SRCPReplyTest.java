package jmri.jmrix.srcp;

import java.io.StringReader;
import jmri.jmrix.srcp.parser.ParseException;
import jmri.jmrix.srcp.parser.SRCPClientParser;
import jmri.util.JUnitUtil;
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
public class SRCPReplyTest extends jmri.jmrix.AbstractMessageTestBase {
        
    private SRCPReply msg = null;

    // Test the string constructor.
    @Test
    public void testStringCtor() {
        String s = "100 OK REASON GOES HERE\n\r";
        msg = new SRCPReply(s);
        Assert.assertNotNull(m);
        Assert.assertTrue("String Constructor Correct", s.equals(msg.toString()));
    }

    // Test the parser constructor.
    @Test
    public void testParserCtor() {
        String s = "12345678910 400 ERROR Reason GOES HERE\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(s));
        msg = null;
        try {
            msg = new SRCPReply(p.commandresponse());
        } catch (ParseException pe) {
            // msg is already null if there is an exception parsing the string
        }
        Assert.assertNotNull(msg);
        Assert.assertEquals("Parser Constructor Correct", s, msg.toString());
        //Assert.assertTrue("Parser Constructor Correct", s.equals(m.toString()));
    }

    @Test
    public void checkIsResponseOK(){
        String s1 = "12345678910 100 OK REASON GOES HERE\n\r";
        SRCPReply m1 = new SRCPReply(s1);
        String s2 = "12345678910 250 OK REASON GOES HERE\n\r";
        SRCPReply m2 = new SRCPReply(s2);
        String s3 = "12345678910 300 OK REASON GOES HERE\n\r";
        SRCPReply m3 = new SRCPReply(s3);
        Assert.assertTrue("100 message ok",m1.isResponseOK());
        Assert.assertTrue("200 message ok",m2.isResponseOK());
        Assert.assertFalse("300 message not ok",m3.isResponseOK());
    }

    @Test
    public void getResponseCode(){
        String s1 = "12345678910 100 OK REASON GOES HERE\n\r";
        SRCPReply m1 = new SRCPReply(s1);
        String s2 = "12345678910 250 OK REASON GOES HERE\n\r";
        SRCPReply m2 = new SRCPReply(s2);
        String s3 = "12345678910 300 OK REASON GOES HERE\n\r";
        SRCPReply m3 = new SRCPReply(s3);
        Assert.assertEquals("100 response code","100",m1.getResponseCode());
        Assert.assertEquals("250 response code","250",m2.getResponseCode());
        Assert.assertEquals("300 response code","300",m3.getResponseCode());
    }

    @Test
    public void checkValue(){
        String s1 = "123456789 100 INFO 1 SM -1 8 99";
        SRCPReply m1 = new SRCPReply(s1);
        Assert.assertEquals("CV value",99,m1.value());
        String s2 = "12345678910 100 OK REASON GOES HERE\n\r";
        SRCPReply m2 = new SRCPReply(s2);
        Assert.assertEquals("CV value",-1,m2.value());
        jmri.util.JUnitAppender.assertErrorMessage("Unable to get number from reply: \"12345678910 100 OK REASON GOES HERE\"");
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = msg = new SRCPReply();
    }

    @After
    public void tearDown() {
	m = msg = null;
        JUnitUtil.tearDown();
    }
}
