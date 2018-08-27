package jmri.jmrix.nce;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Vector;
import jmri.JmriException;
import jmri.ProgrammingMode;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit tests for the NceProgrammer class
 * <P>
 * before conversion to JUnit4, most tests had names starting with x, which 
 * disables them in JUnit3; a note why that was done would have been good!
 * These tests now have a JUnit4 Ignore attribute.
 *
 * @author	Bob Jacobsen
 */
public class NceProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {

    @Test
    @Override
    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.PAGEMODE,
                programmer.getMode());        
    }
    
    @Override
    @Test
    public void testDefaultViaBestMode() {
        Assert.assertEquals("Check Default", ProgrammingMode.PAGEMODE,
                ((NceProgrammer)programmer).getBestMode());        
    }

    @Override
    @Test
    public void testGetCanWriteAddress() {
        Assert.assertFalse("can write address", programmer.getCanWrite("1234"));
    }    


    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        // infrastructure objects
        l = new jmri.ProgListenerScaffold();
        tc = new NceInterfaceScaffold();
        tc.setCommandOptions(NceTrafficController.OPTION_2004);
        programmer = p = new NceProgrammer(tc);
    }

    @Override
    @After
    public void tearDown() {
        tc = null;
        programmer = p = null;
	jmri.util.JUnitUtil.tearDown();
    }

    // infrastructure objects
    private NceInterfaceScaffold tc;
    private NceProgrammer p = null;
    private jmri.ProgListenerScaffold l;

    @Test
    @Ignore("disabled for some reason in JUnit3")
    public void testWriteCvSequenceAscii() throws JmriException, Exception {
        // and do the write
        p.writeCV(10, 20, l);
        // correct message sent
        Assert.assertEquals("mode message sent", 1, tc.outbound.size());
        Assert.assertEquals("write message contents", "P010 020",
                ((tc.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        tc.sendTestReply(r, p);
        Assert.assertEquals(" got data value back", 20, l.getRcvdValue());
        Assert.assertEquals(" listener invoked", 1, l.getRcvdInvoked());
    }

    // Test names ending with "String" are for the new writeCV(String, ...)
    // etc methods.  If you remove the older writeCV(int, ...) tests,
    // you can rename these. Note that not all (int,...) tests may have a
    // String(String, ...) test defined, in which case you should create those.
    @Test
    @Ignore("disabled for some reason in JUnit3")
    public void testWriteCvSequenceAsciiString() throws JmriException, Exception {
        // and do the write
        p.writeCV("10", 20, l);
        // correct message sent
        Assert.assertEquals("mode message sent", 1, tc.outbound.size());
        Assert.assertEquals("write message contents", "P010 020",
                ((tc.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        tc.sendTestReply(r, p);
        Assert.assertEquals(" got data value back", 20, l.getRcvdValue());
        Assert.assertEquals(" listener invoked", 1, l.getRcvdInvoked());
    }

    @Test
    @Ignore("disabled for some reason in JUnit3")
    public void testWriteCvSequenceBin() throws JmriException {
        // and do the write
        p.writeCV(10, 20, l);
        // correct message sent
        Assert.assertEquals("mode message sent", 1, tc.outbound.size());
        Assert.assertEquals("write message contents", "A0 00 0A 14",
                ((tc.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        tc.sendTestReply(r, p);
        Assert.assertEquals(" got data value back", 20, l.getRcvdValue());
        Assert.assertEquals(" listener invoked", 1, l.getRcvdInvoked());
    }

    @Test
    @Ignore("disabled for some reason in JUnit3")
    public void testWriteCvSequenceBinString() throws JmriException {
        // and do the write
        p.writeCV("10", 20, l);
        // correct message sent
        Assert.assertEquals("mode message sent", 1, tc.outbound.size());
        Assert.assertEquals("write message contents", "A0 00 0A 14",
                ((tc.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        tc.sendTestReply(r, p);
        Assert.assertEquals(" got data value back", 20, l.getRcvdValue());
        Assert.assertEquals(" listener invoked", 1, l.getRcvdInvoked());
    }

    @Test
    @Ignore("disabled for some reason in JUnit3")
    public void testWriteRegisterSequenceAscii() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the write
        p.writeCV(3, 12, l);
        // check "prog mode" message sent
        Assert.assertEquals("write message sent", 1, tc.outbound.size());
        Assert.assertEquals("write message contents", "S3 012",
                ((tc.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        tc.sendTestReply(r, p);
        Assert.assertEquals(" got data value back", 12, l.getRcvdValue());
        Assert.assertEquals(" listener invoked", 1, l.getRcvdInvoked());
    }

    @Test
    @Ignore("disabled for some reason in JUnit3")
    public void testWriteRegisterSequenceAsciiString() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the write
        p.writeCV("3", 12, l);
        // check "prog mode" message sent
        Assert.assertEquals("write message sent", 1, tc.outbound.size());
        Assert.assertEquals("write message contents", "S3 012",
                ((tc.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        tc.sendTestReply(r, p);
        Assert.assertEquals(" got data value back", 12, l.getRcvdValue());
        Assert.assertEquals(" listener invoked", 1, l.getRcvdInvoked());
    }

    @Test
    @Ignore("disabled for some reason in JUnit3")
    public void testWriteRegisterSequenceBin() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the write
        p.writeCV(3, 12, l);
        // check "prog mode" message sent
        Assert.assertEquals("write message sent", 1, tc.outbound.size());
        Assert.assertEquals("write message contents", "A6 03 0C",
                ((tc.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        tc.sendTestReply(r, p);
        Assert.assertEquals(" got data value back", 12, l.getRcvdValue());
        Assert.assertEquals(" listener invoked", 1, l.getRcvdInvoked());
    }

    @Test
    @Ignore("disabled for some reason in JUnit3")
    public void testWriteRegisterSequenceBinString() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the write
        p.writeCV("3", 12, l);
        // check "prog mode" message sent
        Assert.assertEquals("write message sent", 1, tc.outbound.size());
        Assert.assertEquals("write message contents", "A6 03 0C",
                ((tc.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        tc.sendTestReply(r, p);
        Assert.assertEquals(" got data value back", 12, l.getRcvdValue());
        Assert.assertEquals(" listener invoked", 1, l.getRcvdInvoked());
    }

    @Test
    @Ignore("disabled for some reason in JUnit3")
    public void testReadCvSequenceAscii() throws JmriException {
        // and do the read
        p.readCV(10, l);

        // check "read command" message sent
        Assert.assertEquals("read message sent", 1, tc.outbound.size());
        Assert.assertEquals("read message contents", "R010",
                ((tc.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        r.setElement(0, '0');
        r.setElement(1, '2');
        r.setElement(2, '0');
        tc.sendTestReply(r, p);

        Assert.assertEquals(" programmer listener invoked", 1, l.getRcvdInvoked());
        Assert.assertEquals(" value read", 20, l.getRcvdValue());
    }

    @Test
    @Ignore("disabled for some reason in JUnit3")
    public void testReadCvSequenceAsciiString() throws JmriException {
        // and do the read
        p.readCV("10", l);

        // check "read command" message sent
        Assert.assertEquals("read message sent", 1, tc.outbound.size());
        Assert.assertEquals("read message contents", "R010",
                ((tc.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        r.setElement(0, '0');
        r.setElement(1, '2');
        r.setElement(2, '0');
        tc.sendTestReply(r, p);

        Assert.assertEquals(" programmer listener invoked", 1, l.getRcvdInvoked());
        Assert.assertEquals(" value read", 20, l.getRcvdValue());
    }

    @Test
    @Ignore("disabled for some reason in JUnit3")
    public void testReadCvSequenceBin() throws JmriException {
        // and do the read
        p.readCV(10, l);

        // check "read command" message sent
        Assert.assertEquals("read message sent", 1, tc.outbound.size());
        Assert.assertEquals("read message contents", "A1 00 0A",
                ((tc.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        r.setElement(0, '0');
        r.setElement(1, '2');
        r.setElement(2, '0');
        tc.sendTestReply(r, p);

        Assert.assertEquals(" programmer listener invoked", 1, l.getRcvdInvoked());
        Assert.assertEquals(" value read", 20, l.getRcvdValue());
    }

    @Test
    @Ignore("disabled for some reason in JUnit3")
    public void testReadRegisterSequenceAscii() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the read
        p.readCV(3, l);

        // check "read command" message sent
        Assert.assertEquals("read message sent", 1, tc.outbound.size());
        Assert.assertEquals("read message contents", "V3",
                ((tc.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        r.setElement(0, '0');
        r.setElement(1, '2');
        r.setElement(2, '0');
        tc.sendTestReply(r, p);

        Assert.assertEquals(" programmer listener invoked", 1, l.getRcvdInvoked());
        Assert.assertEquals(" value read", 20, l.getRcvdValue());
    }

    @Test
    @Ignore("disabled for some reason in JUnit3")
    public void testReadRegisterSequenceBin() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the read
        p.readCV(3, l);

        // check "read command" message sent
        Assert.assertEquals("read message sent", 1, tc.outbound.size());
        Assert.assertEquals("read message contents", "A7 03",
                ((tc.outbound.elementAt(0))).toString());
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        r.setElement(0, '0');
        r.setElement(1, '2');
        r.setElement(2, '0');
        tc.sendTestReply(r, p);

        Assert.assertEquals(" programmer listener invoked", 1, l.getRcvdInvoked());
        Assert.assertEquals(" value read", 20, l.getRcvdValue());
    }

    // private final static Logger log = LoggerFactory.getLogger(NceProgrammerTest.class);

}
