package jmri.jmrix.loconet;

import jmri.jmrix.AbstractPowerManagerTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * tests for the Jmri package LnPowerManager
 *
 * @author	Bob Jacobsen Copyright 2001
 */
public class LnPowerManagerTest extends AbstractPowerManagerTest {

    /**
     * service routines to simulate receiving on, off from interface
     */
    protected void hearOn() {
        LocoNetMessage l = new LocoNetMessage(2);
        l.setOpCode(LnConstants.OPC_GPON);
        controller.sendTestMessage(l);
    }

    protected void sendOnReply() {
        hearOn();
    }

    protected void hearOff() {
        LocoNetMessage l = new LocoNetMessage(2);
        l.setOpCode(LnConstants.OPC_GPOFF);
        controller.sendTestMessage(l);
    }

    protected void sendOffReply() {
        hearOff();
    }

    protected int numListeners() {
        return controller.numListeners();
    }

    protected int outboundSize() {
        return controller.outbound.size();
    }

    protected boolean outboundOnOK(int index) {
        return LnConstants.OPC_GPON
                == controller.outbound.elementAt(index).getOpCode();
    }

    protected boolean outboundOffOK(int index) {
        return LnConstants.OPC_GPOFF
                == controller.outbound.elementAt(index).getOpCode();
    }

    // setup a default interface
    public void setUp() {
        controller = new LocoNetInterfaceScaffold();
        p = new LnPowerManager(new LocoNetSystemConnectionMemo(controller, null));
    }

    LocoNetInterfaceScaffold controller;  // holds dummy for testing

    // from here down is testing infrastructure
    public LnPowerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LnPowerManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LnPowerManagerTest.class);
        return suite;
    }

}
