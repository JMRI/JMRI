package jmri.jmrix.roco.z21;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.roco.z21.z21TrafficController class
 *
 * @author Paul Bender
 */
public class Z21TrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Test
    public void testReceivesCombinedPacketLongerThan100Bytes() throws Exception {
        TestTrafficController controller = (TestTrafficController) tc;
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

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new TestTrafficController();
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

    private static class TestTrafficController extends Z21TrafficController {

        TestTrafficController() {
            xmtRunnable = () -> {
            };
        }
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
