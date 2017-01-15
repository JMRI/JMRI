package jmri.jmrix.lenz.liusbethernet;

import java.util.ResourceBundle;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInitializationManager;
import jmri.jmrix.lenz.XNetNetworkPortController;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.util.zeroconf.ZeroConfClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to XPressNet via a the Lenz LIUSBEthernet. NOTES: By default,
 * the LIUSBEthernet has an IP address of 192.168.0.200 and listens to port
 * 5550. The LIUSBEtherenet disconnects both ports if there is 60 seconds of
 * inactivity on the port.
 *
 * @author	Paul Bender (C) 2011-2013
 */
public class LIUSBEthernetAdapter extends XNetNetworkPortController {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.XNetConfigurationBundle");
    static final int COMMUNICATION_TCP_PORT = 5550;
    static final String DEFAULT_IP_ADDRESS = "192.168.0.200";

    private java.util.TimerTask keepAliveTimer; // Timer used to periodically
    // send a message to both
    // ports to keep the ports 
    // open
    private static final int keepAliveTimeoutValue = 30000; // Interval 
    // to send a message
    // Must be < 60s.

    public LIUSBEthernetAdapter() {
        super();
        if (log.isDebugEnabled()) {
            log.debug("Constructor Called");
        }
        setHostName(DEFAULT_IP_ADDRESS);
        setPort(COMMUNICATION_TCP_PORT);
        this.manufacturerName = jmri.jmrix.lenz.LenzConnectionTypeList.LENZ;
    }

    @Override
    public void connect() throws Exception {
        super.connect();
        if (log.isDebugEnabled()) {
            log.debug("openPort called");
        }
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
     * set up all of the other objects to operate with a LIUSB Ethernet
     * interface
     */
    @Override
    public void configure() {
        if (log.isDebugEnabled()) {
            log.debug("configure called");
        }
        // connect to a packetizing traffic controller
        XNetTrafficController packets = (new LIUSBEthernetXNetPacketizer(new LenzCommandStation()));
        packets.connectPort(this);

        // start operation
        // packets.startThreads();
        this.getSystemConnectionMemo().setXNetTrafficController(packets);

        new XNetInitializationManager(this.getSystemConnectionMemo());
    }

    /*
     * Set up the keepAliveTimer, and start it.
     */
    private void keepAliveTimer() {
        if (keepAliveTimer == null) {
            keepAliveTimer = new java.util.TimerTask() {
                @Override
                public void run() {
                    // If the timer times out, and we are not currently 
                    // programming, send a request for status
                    jmri.jmrix.lenz.XNetSystemConnectionMemo m = LIUSBEthernetAdapter.this
                            .getSystemConnectionMemo();
                    XNetTrafficController t = m.getXNetTrafficController();
                    jmri.jmrix.lenz.XNetProgrammer p = (jmri.jmrix.lenz.XNetProgrammer) (m.getProgrammerManager().getGlobalProgrammer());
                    if (p == null || !(p.programmerBusy())) {
                        t.sendXNetMessage(
                                jmri.jmrix.lenz.XNetMessage.getCSStatusRequestMessage(),
                                null);
                    }
                }
            };
        } else {
            keepAliveTimer.cancel();
        }
        new java.util.Timer().schedule(keepAliveTimer, keepAliveTimeoutValue, keepAliveTimeoutValue);
    }

    private boolean mDNSConfigure = false;

    /*
     * Set whether or not this adapter should be
     * configured automatically via MDNS.
     * @param autoconfig boolean value.
     */
    @Override
    public void setMdnsConfigure(boolean autoconfig) {
        log.debug("Setting LIUSB Ethernet adapter autoconfiguration to: "
                + autoconfig);
        mDNSConfigure = autoconfig;
    }

    /*
     * Get whether or not this adapter is configured
     * to use autoconfiguration via MDNS
     * @return true if configured using MDNS.
     */
    @Override
    public boolean getMdnsConfigure() {
        return mDNSConfigure;
    }

    /*
     * set the server's host name and port
     * using mdns autoconfiguration.
     */
    @Override
    public void autoConfigure() {
        log.info("Configuring XPressNet interface via JmDNS");
        if (getHostName().equals(DEFAULT_IP_ADDRESS)) {
            setHostName(""); // reset the hostname to none.
        }
        String serviceType = rb.getString("defaultMDNSServiceType");
        log.debug("Listening for service: " + serviceType);

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
                    + "." + rb.getString("defaultMDNSDomainName");
            setHostAddress(mdnsClient.getServiceOnHost(serviceType,
                    qualifiedHostName).getHostAddresses()[0]);
        } catch (java.lang.NullPointerException npe) {
            // if there is no hostname set, use the service name (which can't
            // be changed) to find the service.
            String qualifiedServiceName = rb.getString("defaultMDNSServiceName")
                    + "." + serviceType;
            setHostAddress(mdnsClient.getServicebyAdName(serviceType,
                    qualifiedServiceName).getHostAddresses()[0]);
        }
    }

    ZeroConfClient mdnsClient = null;

    /*
     * Get the ZeroConf/mDNS advertisement name.
     * this value is fixed on the LIUSB-Ethernet, so return the default
     * value.
     */
    @Override
    public String getAdvertisementName() {
        return rb.getString("defaultMDNSServiceName");
    }

    /*
     * Get the ZeroConf/mDNS service type.
     * this value is fixed on the LIUSB-Ethernet, so return the default
     * value.
     */
    @Override
    public String getServiceType() {
        return rb.getString("defaultMDNSServiceType");
    }

    private final static Logger log = LoggerFactory.getLogger(LIUSBEthernetAdapter.class.getName());

}
