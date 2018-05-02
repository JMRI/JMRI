package jmri.jmris.srcp.parser;

import java.io.StringReader;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the {@link jmri.jmris.srcp.parser.SRCPVisitor} class.
 *
 * @author Paul Bender Copyright (C) 2012,2017
 * 
 */
public class SRCPVisitorTest {

    @Test
    public void testCTor() {
        // test the constructor.
        SRCPVisitor v = new SRCPVisitor();
        Assert.assertNotNull(v);
    }

    @Test
    public void testGetServer() {
        // test that an inbound "GET 0 SERVER" returns the
        // expected response.
        boolean exceptionOccured = false;
        String code = "GET 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        SRCPVisitor v = new SRCPVisitor();
        try {
            SimpleNode e = p.command();
            e.jjtAccept(v, null);
            Assert.assertEquals(v.getOutputString(), "100 INFO 0 SERVER RUNNING");
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResetServer() {
        // test that an inbound "RESET 0 SERVER" returns the
        // expected response.
        boolean exceptionOccured = false;
        String code = "RESET 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        SRCPVisitor v = new SRCPVisitor();
        try {
            SimpleNode e = p.command();
            e.jjtAccept(v, null);
            Assert.assertEquals(v.getOutputString(), "413 ERROR temporarily prohibited");
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testTERMServer() {
        // test that an inbound "TERM 0 SERVER" returns the
        // expected response.
        boolean exceptionOccured = false;
        String code = "TERM 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        SRCPVisitor v = new SRCPVisitor();
        try {
            SimpleNode e = p.command();
            e.jjtAccept(v, null);
            Assert.assertEquals(v.getOutputString(), "200 OK");
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
