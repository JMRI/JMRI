/**
 * EasyDccConsistManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.nce.EasyDccConsistManager class
 *
 * @author	Paul Bender
 * @version
 */
package jmri.jmrix.easydcc;

import java.util.Vector;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyDccConsistManagerTest extends TestCase {

    public void testCtor() {
        EasyDccConsistManager m = new EasyDccConsistManager();
        Assert.assertNotNull(m);
    }

    // test the initilization loop
    public void testInitSequence() {
        EasyDccInterfaceScaffold t = new EasyDccInterfaceScaffold();
        new EasyDccListenerScaffold();
        EasyDccConsistManager m = new EasyDccConsistManager();
        // we need to call requestUpdateFromLayout() to trigger the 
        // init sequence.
        m.requestUpdateFromLayout();

        for (int i = 1; i < 255; i++) {
            // check "display consist" message sent
            Assert.assertEquals("display consist", i, t.outbound.size());
            if (i < 16) {
                Assert.assertEquals("read message contents", "GD 0" + Integer.toHexString(i).toUpperCase(),
                        ((t.outbound.elementAt(i - 1))).toString());
            } else {
                Assert.assertEquals("read message contents", "GD " + Integer.toHexString(i).toUpperCase(),
                        ((t.outbound.elementAt(i - 1))).toString());
            }
            // reply from programmer arrives
            EasyDccReply r = new EasyDccReply();

            r.setElement(0, 'G');
            r.setElement(1, i < 16 ? '0' : Integer.toHexString(i).charAt(0)); // first hex digit of i
            r.setElement(2, i < 16 ? Integer.toHexString(i).charAt(0) : Integer.toHexString(i).charAt(1)); // second hex digit of i
            if (i == 80) {
                // For consist 80, use data from real hardware
                // provided by Rick Beaber. PAB
                r.setElement(1, '5');
                r.setElement(2, '0');
                r.setElement(3, '0');
                r.setElement(4, '0');
                r.setElement(5, '5');
                r.setElement(6, '0');
                r.setElement(7, '0');
                r.setElement(8, '1');
                r.setElement(9, '1');
                r.setElement(10, '8');
                r.setElement(11, '8');
                r.setElement(12, '1');
                r.setElement(13, '2');
                r.setElement(14, '1');
                r.setElement(15, 0x0D);
            } else if (i < 254) {
                // for the rest of the first 254 consists, reply with 
                // an empty consist
                r.setElement(3, ' ');
                r.setElement(4, '0');
                r.setElement(5, '0');
                r.setElement(6, '0');
                r.setElement(7, '0');
                r.setElement(8, 0x0D);
            } else {
                // for the last consist, reply with a non-empty consist.
                // the data here is from the EasyDCC manual.
                r.setElement(3, ' ');
                r.setElement(4, '0');
                r.setElement(5, '5');
                r.setElement(6, '0');
                r.setElement(7, 'F');
                r.setElement(8, ' ');
                r.setElement(9, '0');
                r.setElement(10, '7');
                r.setElement(11, '3');
                r.setElement(12, '6');
                r.setElement(13, ' ');
                r.setElement(14, '1');
                r.setElement(15, '9');
                r.setElement(16, '4');
                r.setElement(17, 'A');
                r.setElement(18, 0x0D);
            }
            t.sendTestReply(r);

        }
        // check and make sure the last consist was created
        EasyDccConsist c = (EasyDccConsist) m.getConsist(new jmri.DccLocoAddress(255, true));
        Assert.assertNotNull(c);

    }

    // from here down is testing infrastructure
    public EasyDccConsistManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EasyDccConsistManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EasyDccConsistManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
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

        public void sendEasyDccMessage(EasyDccMessage m, jmri.jmrix.easydcc.EasyDccListener l) {
            if (log.isDebugEnabled()) {
                log.debug("sendEasyDccMessage [" + m + "]");
            }
            // save a copy
            outbound.addElement(m);
            lastSender = l;
        }

        jmri.jmrix.easydcc.EasyDccListener lastSender;
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
            notifyReply(m, lastSender);
            return;
        }

        /*
         * Check number of listeners, used for testing dispose()
         */
        public int numListeners() {
            return cmdListeners.size();
        }

    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccConsistManagerTest.class.getName());

}
