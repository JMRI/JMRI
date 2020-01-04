package jmri.jmrix.roco.z21;

import jmri.JmriException;
import jmri.util.JUnitUtil;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import org.junit.*;

/**
 * Tests for the z21XNetProgrammer class
 *
 * @author	Bob Jacobsen
 */
public class Z21XNetProgrammerTest extends jmri.jmrix.lenz.XNetProgrammerTest {

    static final int RESTART_TIME = 20;

    @Override
    @Test
    public void testGetCanReadAddress() {
        Assert.assertTrue("can read address", programmer.getCanRead("1234"));
    }   
 
    @Override
    @Test
    public void testGetCanWriteAddress() {
        Assert.assertTrue("can write address", programmer.getCanWrite("1234"));
    }    

    @Override
    @Test
    public void testWriteCvSequence() throws JmriException {
        // and do the write
        p.writeCV("29", 34, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "24 12 00 1C 22 08", t.outbound.elementAt(0).toString());
        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x64);
        mr1.setElement(1, 0x14);
        mr1.setElement(2, 0x00);
        mr1.setElement(3, 0x1C);
        mr1.setElement(4, 0x22);
        mr1.setElement(5, 0x4E);
        t.sendTestMessage(mr1);

        // At this point, the z21XpressNetProgrammer
        // should send a result to the programmer listeners, and 
        // wait for either the next read/write request or for the 
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.
        JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");

        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    @Override
    @Test
    public void testReadCvSequence() throws JmriException {
        // and do the read
        p.readCV("29", l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "23 11 00 1C 2E", t.outbound.elementAt(0).toString());

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x64);
        mr1.setElement(1, 0x14);
        mr1.setElement(2, 0x00);
        mr1.setElement(3, 0x1C);
        mr1.setElement(4, 0x22);
        mr1.setElement(5, 0x4E);
        t.sendTestMessage(mr1);

        // At this point, the z21XpressNetProgrammer
        // should send a result to the programmer listeners, and 
        // wait for either the next read/write request or for the 
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.
        JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");

        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    // this test is the same as the testWriteCvSequence test, but
    // it checks the sequence for CVs greater than 256, which use 
    // different XpressNet commands.
    @Override
    @Test
    public void testWriteHighCvSequence() throws JmriException {
        // and do the write
        p.writeCV("300", 34, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "24 12 01 2B 22 3E", t.outbound.elementAt(0).toString());
        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x64);
        mr1.setElement(1, 0x14);
        mr1.setElement(2, 0x01);
        mr1.setElement(3, 0x2B);
        mr1.setElement(4, 0x22);
        mr1.setElement(5, 0x78);
        t.sendTestMessage(mr1);

        // At this point, the z21XpressNetProgrammer
        // should send a result to the programmer listeners, and 
        // wait for either the next read/write request or for the 
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.
        JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");

        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    // this test is the same as the testReadCvSequence test, but
    // it checks the sequence for CVs greater than 256, which use 
    // different XpressNet commands.
    @Override
    @Test
    public void testReadHighCvSequence() throws JmriException {
        // and do the read
        p.readCV("300", l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "23 11 01 2B 18", t.outbound.elementAt(0).toString());

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x64);
        mr1.setElement(1, 0x14);
        mr1.setElement(2, 0x01);
        mr1.setElement(3, 0x2B);
        mr1.setElement(4, 0x22);
        mr1.setElement(5, 0x78);
        t.sendTestMessage(mr1);

        // At this point, the Z21XNetProgrammer
        // should send a result to the programmer listeners, and 
        // wait for either the next read/write request or for the 
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.
        JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        t = new XNetInterfaceScaffold(new RocoZ21CommandStation());
        l = new jmri.ProgListenerScaffold();

        p = new Z21XNetProgrammer(t) {
            @Override
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };
	programmer=p;
    }

    @Override
    @After
    public void tearDown() {
	t = null;
	l = null;
	programmer=p=null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
