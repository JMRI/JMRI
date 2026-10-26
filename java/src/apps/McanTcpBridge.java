package apps;

import com.fazecast.jSerialComm.SerialPort;
import jmri.jmrix.marklin.MarklinMessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Standalone CLI program that bridges MCAN (Märklin CAN) serial adapters to TCP/IP.
 * <p>
 * This bridge creates a TCP multiplexer for MCAN protocol devices, allowing multiple JMRI
 * instances and other software to connect simultaneously to serial MCAN adapters
 * (such as CC-Schnitte by CanDigitalBahn) over TCP/IP using the CS2 Ethernet protocol.
 * <p>
 * Supports:
 * - Single or multiple serial devices simultaneously
 * - Custom TCP port per device
 * - Auto-increment TCP ports for multiple devices
 * - Message queuing to prevent command collisions from multiple clients
 * - Specific IP binding for security
 * <p>
 * Usage:
 * <pre>
 * # Single device
 * java -jar mcan-tcp-bridge.jar --port COM3
 * java -jar mcan-tcp-bridge.jar --port COM3:50000
 *
 * # Multiple devices
 * java -jar mcan-tcp-bridge.jar --port COM2,COM3:49999,COM4:50000
 * # COM2 gets default port 15731
 * # COM3 gets explicit port 49999
 * # COM4 gets explicit port 50000
 * </pre>
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de) Copyright (C) 2026
 * @see <a href="https://www.maerklin.de/fileadmin/media/produkte/CS2_can-protokoll_1-0.pdf">CS2 CAN Protocol</a>
 */
public class McanTcpBridge {

    private static final Logger log = LoggerFactory.getLogger(McanTcpBridge.class);

    // Default configuration
    private static final int DEFAULT_TCP_PORT = 15731; // Standard Märklin CS2 TCP port
    private static final int DEFAULT_BAUD_RATE = 500000; // CC-Schnitte (third-party) uses 500k baud
    private static final Pattern PORT_PATTERN = Pattern.compile("^([^:]+)(?::([^:]+))?(?::([0-9]+))?$");

    private static final List<BridgeInstance> bridges = new ArrayList<>();
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static boolean monitorMode = false; // Message logging to stdout

    /**
     * Device specification: serial port name + optional bind address + TCP port.
     */
    static class DeviceSpec {
        final String serialPort;
        final String bindAddress; // null means bind to all interfaces (0.0.0.0)
        final int tcpPort;

        DeviceSpec(String serialPort, String bindAddress, int tcpPort) {
            this.serialPort = serialPort;
            this.bindAddress = bindAddress;
            this.tcpPort = tcpPort;
        }

        @Override
        public String toString() {
            if (bindAddress != null) {
                return serialPort + " -> " + bindAddress + ":" + tcpPort;
            }
            return serialPort + ":" + tcpPort;
        }
    }

    /**
     * Parse device specifications from command line.
     * Supports:
     * - COM3 (default port on all interfaces)
     * - COM3:50000 (custom port on all interfaces)
     * - COM3:127.0.0.1:50000 (specific IP and port)
     * - /dev/ttyUSB0:192.168.1.100:49999 (Linux with specific IP and port)
     */
    static List<DeviceSpec> parseDeviceSpecs(String portArg) {
        List<DeviceSpec> specs = new ArrayList<>();
        String[] ports = portArg.split(",");
        int autoPort = DEFAULT_TCP_PORT;

        for (String portSpec : ports) {
            portSpec = portSpec.trim();
            Matcher matcher = PORT_PATTERN.matcher(portSpec);

            if (matcher.matches()) {
                String serialPort = matcher.group(1);
                String bindAddressOrPort = matcher.group(2);
                String tcpPortStr = matcher.group(3);

                String bindAddress = null;
                int tcpPort;

                // Parse the middle component - could be bind address or port number
                if (tcpPortStr != null) {
                    // Format: serial:bindAddress:port
                    bindAddress = bindAddressOrPort;
                    tcpPort = Integer.parseInt(tcpPortStr);
                } else if (bindAddressOrPort != null) {
                    // Format: serial:X where X could be port or bind address
                    try {
                        tcpPort = Integer.parseInt(bindAddressOrPort);
                        // It's a port number, bind address stays null (all interfaces)
                    } catch (NumberFormatException e) {
                        // It's a bind address, use auto-increment port
                        bindAddress = bindAddressOrPort;
                        tcpPort = autoPort++;
                    }
                } else {
                    // Format: serial (no bind address, auto port)
                    tcpPort = autoPort++;
                }

                specs.add(new DeviceSpec(serialPort, bindAddress, tcpPort));
            } else {
                throw new IllegalArgumentException("Invalid port specification: " + portSpec);
            }
        }

        return specs;
    }

    /**
     * Specification for a remote CS2/bridge TCP server to connect to as a client.
     * <p>
     * Parsed from {@code --client-for} arguments. {@code bridgePorts} is the list of
     * local serial port names whose {@link BridgeInstance}s this forwarder attaches to.
     * An empty list means "first bridge".
     */
    static class RemoteClientSpec {
        final String remoteHost;
        final int remotePort;
        final List<String> bridgePorts; // empty = first bridge

        RemoteClientSpec(String remoteHost, int remotePort, List<String> bridgePorts) {
            this.remoteHost = remoteHost;
            this.remotePort = remotePort;
            this.bridgePorts = Collections.unmodifiableList(new ArrayList<>(bridgePorts));
        }

        @Override
        public String toString() {
            String target = remoteHost + ":" + remotePort;
            if (bridgePorts.isEmpty()) return target + " (first bridge)";
            return target + " -> " + bridgePorts;
        }
    }

    /**
     * Parse a {@code --client-for} argument value into a {@link RemoteClientSpec}.
     * <p>
     * Syntax (single value  -  the caller may call this once per {@code --client-for} flag):
     * <ul>
     *   <li>{@code IP}  -  remote port 15731 connects into first bridge</li>
     *   <li>{@code IP:remotePort}  -  custom remote port (numeric) connects into first bridge</li>
     *   <li>{@code IP:COMx}  -  default remote port 15731 connects into first bridge from --port statement</li>
     *   <li>{@code IP:COMx,COMy}  -  default remote port 15731, remote host connects to both COMx and COMy bridges</li>
     *   <li>{@code IP:remotePort:COMx,COMy}  -  custom port and remote host connects to both COMx and COMy bridges</li>
     * </ul>
     * The second segment is treated as a remote port only when it is a pure decimal integer
     * in [1,65535]. Anything else (contains letters, slashes or a comma) is a COM list.
     *
     * @param arg the argument value (not including the {@code --client-for} flag itself)
     * @return parsed spec
     * @throws IllegalArgumentException if the argument is invalid
     */
    static RemoteClientSpec parseClientForSpecs(String arg) {
        if (arg == null || arg.isEmpty()) {
            throw new IllegalArgumentException("--client-for argument must not be empty");
        }

        // Split on ':' but limit to 3 parts so a Linux path like /dev/ttyUSB0 in the
        // third segment survives intact (e.g. "10.0.0.1:50000:/dev/ttyUSB0").
        String[] parts = arg.split(":", 3);
        String host = parts[0];

        if (host.isEmpty()) {
            throw new IllegalArgumentException("--client-for: host must not be empty in: " + arg);
        }

        if (parts.length == 1) {
            // IP only
            return new RemoteClientSpec(host, DEFAULT_TCP_PORT, Collections.emptyList());
        }

        String second = parts[1];

        if (parts.length == 2) {
            // IP:X  -  X is remotePort (pure numeric) or COM list (anything else)
            if (isNumericPort(second)) {
                int port = parsePort(second, arg);
                return new RemoteClientSpec(host, port, Collections.emptyList());
            } else {
                // COM list (possibly a single entry)
                return new RemoteClientSpec(host, DEFAULT_TCP_PORT, parseBridgeList(second));
            }
        }

        // parts.length == 3: IP:remotePort:COMlist
        if (!isNumericPort(second)) {
            throw new IllegalArgumentException(
                "--client-for: expected numeric port as second segment before COM list in: " + arg);
        }
        int port = parsePort(second, arg);
        return new RemoteClientSpec(host, port, parseBridgeList(parts[2]));
    }

    private static boolean isNumericPort(String s) {
        if (s.isEmpty()) return false;
        for (char c : s.toCharArray()) {
            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    private static int parsePort(String s, String fullArg) {
        int port = Integer.parseInt(s);
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(
                "--client-for: port " + port + " out of range [1,65535] in: " + fullArg);
        }
        return port;
    }

    private static List<String> parseBridgeList(String s) {
        String[] entries = s.split(",");
        List<String> list = new ArrayList<>();
        for (String e : entries) {
            String trimmed = e.trim();
            if (!trimmed.isEmpty()) list.add(trimmed);
        }
        if (list.isEmpty()) {
            throw new IllegalArgumentException("--client-for: bridge port list is empty");
        }
        return list;
    }

    /**
     * Resolve which {@link BridgeInstance}s a {@link RemoteClientSpec} should attach to.
     * <p>
     * If {@code spec.bridgePorts} is empty, attaches to the first bridge.
     * Otherwise attaches to every bridge whose serial port name appears in the list.
     * Unknown port names are warned about but do not abort.
     */
    static List<BridgeInstance> resolveBridgeTargets(RemoteClientSpec spec, List<BridgeInstance> allBridges) {
        if (allBridges.isEmpty()) return Collections.emptyList();
        if (spec.bridgePorts.isEmpty()) {
            return Collections.singletonList(allBridges.get(0));
        }
        List<BridgeInstance> targets = new ArrayList<>();
        for (String portName : spec.bridgePorts) {
            boolean found = false;
            for (BridgeInstance b : allBridges) {
                if (portName.equalsIgnoreCase(b.serialPortName)) {
                    targets.add(b);
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.warn("[RemoteFwd] --client-for references unknown bridge port '{}'  -  ignored", portName);
            }
        }
        return targets;
    }

    public static void main(String[] args) {
        // Configure logging to console
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "INFO");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yyyy-MM-dd HH:mm:ss");

        System.out.println("MCAN Protocol Serial To TCP Multiplexer Bridge");
        System.out.println("==============================================");
        System.out.println("Bridges MCAN serial adapters to CS2 TCP protocol");
        System.out.println();

        // Parse command line arguments
        String portArg = null;
        int baudRate = DEFAULT_BAUD_RATE;
        List<RemoteClientSpec> clientForSpecs = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--port":
                case "-p":
                    if (i + 1 < args.length) {
                        portArg = args[++i];
                    }
                    break;
                case "--client-for":
                case "-c":
                    if (i + 1 < args.length) {
                        try {
                            clientForSpecs.add(parseClientForSpecs(args[++i]));
                        } catch (IllegalArgumentException e) {
                            System.err.println("Invalid --client-for argument: " + e.getMessage());
                            System.exit(1);
                        }
                    }
                    break;
                case "--baud":
                case "-b":
                    if (i + 1 < args.length) {
                        try {
                            baudRate = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid baud rate: " + args[i]);
                            System.exit(1);
                        }
                    }
                    break;
                case "--monitor":
                case "-m":
                    monitorMode = true;
                    break;
                case "--help":
                case "-h":
                    printUsage();
                    System.exit(0);
                    break;
                case "--list":
                case "-l":
                    listSerialPorts();
                    System.exit(0);
                    break;
                default:
                    System.err.println("Unknown option: " + args[i]);
                    printUsage();
                    System.exit(1);
            }
        }

        // Validate required arguments.  --port is required unless --client-for is given alone
        if (portArg == null && clientForSpecs.isEmpty()) {
            System.err.println("Error: Serial port is required (--port) or at least one --client-for upstream");
            System.out.println();
            printUsage();
            System.exit(1);
        }

        // Parse device specifications
        List<DeviceSpec> devices = new ArrayList<>();
        if (portArg != null) {
            try {
                devices = parseDeviceSpecs(portArg);
            } catch (IllegalArgumentException e) {
                System.err.println("Error: " + e.getMessage());
                System.exit(1);
                return;
            }
            if (devices.isEmpty()) {
                System.err.println("Error: No devices specified");
                System.exit(1);
            }
        }

        // Print configuration
        System.out.println("Configuration:");
        System.out.println("  Baud rate: " + baudRate);
        if (!devices.isEmpty()) {
            System.out.println("  Devices: " + devices.size());
            for (DeviceSpec spec : devices) {
                if (spec.bindAddress != null) {
                    System.out.println("    - " + spec.serialPort + " -> " + spec.bindAddress + ":" + spec.tcpPort);
                } else {
                    System.out.println("    - " + spec.serialPort + " -> TCP port " + spec.tcpPort + " (all interfaces)");
                }
            }
        }
        if (!clientForSpecs.isEmpty()) {
            System.out.println("  Upstream remotes: " + clientForSpecs.size());
            for (RemoteClientSpec spec : clientForSpecs) {
                System.out.println("    - " + spec);
            }
        }
        System.out.println();

        // Create and start bridges
        try {
            for (DeviceSpec spec : devices) {
                BridgeInstance bridge = new BridgeInstance(spec.serialPort, spec.bindAddress, spec.tcpPort, baudRate);
                bridge.start();
                bridges.add(bridge);
                log.info("Bridge {} started successfully", spec);
            }

            // Attach and start remote forwarders
            for (RemoteClientSpec spec : clientForSpecs) {
                List<BridgeInstance> targets = resolveBridgeTargets(spec, bridges);
                for (BridgeInstance target : targets) {
                    RemoteForwarder forwarder = new RemoteForwarder(spec);
                    forwarder.setBridge(target);
                    target.addRemoteForwarder(forwarder);
                    forwarder.start();
                    log.info("Remote forwarder {} attached to bridge {}", spec, target.serialPortName);
                }
            }

            // Register shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(McanTcpBridge::shutdown));

            System.out.println();
            System.out.println("All bridges running. Press Ctrl+C to stop.");
            System.out.println();

            Thread.currentThread().join();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted: " + e.getMessage());
            log.error("Bridge interrupted", e);
            shutdown();
            System.exit(1);
        } catch (java.io.IOException e) {
            System.err.println("I/O Error: " + e.getMessage());
            log.error("Failed to start bridge", e);
            shutdown();
            System.exit(1);
        }
    }

    /**
     * Shutdown all bridges gracefully.
     */
    static void shutdown() {
        log.info("Shutting down all bridges...");
        running.set(false);

        for (BridgeInstance bridge : bridges) {
            bridge.shutdown();
        }

        log.info("All bridges stopped");
    }

    /**
     * Print usage information.
     */
    private static void printUsage() {
        System.out.println("Usage: java -jar mcan-tcp-bridge.jar --port <port_spec> [options]");
        System.out.println();
        System.out.println("Port Specification:");
        System.out.println("  Single device:    --port COM3");
        System.out.println("  Custom TCP port:  --port COM3:50000");
        System.out.println("  Bind to specific IP: --port COM3:127.0.0.1:50000");
        System.out.println("  Multiple devices: --port COM2,COM3:49999,COM4:192.168.1.100:50000");
        System.out.println();
        System.out.println("  Format: <serial>[:<bind_addr>][:<tcp_port>][,<serial>[:<bind_addr>][:<tcp_port>],...]");
        System.out.println("    - If TCP port omitted, uses default (15731) for first device");
        System.out.println("    - Subsequent devices without port get auto-incremented (15732, 15733, ...)");
        System.out.println("    - If bind address omitted, binds to all interfaces (0.0.0.0)");
        System.out.println("    - Bind address can be IPv4 (192.168.1.100) or IPv6 (::1) or hostname (localhost)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  --port COM2,COM3,COM4");
        System.out.println("    COM2 -> TCP 15731 (all interfaces)");
        System.out.println("    COM3 -> TCP 15732 (all interfaces)");
        System.out.println("    COM4 -> TCP 15733 (all interfaces)");
        System.out.println();
        System.out.println("  --port COM2,COM3:49999,COM4:50000");
        System.out.println("    COM2 -> TCP 15731 (default, all interfaces)");
        System.out.println("    COM3 -> TCP 49999 (explicit, all interfaces)");
        System.out.println("    COM4 -> TCP 50000 (explicit, all interfaces)");
        System.out.println();
        System.out.println("  --port COM3:127.0.0.1:15731");
        System.out.println("    COM3 -> localhost:15731 (localhost only)");
        System.out.println();
        System.out.println("  --port COM3:192.168.1.100:15731,COM4:192.168.1.100:15732");
        System.out.println("    COM3 -> 192.168.1.100:15731 (specific interface)");
        System.out.println("    COM4 -> 192.168.1.100:15732 (specific interface)");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --port, -p <spec>         Port specification (required if no --client-for, see above)");
        System.out.println("  --client-for, -c <target> Connect as client to a remote CS2/bridge TCP server.");
        System.out.println("                            Frames from remote are forwarded to local TCP clients");
        System.out.println("                            and serial. Can be repeated for multiple upstreams.");
        System.out.println("                            Syntax:");
        System.out.println("                              IP                   -  default port 15731, first bridge");
        System.out.println("                              IP:remotePort         -  custom remote port, first bridge");
        System.out.println("                              IP:COMx               -  default port, specific bridge");
        System.out.println("                              IP:COMx,COMy          -  default port, multiple bridges");
        System.out.println("                              IP:remotePort:COMx,COMy  -  custom port and bridge filter");
        System.out.println("                            Reconnects with creeping delay (2s,5s,20s,60s,3m,5m...)");
        System.out.println("  --baud, -b <rate>         Serial baud rate for all devices (default: 500000)");
        System.out.println("  --monitor, -m             Enable message monitoring to stdout");
        System.out.println("  --list, -l                List available serial ports and exit");
        System.out.println("  --help, -h                Show this help message");
        System.out.println();
        System.out.println("Advanced: Virtual COM Port for Serial Software");
        System.out.println("  To use serial-only software (e.g., programmer tools) alongside JMRI,");
        System.out.println("  you can create a virtual COM port that connects to the bridge's TCP server.");
        System.out.println();
        System.out.println("  Linux/macOS: ./scripts/Linux/create-mcan-virtual-port.sh /dev/ttyVCOM0 localhost 15731");
        System.out.println("  Windows:     use third-party software such as com2tcp or com2com");
        System.out.println();
        System.out.println("  See: help/en/html/hardware/marklin/McanTcpBridge.shtml#virtual-ports");
        System.out.println();
    }

    private static void listSerialPorts() {
        System.out.println("Available serial ports:");
        System.out.println();

        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) {
            System.out.println("  (none found)");
        } else {
            for (SerialPort port : ports) {
                System.out.println("  " + port.getSystemPortName());
                System.out.println("    Description: " + port.getPortDescription());
                System.out.println("    Location: " + port.getPortLocation());
                System.out.println();
            }
        }
    }

    /**
     * Single bridge instance for one serial port.
     */
    static class BridgeInstance {
        private static final int BUFFER_SIZE = 1024;
        private static final int RECONNECT_DELAY_MS = 5000; // 5 seconds
        private static final int CAN_MESSAGE_SIZE = 13; // Märklin CAN messages are padded to 13 bytes

        final String serialPortName;
        private final String bindAddress; // null means bind to all interfaces
        private final int tcpPort;
        private final int baudRate;
        private volatile SerialPort serialPort;  // com.fazecast.jSerialComm.SerialPort
        private volatile boolean serialPortConnected = false;
        private ServerSocket serverSocket;
        private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
        private final List<RemoteForwarder> remoteForwarders = new CopyOnWriteArrayList<>();
        private final AtomicBoolean reconnecting = new AtomicBoolean(false);

        // Message queue for TCP to Serial (prevents command interleaving)
        private final BlockingQueue<byte[]> outgoingQueue = new LinkedBlockingQueue<>();

        // Statistics
        private long serialBytesRead = 0;
        private long serialBytesWritten = 0;
        private long tcpBytesRead = 0;
        private long tcpBytesWritten = 0;

        BridgeInstance(String serialPortName, String bindAddress, int tcpPort, int baudRate) {
            this.serialPortName = serialPortName;
            this.bindAddress = bindAddress;
            this.tcpPort = tcpPort;
            this.baudRate = baudRate;
        }

        void start() throws IOException {
            log.info("[{}:{}] Starting bridge", serialPortName, tcpPort);

            // Try to open serial port (non-fatal if it fails)
            tryOpenSerialPort();

            // Start TCP server
            startTcpServer();

            // Start serial reader thread
            startSerialReader();

            // Start serial writer thread (processes outgoing queue)
            startSerialWriter();

            // Start reconnection monitor thread
            startReconnectionMonitor();

            log.info("[{}:{}] Bridge started (serial port {})",
                     serialPortName, tcpPort, serialPortConnected ? "connected" : "not available");
        }

        private void tryOpenSerialPort() {
            try {
                log.info("[{}:{}] Attempting to open serial port", serialPortName, tcpPort);

                serialPort = SerialPort.getCommPort(serialPortName);

                if (!serialPort.openPort()) {
                    log.warn("[{}:{}] Serial port not available: {}", serialPortName, tcpPort, serialPortName);
                    serialPortConnected = false;
                    serialPort = null;
                    return;
                }

                // Apply settings after open. See JMRI JSerialPort.activatePort()
                serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 100, 0);
                serialPort.setNumDataBits(8);
                serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
                serialPort.setParity(SerialPort.NO_PARITY);
                serialPort.setBaudRate(baudRate);
                serialPort.setFlowControl(SerialPort.FLOW_CONTROL_RTS_ENABLED);
                serialPort.setDTR();
                serialPort.setRTS();

                // Drain any stale bytes left in the adapter's RX buffer
                InputStream serialIn = serialPort.getInputStream();
                int stale = serialIn.available();
                if (stale > 0) {
                    serialIn.skip(stale);
                    log.debug("[{}:{}] Purged {} stale bytes from serial RX buffer", serialPortName, tcpPort, stale);
                }
                serialPortConnected = true;
                log.info("[{}:{}] Serial port opened: {}", serialPortName, tcpPort, serialPort.getDescriptivePortName());
            } catch (Exception e) {
                log.warn("[{}:{}] Failed to open serial port: {}", serialPortName, tcpPort, e.getMessage());
                serialPortConnected = false;
                serialPort = null;
            }
        }

        private void closeSerialPort() {
            if (serialPort != null && serialPort.isOpen()) {
                serialPort.closePort();
                log.info("[{}:{}] Serial port closed", serialPortName, tcpPort);
            }
            serialPort = null;
            serialPortConnected = false;
        }
        private void startReconnectionMonitor() {
            Thread monitorThread = new Thread(() -> {
                log.info("[{}:{}] Reconnection monitor started", serialPortName, tcpPort);

                while (running.get()) {
                    try {
                        Thread.sleep(RECONNECT_DELAY_MS);

                        // Check if serial port is connected
                        if (!serialPortConnected || serialPort == null || !serialPort.isOpen()) {
                            if (reconnecting.compareAndSet(false, true)) {
                                log.info("[{}:{}] Serial port disconnected, attempting reconnection...",
                                         serialPortName, tcpPort);

                                closeSerialPort();
                                tryOpenSerialPort();

                                if (serialPortConnected) {
                                    log.info("[{}:{}] Serial port reconnected successfully",
                                             serialPortName, tcpPort);
                                }
                                reconnecting.set(false);
                            }
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        log.error("[{}:{}] Error in reconnection monitor", serialPortName, tcpPort, e);
                        reconnecting.set(false);
                    }
                }

                log.info("[{}:{}] Reconnection monitor stopped", serialPortName, tcpPort);
            }, "Reconnect-Monitor-" + tcpPort);

            monitorThread.setDaemon(true);
            monitorThread.start();
        }

        private void startTcpServer() throws IOException {
            if (bindAddress != null) {
                // Bind to specific address
                java.net.InetAddress addr = java.net.InetAddress.getByName(bindAddress);
                serverSocket = new ServerSocket(tcpPort, 50, addr);
                log.info("[{}:{}] TCP server listening on {}:{}", serialPortName, tcpPort, bindAddress, tcpPort);
            } else {
                // Bind to all interfaces
                serverSocket = new ServerSocket(tcpPort);
                log.info("[{}:{}] TCP server listening on all interfaces", serialPortName, tcpPort);
            }

            Thread acceptorThread = new Thread(this::acceptClients, "TCP-Acceptor-" + tcpPort);
            acceptorThread.setDaemon(true);
            acceptorThread.start();
        }

        private void acceptClients() {
            while (running.get() && !serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    log.info("[{}:{}] Client connected: {}", serialPortName, tcpPort,
                             clientSocket.getRemoteSocketAddress());

                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    clients.add(handler);

                    Thread clientThread = new Thread(handler,
                        "TCP-Client-" + tcpPort + "-" + clientSocket.getRemoteSocketAddress());
                    clientThread.setDaemon(true);
                    clientThread.start();

                } catch (IOException e) {
                    if (running.get()) {
                        log.error("[{}:{}] Error accepting client", serialPortName, tcpPort, e);
                    }
                }
            }
        }

        private void startSerialReader() {
            Thread readerThread = new Thread(() -> {
                byte[] buffer = new byte[BUFFER_SIZE];

                log.info("[{}:{}] Serial reader started", serialPortName, tcpPort);

                while (running.get()) {
                    try {
                        // Wait for serial port to be available
                        if (!serialPortConnected || serialPort == null) {
                            Thread.sleep(1000);
                            continue;
                        }

                        InputStream serialIn = serialPort.getInputStream();
                        if (serialIn == null) {
                            log.warn("[{}:{}] Serial input stream unavailable", serialPortName, tcpPort);
                            serialPortConnected = false;
                            Thread.sleep(1000);
                            continue;
                        }

                        int bytesRead = serialIn.read(buffer);
                        if (bytesRead > 0) {
                            serialBytesRead += bytesRead;

                            if (log.isDebugEnabled()) {
                                StringBuilder hex = new StringBuilder();
                                for (int i = 0; i < Math.min(bytesRead, 13); i++) {
                                    hex.append(String.format("%02X ", buffer[i] & 0xFF));
                                }
                                log.debug("[{}:{}] Serial RX ({} bytes): {}",
                                         serialPortName, tcpPort, bytesRead, hex);
                            }

                            broadcastToClients(buffer, bytesRead);
                        } else if (bytesRead < 0) {
                            // End of stream - port disconnected
                            log.warn("[{}:{}] Serial port disconnected (EOF)", serialPortName, tcpPort);
                            serialPortConnected = false;
                        }
                    } catch (com.fazecast.jSerialComm.SerialPortTimeoutException e) {
                        // 100ms read timeout  -  normal, just loop back
                    } catch (IOException e) {
                        if (running.get()) {
                            log.warn("[{}:{}] Serial read error (device may be unplugged): {}",
                                    serialPortName, tcpPort, e.getMessage());
                            serialPortConnected = false;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ie) {
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                log.info("[{}:{}] Serial reader stopped", serialPortName, tcpPort);
            }, "Serial-Reader-" + tcpPort);

            readerThread.setDaemon(true);
            readerThread.start();
        }

        private void startSerialWriter() {
            Thread writerThread = new Thread(() -> {
                byte[] messageBuffer = new byte[CAN_MESSAGE_SIZE];
                int bufferPos = 0;

                log.info("[{}:{}] Serial writer started", serialPortName, tcpPort);

                while (running.get()) {
                    try {
                        // Wait for serial port to be available
                        if (!serialPortConnected || serialPort == null) {
                            Thread.sleep(1000);
                            continue;
                        }

                        // Take a message from the queue (blocking)
                        byte[] queuedData = outgoingQueue.take();

                        // Accumulate bytes into 13-byte CAN messages
                        for (int i = 0; i < queuedData.length; i++) {
                            messageBuffer[bufferPos++] = queuedData[i];

                            // When we have a complete 13-byte message, send it
                            if (bufferPos == CAN_MESSAGE_SIZE) {
                                sendMessageToSerial(messageBuffer, CAN_MESSAGE_SIZE);
                                bufferPos = 0; // Reset buffer for next message
                            }
                        }

                        // Note: Partial messages stay in buffer until completed
                        // This ensures we only send complete 13-byte CAN packets

                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        log.error("[{}:{}] Error in serial writer", serialPortName, tcpPort, e);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            break;
                        }
                    }
                }

                log.info("[{}:{}] Serial writer stopped", serialPortName, tcpPort);
            }, "Serial-Writer-" + tcpPort);

            writerThread.setDaemon(true);
            writerThread.start();
        }

        private void sendMessageToSerial(byte[] data, int length) {
            if (!serialPortConnected || serialPort == null) {
                if (log.isDebugEnabled()) {
                    log.debug("[{}:{}] Serial port not connected, discarding {} bytes",
                             serialPortName, tcpPort, length);
                }
                return;
            }

            // Monitor mode: Log TCP to Serial with device identification
            if (monitorMode && length >= 13) {
                int[] rawData = new int[13];
                for (int i = 0; i < 13; i++) {
                    rawData[i] = data[i] & 0xFF;
                }
                String message = MarklinMessageFormatter.formatRaw(rawData, null, true);
                log.info("[{}] [TCP->Serial] {}", serialPortName, message);
            }

            try {
                OutputStream serialOut = serialPort.getOutputStream();
                if (serialOut == null) {
                    log.warn("[{}:{}] Serial output stream unavailable", serialPortName, tcpPort);
                    serialPortConnected = false;
                    return;
                }

                serialOut.write(data, 0, length);
                serialOut.flush();
                serialBytesWritten += length;

                if (log.isDebugEnabled()) {
                    StringBuilder hex = new StringBuilder();
                    for (int i = 0; i < Math.min(length, 13); i++) {
                        hex.append(String.format("%02X ", data[i] & 0xFF));
                    }
                    log.debug("[{}:{}] Serial TX ({} bytes): {}",
                             serialPortName, tcpPort, length, hex);
                }
            } catch (IOException e) {
                log.warn("[{}:{}] Error writing to serial (device may be unplugged): {}",
                        serialPortName, tcpPort, e.getMessage());
                serialPortConnected = false;
            }
        }

        void broadcastToClients(byte[] data, int length) {
            broadcastToClients(data, length, null);
        }

        void broadcastToClients(byte[] data, int length, ClientHandler exclude) {
            // Monitor mode: Log Serial to TCP with device identification
            if (monitorMode && length >= 13) {
                int[] rawData = new int[13];
                for (int i = 0; i < 13; i++) {
                    rawData[i] = data[i] & 0xFF;
                }
                String message = MarklinMessageFormatter.formatRaw(rawData, null, true);
                log.info("[{}] [Serial->TCP] {}", serialPortName, message);
            }

            for (ClientHandler client : clients) {
                if (client == exclude) continue;
                try {
                    client.send(data, length);
                    tcpBytesWritten += length;
                } catch (IOException e) {
                    log.warn("[{}:{}] Failed to send to client", serialPortName, tcpPort, e);
                    removeClient(client);
                }
            }

            // Forward to any upstream remote forwarders
            for (RemoteForwarder forwarder : remoteForwarders) {
                forwarder.send(data, length);
            }
        }

        void addRemoteForwarder(RemoteForwarder forwarder) {
            remoteForwarders.add(forwarder);
        }

        void sendToSerial(byte[] data, int length) {
            if (!serialPortConnected || serialPort == null) {
                if (log.isDebugEnabled()) {
                    log.debug("[{}:{}] Serial port not connected, discarding {} bytes",
                             serialPortName, tcpPort, length);
                }
                return;
            }

            // Queue the data for the serial writer thread
            // The writer will accumulate bytes into complete 13-byte CAN messages
            byte[] copy = new byte[length];
            System.arraycopy(data, 0, copy, 0, length);

            try {
                outgoingQueue.put(copy);
            } catch (InterruptedException e) {
                log.warn("[{}:{}] Interrupted while queueing message", serialPortName, tcpPort);
                Thread.currentThread().interrupt();
            }
        }

        void removeClient(ClientHandler client) {
            clients.remove(client);
            client.close();
        }

        void onTcpBytesRead(int bytes) {
            tcpBytesRead += bytes;
        }

        void printStatistics() {
            log.info("[{}:{}] === Statistics ===", serialPortName, tcpPort);
            log.info("[{}:{}] Serial port status: {}", serialPortName, tcpPort,
                     serialPortConnected ? "connected" : "disconnected");
            log.info("[{}:{}] Serial: {} bytes read, {} bytes written",
                     serialPortName, tcpPort, serialBytesRead, serialBytesWritten);
            log.info("[{}:{}] TCP: {} bytes read, {} bytes written",
                     serialPortName, tcpPort, tcpBytesRead, tcpBytesWritten);
            log.info("[{}:{}] Connected clients: {}", serialPortName, tcpPort, clients.size());
        }

        void shutdown() {
            log.info("[{}:{}] Shutting down bridge", serialPortName, tcpPort);

            printStatistics();

            // Close all clients
            for (ClientHandler client : clients) {
                client.close();
            }
            clients.clear();

            // Stop all remote forwarders
            for (RemoteForwarder forwarder : remoteForwarders) {
                forwarder.stop();
            }
            remoteForwarders.clear();

            // Close server socket
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                log.error("[{}:{}] Error closing server socket", serialPortName, tcpPort, e);
            }

            // Close serial port
            closeSerialPort();

            log.info("[{}:{}] Bridge shutdown complete", serialPortName, tcpPort);
        }
    }

    /**
     * Handler for a single TCP client connection.
     */
    static class ClientHandler implements Runnable {
        private static final int BUFFER_SIZE = 1024;

        private final Socket socket;
        private final InputStream in;
        private final OutputStream out;
        private final String remoteAddress;
        private final BridgeInstance bridge;

        ClientHandler(Socket socket, BridgeInstance bridge) throws IOException {
            this.socket = socket;
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
            this.remoteAddress = socket.getRemoteSocketAddress().toString();
            this.bridge = bridge;

            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
        }

        @Override
        public void run() {
            byte[] buffer = new byte[BUFFER_SIZE];

            try {
                while (running.get() && !socket.isClosed()) {
                    int bytesRead = in.read(buffer);
                    if (bytesRead < 0) {
                        break;
                    }

                    if (bytesRead > 0) {
                        bridge.onTcpBytesRead(bytesRead);

                        if (log.isDebugEnabled()) {
                            StringBuilder hex = new StringBuilder();
                            for (int i = 0; i < Math.min(bytesRead, 13); i++) {
                                hex.append(String.format("%02X ", buffer[i] & 0xFF));
                            }
                            log.debug("[{}] TCP RX from {} ({} bytes): {}",
                                     bridge.tcpPort, remoteAddress, bytesRead, hex);
                        }

                        bridge.sendToSerial(buffer, bytesRead);
                        bridge.broadcastToClients(buffer, bytesRead, this);
                    }
                }
            } catch (SocketException e) {
                if (running.get()) {
                    log.info("[{}] Client disconnected: {}", bridge.tcpPort, remoteAddress);
                }
            } catch (IOException e) {
                log.error("[{}] Error reading from client: {}", bridge.tcpPort, remoteAddress, e);
            } finally {
                close();
                bridge.removeClient(this);
            }
        }

        void send(byte[] data, int length) throws IOException {
            synchronized (out) {
                out.write(data, 0, length);
                out.flush();
            }
        }

        void close() {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                log.error("Error closing client socket", e);
            }
        }
    }

    /**
     * Outbound TCP client that connects to a remote CS2 or MCAN bridge server.
     * <p>
     * Frames received from the remote are broadcast to local TCP clients and forwarded
     * to the serial port via the owning {@link BridgeInstance}. Frames sent to this
     * forwarder (via {@link #send}) are written to the remote socket.
     * <p>
     * On any connection failure or loss the forwarder waits a creeping delay before
     * retrying: 2 s, 5 s, 20 s, 60 s, 180 s, then 5 minutes indefinitely.
     * The bridge continues serving local clients regardless of remote availability.
     */
    static class RemoteForwarder {

        private static final int[] RECONNECT_DELAYS_MS = {2000, 5000, 20000, 60000, 180000, 300000};
        private static final int CAN_MESSAGE_SIZE = 13;

        private final RemoteClientSpec spec;
        private BridgeInstance bridge; // set via setBridge() before start()
        private volatile Socket remoteSocket;
        private volatile OutputStream remoteOut;
        private final AtomicBoolean stopped = new AtomicBoolean(false);

        RemoteForwarder(RemoteClientSpec spec) {
            this.spec = spec;
        }

        void setBridge(BridgeInstance bridge) {
            this.bridge = bridge;
        }

        /** Returns the delay in ms for the given zero-based attempt index. */
        static int reconnectDelayMs(int attempt) {
            int idx = Math.min(attempt, RECONNECT_DELAYS_MS.length - 1);
            return RECONNECT_DELAYS_MS[idx];
        }

        void start() {
            Thread t = new Thread(this::runLoop, "RemoteFwd-" + spec.remoteHost + ":" + spec.remotePort);
            t.setDaemon(true);
            t.start();
        }

        void stop() {
            stopped.set(true);
            closeRemote();
        }

        /** Send a frame to the remote. Silently discards if not connected. */
        void send(byte[] data, int length) {
            OutputStream out = remoteOut;
            if (out == null) return;
            try {
                synchronized (out) {
                    out.write(data, 0, length);
                    out.flush();
                }
            } catch (IOException e) {
                log.warn("[RemoteFwd->{}:{}] Send failed: {}", spec.remoteHost, spec.remotePort, e.getMessage());
                closeRemote();
            }
        }

        private void runLoop() {
            int attempt = 0;
            while (!stopped.get() && running.get()) {
                try {
                    log.info("[RemoteFwd] Connecting to {}:{} (attempt {})",
                             spec.remoteHost, spec.remotePort, attempt + 1);
                    Socket s = new Socket(spec.remoteHost, spec.remotePort);
                    s.setKeepAlive(true);
                    s.setTcpNoDelay(true);
                    remoteSocket = s;
                    remoteOut = s.getOutputStream();
                    attempt = 0; // reset creep on successful connect
                    log.info("[RemoteFwd] Connected to {}:{}", spec.remoteHost, spec.remotePort);

                    readLoop(s);

                } catch (IOException e) {
                    if (!stopped.get() && running.get()) {
                        log.warn("[RemoteFwd] {}:{} unreachable: {}", spec.remoteHost, spec.remotePort, e.getMessage());
                    }
                } finally {
                    closeRemote();
                }

                if (stopped.get() || !running.get()) break;

                int delay = reconnectDelayMs(attempt++);
                log.info("[RemoteFwd] Retrying {}:{} in {} s",
                         spec.remoteHost, spec.remotePort, delay / 1000);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            log.info("[RemoteFwd] Forwarder to {}:{} stopped", spec.remoteHost, spec.remotePort);
        }

        private void readLoop(Socket s) throws IOException {
            InputStream in = s.getInputStream();
            byte[] buf = new byte[CAN_MESSAGE_SIZE];
            int pos = 0;
            byte[] tmp = new byte[256];

            while (!stopped.get() && running.get() && !s.isClosed()) {
                int n = in.read(tmp);
                if (n < 0) {
                    log.info("[RemoteFwd] {}:{} closed connection", spec.remoteHost, spec.remotePort);
                    return;
                }
                for (int i = 0; i < n; i++) {
                    buf[pos++] = tmp[i];
                    if (pos == CAN_MESSAGE_SIZE) {
                        if (bridge != null) {
                            bridge.sendToSerial(buf, CAN_MESSAGE_SIZE);
                            bridge.broadcastToClients(buf, CAN_MESSAGE_SIZE);
                        }
                        pos = 0;
                    }
                }
            }
        }

        private void closeRemote() {
            remoteOut = null;
            Socket s = remoteSocket;
            remoteSocket = null;
            if (s != null && !s.isClosed()) {
                try { s.close(); } catch (IOException ignored) {}
            }
        }
    }
}
