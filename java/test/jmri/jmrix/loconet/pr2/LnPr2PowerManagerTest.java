package jmri.jmrix.loconet.pr2;

import jmri.jmrix.AbstractPowerManagerTestBase;
import org.junit.Before;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LnConstants;

/**
 * tests for the Jmri package LnPr2PowerManager
 *
 * @author	Bob Jacobsen Copyright 2001
 */
public class LnPr2PowerManagerTest extends AbstractPowerManagerTestBase {

    /**
     * service routines to simulate receiving on, off from interface
     */
    @Override
    protected void hearOn() {
        LocoNetMessage l = new LocoNetMessage(2);
        l.setOpCode(LnConstants.OPC_GPON);
        controller.sendTestMessage(l);
    }

    @Override
    protected void sendOnReply() {
        hearOn();
    }

    @Override
    protected void hearOff() {
        LocoNetMessage l = new LocoNetMessage(2);
        l.setOpCode(LnConstants.OPC_GPOFF);
        controller.sendTestMessage(l);
    }

    @Override
    protected void sendOffReply() {
        hearOff();
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
        return LnConstants.OPC_GPON
                == controller.outbound.elementAt(index).getOpCode();
    }

    @Override
    protected boolean outboundOffOK(int index) {
        return LnConstants.OPC_GPOFF
                == controller.outbound.elementAt(index).getOpCode();
    }

    // setup a default interface
    @Before
    @Override
    public void setUp() {
        controller = new LocoNetInterfaceScaffold();
        p = new LnPr2PowerManager(new LocoNetSystemConnectionMemo(controller, null));
    }

    LocoNetInterfaceScaffold controller;  // holds dummy for testing

}
