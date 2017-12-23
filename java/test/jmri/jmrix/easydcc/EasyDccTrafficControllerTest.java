package jmri.jmrix.easydcc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Vector;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import jmri.util.junit.rules.RetryRule;
import org.junit.rules.Timeout;

/**
 * JUnit tests for the EasyDccTrafficController class
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2007, 2015
 */
public class EasyDccTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(90); // 90 second timeout for methods in this test class.

    @Rule
    public RetryRule retryRule = new RetryRule(3);  // allow 3 retries

    @Test
    public void testSendThenRcvReply() throws Exception {
        EasyDccTrafficController c = new EasyDccTrafficController(new EasyDccSystemConnectionMemo("E", "EasyDCC Test")){
            @Override
            protected void terminate(){
               // do nothing, so we don't try to write to a closed pipe
               // after this test
            }
        };

        // connect to iostream via port controller
        EasyDccPortControllerScaffold p = new EasyDccPortControllerScaffold();
        c.connectPort(p);

        // object to receive reply
        EasyDccListener l = new EasyDccListenerScaffold();
        c.addEasyDccListener(l);

        // send a message
        EasyDccMessage m = new EasyDccMessage(3);
        m.setOpCode('0');
        m.setElement(1, '1');
        m.setElement(2, '2');
        c.sendEasyDccMessage(m, l);

        ostream.flush();
        JUnitUtil.waitFor(()->{return tostream.available() == 4;}, "total length");
        
		// test the result of sending

		Assert.assertEquals("total length ", 4, tostream.available());
        Assert.assertEquals("Char 0", '0', tostream.readByte());
        Assert.assertEquals("Char 1", '1', tostream.readByte());
        Assert.assertEquals("Char 2", '2', tostream.readByte());
        Assert.assertEquals("EOM", 0x0d, tostream.readByte());

        // now send reply
        tistream.write('P');
        tistream.write(0x0d);

        // threading causes the traffic controller to handle the reply,
        // so wait until that happens.
        JUnitUtil.waitFor(()->{return rcvdReply != null;}, "reply received");

        Assert.assertTrue("reply received ", rcvdReply != null);
        Assert.assertEquals("first char of reply ", 'P', rcvdReply.getOpCode());
        c.terminateThreads(); // stop any threads we might have created.
    }

    // internal class to simulate an EasyDccListener
    class EasyDccListenerScaffold implements EasyDccListener {

        public EasyDccListenerScaffold() {
            rcvdReply = null;
            rcvdMsg = null;
        }

        @Override
        public void message(EasyDccMessage m) {
            rcvdMsg = m;
        }

        @Override
        public void reply(EasyDccReply r) {
            rcvdReply = r;
        }
    }
    EasyDccReply rcvdReply;
    EasyDccMessage rcvdMsg;

    // internal class to simulate an EasyDccPortController
    class EasyDccPortControllerScaffold extends EasyDccPortController {

        @Override
        public Vector<String> getPortNames() {
            return null;
        }

        @Override
        public String openPort(String portName, String appName) {
            return null;
        }

        @Override
        public void configure() {
        }

        @Override
        public String[] validBaudRates() {
            return null;
        }

        protected EasyDccPortControllerScaffold() throws Exception {
            super(new EasyDccSystemConnectionMemo());
            PipedInputStream tempPipe;
            tempPipe = new PipedInputStream();
            tostream = new DataInputStream(tempPipe);
            ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
            tempPipe = new PipedInputStream();
            istream = new DataInputStream(tempPipe);
            tistream = new DataOutputStream(new PipedOutputStream(tempPipe));
        }

        // returns the InputStream from the port
        @Override
        public DataInputStream getInputStream() {
            return istream;
        }

        // returns the outputStream to the port
        @Override
        public DataOutputStream getOutputStream() {
            return ostream;
        }

        // check that this object is ready to operate
        @Override
        public boolean status() {
            return true;
        }
    }
    DataOutputStream ostream;  // Traffic controller writes to this
    DataInputStream tostream;  // so we can read it from this

    DataOutputStream tistream; // tests write to this
    DataInputStream istream;   // so the traffic controller can read from this

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new EasyDccTrafficController(new EasyDccSystemConnectionMemo("E", "EasyDCC Test"));
    }

    @Override
    @After
    public void tearDown() {
        if (tc!=null) {
            tc.terminateThreads();
        }
        jmri.util.JUnitUtil.tearDown();
    }

}
