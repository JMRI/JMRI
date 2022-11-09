package jmri.jmrix.can;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.jmrix.can.ConfigurationManager.SubProtocol;
import jmri.jmrix.can.ConfigurationManager.ProgModeSwitch;
import jmri.util.NamedBeanComparator;

import jmri.util.startup.StartupActionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CanSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {
    // This user name will be overwritten by the adapter and saved to the connection config.
    public static final String DEFAULT_USERNAME = "CAN";

    private boolean protocolOptionsChanged = false;

    /**
     * Create a new CanSystemConnectionMemo.
     * Default prefix: M
     * Default Username: CAN
     */
    public CanSystemConnectionMemo() {
        super("M", DEFAULT_USERNAME);
    }

    /**
     * Create a new CanSystemConnectionMemo.
     * Allows for default systemPrefix other than "M"
     * Default Username: CAN
     * @param prefix System prefix to use, e.g. M.
     */
    public CanSystemConnectionMemo(String prefix) {
        super(prefix, DEFAULT_USERNAME);
    }

    protected final void storeCanMemotoInstance() {
        register(); // registers general type
        InstanceManager.store(this, CanSystemConnectionMemo.class); // also register as specific type
    }

    protected String _protocol = ConfigurationManager.MERGCBUS;
    protected SubProtocol _subProtocol = SubProtocol.CBUS;
    protected ProgModeSwitch _progModeSwitch = ProgModeSwitch.NONE;
    protected boolean _supportsCVHints = false; // Support for CV read hint values
    private boolean _multipleThrottles = true;  // Support for multiple throttles
    private boolean _powerOnArst = true;        // Turn power on if ARST opcode received

    jmri.jmrix.swing.ComponentFactory cf = null;

    protected TrafficController tm;

    /**
     * Set Connection Traffic Controller.
     * @param tm System Connection Traffic Controller
     */
    public void setTrafficController(TrafficController tm) {
        this.tm = tm;
    }

    /**
     * Get Connection Traffic Controller.
     * @return System Connection Traffic Controller
     */
    public TrafficController getTrafficController() {
        return tm;
    }

    private jmri.jmrix.can.ConfigurationManager manager;

    private final Map<String, Map<String, String>> protocolOptions = new HashMap<>();

    /**
     * {@inheritDoc }
     * Searches ConfigurationManager for class before super.
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
            jmri.GlobalProgrammerManager mgr = get(jmri.GlobalProgrammerManager.class);
            if (mgr == null) return false;
            return mgr.isGlobalProgrammerAvailable();
        }
        if (type.equals(jmri.AddressedProgrammerManager.class)) {
            jmri.AddressedProgrammerManager mgr = get(jmri.AddressedProgrammerManager.class);
            if (mgr == null) return false;
            return mgr.isAddressedModePossible();
        }

        boolean result = manager.provides(type);
        if(result) {
           return true;
        } else {
           return super.provides(type);
        }
    }

    /**
     * {@inheritDoc }
     * Searches ConfigurationManager for class before super.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<T> T) {
        if (manager != null && !getDisabled()) {
            T existing = manager.get(T);
            if ( existing !=null ) {
                return existing;
            }
        }
        return super.get(T);
    }

    /**
     * Get a class directly from the memo Class Object Map.
     * @param <T> Class type
     * @param T Class type
     * @return object in class object map, or null if not present.
     */
    @SuppressWarnings("unchecked")
    public <T> T getFromMap(Class<?> T) {
        return (T) classObjectMap.get(T);
    }

    /**
     * Get the Protocol in use by the CAN Connection.
     * e.g. ConfigurationManager.SPROGCBUS
     * @return ConfigurationManager constant of protocol.
     */
    public String getProtocol() {
        return _protocol;
    }

    /**
     * Set the protocol in use by the connection.
     * @param protocol e.g. ConfigurationManager.SPROGCBUS
     */
    public void setProtocol(String protocol) {
        StartupActionFactory old = getActionFactory();
        if (null != protocol) {
            _protocol = protocol;
            switch (protocol) {
                case ConfigurationManager.SPROGCBUS:
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
        firePropertyChange("actionFactory", old, getActionFactory());
    }

    /**
     * Get ENUM of the sub protocol.
     * @return the sub protocol in use, e.g. SubProtocol.CBUS
     */
    public SubProtocol getSubProtocol() {
        return _subProtocol;
    }

    /**
     * Set the sub protocol ENUM.
     * @param sp e.g. SubProtocol.CBUS
     */
    public void setSubProtocol(SubProtocol sp) {
        if (null != sp) {
            _subProtocol = sp;
        }
    }

    /**
     * Get the state of the programming mode switch which indicates what combination
     * of service and/or ops mode programming is supported by the connection.
     *
     * @return the supported modes
     */
    public ProgModeSwitch getProgModeSwitch() {
        return _progModeSwitch;
    }

    public void setProgModeSwitch(ProgModeSwitch pms) {
        if (null != pms) {
            _progModeSwitch = pms;
        }
    }

    /**
     * Some connections support only a single throttle, e.g., a service mode programmer
     * that allows for test running of a single loco.
     *
     * @return true if mutltiple throttles are available
     */
    public boolean hasMultipleThrottles() {
        return _multipleThrottles;
    }

    public void setMultipleThrottles(boolean b) {
        _multipleThrottles = b;
    }

    /**
     * Get the CV hint support flag
     *
     * @return true if CV hints are supported
     */
    public boolean supportsCVHints() {
        return _supportsCVHints;
    }

    public void setSupportsCVHints(boolean b) {
        _supportsCVHints = b;
    }

    /**
     * Get the behaviour on ARST opcode
     *
     * @return true if track power is on after ARST
     */
    public boolean powerOnArst() {
        return _powerOnArst;
    }

    public void setPowerOnArst(boolean b) {
        _powerOnArst = b;
    }

    /**
     * Configure the common managers for Can connections.
     * {@inheritDoc }
     * Calls ConfigurationManager.configureManagers
     * Stores Can Memo to Instance to indicate available.
     *
     */
    @Override
    public void configureManagers() {
        if (manager != null) {
            manager.configureManagers();
        }
        storeCanMemotoInstance();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        if (manager == null) {
            return null;
        }
        return manager.getActionModelResourceBundle();
    }

    /**
     * {@inheritDoc }
     */
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
        Map<String, String> m = protocolOptions.computeIfAbsent(protocol, k -> new HashMap<>());
        String oldValue = m.get(option);
        if (value.equals(oldValue)) return;
        m.put(option, value);
        protocolOptionsChanged = true;
    }

    @Override
    public boolean isDirty() {
        return super.isDirty() || protocolOptionsChanged;
    }

    @Override
    public boolean isRestartRequired() {
        return super.isRestartRequired() || protocolOptionsChanged;
    }

    /**
     * Custom interval of 100ms.
     * {@inheritDoc}
     */
    @Override
    public int getDefaultOutputInterval(){
        return 100;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {
        super.dispose(); // remove class map object items before manager config
        if (manager != null) {
            manager.dispose();
        }
        tm = null;
    }

    private static final Logger log = LoggerFactory.getLogger(CanSystemConnectionMemo.class);

}
