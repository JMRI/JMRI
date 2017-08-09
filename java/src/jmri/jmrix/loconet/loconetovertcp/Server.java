package jmri.jmrix.loconet.loconetovertcp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Properties;
import jmri.InstanceManager;
import jmri.implementation.QuietShutDownTask;
import jmri.util.FileUtil;
import jmri.util.zeroconf.ZeroConfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the LocoNetOverTcp LbServer Server Protocol
 *
 * @author Alex Shepherd Copyright (C) 2006
 */
public class Server {

    static Server self;
    LinkedList<ClientRxHandler> clients;
    Thread socketListener;
    ServerSocket serverSocket;
    boolean settingsLoaded = false;
    ServerListner stateListner;
    boolean settingsChanged = false;
    QuietShutDownTask shutDownTask;
    ZeroConfService service = null;
    static final String AUTO_START_KEY = "AutoStart";
    static final String PORT_NUMBER_KEY = "PortNumber";
    static final String SETTINGS_FILE_NAME = "LocoNetOverTcpSettings.ini";

    private Server() {
        clients = new LinkedList<ClientRxHandler>();
    }

    public void setStateListner(ServerListner l) {
        stateListner = l;
    }

    public static synchronized Server getInstance() {
        if (self == null) {
            self = new Server();
            if (self.getAutoStart()) {
                self.enable();
            }
        }
        return self;
    }

    private void loadSettings() {
        if (!settingsLoaded) {
            settingsLoaded = true;
            Properties settings = new Properties();

            String settingsFileName = FileUtil.getUserFilesPath() + SETTINGS_FILE_NAME;

            try {
                log.debug("Server: opening settings file " + settingsFileName);
                java.io.InputStream settingsStream = new FileInputStream(settingsFileName);
                try {
                    settings.load(settingsStream);
                } finally {
                    settingsStream.close();
                }

                String val = settings.getProperty(AUTO_START_KEY, "0");
                autoStart = (val.equals("1"));
                val = settings.getProperty(PORT_NUMBER_KEY, "1234");
                portNumber = Integer.parseInt(val, 10);
            } catch (FileNotFoundException ex) {
                log.debug("Server: loadSettings file not found");
            } catch (IOException ex) {
                log.debug("Server: loadSettings exception: ", ex);
            }
            updateServerStateListener();
        }
    }

    public void saveSettings() {
        // we can't use the store capabilities of java.util.Properties, as
        // they are not present in Java 1.1.8
        String settingsFileName = FileUtil.getUserFilesPath() + SETTINGS_FILE_NAME;
        log.debug("Server: saving settings file " + settingsFileName);

        try {
            OutputStream outStream = new FileOutputStream(settingsFileName);
            PrintStream settingsStream = new PrintStream(outStream);
            settingsStream.println("# LocoNetOverTcp Configuration Settings");
            settingsStream.println(AUTO_START_KEY + " = " + (autoStart ? "1" : "0"));
            settingsStream.println(PORT_NUMBER_KEY + " = " + portNumber);

            settingsStream.flush();
            settingsStream.close();
            settingsChanged = false;
        } catch (FileNotFoundException ex) {
            log.warn("Server: saveSettings exception: ", ex);
        }
        updateServerStateListener();
    }
    private boolean autoStart;

    public boolean getAutoStart() {
        loadSettings();
        return autoStart;
    }

    public void setAutoStart(boolean start) {
        loadSettings();
        autoStart = start;
        settingsChanged = true;
        updateServerStateListener();
    }
    private int portNumber = 1234;

    public int getPortNumber() {
        loadSettings();
        return portNumber;
    }

    public void setPortNumber(int port) {
        loadSettings();
        if ((port >= 1024) && (port <= 65535)) {
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
                        Server.getInstance().disable();
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
        if (this.shutDownTask != null && InstanceManager.getNullableDefault(jmri.ShutDownManager.class) != null) {
            InstanceManager.getDefault(jmri.ShutDownManager.class).deregister(this.shutDownTask);
        }
    }

    public void updateServerStateListener() {
        if (stateListner != null) {
            stateListner.notifyServerStateChanged(this);
        }
    }

    public void updateClinetStateListener() {
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
        updateClinetStateListener();
    }

    protected void removeClient(ClientRxHandler handler) {
        synchronized (clients) {
            clients.remove(handler);
        }
        updateClinetStateListener();
    }

    public int getClientCount() {
        synchronized (clients) {
            return clients.size();
        }
    }
    private final static Logger log = LoggerFactory.getLogger(Server.class.getName());
}
