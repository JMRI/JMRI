package jmri.jmrix.nce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import jmri.JmriException;
import jmri.ProgrammingMode;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the NceProgrammer class
 * <p>
 *
 * @author Bob Jacobsen
 */
public class NceProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {

    @Test
    @Override
    public void testDefault() {
        assertEquals( ProgrammingMode.PAGEMODE, programmer.getMode(),
            "Check Default");
    }

    @Override
    @Test
    public void testDefaultViaBestMode() {
        assertEquals( ProgrammingMode.PAGEMODE, ((NceProgrammer) programmer).getBestMode(),
            "Check Default");
    }

    @Override
    @Test
    public void testGetCanWriteAddress() {
        assertFalse( programmer.getCanWrite("1234"), "can write address");
    }

    @Test
    public void testWriteCvSequenceAscii() throws JmriException {
        // and do the write
        p.writeCV("10", 20, l);
        // correct message sent
        assertEquals( 1, tc.outbound.size(), "mode message sent");
        assertEquals( "P010 020",
                ((tc.outbound.elementAt(0))).toString(),
                "write message contents");
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        tc.sendTestReply(r, p);
        assertEquals( 20, l.getRcvdValue(), " got data value back");
        assertEquals( 1, l.getRcvdInvoked(), " listener invoked");
    }

    @Test
    public void testWriteRegisterSequenceAscii() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the write
        p.writeCV("3", 12, l);
        // check "prog mode" message sent
        assertEquals( 1, tc.outbound.size(), "write message sent");
        assertEquals( "S3 012",
                ((tc.outbound.elementAt(0))).toString(),
                "write message contents");
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        tc.sendTestReply(r, p);
        assertEquals( 12, l.getRcvdValue(), " got data value back");
        assertEquals( 1, l.getRcvdInvoked(), " listener invoked");
    }

    @Test
    public void testReadCvSequenceAscii() throws JmriException {
        // and do the read
        p.readCV("10", l);

        // check "read command" message sent
        assertEquals( 1, tc.outbound.size(), "read message sent");
        assertEquals( "R010",
                ((tc.outbound.elementAt(0))).toString(),
                "read message contents");
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        r.setElement(0, '0');
        r.setElement(1, '2');
        r.setElement(2, '0');
        tc.sendTestReply(r, p);

        assertEquals( 1, l.getRcvdInvoked(), " programmer listener invoked");
        assertEquals( 20, l.getRcvdValue(), " value read");
    }

    @Test
    public void testReadRegisterSequenceAscii() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the read
        p.readCV("3", l);

        // check "read command" message sent
        assertEquals( 1, tc.outbound.size(), "read message sent");
        assertEquals( "V3",
                ((tc.outbound.elementAt(0))).toString(),
                "read message contents");
        // reply from programmer arrives
        NceReply r = new NceReply(tc);
        r.setElement(0, '0');
        r.setElement(1, '2');
        r.setElement(2, '0');
        tc.sendTestReply(r, p);

        assertEquals( 1, l.getRcvdInvoked(), " programmer listener invoked");
        assertEquals( 20, l.getRcvdValue(), " value read");
    }

    // infrastructure objects
    private NceInterfaceScaffold tc;
    private NceProgrammer p = null;
    private jmri.ProgListenerScaffold l;

    @Override
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        // infrastructure objects
        l = new jmri.ProgListenerScaffold();
        tc = new NceInterfaceScaffold();
        tc.setCommandOptions(NceTrafficController.OPTION_2004);
        p = new NceProgrammer(tc);
        programmer = p;
    }

    @Override
    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        p = null;
        programmer = null;
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NceProgrammerTest.class);
}
