package jmri.jmrix.loconet.loconetovertcp;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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

    private final List<ClientRxHandler> clients = new LinkedList<>();
    private Thread socketListener;
    private ServerSocket serverSocket;
    private final List<LnTcpServerListener> stateListeners = new ArrayList<>();
    private boolean settingsChanged = false;
    private QuietShutDownTask shutDownTask;
    private ZeroConfService service = null;

    private int portNumber;

    private LnTcpServer() {
        LnTcpPreferences pm = LnTcpPreferences.getDefault();
        portNumber = pm.getPort();
        pm.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            switch (evt.getPropertyName()) {
                case LnTcpPreferences.PORT:
                    // only change the port if stopped
                    if (!isEnabled()) {
                        portNumber = pm.getPort();
                    }
                    break;
                default:
                    // ignore uninteresting property changes
                    break;
            }
        });
    }

    /**
     * Add a state listener to this server.
     *
     * @param l the listener to add
     * @deprecated since 4.7.4; use
     * {@link #addStateListener(jmri.jmrix.loconet.loconetovertcp.LnTcpServerListener)}
     * instead.
     */
    @Deprecated
    public void setStateListner(LnTcpServerListener l) {
        this.addStateListener(l);
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
     * Get the default server instance, creating it if necessary.
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
     * @deprecated since 4.7.5 without replacement; use the JMRI startup actions
     * mechanism to control this
     */
    @Deprecated
    public void setAutoStart(boolean start) {
        // do nothing
    }

    /**
     * Get the port the server listens to.
     *
     * @return the port
     * @deprecated since 4.7.5; use {@link #getPort() }
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
     * {@link jmri.jmrix.loconet.loconetovertcp.LnTcpPreferences#setPort(int) }
     * instead
     */
    @Deprecated
    public void setPortNumber(int port) {
        if ((port >= 1) && (port <= 65535)) {
            portNumber = port;
            settingsChanged = true;
            updateServerStateListeners();
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
            updateServerStateListeners();
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
                        LnTcpServer.this.disable();
                        return true;
                    }
                };
            }
            InstanceManager.getOptionalDefault(jmri.ShutDownManager.class).ifPresent((manager) -> {
                manager.register(this.shutDownTask);
            });
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

            updateServerStateListeners();

            // Now close all the client connections
            Object[] clientsArray;

            synchronized (clients) {
                clientsArray = clients.toArray();
            }
            for (int i = 0; i < clientsArray.length; i++) {
                ((ClientRxHandler) clientsArray[i]).close();
            }
        }
        if (this.service != null) {
            this.service.stop();
        }
        InstanceManager.getOptionalDefault(ShutDownManager.class).ifPresent((manager) -> {
            manager.deregister(this.shutDownTask);
        });
    }

    private void updateServerStateListeners() {
        synchronized (this) {
            this.stateListeners.stream().filter((l) -> (l != null)).forEachOrdered((l) -> {
                l.notifyServerStateChanged(this);
            });
        }
    }

    private void updateClientStateListeners() {
        synchronized (this) {
            this.stateListeners.stream().filter((l) -> (l != null)).forEachOrdered((l) -> {
                l.notifyClientStateChanged(this);
            });
        }
    }

    public void addStateListener(LnTcpServerListener l) {
        this.stateListeners.add(l);
    }

    public boolean removeStateListener(LnTcpServerListener l) {
        return this.stateListeners.remove(l);
    }

    /**
     * Get the port this server is using.
     *
     * @return the port
     */
    public int getPort() {
        return this.portNumber;
    }

    class ClientListener implements Runnable {

        @Override
        public void run() {
            Socket newClientConnection;
            String remoteAddress;
            try {
                serverSocket = new ServerSocket(portNumber);
                serverSocket.setReuseAddress(true);
                while (!socketListener.isInterrupted()) {
                    newClientConnection = serverSocket.accept();
                    remoteAddress = newClientConnection.getRemoteSocketAddress().toString();
                    log.info("Server: Connection from: {}", remoteAddress);
                    addClient(new ClientRxHandler(remoteAddress, newClientConnection));
                }
                serverSocket.close();
            } catch (IOException ex) {
                if (!ex.toString().toLowerCase().contains("socket closed")) {
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
        updateClientStateListeners();
    }

    protected void removeClient(ClientRxHandler handler) {
        synchronized (clients) {
            clients.remove(handler);
        }
        updateClientStateListeners();
    }

    public int getClientCount() {
        synchronized (clients) {
            return clients.size();
        }
    }
    private final static Logger log = LoggerFactory.getLogger(LnTcpServer.class);
}
