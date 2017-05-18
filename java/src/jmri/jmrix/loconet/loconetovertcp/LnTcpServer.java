package jmri.jmrix.loconet.loconetovertcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.implementation.QuietShutDownTask;
import jmri.util.zeroconf.ZeroConfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the LocoNetOverTcp LbServer Server Protocol
 *
 * @author Alex Shepherd Copyright (C) 2006
 */
public class LnTcpServer {

    final LinkedList<ClientRxHandler> clients;
    Thread socketListener;
    ServerSocket serverSocket;
    boolean settingsLoaded = false;
    ServerListner stateListner;
    boolean settingsChanged = false;
    QuietShutDownTask shutDownTask;
    ZeroConfService service = null;
    static final String AUTO_START_KEY = "AutoStart";
    static final String PORT_NUMBER_KEY = "PortNumber";

    private int portNumber = 1234;

    private LnTcpServer() {
        clients = new LinkedList<>();
        portNumber = InstanceManager.getOptionalDefault(LnTcpPreferences.class).orElseGet(() -> {
            return InstanceManager.setDefault(LnTcpPreferences.class, new LnTcpPreferences());
        }).getPort();
    }

    public void setStateListner(ServerListner l) {
        stateListner = l;
    }

    /**
     * Get the default server instance, creating it if necessary.
     *
     * @return the default server instance
     */
    public static synchronized LnTcpServer getDefault() {
        return InstanceManager.getOptionalDefault(LnTcpServer.class).orElseGet(() -> {
            LnTcpServer server = new LnTcpServer();
            return InstanceManager.setDefault(LnTcpServer.class, server);
        });
    }

    /**
     *
     * @return the default server
     * @deprecated since 4.7.5; use {@link #getDefault()} instead
     */
    @Deprecated
    public static synchronized LnTcpServer getInstance() {
        return LnTcpServer.getDefault();
    }

    /**
     * Determine if server will start when created by an action.
     *
     * @return true
     * @deprecated since 4.7.5 without replacement; use the JMRI startup actions
     * mechanisms to control this
     */
    @Deprecated
    public boolean getAutoStart() {
        return true;
    }

    /**
     * Set if server will start when created by an action.
     *
     * @param start ignored
     * @deprecated since 4.7.5 without replacement
     */
    @Deprecated
    public void setAutoStart(boolean start) {
        // do nothing
    }

    /**
     * Get the port the server listens to.
     *
     * @return the port
     * @deprecated since 4.7.5; use
     * {@link jmri.jmrix.loconet.loconetovertcp.LnTcpPreferences#getPort() }
     * instead
     */
    @Deprecated
    public int getPortNumber() {
        return portNumber;
    }

    /**
     * Set the port the server listens to.
     *
     * @param port ignored
     * @deprecated since 4.7.5; use
     * {@link jmri.jmrix.loconet.loconetovertcp.LnTcpPreferences#setPort()}
     * instead
     */
    @Deprecated
    public void setPortNumber(int port) {
        if ((port >= 1) && (port <= 65535)) {
            portNumber = port;
            settingsChanged = true;
            updateServerStateListener();
        }
    }

    public boolean isEnabled() {
        return (socketListener != null) && (socketListener.isAlive());
    }

    public boolean isSettingChanged() {
        return settingsChanged;
    }

    public void enable() {
        if (socketListener == null) {
            socketListener = new Thread(new ClientListener());
            socketListener.setDaemon(true);
            socketListener.setName("LocoNetOverTcpServer");
            log.info("Starting new LocoNetOverTcpServer listener on port " + portNumber);
            socketListener.start();
            updateServerStateListener();
            // advertise over Zeroconf/Bonjour
            if (this.service == null) {
                this.service = ZeroConfService.create("_loconetovertcpserver._tcp.local.", portNumber);
            }
            log.info("Starting ZeroConfService _loconetovertcpserver._tcp.local for LocoNetOverTCP Server");
            this.service.publish();
            if (this.shutDownTask == null) {
                this.shutDownTask = new QuietShutDownTask("LocoNetOverTcpServer") {
                    @Override
                    public boolean execute() {
                        LnTcpServer.getDefault().disable();
                        return true;
                    }
                };
            }
            if (InstanceManager.getNullableDefault(jmri.ShutDownManager.class) != null) {
                InstanceManager.getDefault(jmri.ShutDownManager.class).register(this.shutDownTask);
            }
        }
    }

    public void disable() {
        if (socketListener != null) {
            socketListener.interrupt();
            socketListener = null;
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException ex) {
            }

            updateServerStateListener();

            // Now close all the client connections
            Object[] clientsArray;

            synchronized (clients) {
                clientsArray = clients.toArray();
            }
            for (int i = 0; i < clientsArray.length; i++) {
                ((ClientRxHandler) clientsArray[i]).close();
            }
        }
        this.service.stop();
        InstanceManager.getOptionalDefault(ShutDownManager.class).ifPresent((manager) -> {
            manager.deregister(this.shutDownTask);
        });
    }

    public void updateServerStateListener() {
        if (stateListner != null) {
            stateListner.notifyServerStateChanged(this);
        }
    }

    public void updateClientStateListener() {
        if (stateListner != null) {
            stateListner.notifyClientStateChanged(this);
        }
    }

    class ClientListener implements Runnable {

        @Override
        public void run() {
            Socket newClientConnection;
            String remoteAddress;
            try {
                serverSocket = new ServerSocket(getPortNumber());
                serverSocket.setReuseAddress(true);
                while (!socketListener.isInterrupted()) {
                    newClientConnection = serverSocket.accept();
                    remoteAddress = newClientConnection.getRemoteSocketAddress().toString();
                    log.info("Server: Connection from: " + remoteAddress);
                    addClient(new ClientRxHandler(remoteAddress, newClientConnection));
                }
                serverSocket.close();
            } catch (IOException ex) {
                if (ex.toString().indexOf("socket closed") == -1) {
                    log.error("Server: IO Exception: ", ex);
                }
            }
            serverSocket = null;
        }
    }

    protected void addClient(ClientRxHandler handler) {
        synchronized (clients) {
            clients.add(handler);
        }
        updateClientStateListener();
    }

    protected void removeClient(ClientRxHandler handler) {
        synchronized (clients) {
            clients.remove(handler);
        }
        updateClientStateListener();
    }

    public int getClientCount() {
        synchronized (clients) {
            return clients.size();
        }
    }
    private final static Logger log = LoggerFactory.getLogger(LnTcpServer.class.getName());
}
