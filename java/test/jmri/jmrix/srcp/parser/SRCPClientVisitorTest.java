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
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 0 POWER ON hello world\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
        Assert.assertEquals("12345678910 100 INFO0 POWER",new SRCPReply(e).toString());
    }

    @Test
    public void testInfoPowerOffResponse() throws ParseException {
        boolean exceptionOccured = false;
        String code = "12345678910 100 INFO 0 POWER OFF goodye\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
        Assert.assertEquals("12345678910 100 INFO0 POWER",new SRCPReply(e).toString());
    }

    @Test
    public void testInfoPowerInitResponse() throws ParseException {
        boolean exceptionOccured = false;
        String code = "12345678910 101 INFO 0 POWER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
        Assert.assertEquals("12345678910 101 INFO0 POWER",new SRCPReply(e).toString());
    }

    @Test
    public void testInfoPowerTermResponse() throws ParseException {
        boolean exceptionOccured = false;
        String code = "12345678910 102 INFO 0 POWER\n\r";
        SRCPClientParser p = new SRCPClientParser(new StringReader(code));
        SRCPClientVisitor v = new SRCPClientVisitor();
        SimpleNode e = p.commandresponse();
        e.jjtAccept(v, memo);
        Assert.assertEquals("12345678910 102 INFO0 POWER",new SRCPReply(e).toString());
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
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SRCPClientVisitorTest.class);

}
