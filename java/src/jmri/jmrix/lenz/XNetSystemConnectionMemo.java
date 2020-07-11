package jmri.jmrix.lenz;

import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.*;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.util.NamedBeanComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active and provide general
 * information
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class XNetSystemConnectionMemo extends DefaultSystemConnectionMemo {

    public XNetSystemConnectionMemo(XNetTrafficController xt) {
        super("X", Bundle.getMessage("MenuXpressNet"));
        this.xt = xt;
        xt.setSystemConnectionMemo(this);
        this.setLenzCommandStation(xt.getCommandStation());
        commonInit();
    }

    public XNetSystemConnectionMemo() {
        super("X", Bundle.getMessage("MenuXpressNet"));
        commonInit();
    }

    private void commonInit() {
        register(); // registers general type
        InstanceManager.store(this, XNetSystemConnectionMemo.class); // also register as specific type

        // create and register the XNetComponentFactory
        cf = new jmri.jmrix.lenz.swing.XNetComponentFactory(this);
        InstanceManager.store(cf, jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created XNetSystemConnectionMemo");
    }

    private jmri.jmrix.swing.ComponentFactory cf;

    /**
     * Provide access to the TrafficController for this particular connection.
     * @return traffic controller.
     */
    public XNetTrafficController getXNetTrafficController() {
        return xt;
    }

    private XNetTrafficController xt;

    public void setXNetTrafficController(XNetTrafficController xt) {
        this.xt = xt;
        // in addition to setting the traffic controller in this object,
        // set the systemConnectionMemo in the traffic controller
        xt.setSystemConnectionMemo(this);
        // and make sure the Lenz command station is set.
        this.setLenzCommandStation(xt.getCommandStation());
    }

    /**
     * Provide access to the Programmer for this particular connection.
     * <p>
     * NOTE: Programmer defaults to null
     * @return programmer manager.
     */
    public XNetProgrammerManager getProgrammerManager() {
        return get(XNetProgrammerManager.class);
    }

    public void setProgrammerManager(XNetProgrammerManager p) {
        store(p,XNetProgrammerManager.class);
        if(p.isGlobalProgrammerAvailable()) {
            store(p,GlobalProgrammerManager.class);
        }
        if(p.isAddressedModePossible()){
            store(p,AddressedProgrammerManager.class);
        }
    }

    /*
     * Provide access to the Throttle Manager for this particular connection.
     */
    public ThrottleManager getThrottleManager() {
        return get(ThrottleManager.class);
    }

    public void setThrottleManager(ThrottleManager t) {
        store(t,ThrottleManager.class);
    }

    /*
     * Provide access to the PowerManager for this particular connection.
     */
    public PowerManager getPowerManager() {
        return get(PowerManager.class);
    }

    public void setPowerManager(PowerManager p) {
        store(p,PowerManager.class);
    }

    /**
     * Provide access to the SensorManager for this particular connection.
     * <p>
     * NOTE: SensorManager defaults to NULL
     * @return sensor manager.
     */
    public SensorManager getSensorManager() {
        return get(SensorManager.class);
    }

    public void setSensorManager(SensorManager s) {
        store(s, SensorManager.class);
    }

    /**
     * Provide access to the TurnoutManager for this particular connection.
     * <p>
     * NOTE: TurnoutManager defaults to NULL
     * @return turnout manager.
     */
    public TurnoutManager getTurnoutManager() {
        return get(TurnoutManager.class);

    }

    public void setTurnoutManager(TurnoutManager t) {
        store(t,TurnoutManager.class);
    }

    /**
     * Provide access to the LightManager for this particular connection.
     * <p>
     * NOTE: LightManager defaults to NULL
     * @return light manager.
     */
    public LightManager getLightManager() {
        return get(LightManager.class);

    }

    public void setLightManager(LightManager l) {
        store(l,LightManager.class);
    }

    /**
     * Provide access to the Command Station for this particular connection.
     * <p>
     * NOTE: Command Station defaults to NULL
     * @return command station.
     */
    public CommandStation getCommandStation() {
        return get(CommandStation.class);
    }

    public void setCommandStation(CommandStation c) {

        if (c instanceof LenzCommandStation ) {
            setLenzCommandStation((LenzCommandStation) c);
            // don't set as command station object if instruction
            // not supported (Lenz Compact)
            if(((LenzCommandStation)c).getCommandStationType()!=0x02) {
                store(c, CommandStation.class);
            }
        } else {
            store(c,CommandStation.class);
        }
    }

    /**
     * Provide access to the Lenz Command Station for this particular connection.
     * <p>
     * NOTE: Lenz Command Station defaults to NULL
     * @return Lenz command station.
     */
    public LenzCommandStation getLenzCommandStation() {
        return get(LenzCommandStation.class);
    }

    public void setLenzCommandStation(LenzCommandStation c) {
        store(c,LenzCommandStation.class);
        c.setTrafficController(xt);
        c.setSystemConnectionMemo(this);
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.lenz.XNetActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        xt = null;
        InstanceManager.deregister(this, XNetSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    private static final Logger log = LoggerFactory.getLogger(XNetSystemConnectionMemo.class);

}
