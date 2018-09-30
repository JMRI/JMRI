package jmri.jmrix.xpa;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Description:	tests for the jmri.jmrix.xpa.XpaPowerManager class
 * <P>
 * @author	Paul Bender
 */
public class XpaPowerManagerTest extends jmri.jmrix.AbstractPowerManagerTestBase {

    private XpaTrafficControlScaffold tc = null;

    // service routines to simulate receiving on, off from interface
    @Override
    protected void hearOn() {
    }

    @Override
    protected void sendOnReply() {
        ((XpaPowerManager) p).reply(new XpaMessage("ATDT0;"));
    }

    @Override
    protected void sendOffReply() {
        ((XpaPowerManager) p).reply(new XpaMessage("ATDT0;"));
    }

    @Override
    protected void hearOff() {
    }

    @Override
    protected int numListeners() {
        return tc.numListeners();
    }

    @Override
    protected int outboundSize() {
        return tc.outbound.size();
    }

    @Override
    protected boolean outboundOnOK(int index) {
        return ((tc.outbound.get(index))).toString().equals("ATDT0;");
    }

    @Override
    protected boolean outboundOffOK(int index) {
        return ((tc.outbound.get(index))).toString().equals("ATDT0;");
    }

    @Test
    @Override
    @Ignore("unsolicited state changes are currently ignored")
    public void testStateOn() {
    }

    @Test
    @Override
    @Ignore("unsolicited state changes are currently ignored")
    public void testStateOff() {
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new XpaTrafficControlScaffold();
        p = new XpaPowerManager(tc);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
        tc = null;
    }

}
