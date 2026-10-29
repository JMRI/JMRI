package external.marklin;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link McanTcpBridge#parseClientForSpecs(String)} argument parsing.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
class McanTcpBridgeClientForTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // -- IP only: default port 15731, attach to first bridge --

    @Test
    void parse_ipOnly_defaultPortAndNoBridgeFilter() {
        McanTcpBridge.RemoteClientSpec s = McanTcpBridge.parseClientForSpecs("192.168.178.22");
        assertEquals("192.168.178.22", s.remoteHost);
        assertEquals(15731, s.remotePort);
        assertTrue(s.bridgePorts.isEmpty());
    }

    // -- IP:remotePort: custom port, attach to first bridge --

    @Test
    void parse_ipColonPort_customPortAndNoBridgeFilter() {
        McanTcpBridge.RemoteClientSpec s = McanTcpBridge.parseClientForSpecs("192.168.178.22:50000");
        assertEquals("192.168.178.22", s.remoteHost);
        assertEquals(50000, s.remotePort);
        assertTrue(s.bridgePorts.isEmpty());
    }

    // -- IP:COM1: non-numeric second segment → default port, single bridge filter --

    @Test
    void parse_ipColonComPort_defaultRemotePortAndBridgeFilter() {
        McanTcpBridge.RemoteClientSpec s = McanTcpBridge.parseClientForSpecs("192.168.178.22:COM6");
        assertEquals("192.168.178.22", s.remoteHost);
        assertEquals(15731, s.remotePort);
        assertEquals(List.of("COM6"), s.bridgePorts);
    }

    // -- IP:remotePort:COM1,COM2,COM3 --

    @Test
    void parse_ipColonPortColonComList_customPortAndMultipleBridges() {
        McanTcpBridge.RemoteClientSpec s = McanTcpBridge.parseClientForSpecs("192.168.178.22:50000:COM1,COM2,COM3");
        assertEquals("192.168.178.22", s.remoteHost);
        assertEquals(50000, s.remotePort);
        assertEquals(List.of("COM1", "COM2", "COM3"), s.bridgePorts);
    }

    // -- IP:COM1,COM2 (comma in second segment → COM list, default remote port) --

    @Test
    void parse_ipColonCommaList_defaultRemotePortAndMultipleBridges() {
        McanTcpBridge.RemoteClientSpec s = McanTcpBridge.parseClientForSpecs("10.0.0.1:COM6,COM7");
        assertEquals("10.0.0.1", s.remoteHost);
        assertEquals(15731, s.remotePort);
        assertEquals(List.of("COM6", "COM7"), s.bridgePorts);
    }

    // -- Linux device path in COM list --

    @Test
    void parse_linuxDevicePath_recognisedAsComList() {
        McanTcpBridge.RemoteClientSpec s = McanTcpBridge.parseClientForSpecs("10.0.0.1:/dev/ttyUSB0");
        assertEquals(15731, s.remotePort);
        assertEquals(List.of("/dev/ttyUSB0"), s.bridgePorts);
    }

    // -- Invalid: empty string --

    @Test
    void parse_empty_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> McanTcpBridge.parseClientForSpecs(""));
    }

    // -- Invalid: port number out of range --

    @Test
    void parse_remotePortOutOfRange_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> McanTcpBridge.parseClientForSpecs("192.168.1.1:99999"));
    }
}
