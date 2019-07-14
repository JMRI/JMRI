package jmri.jmris;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.util.zeroconf.ZeroConfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main JMRI Server implementation.
 *
 * It starts a thread for each client.
 *
 */
public class JmriServer {

    protected int portNo = 3000; // Port to listen to for new clients.
    protected int timeout = 0; // Timeout in milliseconds (0 = no timeout).
    protected ServerSocket connectSocket;
    protected ZeroConfService service = null;
    protected ShutDownTask shutDownTask = null;
    private Thread listenThread = null;
    protected ArrayList<ClientListener> connectedClientThreads = new ArrayList<>();

    // Create a new server using the default port
    public JmriServer() {
        this(3000);
    }

    // Create a new server using a given port and no timeout
    public JmriServer(int port) {
        this(port, 0);
    }

    // Create a new server using a given port with a timeout
    // A timeout of 0 is infinite
    public JmriServer(int port, int timeout) {
        super();
        // Try registering the server on the given port
        try {
            this.connectSocket = new ServerSocket(port);
        } catch (IOException e) {
            log.error("Failed to connect to port {}", port);
        }
        this.portNo = port;
        this.timeout = timeout;
    }

    // Maintain a vector of connected clients
    // Add a new client
    private synchronized void addClient(ClientListener client) {
        if (!connectedClientThreads.contains(client)) {
            connectedClientThreads.add(client);
            client.start();
        }
    }

    //Remove a client
    private synchronized void removeClient(ClientListener client) {
        if (connectedClientThreads.contains(client)) {
            client.stop(this);
            connectedClientThreads.remove(client);
        }
    }

    public void start() {
        /* Start the server thread */
        if (this.listenThread == null) {
            this.listenThread = new Thread(new NewClientListener(connectSocket));
            this.listenThread.start();
            this.advertise();
        }
        if (this.shutDownTask != null) {
            InstanceManager.getDefault(jmri.ShutDownManager.class).register(this.shutDownTask);
        }
    }

    // Advertise the service with ZeroConf
    protected void advertise() {
        this.advertise("_jmri._tcp.local.");
    }

    protected void advertise(String type) {
        this.advertise(type, new HashMap<>());
    }

    protected void advertise(String type, HashMap<String, String> properties) {
        if (this.service == null) {
            this.service = ZeroConfService.create(type, this.portNo, properties);
        }
        this.service.publish();
    }

    public void stop() {
        this.connectedClientThreads.forEach((client) -> {
            client.stop(this);
        });
        this.listenThread = null;
        this.service.stop();
        if (this.shutDownTask != null) {
            InstanceManager.getDefault(jmri.ShutDownManager.class).deregister(this.shutDownTask);
        }
    }

    // Internal thread to listen for new connections
    class NewClientListener implements Runnable {

        ServerSocket listenSocket = null;
        boolean running = true;

        public NewClientListener(ServerSocket socket) {

            listenSocket = socket;
        }

        @Override
        public void run() {
            // Listen for connection requests
            try {
                while (running) {
                    Socket clientSocket = listenSocket.accept();
                    clientSocket.setSoTimeout(timeout);
                    log.debug(" Client Connected from IP {} port {}", clientSocket.getInetAddress(), clientSocket.getPort());
                    addClient(new ClientListener(clientSocket));
                }
            } catch (IOException e) {
                log.error("IOException while Listening for clients");
            }
        }

        public void stop() {
            //super.stop();
            running = false;
            try {
                listenSocket.close();
                log.debug("Listen Socket closed");
            } catch (IOException e) {
                log.error("socket in ThreadedServer won't close");
            }
        }
    } // end of NewClientListener class

    // Internal class to handle a client
    protected class ClientListener implements Runnable {

        Socket clientSocket = null;
        DataInputStream inStream = null;
        DataOutputStream outStream = null;
        Thread clientThread = null;

        public ClientListener(Socket socket) {
            log.debug("Starting new Client");
            clientSocket = socket;
            try {
                inStream = new DataInputStream(clientSocket.getInputStream());
                outStream = new DataOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                log.error("Error obtaining I/O Stream from socket.");
            }
        }

        public void start() {
            clientThread = new Thread(this);
            clientThread.start();
        }

        public void stop(JmriServer server) {
            try {
                server.stopClient(inStream, outStream);
                clientSocket.close();
            } catch (IOException e) {
                // silently ignore, since we may be reacting to a closed socket
            }
            clientThread = null;
        }

        @Override
        public void run() {
            // handle a client.
            try {
                handleClient(inStream, outStream);
            } catch (IOException ex) {
                // When we get an IO exception here, we're done
                log.debug("Server Exiting");
                // Unregister with the server
                removeClient(this);
            } catch (java.lang.NullPointerException ex) {
                // When we get an IO exception here, we're done with this client
                log.debug("Client Disconnect", ex);
                // Unregister with the server
                removeClient(this);
            }
        }
    } // end of ClientListener class.

    // Handle communication to a client through inStream and outStream
    public void handleClient(DataInputStream inStream, DataOutputStream outStream) throws IOException {
        // Listen for commands from the client until the connection closes
        byte cmd[] = new byte[100];
        int count;
        while (true) {
            // Read the command from the client
            count = inStream.read(cmd);
            // Echo the input back to the client
            if (count != 0) {
                outStream.write(cmd);
            }
        }
    }

    // Send a stop message to the client if applicable
    public void stopClient(DataInputStream inStream, DataOutputStream outStream) throws IOException {
        outStream.writeBytes("");
    }
    private final static Logger log = LoggerFactory.getLogger(JmriServer.class);
}
