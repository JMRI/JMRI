package jmri.jmrix.lenz.li100;

import jmri.JmriException;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import org.junit.*;

/**
 * LI100XNetProgrammerTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.li100.LI100XNetProgrammer class
 *
 * @author	Paul Bender
 */
public class LI100XNetProgrammerTest extends jmri.jmrix.lenz.XNetProgrammerTest {

    static final int RESTART_TIME = 20;

    @Test
    @Override
    public void testWriteCvSequence() throws JmriException {
        // and do the write
        p.writeCV("29", 34, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "23 16 1D 22 0A", t.outbound.elementAt(0).toString());
        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);
        Assert.assertEquals("inquire message sent", 2, t.outbound.size());
        Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // send a result string
        XNetReply mr2 = new XNetReply();
        mr2.setElement(0, 0x63);
        mr2.setElement(1, 0x14);
        mr2.setElement(2, 0x1D);
        mr2.setElement(3, 0x22);
        mr2.setElement(4, 0x48);
        t.sendTestMessage(mr2);

        // At this point, the LI100 XpressNet programmer
        // deviates from the standard XpressNet programmer.
        // the LI100 version requests an exit from service
        // before sending a result to the registered listeners.
        Assert.assertEquals("mode message sent", 3, t.outbound.size());
        Assert.assertEquals("exit service mode message contents", "21 81 A0", t.outbound.elementAt(2).toString());

        // send reply stating service mode has exited
        XNetReply mr3 = new XNetReply();
        mr3.setElement(0, 0x61);
        mr3.setElement(1, 0x01);
        mr3.setElement(2, 0x60);
        t.sendTestMessage(mr3);

        //failure in this test occurs with the next line.
        jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;},"Receive Called not set");
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    @Test
    @Override
    public void testWriteRegisterSequence() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the write
        p.writeCV("29", 12, l);
        // check "prog mode" message sent
        Assert.assertEquals("read message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "23 12 05 0C 38", t.outbound.elementAt(0).toString());

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("inquire message sent", 2, t.outbound.size());
        Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // send a result string
        XNetReply mr2 = new XNetReply();
        mr2.setElement(0, 0x63);
        mr2.setElement(1, 0x10);
        mr2.setElement(2, 0x05);
        mr2.setElement(3, 0x0C);
        mr2.setElement(4, 0x7A);
        t.sendTestMessage(mr2);

        // At this point, the LI100 XpressNet programmer
        // deviates from the standard XpressNet programmer.
        // the LI100 version requests an exit from service
        // before sending a result to the registered listeners.
        Assert.assertEquals("mode message sent", 3, t.outbound.size());
        Assert.assertEquals("exit service mode message contents", "21 81 A0", t.outbound.elementAt(2).toString());

        // send reply stating service mode has exited
        XNetReply mr3 = new XNetReply();
        mr3.setElement(0, 0x61);
        mr3.setElement(1, 0x01);
        mr3.setElement(2, 0x60);
        t.sendTestMessage(mr3);

        //failure in this test occurs with the next line.
        jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;},"Receive Called not set");
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);
        Assert.assertEquals("Register mode received value", 12, l.getRcvdValue());

    }

    @Test
    @Override
    public void testReadCvSequence() throws JmriException {
        // and do the read
        p.readCV("29", l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "22 15 1D 2A", t.outbound.elementAt(0).toString());

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("inquire message sent", 2, t.outbound.size());
        Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // send a result string
        XNetReply mr2 = new XNetReply();
        mr2.setElement(0, 0x63);
        mr2.setElement(1, 0x14);
        mr2.setElement(2, 0x1D);
        mr2.setElement(3, 0x22);
        mr2.setElement(4, 0x48);
        t.sendTestMessage(mr2);

        // At this point, the LI100 XpressNet programmer
        // deviates from the standard XpressNet programmer.
        // the LI100 version requests an exit from service
        // before sending a result to the registered listeners.
        Assert.assertEquals("mode message sent", 3, t.outbound.size());
        Assert.assertEquals("exit service mode message contents", "21 81 A0", t.outbound.elementAt(2).toString());

        // send reply stating service mode has exited
        XNetReply mr3 = new XNetReply();
        mr3.setElement(0, 0x61);
        mr3.setElement(1, 0x01);
        mr3.setElement(2, 0x60);
        t.sendTestMessage(mr3);

        //failure in this test occurs with the next line.
        jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;},"Receive Called not set");
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());

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
        // send reply (enter service mode )
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("inquire message sent", 2, t.outbound.size());
        Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // send a result string
        XNetReply mr2 = new XNetReply();
        mr2.setElement(0, 0x63);
        mr2.setElement(1, 0x10);
        mr2.setElement(2, 0x05);
        mr2.setElement(3, 0x22);
        mr2.setElement(4, 0x54);
        t.sendTestMessage(mr2);

        // At this point, the LI100 XpressNet programmer
        // deviates from the standard XpressNet programmer.
        // the LI100 version requests an exit from service
        // before sending a result to the registered listeners.
        //Assert.assertEquals("mode message sent", 3, t.outbound.size());
        Assert.assertEquals("exit service mode message contents", "21 81 A0", t.outbound.elementAt(2).toString());

        // send reply stating service mode has exited
        XNetReply mr3 = new XNetReply();
        mr3.setElement(0, 0x61);
        mr3.setElement(1, 0x01);
        mr3.setElement(2, 0x60);
        t.sendTestMessage(mr3);

        //failure in this test occurs with the next line.
        jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;},"Receive Called not set");
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);
        Assert.assertEquals("Register mode received value", 34, l.getRcvdValue());
    }

    // this test is the same as the testWriteCvSequence test, but
    // it checks the sequence for CVs greater than 255, which use
    // different XpressNet commands.
    @Test
    @Override
    public void testWriteHighCvSequence() throws JmriException {
        // and do the write
        p.writeCV("300", 34, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "23 1D 2C 22 30", t.outbound.elementAt(0).toString());
        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);
        Assert.assertEquals("inquire message sent", 2, t.outbound.size());
        Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // send a result string
        XNetReply mr2 = new XNetReply();
        mr2.setElement(0, 0x63);
        mr2.setElement(1, 0x15);
        mr2.setElement(2, 0x2C);
        mr2.setElement(3, 0x22);
        mr2.setElement(4, 0x78);
        t.sendTestMessage(mr2);

        // At this point, the LI100 XpressNet programmer
        // deviates from the standard XpressNet programmer.
        // the LI100 version requests an exit from service
        // before sending a result to the registered listeners.
        Assert.assertEquals("mode message sent", 3, t.outbound.size());
        Assert.assertEquals("exit service mode message contents", "21 81 A0", t.outbound.elementAt(2).toString());

        // send reply stating service mode has exited
        XNetReply mr3 = new XNetReply();
        mr3.setElement(0, 0x61);
        mr3.setElement(1, 0x01);
        mr3.setElement(2, 0x60);
        t.sendTestMessage(mr3);

        //failure in this test occurs with the next line.
        jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;},"Receive Called not set");
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    // this test is the same as the testReadCvSequence test, but
    // it checks the sequence for CVs greater than 256, which use
    // different XpressNet commands.
    @Test
    @Override
    public void testReadHighCvSequence() throws JmriException {
        // and do the read
        p.readCV("300", l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "22 19 2C 17", t.outbound.elementAt(0).toString());

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("inquire message sent", 2, t.outbound.size());
        Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // send a result string
        XNetReply mr2 = new XNetReply();
        mr2.setElement(0, 0x63);
        mr2.setElement(1, 0x15);
        mr2.setElement(2, 0x2C);
        mr2.setElement(3, 0x22);
        mr2.setElement(4, 0x78);
        t.sendTestMessage(mr2);

        // At this point, the LI100 XpressNet programmer
        // deviates from the standard XpressNet programmer.
        // the LI100 version requests an exit from service
        // before sending a result to the registered listeners.
        Assert.assertEquals("mode message sent", 3, t.outbound.size());
        Assert.assertEquals("exit service mode message contents", "21 81 A0", t.outbound.elementAt(2).toString());

        // send reply stating service mode has exited
        XNetReply mr3 = new XNetReply();
        mr3.setElement(0, 0x61);
        mr3.setElement(1, 0x01);
        mr3.setElement(2, 0x60);
        t.sendTestMessage(mr3);

        //failure in this test occurs with the next line.
        jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;},"Receive Called not set");
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());

    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        t = new XNetInterfaceScaffold(new LenzCommandStation());
        l = new jmri.ProgListenerScaffold();

        p = new LI100XNetProgrammer(t) {
            @Override
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };
	    programmer = p;
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
