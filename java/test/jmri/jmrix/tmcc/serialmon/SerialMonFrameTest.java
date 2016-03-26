/**
 * SerialMonFrameTest.java
 *
 * Description:	JUnit tests
 *
 * @author	Bob Jacobsen
 * @version
 */
package jmri.jmrix.tmcc.serialmon;

import java.util.Vector;
import jmri.jmrix.tmcc.SerialMessage;
import jmri.jmrix.tmcc.SerialReply;
import jmri.jmrix.tmcc.SerialTrafficController;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialMonFrameTest extends TestCase {

    public void testCreateAndShow() {
        SerialMonFrame f = new SerialMonFrame();
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

// Following are timing-specific, occasionally fail, so commented out    
/*     public void testMsg() { */
    /*         NceMessage m = new NceMessage(3); */
    /*         m.setBinary(false); */
    /*         m.setOpCode('L'); */
    /*         m.setElement(1, '0'); */
    /*         m.setElement(2, 'A'); */
    /*          */
    /*         NceMonFrame f = new NceMonFrame(); */
    /*          */
    /*         f.message(m); */
    /*          */
    /*         Assert.assertEquals("length ", "cmd: \"L0A\"\n".length(), f.getFrameText().length()); */
    /*         Assert.assertEquals("display", "cmd: \"L0A\"\n", f.getFrameText()); */
    /*     } */
    /*      */
    /*     public void testReply() { */
    /*         NceReply m = new NceReply(); */
    /*         m.setBinary(false); */
    /*         m.setOpCode('C'); */
    /*         m.setElement(1, 'o'); */
    /*         m.setElement(2, ':'); */
    /*          */
    /*         NceMonFrame f = new NceMonFrame(); */
    /*          */
    /*         f.reply(m); */
    /*          */
    /*         Assert.assertEquals("display", "rep: \"Co:\"\n", f.getFrameText()); */
    /*         Assert.assertEquals("length ", "rep: \"Co:\"\n".length(), f.getFrameText().length()); */
    /*     } */
    public void testWrite() {

        // infrastructure objects
        SerialInterfaceScaffold t = new SerialInterfaceScaffold();
        Assert.assertNotNull("exists", t);

    }

    // service internal class to handle transmit/receive for tests
    class SerialInterfaceScaffold extends SerialTrafficController {

        public SerialInterfaceScaffold() {
        }

        // override some SerialInterfaceController methods for test purposes
        public boolean status() {
            return true;
        }

        /**
         * record messages sent, provide access for making sure they are OK
         */
        public Vector<SerialMessage> outbound = new Vector<SerialMessage>();  // public OK here, so long as this is a test class

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

        /*
         * Check number of listeners, used for testing dispose()
         */
        public int numListeners() {
            return cmdListeners.size();
        }

    }

    // from here down is testing infrastructure
    public SerialMonFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialMonFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialMonFrameTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();

        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    protected void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }
    
    private final static Logger log = LoggerFactory.getLogger(SerialMonFrameTest.class.getName());

}
