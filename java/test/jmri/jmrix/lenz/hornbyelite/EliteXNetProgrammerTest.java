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
import org.junit.*;

public class EliteXNetProgrammerTest extends jmri.jmrix.lenz.XNetProgrammerTest {

    @Override
    @Test
    public void testWriteCvSequence() throws JmriException {
        // and do the write
        p.writeCV(10, 20, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 2, t.outbound.size());
        Assert.assertEquals("write message contents", "23 16 0A 14 2B", t.outbound.elementAt(0).toString());
        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("enquire message sent", 2, t.outbound.size());
        Assert.assertEquals("enquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

    }

    @Test
    @Override
    @Ignore("Elite behavior is unknown for this sequence")
    public void testWriteHighCvSequence() throws JmriException {
    }

    @Test
    @Override
    @Ignore("Elite behavior is unknown for this sequence")
    public void testReadHighCvSequence() throws JmriException {
    }

    // Test names ending with "String" are for the new writeCV(String, ...)
    // etc methods.  If you remove the older writeCV(int, ...) tests,
    // you can rename these. Note that not all (int,...) tests may have a
    // String(String, ...) test defined, in which case you should create those.
    @Test
    public void testWriteCvSequenceString() throws JmriException {
        // and do the write
        p.writeCV("10", 20, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 2, t.outbound.size());
        Assert.assertEquals("write message contents", "23 16 0A 14 2B", t.outbound.elementAt(0).toString());
        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("enquire message sent", 2, t.outbound.size());
        Assert.assertEquals("enquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

    }

    @Test
    public void testWriteRegisterSequence() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the write
        p.writeCV(29, 12, l);
        // check "prog mode" message sent
        Assert.assertEquals("read message sent", 2, t.outbound.size());
        Assert.assertEquals("write message contents", "23 12 05 0C 38", t.outbound.elementAt(0).toString());

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("enquire message sent", 2, t.outbound.size());
        Assert.assertEquals("enquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

    }

    @Test
    public void testWriteRegisterSequenceString() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the write
        p.writeCV("29", 12, l);
        // check "prog mode" message sent
        Assert.assertEquals("read message sent", 2, t.outbound.size());
        Assert.assertEquals("write message contents", "23 12 05 0C 38", t.outbound.elementAt(0).toString());

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("enquire message sent", 2, t.outbound.size());
        Assert.assertEquals("enquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

    }

    @Test
    public void testReadCvSequence() throws JmriException {
        // and do the read
        p.readCV(10, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 2, t.outbound.size());
        Assert.assertEquals("read message contents", "22 15 0A 3D", t.outbound.elementAt(0).toString());

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("enquire message sent", 2, t.outbound.size());
        Assert.assertEquals("enquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

    }

    @Test
    public void testReadCvSequenceString() throws JmriException {
        // and do the read
        p.readCV("10", l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 2, t.outbound.size());
        Assert.assertEquals("read message contents", "22 15 0A 3D", t.outbound.elementAt(0).toString());

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("enquire message sent", 2, t.outbound.size());
        Assert.assertEquals("enquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

    }

    @Test
    public void testReadRegisterSequence() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the read
        p.readCV(29, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 2, t.outbound.size());
        Assert.assertEquals("read message contents", "22 11 05 36", t.outbound.elementAt(0).toString());
        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("enquire message sent", 2, t.outbound.size());
        Assert.assertEquals("enquire message contents", "21 10 31", t.outbound.elementAt(1).toString());
    }

    @Test
    public void testReadRegisterSequenceString() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the read
        p.readCV("29", l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 2, t.outbound.size());
        Assert.assertEquals("read message contents", "22 11 05 36", t.outbound.elementAt(0).toString());
        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("enquire message sent", 2, t.outbound.size());
        Assert.assertEquals("enquire message contents", "21 10 31", t.outbound.elementAt(1).toString());
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
        JUnitUtil.tearDown();
    }

}
