package jmri.jmrix.cmri.serial.sim;

import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.nodeconfigmanager.NodeConfigManagerAction;

/**
 * Definition of objects to handle configuring a layout connection via a C/MRI
 * Simulator object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2008
 * @author Chuck Catania Copyright (C) 2017
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSimulatorConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     * @param p serial port adapter.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public ConnectionConfig() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(JPanel details) {

        setInstance();

        // have to embed the usual one in a new JPanel

        JPanel p = new JPanel();
        super.loadDetails(p);

        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.add(p);

        // add another button
        JButton b = new JButton(Bundle.getMessage("ConfigureNodesTitle"));

        details.add(b);

        b.addActionListener(new NodeConfigManagerAction((CMRISystemConnectionMemo)adapter.getSystemConnectionMemo()));
    }

    @Override
    public String name() {
        return "C/MRI Simulator";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if(adapter == null ) {
           adapter = new SimDriverAdapter();
           adapter.configure(); // make sure the traffic controller
                                // loads so that node details can be
                                // saved.
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
            var memo = (CMRISystemConnectionMemo) getAdapter().getSystemConnectionMemo();
            memo.setConfig((CMRISystemConnectionMemo.Config) config);
            memo.restoreConfig();
        } else {
            log.info("Can't set config. Expected {} but got {}",
                    config.getClass().getName(),
                    CMRISystemConnectionMemo.Config.class.getName());
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnectionConfig.class);
}
