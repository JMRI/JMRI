// NcePowerManagerTest.java

package jmri.jmrix.nce;

import org.apache.log4j.Logger;
import jmri.JmriException;
import jmri.jmrix.AbstractPowerManagerTest;

import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * JUnit tests for the NcePowerManager class.
 *
 * @author	Bob Jacobsen
 * @version	$Revision$
 */
public class NcePowerManagerTest extends AbstractPowerManagerTest {

    /**
     * provide an implementation to detect outbound messages
     */
    public class NceInterfaceScaffold extends NceTrafficController {
        public NceInterfaceScaffold() {
        }

        // override some NceInterfaceController methods for test purposes

        public boolean status() { return true;
        }

        /**
         * record messages sent, provide access for making sure they are OK
         */
        public Vector<NceMessage> outbound = new Vector<NceMessage>();  // public OK here, so long as this is a test class
        public void sendNceMessage(NceMessage m, jmri.jmrix.nce.NceListener l) {
            // save a copy
            outbound.addElement(m);
        }

        // test control member functions

        /**
         * forward a message to the listeners, e.g. test receipt
         */
        protected void sendTestMessage (NceMessage m) {
            // forward a test message to Listeners
            notifyMessage(m, null);
            return;
        }
        protected void sendTestReply (NceReply m) {
            // forward a test message to Listeners
            notifyReply(m, null);
            return;
        }

        /*
         * Check number of listeners, used for testing dispose()
         */
        public int numListeners() {
            return cmdListeners.size();
        }

    }

    // service routines to simulate receiving on, off from interface
    protected void hearOn() {
        // this does nothing, as there is no unsolicited on
    }

    protected void sendOnReply() {
        NceReply l = new NceReply(controller);
        controller.sendTestReply(l);
    }

    protected void sendOffReply() {
        NceReply l = new NceReply(controller);
        controller.sendTestReply(l);
    }

    protected void hearOff() {
        // this does nothing, as there is no unsolicited on
    }

    protected int numListeners() {
        return controller.numListeners();
    }

    protected int outboundSize() {
        return controller.outbound.size();
    }

    protected boolean outboundOnOK(int index) {
        return controller.outbound.elementAt(index).isEnableMain();
    }

    protected boolean outboundOffOK(int index) {
        return controller.outbound.elementAt(index).isKillMain();
    }

    // setup a default NceTrafficController interface
    public void setUp() {
        controller = new NceInterfaceScaffold();
        p = new NcePowerManager(controller, "N");
    }

    NceInterfaceScaffold controller;  // holds dummy NceTrafficController for testing

    // replace some standard tests, as there's no unsolicted message from the
    // master saying power has changed.  Instead, these test the
    // state readback by sending messages & getting a reply
    public void testStateOn() throws JmriException {
    }

    public void testStateOff() throws JmriException {
    }

    // from here down is testing infrastructure

    public NcePowerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NcePowerManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NcePowerManagerTest.class);
        return suite;
    }

    static Logger log = Logger.getLogger(NcePowerManagerTest.class.getName());

}
