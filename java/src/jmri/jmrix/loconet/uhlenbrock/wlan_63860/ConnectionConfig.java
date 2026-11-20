package jmri.jmrix.loconet.uhlenbrock.wlan_63860;

/**
 * Definition of objects to handle configuring a connection via the WLAN
 * (LocoNet-over-TCP) side of an Uhlenbrock LocoNet-USB and WLAN Interface
 * 63860, using a {@link Wlan63860Adapter} object.
 *
 * @author detami01 Copyright (C) 2026
 * @see jmri.jmrix.loconet.loconetovertcp.ConnectionConfig
 */
public class ConnectionConfig extends jmri.jmrix.loconet.loconetovertcp.ConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     * @param p the NetworkPortAdapter to associate with this connection
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return Bundle.getMessage("WLAN_63860Title");
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new Wlan63860Adapter();
        }
    }

}
