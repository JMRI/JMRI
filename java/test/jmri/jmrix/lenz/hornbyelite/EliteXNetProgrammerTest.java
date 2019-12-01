/**
 * EliteXNetProgrammerTest.java
 *
 * Description:	JUnit tests for the EliteXNetProgrammer class
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.lenz.hornbyelite;

import jmri.JmriException;
import jmri.ProgrammingMode;
import jmri.ProgListenerScaffold;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import org.junit.*;

public class EliteXNetProgrammerTest extends jmri.jmrix.lenz.XNetProgrammerTest {

    @Test
    @Override
    @Ignore("Elite behavior is unknown for this sequence")
    @ToDo("investigate proper sequence and reimplement test")
    public void testWriteHighCvSequence() throws JmriException {
    }

    @Test
    @Override
    @Ignore("Elite behavior is unknown for this sequence")
    @ToDo("investigate proper sequence and reimplement test")
    public void testReadHighCvSequence() throws JmriException {
    }

    @Test
    @Override
    public void testWriteCvSequence() throws JmriException {
        // and do the write
        p.writeCV("08", 48, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "23 16 08 30 0D", t.outbound.elementAt(0).toString());

        // The Elite send broadcast service mode entry twice
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);
        t.sendTestMessage(mr1);

        // we should not send any additional messages here.
        Assert.assertEquals("no new message", 1, t.outbound.size());

        // and then send Normal Operations Resumed twice
        XNetReply mr2 = new XNetReply();
        mr2.setElement(0, 0x61);
        mr2.setElement(1, 0x01);
        mr2.setElement(2, 0x60);
        t.sendTestMessage(mr2);

        t.sendTestMessage(mr2);

        Assert.assertEquals("enquire message sent", 2, t.outbound.size());
        Assert.assertEquals("enquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // and then send the result to the programmer
        XNetReply mr3 = new XNetReply();
        mr3.setElement(0, 0x63);
        mr3.setElement(1, 0x14);
        mr3.setElement(2, 0x08);
        mr3.setElement(3, 0x30);
        mr3.setElement(4, 0x4F);
        t.sendTestMessage(mr3);
       
        // At this point, the standard XpressNet programmer
        // should send a result to the programmer listeners, and
        // wait for either the next read/write request or for the
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.

        // failure in this test occurs with the next line.
        JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");

    }

    @Test
    @Override
    public void testWriteRegisterSequence() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the write
        p.writeCV("29", 34, l);
        // check "prog mode" message sent
        Assert.assertEquals("read message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "23 12 05 22 16", t.outbound.elementAt(0).toString());

        // The Elite send broadcast service mode entry twice
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);
        t.sendTestMessage(mr1);

        // we should not send any additional messages here.
        Assert.assertEquals("no new message", 1, t.outbound.size());

        // and then send Normal Operations Resumed twice
        XNetReply mr2 = new XNetReply();
        mr2.setElement(0, 0x61);
        mr2.setElement(1, 0x01);
        mr2.setElement(2, 0x60);
        t.sendTestMessage(mr2);

        t.sendTestMessage(mr2);

        Assert.assertEquals("enquire message sent", 2, t.outbound.size());
        Assert.assertEquals("enquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // and then send the result to the programmer
        XNetReply mr3 = new XNetReply();
        mr3.setElement(0, 0x63);
        mr3.setElement(1, 0x14);
        mr3.setElement(2, 0x1D);
        mr3.setElement(3, 0x22);
        mr3.setElement(4, 0x48);
        t.sendTestMessage(mr3);
       
        // At this point, the standard XpressNet programmer
        // should send a result to the programmer listeners, and
        // wait for either the next read/write request or for the
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.

        // failure in this test occurs with the next line.
        JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");
        Assert.assertEquals("Register mode received value", 34, l.getRcvdValue());

    }

    @Test
    @Override
    public void testReadCvSequence() throws JmriException {
        // and do the read
        p.readCV("8", l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "22 15 08 3F", t.outbound.elementAt(0).toString());

        // The Elite send broadcast service mode entry twice
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        t.sendTestMessage(mr1);

        // we should not send any additional messages here.
        Assert.assertEquals("no new message", 1, t.outbound.size());

        // and then send Normal Operations Resumed twice
        XNetReply mr2 = new XNetReply();
        mr2.setElement(0, 0x61);
        mr2.setElement(1, 0x01);
        mr2.setElement(2, 0x60);
        t.sendTestMessage(mr2);

        t.sendTestMessage(mr2);

        // and now we should send the request for results.

        Assert.assertEquals("enquire message sent", 2, t.outbound.size());
        Assert.assertEquals("enquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // and then send the result to the programmer
        XNetReply mr3 = new XNetReply();
        mr3.setElement(0, 0x63);
        mr3.setElement(1, 0x14);
        mr3.setElement(2, 0x08);
        mr3.setElement(3, 0x30);
        mr3.setElement(4, 0x4F);
        t.sendTestMessage(mr3);
       
        // At this point, the standard XpressNet programmer
        // should send a result to the programmer listeners, and
        // wait for either the next read/write request or for the
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.

        // failure in this test occurs with the next line.
        JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");
        Assert.assertEquals("Direct mode received value", 48, l.getRcvdValue());

    }

    @Test
    @Override
    public void testReadRegisterSequence() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the read
        p.readCV("29", l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "22 11 05 36", t.outbound.elementAt(0).toString());

        // The Elite send broadcast service mode entry twice
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        t.sendTestMessage(mr1);

        // we should not send any additional messages here.
        Assert.assertEquals("no new message", 1, t.outbound.size());

        // and then send Normal Operations Resumed twice
        XNetReply mr2 = new XNetReply();
        mr2.setElement(0, 0x61);
        mr2.setElement(1, 0x01);
        mr2.setElement(2, 0x60);
        t.sendTestMessage(mr2);

        t.sendTestMessage(mr2);

        // and now we should send the request for results.

        Assert.assertEquals("enquire message sent", 2, t.outbound.size());
        Assert.assertEquals("enquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // and then send the result to the programmer
        XNetReply mr3 = new XNetReply();
        mr3.setElement(0, 0x63);
        mr3.setElement(1, 0x14);
        mr3.setElement(2, 0x1D);
        mr3.setElement(3, 0x22);
        mr3.setElement(4, 0x48);
        t.sendTestMessage(mr3);
       
        // At this point, the standard Elite XnetProgrammer
        // should send a result to the programmer listeners, and
        // wait for either the next read/write request or for the
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.

        // failure in this test occurs with the next line.
        JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");
        Assert.assertEquals("Register mode received value", 34, l.getRcvdValue());

    }

    // The minimal setup is for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        t = new XNetInterfaceScaffold(new HornbyEliteCommandStation());
        l = new ProgListenerScaffold();
        programmer = p = new EliteXNetProgrammer(t);
    }

    @Override
    @After
    public void tearDown() {
	    t = null;
	    l = null;
	    programmer = p = null;
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
