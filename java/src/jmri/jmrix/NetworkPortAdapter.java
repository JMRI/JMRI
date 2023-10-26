package jmri.jmrix;

/**
 * Enables basic setup of a network interface for a jmrix implementation. Based
 * upon work by Bob Jacobsen from SerialPortAdapter
 *
 * @author Kevin Dickerson Copyright (C) 2010
 * @author Bob Jacobsen Copyright (C) 2010
 * @see jmri.jmrix.NetworkConfigException
 */
public interface NetworkPortAdapter extends PortAdapter {

    /**
     * Connects to the end device using a hostname/ip address and port
     * @param host hostname / ip address.
     * @param port network port.
     * @throws java.io.IOException on connection error.
     */
    void connect(String host, int port) throws java.io.IOException;

    /**
     * Configure all of the other jmrix widgets needed to work with this adapter
     */
    @Override
    void configure();

    /**
     * Query the status of this connection.
     *
     * @return true if all is OK, at least as far as known.
     */
    @Override
    boolean status();

    /**
     * Remember the associated port name.
     *
     * @param s port name.
     */
    void setPort(String s);

    void setPort(int s);

    int getPort();

    @Override
    String getCurrentPortName();

    void setHostName(String hostname);

    String getHostName();

    /*
     * Set whether or not this adapter should be
     * configured automatically via MDNS.
     */
    void setMdnsConfigure(boolean autoconfig);

    /*
     * Get whether or not this adapter is configured
     * to use autoconfiguration via MDNS
     */
    boolean getMdnsConfigure();

    /*
     * Perform the automatic configuration.
     */
    void autoConfigure();

    /*
     * Get and set the ZeroConf/mDNS advertisement name.
     */
    void setAdvertisementName(String AdName);

    String getAdvertisementName();

    /*
     * Get and set the ZeroConf/mDNS service type.
     */
    void setServiceType(String ServiceType);

    String getServiceType();

}
