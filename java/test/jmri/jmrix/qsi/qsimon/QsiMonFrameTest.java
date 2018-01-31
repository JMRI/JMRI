/**
 * QsiMonFrameTest.java
 *
 * Description:	JUnit tests for the QsiProgrammer class
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.qsi.qsimon;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import jmri.jmrix.qsi.QsiListener;
import jmri.jmrix.qsi.QsiMessage;
import jmri.jmrix.qsi.QsiReply;
import jmri.jmrix.qsi.QsiTrafficController;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QsiMonFrameTest {

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        QsiMonFrame f = new QsiMonFrame(new jmri.jmrix.qsi.QsiSystemConnectionMemo());
        Assert.assertNotNull("exists", f);
    }

    /* Following are not reliable, apparently time-sensitive, so commented out
    @Test
    public void testMsg() {
        QsiMessage m = new QsiMessage(3);
        m.setOpCode('L');
        m.setElement(1, '0');
        m.setElement(2, 'A');

        QsiMonFrame f = new QsiMonFrame();

        f.message(m);

        Assert.assertEquals("length ", "cmd: \"L0A\"\n".length(), f.getFrameText().length());
        Assert.assertEquals("display", "cmd: \"L0A\"\n", f.getFrameText());
    }

    @Test
    public void testReply() {
        QsiReply m = new QsiReply();
        m.setOpCode('C');
        m.setElement(1, 'o');
        m.setElement(2, ':');

        QsiMonFrame f = new QsiMonFrame();

        f.reply(m);

        Assert.assertEquals("display", "rep: \"Co:\"\n", f.getFrameText());
        Assert.assertEquals("length ", "rep: \"Co:\"\n".length(), f.getFrameText().length());
    }
     */
    @Test
    public void testWrite() {

        new QsiInterfaceScaffold();
    }

    // service internal class to handle transmit/receive for tests
    class QsiInterfaceScaffold extends QsiTrafficController {

        public QsiInterfaceScaffold() {
        }

        // override some QsiInterfaceController methods for test purposes
        @Override
        public boolean status() {
            return true;
        }

        /**
         * record messages sent, provide access for making sure they are OK
         */
        public ArrayList<QsiMessage> outbound = new ArrayList<>();  // public OK here, so long as this is a test class

        @Override
        public void sendQsiMessage(QsiMessage m, QsiListener l) {
            if (log.isDebugEnabled()) {
                log.debug("sendQsiMessage [" + m + "]");
            }
            // save a copy
            outbound.add(m);
        }

        // test control member functions
        /**
         * forward a message to the listeners, e.g. test receipt
         */
        protected void sendTestMessage(QsiMessage m) {
            // forward a test message to Listeners
            if (log.isDebugEnabled()) {
                log.debug("sendTestMessage    [" + m + "]");
            }
            notifyMessage(m, null);
        }

        protected void sendTestReply(QsiReply m) {
            // forward a test message to Listeners
            if (log.isDebugEnabled()) {
                log.debug("sendTestReply    [" + m + "]");
            }
            notifyReply(m);
        }

        /*
         * Check number of listeners, used for testing dispose()
         */
        public int numListeners() {
            return cmdListeners.size();
        }

    }

    private final static Logger log = LoggerFactory.getLogger(QsiMonFrameTest.class);

}
