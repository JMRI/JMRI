package jmri.jmrix.sprog;

import java.util.Comparator;
import java.util.ResourceBundle;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.ThrottleManager;
import jmri.TurnoutManager;
import jmri.jmrix.sprog.SprogConstants.SprogMode;
import jmri.jmrix.sprog.update.SprogType;
import jmri.jmrix.sprog.update.SprogVersion;
import jmri.jmrix.sprog.update.SprogVersionQuery;
import jmri.jmrix.swing.ComponentFactory;
import jmri.util.NamedBeanComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class SprogSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public SprogSystemConnectionMemo(SprogTrafficController st, SprogMode sm) {
        super(st.getController().getSystemConnectionMemo().getSystemPrefix(), SprogConnectionTypeList.SPROG);
        if (log.isDebugEnabled()) {
            log.debug("SprogSystemConnectionMemo, prefix='{}'", st.getController().getSystemConnectionMemo().getSystemPrefix());
        }
        this.st = st;
        init(sm, new SprogType(SprogType.UNKNOWN));
    }

    public SprogSystemConnectionMemo(SprogMode sm) {
        this(sm, new SprogType(SprogType.UNKNOWN));
    }

    public SprogSystemConnectionMemo(SprogMode sm, SprogType type) {
        super("S", SprogConnectionTypeList.SPROG); // default to S
        init(sm, type);
    }
    
    private void init(SprogMode sm, SprogType type) {
        sprogMode = sm;  // static
        sprogVersion = new SprogVersion(type);
        cf = new jmri.jmrix.sprog.swing.SprogComponentFactory(this);
        register();
        InstanceManager.store(this, SprogSystemConnectionMemo.class); // also register as specific type
        InstanceManager.store(cf, ComponentFactory.class);
    }

    public SprogSystemConnectionMemo() {
        this(SprogMode.OPS);
    }

    /**
     * Set the SPROG mode for this connection.
     *
     * @param mode selected mode
     */
    public void setSprogMode(SprogMode mode) {
        sprogMode = mode;
    }

    /**
     * Return the SPROG mode for this connection.
     *
     * @return SprogMode
     */
    public SprogMode getSprogMode() {
        return sprogMode;
    }
    private SprogMode sprogMode;

    /**
     * Return the SPROG version object for this connection.
     *
     * @return SprogVersion
     */
    public SprogVersion getSprogVersion() {
        return sprogVersion;
    }

    /**
     * Set the SPROG version object for this connection.
     *
     * @param version type and version class
     */
    public void setSprogVersion(SprogVersion version) {
        sprogVersion = version;
    }

    private SprogVersion sprogVersion;

    /**
     * Return the type of SPROG connected.
     *
     * @return SprogType set
     */
    public SprogType getSprogType() {
        return sprogVersion.sprogType;
    }

    ComponentFactory cf = null;

    /**
     * Provide access to the TrafficController for this particular connection.
     *
     * @return current tc for this connection
     */
    public SprogTrafficController getSprogTrafficController() {
        return st;
    }

    public void setSprogTrafficController(SprogTrafficController st) {
        this.st = st;
    }

    private SprogTrafficController st;
    private SprogCommandStation commandStation;

    private Thread slotThread;

    public Thread getSlotThread() {
        return slotThread;
    }
    
    /**
     * Configure the programming manager and "command station" objects.
     */
    public void configureCommandStation() {
        log.debug("start command station queuing thread");
        commandStation = new jmri.jmrix.sprog.SprogCommandStation(st);
        commandStation.setSystemConnectionMemo(this);
        jmri.InstanceManager.store(commandStation, jmri.CommandStation.class);
        switch (sprogMode) {
            case OPS:
                slotThread = new Thread(commandStation);
                slotThread.setName("SPROG slot thread");
                slotThread.setPriority(Thread.MAX_PRIORITY-2);
                slotThread.start();
                break;
            case SERVICE:
                break;
            default:
                log.error("Unhandled sprogMode: {}", sprogMode);
                break;
        }
    }

    /**
     * Get the command station object associated with this connection.
     * 
     * @return the command station
     */
    public SprogCommandStation getCommandStation() {
        return commandStation;
    }

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (type.equals(jmri.GlobalProgrammerManager.class)) {
            return getProgrammerManager().isGlobalProgrammerAvailable();
        }
        if (type.equals(jmri.AddressedProgrammerManager.class)) {
            return getProgrammerManager().isAddressedModePossible();
        }
        if (type.equals(jmri.PowerManager.class)) {
            return true;
        }
        if (type.equals(jmri.ThrottleManager.class)) {
            log.debug("SPROG provides throttle. sprogMode: {}", sprogMode);
            return true;
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        }
        if ((type.equals(jmri.CommandStation.class))) {
            if (sprogMode == null) {
                return false;
            }
            switch (sprogMode) {
                case OPS:
                    return true;
                case SERVICE:
                    return false;
                default:
                    log.error("Unhandled sprogMode: {}", sprogMode);
                    break;
            }
        }
        return super.provides(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> type) {
        if (getDisabled()) {
            return null;
        }
        if (type.equals(jmri.GlobalProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (type.equals(jmri.AddressedProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (type.equals(jmri.PowerManager.class)) {
            return (T) getPowerManager();
        }
        if (type.equals(jmri.ThrottleManager.class)) {
            return (T) getThrottleManager();
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        if (type.equals(jmri.CommandStation.class)) {
            return (T) getCommandStation();
        }
        return super.get(type);
    }

    /**
     * Configure the common managers for Sprog connections.
     * <p>
     * This puts the common manager config in one place. This method is static
     * so that it can be referenced from classes that don't inherit, including
     * hexfile.HexFileFrame and locormi.LnMessageClient.
     */
    public void configureManagers() {

        if (getProgrammerManager().isAddressedModePossible()) {
            jmri.InstanceManager.store(getProgrammerManager(), jmri.AddressedProgrammerManager.class);
        }
        if (getProgrammerManager().isGlobalProgrammerAvailable()) {
            InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        }

        powerManager = new jmri.jmrix.sprog.SprogPowerManager(this);
        jmri.InstanceManager.store(powerManager, jmri.PowerManager.class);

        sprogTurnoutManager = new jmri.jmrix.sprog.SprogTurnoutManager(this);
        jmri.InstanceManager.setTurnoutManager(sprogTurnoutManager);

        switch (sprogMode) {
            case OPS:
                sprogCSThrottleManager = new jmri.jmrix.sprog.SprogCSThrottleManager(this);
                jmri.InstanceManager.setThrottleManager(sprogCSThrottleManager);
                break;
            case SERVICE:
                sprogThrottleManager = new jmri.jmrix.sprog.SprogThrottleManager(this);
                jmri.InstanceManager.setThrottleManager(sprogThrottleManager);
                break;
            default:
                log.warn("Unhandled programming mode: {}", sprogMode);
                break;
        }
    }

    private SprogProgrammerManager programmerManager;
    private SprogCSThrottleManager sprogCSThrottleManager;
    private SprogThrottleManager sprogThrottleManager;
    private SprogTurnoutManager sprogTurnoutManager;
    private SprogPowerManager powerManager;

    public SprogProgrammerManager getProgrammerManager() {
        if (programmerManager == null) {
            programmerManager = new SprogProgrammerManager(new SprogProgrammer(this), sprogMode, this);
        }
        return programmerManager;
    }

    public void setProgrammerManager(SprogProgrammerManager p) {
        programmerManager = p;
    }

    public SprogPowerManager getPowerManager() {
        return powerManager;
    }

    public ThrottleManager getThrottleManager() {
        if (sprogMode == null) {
            log.error("Sprog Mode not set");
            return null;
        }
        switch (sprogMode) {
            case OPS:
                return sprogCSThrottleManager;
            case SERVICE:
                return sprogThrottleManager;
            default:
                log.warn("Unhandled programming mode: {}", sprogMode);
                break;
        }
        return null;
    }

    public TurnoutManager getTurnoutManager() {
        return sprogTurnoutManager;
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        //No actions that can be loaded at startup
        return null;
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        st = null;
        InstanceManager.deregister(this, SprogSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    private SprogVersionQuery svq = null;

    /**
     * @return a SprogVersionQuery object for this connection
     */
    public SprogVersionQuery getSprogVersionQuery() {
        if (svq == null) {
            svq = new SprogVersionQuery(this);
        }
        return svq;
    }

    private static final Logger log = LoggerFactory.getLogger(SprogSystemConnectionMemo.class);

}
