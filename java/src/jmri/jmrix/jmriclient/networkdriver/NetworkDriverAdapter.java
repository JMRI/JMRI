package jmri.jmrix.jmriclient.networkdriver;

import java.util.ResourceBundle;
import jmri.jmrix.jmriclient.JMRIClientPortController;
import jmri.jmrix.jmriclient.JMRIClientTrafficController;
import jmri.util.zeroconf.ZeroConfClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements NetworkPortAdapter for the jmriclient system network connection.
 * <p>
 * This connects a JMRI Simple Server (daemon) via a telnet connection.
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class NetworkDriverAdapter extends JMRIClientPortController {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.jmriclient.JMRIClientConfigurationBundle");

    public NetworkDriverAdapter() {
        super(new jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo());
        setPort(2048); // set the default port on construction
    }

    /**
     * set up all of the other objects to operate with an JMRI Simple server connected
     * to this port
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        JMRIClientTrafficController control = new JMRIClientTrafficController();
        control.connectPort(this);
        this.getSystemConnectionMemo().setJMRIClientTrafficController(control);
        this.getSystemConnectionMemo().configureManagers();
    }

    @Override
    public boolean status() {
        return opened;
    }

    // private control members
    private boolean opened = false;

    @Deprecated
    static public NetworkDriverAdapter instance() {
        log.error("Deprecated method instance Called", new Exception());
        return null;
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
        log.info("Configuring JMRIClient interface via JmDNS");
        if (getHostName().equals(rb.getString("defaultMDNSServerName"))) {
            setHostName(""); // reset the hostname to none.
        }
        String serviceType = rb.getString("defaultMDNSServiceType");
        log.debug("Listening for service: " + serviceType);

        if (mdnsClient == null) {
            mdnsClient = new ZeroConfClient();
            mdnsClient.startServiceListener(serviceType);
        }
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

    private final static Logger log = LoggerFactory.getLogger(NetworkDriverAdapter.class);

}
