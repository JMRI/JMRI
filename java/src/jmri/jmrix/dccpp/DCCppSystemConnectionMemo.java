package jmri.jmrix.dccpp;

import java.util.Comparator;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrix.DefaultSystemConnectionMemo;
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
public class DCCppSystemConnectionMemo extends DefaultSystemConnectionMemo {

    public DCCppSystemConnectionMemo(@Nonnull DCCppTrafficController xt) {
        super("D", "DCC++");
        this.xt = xt;
        xt.setSystemConnectionMemo(this);
        InstanceManager.store(this, DCCppSystemConnectionMemo.class); // also register as specific type

        // create and register the DCCppComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.dccpp.swing.DCCppComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created DCCppSystemConnectionMemo");
    }

    public DCCppSystemConnectionMemo() {
        super("D", "DCC++");
        InstanceManager.store(this, DCCppSystemConnectionMemo.class); // also register as specific type

        // create and register the DCCppComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.dccpp.swing.DCCppComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created DCCppSystemConnectionMemo");
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provide access to the TrafficController for this particular connection.
     * @return traffic controller, one is provided if null.
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
     * Provides access to the Programmer for this particular connection.
     * NOTE: Programmer defaults to null
     * @return programmer manager.
     */
    public DCCppProgrammerManager getProgrammerManager() {
        return get(DCCppProgrammerManager.class);
    }

    public void setProgrammerManager(DCCppProgrammerManager p) {
        store(p,DCCppProgrammerManager.class);
        store(p,GlobalProgrammerManager.class);
        store(p,AddressedProgrammerManager.class);
    }

    /*
     * Provides access to the Throttle Manager for this particular connection.
     */
    public ThrottleManager getThrottleManager() {
        return (ThrottleManager) classObjectMap.computeIfAbsent(ThrottleManager.class,
                (Class c) -> new DCCppThrottleManager(this));
    }

    public void setThrottleManager(ThrottleManager t) {
        store(t,ThrottleManager.class);
    }

    /*
     * Provides access to the PowerManager for this particular connection.
     */
    @Nonnull
    public PowerManager getPowerManager() {
        return (PowerManager) classObjectMap.computeIfAbsent(PowerManager.class, (Class c) -> {
            PowerManager powerManager = new DCCppPowerManager(this);
            log.debug("power manager created: {}", powerManager);
            return powerManager;
        });
    }

    public void setPowerManager(@Nonnull PowerManager p) {
        store(p,PowerManager.class);
    }

    /*
     * Provides access to the SensorManager for this particular connection.
     * NOTE: SensorManager defaults to NULL
     */
    public SensorManager getSensorManager() {
        return get(SensorManager.class);

    }

    public void setSensorManager(SensorManager s) {
        store(s,SensorManager.class);
    }

    /*
     * Provides access to the TurnoutManager for this particular connection.
     * NOTE: TurnoutManager defaults to NULL
     */
    public TurnoutManager getTurnoutManager() {
        return get(TurnoutManager.class);

    }

    public void setTurnoutManager(TurnoutManager t) {
        store(t,TurnoutManager.class);
    }

    /*
     * Provides access to the LightManager for this particular connection.
     * NOTE: Light manager defaults to NULL
     */
    public LightManager getLightManager() {
        return get(LightManager.class);

    }

    public void setLightManager(LightManager l) {
        store(l,LightManager.class);
    }

    /*
     * Provides access to the Command Station for this particular connection.
     * NOTE: Command Station defaults to NULL
     */
    public CommandStation getCommandStation() {
        return get(CommandStation.class);
    }

    public void setCommandStation(@Nonnull CommandStation c) {
        store(c,CommandStation.class);
        if ( c instanceof DCCppCommandStation ) {
            ((DCCppCommandStation) c).setTrafficController(xt);
            ((DCCppCommandStation) c).setSystemConnectionMemo(this);
        }
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

