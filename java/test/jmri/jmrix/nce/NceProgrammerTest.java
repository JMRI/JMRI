// NceProgrammerTest.java
package jmri.jmrix.nce;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Vector;
import jmri.JmriException;
import jmri.managers.DefaultProgrammerManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit tests for the NceProgrammer class
 * <P>
 * Note most have names starting with x, which disables them; a note why that
 * was done would have been good!
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class NceProgrammerTest extends TestCase {

    public void setUp() {
        tc = new NceTrafficController();
        saveCommandOptions = tc.getCommandOptions();
    }

    public void tearDown() {
        tc.setCommandOptions(saveCommandOptions);
    }
    NceTrafficController tc;
    int saveCommandOptions;

    public void testCreate() {
        NceTrafficController tc = new NceTrafficController();
        NceProgrammer p = new NceProgrammer(tc);

        Assert.assertNotNull("programmer exists", p);
    }

    public void xtestWriteCvSequenceAscii() throws JmriException, Exception {

        // infrastructure objects
        NceInterfaceScaffold t = new NceInterfaceScaffold();
        NceListenerScaffold l = new NceListenerScaffold();
        NceTrafficController tc = new NceTrafficController();
        tc.setCommandOptions(NceTrafficController.OPTION_2004);

        NcePortControllerScaffold pC = new NcePortControllerScaffold();

        tc.connectPort(pC);

        NceProgrammer p = new NceProgrammer(tc);

        // and do the write
        p.writeCV(10, 20, l);
        // correct message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "P010 020",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        t.sendTestReply(r, p);
        Assert.assertEquals(" got data value back", 20, rcvdValue);
        Assert.assertEquals(" listener invoked", 1, rcvdInvoked);
    }

    // Test names ending with "String" are for the new writeCV(String, ...) 
    // etc methods.  If you remove the older writeCV(int, ...) tests, 
    // you can rename these. Note that not all (int,...) tests may have a 
    // String(String, ...) test defined, in which case you should create those.
    public void xtestWriteCvSequenceAsciiString() throws JmriException, Exception {

        // infrastructure objects
        NceInterfaceScaffold t = new NceInterfaceScaffold();
        NceListenerScaffold l = new NceListenerScaffold();
        NceTrafficController tc = new NceTrafficController();
        tc.setCommandOptions(NceTrafficController.OPTION_2004);

        NcePortControllerScaffold pC = new NcePortControllerScaffold();

        tc.connectPort(pC);

        NceProgrammer p = new NceProgrammer(tc);

        // and do the write
        p.writeCV("10", 20, l);
        // correct message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "P010 020",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        t.sendTestReply(r, p);
        Assert.assertEquals(" got data value back", 20, rcvdValue);
        Assert.assertEquals(" listener invoked", 1, rcvdInvoked);
    }

    public void xtestWriteCvSequenceBin() throws JmriException {
        tc.setCommandOptions(NceTrafficController.OPTION_2006);
        // infrastructure objects
        NceInterfaceScaffold t = new NceInterfaceScaffold();
        NceListenerScaffold l = new NceListenerScaffold();

        NceProgrammer p = new NceProgrammer(tc);

        // and do the write
        p.writeCV(10, 20, l);
        // correct message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "A0 00 0A 14",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        t.sendTestReply(r, p);
        Assert.assertEquals(" got data value back", 20, rcvdValue);
        Assert.assertEquals(" listener invoked", 1, rcvdInvoked);
    }

    public void xtestWriteCvSequenceBinString() throws JmriException {
        tc.setCommandOptions(NceTrafficController.OPTION_2006);
        // infrastructure objects
        NceInterfaceScaffold t = new NceInterfaceScaffold();
        NceListenerScaffold l = new NceListenerScaffold();

        NceProgrammer p = new NceProgrammer(tc);

        // and do the write
        p.writeCV("10", 20, l);
        // correct message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "A0 00 0A 14",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        t.sendTestReply(r, p);
        Assert.assertEquals(" got data value back", 20, rcvdValue);
        Assert.assertEquals(" listener invoked", 1, rcvdInvoked);
    }

    public void xtestWriteRegisterSequenceAscii() throws JmriException {
        tc.setCommandOptions(NceTrafficController.OPTION_2004);
        // infrastructure objects
        NceInterfaceScaffold t = new NceInterfaceScaffold();
        NceListenerScaffold l = new NceListenerScaffold();

        NceProgrammer p = new NceProgrammer(tc);

        // set register mode
        p.setMode(DefaultProgrammerManager.REGISTERMODE);

        // and do the write
        p.writeCV(3, 12, l);
        // check "prog mode" message sent
        Assert.assertEquals("write message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "S3 012",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        t.sendTestReply(r, p);
        Assert.assertEquals(" got data value back", 12, rcvdValue);
        Assert.assertEquals(" listener invoked", 1, rcvdInvoked);
    }

    public void xtestWriteRegisterSequenceAsciiString() throws JmriException {
        tc.setCommandOptions(NceTrafficController.OPTION_2004);
        // infrastructure objects
        NceInterfaceScaffold t = new NceInterfaceScaffold();
        NceListenerScaffold l = new NceListenerScaffold();

        NceProgrammer p = new NceProgrammer(tc);

        // set register mode
        p.setMode(DefaultProgrammerManager.REGISTERMODE);

        // and do the write
        p.writeCV("3", 12, l);
        // check "prog mode" message sent
        Assert.assertEquals("write message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "S3 012",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        t.sendTestReply(r, p);
        Assert.assertEquals(" got data value back", 12, rcvdValue);
        Assert.assertEquals(" listener invoked", 1, rcvdInvoked);
    }

    public void xtestWriteRegisterSequenceBin() throws JmriException {
        tc.setCommandOptions(NceTrafficController.OPTION_2006);
        // infrastructure objects
        NceInterfaceScaffold t = new NceInterfaceScaffold();
        NceListenerScaffold l = new NceListenerScaffold();

        NceProgrammer p = new NceProgrammer(tc);

        // set register mode
        p.setMode(DefaultProgrammerManager.REGISTERMODE);

        // and do the write
        p.writeCV(3, 12, l);
        // check "prog mode" message sent
        Assert.assertEquals("write message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "A6 03 0C",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        t.sendTestReply(r, p);
        Assert.assertEquals(" got data value back", 12, rcvdValue);
        Assert.assertEquals(" listener invoked", 1, rcvdInvoked);
    }

    public void xtestWriteRegisterSequenceBinString() throws JmriException {
        tc.setCommandOptions(NceTrafficController.OPTION_2006);
        // infrastructure objects
        NceInterfaceScaffold t = new NceInterfaceScaffold();
        NceListenerScaffold l = new NceListenerScaffold();

        NceProgrammer p = new NceProgrammer(tc);

        // set register mode
        p.setMode(DefaultProgrammerManager.REGISTERMODE);

        // and do the write
        p.writeCV("3", 12, l);
        // check "prog mode" message sent
        Assert.assertEquals("write message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "A6 03 0C",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        t.sendTestReply(r, p);
        Assert.assertEquals(" got data value back", 12, rcvdValue);
        Assert.assertEquals(" listener invoked", 1, rcvdInvoked);
    }

    public void xtestReadCvSequenceAscii() throws JmriException {
        tc.setCommandOptions(NceTrafficController.OPTION_2004);
        // infrastructure objects
        NceInterfaceScaffold t = new NceInterfaceScaffold();
        NceListenerScaffold l = new NceListenerScaffold();

        NceProgrammer p = new NceProgrammer(tc);

        // and do the read
        p.readCV(10, l);

        // check "read command" message sent
        Assert.assertEquals("read message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "R010",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        r.setElement(0, '0');
        r.setElement(1, '2');
        r.setElement(2, '0');
        t.sendTestReply(r, p);

        Assert.assertEquals(" programmer listener invoked", 1, rcvdInvoked);
        Assert.assertEquals(" value read", 20, rcvdValue);
    }

    public void xtestReadCvSequenceAsciiString() throws JmriException {
        tc.setCommandOptions(NceTrafficController.OPTION_2004);
        // infrastructure objects
        NceInterfaceScaffold t = new NceInterfaceScaffold();
        NceListenerScaffold l = new NceListenerScaffold();

        NceProgrammer p = new NceProgrammer(tc);

        // and do the read
        p.readCV("10", l);

        // check "read command" message sent
        Assert.assertEquals("read message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "R010",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        r.setElement(0, '0');
        r.setElement(1, '2');
        r.setElement(2, '0');
        t.sendTestReply(r, p);

        Assert.assertEquals(" programmer listener invoked", 1, rcvdInvoked);
        Assert.assertEquals(" value read", 20, rcvdValue);
    }

    public void xtestReadCvSequenceBin() throws JmriException {
        tc.setCommandOptions(NceTrafficController.OPTION_2006);
        // infrastructure objects
        NceInterfaceScaffold t = new NceInterfaceScaffold();
        NceListenerScaffold l = new NceListenerScaffold();

        NceProgrammer p = new NceProgrammer(tc);

        // and do the read
        p.readCV(10, l);

        // check "read command" message sent
        Assert.assertEquals("read message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "A1 00 0A",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        r.setElement(0, '0');
        r.setElement(1, '2');
        r.setElement(2, '0');
        t.sendTestReply(r, p);

        Assert.assertEquals(" programmer listener invoked", 1, rcvdInvoked);
        Assert.assertEquals(" value read", 20, rcvdValue);
    }

    public void xtestReadRegisterSequenceAscii() throws JmriException {
        tc.setCommandOptions(NceTrafficController.OPTION_2004);
        // infrastructure objects
        NceInterfaceScaffold t = new NceInterfaceScaffold();
        NceListenerScaffold l = new NceListenerScaffold();

        NceProgrammer p = new NceProgrammer(tc);

        // set register mode
        p.setMode(DefaultProgrammerManager.REGISTERMODE);

        // and do the read
        p.readCV(3, l);

        // check "read command" message sent
        Assert.assertEquals("read message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "V3",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        r.setElement(0, '0');
        r.setElement(1, '2');
        r.setElement(2, '0');
        t.sendTestReply(r, p);

        Assert.assertEquals(" programmer listener invoked", 1, rcvdInvoked);
        Assert.assertEquals(" value read", 20, rcvdValue);
    }

    public void xtestReadRegisterSequenceBin() throws JmriException {
        tc.setCommandOptions(NceTrafficController.OPTION_2006);
        // infrastructure objects
        NceInterfaceScaffold t = new NceInterfaceScaffold();
        NceListenerScaffold l = new NceListenerScaffold();

        NceProgrammer p = new NceProgrammer(tc);

        // set register mode
        p.setMode(DefaultProgrammerManager.REGISTERMODE);

        // and do the read
        p.readCV(3, l);

        // check "read command" message sent
        Assert.assertEquals("read message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "A7 03",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        r.setElement(0, '0');
        r.setElement(1, '2');
        r.setElement(2, '0');
        t.sendTestReply(r, p);

        Assert.assertEquals(" programmer listener invoked", 1, rcvdInvoked);
        Assert.assertEquals(" value read", 20, rcvdValue);
    }

    // internal class to simulate a NceListener
    class NceListenerScaffold implements jmri.ProgListener {

        public NceListenerScaffold() {
            rcvdInvoked = 0;
            rcvdValue = 0;
            rcvdStatus = 0;
        }

        public void programmingOpReply(int value, int status) {
            rcvdValue = value;
            rcvdStatus = status;
            rcvdInvoked++;
        }
    }
    int rcvdValue;
    int rcvdStatus;
    int rcvdInvoked;

    // service internal class to handle transmit/receive for tests
    class NceInterfaceScaffold extends NceTrafficController {

        public NceInterfaceScaffold() {
        }

        // override some NceInterfaceController methods for test purposes
        public boolean status() {
            return true;
        }

        /**
         * record messages sent, provide access for making sure they are OK
         */
        public Vector<NceMessage> outbound = new Vector<NceMessage>();  // public OK here, so long as this is a test class

        public void sendNceMessage(NceMessage m, jmri.jmrix.nce.NceListener l) {
            if (log.isDebugEnabled()) {
                log.debug("sendNceMessage [" + m + "]");
            }
            // save a copy
            outbound.addElement(m);
            mLastSender = l;
        }

        // test control member functions
        /**
         * forward a message to the listeners, e.g. test receipt
         */
        protected void sendTestMessage(NceMessage m) {
            // forward a test message to Listeners
            if (log.isDebugEnabled()) {
                log.debug("sendTestMessage    [" + m + "]");
            }
            notifyMessage(m, null);
            return;
        }

        protected void sendTestReply(NceReply m, NceProgrammer p) {
            // forward a test message to Listeners
            if (log.isDebugEnabled()) {
                log.debug("sendTestReply    [" + m + "]");
            }
            notifyReply(m, p);
            return;
        }

        /*
         * Check number of listeners, used for testing dispose()
         */
        public int numListeners() {
            return cmdListeners.size();
        }

    }

    // internal class to simulate a NcePortController
    class NcePortControllerScaffold extends NcePortController {

        public java.util.Vector<String> getPortNames() {
            return null;
        }

        public String openPort(String portName, String appName) {
            return null;
        }

        public void configure() {
        }

        public String[] validBaudRates() {
            return null;
        }

        protected NcePortControllerScaffold() throws Exception {
            super(null);
            PipedInputStream tempPipe;
            tempPipe = new PipedInputStream();
            tostream = new DataInputStream(tempPipe);
            ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
            tempPipe = new PipedInputStream();
            istream = new DataInputStream(tempPipe);
            tistream = new DataOutputStream(new PipedOutputStream(tempPipe));
        }

        // returns the InputStream from the port
        public DataInputStream getInputStream() {
            return istream;
        }

        // returns the outputStream to the port
        public DataOutputStream getOutputStream() {
            return ostream;
        }

        // check that this object is ready to operate
        public boolean status() {
            return true;
        }
    }
    static DataOutputStream ostream;  // Traffic controller writes to this
    static DataInputStream tostream; // so we can read it from this

    static DataOutputStream tistream; // tests write to this
    static DataInputStream istream;  // so the traffic controller can read from this

    // from here down is testing infrastructure
    // from here down is testing infrastructure
    public NceProgrammerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NceProgrammerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NceProgrammerTest.class);
        return suite;
    }

    // The minimal setup is for log4J
    // apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    // protected void setUp() { log4jfixtureInst.setUp(); }
    // protected void tearDown() { log4jfixtureInst.tearDown(); }
    private final static Logger log = LoggerFactory.getLogger(NceProgrammerTest.class.getName());

}
