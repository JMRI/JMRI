package jmri.jmrix.loconet.uhlenbrock.wlan_63860;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.loconetovertcp.LnTcpDriverAdapter;
import jmri.jmrix.loconet.uhlenbrock.UhlenbrockConnectionTypeList;

/**
 * Adapter for the WLAN side of the Uhlenbrock "LocoNet-USB und WLAN
 * Interface", part no. 63860.
 * <p>
 * The 63860 contains a LocoNet-over-TCP server (LBServer protocol), so this
 * is a {@link LnTcpDriverAdapter} preset to the factory defaults of the
 * interface: in its own access point (SSID "UB_WLAN_" plus serial number, no
 * password) it is reachable at 192.168.0.111, TCP port 1234. If the interface
 * has been joined to a home network, the host name or address can be changed
 * in the connection preferences as usual.
 *
 * @author detami01 Copyright (C) 2026
 * @see jmri.jmrix.loconet.uhlenbrock.usb_63860.Usb63860Adapter
 */
public class Wlan63860Adapter extends LnTcpDriverAdapter {

    /**
     * Factory default address of the 63860 in its own access point.
     */
    public static final String DEFAULT_HOST = "192.168.0.111"; // NOI18N

    /**
     * Factory default LocoNet-over-TCP port of the 63860.
     */
    public static final int DEFAULT_PORT = 1234;

    public Wlan63860Adapter(LocoNetSystemConnectionMemo m) {
        super(m);
        setManufacturer(UhlenbrockConnectionTypeList.UHLEN);
        setHostName(DEFAULT_HOST);
        setPort(DEFAULT_PORT);
    }

    public Wlan63860Adapter() {
        this(new LocoNetSystemConnectionMemo());
    }

}
