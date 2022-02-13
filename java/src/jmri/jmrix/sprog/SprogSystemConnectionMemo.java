package jmri.jmrix.sprog;

import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.DefaultSystemConnectionMemo;
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
public class SprogSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

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
        InstanceManager.store(this, SprogSystemConnectionMemo.class);
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

    private Thread slotThread;

    public Thread getSlotThread() {
        return slotThread;
    }

    private int numSlots = SprogConstants.DEFAULT_MAX_SLOTS;

    /**
     * Get the number of command station slots
     *
     * @return The number fo slots
     */
    public int getNumSlots() {
        return numSlots;
    }

    /**
     * Configure the programming manager and "command station" objects.
     */
    public void configureCommandStation() {
        if(classObjectMap.containsKey(CommandStation.class)) {
            return;
        }
        log.debug("start command station queuing thread");
        SprogCommandStation commandStation = new jmri.jmrix.sprog.SprogCommandStation(st);
        commandStation.setSystemConnectionMemo(this);
        jmri.InstanceManager.store(commandStation, jmri.CommandStation.class);
        store(commandStation, jmri.CommandStation.class);
        switch (sprogMode) {
            case OPS:
                slotThread = jmri.util.ThreadingUtil.newThread(commandStation);
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

    public void configureCommandStation(int slots) {
        numSlots = slots;
        this.configureCommandStation();
    }

    /**
     * Get the command station object associated with this connection.
     *
     * @return the command station
     */
    public SprogCommandStation getCommandStation() {
        return get(CommandStation.class);
    }

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
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

    /**
     * Configure the common managers for Sprog connections.
     */
    public void configureManagers() {

        configureCommandStation();

        if (getProgrammerManager().isAddressedModePossible()) {
            jmri.InstanceManager.store(getProgrammerManager(), AddressedProgrammerManager.class);
            store(getProgrammerManager(), AddressedProgrammerManager.class);
        }
        if (getProgrammerManager().isGlobalProgrammerAvailable()) {
            InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
            store(getProgrammerManager(), GlobalProgrammerManager.class);
        }

        PowerManager powerManager = new jmri.jmrix.sprog.SprogPowerManager(this);
        jmri.InstanceManager.store(powerManager, PowerManager.class);
        store(powerManager, PowerManager.class);

        TurnoutManager sprogTurnoutManager = new SprogTurnoutManager(this);
        jmri.InstanceManager.setTurnoutManager(sprogTurnoutManager);
        store(sprogTurnoutManager,TurnoutManager.class);

        switch (sprogMode) {
            case OPS:
                ThrottleManager sprogCSThrottleManager = new jmri.jmrix.sprog.SprogCSThrottleManager(this);
                jmri.InstanceManager.setThrottleManager(sprogCSThrottleManager);
                store(sprogCSThrottleManager,ThrottleManager.class);
                break;
            case SERVICE:
                ThrottleManager sprogThrottleManager = new jmri.jmrix.sprog.SprogThrottleManager(this);
                jmri.InstanceManager.setThrottleManager(sprogThrottleManager);
                store(sprogThrottleManager,ThrottleManager.class);
                break;
            default:
                log.warn("Unhandled programming mode: {}", sprogMode);
                break;
        }
        register();
    }

    public SprogProgrammerManager getProgrammerManager() {

        return (SprogProgrammerManager) classObjectMap.computeIfAbsent(SprogProgrammerManager.class, (Class<?> c) -> new SprogProgrammerManager(new SprogProgrammer(this), sprogMode, this));
    }

    public void setProgrammerManager(SprogProgrammerManager p) {
        store(p,SprogProgrammerManager.class);
    }

    public SprogPowerManager getPowerManager() {
        return get(PowerManager.class);
    }

    public ThrottleManager getThrottleManager() {
        return get(ThrottleManager.class);
    }

    public TurnoutManager getTurnoutManager() {
        return get(TurnoutManager.class);
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.sprog.SprogActionListBundle");
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
