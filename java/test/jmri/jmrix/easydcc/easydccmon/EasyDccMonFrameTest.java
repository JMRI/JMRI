/**
 * EasyDccMonFrameTest.java
 *
 * Description:	JUnit tests for the EasyDccProgrammer class
 *
 * @author	Bob Jacobsen
 * @version
 */
package jmri.jmrix.easydcc.easydccmon;

import java.util.Vector;
import jmri.jmrix.easydcc.EasyDccListener;
import jmri.jmrix.easydcc.EasyDccMessage;
import jmri.jmrix.easydcc.EasyDccReply;
import jmri.jmrix.easydcc.EasyDccTrafficController;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyDccMonFrameTest extends TestCase {

    public void testCreate() {
        EasyDccMonFrame f = new EasyDccMonFrame();
        Assert.assertNotNull("exists", f);
    }

// Following are not reliable, apparently time-sensitive, so commented out
/* 	public void testMsg() { */
    /* 		EasyDccMessage m = new EasyDccMessage(3); */
    /* 		m.setOpCode('L'); */
    /* 		m.setElement(1, '0'); */
    /* 		m.setElement(2, 'A'); */
    /*  */
    /* 		EasyDccMonFrame f = new EasyDccMonFrame(); */
    /*  */
    /* 		f.message(m); */
    /*  */
    /* 		Assert.assertEquals("length ", "cmd: \"L0A\"\n".length(), f.getFrameText().length()); */
    /* 		Assert.assertEquals("display", "cmd: \"L0A\"\n", f.getFrameText()); */
    /* 	} */
    /*  */
    /* 	public void testReply() { */
    /* 		EasyDccReply m = new EasyDccReply(); */
    /* 		m.setOpCode('C'); */
    /* 		m.setElement(1, 'o'); */
    /* 		m.setElement(2, ':'); */
    /*  */
    /* 		EasyDccMonFrame f = new EasyDccMonFrame(); */
    /*  */
    /* 		f.reply(m); */
    /*  */
    /* 		Assert.assertEquals("display", "rep: \"Co:\"\n", f.getFrameText()); */
    /* 		Assert.assertEquals("length ", "rep: \"Co:\"\n".length(), f.getFrameText().length()); */
    /* 	} */
    public void testWrite() {

        // infrastructure objects
        EasyDccInterfaceScaffold t = new EasyDccInterfaceScaffold();
        Assert.assertNotNull("exists", t);

    }

    // service internal class to handle transmit/receive for tests
    class EasyDccInterfaceScaffold extends EasyDccTrafficController {

        public EasyDccInterfaceScaffold() {
        }

        // override some EasyDccInterfaceController methods for test purposes
        public boolean status() {
            return true;
        }

        /**
         * record messages sent, provide access for making sure they are OK
         */
        public Vector<EasyDccMessage> outbound = new Vector<EasyDccMessage>();  // public OK here, so long as this is a test class

        public void sendEasyDccMessage(EasyDccMessage m, EasyDccListener l) {
            if (log.isDebugEnabled()) {
                log.debug("sendEasyDccMessage [" + m + "]");
            }
            // save a copy
            outbound.addElement(m);
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
            return;
        }

        protected void sendTestReply(EasyDccReply m) {
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
    public EasyDccMonFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {EasyDccMonFrameTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EasyDccMonFrameTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccMonFrameTest.class.getName());

}
