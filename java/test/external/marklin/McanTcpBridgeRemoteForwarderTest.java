package external.marklin;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link McanTcpBridge.RemoteForwarder} reconnect delay logic and send guard.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
class McanTcpBridgeRemoteForwarderTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // Creeping delay sequence: 2s, 5s, 20s, 60s, 180s, then 300s indefinitely.

    @Test
    void reconnectDelay_firstAttempt_is2s() {
        assertEquals(2000, McanTcpBridge.RemoteForwarder.reconnectDelayMs(0));
    }

    @Test
    void reconnectDelay_secondAttempt_is5s() {
        assertEquals(5000, McanTcpBridge.RemoteForwarder.reconnectDelayMs(1));
    }

    @Test
    void reconnectDelay_thirdAttempt_is20s() {
        assertEquals(20000, McanTcpBridge.RemoteForwarder.reconnectDelayMs(2));
    }

    @Test
    void reconnectDelay_fourthAttempt_is60s() {
        assertEquals(60000, McanTcpBridge.RemoteForwarder.reconnectDelayMs(3));
    }

    @Test
    void reconnectDelay_fifthAttempt_is180s() {
        assertEquals(180000, McanTcpBridge.RemoteForwarder.reconnectDelayMs(4));
    }

    @Test
    void reconnectDelay_sixthAttempt_is300s() {
        assertEquals(300000, McanTcpBridge.RemoteForwarder.reconnectDelayMs(5));
    }

    @Test
    void reconnectDelay_largeAttemptCount_cappedAt300s() {
        assertEquals(300000, McanTcpBridge.RemoteForwarder.reconnectDelayMs(100));
    }

    // send() must not throw and must silently discard when not connected.

    @Test
    void send_whenNotConnected_doesNotThrow() {
        McanTcpBridge.RemoteClientSpec spec =
            McanTcpBridge.parseClientForSpecs("127.0.0.1:9");  // port 9 = discard, unlikely to be open
        McanTcpBridge.RemoteForwarder forwarder = new McanTcpBridge.RemoteForwarder(spec);
        byte[] frame = new byte[13];
        // Must not throw even though no connection has been established
        assertDoesNotThrow(() -> forwarder.send(frame, 13));
    }
}
