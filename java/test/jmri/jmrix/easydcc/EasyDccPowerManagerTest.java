// EasyDccPowerManagerTest.java
package jmri.jmrix.easydcc;

import java.util.Vector;
import jmri.JmriException;
import jmri.jmrix.AbstractPowerManagerTest;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:	JUnit tests for the EasyDccPowerManager class
 *
 * @author	Bob Jacobsen Copyright 2006
 * @version
 */
public class EasyDccPowerManagerTest extends AbstractPowerManagerTest {

    /**
     * provide an implementation to detect outbound messages
     */
    class EasyDccInterfaceScaffold extends EasyDccTrafficController {

        public EasyDccInterfaceScaffold() {
        }

        // override some EasyDccInterfaceController methods for test purposes
        public boolean status() {
            return true;
        }

        /**
         * record messages sent, provide access for making sure they are OK
         */
        public Vector<EasyDccMessage> outbound = new Vector<EasyDccMessage>();  // public OK here, so long as this is a test class

        public void sendEasyDccMessage(EasyDccMessage m, EasyDccListener l) {
            // save a copy
            outbound.addElement(m);
        }

        // test control member functions
        /**
         * forward a message to the listeners, e.g. test receipt
         */
        protected void sendTestMessage(EasyDccMessage m) {
            // forward a test message to Listeners
            notifyMessage(m, null);
            return;
        }

        protected void sendTestReply(EasyDccReply m) {
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

    // service routines to simulate recieving on, off from interface
    protected void hearOn() {
        // this does nothing, as there is no unsolicited on
    }

    protected void sendOnReply() {
        EasyDccReply l = new EasyDccReply();
        controller.sendTestReply(l);
    }

    protected void sendOffReply() {
        EasyDccReply l = new EasyDccReply();
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
        return 'E' == ((controller.outbound.elementAt(index))).getOpCode();
    }

    protected boolean outboundOffOK(int index) {
        return 'K' == ((controller.outbound.elementAt(index))).getOpCode();
    }

    // setup a default EasyDccTrafficController interface
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        controller = new EasyDccInterfaceScaffold();
        p = new EasyDccPowerManager();
    }

    EasyDccInterfaceScaffold controller;  // holds dummy EasyDccTrafficController for testing

    // replace some standard tests, as there's no unsolicted message from the
    // master saying power has changed.  Instead, these test the 
    // state readback by sending messages & getting a reply
    public void testStateOn() throws JmriException {
    }

    public void testStateOff() throws JmriException {
    }

    // from here down is testing infrastructure
    public EasyDccPowerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EasyDccPowerManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EasyDccPowerManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccPowerManagerTest.class.getName());

}
