package jmri.jmrix.tmcc.serialmon;

import java.awt.GraphicsEnvironment;
import java.util.Vector;
import jmri.jmrix.tmcc.SerialMessage;
import jmri.jmrix.tmcc.SerialReply;
import jmri.jmrix.tmcc.SerialTrafficController;
import jmri.jmrix.tmcc.TmccSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.tmcc.serialmon.SerialMonFrame
 * class
 *
 * @author Bob Jacobsen
 */
public class SerialMonFrameTest {

    @Test
    public void testCreateAndShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TmccSystemConnectionMemo memo = new TmccSystemConnectionMemo("T", "TMCC via Serial");
        memo.setTrafficController(new SerialTrafficController(memo));
        SerialMonFrame f = new SerialMonFrame(memo);
        // MonFrame needs a TrafficController for dispose() in line 51
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("SerialMonAction starting SerialMonFrame: Exception: " + ex.toString());
        }
        f.pack();
        f.setVisible(true);

        SerialReply m = new SerialReply();
        m.setOpCode(0xFE);
        m.setElement(1, 0x21);
        m.setElement(2, 0x43);
        f.reply(m);

        m = new SerialReply();
        m.setElement(0, 0x21);
        f.reply(m);

        f.dispose();
    }

    /*  When uncommented following cause compiler to fail
    @Ignore("timing-specific, occasionally fail, so commented out")
    @Test
    public void testMsg() {
        NceMessage m = new NceMessage(3);
        m.setBinary(false);
        m.setOpCode('L');
        m.setElement(1, '0');
        m.setElement(2, 'A');

        NceMonFrame f = new NceMonFrame();

        f.message(m);

        Assert.assertEquals("length ", "cmd: \"L0A\"\n".length(), f.getFrameText().length());
        Assert.assertEquals("display", "cmd: \"L0A\"\n", f.getFrameText());
    }

    @Ignore("timing-specific, occasionally fail, so commented out")
    @Test
    public void testReply() {
        NceReply m = new NceReply();
        m.setBinary(false);
        m.setOpCode('C');
        m.setElement(1, 'o');
        m.setElement(2, ':');

        NceMonFrame f = new NceMonFrame();

        f.reply(m);

        Assert.assertEquals("display", "rep: \"Co:\"\n", f.getFrameText());
        Assert.assertEquals("length ", "rep: \"Co:\"\n".length(), f.getFrameText().length());
    }
     */
    
    @Test
    public void testWrite() {

        // infrastructure objects
        SerialInterfaceScaffold t = new SerialInterfaceScaffold();
        Assert.assertNotNull("exists", t);

    }

    // service internal class to handle transmit/receive for tests
    class SerialInterfaceScaffold extends SerialTrafficController {

        public SerialInterfaceScaffold() {
            super(new TmccSystemConnectionMemo("T", "TMCC via Serial"));
        }

        // override some SerialInterfaceController methods for test purposes
        @Override
        public boolean status() {
            return true;
        }

        /**
         * Record messages sent, provide access for making sure they are OK.
         */
        public Vector<SerialMessage> outbound = new Vector<SerialMessage>();  // public OK here, so long as this is a test class

        @Override
        public void sendSerialMessage(SerialMessage m, jmri.jmrix.tmcc.SerialListener l) {
            if (log.isDebugEnabled()) {
                log.debug("sendMessage [" + m + "]");
            }
            // save a copy
            outbound.addElement(m);
        }

        // test control member functions

        /**
         * forward a message to the listeners, e.g. test receipt
         */
        protected void sendTestMessage(SerialMessage m) {
            // forward a test message to Listeners
            if (log.isDebugEnabled()) {
                log.debug("sendTestMessage    [" + m + "]");
            }
            notifyMessage(m, null);
            return;
        }

        protected void sendTestReply(SerialReply m) {
            // forward a test message to Listeners
            if (log.isDebugEnabled()) {
                log.debug("sendTestReply    [" + m + "]");
            }
            notifyReply(m, null);
            return;
        }

        /**
         * Check number of listeners, used for testing dispose()
         */
        public int numListeners() {
            return cmdListeners.size();
        }

    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    private final static Logger log = LoggerFactory.getLogger(SerialMonFrameTest.class);

}
