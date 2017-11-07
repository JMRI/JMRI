/**
 * EasyDccProgrammerTest.java
 *
 * Description:	JUnit tests for the EasyDccProgrammer class
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.easydcc;

import java.util.Vector;
import jmri.JmriException;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyDccProgrammerTest extends TestCase {

    public void testWriteSequence() throws JmriException {
        // infrastructure objects
        EasyDccInterfaceScaffold t = new EasyDccInterfaceScaffold();
        EasyDccListenerScaffold l = new EasyDccListenerScaffold();

        EasyDccProgrammer p = new EasyDccProgrammer();
        p.setMode(ProgrammingMode.PAGEMODE);

        // and do the write
        p.writeCV(10, 20, l);

        // check write message sent
        Assert.assertEquals("write message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "P 00A 14",
                ((t.outbound.elementAt(0))).toString());
    }

    // Test names ending with "String" are for the new writeCV(String, ...)
    // etc methods.  If you remove the older writeCV(int, ...) tests,
    // you can rename these. Note that not all (int,...) tests may have a
    // String(String, ...) test defined, in which case you should create those.
    public void testWriteSequenceString() throws JmriException {
        // infrastructure objects
        EasyDccInterfaceScaffold t = new EasyDccInterfaceScaffold();
        EasyDccListenerScaffold l = new EasyDccListenerScaffold();

        EasyDccProgrammer p = new EasyDccProgrammer();
        p.setMode(ProgrammingMode.PAGEMODE);

        // and do the write
        p.writeCV("10", 20, l);

        // check write message sent
        Assert.assertEquals("write message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "P 00A 14",
                ((t.outbound.elementAt(0))).toString());
    }

    public void testWriteRegisterSequence() throws JmriException {
        // infrastructure objects
        EasyDccInterfaceScaffold t = new EasyDccInterfaceScaffold();
        EasyDccListenerScaffold l = new EasyDccListenerScaffold();

        EasyDccProgrammer p = new EasyDccProgrammer();

        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the write
        p.writeCV(3, 12, l);

        // check write message sent
        Assert.assertEquals("write message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "S3 0C",
                ((t.outbound.elementAt(0))).toString());
    }

    public void testWriteRegisterSequenceString() throws JmriException {
        // infrastructure objects
        EasyDccInterfaceScaffold t = new EasyDccInterfaceScaffold();
        EasyDccListenerScaffold l = new EasyDccListenerScaffold();

        EasyDccProgrammer p = new EasyDccProgrammer();

        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the write
        p.writeCV("3", 12, l);

        // check write message sent
        Assert.assertEquals("write message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "S3 0C",
                ((t.outbound.elementAt(0))).toString());
    }

    public void testReadSequence() throws JmriException {
        // infrastructure objects
        EasyDccInterfaceScaffold t = new EasyDccInterfaceScaffold();
        EasyDccListenerScaffold l = new EasyDccListenerScaffold();

        EasyDccProgrammer p = new EasyDccProgrammer();
        p.setMode(ProgrammingMode.PAGEMODE);

        // and do the read
        p.readCV(10, l);

        // check "read command" message sent
        Assert.assertEquals("read message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "R 00A",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        EasyDccReply r = new EasyDccReply();
        r.setElement(0, 'C');
        r.setElement(1, 'V');
        r.setElement(2, '0');
        r.setElement(3, '1');
        r.setElement(4, '0');
        r.setElement(5, '1');
        r.setElement(6, '4');
        t.sendTestReply(r);
        Assert.assertEquals(" programmer listener invoked", 1, rcvdInvoked);
        Assert.assertEquals(" value read", 20, rcvdValue);
    }

    public void testReadSequenceString() throws JmriException {
        // infrastructure objects
        EasyDccInterfaceScaffold t = new EasyDccInterfaceScaffold();
        EasyDccListenerScaffold l = new EasyDccListenerScaffold();

        EasyDccProgrammer p = new EasyDccProgrammer();
        p.setMode(ProgrammingMode.PAGEMODE);

        // and do the read
        p.readCV("10", l);

        // check "read command" message sent
        Assert.assertEquals("read message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "R 00A",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        EasyDccReply r = new EasyDccReply();
        r.setElement(0, 'C');
        r.setElement(1, 'V');
        r.setElement(2, '0');
        r.setElement(3, '1');
        r.setElement(4, '0');
        r.setElement(5, '1');
        r.setElement(6, '4');
        t.sendTestReply(r);
        Assert.assertEquals(" programmer listener invoked", 1, rcvdInvoked);
        Assert.assertEquals(" value read", 20, rcvdValue);
    }

    public void testReadRegisterSequence() throws JmriException {
        // infrastructure objects
        EasyDccInterfaceScaffold t = new EasyDccInterfaceScaffold();
        EasyDccListenerScaffold l = new EasyDccListenerScaffold();

        EasyDccProgrammer p = new EasyDccProgrammer();

        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the read
        p.readCV(3, l);

        // check "read command" message sent
        Assert.assertEquals("read message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "V3",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        EasyDccReply r = new EasyDccReply();
        r.setElement(0, 'V');
        r.setElement(1, '3');
        r.setElement(2, '1');
        r.setElement(3, '4');
        t.sendTestReply(r);

        Assert.assertEquals(" programmer listener invoked", 1, rcvdInvoked);
        Assert.assertEquals(" value read", 20, rcvdValue);
    }

    public void testReadRegisterSequenceString() throws JmriException {
        // infrastructure objects
        EasyDccInterfaceScaffold t = new EasyDccInterfaceScaffold();
        EasyDccListenerScaffold l = new EasyDccListenerScaffold();

        EasyDccProgrammer p = new EasyDccProgrammer();

        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the read
        p.readCV("3", l);

        // check "read command" message sent
        Assert.assertEquals("read message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "V3",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        EasyDccReply r = new EasyDccReply();
        r.setElement(0, 'V');
        r.setElement(1, '3');
        r.setElement(2, '1');
        r.setElement(3, '4');
        t.sendTestReply(r);

        Assert.assertEquals(" programmer listener invoked", 1, rcvdInvoked);
        Assert.assertEquals(" value read", 20, rcvdValue);
    }

    /**
     * The command station will return a CV001-- format message if programming
     * fails. Test handling of that.
     *
     * @throws JmriException
     */
    public void testReadFailSequence() throws JmriException {
        // infrastructure objects
        EasyDccInterfaceScaffold t = new EasyDccInterfaceScaffold();
        EasyDccListenerScaffold l = new EasyDccListenerScaffold();

        EasyDccProgrammer p = new EasyDccProgrammer();
        p.setMode(ProgrammingMode.PAGEMODE);

        // and do the read
        p.readCV(10, l);

        // check "read command" message sent
        Assert.assertEquals("read message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "R 00A",
                ((t.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        EasyDccReply r = new EasyDccReply();
        r.setElement(0, 'C');
        r.setElement(1, 'V');
        r.setElement(2, '0');
        r.setElement(3, '1');
        r.setElement(4, '0');
        r.setElement(5, '-');
        r.setElement(6, '-');
        t.sendTestReply(r);
        Assert.assertEquals(" programmer listener not invoked again", 1, rcvdInvoked);
    }

    // internal class to simulate a EasyDccListener
    class EasyDccListenerScaffold implements jmri.ProgListener {

        public EasyDccListenerScaffold() {
            rcvdInvoked = 0;
            rcvdValue = 0;
            rcvdStatus = 0;
        }

        @Override
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
    class EasyDccInterfaceScaffold extends EasyDccTrafficController {

        public EasyDccInterfaceScaffold() {
        }

        // override some EasyDccInterfaceController methods for test purposes
        @Override
        public boolean status() {
            return true;
        }

        /**
         * record messages sent, provide access for making sure they are OK
         */
        public Vector<EasyDccMessage> outbound = new Vector<EasyDccMessage>();  // public OK here, so long as this is a test class

        @Override
        public void sendEasyDccMessage(EasyDccMessage m, jmri.jmrix.easydcc.EasyDccListener l) {
            if (log.isDebugEnabled()) {
                log.debug("sendEasyDccMessage [" + m + "]");
            }
            // save a copy
            outbound.addElement(m);
            lastSender = l;
        }

        jmri.jmrix.easydcc.EasyDccListener lastSender;
        // test control member functions

        /**
         * forward a message to the listeners, e.g. test receipt
         */
        protected void sendTestMessage(EasyDccMessage m) {
            // forward a test message to Listeners
            if (log.isDebugEnabled()) {
                log.debug("sendTestMessage    [" + m + "]");
            }
            notifyMessage(m, null);
            return;
        }

        protected void sendTestReply(EasyDccReply m) {
            // forward a test message to Listeners
            if (log.isDebugEnabled()) {
                log.debug("sendTestReply    [" + m + "]");
            }
            notifyReply(m, lastSender);
            return;
        }

        /*
         * Check number of listeners, used for testing dispose()
         */
        public int numListeners() {
            return cmdListeners.size();
        }

    }

    // from here down is testing infrastructure
    public EasyDccProgrammerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EasyDccProgrammerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EasyDccProgrammerTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccProgrammerTest.class);

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
