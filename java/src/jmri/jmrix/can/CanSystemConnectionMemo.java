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
 * <p>
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
     * Tells which managers this provides by class
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (manager == null) {
            return false;
        }
        if (type.equals(jmri.GlobalProgrammerManager.class) && provides(jmri.ProgrammerManager.class)) {
            return ((jmri.ProgrammerManager) get(jmri.ProgrammerManager.class)).isGlobalProgrammerAvailable();
        }
        if (type.equals(jmri.AddressedProgrammerManager.class) && provides(jmri.ProgrammerManager.class)) {
            return ((jmri.ProgrammerManager) get(jmri.ProgrammerManager.class)).isAddressedModePossible();
        }
        return manager.provides(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (manager != null) {
            if (T.equals(jmri.GlobalProgrammerManager.class)) {
                return (T) get(jmri.ProgrammerManager.class);
            }
            if (T.equals(jmri.AddressedProgrammerManager.class)) {
                return (T) get(jmri.ProgrammerManager.class);
            }
            return (T) manager.get(T);
        }
        return null; // nothing, by default
    }

    public void setProtocol(String protocol) {
        if (ConfigurationManager.MERGCBUS.equals(protocol)) {
            manager = new jmri.jmrix.can.cbus.CbusConfigurationManager(this);
        } else if (ConfigurationManager.OPENLCB.equals(protocol)) {
            manager = new jmri.jmrix.openlcb.OlcbConfigurationManager(this);
        } else if (ConfigurationManager.RAWCAN.equals(protocol)) {
            manager = new jmri.jmrix.can.CanConfigurationManager(this);
        } else if (ConfigurationManager.TEST.equals(protocol)) {
            manager = new jmri.jmrix.can.nmranet.NmraConfigurationManager(this);
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
