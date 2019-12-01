package jmri.jmrix.acela;

import java.util.Comparator;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrix.SystemConnectionMemo;
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
public class AcelaSystemConnectionMemo extends SystemConnectionMemo {

    public AcelaSystemConnectionMemo() {
        this("A", AcelaConnectionTypeList.CTI); // default to A
    }

    public AcelaSystemConnectionMemo(@Nonnull String prefix, @Nonnull String userName) {
        super(prefix, userName);

        register(); // registers general type
        InstanceManager.store(this, AcelaSystemConnectionMemo.class); // also register as specific type

        // create and register the AcelaComponentFactory for the GUI
        InstanceManager.store(cf = new jmri.jmrix.acela.swing.AcelaComponentFactory(this), 
         jmri.jmrix.swing.ComponentFactory.class);
        log.debug("Created AcelaSystemConnectionMemo");
    }

    public AcelaSystemConnectionMemo(AcelaTrafficController tc) {
        super("A", AcelaConnectionTypeList.CTI); // default to A
        this.tc = tc;

        register(); // registers general type
        InstanceManager.store(this, AcelaSystemConnectionMemo.class); // also register as specific type

        // create and register the AcelaComponentFactory for the GUI
        InstanceManager.store(cf = new jmri.jmrix.acela.swing.AcelaComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
        log.debug("Created AcelaSystemConnectionMemo");
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     */
    public AcelaTrafficController getTrafficController() {
        if (tc == null) {
            setAcelaTrafficController(new AcelaTrafficController());
            log.debug("Auto create of AcelaTrafficController for initial configuration");
        }
        return tc;
    }

    /**
     * Set the traffic controller instance associated with this connection memo.
     *
     * @param tc jmri.jmrix.acela.AcelaTrafficController object to use
     */
    public void setAcelaTrafficController(AcelaTrafficController tc) {
        this.tc = tc;
    }

    private AcelaTrafficController tc;

    /**
     * Configure the common managers for Acela connections. This puts the
     * common manager config in one place.
     */
    public void configureManagers() {

        InstanceManager.setLightManager(getLightManager());

        InstanceManager.setSensorManager(getSensorManager());
        getTrafficController().setSensorManager(getSensorManager());

        InstanceManager.setTurnoutManager(getTurnoutManager());
        getTrafficController().setTurnoutManager(getTurnoutManager());
    }

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        } else if (type.equals(jmri.SensorManager.class)) {
            return true;
        } else if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        } else if (type.equals(jmri.LightManager.class)) {
            return true;
        } else {
            return false; // nothing, by default
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> type) {
        if (getDisabled()) {
            return null;
        }
        if (type.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        if (type.equals(jmri.LightManager.class)) {
            return (T) getLightManager();
        }
        return null; // nothing by default
    }

    protected AcelaTurnoutManager turnoutManager;

    public AcelaTurnoutManager getTurnoutManager() {
        if (getDisabled()) {
            return null;
        }
        if (turnoutManager == null) {
            turnoutManager = new AcelaTurnoutManager(this);
        }
        return turnoutManager;
    }

    protected AcelaSensorManager sensorManager;

    public AcelaSensorManager getSensorManager() {
        if (getDisabled()) {
            return null;
        }
        if (sensorManager == null) {
            sensorManager = new AcelaSensorManager(this);
        }
        return sensorManager;
    }

    protected AcelaLightManager lightManager;

    public AcelaLightManager getLightManager() {
        if (getDisabled()) {
            return null;
        }
        if (lightManager == null) {
            lightManager = new AcelaLightManager(this);
        }
        return lightManager;
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.acela.AcelaActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        tc = null;
        InstanceManager.deregister(this, AcelaSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaSystemConnectionMemo.class);

}
