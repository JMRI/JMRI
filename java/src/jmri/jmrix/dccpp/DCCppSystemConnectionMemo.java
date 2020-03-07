package jmri.jmrix.dccpp;

import java.util.Comparator;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.MultiMeter;
import jmri.NamedBean;
import jmri.PowerManager;
import jmri.SensorManager;
import jmri.ThrottleManager;
import jmri.TurnoutManager;
import jmri.util.NamedBeanComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 * Based on XNetSystemConnectionMemo by Paul Bender.
 *
 * @author Paul Bender Copyright (C) 2010
 * @author Mark Underwood Copyright (C) 2015
 */
public class DCCppSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public DCCppSystemConnectionMemo(@Nonnull DCCppTrafficController xt) {
        super("D", "DCC++");
        this.xt = xt;
        xt.setSystemConnectionMemo(this);
        register(); // registers general type
        InstanceManager.store(this, DCCppSystemConnectionMemo.class); // also register as specific type

        // create and register the DCCppComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.dccpp.swing.DCCppComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created DCCppSystemConnectionMemo");
    }

    public DCCppSystemConnectionMemo() {
        super("D", "DCC++");
        register(); // registers general type
        InstanceManager.store(this, DCCppSystemConnectionMemo.class); // also register as specific type

        // create and register the DCCppComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.dccpp.swing.DCCppComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created DCCppSystemConnectionMemo");
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provide access to the TrafficController for this particular connection.
     */
    public DCCppTrafficController getDCCppTrafficController() {
        if (xt == null) {
            setDCCppTrafficController(new DCCppPacketizer(new DCCppCommandStation(this))); // default to DCCppPacketizer TrafficController
            log.debug("Auto create of DCCppTrafficController for initial configuration");
        }
        return xt;
    }

    private DCCppTrafficController xt;

    /**
     * Set the traffic controller instance associated with this connection memo.
     *
     * @param xt the {@link jmri.jmrix.dccpp.DCCppTrafficController} object to use.
     */
    public void setDCCppTrafficController(@Nonnull DCCppTrafficController xt) {
        this.xt = xt;
        // in addition to setting the traffic controller in this object,
        // set the systemConnectionMemo in the traffic controller
        xt.setSystemConnectionMemo(this);
    }

    /**
     * Provides access to the Programmer for this particular connection. NOTE:
     * Programmer defaults to null
     */
    public DCCppProgrammerManager getProgrammerManager() {
        return programmerManager;
    }

    public void setProgrammerManager(DCCppProgrammerManager p) {
        programmerManager = p;
    }

    private DCCppProgrammerManager programmerManager = null;

    /*
     * Provides access to the Throttle Manager for this particular connection.
     */
    public ThrottleManager getThrottleManager() {
        if (throttleManager == null) {
            throttleManager = new DCCppThrottleManager(this); // TODO: Create this throttle manager
        }
        return throttleManager;
    }

    public void setThrottleManager(ThrottleManager t) {
        throttleManager = t;
    }

    private ThrottleManager throttleManager;

    /*
     * Provides access to the PowerManager for this particular connection.
     */
    @Nonnull
    public PowerManager getPowerManager() {
        if (powerManager == null) {
            powerManager = new DCCppPowerManager(this);
        }
        log.debug("power manager created: {}", powerManager);
        return powerManager;

    }

    public void setPowerManager(@Nonnull PowerManager p) {
        powerManager = p;
    }

    private PowerManager powerManager;

    /*
     * Provides access to the SensorManager for this particular connection.
     * NOTE: SensorManager defaults to NULL
     */
    public SensorManager getSensorManager() {
        return sensorManager;

    }

    public void setSensorManager(SensorManager s) {
        sensorManager = s;
    }

    private SensorManager sensorManager = null;

    /*
     * Provides access to the TurnoutManager for this particular connection.
     * NOTE: TurnoutManager defaults to NULL
     */
    public TurnoutManager getTurnoutManager() {
        return turnoutManager;

    }

    public void setTurnoutManager(TurnoutManager t) {
        turnoutManager = t;
    }

    private TurnoutManager turnoutManager = null;

    /*
     * Provides access to the LightManager for this particular connection.
     * NOTE: Light manager defaults to NULL
     */
    public LightManager getLightManager() {
        return lightManager;

    }

    public void setLightManager(LightManager l) {
        lightManager = l;
    }

    private LightManager lightManager = null;

    /*
     * Provides access to the Command Station for this particular connection.
     * NOTE: Command Station defaults to NULL
     */
    public CommandStation getCommandStation() {
        return commandStation;
    }

    public void setCommandStation(@Nonnull CommandStation c) {
        commandStation = c;
        ((DCCppCommandStation) c).setTrafficController(xt);
        ((DCCppCommandStation) c).setSystemConnectionMemo(this);
    }

    private CommandStation commandStation = null;

    private MultiMeter multiMeter = null;

    public MultiMeter getMultiMeter() {
        return(multiMeter);
    }

    public void setMultiMeter(MultiMeter m) {
        multiMeter = m;
    }

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        } else if (type.equals(jmri.GlobalProgrammerManager.class)) {
            DCCppProgrammerManager p = getProgrammerManager();
            if (p == null) {
                return false;
            }
            return p.isGlobalProgrammerAvailable();
        } else if (type.equals(jmri.AddressedProgrammerManager.class)) {
            DCCppProgrammerManager p = getProgrammerManager();
            if (p == null) {
                return false;
            }
            return p.isAddressedModePossible();
            //TODO: Update return value of the following as Managers are brought online.
        } else if (type.equals(jmri.ThrottleManager.class)) {
            return true;
        } else if (type.equals(jmri.PowerManager.class)) {
            return true;
        } else if (type.equals(jmri.SensorManager.class)) {
            return true;
        } else if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        } else if (type.equals(jmri.LightManager.class)) {
            return true;
        } else if (type.equals(jmri.CommandStation.class)) {
            return true;
        } else if (type.equals(jmri.MultiMeter.class)) {
            return true;
        } else {
            return super.provides(type);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (T.equals(jmri.GlobalProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.AddressedProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.ThrottleManager.class)) {
            return (T) getThrottleManager();
        }
        if (T.equals(jmri.PowerManager.class)) {
            return (T) getPowerManager();
        }
        if (T.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        if (T.equals(jmri.LightManager.class)) {
            return (T) getLightManager();
        }
        if (T.equals(jmri.CommandStation.class)) {
            return (T) getCommandStation();
        }
        if (T.equals(jmri.MultiMeter.class)) {
            return (T) getMultiMeter();
        }
        return super.get(T);
    }

    @Override
    @Nonnull
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.dccpp.DCCppActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        xt = null;
        InstanceManager.deregister(this, DCCppSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppSystemConnectionMemo.class);

}

