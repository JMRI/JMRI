package jmri.jmrix.bidib.tcpserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.Properties;

import jmri.InstanceManager;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.util.FileUtil;
import jmri.util.zeroconf.ZeroConfService;

import org.bidib.jbidibc.net.serialovertcp.NetBidib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMRI Implementation of the BiDiBOverTcp Server Protocol.
 * Starting and Stopping of the server is delegated to the
 * NetPlainTcpBidib class.
 * 
 * There is one server for each BiDiB connection and they must have different port numbers,
 * so the client is connected to a specific BiDiB connection.
 *
 * @author Alex Shepherd Copyright (C) 2006
 * @author Mark Underwood Copyright (C) 2015
 * @author Eckart Meyer Copyright (C) 2023
 */
public class TcpServer {

    final java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrix.bidib.swing.BiDiBSwingBundle"); // NOI18N

    //private final LinkedList<ClientRxHandler> clients = new LinkedList<>();
    private final BiDiBSystemConnectionMemo memo;
    NetPlainTcpBidib netPlainTcpBidib;
    //Thread socketListener;
    ServerSocket serverSocket;
    boolean settingsLoaded = false;
    //ServerListner stateListner;
    boolean settingsChanged = false;
    Runnable shutDownTask;
    ZeroConfService service = null;
    //private boolean autoStart;
    private boolean autoStart = false;

    static final String AUTO_START_KEY = "AutoStart";
    static final String PORT_NUMBER_KEY = "PortNumber";
    static final String SETTINGS_FILE_NAME = "BiDiBOverTcpSettings.ini";

    // private TcpServer() {
    //    log.debug("BiDiB TcpServer started!");
    //    memo = null;
    // }
    
    public TcpServer(BiDiBSystemConnectionMemo memo) {
        this.memo = memo;
        log.debug("BiDiB TcpServer created for {}", memo.getUserName());
    }

//    public void setStateListner(ServerListner l) {
//        stateListner = l;
//    }

    private void loadSettings() {
        if (!settingsLoaded) {
            settingsLoaded = true;
            Properties settings = new Properties();

            String settingsFileName = FileUtil.getUserFilesPath() + SETTINGS_FILE_NAME;

            try {
                log.debug("TcpServer: opening settings file {}", settingsFileName);
                java.io.InputStream settingsStream = new FileInputStream(settingsFileName);
                try {
                    settings.load(settingsStream);
                } finally {
                    settingsStream.close();
                }

                String val = settings.getProperty(AUTO_START_KEY, "0");
                autoStart = (val.equals("1"));
                val = settings.getProperty(PORT_NUMBER_KEY, Integer.toString(NetBidib.BIDIB_UDP_PORT_NUMBER));
                portNumber = Integer.parseInt(val, 10);
            } catch (FileNotFoundException ex) {
                log.debug("TcpServer: loadSettings file not found");
            } catch (IOException ex) {
                log.debug("TcpServer: loadSettings exception: ", ex);
            }
            updateServerStateListener();
        }
    }

    public void saveSettings() {
        // we can't use the store capabilities of java.util.Properties, as
        // they are not present in Java 1.1.8
        // TODO: Use preferences like some other code do. But more important: Provide a GUI !
        String settingsFileName = FileUtil.getUserFilesPath() + SETTINGS_FILE_NAME;
        log.debug("TcpServer: saving settings file {}", settingsFileName);

        try ( OutputStream outStream = new FileOutputStream(settingsFileName);
            PrintStream settingsStream = new PrintStream(outStream); ) {

            settingsStream.println("# BiDiBOverTcp Configuration Settings");
            settingsStream.println(AUTO_START_KEY + " = " + (autoStart ? "1" : "0"));
            settingsStream.println(PORT_NUMBER_KEY + " = " + portNumber);

            settingsStream.flush();
            settingsStream.close();
            settingsChanged = false;
        } catch ( IOException ex) {
            log.warn("TcpServer: saveSettings exception: ", ex);
        }
        updateServerStateListener();
    }

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
    private int portNumber = NetBidib.BIDIB_UDP_PORT_NUMBER;

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
        //return (socketListener != null) && (socketListener.isAlive());
        if (netPlainTcpBidib != null  &&   netPlainTcpBidib.isStarted()) {
            return true;
        }
        return false;
    }            

    public boolean isSettingChanged() {
        return settingsChanged;
    }

    public void enable() {
        if (netPlainTcpBidib == null  ||  !netPlainTcpBidib.isStarted()) {

            log.info("Starting new BiDiBOverTcpServer listener on port {}", portNumber);

            if (netPlainTcpBidib == null) {
                netPlainTcpBidib = new NetPlainTcpBidib(memo.getBiDiBTrafficController());
            }
            netPlainTcpBidib.start(portNumber);
            
            if (netPlainTcpBidib.isStarted()) {
                
                updateServerStateListener();
                
                if (this.shutDownTask == null) {
                    this.shutDownTask = this::disable;
                }
                if (this.shutDownTask != null) {
                    InstanceManager.getDefault(jmri.ShutDownManager.class).register(this.shutDownTask);
                }
            }
            else {
                jmri.util.swing.JmriJOptionPane.showMessageDialog(null, rb.getString("BiDiBOverTCPServerConnectError"), "TCP over BiDiB Server", jmri.util.swing.JmriJOptionPane.ERROR_MESSAGE);
            }
            
//            // advertise over Zeroconf/Bonjour
//            if (this.service == null) {
//                this.service = ZeroConfService.create("_bidibovertcpserver._tcp.local.", portNumber);
//            }
//            log.info("Starting ZeroConfService _bidibovertcpserver._tcp.local for BiDiBOverTCP Server");
//            this.service.publish();

        }
    }

    public void disable() {
        if (netPlainTcpBidib != null) {
            log.info("Stopping BiDiBOverTcpServer listener.");
            
            netPlainTcpBidib.stop();
            netPlainTcpBidib = null;

            updateServerStateListener();

            // Now close all the client connections
//            Object[] clientsArray;

//            synchronized (clients) {
//                clientsArray = clients.toArray();
//            }
//            for (int i = 0; i < clientsArray.length; i++) {
//                ((ClientRxHandler) clientsArray[i]).close();
//            }
        }
        
//        this.service.stop();
        
        if (this.shutDownTask != null) {
            InstanceManager.getDefault(jmri.ShutDownManager.class).deregister(this.shutDownTask);
        }
    }

    public void updateServerStateListener() {
//        if (stateListner != null) {
//            stateListner.notifyServerStateChanged(this);
//        }
    }

    public void updateClientStateListener() {
//        if (stateListner != null) {
//            stateListner.notifyClientStateChanged(this);
//        }
    }

    /**
     * Get access to the system connection memo associated with this traffic
     * controller.
     *
     * @return associated systemConnectionMemo object
     */
    public BiDiBSystemConnectionMemo getSystemConnectionMemo() {
        return (memo);
    }



//    class ClientListener implements Runnable {
//
//        @Override
//        public void run() {
//            Socket newClientConnection;
//            String remoteAddress;
//            try {
//                serverSocket = new ServerSocket(getPortNumber());
//                serverSocket.setReuseAddress(true);
//                while (!socketListener.isInterrupted()) {
//                    newClientConnection = serverSocket.accept();
//                    remoteAddress = newClientConnection.getRemoteSocketAddress().toString();
//                    log.info("TcpServer: Connection from: {}", remoteAddress);
//                    //addClient(new ClientRxHandler(remoteAddress, newClientConnection));
//                }
//                serverSocket.close();
//            } catch (IOException ex) {
//                if (ex.toString().indexOf("socket closed") == -1) {
//                    log.error("TcpServer: IO Exception: ", ex);
//                }
//            }
//            serverSocket = null;
//        }
//    }

//    protected void addClient(ClientRxHandler handler) {
//        synchronized (clients) {
//            clients.add(handler);
//        }
//        updateClientStateListener();
//    }

//    protected void removeClient(ClientRxHandler handler) {
//        synchronized (clients) {
//            clients.remove(handler);
//        }
//        updateClientStateListener();
//    }

//    public int getClientCount() {
//        synchronized (clients) {
//            return clients.size();
//        }
//    }


//    @ServiceProvider(service = InstanceInitializer.class)
//    public static class Initializer extends AbstractInstanceInitializer {
//
//        @Override
//        public <T> Object getDefault(Class<T> type) {
//            if (type.equals(TcpServer.class)) {
//                TcpServer instance = new TcpServer();
//                if (instance.getAutoStart()) {
//                    instance.enable();
//                }
//                return instance;
//            }
//            return super.getDefault(type);
//        }
//
//        @Override
//        public Set<Class<?>> getInitalizes() {
//            Set<Class<?>> set = super.getInitalizes();
//            set.add(TcpServer.class);
//            return set;
//        }
//    }

    private final static Logger log = LoggerFactory.getLogger(TcpServer.class);

}
