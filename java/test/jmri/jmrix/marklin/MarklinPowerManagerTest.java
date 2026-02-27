package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for MarklinPowerManager.
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2024
 */
public class MarklinPowerManagerTest extends jmri.jmrix.AbstractPowerManagerTestBase {

    private MarklinSystemConnectionMemo memo;
    private MarklinTrafficControlScaffold tc;
    private MarklinPowerManager pwr;

    @Test
    public void testCTor() {
        Assertions.assertNotNull(p, "exists");
    }

    @Override
    protected void hearOn() {
        MarklinReply r = new MarklinReply(); // default priority 1
        r.setCommand( MarklinConstants.SYSCOMMANDSTART);
        r.setElement(9, MarklinConstants.CMDGOSYS);
        pwr.reply(r);
    }

    @Override
    protected void hearOff() {
        MarklinReply r = new MarklinReply(); // default priority 1
        r.setCommand( MarklinConstants.SYSCOMMANDSTART);
        r.setElement(9, MarklinConstants.CMDSTOPSYS);
        pwr.reply(r);
    }

    @Override
    protected void hearIdle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void sendOnReply() {
        hearOn();
    }

    @Override
    protected void sendOffReply() {
        hearOff();
    }

    @Override
    protected void sendIdleReply() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected int numListeners() {
        return tc.getNumberListeners();
    }

    @Override
    protected int outboundSize() {
        return tc.getSentMessages().size();
    }

    @Override
    protected boolean outboundOnOK(int index) {
        MarklinMessage m = tc.getLastMessageSent();
        return m != null && "00 00 47 11 05 00 00 00 00 01 00 00 00".equals(m.toString());
    }

    @Override
    protected boolean outboundOffOK(int index) {
        MarklinMessage m = tc.getLastMessageSent();
        return m != null && "00 00 47 11 05 00 00 00 00 00 00 00 00".equals(m.toString());
    }

    @Override
    protected boolean outboundIdleOK(int index) {
        return false;
    }

    @Test
    @Disabled("Tested class does not throw exception if dispose called twice")
    @Override
    public void testDispose2() {}


    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new MarklinTrafficControlScaffold();
        memo = new MarklinSystemConnectionMemo(tc);
        pwr = new MarklinPowerManager(tc);
        p = pwr;
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(tc);
        Assertions.assertNotNull(memo);
        tc.dispose();
        tc = null;
        memo.dispose();
        memo = null;
        pwr = null;
        p = null;

        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MarklinPowerManagerTest.class);

}
