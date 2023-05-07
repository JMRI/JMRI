package jmri.jmrix.easydcc.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Vector;

import jmri.util.JUnitUtil;
import jmri.jmrix.easydcc.EasyDccMessage;
import jmri.jmrix.easydcc.EasyDccListenerScaffold;
import jmri.jmrix.easydcc.EasyDccPortController;
import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the EasyDccSimulatorTrafficController class
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2007, 2015
 */
@Timeout(90)
public class EasyDccSimulatorTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Test
    public void testSendThenRcvReply() throws Exception {
        EasyDccSimulatorTrafficController c = new EasyDccSimulatorTrafficController(new EasyDccSystemConnectionMemo("E", "EasyDCC Test")){
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
        EasyDccListenerScaffold l = new EasyDccListenerScaffold();
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
        Assert.assertEquals("Char 0", (byte)'0', tostream.readByte());
        Assert.assertEquals("Char 1", (byte)'1', tostream.readByte());
        Assert.assertEquals("Char 2", (byte)'2', tostream.readByte());
        Assert.assertEquals("EOM", 0x0d, tostream.readByte());

        // now send reply
        tistream.write('P');
        tistream.write(0x0d);

        // threading causes the traffic controller to handle the reply,
        // so wait until that happens.
        JUnitUtil.waitFor(()->{return l.rcvdReply != null;}, "reply received");

        Assert.assertNotNull("reply received ", l.rcvdReply );
        Assert.assertEquals("first char of reply ", 'P', l.rcvdReply.getOpCode());
        c.terminateThreads(); // stop any threads we might have created.
    }

    // internal class to simulate an EasyDccPortController
    private class EasyDccPortControllerScaffold extends EasyDccPortController {

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
    private DataInputStream tostream;  // so we can read it from this

    private DataOutputStream tistream; // tests write to this
    private DataInputStream istream;   // so the traffic controller can read from this

    private EasyDccSystemConnectionMemo memo;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new EasyDccSystemConnectionMemo("E", "EasyDCC Test");
        tc = new EasyDccSimulatorTrafficController(memo);
    }

    @Override
    @AfterEach
    public void tearDown() {
        if (tc!=null) {
            tc.terminateThreads();
        }
        if ( memo != null ) {
            memo.dispose();
            memo = null;
        }
        JUnitUtil.tearDown();
    }

}
