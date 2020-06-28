package jmri.jmrix.rfid.generic.standalone;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class StandaloneReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    private StandaloneTrafficController tc = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        StandaloneSystemConnectionMemo memo = new StandaloneSystemConnectionMemo();
        memo.setProtocol(new jmri.jmrix.rfid.protocol.coreid.CoreIdRfidProtocol());
        tc = new StandaloneTrafficController(memo);
        m = new StandaloneReply(tc);
    }

    @AfterEach
    public void tearDown() {
        m = null;
        tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(StandaloneReplyTest.class);
}
