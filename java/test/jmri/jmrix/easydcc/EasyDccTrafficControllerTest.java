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

/**
 * Description:	JUnit tests for the EasyDccTrafficController class
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2007, 2015
 */
public class EasyDccTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Test
    public void testSendThenRcvReply() throws Exception {
        EasyDccTrafficController c = (EasyDccTrafficController)tc;
        /*EasyDccTrafficController c = new EasyDccTrafficController() {
            // skip timeout message
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }

            public void receiveLoop() {
            }

            protected void portWarn(Exception e) {
            }
        };*/

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

        // drive the mechanism
        c.handleOneIncomingReply();

        JUnitUtil.waitFor(()->{return rcvdReply != null;}, "reply received");

        Assert.assertTrue("reply received ", rcvdReply != null);
        Assert.assertEquals("first char of reply ", 'P', rcvdReply.getOpCode());
    }

    // internal class to simulate a EasyDccListener
    class EasyDccListenerScaffold implements EasyDccListener {

        public EasyDccListenerScaffold() {
            rcvdReply = null;
            rcvdMsg = null;
        }

        public void message(EasyDccMessage m) {
            rcvdMsg = m;
        }

        public void reply(EasyDccReply r) {
            rcvdReply = r;
        }
    }
    EasyDccReply rcvdReply;
    EasyDccMessage rcvdMsg;

    // internal class to simulate a EasyDccPortController
    class EasyDccPortControllerScaffold extends EasyDccPortController {

        public Vector<String> getPortNames() {
            return null;
        }

        public String openPort(String portName, String appName) {
            return null;
        }

        public void configure() {
        }

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
        public DataInputStream getInputStream() {
            return istream;
        }

        // returns the outputStream to the port
        public DataOutputStream getOutputStream() {
            return ostream;
        }

        // check that this object is ready to operate
        public boolean status() {
            return true;
        }
    }
    DataOutputStream ostream;  // Traffic controller writes to this
    DataInputStream tostream; // so we can read it from this

    DataOutputStream tistream; // tests write to this
    DataInputStream istream;  // so the traffic controller can read from this

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        tc = new EasyDccTrafficController();
    }

    @Override
    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
