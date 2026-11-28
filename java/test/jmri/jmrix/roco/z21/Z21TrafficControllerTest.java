package jmri.jmrix.roco.z21;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.roco.z21.z21TrafficController class
 *
 * @author Paul Bender
 */
public class Z21TrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Test
    public void testRepliesInSplitsAPacket() {
        byte[] packet = {0x04, 0x00, (byte) 0x88, 0x00, 0x05, 0x00, 0x40, 0x00, 0x61, 0x00, 0x00};
        List<Z21Reply> replies = Z21TrafficController.repliesIn(packet, 9);
        assertEquals(2, replies.size());
        assertEquals("04 00 88 00", replies.get(0).toString());
        assertEquals("05 00 40 00 61", replies.get(1).toString());
    }

    @Test
    public void testRepliesInStopsAtALengthTooShort() {
        // a length of 0 would never move on through the packet
        byte[] packet = {0x04, 0x00, (byte) 0x88, 0x00, 0x00, 0x00, 0x40, 0x00};
        List<Z21Reply> replies = Z21TrafficController.repliesIn(packet, 8);
        assertEquals(1, replies.size());
        JUnitAppender.assertWarnMessageStartsWith("Ignoring the rest of a Z21 packet: message length 0");
    }

    @Test
    public void testRepliesInStopsAtALengthPastTheEnd() {
        byte[] packet = {0x04, 0x00, (byte) 0x88, 0x00, 0x10, 0x00, 0x40, 0x00};
        List<Z21Reply> replies = Z21TrafficController.repliesIn(packet, 8);
        assertEquals(1, replies.size());
        JUnitAppender.assertWarnMessageStartsWith("Ignoring the rest of a Z21 packet: message length 16");
    }

    @Test
    public void testReceivesCombinedPacketLongerThan100Bytes() throws Exception {
        Z21TrafficController controller = (Z21TrafficController) tc;
        List<Z21Reply> replies = new ArrayList<>();

        controller.addz21Listener(new Z21Listener() {
            @Override
            public void reply(Z21Reply msg) {
                replies.add(msg);
            }

            @Override
            public void message(Z21Message msg) {
            }
        });

        InetAddress loopback = InetAddress.getLoopbackAddress();

        try (DatagramSocket receiveSocket = new DatagramSocket(0, loopback);
                DatagramSocket sendSocket = new DatagramSocket()) {
            receiveSocket.setSoTimeout(5000);
            controller.controller = new TestZ21Adapter(receiveSocket);
            setControllerEndpoint(controller, loopback, receiveSocket.getLocalPort());

            byte[] combinedReply = createCombinedReply(26);
            sendSocket.send(new DatagramPacket(combinedReply, combinedReply.length,
                    loopback, receiveSocket.getLocalPort()));

            controller.handleOneIncomingReply();
        } finally {
            controller.controller = null;
        }

        assertEquals(26, replies.size());
        assertEquals("04 00 88 00", replies.get(0).toString());
        assertEquals("04 00 88 00", replies.get(replies.size() - 1).toString());
    }

    @Test
    public void testStoppedWhileWaitingToRetryIsNotAnError() throws InterruptedException {
        Z21TrafficController controller = (Z21TrafficController) tc;
        // with no port the message can't be sent, so the sender waits to retry it
        Z21Message m = Z21Message.getSerialNumberRequestMessage();
        m.setTimeout(60000);
        m.setRetries(1);
        Thread sender = new Thread(() -> controller.forwardToPort(m, null), "Z21 test sender");
        sender.start();
        JUnitUtil.waitFor(() -> sender.getState() == Thread.State.TIMED_WAITING, "waiting to retry");

        // stopped as a connection is when closed
        controller.terminateThreads();
        sender.interrupt();
        sender.join(5000);
        Assertions.assertFalse(sender.isAlive(), "the sender gave up");
        // nothing is logged as an error: the thread is ending, as asked (checked as the test ends)
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new Z21TrafficController();
    }

    @Override
    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
    }

    private static byte[] createCombinedReply(int replyCount) {
        byte[] combinedReply = new byte[replyCount * 4];
        for (int offset = 0; offset < combinedReply.length; offset += 4) {
            combinedReply[offset] = 0x04;
            combinedReply[offset + 1] = 0x00;
            combinedReply[offset + 2] = (byte) 0x88;
            combinedReply[offset + 3] = 0x00;
        }
        return combinedReply;
    }

    private static void setControllerEndpoint(Z21TrafficController controller,
            InetAddress host, int port) throws NoSuchFieldException, IllegalAccessException {
        setControllerField(controller, "host", host);
        setControllerField(controller, "port", port);
    }

    private static void setControllerField(Z21TrafficController controller, String fieldName,
            Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = Z21TrafficController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    private static class TestZ21Adapter extends Z21Adapter {

        private final DatagramSocket socket;

        TestZ21Adapter(DatagramSocket socket) {
            this.socket = socket;
        }

        @Override
        public DatagramSocket getSocket() {
            return socket;
        }
    }
}
