package jmri.jmrix.bidib.serialdriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Set;
import jmri.util.SystemType;
import jmri.jmrix.bidib.BiDiBSerialPortController;
import jmri.jmrix.bidib.BiDiBTrafficController;
import org.bidib.jbidibc.core.BidibFactory;
import org.bidib.jbidibc.core.BidibInterface;
import org.bidib.jbidibc.core.MessageListener;
import org.bidib.jbidibc.core.node.listener.TransferListener;
import org.bidib.jbidibc.core.NodeListener;
import org.bidib.jbidibc.messages.exception.PortNotFoundException;
import org.bidib.jbidibc.messages.exception.PortNotOpenedException;
import org.bidib.jbidibc.messages.helpers.DefaultContext;
import org.bidib.jbidibc.core.node.BidibNode;
import org.bidib.jbidibc.messages.ConnectionListener;
import org.bidib.jbidibc.messages.helpers.Context;
import org.bidib.jbidibc.messages.utils.ByteUtils;
import org.bidib.jbidibc.jserialcomm.JSerialCommSerialBidib;
//import org.bidib.jbidibc.jserialcomm.PortIdentifierUtils;
//import org.bidib.jbidibc.purejavacomm.PureJavaCommSerialBidib;
//import org.bidib.jbidibc.purejavacomm.PortIdentifierUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the BiDiB system.
 * <p>
 * This connects an BiDiB device via a serial com port.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Eckart Meyer Copyright (C) 2019-2024
 */
public class SerialDriverAdapter extends BiDiBSerialPortController {

    private static final boolean useJSerailComm = true;
    private static final boolean usePurjavacomm = !useJSerailComm;
    private static final boolean useScm = false;
    private static final Map<String, Long> connectionRootNodeList = new HashMap<>(); //our static connection list
    
    protected String portNameFilter = "";
    protected Long rootNodeUid;
    protected boolean useAutoScan = false;
    
//    @SuppressWarnings("OverridableMethodCallInConstructor")
    public SerialDriverAdapter() {
        //super(new BiDiBSystemConnectionMemo());
        setManufacturer(jmri.jmrix.bidib.BiDiBConnectionTypeList.BIDIB);
        configureBaudRate(validSpeeds[0]);
        
        if (SystemType.isLinux()) {
            //portNameFilter = "/dev/ttyUSB*";
            portNameFilter = "ttyUSB*";
            //portNameFilter = "/dev/tty*";
        }
        //test
        List<String> portList = getPortIdentifiers();
        log.info("portList: {}", portList);
    }
    
    /**
     * Get the filter string for port names to scan when autoScan is on
     * @return port name filter as a string (wildcard is allowed at the end)
     */
    public String getPortNameFilter() {
        return portNameFilter;
    }
    
    /**
     * Set the port name filter
     * @param filter filter string
     */
    public void setPortNameFilter(String filter) {
        portNameFilter = filter;
    }
    
    /**
     * Get the root node unique ID
     * @return UID as Long
     */
    public Long getRootNodeUid() {
        return rootNodeUid;
    }
    
    /**
     * Set the root node unique ID
     * @param uid Unique ID as Long
     */
    public void setRootNodeUid(Long uid) {
        rootNodeUid = uid;
    }
    
    /**
     * Get the AutoScan status
     * @return true of autoScan is on, false if not
     */
    public boolean getUseAutoScan() {
        return useAutoScan;
    }
    
    /**
     * Set the AutoScan status
     * @param flag true of ON is requested
     */
    public void setUseAutoScan(boolean flag) {
        useAutoScan = flag;
    }
    
    /**
     * {@inheritDoc}
     * 
     * Get the port name in the format which is used by jbidibc
     * @return real port name
     */
    @Override
    public String getRealPortName() {
        return getRealPortName(getCurrentPortName());
    }

    /**
     * Get the canonical port name from the underlying operating system.
     * For a symbolic link, the real path is returned.
     * 
     * @param portName human-readable name
     * @return canonical path
     */
    static public String getCanonicalPortName(String portName) {
        File file = new File(portName);
        if (file.exists()) {
            try {
                portName = file.getCanonicalPath();
                log.debug("Canonical port name: {}", portName);
            }
            catch (IOException ex) {
            }
        }
        return portName;
    }
    
    /**
     * Static function to get the port name in the format which is used by jbidibc
     * @param portName displayed port name
     * @return real port name
     */
    static public String getRealPortName(String portName) {
        if (SystemType.isLinux()) {
            portName = "/dev/" + portName;
        }
        // TODO: MaxOSX. Windows just uses the displayed port name (COMx:)
        return getCanonicalPortName(portName);
    }
    
    /**
     * This methods is called from serial connection config and creates the BiDiB object from jbidibc and opens it.
     * The connectPort method of the traffic controller is called for generic initialisation.
     * 
     * @param portName port name from XML
     * @param appName not used
     * @return error string to be displayed by JMRI. null of no error
     */
    @Override
    public String openPort(String portName, String appName) {
        log.debug("openPort called for {}, driver: {}, expected UID: {}", portName, getRealPortName(), ByteUtils.formatHexUniqueId(rootNodeUid));
        
        MSG_RAW_LOGGER.debug("RAW> create BiDiB Instance for port {}", getCurrentPortName());
        //BidibInterface bidib = createSerialBidib();
        if (useAutoScan) {
            String err = findPortbyUniqueID(rootNodeUid);//returns known port in "portName" or scan all ports for the requested unique ID of the root node
            if (err != null) {
                if (bidib != null) {
                    bidib.close();
                    bidib = null;
                }
                return err;
            }
        }
        log.debug("port table: {}", connectionRootNodeList);
        //bidib.close(); //we can leave it open (creating a new one is time consuming!) - tc.connect then will skip the internal open then
        if (bidib == null) {
            context = getContext();
            bidib = createSerialBidib(context);
        }
        BiDiBTrafficController tc = new BiDiBTrafficController(bidib);
        context  = tc.connnectPort(this); //must be done before configuring managers since they may need features from the device
        log.debug("memo: {}", this.getSystemConnectionMemo());
        this.getSystemConnectionMemo().setBiDiBTrafficController(tc);
        if (context != null) {
            opened = true;
            Long uid = tc.getRootNode().getUniqueId() & 0x0000ffffffffffL; //mask the classid
            if (context.get("serial.baudrate") != null) {
                // Bidib serial controller has Auto-Baud with two baudrates: 115200 and 19200. Bidib specified only those two.
                // If the controller has already an open connection, the baudrate is not set in context but it should have been configured before when it was opened
                log.debug("opened with baud rate {}", context.get("serial.baudrate"));
                configureBaudRateFromNumber(context.get("serial.baudrate").toString());
            }
            if (rootNodeUid != null  &&  !uid.equals(rootNodeUid)) {
                opened = false;
                connectionRootNodeList.remove(getRealPortName());
                tc.getBidib().close(); //wrong UID close to make it available for other checks
                return "Device found on port " + getRealPortName() + "(" + getCurrentPortName() + ") has Unique ID " + ByteUtils.formatHexUniqueId(uid) + ", but should be " + ByteUtils.formatHexUniqueId(rootNodeUid);
            }
            connectionRootNodeList.put(getRealPortName(), tc.getRootNode().getUniqueId() & 0x0000ffffffffffL);
            setRootNodeUid(uid);
        }
        else {
            opened = false;
            connectionRootNodeList.put(getRealPortName(), null); //this port does not have a BiDiB device connected - remember this
            return "No device found on port " + getCurrentPortName() + "(" + getCurrentPortName() + ")";
        }
        
        return null; // indicates OK return
//        return "CANT DO!!"; //DEBUG

    }

    /**
     * Set up all of the other objects to operate with an BiDiB command station
     * connected to this port.
     */
    @Override
    public void configure() {
        log.debug("configure");
        this.getSystemConnectionMemo().configureManagers();
    }
    
    /**
     * Create a BiDiB object. jbidibc has support for various serial implementations.
     * We tested SCM and PUREJAVACOMM. Both worked without problems. Since
     * JMRI generally uses PUREJAVACOMM, we also use it here
     * 
     * @return a BiDiB object from jbidibc
     */
    static private BidibInterface createSerialBidib(Context context) {
        if (useScm) {
//            return BidibFactory.createBidib(ScmSerialBidib.class.getName());
        }
        if (usePurjavacomm) {
//            return BidibFactory.createBidib(PureJavaCommSerialBidib.class.getName(), context);
        }
        if (useJSerailComm) {
            return BidibFactory.createBidib(JSerialCommSerialBidib.class.getName(), context);
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void registerAllListeners(ConnectionListener connectionListener, Set<NodeListener> nodeListeners,
                Set<MessageListener> messageListeners, Set<TransferListener> transferListeners) {
        
        if (useScm) { //NOT SUPPORTED ANY MORE
//            PureJavaCommSerialBidib b = (ScmSerialBidib)bidib;
//            b.setConnectionListener(connectionListener);
//            b.registerListeners(nodeListeners, messageListeners, transferListeners);
        }
        if (usePurjavacomm) {
//            PureJavaCommSerialBidib b = (PureJavaCommSerialBidib)bidib;
//            b.setConnectionListener(connectionListener);
//            b.registerListeners(nodeListeners, messageListeners, transferListeners);
        }
        if (useJSerailComm) {
            JSerialCommSerialBidib b = (JSerialCommSerialBidib)bidib;
            b.setConnectionListener(connectionListener);
            b.registerListeners(nodeListeners, messageListeners, transferListeners);
        }
    }
    
    /**
     * Get a list of available port names
     * @return list of portnames
     */
    /*static - no longer, since we need portNameFilter here */
    public List<String> getPortIdentifiers() {
        List<String> ret = null;
        List<String> list = null;
//        if (useScm) {
//            list = new ArrayList<>();
//            for (String s : ScmPortIdentifierUtils.getPortIdentifiers()) {
//                list.add(s.replace("/dev/", ""));
//            }
//        }
        if (usePurjavacomm) {
            //list = org.bidib.jbidibc.purejavacomm.PortIdentifierUtils.getPortIdentifiers();
        }
        if (useJSerailComm) {
            list = org.bidib.jbidibc.jserialcomm.PortIdentifierUtils.getPortIdentifiers();
        }
        if (list != null) {
            ret = new ArrayList<>();
            String portPrefix = portNameFilter.replaceAll("\\*", "");
            log.trace("port name filter: {}", portPrefix);
            for (String s : list) {
                if (s.startsWith(portPrefix)) {
                    ret.add(s);
                }
            }
        }
        return ret;
    }
    
    /**
     * Internal method to find a port, possibly with already created BiDiB object
     * @param requid requested unique ID of the root node
     * @return port name as String
     */
    //private String findPortbyUniqueID(Long requid, BidibInterface bidib) {
    public String findPortbyUniqueID(Long requid) {
        // find the port for the given UID
        // first check our static if the port was already seen.
        String port = getKownPortName(requid);
        if (port == null) {
            // then try the given port if it has the requested UID
            if (!getCurrentPortName().isEmpty()) {
                if (!connectionRootNodeList.containsKey(getRealPortName())) {
                    //if (bidib == null) {
                    //    bidib = createSerialBidib();
                    //}
                    //Long uid = checkPort(bidib, getCurrentPortName());
                    Long uid = checkPort(getCurrentPortName());
                    if (uid != null  &&  uid.equals(requid)) {
                        port = getCurrentPortName();
                    }
                }
            }
        }
        if (port == null) {
            // if still not found, we have to scan all known ports
            //if (bidib == null) {
            //    bidib = createSerialBidib();
            //}
            //port = scanPorts(bidib, requid, portNameFilter);
            port = scanPorts(requid, portNameFilter);
        }
        if (port != null) {
            setPort(port);
        }
        else {
            if (bidib != null) {
                bidib.close();
                bidib = null;
            }
            if (requid != null) {
                return "No Device found for BiDiB Unique ID " + ByteUtils.formatHexUniqueId(requid);
            }
            else if (!getCurrentPortName().isEmpty()) {
                return "No Device found on port " + getCurrentPortName();
            }
            else {
                return "port name or Unique ID not specified!";
            }
        }
        return null;
    }
    
    /**
     * Scan all ports (filtered by portNameFilter) for a unique ID of the root node.
     * 
     * @param requid requested unique ID of the root node
     * @param portNameFilter a port name filter (e.g. /dev/ttyUSB* for Linux)
     * @return found port name (e.g. /dev/ttyUSB0) or null of not found
     */
    //static private String scanPorts(BidibInterface bidib, Long requid, String portNameFilter) {
    private String scanPorts(Long requid, String portNameFilter) {
        //String portPrefix = portNameFilter.replaceAll("\\*", "");
        log.trace("scanPorts for UID {}, filter: {}", ByteUtils.formatHexUniqueId(requid), portNameFilter);
        List<String> portNameList = getPortIdentifiers();
        for (String portName : portNameList) {
            //log.trace("check port {}", portName);
            //if (portName.startsWith(portPrefix)) {
                log.trace("check port {}", portName);
                if (!connectionRootNodeList.containsKey(getRealPortName(portName))) {
                    log.debug("BIDIB: try port {}", portName);
                    //Long uid = checkPort(bidib, portName);
                    Long uid = checkPort(portName);
                    if (uid.equals(requid)) {
                        return portName;
                    }
                }
            //}
        }
        return null;
    }
    
    /**
     * Check if the given port is a BiDiB connection and returns the unique ID of the root node.
     * Return the UID from cache if we already know the UID.
     * 
     * @param portName port name to check
     * @return unique ID of the root node
     */
//    public Long checkPort(String portName) {
//        if (connectionRootNodeList.containsKey(portName)) {
//            return connectionRootNodeList.get(portName);
//        }
//        else {
//            BidibInterface bidib = createSerialBidib();
//            Long uid = checkPort(bidib, portName);
//            bidib.close();
//            return uid;
//        }
//    }

    /**
     * Internal method to check if the given port is a BiDiB connection and returns the unique ID of the root node.
     * Return the UID from cache if we already know the UID.
     * 
//     * @param bidib a BiDiB object from jbidibc
     * @param portName port name to check
     * @return unique ID of the root node
     */
    //private Long checkPort(BidibInterface bidib, String portName) {
    public Long checkPort(String portName) {
        Long uid = null;
        try {
            context = new DefaultContext();
//            if (bidib.isOpened()) {
//                bidib.close();
//            }
//            bidib.close();
            log.trace("checkPort: port name: {}, real port name: {}", portName, getRealPortName(portName));
            if (bidib != null) {
                bidib.close();
                bidib = null;
            }
            bidib = createSerialBidib(context);
            String realPortName = getRealPortName(portName);
            bidib.open(realPortName, null, Collections.<NodeListener> emptySet(), Collections.<MessageListener> emptySet(), Collections.<TransferListener> emptySet(), context);
            BidibNode rootNode = bidib.getRootNode();
            
            uid = rootNode.getUniqueId() & 0x0000ffffffffffL; //mask the classid
            log.info("root node UID: {}", ByteUtils.formatHexUniqueId(uid));
            connectionRootNodeList.put(realPortName, uid);
            
        }
        catch (PortNotOpenedException ex) {
            log.warn("port not opened: {}", portName);
        }
        catch (PortNotFoundException ex) {
            log.warn("port not found 1: {}", portName);
        }
        catch (Exception ex) {
            log.warn("port not found 2: {}", portName);
        }
        if (uid != null) {
            bidib.close();
            bidib = null;
        }
        return uid;
    }
    
    /**
     * Check if the port name of a given UID already exists in the cache.
     * 
     * @param reqUid requested UID
     * @return port name or null if not found in cache
     */
    public String getKownPortName(Long reqUid) {
        for(Map.Entry<String, Long> entry : connectionRootNodeList.entrySet()) {
            Long uid = entry.getValue();
            if (uid.equals(reqUid)) {
                File f = new File(entry.getKey());
                String portName = f.getName();
                //return entry.getKey();
                return portName;
            }
        }
        return null;
    }

    

    // base class methods for the BiDiBSerialPortController interface
    // not used but must be implemented

    @Override
    public DataInputStream getInputStream() {
//        if (!opened) {
//            log.error("getInputStream called before load(), stream not available");
//            return null;
//        }
//        return new DataInputStream(serialStream);
        return null;
    }

    @Override
    public DataOutputStream getOutputStream() {
//        if (!opened) {
//            log.error("getOutputStream called before load(), stream not available");
//        }
//        try {
//            return new DataOutputStream(activeSerialPort.getOutputStream());
//        } catch (java.io.IOException e) {
//            log.error("getOutputStream exception: " + e);
//        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        return Arrays.copyOf(validSpeeds, validSpeeds.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return Arrays.copyOf(validSpeedValues, validSpeedValues.length);
    }

    // see Bidib SCM serial controller - only those two baud rates are specified
    protected String[] validSpeeds = new String[]{Bundle.getMessage("Baud115200"),
            Bundle.getMessage("Baud19200")};
    protected int[] validSpeedValues = new int[]{115200, 19200};
    protected String selectedSpeed = validSpeeds[0];


    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);
    private static final Logger MSG_RAW_LOGGER = LoggerFactory.getLogger("RAW");

}
