package jmri.jmrix.rfid.generic.standalone;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class StandaloneReplyTest {

    private StandaloneTrafficController tc = null;

    @Test
    public void testCTor() {
        StandaloneReply t = new StandaloneReply(tc);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        StandaloneSystemConnectionMemo memo = new StandaloneSystemConnectionMemo();
        memo.setProtocol(new jmri.jmrix.rfid.protocol.coreid.CoreIdRfidProtocol());
        tc = new StandaloneTrafficController(memo);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(StandaloneReplyTest.class);

}
