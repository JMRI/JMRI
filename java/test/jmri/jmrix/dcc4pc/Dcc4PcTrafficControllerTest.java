package jmri.jmrix.dcc4pc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Vector;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Description:	JUnit tests for the Dcc4PcTrafficController class
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2007, 2015
 */
public class Dcc4PcTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    public void testCreate() {
        Dcc4PcTrafficController m = new Dcc4PcTrafficController();
        Assert.assertNotNull("exists", m);
    }

    public void testSendThenRcvReply() throws Exception {
        Dcc4PcTrafficController c = (Dcc4PcTrafficController)tc;

        // connect to iostream via port controller
        Dcc4PcPortControllerScaffold p = new Dcc4PcPortControllerScaffold();
        c.connectPort(p);

        // object to receive reply
        Dcc4PcListener l = new Dcc4PcListenerScaffold();
        c.addDcc4PcListener(l);

        // send a message
        Dcc4PcMessage m = new Dcc4PcMessage(3);
        m.setOpCode('0');
        m.setElement(1, '1');
        m.setElement(2, '2');
        c.sendDcc4PcMessage(m, l);

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

    // internal class to simulate a Dcc4PcListener
    class Dcc4PcListenerScaffold implements Dcc4PcListener {

        public Dcc4PcListenerScaffold() {
            rcvdReply = null;
            rcvdMsg = null;
        }

        @Override
        public void message(Dcc4PcMessage m) {
            rcvdMsg = m;
        }

        @Override
        public void reply(Dcc4PcReply r) {
            rcvdReply = r;
        }

        @Override
        public void handleTimeout(Dcc4PcMessage m) {
        }

    }
    
    Dcc4PcReply rcvdReply;
    Dcc4PcMessage rcvdMsg;

    // internal class to simulate a Dcc4PcPortController
    class Dcc4PcPortControllerScaffold extends Dcc4PcPortController {

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
            return new String[] {};
        }

        //@Override
        @Override
        public int[] validBaudNumbers() {
            return new int[] {};
        }

        protected Dcc4PcPortControllerScaffold() throws Exception {
            super(new Dcc4PcSystemConnectionMemo());
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
    DataInputStream tostream; // so we can read it from this

    DataOutputStream tistream; // tests write to this
    DataInputStream istream;  // so the traffic controller can read from this

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new Dcc4PcTrafficController();
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
