package jmri.jmrix.nce;

import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.util.NamedBeanComparator;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author ken cameron Copyright (C) 2013
 */
public class NceSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    public NceSystemConnectionMemo() {
        super("N", "NCE");
        register(); // registers general type
        InstanceManager.store(this, NceSystemConnectionMemo.class); // also register as specific type

        // create and register the ComponentFactory
        InstanceManager.store(componentFactory = new jmri.jmrix.nce.swing.NceComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory componentFactory;

    public void setNceUsbSystem(int result) {
        getNceTrafficController().setUsbSystem(result);
    }

    public int getNceUsbSystem() {
        if (getNceTrafficController() != null) {
            return getNceTrafficController().getUsbSystem();
        }
        return NceTrafficController.USB_SYSTEM_NONE;
    } // error no connection!

    public void setNceCmdGroups(long result) {
        getNceTrafficController().setCmdGroups(result);
    }

    public long getNceCmdGroups() {
        if (getNceTrafficController() != null) {
            return getNceTrafficController().getCmdGroups();
        }
        return NceTrafficController.CMDS_NONE;
    } // error no connection!

    /**
     * Provides access to the TrafficController for this particular connection.
     *
     * @return tc for this connection
     */
    public NceTrafficController getNceTrafficController() {
        return nceTrafficController;
    }
    private NceTrafficController nceTrafficController;

    public void setNceTrafficController(NceTrafficController tc) {
        nceTrafficController = tc;
        if (tc != null) {
            tc.setAdapterMemo(this);
        }
    }

    public NceProgrammerManager getProgrammerManager() {
        //Do not want to return a programmer if the system is disabled
        if (getDisabled()) {
            return null;
        }
        return (NceProgrammerManager) classObjectMap.computeIfAbsent(NceProgrammerManager.class,(Class c) -> new NceProgrammerManager(this));
    }

    public void setProgrammerManager(NceProgrammerManager p) {
        store(p,NceProgrammerManager.class);
    }

    /**
     * Sets the NCE message option.
     *
     * @param val command option value
     */
    public void configureCommandStation(int val) {
        getNceTrafficController().setCommandOptions(val);
        store(nceTrafficController,CommandStation.class);
        jmri.InstanceManager.store(nceTrafficController, jmri.CommandStation.class);
    }

    /**
     * Configure the common managers for NCE connections. This puts the common
     * manager config in one place.
     */
    public void configureManagers() {
        log.trace("configureManagers() with: {} ", getNceUsbSystem());
        PowerManager powerManager = new jmri.jmrix.nce.NcePowerManager(this);
        store(powerManager,PowerManager.class);
        InstanceManager.store(powerManager, jmri.PowerManager.class);

        TurnoutManager turnoutManager = new jmri.jmrix.nce.NceTurnoutManager(this);
        store(turnoutManager,TurnoutManager.class);
        InstanceManager.setTurnoutManager(turnoutManager);

        LightManager lightManager = new jmri.jmrix.nce.NceLightManager(this);
        store(lightManager,LightManager.class);
        InstanceManager.setLightManager(lightManager);

        SensorManager sensorManager = new jmri.jmrix.nce.NceSensorManager(this);
        store(sensorManager,SensorManager.class);
        InstanceManager.setSensorManager(sensorManager);

        NceThrottleManager throttleManager = new jmri.jmrix.nce.NceThrottleManager(this);
        store(throttleManager,ThrottleManager.class);
        InstanceManager.setThrottleManager(throttleManager);

        // non-USB case
        if (getProgrammerManager().isAddressedModePossible()) {
            log.trace("store AddressedProgrammerManager");
            store(getProgrammerManager(),AddressedProgrammerManager.class);
            InstanceManager.store(getProgrammerManager(), AddressedProgrammerManager.class);
        }
        if (getProgrammerManager().isGlobalProgrammerAvailable()) {
            log.trace("store GlobalProgrammerManager");
            store(getProgrammerManager(),GlobalProgrammerManager.class);
            InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        }

        NceClockControl clockManager = new NceClockControl(getNceTrafficController(), getSystemPrefix());
        store(clockManager,ClockControl.class);
        // make sure InstanceManager knows about that
        InstanceManager.store(clockManager, jmri.ClockControl.class);
        InstanceManager.setDefault(jmri.ClockControl.class, clockManager);

        setConsistManager(new jmri.jmrix.nce.NceConsistManager(this));

        log.trace("configureManagers() end");
    }

    public NcePowerManager getPowerManager() {
        return get(PowerManager.class);
    }

    public NceTurnoutManager getTurnoutManager() {
        return get(TurnoutManager.class);
    }

    public NceLightManager getLightManager() {
        return get(LightManager.class);
    }

    public NceSensorManager getSensorManager() {
        return get(SensorManager.class);
    }

    public NceThrottleManager getThrottleManager() {
        return get(ThrottleManager.class);
    }

    public NceClockControl getClockControl() {
        return get(ClockControl.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.nce.NceActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        nceTrafficController = null;
        InstanceManager.deregister(this, NceSystemConnectionMemo.class);
        if (componentFactory != null) {
            InstanceManager.deregister(componentFactory, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NceSystemConnectionMemo.class);
}
