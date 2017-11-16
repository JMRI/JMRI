package jmri.jmrix.anyma;

import java.util.Vector;
import javax.swing.JPanel;
import jmri.jmrix.AbstractUsbConnectionConfig;
import jmri.jmrix.JmrixConfigPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Definition of objects to handle configuring an Anyma DMX layout connection
 * via a SerialDriverAdapter object.
 * <P>
 * This uses the {@link AnymaDMX_UsbPortAdapter} class to do the actual
 * connection.
 *
 * @author George Warner Copyright (c) 2017
 * @since 4.9.6
 * @see AnymaDMX_UsbPortAdapter
 */
public class AnymaDMX_ConnectionConfig extends AbstractUsbConnectionConfig {

    private boolean disabled = false;
//    private Date DMXMessageShown = null;

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     *
     * @param p the pre-existing adapter
     */
    public AnymaDMX_ConnectionConfig(AnymaDMX_UsbPortAdapter p) {
        super();
        log.debug("*    constructor('{}').", p);
        adapter = p;
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public AnymaDMX_ConnectionConfig() {
        this(new AnymaDMX_UsbPortAdapter());
    }

    /**
     * Done with this ConnectionConfig object. Invoked in
     * {@link JmrixConfigPane} when switching away from this particular mode.
     */
    public void dispose() {
        log.debug("*    dispose()");
        super.dispose();
    }

    /**
     * Determine if configuration needs to be written to disk.
     *
     * @return true if configuration needs to be saved, false otherwise
     */
    public boolean isDirty() {
        log.debug("*    isDirty()");
        return super.isDirty();
    }

    /**
     * Determine if application needs to be restarted for configuration changes
     * to be applied.
     *
     * @return true if application needs to restart, false otherwise
     */
    public boolean isRestartRequired() {
        log.debug("*    isRestartRequired()");
        return super.isRestartRequired();
    }

    @Override
    public void updateAdapter() {
        log.debug("*    updateAdapter()");
        if ((adapter.getSystemConnectionMemo() != null)
                && !adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }
    }

    @Override
    protected void showAdvancedItems() {
        log.debug("*    showAdvancedItems()");
        super.showAdvancedItems();
    }

    /**
     * Load the Swing widgets needed to configure this connection into a
     * specified JPanel. Used during the configuration process to fill out the
     * preferences window with content specific to this Connection type. The
     * JPanel contents need to handle their own gets/sets to the underlying
     * Connection content.
     *
     * @param details The specific Swing object to be configured and filled.
     */
    @Override
    public void loadDetails(final JPanel details) {
        log.debug("*    loadDetails()");
        super.loadDetails(details);
    }

    /**
     * Register the ConnectionConfig with the running JMRI process.
     * <p>
     * At a minimum, is responsible for:
     * <ul>
     * <li>Registering this object with the ConfigurationManager for
     * persistance, typically at the "Preferences" level
     * <li>Adding this object to the default (@link ConnectionConfigManager}
     * </ul>
     */
    @Override
    public void register() {
        log.debug("*    register()");
        super.register();
    }

    @Override
    protected void setInstance() {
        log.debug("*    setInstance()");
        if (adapter == null) {
            adapter = new AnymaDMX_UsbPortAdapter();
        }
//        if (adapter.getDMXController() == null) {
//            // don't show more than once every 30 seconds
//            if (!GraphicsEnvironment.isHeadless()
//                    && (this.DMXMessageShown == null || ((new Date().getTime() - this.DMXMessageShown.getTime()) / 1000 % 60) > 30)) {
//                JOptionPane.showMessageDialog(this._details,
//                        Bundle.getMessage("NoDMXControllerMessage"),
//                        Bundle.getMessage("NoDMXControllerTitle"),
//                        JOptionPane.ERROR_MESSAGE);
//                this.DMXMessageShown = new Date();
//            }
//        }
    }

    @Override
    public AnymaDMX_UsbPortAdapter getAdapter() {
        log.debug("*    getAdapter()");
        return (AnymaDMX_UsbPortAdapter) adapter;
    }

    @Override
    public String getInfo() {
        log.debug("*    getInfo()");
        return "DMX";
    }

    @Override
    public String getManufacturer() {
        log.debug("*    getManufacturer()");
        return AnymaDMX_ConnectionTypeList.ANYMA_DMX;
    }

    @Override
    public void setManufacturer(String manufacturer) {
        log.debug("*    setManufacturer('{}')", manufacturer);
    }

    @Override
    public String name() {
        log.debug("*    name()");
        return getConnectionName();
    }

    @Override
    public String getConnectionName() {
        log.debug("*    getConnectionName()");
        return "Anyma DMX";
    }

    @Override
    public boolean getDisabled() {
        log.debug("*    getDisabled()");
        return disabled;
    }

    @Override
    public void setDisabled(boolean disable) {
        log.debug("*    setDisabled({})", disable ? "True" : "False");
        this.disabled = disable;
    }

    @Override
    protected Vector<String> getPortNames() {
        log.debug("*	getPortNames()");
        return new Vector<String>(getAdapter().getPortNames());
    }

    private final static Logger log
            = LoggerFactory.getLogger(AnymaDMX_ConnectionConfig.class);
}
