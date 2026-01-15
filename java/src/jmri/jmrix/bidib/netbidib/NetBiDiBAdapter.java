package jmri.jmrix.bidib.netbidib;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import jmri.util.FileUtil;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
import javax.jmdns.ServiceInfo;

//import jmri.InstanceManager;
import jmri.jmrix.bidib.BiDiBNetworkPortController;
import jmri.jmrix.bidib.BiDiBPortController;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.jmrix.bidib.BiDiBTrafficController;
import jmri.util.zeroconf.ZeroConfClient;
//import jmri.util.zeroconf.ZeroConfServiceManager;

import org.bidib.jbidibc.core.MessageListener;
import org.bidib.jbidibc.core.NodeListener;
import org.bidib.jbidibc.core.node.listener.TransferListener;
import org.bidib.jbidibc.messages.ConnectionListener;
import org.bidib.jbidibc.netbidib.client.NetBidibClient;
import org.bidib.jbidibc.netbidib.client.BidibNetAddress;
import org.bidib.jbidibc.netbidib.pairingstore.LocalPairingStore;
import org.bidib.jbidibc.messages.Node;
import org.bidib.jbidibc.messages.enums.NetBidibRole;
import org.bidib.jbidibc.messages.helpers.Context;
import org.bidib.jbidibc.messages.message.netbidib.NetBidibLinkData;
import org.bidib.jbidibc.messages.message.netbidib.NetBidibLinkData.PartnerType;
import org.bidib.jbidibc.messages.utils.ByteUtils;
import org.bidib.jbidibc.messages.ProtocolVersion;
import org.bidib.jbidibc.messages.enums.PairingResult;
import org.bidib.jbidibc.messages.helpers.DefaultContext;
import org.bidib.jbidibc.netbidib.NetBidibContextKeys;
import org.bidib.jbidibc.netbidib.client.pairingstates.PairingStateEnum;
import org.bidib.jbidibc.netbidib.pairingstore.PairingStore;
import org.bidib.jbidibc.netbidib.pairingstore.PairingStoreEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements BiDiBPortController for the netBiDiB system network
 * connection.
 *
 * @author Eckart Meyer Copyright (C) 2024-2025
 *
 * mDNS code based on LIUSBEthernetAdapter.
 */
public class NetBiDiBAdapter extends BiDiBNetworkPortController {

    public static final String NET_BIDIB_DEFAULT_PAIRING_STORE_FILE = "preference:netBiDiBPairingStore.bidib";
    static final String OPTION_DEVICE_LIST = "AvailableDeviceList";
    static final String OPTION_UNIQUE_ID = "UniqueID";
    
    // The PID (product id as part of the Unique ID) was registered for JMRI with bidib.org (thanks to Andreas Kuhtz and Wolfgang Kufer)
    static final int BIDIB_JMRI_PID = 0x00FE; //don't touch without synchronizing with bidib.org
    
    private final Map<Long, NetBiDiDDevice> deviceList = new LinkedHashMap<>();
    private boolean mDNSConfigure = false;
    private final javax.swing.Timer delayedCloseTimer;
    private PairingStore pairingStore = null;
    private NetBiDiBPairingRequestDialog pairingDialog = null;
    private ActionListener pairingListener = null;
    
    private Long uniqueId = null; //also used as mDNS advertisement name
    long timeout;
    private ZeroConfClient mdnsClient = null;

    private final BiDiBPortController portController = this; //this instance is used from a listener class
    
    protected static class NetBiDiDDevice {
        private PairingStoreEntry pairingStoreEntry = new PairingStoreEntry();
        private BidibNetAddress bidibAddress = null;

        public NetBiDiDDevice() {
        }
        
        public PairingStoreEntry getPairingStoreEntry() {
            return pairingStoreEntry;
        }
        public void setPairingStoreEntry(PairingStoreEntry pairingStoreEntry) {
            this.pairingStoreEntry = pairingStoreEntry;
            //uniqueID = ByteUtils.parseHexUniqueId(pairingStoreEntry.getUid());
        }
        
        public Long getUniqueId() {
            //return ByteUtils.parseHexUniqueId(pairingStoreEntry.getUid());
            //return uniqueID & 0xFFFFFFFFFFL;
            return ByteUtils.parseHexUniqueId(pairingStoreEntry.getUid()) & 0xFFFFFFFFFFL;
        }
        public void setUniqueId(Long uid) {
            pairingStoreEntry.setUid(ByteUtils.formatHexUniqueId(uid));
        }
        
        public void setAddressAndPort(String addr, String port) {
            InetAddress address = null;
            try {
                address = InetAddress.getLocalHost(); //be sure there is a valid address
                address = InetAddress.getByName(addr);
            }
            catch (UnknownHostException e) {
                log.error("unable to resolve remote server address {}:", e.toString());
            }
            int portAsInt;
            try {
               portAsInt = Integer.parseInt(port);
            }
            catch (NumberFormatException e) {
               portAsInt = 0;
            }
            bidibAddress = new BidibNetAddress(address, portAsInt);
        }
        public InetAddress getAddress() {
            return (bidibAddress == null) ? InetAddress.getLoopbackAddress() : bidibAddress.getAddress();
        }
        public int getPort() {
            return (bidibAddress == null) ? 0 : bidibAddress.getPortNumber();
        }
        public void setAddress(InetAddress addr) {
            if (addr == null) {
                bidibAddress = null;
            }
            else {
                bidibAddress = new BidibNetAddress(addr, getPort());
            }
        }
        public void setPort(int port) {
            bidibAddress = new BidibNetAddress(getAddress(), port);
        }
        
        public String getProductName() {
            return pairingStoreEntry.getProductName();
        }
        
        public void setProductName(String productName) {
            pairingStoreEntry.setProductName(productName);
        }

        public String getUserName() {
            return pairingStoreEntry.getUserName();
        }
        
        public void setUserName(String userName) {
            pairingStoreEntry.setUserName(userName);
        }

        public boolean isPaired() {
            return pairingStoreEntry.isPaired();
        }
        
        public void setPaired(boolean paired) {
            pairingStoreEntry.setPaired(paired);
        }

        public String getString() {
            String s = pairingStoreEntry.getUserName()
                    + " (" + pairingStoreEntry.getProductName()
                    + ", " + ByteUtils.getUniqueIdAsString(getUniqueId());
            if (bidibAddress != null) {
                s +=  ", " + bidibAddress.getAddress().toString();
                if (getPort() != 0) {
                    s += ":" + String.valueOf(getPort());
                }
            }
            if (pairingStoreEntry.isPaired()) {
                s +=  ", paired";
            }
            s += ")";
            return s;
        }
    }

    public NetBiDiBAdapter() {
        //super(new BiDiBSystemConnectionMemo());
        setManufacturer(jmri.jmrix.bidib.BiDiBConnectionTypeList.BIDIB);
        delayedCloseTimer = new javax.swing.Timer(1000, e -> bidib.close() );
        delayedCloseTimer.setRepeats(false);
        try {
            pairingStore = new LocalPairingStore(FileUtil.getFile(NET_BIDIB_DEFAULT_PAIRING_STORE_FILE));
        }
        catch (FileNotFoundException ex) {
            log.warn("pairing store file is invalid: {}", ex.getMessage());
        }
        //deviceListAddFromPairingStore();
        
        options.put("ConnectionKeepAlive", new Option(Bundle.getMessage("KeepAlive"),
                new String[]{Bundle.getMessage("KeepAliveLocalPing"),Bundle.getMessage("KeepAliveNone")} )); // NOI18N
    }
    
    public void deviceListAddFromPairingStore() {
        pairingStore.load();
        List<PairingStoreEntry> entries = pairingStore.getPairingStoreEntries();
        for (PairingStoreEntry pe : entries) {
            log.debug("Pairing store entry: {}", pe);
            Long uid = ByteUtils.parseHexUniqueId(pe.getUid()) & 0xFFFFFFFFFFL;
            NetBiDiDDevice dev = deviceList.get(uid);
//            if (dev == null) {
//                dev = new NetBiDiDDevice();
//                dev.setPairingStoreEntry(pe);
//            }
            if (dev != null) {
                dev.setPaired(pe.isPaired());
                deviceList.put(uid, dev);
            }
        }
    }
    
    @Override
    public void connect(String host, int port) throws IOException {
        setHostName(host);
        setPort(port);
        connect();
    }

    /**
     * This methods is called from network connection config. It creates the BiDiB object from jbidibc and opens it.
     * The connectPort method of the traffic controller is called for generic initialisation.
     * 
     */
    @Override
    public void connect() {// throws IOException {
        log.debug("connect() starts to {}:{}", getHostName(), getPort());
        
        opened = false;
        
        prepareOpenContext();
        
        // create the BiDiB instance
        bidib = NetBidibClient.createInstance(getContext());
        // create the correspondent traffic controller
        BiDiBTrafficController tc = new BiDiBTrafficController(bidib);
        this.getSystemConnectionMemo().setBiDiBTrafficController(tc);
        
        log.debug("memo: {}, netBiDiB: {}", this.getSystemConnectionMemo(), bidib);
        
        // connect to the device
        context = tc.connnectPort(this); //must be done before configuring managers since they may need features from the device

        opened = false;
        if (context != null) {
            opened = true;
        }
        else {
            //opened = false;
            log.warn("No device found on port {} ({}})",
                    getCurrentPortName(), getCurrentPortName());
        }
        
// DEBUG!
//        final NetBidibLinkData clientLinkData = ctx.get(Context.NET_BIDIB_CLIENT_LINK_DATA, NetBidibLinkData.class, null);
//        try {
//            bidib.detach(clientLinkData.getUniqueId());
//            //int magic = bidib.getRootNode().getMagic(0);
//            //log.debug("Root Node returned magic: 0x{}", ByteUtils.magicToHex(magic));
//            bidib.attach(clientLinkData.getUniqueId());
//            int magic2 = bidib.getRootNode().getMagic(0);
//            log.debug("Root Node returned magic: 0x{}", ByteUtils.magicToHex(magic2));
//        }
//        catch (Exception e) {
//            log.warn("get magic failed!");
//        }
// /DEBUG!

    }
    
    private void prepareOpenContext() {
        if (getContext() == null) {
            context = new DefaultContext();
        }
        Context ctx = getContext();

        // Register a local file pairingstore into context
        try {
            PairingStore pairingStore = new LocalPairingStore(FileUtil.getFile(NET_BIDIB_DEFAULT_PAIRING_STORE_FILE));
            pairingStore.load();
            ctx.register(Context.PAIRING_STORE, pairingStore);
        }
        catch (FileNotFoundException ex) {
            log.warn("pairing store file is invalid: {}", ex.getMessage());
        }

        final NetBidibLinkData providedClientLinkData =
                ctx.get(Context.NET_BIDIB_CLIENT_LINK_DATA, NetBidibLinkData.class, null);

        if (providedClientLinkData == null) { //if the context is not already set (not possible so far...)

                final NetBidibLinkData localClientLinkData = new NetBidibLinkData(PartnerType.LOCAL);
                localClientLinkData.setRequestorName("BiDiB-JMRI-Client"); //Must start with "BiDiB" since this is the begin of MSG_LOCAL_PROTOCOL_SIGNATURE
                //localClientLinkData.setUniqueId(ByteUtils.convertUniqueIdToLong(uniqueId));
                localClientLinkData.setUniqueId(this.getNetBidibUniqueId());
                localClientLinkData.setProdString("JMRI");
                // Always set the pairing timeout.
                // There is a default in the jbibibc library, but we can't get the value.
                localClientLinkData.setRequestedPairingTimeout(20);
                // set netBiDiB username to the hostname of the local machine.
                // TODO: make this a user settable connection preference field
                try {
                    String myHostName = InetAddress.getLocalHost().getHostName();
                    log.debug("setting netBiDiB username to local hostname: {}", myHostName);
                    localClientLinkData.setUserString(myHostName);
                }
                catch (UnknownHostException ex) {
                    log.warn("Cannot determine local host name: {}", ex.toString());
                }
                localClientLinkData.setProtocolVersion(ProtocolVersion.VERSION_0_8);
                localClientLinkData.setNetBidibRole(NetBidibRole.INTERFACE);

                //localClientLinkData.setRequestedPairingTimeout(netBidibSettings.getPairingTimeout()); TODO use default for now

                log.info("Register the created client link data in the create context: {}", localClientLinkData);
                ctx.register(Context.NET_BIDIB_CLIENT_LINK_DATA, localClientLinkData);
            
        }
        
        ctx.register(BiDiBTrafficController.ASYNCCONNECTIONINIT, true); //netBiDiB uses asynchroneous initialization
        ctx.register(BiDiBTrafficController.ISNETBIDIB, true);
        ctx.register(BiDiBTrafficController.USELOCALPING, getOptionState("ConnectionKeepAlive").equals(Bundle.getMessage("KeepAliveLocalPing")));

        log.debug("Context: {}", ctx);
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        log.debug("configure");
        this.getSystemConnectionMemo().configureManagers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void closeConnection() {
        BiDiBTrafficController tc = this.getSystemConnectionMemo().getBiDiBTrafficController();
        if (tc != null) {
            tc.getBidib().close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerAllListeners(ConnectionListener connectionListener, Set<NodeListener> nodeListeners,
                Set<MessageListener> messageListeners, Set<TransferListener> transferListeners) {
        
        NetBidibClient b = (NetBidibClient)bidib;
        b.setConnectionListener(connectionListener);
        b.registerListeners(nodeListeners, messageListeners, transferListeners);
    }
    
    /**
     * Get a unique id for ourself. The product id part is fixed and registered with bidib.org.
     * The serial number is a hash from the MAC address.
     * 
     * This is a variation of org.bidib.wizard.core.model.settings.NetBidibSettings.getNetBidibUniqueId().
     * Instead of just using the network interface from InetAddress.getLocalHost() - which can result to the loopback-interface,
     * which does not have a hardware address - we loop through the list of interfaces until we find an interface which is up and
     * not a loopback. It would be even better, if we check for virtual interfaces (those could be present if VMs run on the machine)
     * and then exclude them. But there is no generic method to find those interfaces. So we just return an UID derived from the first
     * found non-loopback interface or the default UID if there is no such interface.
     * 
     * @return Unique ID as long
     */
    public Long getNetBidibUniqueId() {
        // set a default UID
        byte[] uniqueId = 
                new byte[] { 0x00, 0x00, 0x0D, ByteUtils.getLowByte(BIDIB_JMRI_PID), ByteUtils.getHighByte(BIDIB_JMRI_PID),
                    0x00, (byte) 0xE8 };

        // try to generate the uniqueId from a mac address
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface networkInterface = nis.nextElement();
                // Check if the interface is up and not a loopback
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    byte[] hardwareAddress = networkInterface.getHardwareAddress();
                    if (hardwareAddress != null) {
                        String[] hexadecimal = new String[hardwareAddress.length];
                        for (int i = 0; i < hardwareAddress.length; i++) {
                            hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
                        }
                        String macAddress = String.join("", hexadecimal);
                        log.debug("MAC address used to generate an UID: {} from interface {}", macAddress, networkInterface.getDisplayName());
                        int hashCode = macAddress.hashCode();

                        uniqueId =
                            new byte[] { 0x00, 0x00, 0x0D, ByteUtils.getLowByte(BIDIB_JMRI_PID), ByteUtils.getHighByte(BIDIB_JMRI_PID),
                                ByteUtils.getHighByte(hashCode), ByteUtils.getLowByte(hashCode) };

                        log.info("Generated netBiDiB uniqueId from the MAC address: {}",
                                ByteUtils.convertUniqueIdToString(uniqueId));
                        break;
                    }
                    else {
                        log.warn("No hardware address for localhost available. Use default netBiDiB uniqueId.");
                    }
                }
            }
        }
        catch (Exception ex) {
            log.warn("Generate the netBiDiB uniqueId from the MAC address failed.", ex);
        }
        return ByteUtils.convertUniqueIdToLong(uniqueId);
    }

    // base class methods for the BiDiBNetworkPortController interface
    // not used but must be implemented

    @Override
    public DataInputStream getInputStream() {
        return null;
    }

    @Override
    public DataOutputStream getOutputStream() {
        return null;
    }
    
    // autoconfig via mDNS

    /**
     * Set whether or not this adapter should be
     * configured automatically via MDNS.
     *
     * @param autoconfig boolean value.
     */
    @Override
    public void setMdnsConfigure(boolean autoconfig) {
        log.debug("Setting netBiDiB adapter autoconfiguration to: {}", autoconfig);
        mDNSConfigure = autoconfig;
    }
    
    /**
     * Get whether or not this adapter is configured
     * to use autoconfiguration via MDNS.
     *
     * @return true if configured using MDNS.
     */
    @Override
    public boolean getMdnsConfigure() {
        return mDNSConfigure;
    }
    
    /**
     * Set the server's host name and port
     * using mdns autoconfiguration.
     */
    @Override
    public void autoConfigure() {
        log.info("Configuring BiDiB interface via JmDNS");
        //if (getHostName().equals(DEFAULT_IP_ADDRESS)) {
        //    setHostName(""); // reset the hostname to none.
        //}
        log.debug("current host address: {} {}, port: {}, UniqueID: {}", getHostAddress(), getHostName(), getPort(), ByteUtils.formatHexUniqueId(getUniqueId()));
        String serviceType = Bundle.getMessage("defaultMDNSServiceType");
        log.debug("Listening for mDNS service: {}", serviceType);
        if (getUniqueId() != null) {
            log.info("try to find mDNS announcement for unique id: {} (IP: {})", ByteUtils.getUniqueIdAsString(getUniqueId()), getHostName());
        }

// the folowing selections are valid only for a zeroconf server, the client does NOT use them...
//        ZeroConfServiceManager mgr = InstanceManager.getDefault(ZeroConfServiceManager.class);
//        mgr.getPreferences().setUseIPv6(false);
//        mgr.getPreferences().setUseLinkLocal(false);
//        mgr.getPreferences().setUseLoopback(false);

        if (mdnsClient == null) {
            mdnsClient = new ZeroConfClient();
            mdnsClient.startServiceListener(serviceType);
            timeout = mdnsClient.getTimeout(); //the original default timeout
        }
        // leave the wait code below commented out for now.  It
        // does not appear to be needed for proper ZeroConf discovery.
        //try {
        //  synchronized(mdnsClient){
        //  // we may need to add a timeout here.
        //  mdnsClient.wait(keepAliveTimeoutValue);
        //  if(log.isDebugEnabled()) mdnsClient.listService(serviceType);
        //  }
        //} catch(java.lang.InterruptedException ie){
        //  log.error("MDNS auto Configuration failed.");
        //  return;
        //}
        List<ServiceInfo> infoList = new ArrayList<>();
        mdnsClient.setTimeout(0); //set minimum timeout
        long startTime = System.currentTimeMillis();
        Long foundUniqueId = null;
        while (System.currentTimeMillis() < startTime + timeout) {
            try {
                // getServices() looks for each other on all interfaces using the timeout set by
                // setTimeout(). Therefor we have set the timeout to 0 to get the current services list
                // almost immediately (the real minimum timeout is 200ms - a "feature" of the Jmdns library).
                // If the mDNS announcement for the requested unique id is not found on any of the interfaces,
                // we wait a while (1000ms) and try again until the overall timeout is reached.
                infoList = mdnsClient.getServices(serviceType);
                log.debug("mDNS: \n{}", infoList);
            } catch (Exception e) { log.error("Error getting mDNS services list: {}", e.toString()); }

            // Fill the device list with the found info from mDNS records.
            // infoList always contains the complete list of the mDNS announcements found so far,
            // so the clear our internal list before filling it (again).
            deviceList.clear();

            for (ServiceInfo serviceInfo : infoList) {
                //log.trace("{}", serviceInfo.getNiceTextString());
                log.trace("key: {}", serviceInfo.getKey());
                log.trace("server: {}", serviceInfo.getServer());
                log.trace("qualified name: {}", serviceInfo.getQualifiedName());
                log.trace("type: {}", serviceInfo.getType());
                log.trace("subtype: {}", serviceInfo.getSubtype());
                log.trace("app: {}, proto: {}", serviceInfo.getApplication(), serviceInfo.getProtocol());
                log.trace("name: {}, port: {}", serviceInfo.getName(), serviceInfo.getPort());
                log.trace("inet addresses: {}", new ArrayList<>(Arrays.asList(serviceInfo.getInetAddresses())));
                log.trace("hostnames: {}", new ArrayList<>(Arrays.asList(serviceInfo.getHostAddresses())));
                log.trace("urls: {}", new ArrayList<>(Arrays.asList(serviceInfo.getURLs())));
                Enumeration<String> propList = serviceInfo.getPropertyNames();
                while (propList.hasMoreElements()) {
                    String prop = propList.nextElement();
                    log.trace("service info property {}: {}", prop, serviceInfo.getPropertyString(prop));
                }
                Long uid = ByteUtils.parseHexUniqueId(serviceInfo.getPropertyString("uid")) & 0xFFFFFFFFFFL;
                // if the same UID is announced twice (or more) overwrite the previous entry
                NetBiDiDDevice dev = deviceList.getOrDefault(uid, new NetBiDiDDevice());
                dev.setAddress(serviceInfo.getInetAddresses()[0]);
                dev.setPort(serviceInfo.getPort());
                dev.setUniqueId(uid);
                dev.setProductName(serviceInfo.getPropertyString("prod"));
                dev.setUserName(serviceInfo.getPropertyString("user"));
                deviceList.put(uid, dev);
                
                log.info("Found announcement: {}", dev.getString());

                // if no current unique id is known, try the known IP address if valid
                if (getUniqueId() == null) {
                    try {
                        InetAddress curHostAddr = InetAddress.getByName(getHostName());
                        if (dev.getAddress().equals(curHostAddr)) {
                            setUniqueId(dev.getUniqueId());
                        }
                    }
                    catch (UnknownHostException e) { log.trace("No known hostname {}", getHostName()); } //no known host address is not an error
                }

                // set current hostname and port from the list if the this entry is the requested unique id
                if (uid.equals(getUniqueId())) {
                    setHostName(dev.getAddress().getHostAddress());
                    setPort(dev.getPort());
                    foundUniqueId = uid; //we have found what we have looked for
                    //break; //exit the for loop as 
                }
            }
            if (foundUniqueId != null) {
                break; //the while loop
            }
            try {
                Thread.sleep(1000); //wait a moment and then try again until timeout has been reached or the announcement was found
            } catch (final InterruptedException e) {
                /* Stub */
            }
        }
        
        // some log info
        if (foundUniqueId == null) {
            // Write out a warning if we have been looking for a known uid.
            // If we don't have a request uid, this is no warning as we just collect the announcements.
            if (getUniqueId() != null) {
                log.warn("no mDNS announcement found for requested unique id {} - last known IP: {}", ByteUtils.formatHexUniqueId(getUniqueId()), getHostName());
            }
        }
        else {
            log.info("using mDNS announcement: {}", deviceList.get(foundUniqueId).getString());
        }

        deviceListAddFromPairingStore(); //add "paired" status from the pairing store to the device list
    }

    /**
     * Get and set the ZeroConf/mDNS advertisement name.
     * <p>
     * This value is the unique id in BiDiB.
     * 
     * @return advertisement name.
     */
    @Override
    public String getAdvertisementName() {
        //return Bundle.getMessage("defaultMDNSServiceName");
        //return ByteUtils.formatHexUniqueId(uniqueId);
        /////// use "VnnPnnnnnn" instead
        return ByteUtils.getUniqueIdAsStringCompact(getUniqueId());
    }
    
    @Override
    public void setAdvertisementName(String AdName) {
        // AdName has the format "VvvPppppssss"
        setUniqueId(ByteUtils.parseHexUniqueId(AdName.replaceAll("[VP]", ""))); //remove V and P and convert the remaining hex string to Long
    }

    /**
     * Get the ZeroConf/mDNS service type.
     * <p>
     * This value is fixed in BiDiB, so return the default
     * value.
     * 
     * @return service type.
     */
    @Override
    public String getServiceType() {
        return Bundle.getMessage("defaultMDNSServiceType");
    }
    
    // netBiDiB Adapter specific methods
    
    /**
     * Get the device list of all found devices and return them as a map
     * of strings suitable for display and indexed by the unique id.
     * 
     * This is used by the connection config.
     * 
     * @return map of strings containing device info.
     */

    public Map<Long, String> getDeviceListEntries() {
        Map<Long, String> stringList = new LinkedHashMap<>();
        for (NetBiDiDDevice dev : deviceList.values()) {
            stringList.put(dev.getUniqueId(), dev.getString());
        }
        return stringList;
    }
    
    /**
     * Set hostname, port and unique id from the device list entry selected by a given index.
     * 
     * @param i selected index into device list
     */
    public void selectDeviceListItem(int i) {
        if (i >= 0  &&  i < deviceList.size()) {
            List<Map.Entry<Long, NetBiDiDDevice>> entryList = new ArrayList<>(deviceList.entrySet());
            NetBiDiDDevice dev = entryList.get(i).getValue();
            log.trace("index {}: uid: {}, entry: {}", i, ByteUtils.formatHexUniqueId(entryList.get(i).getKey()), entryList.get(i).getValue().getString());
            // update host name, port and unique id from device list
            setHostName(dev.getAddress().getHostAddress());
            setPort(dev.getPort());
            setUniqueId(dev.getUniqueId());
        }
    }
    
    /**
     * Get and set the BiDiB Unique ID.
     * <p>
     * If we haven't set the unique ID of the connection before, try to find it from the root node
     * of the connection. This will work only if the connection is open and not detached.
     * 
     * @return unique Id as Long
     */
    public Long getUniqueId() {
        if (uniqueId == null) {
            if (bidib != null  &&  bidib.isOpened()  &&  !isDetached()) {
                Node rootNode = getSystemConnectionMemo().getBiDiBTrafficController().getRootNode();
                if (rootNode != null  &&  rootNode.getUniqueId() != 0)
                uniqueId = rootNode.getUniqueId() & 0xFFFFFFFFFFL;
            }
        }
        return uniqueId;
    }
    
    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }
    
//UNUSED
//    public boolean isLocalPaired() {
//        if (getUniqueId() != null) {
//            NetBiDiDDevice dev = deviceList.get(getUniqueId());
//            if (dev != null) {
//                return dev.isPaired();
//            }
//        }
//        return false;
//    }

    /**
     * Get the connection ready status from the traffic controller
     * 
     * @return true if the connection is opened and ready to use (paired and logged in)
     */
    public boolean isConnectionReady() {
        BiDiBSystemConnectionMemo memo = getSystemConnectionMemo();
        if (memo != null) {
            BiDiBTrafficController tc = memo.getBiDiBTrafficController();
            if (tc != null) {
                return tc.isConnectionReady();
            }
        }
        return false;
    }
    
    /**
     * Set new pairing state.
     * 
     * If the pairing should be removed, close the connection, set pairing state in
     * the device list and update the pairing store.
     * 
     * If pairing should be initiated, a connection is temporary opened and a pariring dialog
     * is displayed which informs the user to confirm the pairing on the remote device.
     * If the process has completed, the temporary connection is closed.
     * 
     * Pairing and unpairing is an asynchroneous process, so an action listener may be provided which
     * is called when the process has completed.
     * 
     * @param paired - true if the pairing should be initiated, false if pairing should be removed
     * @param l - and event listener, called when pairing or unpairing has finished.
     */
    public void setPaired(boolean paired, ActionListener l) {
        pairingListener = l;
        if (!paired) {
            // close existent BiDiB connection
            if (bidib != null) {
                if (bidib.isOpened()) {
                    bidib.close();
                }
            }
            NetBiDiDDevice dev = deviceList.get(getUniqueId());
            if (dev != null) {
                dev.setPaired(false);
            }
            // setup Pairing store
            pairingStore.load();
            List<PairingStoreEntry> entries = pairingStore.getPairingStoreEntries();
            for (PairingStoreEntry pe : entries) {
                log.debug("Pairing store entry: {}", pe);
                Long uid = ByteUtils.parseHexUniqueId(pe.getUid()); //uid is the full uid with all class bits as stored in the pairing store
                if ((uid  & 0xFFFFFFFFFFL) == getUniqueId()) { //check if this uid (without class bits) matches our uid
                    pairingStore.setPaired(uid, false);
                }
            }
            pairingStore.store();
            if (pairingListener != null)  {
                pairingListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
            }
        }
        else {
            //connect();
            //closeConnection();
            prepareOpenContext();
            if (bidib == null) {
                log.info("create netBiDiB instance");
                bidib = NetBidibClient.createInstance(getContext());
                //log.warn("Pairing request - no BiDiB instance available. This should never happen.");
                //return;
            }
            if (bidib.isOpened()) {
                log.warn("Pairing request - BiDiB instance is already opened. This should never happen.");
                return;
            }
            ConnectionListener connectionListener = new ConnectionListener() {
                
                @Override
                public void opened(String port) {
                    // no implementation
                    log.debug("opened port {}", port);
                }

                @Override
                public void closed(String port) {
                    log.debug("closed port {}", port);
                    if (pairingDialog != null) {
                        pairingDialog.hide();
                        pairingDialog = null;
                    }
                    if (pairingListener != null)  {
                        pairingListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
                    }
                }

                @Override
                public void status(String messageKey, Context context) {
                    // no implementation
                }
                
                @Override
                public void pairingFinished(final PairingResult pairingResult, long uniqueId) {
                    log.debug("** pairingFinished - result: {}, uniqueId: {}", pairingResult,
                            ByteUtils.convertUniqueIdToString(ByteUtils.convertLongToUniqueId(uniqueId)));
                    // The pairing timed out or was cancelled on the server side.
                    // Cancelling is also possible while in normal operation.
                    // Close the connection.
                    if (bidib.isOpened()) {
                        //bidib.close(); //close() from a listener causes an exception in jbibibc, so delay the close
                        delayedCloseTimer.start();
                    }
                }

                @Override
                public void actionRequired(String messageKey, final Context context) {
                    log.info("actionRequired - messageKey: {}, context: {}", messageKey, context);
                    if (messageKey.equals(NetBidibContextKeys.KEY_ACTION_PAIRING_STATE)) {
                        if (context.get(NetBidibContextKeys.KEY_PAIRING_STATE) == PairingStateEnum.Unpaired) {
                            log.trace("**** send pairing request ****");
                            log.trace("context: {}", context);
                            // Send a pairing request to the remote side and show a dialog so the user
                            // will be informed.
                            bidib.signalUserAction(NetBidibContextKeys.KEY_PAIRING_REQUEST, context);

                            pairingDialog = new NetBiDiBPairingRequestDialog(context, portController, new ActionListener() {

                                /**
                                 * called when the pairing dialog was closed by the user or if the user pressed the cancel-button.
                                 * In this case the init should fail.
                                 */
                                @Override
                                public void actionPerformed(ActionEvent ae) {
                                    log.debug("pairingDialog cancelled: {}", ae);
                                    //bidib.close(); //close() from a listener causes an exception in jbibibc, so delay the close
                                    delayedCloseTimer.start();
                                }
                            });
                            // Show the dialog.
                            pairingDialog.show();
                        }
                    }       
                }

                
            };
            // open the device
            String portName = getRealPortName();
            log.info("Open BiDiB connection for pairting on \"{}\"", portName);

            bidib = NetBidibClient.createInstance(getContext());

            try {
                bidib.setResponseTimeout(1600);
                bidib.open(portName, connectionListener, null, null, null, context);
            }
            catch (Exception e) {
                log.error("Execute command failed: ", e); // NOSONAR
            }
        }
    }
    
    /**
     * Check of the connection is opened.
     * This does not mean that it is paired or logged on.
     * 
     * @return true if opened
     */
    public boolean isOpened() {
        if (bidib != null) {
            return bidib.isOpened();
        }
        return false;
    }
    
    /**
     * Check if the connection is detached i.e. it is opened, paired
     * but the logon has been rejected.
     * 
     * @return true if detached
     */
    public boolean isDetached() {
        return getSystemConnectionMemo().getBiDiBTrafficController().isDetached();
    }
    
    /**
     * Set or remove the detached state.
     * 
     * @param logon - true for logon (attach), false for logoff (detach)
     */
    public void setLogon(boolean logon) {
        getSystemConnectionMemo().getBiDiBTrafficController().setLogon(logon);
    }
    
    public void addConnectionChangedListener(ActionListener l) {
        getSystemConnectionMemo().getBiDiBTrafficController().addConnectionChangedListener(l);
    }

    public void removeConnectionChangedListener(ActionListener l) {
        getSystemConnectionMemo().getBiDiBTrafficController().removeConnectionChangedListener(l);
    }

// WE USE ZEROCONF CLIENT
//    /**
//     * Get all servers providing the specified service.
//     *
//     * @param service the name of service as generated using
//     *                {@link jmri.util.zeroconf.ZeroConfServiceManager#key(java.lang.String, java.lang.String) }
//     * @return A list of servers or an empty list.
//     */
//    @Nonnull
//    public List<ServiceInfo> getServices(@Nonnull String service) {
//        ArrayList<ServiceInfo> services = new ArrayList<>();
//        for (JmDNS server : InstanceManager.getDefault(ZeroConfServiceManager.class).getDNSes().values()) {
//            if (server.list(service,0) != null) {
//                services.addAll(Arrays.asList(server.list(service,0)));
//            }
//        }
//        return services;
//    }


    private final static Logger log = LoggerFactory.getLogger(NetBiDiBAdapter.class);

    
}
