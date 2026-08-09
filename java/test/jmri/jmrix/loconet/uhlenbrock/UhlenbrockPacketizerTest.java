package jmri.jmrix.loconet.uhlenbrock;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.atomic.AtomicReference;

import jmri.jmrix.loconet.LnPortController;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class UhlenbrockPacketizerTest {

    private UhlenbrockSystemConnectionMemo memo;
    private TestUhlenbrockPacketizer packetizer;
    private PipedOutputStream inboundWriter;
    private ByteArrayOutputStream outboundStream;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists", packetizer);
    }

    @Test
    public void testMatchingEchoReleasesNextMessage() throws IOException {
        LocoNetMessage first = new LocoNetMessage(new int[]{0xB0, 0x01, 0x02, 0x00});
        LocoNetMessage second = new LocoNetMessage(new int[]{0xB1, 0x03, 0x04, 0x00});

        packetizer.sendLocoNetMessage(first);
        packetizer.sendLocoNetMessage(second);
        waitForTransmittedBytes(first.getNumDataElements());

        writeInbound(first);

        waitForTransmittedBytes(first.getNumDataElements() + second.getNumDataElements());

        JUnitAppender.suppressErrorMessage("transmitLoop interrupted");     // This error is sometimes logged in CI test on GitHub
    }

    @Test
    public void testEchoDuringWriteReleasesNextMessage() {
        LocoNetMessage first = new LocoNetMessage(new int[]{0xB0, 0x01, 0x02, 0x00});
        LocoNetMessage second = new LocoNetMessage(new int[]{0xB1, 0x03, 0x04, 0x00});
        packetizer.ostream = new EchoingOutputStream();

        packetizer.sendLocoNetMessage(first);
        packetizer.sendLocoNetMessage(second);

        waitForTransmittedBytes(first.getNumDataElements() + second.getNumDataElements());

        JUnitAppender.suppressErrorMessage("transmitLoop interrupted");     // This error is sometimes logged in CI test on GitHub
    }

    @Test
    public void testNonMatchingMessageDoesNotReleaseNextMessage() throws IOException {
        LocoNetMessage first = new LocoNetMessage(new int[]{0xB0, 0x01, 0x02, 0x00});
        LocoNetMessage second = new LocoNetMessage(new int[]{0xB1, 0x03, 0x04, 0x00});
        LocoNetMessage inbound = new LocoNetMessage(new int[]{0xB2, 0x05, 0x06, 0x00});
        inbound.setParity();
        AtomicReference<LocoNetMessage> received = new AtomicReference<>();
        packetizer.addLocoNetListener(~0, received::set);

        packetizer.sendLocoNetMessage(first);
        packetizer.sendLocoNetMessage(second);
        waitForTransmittedBytes(first.getNumDataElements());

        writeInbound(inbound);

        JUnitUtil.waitFor(() -> received.get() != null, "non-matching message delivered");
        Assert.assertEquals("inbound message", inbound, received.get());
        Assert.assertEquals("second message remains queued", first.getNumDataElements(), outboundStream.size());
    }

    @Test
    public void testTerminateThreadsWhileWaitingForEcho() {
        LocoNetMessage message = new LocoNetMessage(new int[]{0xB0, 0x01, 0x02, 0x00});
        packetizer.sendLocoNetMessage(message);
        waitForTransmittedBytes(message.getNumDataElements());

        stopPacketizer();

        Assert.assertTrue("packetizer threads terminated", packetizer.threadsStopped());
    }

    private void waitForTransmittedBytes(int expected) {
        JUnitUtil.waitFor(() -> outboundStream.size() >= expected,
                () -> "expected " + expected + " transmitted bytes, found " + outboundStream.size());
    }

    private void writeInbound(LocoNetMessage message) throws IOException {
        for (int i = 0; i < message.getNumDataElements(); i++) {
            inboundWriter.write(message.getElement(i));
        }
        inboundWriter.flush();
    }

    private void stopPacketizer() {
        if (packetizer == null) {
            return;
        }
        try {
            inboundWriter.close();
        } catch (IOException ex) {
            // Already closed during cleanup.
        }
        packetizer.releaseTransmitWait();
        packetizer.terminateThreads();
        packetizer.waitForThreadsToStop();
    }

    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.WAITFOR_MAX_DELAY = 2000;
        memo = new UhlenbrockSystemConnectionMemo();
        packetizer = new TestUhlenbrockPacketizer(memo);
        PipedInputStream inboundStream = new TestPipedInputStream();
        inboundWriter = new PipedOutputStream(inboundStream);
        outboundStream = new ByteArrayOutputStream();
        packetizer.connectPort(new TestPortController(memo,
                new DataInputStream(inboundStream), new DataOutputStream(outboundStream)));
        packetizer.startThreads();
    }

    @AfterEach
    public void tearDown() {
        stopPacketizer();
        memo.dispose();
        packetizer = null;
        memo = null;
        JUnitUtil.tearDown();
    }

    private static class TestPortController extends LnPortController {

        private final DataInputStream inputStream;
        private final DataOutputStream outputStream;

        TestPortController(UhlenbrockSystemConnectionMemo memo,
                DataInputStream inputStream, DataOutputStream outputStream) {
            super(memo);
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        @Override
        public boolean status() {
            return true;
        }

        @Override
        public void configure() {
        }

        @Override
        public DataInputStream getInputStream() {
            return inputStream;
        }

        @Override
        public DataOutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public String[] validBaudRates() {
            return new String[]{"9600"};
        }

        @Override
        public String openPort(String portName, String appName) {
            return "";
        }
    }

    private static class TestUhlenbrockPacketizer extends UhlenbrockPacketizer {

        TestUhlenbrockPacketizer(UhlenbrockSystemConnectionMemo memo) {
            super(memo);
        }

        void releaseTransmitWait() {
            synchronized (xmtHandler) {
                mCurrentState = NOTIFIEDSTATE;
                xmtHandler.notify();
            }
        }

        void waitForThreadsToStop() {
            JUnitUtil.waitThreadTerminated(xmtThread);
            JUnitUtil.waitThreadTerminated(rcvThread);
        }

        boolean threadsStopped() {
            return !xmtThread.isAlive() && !rcvThread.isAlive();
        }

        boolean echoReceived() {
            return mCurrentState == NOTIFIEDSTATE;
        }
    }

    private static class TestPipedInputStream extends PipedInputStream {

        @Override
        public synchronized int read(byte[] bytes, int offset, int length) throws IOException {
            int result = super.read(bytes, offset, length);
            if (result < 0) {
                throw new IOException("Input stream closed");
            }
            return result;
        }
    }

    private class EchoingOutputStream extends OutputStream {

        private boolean echoed;

        @Override
        public void write(int value) throws IOException {
            outboundStream.write(value);
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            outboundStream.write(bytes, offset, length);
            if (!echoed) {
                echoed = true;
                inboundWriter.write(bytes, offset, length);
                inboundWriter.flush();
                JUnitUtil.waitFor(packetizer::echoReceived, "echo received before write returns");
            }
        }
    }

    // private static final Logger log = LoggerFactory.getLogger(UhlenbrockPacketizerTest.class);

}
