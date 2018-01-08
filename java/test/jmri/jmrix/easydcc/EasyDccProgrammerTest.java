/**
 * JUnit tests for the EasyDccProgrammer class
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.easydcc;

import jmri.JmriException;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyDccProgrammerTest {
        
    private EasyDccTrafficControlScaffold t = null;
    private EasyDccSystemConnectionMemo memo = null;
    private EasyDccListenerScaffold l = null;
    private EasyDccProgrammer p = null;

    @Test
    public void testWriteSequence() throws JmriException {
        p.setMode(ProgrammingMode.PAGEMODE);

        // and do the write
        p.writeCV(10, 20, l);

        // check write message sent
        Assert.assertEquals("write message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "P 00A 14",
                ((t.outbound.elementAt(0))).toString());
    }

    // Test names ending with "String" are for the new writeCV(String, ...)
    // etc methods. If you remove the older writeCV(int, ...) tests,
    // you can rename these. Note that not all (int,...) tests may have a
    // String(String, ...) test defined, in which case you should create those.
    @Test
    public void testWriteSequenceString() throws JmriException {
        p.setMode(ProgrammingMode.PAGEMODE);

        // and do the write
        p.writeCV("10", 20, l);

        // check write message sent
        Assert.assertEquals("write message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "P 00A 14",
                ((t.outbound.elementAt(0))).toString());
    }

    @Test
    public void testWriteRegisterSequence() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the write
        p.writeCV(3, 12, l);

        // check write message sent
        Assert.assertEquals("write message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "S3 0C",
                ((t.outbound.elementAt(0))).toString());
    }

    @Test
    public void testWriteRegisterSequenceString() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the write
        p.writeCV("3", 12, l);

        // check write message sent
        Assert.assertEquals("write message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "S3 0C",
                ((t.outbound.elementAt(0))).toString());
    }

    @Test
    public void testReadSequence() throws JmriException {
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

    @Test
    public void testReadSequenceString() throws JmriException {
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

    @Test
    public void testReadRegisterSequence() throws JmriException {
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

    @Test
    public void testReadRegisterSequenceString() throws JmriException {
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
    @Test
    public void testReadFailSequence() throws JmriException {
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

    // internal class to simulate an EasyDccListener
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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new EasyDccSystemConnectionMemo("E", "EasyDCC Test");
        t = new EasyDccTrafficControlScaffold(memo);
        memo.setEasyDccTrafficController(t);
        l = new EasyDccListenerScaffold();
        p = new EasyDccProgrammer(memo);
    }

    @After
    public void tearDown() {
        t.terminateThreads();
        p = null;
        memo = null;
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccProgrammerTest.class);

}
