package jmri.jmrix.anyma;

import java.util.List;
import jmri.jmrix.AbstractUsbConnectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Definition of objects to handle configuring an Anyma DMX layout connection
 * via a AnymaDMX_UsbPortAdapter object.
 * <p>
 * This uses the {@link AnymaDMX_UsbPortAdapter} class to do the actual
 * connection.
 *
 * @author George Warner Copyright (c) 2017-2018
 * @since 4.9.6
 * @see AnymaDMX_UsbPortAdapter
 */
public class AnymaDMX_ConnectionConfig extends AbstractUsbConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     *
     * @param p the pre-existing adapter
     */
    public AnymaDMX_ConnectionConfig(AnymaDMX_UsbPortAdapter p) {
        super(p);
        log.debug("*    constructor('{}').", p);
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public AnymaDMX_ConnectionConfig() {
        this(new AnymaDMX_UsbPortAdapter());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAdapter() {
        log.debug("*    updateAdapter()");
        if ((adapter.getSystemConnectionMemo() != null)
                && !adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
            systemPrefixField.setValue(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new AnymaDMX_UsbPortAdapter();
        }
        //if (adapter.getDMXController() == null) {
        //    // don't show more than once every 30 seconds
        //    if (!GraphicsEnvironment.isHeadless()
        //            && (this.DMXMessageShown == null || ((new Date().getTime() - this.DMXMessageShown.getTime()) / 1000 % 60) > 30)) {
        //        JOptionPane.showMessageDialog(this._details,
        //                Bundle.getMessage("NoDMXControllerMessage"),
        //                Bundle.getMessage("NoDMXControllerTitle"),
        //                JOptionPane.ERROR_MESSAGE);
        //        this.DMXMessageShown = new Date();
        //    }
        //}
    }

    @Override
    public String name() {
        log.debug("*    name()");
        return getConnectionName();
    }

    @Override
    protected List<String> getPortNames() {
        log.debug("*	getPortNames()");
        return getAdapter().getPortNames();
    }

    private final static Logger log
            = LoggerFactory.getLogger(AnymaDMX_ConnectionConfig.class);
}
