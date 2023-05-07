package jmri.jmrix.dcc4pc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Vector;

import jmri.jmrix.dcc4pc.serialdriver.SerialDriverAdapter;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the Dcc4PcTrafficController class
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2007, 2015
 */
public class Dcc4PcTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Test
    public void testCreate() {
        Assert.assertNotNull("exists", tc);
    }

    @Test
    @Disabled("Requires further setup")
    public void testSendThenRcvReply() throws java.io.IOException {
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
        JUnitUtil.waitFor(() -> tostream.available() == 4, "total length");
        
        // test the result of sending
        Assert.assertEquals("total length ", 4, tostream.available());
        Assert.assertEquals("Char 0", (byte)'0', tostream.readByte());
        Assert.assertEquals("Char 1", (byte)'1', tostream.readByte());
        Assert.assertEquals("Char 2", (byte)'2', tostream.readByte());
        Assert.assertEquals("EOM", 0x0d, tostream.readByte());


        // now send reply
        tistream.write('P');
        tistream.write(0x0d);

        // drive the mechanism
        c.handleOneIncomingReply();

        JUnitUtil.waitFor(()->{return rcvdReply != null;}, "reply received");

        Assert.assertTrue("reply received ", rcvdReply != null);
        Assert.assertEquals("first char of reply ", 'P', rcvdReply.getOpCode());

        c.terminateThreads();
        p.getSystemConnectionMemo().dispose();

    }

    @Test
    public void testListenerScaffold() {
        
        Dcc4PcListenerScaffold l = new Dcc4PcListenerScaffold();
        Dcc4PcMessage m = Dcc4PcMessage.getInfo();
        l.message(m);
        Assertions.assertTrue( m == rcvdMsg );
        
        Dcc4PcReply reply = new Dcc4PcReply(new byte[]{0x01,0x02,0x03});
        l.reply(reply);
        Assertions.assertEquals(reply, rcvdReply);

    }
    
    // internal class to simulate a Dcc4PcListener
    private class Dcc4PcListenerScaffold implements Dcc4PcListener {

        Dcc4PcListenerScaffold() {
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
    private class Dcc4PcPortControllerScaffold extends SerialDriverAdapter {

        Dcc4PcPortControllerScaffold() throws java.io.IOException {
            super();
            // super(new Dcc4PcSystemConnectionMemo());
            PipedInputStream tempPipe;
            tempPipe = new PipedInputStream();
            tostream = new DataInputStream(tempPipe);
            ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
            tempPipe = new PipedInputStream();
            istream = new DataInputStream(tempPipe);
            tistream = new DataOutputStream(new PipedOutputStream(tempPipe));
        }

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

    private DataOutputStream ostream;  // Traffic controller writes to this
    private DataInputStream tostream; // so we can read it from this

    private DataOutputStream tistream; // tests write to this
    private DataInputStream istream;  // so the traffic controller can read from this

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new Dcc4PcTrafficController();
    }

    @Override
    @AfterEach
    public void tearDown() {
        if ( tc != null ) {
            tc.terminateThreads();
            tc = null;
        }
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
