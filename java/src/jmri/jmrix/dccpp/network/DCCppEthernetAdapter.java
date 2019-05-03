package jmri.jmrix.dccpp.network;

import java.util.ResourceBundle;
import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInitializationManager;
import jmri.jmrix.dccpp.DCCppNetworkPortController;
import jmri.jmrix.dccpp.DCCppTrafficController;
import jmri.util.zeroconf.ZeroConfClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to DCC++ Base Station via Ethernet. NOTES: By default,
 * the LIUSBEthernet has an IP address of 192.168.0.200 and listens to port
 * 5550. The LIUSBEtherenet disconnects both ports if there is 60 seconds of
 * inactivity on the port.
 *
 * @author Paul Bender (C) 2011-2013
 * @author Mark Underwood (C) 2015
 * Based on LIUSBEthernetAdapter
 */
public class DCCppEthernetAdapter extends DCCppNetworkPortController {

    static final int COMMUNICATION_TCP_PORT = 2560;
    static final String DEFAULT_IP_ADDRESS = "192.168.0.200";

    private java.util.TimerTask keepAliveTimer; // Timer used to periodically
    // send a message to both
    // ports to keep the ports 
    // open
    private static final long keepAliveTimeoutValue = 30000; // Interval 
    // to send a message
    // Must be < 60s.
    
    public DCCppEthernetAdapter() {
        super();
        log.debug("Constructor Called");
        setHostName(DEFAULT_IP_ADDRESS);
        setPort(COMMUNICATION_TCP_PORT);
        this.manufacturerName = jmri.jmrix.dccpp.DCCppConnectionTypeList.DCCPP;
    }
    
    @Override
    public void connect() throws java.io.IOException {
        super.connect();
        log.debug("openPort called");
        keepAliveTimer();
    }
    
    /**
     * Can the port accept additional characters?
     *
     * @return true if the port is opened
     */
    @Override
    public boolean okToSend() {
        return status();
    }
    
    @Override
    public boolean status() {
        return (opened);
    }
    
    /**
     * Set up all of the other objects to operate with a LIUSB Ethernet
     * interface.
     */
    @Override
    public void configure() {
        log.debug("configure called");
        // connect to a packetizing traffic controller
        DCCppTrafficController packets = (new DCCppEthernetPacketizer(new DCCppCommandStation()));
        packets.connectPort(this);
        
        // start operation
        // packets.startThreads();
        this.getSystemConnectionMemo().setDCCppTrafficController(packets);
        
        new DCCppInitializationManager(this.getSystemConnectionMemo());
    }
    
    /**
     * Local method to do specific configuration.
     */
    @Deprecated
    static public DCCppEthernetAdapter instance() {
        if (mInstance == null) {
            mInstance = new DCCppEthernetAdapter();
        }
        return mInstance;
    }
    volatile static DCCppEthernetAdapter mInstance = null;
    
    /**
     * Set up the keepAliveTimer, and start it.
     */
    private void keepAliveTimer() {
        if (keepAliveTimer == null) {
            keepAliveTimer = new java.util.TimerTask(){
                    @Override
                    public void run() {
                        // If the timer times out, send a request for status
                        DCCppEthernetAdapter.this.getSystemConnectionMemo().getDCCppTrafficController()
                            .sendDCCppMessage(
                                              jmri.jmrix.dccpp.DCCppMessage.makeCSStatusMsg(),
                                              null);
                    }
                };
        } else {
            keepAliveTimer.cancel();
        }
        jmri.util.TimerUtil.schedule(keepAliveTimer, keepAliveTimeoutValue, keepAliveTimeoutValue);
    }
    
    private boolean mDNSConfigure = false;
    
    /**
     * Set whether or not this adapter should be
     * configured automatically via MDNS.
     *
     * @param autoconfig boolean value.
     */
    @Override
    public void setMdnsConfigure(boolean autoconfig) {
        log.debug("Setting DCC++ Ethernet adapter autoconfiguration to: {}", autoconfig);
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
        log.info("Configuring DCC++ interface via JmDNS");
        if (getHostName().equals(DEFAULT_IP_ADDRESS)) {
            setHostName(""); // reset the hostname to none.
        }
        String serviceType = Bundle.getMessage("defaultMDNSServiceType");
        log.debug("Listening for service: {}", serviceType);
        
        if (mdnsClient == null) {
            mdnsClient = new ZeroConfClient();
            mdnsClient.startServiceListener(serviceType);
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
        try {
            // if there is a hostname set, use the host name (which can
            // be changed) to find the service.
            String qualifiedHostName = m_HostName
                + "." + Bundle.getMessage("defaultMDNSDomainName");
            setHostAddress(mdnsClient.getServiceOnHost(serviceType,
                                                       qualifiedHostName).getHostAddresses()[0]);
        } catch (java.lang.NullPointerException npe) {
            // if there is no hostname set, use the service name (which can't
            // be changed) to find the service.
            String qualifiedServiceName = Bundle.getMessage("defaultMDNSServiceName")
                + "." + serviceType;
            setHostAddress(mdnsClient.getServicebyAdName(serviceType,
                                                         qualifiedServiceName).getHostAddresses()[0]);
        }
    }
    
    ZeroConfClient mdnsClient = null;
    
    /**
     * Get the ZeroConf/mDNS advertisement name.
     * this value is fixed on the LIUSB-Ethernet, so return the default
     * value.
     */
    @Override
    public String getAdvertisementName() {
        return Bundle.getMessage("defaultMDNSServiceName");
    }
    
    /**
     * Get the ZeroConf/mDNS service type.
     * this value is fixed on the LIUSB-Ethernet, so return the default
     * value.
     */
    @Override
    public String getServiceType() {
        return Bundle.getMessage("defaultMDNSServiceType");
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppEthernetAdapter.class);

}
