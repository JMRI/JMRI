package jmri.jmrix.easydcc.easydccmon;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import jmri.jmrix.easydcc.EasyDccListener;
import jmri.jmrix.easydcc.EasyDccMessage;
import jmri.jmrix.easydcc.EasyDccReply;
import jmri.jmrix.easydcc.EasyDccTrafficController;
import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit tests for the EasyDccMonFrame class
 *
 * @author	Bob Jacobsen
 */
public class EasyDccMonFrameTest {

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EasyDccMonFrame f = new EasyDccMonFrame(new EasyDccSystemConnectionMemo("E", "EasyDCC via Serial"));
        Assert.assertNotNull("exists", f);
    }

    // Following are not reliable, apparently time-sensitive, so commented out
    @Ignore
    public void testMsg() {
        EasyDccMessage m = new EasyDccMessage(3);
        m.setOpCode('L');
        m.setElement(1, '0');
        m.setElement(2, 'A');

        EasyDccMonFrame f = new EasyDccMonFrame(new EasyDccSystemConnectionMemo("E", "EasyDCC via Serial"));

        f.message(m);

        Assert.assertEquals("length ", "cmd: \"L0A\"\n".length(), f.getFrameText().length());
        Assert.assertEquals("display", "cmd: \"L0A\"\n", f.getFrameText());
    }

    @Ignore
    public void testReply() {
        EasyDccReply m = new EasyDccReply();
        m.setOpCode('C');
        m.setElement(1, 'o');
        m.setElement(2, ':');

        EasyDccMonFrame f = new EasyDccMonFrame(new EasyDccSystemConnectionMemo("E", "EasyDCC via Serial"));

        f.reply(m);

        Assert.assertEquals("display", "rep: \"Co:\"\n", f.getFrameText());
        Assert.assertEquals("length ", "rep: \"Co:\"\n".length(), f.getFrameText().length());
    }

    @Test
    @Ignore("Test currently just creates a test scaffold and verifies it exists")
    public void testWrite() {

        // infrastructure objects
        EasyDccInterfaceScaffold t = new EasyDccInterfaceScaffold();
        Assert.assertNotNull("exists", t);

    }

    // service internal class to handle transmit/receive for tests
    class EasyDccInterfaceScaffold extends EasyDccTrafficController {

        public EasyDccInterfaceScaffold() {
            super(new EasyDccSystemConnectionMemo("E", "EasyDCC via Serial"));
        }

        // override some EasyDccInterfaceController methods for test purposes
        @Override
        public boolean status() {
            return true;
        }

        /**
         * record messages sent, provide access for making sure they are OK
         */
        public ArrayList<EasyDccMessage> outbound = new ArrayList<>();  // public OK here, so long as this is a test class

        @Override
        public void sendEasyDccMessage(EasyDccMessage m, EasyDccListener l) {
            if (log.isDebugEnabled()) {
                log.debug("sendEasyDccMessage [" + m + "]");
            }
            // save a copy
            outbound.add(m);
        }

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
        }

        protected void sendTestReply(EasyDccReply m) {
            // forward a test message to Listeners
            if (log.isDebugEnabled()) {
                log.debug("sendTestReply    [" + m + "]");
            }
            notifyReply(m, null);
        }

        /*
         * Check number of listeners, used for testing dispose()
         */
        public int numListeners() {
            return cmdListeners.size();
        }

    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccMonFrameTest.class);

}
