package jmri.jmrix.loconet.locormi;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Definition of objects to handle configuring the layout connection via LocoNet
 * RMI.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 */
 //@todo This class could ideally do with refactoring to the NetworkConnectionConfig and also multi-connection
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    private final static Logger log = LoggerFactory.getLogger(ConnectionConfig.class);

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(String p, String m) {
        super();
        hostName = p;
        if (m != null) {
            manufacturerName = m;
        }
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public ConnectionConfig() {
        super();
    }

    public JTextField host;
    String hostName = "";

    @Override
    public String name() {
        return "LocoNet Server";
    }

    @Override
    public String getConnectionName() {
        if ((lmc != null) && (lmc.getAdapterMemo() != null)) {
            return lmc.getAdapterMemo().getUserName();
        }
        return name();
    }

    @Override
    public String getInfo() {
        return hostName;
    }

    public void setLnMessageClient(LnMessageClient ln) {
        lmc = ln;
    }

    LnMessageClient lmc;

    public LnMessageClient getLnMessageClient() {
        return lmc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(JPanel details) {
        //details.setLayout(new BoxLayout(details, BoxLayout.X_AXIS));
        details.add(new JLabel("Server hostname:"));
        host = new JTextField(20);
        host.setText(hostName);
        details.add(host);
    }

    public boolean isOptList2Advanced() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        log.warn("Unexpected call to setInstance, multi-replica capability not yet present");
    }

    String manufacturerName = jmri.jmrix.loconet.LnConnectionTypeList.DIGITRAX;

    @Override
    public String getManufacturer() {
        return manufacturerName;
    }

    @Override
    public void setManufacturer(String manu) {
        manufacturerName = manu;
    }

    boolean disabled = false;

    @Override
    public boolean getDisabled() {
        return disabled;
    }

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        if ((lmc != null) && (lmc.getAdapterMemo() != null)) {
            lmc.getAdapterMemo().setDisabled(disabled);
        }
    }

}
