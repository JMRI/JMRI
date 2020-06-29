package jmri.jmrix.rfid.generic.standalone;

import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class StandaloneReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    private StandaloneTrafficController tc = null;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        RfidSystemConnectionMemo memo = new RfidSystemConnectionMemo();
        memo.setProtocol(new jmri.jmrix.rfid.protocol.coreid.CoreIdRfidProtocol());
        tc = new StandaloneTrafficController(memo);
        m = new StandaloneReply(tc);
    }

    @After
    public void tearDown() {
        m = null;
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(StandaloneReplyTest.class);
}
