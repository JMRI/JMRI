package jmri.jmrix.bidib.bidibovertcp;

import javax.swing.JPanel;
import jmri.jmrix.bidib.BiDiBConstants;
import org.bidib.jbidibc.net.serialovertcp.NetBidib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Definition of objects to handle configuring a BiDiB OverTcp layout
 * connection via a BiDiBTcpDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Stephen Williams Copyright (C) 2008
 * @author Mark Underwood Copyright (C) 2015
 * @author Eckart Meyer Copyright (C) 2023
 *
 * Based on DCCppNetOverTCP 
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractNetworkConnectionConfig {

    public final static String NAME = "BiDiB over TCP"; //text to show in ComboBox

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig() {
        super();
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     * @param p network port adapter.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);
        log.info("NetworkPortAdapter opening.");
    }

    @Override
    public String name() {
        return NAME;
    }

    public boolean isOptList1Advanced() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new BiDiBOverTcpAdapter();
            adapter.setPort(NetBidib.BIDIB_UDP_PORT_NUMBER);
            adapter.setHostName(BiDiBConstants.BIDIB_OVER_TCP_DEFAULT_HOST);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(JPanel details) {
        super.loadDetails(details);
        //hostNameField.setText(BiDiBConstants.BIDIB_OVER_TCP_DEFAULT_HOST);
        portFieldLabel.setText("TCP Port");
//        portField.setText(String.valueOf(LIUSBServerAdapter.COMMUNICATION_TCP_PORT));
    }


    private static final Logger log = LoggerFactory.getLogger(ConnectionConfig.class);

}
