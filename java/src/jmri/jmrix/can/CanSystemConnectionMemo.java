package jmri.jmrix.can;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.util.NamedBeanComparator;

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
    // This user name will be overwritten by the adapter and saved to the connection config.
    public static String DEFAULT_USERNAME = "CAN";

    public CanSystemConnectionMemo() {
        super("M", DEFAULT_USERNAME);
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

    private final Map<String, Map<String, String>> protocolOptions = new HashMap<>();

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
        if (type.equals(jmri.ConsistManager.class)) { // until a CAN ConsistManager is implemented, use Internal
            return false;
        }
        boolean result = manager.provides(type);
        if(result) {
           return result;
        } else {
           return super.provides(type);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (manager != null && !getDisabled()) {
            return (T) manager.get(T);
        }
        return super.get(T);
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
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    /**
     * Enumerate all protocols that have options set.
     *
     * @return set of protocol names.
     */
    public Set<String> getProtocolsWithOptions() {
        return protocolOptions.keySet();
    }

    /**
     * Get all options we have set (saved in the connection XML) for a given protocol type.
     *
     * @param protocol String name of the protocol.
     * @return map of known protocol options to values, or empty map.
     */
    @Nonnull
    public Map<String, String> getProtocolAllOptions(String protocol) {
        return protocolOptions.getOrDefault(protocol, new HashMap<>());
    }

    /**
     * Get a single option of a single protocol, or null if not present.
     *
     * @param protocol name of the protocol.
     * @param option name of the option.
     * @return null if option has never been set; or the option value if set.
     */
    public synchronized String getProtocolOption(String protocol, String option) {
        if (!protocolOptions.containsKey(protocol)) return null;
        Map<String, String> m = getProtocolAllOptions(protocol);
        return m.getOrDefault(option, null);
    }

    /**
     * Sets a protocol option. This list will be persisted when the connection gets saved.
     *
     * @param protocol name of the protocol
     * @param option name of the option
     * @param value option value
     */
    public synchronized void setProtocolOption(String protocol, String option, String value) {
        log.debug("Setting protocol option {} {} := {}", protocol, option, value);
        if (value == null) return;
        Map<String, String> m = protocolOptions.get(protocol);
        if (m == null) {
            m = new HashMap<>();
            protocolOptions.put(protocol, m);
        }
        String oldValue = m.get(option);
        if (value.equals(oldValue)) return;
        m.put(option, value);
        // @todo When the connection options are changed, we need to mark the profile as dirty.
    }

    @Override
    public void dispose() {
        if (manager != null) {
            manager.dispose();
        }
        tm = null;
        super.dispose();
    }

    private static final Logger log = LoggerFactory.getLogger(CanSystemConnectionMemo.class);

}
