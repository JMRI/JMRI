package jmri.jmrix.mrc;

import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.*;
import jmri.util.NamedBeanComparator;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Ken Cameron Copyright (C) 2014
 * @author Kevin Dickerson Copyright (C) 2014
 *
 */
public class MrcSystemConnectionMemo extends jmri.jmrix.DefaultSystemConnectionMemo {

    public MrcSystemConnectionMemo() {
        super("M", "MRC"); // NOI18N
        register(); // registers general type
        InstanceManager.store(this, MrcSystemConnectionMemo.class); // also register as specific type

        // create and register the ComponentFactory
        InstanceManager.store(componentFactory = new jmri.jmrix.mrc.swing.MrcComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory componentFactory = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     *
     * @return current traffic controller for this connection
     */
    public MrcTrafficController getMrcTrafficController() {
        if (mrcTrafficController == null) log.error("found tc null in request", new Exception("traceback"));
        return mrcTrafficController;
    }
    private MrcTrafficController mrcTrafficController;

    public void setMrcTrafficController(MrcTrafficController tc) {
        mrcTrafficController = tc;
        if (tc != null) {
            tc.setAdapterMemo(this);
        }
    }

    public MrcProgrammerManager getProgrammerManager() {
        //Do not want to return a programmer if the system is disabled
        if (getDisabled()) {
            return null;
        }
        return (MrcProgrammerManager)classObjectMap.computeIfAbsent(MrcProgrammerManager.class,
                (Class c) -> new MrcProgrammerManager(new MrcProgrammer(this), this));
    }

    public void setProgrammerManager(MrcProgrammerManager p) {
        store(p, MrcProgrammerManager.class);
    }

    /**
     * Configure the common managers for MRC connections. This puts the common
     * manager config in one place.
     */
    public void configureManagers() {
        PowerManager powerManager = new jmri.jmrix.mrc.MrcPowerManager(this);
        store(powerManager,PowerManager.class);
        InstanceManager.store(powerManager, PowerManager.class);

        TurnoutManager turnoutManager = new jmri.jmrix.mrc.MrcTurnoutManager(this);
        store(turnoutManager,TurnoutManager.class);
        InstanceManager.setTurnoutManager(turnoutManager);

        ThrottleManager throttleManager = new jmri.jmrix.mrc.MrcThrottleManager(this);
        store(throttleManager,ThrottleManager.class);
        InstanceManager.setThrottleManager(throttleManager);

        if (getProgrammerManager().isAddressedModePossible()) {
            store(getProgrammerManager(),AddressedProgrammerManager.class);
            InstanceManager.store(getProgrammerManager(), AddressedProgrammerManager.class);
        }
        if (getProgrammerManager().isGlobalProgrammerAvailable()) {
            store(getProgrammerManager(),GlobalProgrammerManager.class);
            InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        }

        ClockControl clockManager = new jmri.jmrix.mrc.MrcClockControl(getMrcTrafficController(), getSystemPrefix());
        store(clockManager,ClockControl.class);
        InstanceManager.store(clockManager, jmri.ClockControl.class);
        InstanceManager.setDefault(jmri.ClockControl.class, clockManager);

    }

    public MrcPowerManager getPowerManager() {
        return get(PowerManager.class);
    }

    public MrcTurnoutManager getTurnoutManager() {
        return get(TurnoutManager.class);
    }

    public MrcThrottleManager getThrottleManager() {
        return get(ThrottleManager.class);
    }

    public MrcClockControl getClockControl() {
        return get(ClockControl.class);
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.mrc.MrcActionListBundle"); //NO18N
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        mrcTrafficController = null;
        InstanceManager.deregister(this, MrcSystemConnectionMemo.class);
        if (componentFactory != null) {
            InstanceManager.deregister(componentFactory, jmri.jmrix.swing.ComponentFactory.class);
        }

        super.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MrcSystemConnectionMemo.class.getName());

}
