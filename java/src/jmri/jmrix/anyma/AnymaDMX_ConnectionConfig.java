package jmri.jmrix.anyma;

import java.util.List;
import java.util.Vector;
import javax.swing.JPanel;
import javax.usb.UsbDevice;
import jmri.jmrix.AbstractUsbConnectionConfig;
import jmri.jmrix.JmrixConfigPane;
import jmri.util.USBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Definition of objects to handle configuring an Anyma DMX layout connection
 * via a SerialDriverAdapter object.
 * <P>
 * This uses the {@link AnymaDMX_Adapter} class to do the actual connection.
 *
 * @author George Warner Copyright (c) 2017
 * @since 4.9.6
 * @see AnymaDMX_Adapter
 */
public class AnymaDMX_ConnectionConfig extends AbstractUsbConnectionConfig {

    private boolean disabled = false;
//    private AnymaDMX_Adapter adapter = null;
//    private Date DMXMessageShown = null;

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     *
     * @param p the pre-existing adapter
     */
    public AnymaDMX_ConnectionConfig(AnymaDMX_Adapter p) {
        super();
        log.info("* constructor('{}').", p);
        adapter = p;
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public AnymaDMX_ConnectionConfig() {
        this(new AnymaDMX_Adapter());
    }

    /**
     * Done with this ConnectionConfig object. Invoked in
     * {@link JmrixConfigPane} when switching away from this particular mode.
     */
    public void dispose() {
        log.info("* dispose()");
        super.dispose();
    }

    /**
     * Determine if configuration needs to be written to disk.
     *
     * @return true if configuration needs to be saved, false otherwise
     */
    public boolean isDirty() {
        log.info("* isDirty()");
        return super.isDirty();
    }

    /**
     * Determine if application needs to be restarted for configuration changes
     * to be applied.
     *
     * @return true if application needs to restart, false otherwise
     */
    public boolean isRestartRequired() {
        log.info("* isRestartRequired()");
        return super.isRestartRequired();
    }

    @Override
    public void updateAdapter() {
        log.info("* updateAdapter()");
        if ((adapter.getSystemConnectionMemo() != null)
                && !adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }

    }

    @Override
    protected void showAdvancedItems() {
        log.info("* showAdvancedItems()");
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
        log.info("* loadDetails()");
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
        log.info("* register()");
        super.register();
    }

    @Override
    protected void setInstance() {
        log.info("* setInstance()");
        if (adapter == null) {
            adapter = new AnymaDMX_Adapter();

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
    public AnymaDMX_Adapter getAdapter() {
        log.info("* getAdapter()");
        return (AnymaDMX_Adapter) adapter;
    }

    @Override
    public String getInfo() {
        log.info("* getInfo()");
        return "DMX";
    }

    @Override
    public String getManufacturer() {
        log.info("* getManufacturer()");
        return AnymaDMX_ConnectionTypeList.ANYMA_DMX;
    }

    @Override
    public void setManufacturer(String manufacturer) {
        log.info("* setManufacturer('{}')", manufacturer);
    }

    @Override
    public String name() {
        log.info("* name()");
        return getConnectionName();
    }

    @Override
    public String getConnectionName() {
        log.info("* getConnectionName()");
        return "Anyma DMX";
    }

    @Override
    public boolean getDisabled() {
        log.info("* getDisabled()");
        return disabled;
    }

    @Override
    public void setDisabled(boolean disable) {
        log.info("* setDisabled({})", disable ? "True" : "False");
        this.disabled = disable;
    }

    @Override
    protected Vector<String> getPortNames() {
        log.info("*	getPortNames()");
        Vector<String> results = new Vector<>();

        List<UsbDevice> usbDevices = USBUtil.getMatchingDevices((short) 0x16C0, (short) 0x05DC);
        for (UsbDevice usbDevice : usbDevices) {
            String location = USBUtil.getLocationID(usbDevice);
            if (!location.isEmpty()) {
                results.add(location);
            }
        }
        return results;
    }

    private final static Logger log = LoggerFactory.getLogger(AnymaDMX_ConnectionConfig.class);
}
