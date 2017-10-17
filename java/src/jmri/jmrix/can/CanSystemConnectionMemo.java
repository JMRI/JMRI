package jmri.jmrix.can;

import java.util.ResourceBundle;
import jmri.InstanceManager;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * As various CAN adapters, can work with different CAN Bus systems, the adapter
 * memo is generic for all adapters, it then uses a ConfigurationManager for
 * each of the CAN Bus systems. Any requests for provision or configuration is
 * passed on to the relevant ConfigurationManager to handle.
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class CanSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public CanSystemConnectionMemo() {
        super("M", "MERG");
        register(); // registers general type
        InstanceManager.store(this, CanSystemConnectionMemo.class); // also register as specific type
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    protected TrafficController tm;

    public void setTrafficController(TrafficController tm) {
        this.tm = tm;
    }

    public TrafficController getTrafficController() {
        return tm;
    }

    private jmri.jmrix.can.ConfigurationManager manager;

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (manager == null) {
            return false;
        }
        if (type.equals(jmri.GlobalProgrammerManager.class)) {
            jmri.GlobalProgrammerManager mgr = ((jmri.GlobalProgrammerManager) get(jmri.GlobalProgrammerManager.class));
            if (mgr == null) return false;
            return mgr.isGlobalProgrammerAvailable();
        }
        if (type.equals(jmri.AddressedProgrammerManager.class)) {
            jmri.AddressedProgrammerManager mgr =((jmri.AddressedProgrammerManager) get(jmri.AddressedProgrammerManager.class));
            if (mgr == null) return false;
            return mgr.isAddressedModePossible();
        }
        return manager.provides(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (manager != null && !getDisabled()) {
            return (T) manager.get(T);
        }
        return null; // nothing, by default
    }

    public void setProtocol(String protocol) {
        if (null != protocol) {
            switch (protocol) {
                case ConfigurationManager.MERGCBUS:
                    manager = new jmri.jmrix.can.cbus.CbusConfigurationManager(this);
                    break;
                case ConfigurationManager.OPENLCB:
                    manager = new jmri.jmrix.openlcb.OlcbConfigurationManager(this);
                    break;
                case ConfigurationManager.RAWCAN:
                    manager = new jmri.jmrix.can.CanConfigurationManager(this);
                    break;
                case ConfigurationManager.TEST:
                    manager = new jmri.jmrix.can.nmranet.NmraConfigurationManager(this);
                    break;
                default:
                    break;
            }
        }
        // make sure appropriate actions in preferences
        addToActionList();
    }

    /**
     * Configure the common managers for Can connections. This puts the common
     * manager config in one place.
     */
    public void configureManagers() {
        if (manager != null) {
            manager.configureManagers();
        }
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        if (manager == null) {
            return null;
        }
        return manager.getActionModelResourceBundle();
    }

    @Override
    public void dispose() {
        if (manager != null) {
            manager.dispose();
        }
        tm = null;
        super.dispose();
    }

}
