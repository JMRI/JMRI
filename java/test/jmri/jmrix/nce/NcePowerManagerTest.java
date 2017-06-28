package jmri.jmrix.nce;

import jmri.JmriException;
import jmri.jmrix.AbstractPowerManagerTestBase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for the NcePowerManager class.
 *
 * @author	Bob Jacobsen
  */
public class NcePowerManagerTest extends AbstractPowerManagerTestBase {

    // service routines to simulate receiving on, off from interface
    @Override
    protected void hearOn() {
        // this does nothing, as there is no unsolicited on
    }

    @Override
    protected void sendOnReply() {
        NceReply l = new NceReply(controller);
        controller.sendTestReply(l);
    }

    @Override
    protected void sendOffReply() {
        NceReply l = new NceReply(controller);
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
        return controller.outbound.elementAt(index).isEnableMain();
    }

    @Override
    protected boolean outboundOffOK(int index) {
        return controller.outbound.elementAt(index).isKillMain();
    }

    // setup a default NceTrafficController interface
    @Before
    @Override
    public void setUp() {
        controller = new NceTrafficControlScaffold();
        p = new NcePowerManager(controller, "N");
    }

    NceTrafficControlScaffold controller;  // holds dummy NceTrafficController for testing

    // replace some standard tests, as there's no unsolicted message from the
    // master saying power has changed.  Instead, these test the
    // state readback by sending messages & getting a reply
    @Override
    @Test
    @Ignore("no unsolicted message, so do not run test")
    public void testStateOn() throws JmriException {
    }

    @Override
    @Test
    @Ignore("no unsolicted message, so do not run test")
    public void testStateOff() throws JmriException {
    }

}
