package jmri.jmrix.cmri.serial.networkdriver;

import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JPanel;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.nodeconfigmanager.NodeConfigManagerAction;

/**
 * Definition of objects to handle configuring a layout connection via a
 * NetworkDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2015
 */
public class ConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    public static final String NAME = "Network Interface";

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     * @param p network port adapter.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no existing adapter.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return NAME;
    }

    JButton b;

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(JPanel details) {

        setInstance();
        b = new JButton(Bundle.getMessage("ConfigureNodesTitle"));
        b.addActionListener(new NodeConfigManagerAction((CMRISystemConnectionMemo)adapter.getSystemConnectionMemo()));
        if (!additionalItems.contains(b)) {
            additionalItems.add(b);
        }
        super.loadDetails(details);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new NetworkDriverAdapter();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Config getConfig() {
        return ((CMRISystemConnectionMemo) getAdapter().getSystemConnectionMemo())
                .getConfig();
    }

    /** {@inheritDoc} */
    @Override
    public void setConfig(@Nonnull Config config) {
        if (config instanceof CMRISystemConnectionMemo.Config) {
            ((CMRISystemConnectionMemo) getAdapter().getSystemConnectionMemo())
                    .setConfig((CMRISystemConnectionMemo.Config) config);
        } else {
            log.info("Can't set config. Expected {} but got {}",
                    config.getClass().getName(),
                    CMRISystemConnectionMemo.Config.class.getName());
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnectionConfig.class);
}
