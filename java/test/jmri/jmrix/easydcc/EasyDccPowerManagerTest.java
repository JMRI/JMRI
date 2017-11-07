package jmri.jmrix.easydcc;

import jmri.JmriException;
import jmri.jmrix.AbstractPowerManagerTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Description:	JUnit tests for the EasyDccPowerManager class
 *
 * @author	Bob Jacobsen Copyright 2006
 */
public class EasyDccPowerManagerTest extends AbstractPowerManagerTestBase {

    // service routines to simulate recieving on, off from interface
    @Override
    protected void hearOn() {
        // this does nothing, as there is no unsolicited on
    }

    @Override
    protected void sendOnReply() {
        EasyDccReply l = new EasyDccReply();
        controller.sendTestReply(l);
    }

    @Override
    protected void sendOffReply() {
        EasyDccReply l = new EasyDccReply();
        controller.sendTestReply(l);
    }

    @Override
    protected void hearOff() {
        // this does nothing, as there is no unsolicited on
    }

    @Override
    protected int numListeners() {
        return controller.numListeners();
    }

    @Override
    protected int outboundSize() {
        return controller.outbound.size();
    }

    @Override
    protected boolean outboundOnOK(int index) {
        return 'E' == ((controller.outbound.elementAt(index))).getOpCode();
    }

    @Override
    protected boolean outboundOffOK(int index) {
        return 'K' == ((controller.outbound.elementAt(index))).getOpCode();
    }

    // setup a default EasyDccTrafficController interface
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        controller = new EasyDccTrafficControlScaffold();
        p = new EasyDccPowerManager();
    }

    EasyDccTrafficControlScaffold controller;  // holds dummy EasyDccTrafficController for testing

    // replace some standard tests, as there's no unsolicted message from the
    // master saying power has changed.  Instead, these test the 
    // state readback by sending messages & getting a reply
    @Override
    @Test
    @Ignore("no unsolicited messages, so skip test")
    public void testStateOn() throws JmriException {
    }

    @Override
    @Test
    @Ignore("no unsolicited messages, so skip test")
    public void testStateOff() throws JmriException {
    }

    // The minimal setup for log4J
    @After 
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
